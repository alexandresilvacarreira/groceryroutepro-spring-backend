package pt.upskill.groceryroutepro.services;

import okhttp3.*;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.Chain;
import pt.upskill.groceryroutepro.entities.Price;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.models.ScraperParams;
import pt.upskill.groceryroutepro.repositories.CategoryRepository;
import pt.upskill.groceryroutepro.repositories.ChainRepository;
import pt.upskill.groceryroutepro.repositories.PriceRepository;
import pt.upskill.groceryroutepro.repositories.ProductRepository;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScraperServiceImpl implements ScraperService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    PriceRepository priceRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ChainRepository chainRepository;

    @Autowired
    GenericProductsService genericProductsService;

    private List<String> userAgentList = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    );

    private boolean endOfCategory;

    private int productsInCategory;

    private int pagesInCategory;

    private List<String> categories = Arrays.asList("mercearia", "frutas e legumes", "congelados", "laticínios e ovos", "peixaria", "talho", "charcutaria", "alternativas alimentares, bio, saudável", "bebidas", "padaria e pastelaria");

    /************************** CONTINENTE **************************/

    @Override
    public void scrapeContinenteAll() {

        int size = 100;

        List<ScraperParams> scraperParamsList = new ArrayList<>();
        scraperParamsList.add(new ScraperParams("https://www.continente.pt/on/demandware.store/Sites-continente-Site/default/Search-UpdateGrid?cgid=mercearias&pmin=0%2e01", "mercearia"));
        scraperParamsList.add(new ScraperParams("https://www.continente.pt/on/demandware.store/Sites-continente-Site/default/Search-UpdateGrid?cgid=frutas-legumes&pmin=0%2e01", "frutas e legumes"));
        scraperParamsList.add(new ScraperParams("https://www.continente.pt/on/demandware.store/Sites-continente-Site/default/Search-UpdateGrid?cgid=congelados&pmin=0%2e01", "congelados"));
        scraperParamsList.add(new ScraperParams("https://www.continente.pt/on/demandware.store/Sites-continente-Site/default/Search-UpdateGrid?cgid=laticinios&pmin=0%2e01", "laticínios e ovos"));
        scraperParamsList.add(new ScraperParams("https://www.continente.pt/on/demandware.store/Sites-continente-Site/default/Search-UpdateGrid?cgid=peixaria-e-talho-peixaria&pmin=0%2e01", "peixaria"));
        scraperParamsList.add(new ScraperParams("https://www.continente.pt/on/demandware.store/Sites-continente-Site/default/Search-UpdateGrid?cgid=peixaria-e-talho-talho&pmin=0%2e01", "talho"));
        scraperParamsList.add(new ScraperParams("https://www.continente.pt/on/demandware.store/Sites-continente-Site/default/Search-UpdateGrid?cgid=charcutaria-queijo&pmin=0%2e01", "charcutaria"));
        scraperParamsList.add(new ScraperParams("https://www.continente.pt/on/demandware.store/Sites-continente-Site/default/Search-UpdateGrid?cgid=biologicos&pmin=0%2e01", "alternativas alimentares, bio, saudável"));
        scraperParamsList.add(new ScraperParams("https://www.continente.pt/on/demandware.store/Sites-continente-Site/default/Search-UpdateGrid?cgid=bebidas&pmin=0%2e01", "bebidas"));
        scraperParamsList.add(new ScraperParams("https://www.continente.pt/on/demandware.store/Sites-continente-Site/default/Search-UpdateGrid?cgid=padaria-e-pastelaria&pmin=0%2e01", "padaria e pastelaria"));

        for (ScraperParams scraperParams :
                scraperParamsList) {

            int start = 0;

            this.endOfCategory = false;

            while (!this.endOfCategory) {

                // Sleeps aleatórios entre pedidos para evitar bloqueios
                Random random = new Random();
                int randomTimeout = random.nextInt(4000) + 1000;
                try {
                    Thread.sleep(randomTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String url = scraperParams.getUrl() + "&start=" + start + "&sz=" + size;
                this.scrapeContinente(url, scraperParams.getCategory());
                start += size;
            }

        }
    }

    @Override
    public void scrapeContinente(String url, String category) {

        try {

            // Pedido à API da loja
            Random random = new Random();
            int randomIndex = random.nextInt(this.userAgentList.size());
            Connection connection = Jsoup.connect(url)
                    .userAgent(this.userAgentList.get(randomIndex))
                    .header("Connection", "keep-alive");

            Document document = connection.get();

            // Produtos desta loja estão na classe .product
            Elements productElements = document.select(".product");
            if (productElements == null || productElements.isEmpty()) {
                this.endOfCategory = true;
                return;
            }

            // Iterar produtos
            for (Element productElement : productElements) {

                // Inicializar produto
                Element nameElement = productElement.select(".pwc-tile--description").first();
                if (nameElement == null) {
                    continue;
                }
                String name = nameElement.text();
                String quantity = productElement.select(".pwc-tile--quantity").text();
                String brand = productElement.select(".col-tile--brand").text();
                String imageUrl = productElement.select(".ct-tile-image").attr("data-src");
                Chain chain = chainRepository.findByName("continente");
                Product product = this.createOrGetProduct(name, quantity, brand, imageUrl, chain.getId());
                product.setChain(chain);
                product.getCategories().add(categoryRepository.findByName(category));


                // Info relativa ao desconto
                Element discountPercentageElement = productElement.select("p.pwc-discount-amount.col-discount-amount:not(.pwc-info-amount-iva-zero .pwc-discount-amount-pvpr)").first();
                int discountPercentage = 0;
                if (discountPercentageElement != null) {
                    String discountPercentageStr = discountPercentageElement.text();
                    if (discountPercentageStr.contains("Desconto Imediato: ")) {
                        try {
                            discountPercentage = Integer.parseInt(discountPercentageElement.text().replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException e) {
                            System.out.println("Product ID: " + product.getId());
                            System.out.println("Product name: " + name);
                            System.out.println("Product brand: " + brand);
                            System.out.println(e.getMessage());
                        }
                    }
                }
                Element priceWithoutDiscountElement = productElement.select(".pwc-tile--price-dashed").first();
                String priceWithoutDiscount = "";
                if (priceWithoutDiscountElement != null) {
                    priceWithoutDiscount = priceWithoutDiscountElement.select(".pwc-tile--price-value").text().replaceAll("[^0-9,]", "") + " €" +
                            productElement.select(".pwc-m-unit").first().text();
                }

                // Atualizar produto
                product.setName(name);
                product.setBrand(brand);
                product.setQuantity(quantity);
                product.setImageUrl(imageUrl);

                // Obter preços

                // Primário
                Element primaryPriceElement = productElement.select(".pwc-tile--price-primary").first();
                if (primaryPriceElement == null) {
                    System.out.println("Product name: " + name);
                    System.out.println("Product brand: " + brand);
                    System.out.println("primaryPriceElement null: " + url);
                    continue;
                }

                String primaryValueStr = primaryPriceElement.select(".ct-price-formatted").text();
                int lastCommaIndex = primaryValueStr.lastIndexOf(",");
                String commaPrimaryValueStr = primaryValueStr.substring(0, lastCommaIndex) + "." + primaryValueStr.substring(lastCommaIndex + 1);
                String modifiedPrimaryValueStr = commaPrimaryValueStr.replaceAll("[^0-9.]", "");
                double primaryValue = 0.0;
                try {
                    primaryValue = Double.parseDouble(modifiedPrimaryValueStr);
                } catch (NumberFormatException e) {
                    System.out.println("Product ID: " + product.getId());
                    System.out.println("Product name: " + name);
                    System.out.println("Product brand: " + brand);
                    System.out.println(e.getMessage());
                }

                String primaryUnit = primaryPriceElement.select(".pwc-m-unit").text().replace("/", "");

                // Secundário (normalmente é o preço por kg)
                Element secondaryPriceElement = productElement.select(".pwc-tile--price-secondary").first();
                double secondaryValue = 0.0;
                String secondaryUnit = "";
                if (secondaryPriceElement == null) {
                    System.out.println("Product name: " + name);
                    System.out.println("Product brand: " + brand);
                    System.out.println("secondaryPriceElement null: " + url);
                } else {
                    String secondaryValueStr = secondaryPriceElement.select(".ct-price-value").text();
                    int lastCommaIndexSecondary = secondaryValueStr.lastIndexOf(",");
                    String commaSecondaryValueStr = secondaryValueStr.substring(0, lastCommaIndexSecondary) + "." + secondaryValueStr.substring(lastCommaIndexSecondary + 1);
                    String modifiedSecondaryValueStr = commaSecondaryValueStr.replaceAll("[^0-9.]", "");
                    try {
                        secondaryValue = Double.parseDouble(modifiedSecondaryValueStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Product ID: " + product.getId());
                        System.out.println("Product name: " + name);
                        System.out.println("Product brand: " + brand);
                        System.out.println(e.getMessage());
                    }
                    secondaryUnit = secondaryPriceElement.select(".pwc-m-unit").text().replace("/", "");
                }

                // Instanciar preço
                Price price = new Price();
                price.setPrimaryValue(primaryValue);
                price.setPrimaryUnit(primaryUnit);
                price.setSecondaryValue(secondaryValue);
                price.setSecondaryUnit(secondaryUnit);
                price.setDiscountPercentage(discountPercentage);
                price.setPriceWoDiscount(priceWithoutDiscount);
                price.setCollectionDate(LocalDateTime.now());

                // Associar preço ao produto
                price.setProduct(product);

                // Adicionar o preço à lista do produto
                product.getPrices().add(price);

                // Guardar produto
                productRepository.save(product);

                //Atualizar GenericProducts
                genericProductsService.updateGenericProducts(product);

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    /************************** AUCHAN **************************/

    @Override
    public void scrapeAuchanAll() {

        int size = 100;

        List<ScraperParams> scraperParamsList = new ArrayList<>();
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=mercearia&prefn1=soldInStores&prefv1=000", "mercearia"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=fruta&prefn1=soldInStores&prefv1=000", "frutas e legumes"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=legumes&prefn1=soldInStores&prefv1=000", "frutas e legumes"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=congelados&prefn1=soldInStores&prefv1=000", "congelados"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=produtos-lacteos&prefn1=soldInStores&prefv1=000", "laticínios e ovos"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=peixaria&prefn1=soldInStores&prefv1=000", "peixaria"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=talho&prefn1=soldInStores&prefv1=000", "talho"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=charcutaria&prefn1=soldInStores&prefv1=000", "charcutaria"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=biologico-e-escolhas-alimentares&prefn1=soldInStores&prefv1=000", "alternativas alimentares, bio, saudável"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=bebidas-e-garrafeira&prefn1=soldInStores&prefv1=000", "bebidas"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=padaria&prefn1=soldInStores&prefv1=000", "padaria e pastelaria"));
        scraperParamsList.add(new ScraperParams("https://www.auchan.pt/on/demandware.store/Sites-AuchanPT-Site/pt_PT/Search-UpdateGrid?cgid=pastelaria&prefn1=soldInStores&prefv1=000", "padaria e pastelaria"));

        for (ScraperParams scraperParams :
                scraperParamsList) {

            this.endOfCategory = false;

            int start = 0;

            while (!this.endOfCategory) {

                // Sleeps aleatórios entre pedidos para evitar bloqueios
                Random random = new Random();
                int randomTimeout = random.nextInt(4000) + 1000;
                try {
                    Thread.sleep(randomTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String url = scraperParams.getUrl() + "&start=" + start + "&sz=" + size;
                this.scrapeAuchan(url, scraperParams.getCategory());
                start += size;
            }

        }
    }

    @Override
    public void scrapeAuchan(String url, String category) {

        try {

            // Pedido à API da loja
            Random random = new Random();
            int randomIndex = random.nextInt(this.userAgentList.size());
            Connection connection = Jsoup.connect(url)
                    .userAgent(this.userAgentList.get(randomIndex))
                    .header("Connection", "keep-alive");
            Document document = connection.get();

            // Produtos desta loja estão na classe .product
            Elements productElements = document.select(".product");
            if (productElements == null || productElements.isEmpty()) {
                this.endOfCategory = true;
                return;
            }

            // Iterar produtos
            for (Element productElement : productElements) {

                Element productNameElement = productElement.select(".auc-product-tile__name a").first();
                if (productNameElement == null) {
                    continue;
                }
                String name = productNameElement.text();
                String brand = ""; // Auchan inclui a marca no nome dos produtos, não temos uma classe à parte para obter esta informação

                // Quantidade - não disponível em todos, quando não existe usamos a última palavra do nome
                Element quantityElement = productElement.select(".auc-measures--avg-weight").first();
                String quantity = "";
                if (quantityElement != null) {
                    quantity = quantityElement.text();
                } else {
                    String[] nameWords = name.split(" ");
                    quantity = nameWords[nameWords.length - 1];
                }
                String imageUrl = productElement.select(".tile-image").attr("data-src");

                // Info relativa ao desconto
                Element discountPercentageElement = productElement.select(".auc-promo--discount--red").first();
                int discountPercentage = 0;
                if (discountPercentageElement != null) {
                    try {
                        discountPercentage = Integer.parseInt(discountPercentageElement.text().replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException e) {
                        System.out.println("Product name: " + name);
                        System.out.println("Product brand: " + brand);
                        System.out.println(e.getMessage());
                    }
                }
                Element priceWithoutDiscountElement = productElement.select(".auc-price__stricked").first();
                String priceWithoutDiscount = "";
                if (priceWithoutDiscountElement != null) {
                    priceWithoutDiscount = priceWithoutDiscountElement.select("span.strike-through, span.value").attr("content").replace(".", ",") + " €";
                    // Nem todos têm unidade, adicionar só nos que têm
                    Element discountUnit = priceWithoutDiscountElement.select(".auc-avgWeight").first();
                    if (discountUnit != null) {
                        priceWithoutDiscount += discountUnit.text();
                    }
                }

                // Atualizar produto
                Chain chain = chainRepository.findByName("auchan");
                Product product = this.createOrGetProduct(name, quantity, brand, imageUrl, chain.getId());
                product.setChain(chain);
                product.getCategories().add(categoryRepository.findByName(category));
                product.setName(name);
                product.setBrand(brand);
                product.setQuantity(quantity);
                product.setImageUrl(imageUrl);

                // Obter preços

                // Primário
                Element primaryPriceElement = productElement.select(".auc-product-tile__prices .sales").first();
                if (primaryPriceElement == null) {
                    System.out.println("primaryPriceElement null for name: " + name + " url: " + url);
                    continue;
                }
                String primaryValueStr = primaryPriceElement.select(".value").attr("content");
                double primaryValue = 0.0;
                try {
                    primaryValue = Double.parseDouble(primaryValueStr);
                } catch (NumberFormatException e) {
                    System.out.println("Product name: " + name);
                    System.out.println("Product brand: " + brand);
                    System.out.println(e.getMessage());
                }

                Element primaryUnitElement = primaryPriceElement.selectFirst(".auc-avgWeight");
                String primaryUnit = "";
                if (primaryUnitElement != null) {
                    primaryUnit = primaryUnitElement.text().replace("/", "");
                }

                // Secundário (normalmente é o preço por kg)
                Element secondaryPriceElement = productElement.select(".auc-measures--price-per-unit").first();
                double secondaryValue = 0.0;
                String secondaryUnit = "";
                if (secondaryPriceElement != null) {
                    String secondaryValueStr = secondaryPriceElement.text().replaceAll("[^0-9.]", "");
                    secondaryUnit = secondaryPriceElement.text().substring(secondaryPriceElement.text().lastIndexOf('/') + 1).trim();
                    try {
                        secondaryValue = Double.parseDouble(secondaryValueStr);
                    } catch (NumberFormatException e) {
                        System.out.println("Product name: " + name);
                        System.out.println("Product brand: " + brand);
                        System.out.println(e.getMessage());
                    }
                }

                // Instanciar preço
                Price price = new Price();
                price.setPrimaryValue(primaryValue);
                price.setPrimaryUnit(primaryUnit);
                price.setSecondaryValue(secondaryValue);
                price.setSecondaryUnit(secondaryUnit);
                price.setDiscountPercentage(discountPercentage);
                price.setPriceWoDiscount(priceWithoutDiscount);
                price.setCollectionDate(LocalDateTime.now());

                // Associar preço ao produto
                price.setProduct(product);

                // Adicionar o preço à lista do produto
                product.getPrices().add(price);

                // Guardar produto
                productRepository.save(product);

                //Atualizar GenericProducts
                genericProductsService.updateGenericProducts(product);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /************************** MINIPREÇO **************************/

    @Override
    public void scrapeMiniprecoAll() {

        List<ScraperParams> scraperParamsList = new ArrayList<>();
        scraperParamsList.add(new ScraperParams("https://www.minipreco.pt/produtos/mercearia/c/WEB.003.000.00000?q=%3Arelevance", "mercearia"));
        scraperParamsList.add(new ScraperParams("https://www.minipreco.pt/produtos/frutas-e-vegetais/c/WEB.001.000.00000?q=%3Arelevance", "frutas e legumes"));
        scraperParamsList.add(new ScraperParams("https://www.minipreco.pt/produtos/gelados-e-congelados/c/WEB.006.000.00000?q=%3Arelevance", "congelados"));
        scraperParamsList.add(new ScraperParams("https://www.minipreco.pt/produtos/laticinios-e-ovos/c/WEB.005.000.00000?q=%3Arelevance", "laticínios e ovos"));
        scraperParamsList.add(new ScraperParams("https://www.minipreco.pt/produtos/talho-e-peixaria/peixaria/c/WEB.022.001.00000", "peixaria"));
        scraperParamsList.add(new ScraperParams("https://www.minipreco.pt/produtos/talho-e-peixaria/talho/c/WEB.022.002.00000?q=%3Arelevance", "talho"));
        scraperParamsList.add(new ScraperParams("https://www.minipreco.pt/produtos/charcutaria-e-queijos/c/WEB.021.000.00000?q=%3Arelevance", "charcutaria"));
        scraperParamsList.add(new ScraperParams("https://www.minipreco.pt/produtos/equilibrio-e-bio/c/WEB.019.000.00000?q=%3Arelevance", "alternativas alimentares, bio, saudável"));
        scraperParamsList.add(new ScraperParams("https://www.minipreco.pt/produtos/bebidas-e-garrafeira/c/WEB.007.000.00000?q=%3Arelevance", "bebidas"));
        scraperParamsList.add(new ScraperParams("https://www.minipreco.pt/produtos/padaria-e-pastelaria/c/WEB.002.000.00000?q=%3Arelevance", "padaria e pastelaria"));


        for (ScraperParams scraperParams :
                scraperParamsList) {

            this.endOfCategory = false;

            int page = 0;

            while (!this.endOfCategory) {

                // Sleeps aleatórios entre pedidos para evitar bloqueios
                Random random = new Random();
                int randomTimeout = random.nextInt(4000) + 1000;
                try {
                    Thread.sleep(randomTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (scraperParams.getCategory().equals("peixaria")) {
                    scrapeMinipreco(scraperParams.getUrl(), scraperParams.getCategory());
                    this.endOfCategory = true;
                } else {
                    String url = scraperParams.getUrl() + "&page=" + page + "&disp=";
                    scrapeMinipreco(url, scraperParams.getCategory());
                    page += 1;
                }
            }

        }
    }

    @Override
    public void scrapeMinipreco(String url, String category) {

        try {

            // Pedido à API da loja
            Random random = new Random();
            int randomIndex = random.nextInt(this.userAgentList.size());
            Connection connection = Jsoup.connect(url)
                    .userAgent(this.userAgentList.get(randomIndex))
                    .header("Connection", "keep-alive");
            Document document = connection.get();

            // Produtos desta loja estão na classe .product-list__item
            Elements productElements = document.select(".product-list__item");
            if (productElements == null || productElements.isEmpty()) {
                this.endOfCategory = true;
                return;
            } else {
                // Fim da categoria é indicado por um redirect para a página com a listagem de todos os produtos
                Element pageTitleElement = document.select(".category-page-title").first();
                if (pageTitleElement != null) {
                    String pageTitle = pageTitleElement.text().toLowerCase();
                    if (pageTitle.equals("produtos")) {
                        this.endOfCategory = true;
                        return;
                    }
                }
            }

            // Iterar produtos
            for (Element productElement : productElements) {

                Element nameElement = productElement.select(".details").first();
                if (nameElement == null) {
                    continue;
                }

                String name = nameElement.text();

                // Minipreco normalmente inclui a marca em maiúsculas no início do nome dos produtos, por defeito usamos Minipreco
                String brand = "Minipreço";
                Pattern pattern = Pattern.compile("^([A-Z]+\\s?)+");
                Matcher matcher = pattern.matcher(name);

                if (matcher.find()) {
                    brand = matcher.group().trim();
                    // Temos de truncar a string, senão vem também a primeira letra do resto do nome
                    if (!brand.isEmpty()) {
                        try {
                            brand = brand.substring(0, brand.length() - 1);
                        } catch (IndexOutOfBoundsException e) {
                            brand = "";
                            System.out.println(name);
                            System.out.println(e.getMessage());
                        }
                    }
                }

                // Quantidade - ora são as duas últimas palavras, ora o que está em parênteses no fim do nome
                String quantity = "";
                if (name.lastIndexOf(")") == name.length() - 1) {
                    try {
                        quantity = name.substring(name.lastIndexOf("(") + 1, name.lastIndexOf(")"));
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("Could not get quantity for: " + url);
                        System.out.println("Product name: " + name);
                        System.out.println(e.getMessage());
                    }
                } else {
                    String[] nameWords = name.split(" ");
                    try {
                        quantity = nameWords[nameWords.length - 2] + nameWords[nameWords.length - 1];
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("Could not get quantity for: " + url);
                        System.out.println("Product name: " + name);
                        System.out.println(e.getMessage());
                    }
                }

                String imageUrl = productElement.select(".thumb img").first().attr("data-original");

                // Info relativa ao desconto
                Element discountPercentageElement = productElement.select(".promotion_text").first();
                int discountPercentage = 0;
                if (discountPercentageElement != null) {
                    try {
                        discountPercentage = Integer.parseInt(discountPercentageElement.text().replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException e) {
                        System.out.println("Product name: " + name);
                        System.out.println("Product brand: " + brand);
                        System.out.println("url: " + url);
                        System.out.println(e.getMessage());
                    }
                }

                Element priceWithoutDiscountElement = productElement.select("p.price s").first();
                String priceWithoutDiscount = "";
                if (priceWithoutDiscountElement != null) {
                    priceWithoutDiscount = priceWithoutDiscountElement.text();
                }

                // Atualizar produto
                Chain chain = chainRepository.findByName("minipreço");
                Product product = this.createOrGetProduct(name, quantity, brand, imageUrl, chain.getId());
                product.setChain(chain);
                product.getCategories().add(categoryRepository.findByName(category));
                product.setName(name);
                product.setBrand(brand);
                product.setQuantity(quantity);
                product.setImageUrl(imageUrl);

                // Obter preços

                // Primário
                Element primaryPriceElement = productElement.select("p.price").first();
                if (primaryPriceElement == null) {
                    System.out.println("primaryPriceElement is null for: " + name);
                    System.out.println("Product brand: " + brand);
                    System.out.println("url: " + url);
                    continue;
                }

                String primaryValueStr = primaryPriceElement.ownText().replaceAll("[^0-9,]", "").replace(",", ".");
                double primaryValue = 0.0;
                try {
                    primaryValue = Double.parseDouble(primaryValueStr);
                } catch (NumberFormatException e) {
                    System.out.println("Product name: " + name);
                    System.out.println("Product brand: " + brand);
                    System.out.println("url: " + url);
                    System.out.println(e.getMessage());
                }

                String primaryUnit = "";

                // Secundário (normalmente é o preço por kg)
                Element secondaryPriceElement = productElement.select(".pricePerKilogram").first();
                double secondaryValue = 0.0;
                String secondaryUnit = "";
                if (secondaryPriceElement != null) {
                    String secondaryPriceString = secondaryPriceElement.ownText();
                    String secondaryValueStr = secondaryPriceString.replaceAll("[^0-9,]", "").replace(",", ".");
                    try {
                        secondaryValue = Double.parseDouble(secondaryValueStr);
                        secondaryUnit = secondaryPriceString.substring(secondaryPriceString.lastIndexOf("/") + 1, secondaryPriceString.length() - 2);
                    } catch (NumberFormatException e) {
                        System.out.println("Product name: " + name);
                        System.out.println("url: " + url);
                        System.out.println(e.getMessage());
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("Product name: " + name);
                        System.out.println("url: " + url);
                        System.out.println(e.getMessage());
                    }
                }

                // Instanciar preço
                Price price = new Price();
                price.setPrimaryValue(primaryValue);
                price.setPrimaryUnit(primaryUnit);
                price.setSecondaryValue(secondaryValue);
                price.setSecondaryUnit(secondaryUnit);
                price.setDiscountPercentage(discountPercentage);
                price.setPriceWoDiscount(priceWithoutDiscount);
                price.setCollectionDate(LocalDateTime.now());

                // Associar preço ao produto
                price.setProduct(product);

                // Adicionar o preço à lista do produto
                product.getPrices().add(price);

                // Guardar produto
                productRepository.save(product);

                //Atualizar GenericProducts
                genericProductsService.updateGenericProducts(product);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /************************** PINGO DOCE **************************/

    @Override
    public void scrapePingoDoceAll() {

        int size = 100;

        List<ScraperParams> scraperParamsList = new ArrayList<>();
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eedde7fd2bff003f50812d%22%5D&esPreference=0.20779248609075285", "mercearia"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eedde8fd2bff003f508138%22%5D&esPreference=0.20779248609075285", "frutas e legumes"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eedde9fd2bff003f50815a%22%5D&esPreference=0.20779248609075285", "congelados"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eedde9fd2bff003f508168%22%5D&esPreference=0.20779248609075285", "laticínios e ovos"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eeddedfd2bff003f5081f4%22%5D&esPreference=0.9580062688292429", "laticínios e ovos"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eede01fd2bff003f508304%22%5D&esPreference=0.9580062688292429", "laticínios e ovos"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eedde8fd2bff003f508144%22%5D&esPreference=0.20779248609075285", "peixaria"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eedde8fd2bff003f50813d%22%5D&esPreference=0.20779248609075285", "talho"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eedde8fd2bff003f50814a%22%5D&esPreference=0.20779248609075285", "charcutaria"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eeddedfd2bff003f5081f6%22%5D&esPreference=0.9580062688292429", "charcutaria"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eedde9fd2bff003f508174%22%5D&esPreference=0.20779248609075285", "alternativas alimentares, bio, saudável"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eeddeafd2bff003f508185%22%5D&esPreference=0.9580062688292429", "bebidas"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eedde9fd2bff003f508173%22%5D&esPreference=0.9580062688292429", "bebidas"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eedde8fd2bff003f508155%22%5D&esPreference=0.9580062688292429", "padaria e pastelaria"));
        scraperParamsList.add(new ScraperParams("https://mercadao.pt/api/catalogues/6107d28d72939a003ff6bf51/products/search?mainCategoriesIds=%5B%2261eeddedfd2bff003f5081fe%22%5D&esPreference=0.9580062688292429", "padaria e pastelaria"));


        for (ScraperParams scraperParams :
                scraperParamsList) {

            this.productsInCategory = 100;

            int from = 0;

            while (from <= this.productsInCategory) {

                // Sleeps aleatórios entre pedidos para evitar bloqueios
                Random random = new Random();
                int randomTimeout = random.nextInt(4000) + 1000;
                try {
                    Thread.sleep(randomTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String url = scraperParams.getUrl() + "&from=" + from + "&size=" + size;
                scrapePingoDoce(url, scraperParams.getCategory());
                from += size;

            }

        }
    }

    @Override
    public void scrapePingoDoce(String url, String category) {

        try {

            Random random = new Random();
            int randomIndex = random.nextInt(this.userAgentList.size());

            // Gerado pelo Postman
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", this.userAgentList.get(randomIndex))
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Language", "pt-PT,pt;q=0.8,en;q=0.5,en-US;q=0.3")
//                    .addHeader("Accept-Encoding", "gzip, deflate, br")
//                    .addHeader("Referer", "https://mercadao.pt/store/pingo-doce/category/frutas-e-legumes-12")
                    .addHeader("X-Version", "3.16.0")
                    .addHeader("X-Name", "webapp")
                    .addHeader("ngsw-bypass", "true")
                    .addHeader("Connection", "keep-alive")
//                    .addHeader("Cookie", "OptanonConsent=isGpcEnabled=0&datestamp=Wed+Dec+20+2023+20%3A57%3A22+GMT%2B0000+(Hora+padr%C3%A3o+da+Europa+Ocidental)&version=202301.2.0&isIABGlobal=false&hosts=&landingPath=NotLandingPage&groups=C0002%3A1%2CC0001%3A1%2CC0005%3A1%2CC0004%3A1&geolocation=PT%3B11&AwaitingReconsent=false; OptanonAlertBoxClosed=2023-12-05T18:33:49.947Z")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Site", "same-origin")
                    .addHeader("TE", "trailers")
                    .build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> responseMap = jsonParser.parseMap(responseString);
            List<Map<String, Object>> products = (List<Map<String, Object>>) ((Map<String, Object>) ((Map<String, Object>) responseMap.get("sections")).get("null")).get("products");
            this.productsInCategory = ((int) ((Map<String, Object>) ((Map<String, Object>) responseMap.get("sections")).get("null")).get("total"));

            if (products == null || products.isEmpty()) {
                return;
            }

            //Iterar produtos
            for (Map<String, Object> productMap : products) {

                // Info do produto vem no _source
                Map<String, Object> productData = (Map<String, Object>) productMap.get("_source");

                String imageUrl = "https://res.cloudinary.com/fonte-online/image/upload/c_fill,h_300,q_auto,w_300/v1/PDO_PROD/" + productData.get("sku") + "_1";
                String name = (String) productData.get("firstName");
                String brand = (String) ((Map<String, Object>) productData.get("brand")).get("name");
                String quantity = "";
                double primaryValue = 0.0;
                try {
                    Object primaryValueElement = productData.get("buyingPrice");
                    if (primaryValueElement instanceof Integer) {
                        primaryValue = (int) primaryValueElement;
                    } else {
                        primaryValue = (double) Math.round((double) productData.get("buyingPrice") * 100) / 100;
                    }
                } catch (ClassCastException e) {
                    System.out.println("primaryValue failed for: \n" + name + "\n url: " + url);
                    continue;
                }

                String primaryUnit = "";
                double secondaryValue = 0.0;
                String secondaryUnit = ((String) productData.get("netContentUnit")).toLowerCase();
                if ((productData.get("capacity")).equals("0")) {
                    quantity = (productData.get("averageWeight") + " " + productData.get("netContentUnit")).toLowerCase();
                    primaryUnit = ((String) productData.get("netContentUnit")).toLowerCase();
                    secondaryValue = primaryValue;
                } else {
                    quantity = ((String) productData.get("capacity")).toLowerCase();
                    try {
                        Object netContentObject = productData.get("netContent");
                        double netContent;
                        if (netContentObject instanceof Integer) {
                            netContent = (int) netContentObject;
                        } else {
                            netContent = (double) netContentObject;
                        }
                        secondaryValue = (double) Math.round((primaryValue / netContent) * 100) / 100;
                    } catch (ClassCastException e) {
                        System.out.println("secondaryValue failed for: \n" + name + "\n url: " + url);
                    }

                }

                int discountPercentage = 0;
                String priceWithoutDiscount = "";
                Map<String, Object> promotion = (Map<String, Object>) productData.get("promotion");
                if (promotion != null && promotion.get("amount") != null && promotion.get("type").equals("PERCENTAGE")) {
                    try {
                        Object promotionObject = promotion.get("amount");
                        double promotionAmount;
                        if (promotionObject instanceof Integer) {
                            promotionAmount = (int) promotionObject;
                        } else {
                            promotionAmount = (double) promotionObject;
                        }
                        discountPercentage = (int) Math.round(promotionAmount);
                    } catch (ClassCastException e) {
                        System.out.println("discountPercentage failed for: \n" + name + "\n url: " + url);
                    }
                    try {
                        Object priceWoDiscountObject = productData.get("regularPrice");
                        double priceWoDiscount;
                        if (priceWoDiscountObject instanceof Integer) {
                            priceWoDiscount = (int) priceWoDiscountObject;
                        } else {
                            priceWoDiscount = (double) priceWoDiscountObject;
                        }
                        priceWithoutDiscount = ((double) Math.round(priceWoDiscount * 100) / 100) + " €";
                    } catch (ClassCastException e) {
                        System.out.println("priceWithoutDiscount failed for: \n" + name + "\n url: " + url);
                    }
                }

                // Atualizar produto
                Chain chain = chainRepository.findByName("pingo doce");
                Product product = this.createOrGetProduct(name, quantity, brand, imageUrl, chain.getId());
                product.setChain(chain);
                product.getCategories().add(categoryRepository.findByName(category));
                product.setName(name);
                product.setBrand(brand);
                product.setQuantity(quantity);
                product.setImageUrl(imageUrl);

                // Instanciar preço
                Price price = new Price();
                price.setPrimaryValue(primaryValue);
                price.setPrimaryUnit(primaryUnit);
                price.setSecondaryValue(secondaryValue);
                price.setSecondaryUnit(secondaryUnit);
                price.setDiscountPercentage(discountPercentage);
                price.setPriceWoDiscount(priceWithoutDiscount);
                price.setCollectionDate(LocalDateTime.now());

                // Associar preço ao produto
                price.setProduct(product);

                // Adicionar o preço à lista do produto
                product.getPrices().add(price);

                // Guardar produto
                productRepository.save(product);

                //Atualizar GenericProducts
                genericProductsService.updateGenericProducts(product);

            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    /************************** INTERMARCHÉ **************************/

    @Override
    public void scrapeIntermarcheAll() {

        // Começamos por obter os ids de todas as subcategorias do intermarché (a api não permite pedidos às categorias principais)

        Map<String, List<Integer>> subcategoriesIntermarche = new HashMap<>();

        for (String category :
                this.categories) {
            String categoryIdsName = "ids_" + category;
            String categoryNbProductsName = "nbProducts_" + category;
            subcategoriesIntermarche.put(categoryIdsName, new ArrayList<>());
            subcategoriesIntermarche.put(categoryNbProductsName, new ArrayList<>());
        }

        Random random = new Random();
        int randomIndex = random.nextInt(this.userAgentList.size());

        try {

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url("https://www.loja-online.intermarche.pt/api/service/arborescence-produit/v1/pdvs/03622/categories")
                    .addHeader("User-Agent", this.userAgentList.get(randomIndex))
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Language", "pt-PT,pt;q=0.8,en;q=0.5,en-US;q=0.3")
//                    .addHeader("Accept-Encoding", "gzip, deflate, br")
                    .addHeader("x-red-device", "red_fo_desktop")
                    .addHeader("x-red-version", "3")
                    .addHeader("x-service-name", "arborescence-produit")
                    .addHeader("x-itm-device-fp", "ead3ce12-5cec-43d6-abf0-62d10b4e0bcd")
                    .addHeader("x-itm-session-id", "7ea3cd74-a97c-4ca5-87ca-d634800964fa")
                    .addHeader("x-pdv", "{\"ref\":\"03622\",\"isEcommerce\":true}")
                    .addHeader("Alt-Used", "www.loja-online.intermarche.pt")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Referer", "https://www.loja-online.intermarche.pt/shelves/mercearia/arroz-massa-e-farinha/farinha/11717")
//                    .addHeader("Cookie", "datadome=K0uYCkbUK7VY~sl5a0SNRG9SkvjBlGu6oJC8vqETdwKtCXjkrJVcxk9XujJNL8n6W5UQ1WgZWVMKqlC8MGgKMymImAlq_8CLbbues8oCOFjOAObGQru8nFHqj_T9AdJT; itm_device_id=08658591-bbcd-4d03-9219-db4bf532a7bd; itm_usid=a02ca707-a700-4c4c-91e2-8864839266af; itm_pdv={%22ref%22:%2203622%22%2C%22isEcommerce%22:true}; novaParams={%22pdvRef%22:%2203622%22}; datadome=V~gI1MmkrURswVESZMni4zB71SQkT56_8ogiSSA9HqAkBGDMEfCT2OJaONu87_Cmjwxh_YBjyZ9Ix_sTLcDAltKvje~k74x0r4GdLDPm8sojdAg_RaEj3itxAnYWIP8o")                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Site", "same-origin")
                    .addHeader("Pragma", "no-cache")
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("TE", "trailers")
                    .build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> responseMap = jsonParser.parseMap(responseString);

            // As subcategorias que precisamos estão no 3o nível do json da resposta
            List<Map<String, Map<String, Object>>> categoriesLvl1 = (List<Map<String, Map<String, Object>>>) responseMap.get("categories");

            for (Map<String, Map<String, Object>> categoryLvl1 :
                    categoriesLvl1) {

                Object categoryLvl1NameObj = categoryLvl1.get("libelle");
                String categoryLvl1Name = (String) categoryLvl1NameObj;
                List<Map<String, Map<String, Object>>> categoriesLvl2 = (List<Map<String, Map<String, Object>>>) categoryLvl1.get("categories");

                for (Map<String, Map<String, Object>> categoryLvl2 :
                        categoriesLvl2) {

                    Object categoryLvl2NameObj = categoryLvl2.get("libelle");
                    String categoryLvl2Name = (String) categoryLvl2NameObj;
                    List<Map<String, Map<String, Object>>> categoriesLvl3 = (List<Map<String, Map<String, Object>>>) categoryLvl2.get("categories");


                    for (Map<String, Map<String, Object>> categoryLvl3 :
                            categoriesLvl3) {

                        Object categoryLvl3IdObj = categoryLvl3.get("id_categorie");
                        Integer categoryLvl3Id = (Integer) categoryLvl3IdObj;
                        Object categoryLvl3NbProductsObj = categoryLvl3.get("nb_produits");
                        Integer categoryLvl3NbProducts = (Integer) categoryLvl3NbProductsObj;

                        if (categoryLvl2Name.equals("Padaria e Pastelaria")) {
                            subcategoriesIntermarche.get("ids_padaria e pastelaria").add(categoryLvl3Id);
                            subcategoriesIntermarche.get("nbProducts_padaria e pastelaria").add(categoryLvl3NbProducts);
                        } else if (categoryLvl2Name.equals("Fruta") || categoryLvl2Name.equals("Legumes")) {
                            subcategoriesIntermarche.get("ids_frutas e legumes").add(categoryLvl3Id);
                            subcategoriesIntermarche.get("nbProducts_frutas e legumes").add(categoryLvl3NbProducts);
                        } else if (categoryLvl2Name.equals("Peixaria")) {
                            subcategoriesIntermarche.get("ids_peixaria").add(categoryLvl3Id);
                            subcategoriesIntermarche.get("nbProducts_peixaria").add(categoryLvl3NbProducts);
                        } else if (categoryLvl2Name.equals("Talho")) {
                            subcategoriesIntermarche.get("ids_talho").add(categoryLvl3Id);
                            subcategoriesIntermarche.get("nbProducts_talho").add(categoryLvl3NbProducts);
                        } else if (categoryLvl2Name.equals("Charcutaria")) {
                            subcategoriesIntermarche.get("ids_charcutaria").add(categoryLvl3Id);
                            subcategoriesIntermarche.get("nbProducts_charcutaria").add(categoryLvl3NbProducts);
                            // Lvl 1 aqui
                        } else if (categoryLvl1Name.equals("Mercearia")) {
                            subcategoriesIntermarche.get("ids_mercearia").add(categoryLvl3Id);
                            subcategoriesIntermarche.get("nbProducts_mercearia").add(categoryLvl3NbProducts);
                        } else if (categoryLvl1Name.equals("Congelados")) {
                            subcategoriesIntermarche.get("ids_congelados").add(categoryLvl3Id);
                            subcategoriesIntermarche.get("nbProducts_congelados").add(categoryLvl3NbProducts);
                        } else if (categoryLvl1Name.equals("Laticínios")) {
                            subcategoriesIntermarche.get("ids_laticínios e ovos").add(categoryLvl3Id);
                            subcategoriesIntermarche.get("nbProducts_laticínios e ovos").add(categoryLvl3NbProducts);
                        } else if (categoryLvl1Name.equals("Bebidas e Garrafeira")) {
                            subcategoriesIntermarche.get("ids_bebidas").add(categoryLvl3Id);
                            subcategoriesIntermarche.get("nbProducts_bebidas").add(categoryLvl3NbProducts);
                        } else if (categoryLvl1Name.equals("Alimentação Alternativa")) {
                            subcategoriesIntermarche.get("ids_alternativas alimentares, bio, saudável").add(categoryLvl3Id);
                            subcategoriesIntermarche.get("nbProducts_alternativas alimentares, bio, saudável").add(categoryLvl3NbProducts);
                        }
                    }
                }
            }

            // Obtidas as subcategorias e o nr de produtos de cada uma, podemos passar ao scraping dos produtos
            for (String category :
                    this.categories) {

                // Estas listas deverão ter o mesmo tamanho
                List<Integer> subcategoryIds = subcategoriesIntermarche.get("ids_" + category);
                List<Integer> subcategoryNbProducts = subcategoriesIntermarche.get("nbProducts_" + category);


                for (int i = 0; i < subcategoryIds.size(); i++) {

                    String requestBody = "{\"category\":\"" + subcategoryIds.get(i) + "\",\"page\":1,\"size\":" + subcategoryNbProducts.get(i) + ",\"filtres\":[],\"tri\":\"pertinence\",\"ordreTri\":null,\"catalog\":[\"PDV\"]}";

                    // Sleeps aleatórios entre pedidos para evitar bloqueios
                    Random randomNb = new Random();
                    int randomTimeout = randomNb.nextInt(4000) + 1000;
                    try {
                        Thread.sleep(randomTimeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    scrapeIntermarche("https://www.loja-online.intermarche.pt/api/service/produits/v2/pdvs/03622/products/byKeywordAndCategory", category, requestBody);

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void scrapeIntermarche(String url, String category, String requestBody) {

        try {

            Random random = new Random();
            int randomIndex = random.nextInt(this.userAgentList.size());

            // Gerado pelo Postman
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
            // Alterar o body para mudar as categorias, páginas, etc.
            RequestBody body = RequestBody.create(mediaType, requestBody);
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("User-Agent", this.userAgentList.get(randomIndex))
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Language", "pt-PT,pt;q=0.8,en;q=0.5,en-US;q=0.3")
                    .addHeader("Content-Type", "application/json;charset=utf-8")
                    .addHeader("x-red-device", "red_fo_desktop")
                    .addHeader("x-red-version", "3")
                    .addHeader("x-optional-oauth", "true")
                    .addHeader("x-service-name", "produits")
                    .addHeader("x-itm-device-fp", "c27ffc45-01f9-4b99-bab5-b9471fd451a9")
                    .addHeader("x-itm-session-id", "1484064c-612a-44ed-b018-a9c090267413")
                    .addHeader("x-pdv", "{\"ref\":\"03622\",\"isEcommerce\":true}")
                    .addHeader("Origin", "https://www.loja-online.intermarche.pt")
                    .addHeader("Alt-Used", "www.loja-online.intermarche.pt")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Referer", "https://www.loja-online.intermarche.pt/shelves/mercearia/arroz-massa-e-farinha/11715")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Site", "same-origin")
                    .addHeader("TE", "trailers")
//                    .addHeader("Cookie", "datadome=qxk8ELwDnjdUiX~02T6en3Kzic81WtXHrHNMDUz4PUtA4XSN7_JB9Xk1Cf~mN2ZaPszWL_Br6G6G7fmdcdTiJhBqgFYaWlTA5gof7942t83eIhI7C60r8MyIpBAUl2hR; itm_device_id=08658591-bbcd-4d03-9219-db4bf532a7bd; itm_usid=a02ca707-a700-4c4c-91e2-8864839266af; itm_pdv={%22ref%22:%2203622%22%2C%22isEcommerce%22:true}; novaParams={%22pdvRef%22:%2203622%22}; datadome=V~gI1MmkrURswVESZMni4zB71SQkT56_8ogiSSA9HqAkBGDMEfCT2OJaONu87_Cmjwxh_YBjyZ9Ix_sTLcDAltKvje~k74x0r4GdLDPm8sojdAg_RaEj3itxAnYWIP8o")
                    .build();


            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> responseMap = jsonParser.parseMap(responseString);

            List<Map<String, Object>> products = (List<Map<String, Object>>) responseMap.get("produits");
            if (products == null) return; // Quando não há produtos é porque enviam um captcha
            // Iterar produtos
            for (Map<String, Object> productData : products) {

                String imageUrl = ((List<String>) productData.get("images")).get(0);
                String name = (String) productData.get("libelle");
                String brand = (String) productData.get("marque");
                String quantity = (String) productData.get("conditionnement");

                double primaryValue = (double) Math.round((double) productData.get("prix") * 100) / 100;
                String primaryUnit = "";
                double secondaryValue = (double) Math.round((double) productData.get("prixKg") * 100) / 100;
                String secondaryUnit = (String) ((Map<String, Object>) productData.get("typeProduit")).get("uniteByCode");


                // No intermarché apenas indicam que está com desconto, mas não a percentagem nem o preço original (a não ser pelos folhetos)
                int discountPercentage = 0;
                String priceWithoutDiscount = "";

                // Atualizar produto
                Chain chain = chainRepository.findByName("intermarché");
                Product product = this.createOrGetProduct(name, quantity, brand, imageUrl, chain.getId());
                product.setChain(chain);
                product.getCategories().add(categoryRepository.findByName(category));
                product.setName(name);
                product.setBrand(brand);
                product.setQuantity(quantity);
                product.setImageUrl(imageUrl);

                // Instanciar preço
                Price price = new Price();
                price.setPrimaryValue(primaryValue);
                price.setPrimaryUnit(primaryUnit);
                price.setSecondaryValue(secondaryValue);
                price.setSecondaryUnit(secondaryUnit);
                price.setDiscountPercentage(discountPercentage);
                price.setPriceWoDiscount(priceWithoutDiscount);
                price.setCollectionDate(LocalDateTime.now());

                // Associar preço ao produto
                price.setProduct(product);

                // Adicionar o preço à lista do produto
                product.getPrices().add(price);

                // Guardar produto
                productRepository.save(product);

                //Atualizar GenericProducts
                genericProductsService.updateGenericProducts(product);

            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }


    private Product createOrGetProduct(String productName, String productQuantity, String productBrand, String imageUrl, Long chainId) {
        Product product = productRepository.findByAttributes(productName, productQuantity, productBrand, imageUrl, chainId);
        if (product == null) {
            product = new Product();
        }
        return product;
    }

}