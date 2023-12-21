package pt.upskill.groceryroutepro.services;

import okhttp3.*;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.Chain;
import pt.upskill.groceryroutepro.entities.Price;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.entities.Store;
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
import java.time.LocalDate;
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

    private List<String> userAgentList = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    );

    @Override
    public void scrapeContinenteAll() {

        int start = 0;
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

            int maxSize = 1000;

            switch (scraperParams.getCategory()) {
                case "mercearia":
                    maxSize = 5394;
                    break;
                case "frutas e legumes":
                    maxSize = 713;
                    break;
                case "congelados":
                    maxSize = 936;
                    break;
                case "laticínios e ovos":
                    maxSize = 1025;
                    break;
                case "peixaria":
                    maxSize = 453;
                    break;
                case "talho":
                    maxSize = 284;
                    break;
                case "charcutaria":
                    maxSize = 859;
                    break;
                case "alternativas alimentares, bio, saudável":
                    maxSize = 2295;
                    break;
                case "bebidas":
                    maxSize = 5062;
                    break;
                case "padaria e pastelaria":
                    maxSize = 602;
                    break;
            }

            while (start <= maxSize) {

                // Sleeps aleatórios entre pedidos para evitar bloqueios
                Random random = new Random();
                int randomTimeout = random.nextInt(4000) + 1000;
                try {
                    Thread.sleep(randomTimeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String url = scraperParams.getUrl() + "&start=" + start + "&sz=" + size;
                scrapeContinente(url, scraperParams.getCategory());
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
            Connection connection = Jsoup.connect(url);
            connection.userAgent(this.userAgentList.get(randomIndex));
            connection.header("Connection", "keep-alive");
            Document document = Jsoup.connect(url).get();

            // Produtos desta loja estão na classe .product
            Elements productElements = document.select(".product");

            // Iterar produtos
            for (Element productElement : productElements) {

                // Inicializar produto
                Element nameElement = productElement.select(".pwc-tile--description").first();
                if (nameElement == null) {
                    continue;
                }
                String name = nameElement.text();
                String quantity = productElement.select(".pwc-tile--quantity").text();
                Chain chain = chainRepository.findByName("continente");
                Product product = this.createOrGetProduct(name, quantity, chain.getId());
                product.setChain(chain);
                product.getCategories().add(categoryRepository.findByName(category));
                ;

                String brand = productElement.select(".col-tile--brand").text();
                String imageUrl = productElement.select(".ct-tile-image").attr("data-src");

                // Info relativa ao desconto
                Element discountPercentageElement = productElement.select("p.pwc-discount-amount.col-discount-amount:not(.pwc-info-amount-iva-zero .pwc-discount-amount-pvpr)").first();
                int discountPercentage = 0;
                if (discountPercentageElement != null) {
                    String discountPercentageStr = discountPercentageElement.text();
                    if (discountPercentageStr.contains("Desconto Imediato: ")) {
                        discountPercentage = Integer.parseInt(discountPercentageElement.text().replaceAll("[^0-9]", ""));
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
                String primaryValueStr = primaryPriceElement.select(".ct-price-formatted").text().replaceAll("[^0-9,]", "");
                double primaryValue = Double.parseDouble(primaryValueStr.replace(",", "."));
                String primaryUnit = primaryPriceElement.select(".pwc-m-unit").text().replace("/", "");

                // Secundário (normalmente é o preço por kg)
                Element secondaryPriceElement = productElement.select(".pwc-tile--price-secondary").first();
                String secondaryValueStr = secondaryPriceElement.select(".ct-price-value").text().replaceAll("[^0-9,]", "");
                double secondaryValue = Double.parseDouble(secondaryValueStr.replace(",", "."));
                String secondaryUnit = secondaryPriceElement.select(".pwc-m-unit").text().replace("/", "");

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
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }


    @Override
    public void scrapeAuchan(String url, String category) {

        try {

            // Pedido à API da loja
            Random random = new Random();
            int randomIndex = random.nextInt(this.userAgentList.size());
            Connection connection = Jsoup.connect(url);
            connection.userAgent(this.userAgentList.get(randomIndex));
            connection.header("Connection", "keep-alive");
            Document document = Jsoup.connect(url).get();

            // Produtos desta loja estão na classe .product
            Elements productElements = document.select(".product");

            // Iterar produtos
            for (Element productElement : productElements) {


                String name = productElement.select(".auc-product-tile__name a").text();
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
                    discountPercentage = Integer.parseInt(discountPercentageElement.text().replaceAll("[^0-9]", ""));
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
                Product product = this.createOrGetProduct(name, quantity, chain.getId());
                product.setChain(chain);
                product.getCategories().add(categoryRepository.findByName(category));
                product.setName(name);
                product.setBrand(brand);
                product.setQuantity(quantity);
                product.setImageUrl(imageUrl);

                // Obter preços

                // Primário
                Element primaryPriceElement = productElement.select(".auc-product-tile__prices .sales").first();
                String primaryValueStr = primaryPriceElement.select(".value").attr("content");
                double primaryValue = Double.parseDouble(primaryValueStr);
                Element primaryUnitElement = primaryPriceElement.selectFirst(".auc-avgWeight");
                String primaryUnit = "";
                if (primaryUnitElement != null) {
                    primaryUnit = primaryUnitElement.text().replace("/", "");
                }

                // Secundário (normalmente é o preço por kg)
                Element secondaryPriceElement = productElement.select(".auc-measures--price-per-unit").first();
                String secondaryValueStr = secondaryPriceElement.text().replaceAll("[^0-9.]", "");
                double secondaryValue = Double.parseDouble(secondaryValueStr);
                String secondaryUnit = secondaryPriceElement.text().substring(secondaryPriceElement.text().lastIndexOf('/') + 1).trim();
                ;

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
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void scrapeMinipreco(String url, String category) {

        try {

            // Pedido à API da loja
            Document document = Jsoup.connect(url).get();

            Elements productElements = document.select(".product-list__item");

            // Iterar produtos
            for (Element productElement : productElements) {

                String name = productElement.select(".details").text();

                // Minipreco normalmente inclui a marca em maiúsculas no início do nome dos produtos, por defeito usamos Minipreco
                String brand = "Minipreço";
                Pattern pattern = Pattern.compile("^([A-Z]+\\s?)+");
                Matcher matcher = pattern.matcher(name);

                if (matcher.find()) {
                    brand = matcher.group().trim();
                    // Temos de truncar a string, senão vem também a primeira letra do resto do nome
                    if (!brand.isEmpty()) {
                        brand = brand.substring(0, brand.length() - 1);
                    }
                }

                // Quantidade - ora são as duas últimas palavras, ora o que está em parênteses no fim do nome
                String quantity = "";
                if (name.lastIndexOf(")") == name.length() - 1) {
                    quantity = name.substring(name.lastIndexOf("(") + 1, name.lastIndexOf(")"));
                } else {
                    String[] nameWords = name.split(" ");
                    quantity = nameWords[nameWords.length - 2] + nameWords[nameWords.length - 1];
                }

                String imageUrl = productElement.select(".thumb img").first().attr("data-original");

                // Info relativa ao desconto
                Element discountPercentageElement = productElement.select(".promotion_text").first();
                int discountPercentage = 0;
                if (discountPercentageElement != null) {
                    discountPercentage = Integer.parseInt(discountPercentageElement.text().replaceAll("[^0-9]", ""));
                }

                Element priceWithoutDiscountElement = productElement.select("p.price s").first();
                String priceWithoutDiscount = "";
                if (priceWithoutDiscountElement != null) {
                    priceWithoutDiscount = priceWithoutDiscountElement.text();
                }

                // Atualizar produto
                Chain chain = chainRepository.findByName("minipreço");
                Product product = this.createOrGetProduct(name, quantity, chain.getId());
                product.setChain(chain);
                product.getCategories().add(categoryRepository.findByName(category));
                product.setName(name);
                product.setBrand(brand);
                product.setQuantity(quantity);
                product.setImageUrl(imageUrl);

                // Obter preços

                // Primário
                Element primaryPriceElement = productElement.select("p.price").first();
                String primaryValueStr = primaryPriceElement.ownText().replaceAll("[^0-9,]", "").replace(",", ".");
                double primaryValue = Double.parseDouble(primaryValueStr);

                String primaryUnit = "";

                // Secundário (normalmente é o preço por kg)
                String secondaryPriceString = productElement.select(".pricePerKilogram").first().ownText();
                String secondaryValueStr = secondaryPriceString.replaceAll("[^0-9,]", "").replace(",", ".");
                double secondaryValue = Double.parseDouble(secondaryValueStr);
                String secondaryUnit = secondaryPriceString.substring(secondaryPriceString.lastIndexOf("/") + 1, secondaryPriceString.length() - 2);

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
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void scrapePingoDoce(String url, String category) {

        try {

            // Gerado pelo Postman
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, "");
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0")
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Language", "pt-PT,pt;q=0.8,en;q=0.5,en-US;q=0.3")
//                    .addHeader("Accept-Encoding", "gzip, deflate, br")
//                    .addHeader("Referer", "https://mercadao.pt/store/pingo-doce/category/frutas-e-legumes-12")
                    .addHeader("X-Version", "3.16.0")
                    .addHeader("X-Name", "webapp")
                    .addHeader("ngsw-bypass", "true")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Cookie", "OptanonConsent=isGpcEnabled=0&datestamp=Wed+Dec+20+2023+20%3A57%3A22+GMT%2B0000+(Hora+padr%C3%A3o+da+Europa+Ocidental)&version=202301.2.0&isIABGlobal=false&hosts=&landingPath=NotLandingPage&groups=C0002%3A1%2CC0001%3A1%2CC0005%3A1%2CC0004%3A1&geolocation=PT%3B11&AwaitingReconsent=false; OptanonAlertBoxClosed=2023-12-05T18:33:49.947Z")
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

            // Iterar produtos
            for (Map<String, Object> productMap : products) {

                // Info do produto vem no _source
                Map<String, Object> productData = (Map<String, Object>) productMap.get("_source");

                String imageUrl = "https://res.cloudinary.com/fonte-online/image/upload/c_fill,h_300,q_auto,w_300/v1/PDO_PROD/" + productData.get("sku") + "_1";
                String name = (String) productData.get("firstName");
                System.out.println(name);
                String brand = (String) ((Map<String, Object>) productData.get("brand")).get("name");
                String quantity = "";
                double primaryValue = (double) Math.round((double) productData.get("buyingPrice") * 100) / 100;
                System.out.println("primaryValue: " + primaryValue);
                String primaryUnit = "";
                double secondaryValue;
                String secondaryUnit = ((String) productData.get("netContentUnit")).toLowerCase();
                if ((productData.get("capacity")).equals("0")) {
                    System.out.println("capacity 0");
                    quantity = (productData.get("averageWeight") + " " + productData.get("netContentUnit")).toLowerCase();
                    primaryUnit = ((String) productData.get("netContentUnit")).toLowerCase();
                    secondaryValue = primaryValue;
                    System.out.println("secondaryValue:" + secondaryValue);
                } else {
                    System.out.println("capacity with stuff");
                    quantity = ((String) productData.get("capacity")).toLowerCase();
                    secondaryValue = (double) Math.round((primaryValue / ((Number) productData.get("netContent")).doubleValue()) * 100) / 100;
                    System.out.println("secondaryValue: " + secondaryValue);
                }


                int discountPercentage = 0;
                String priceWithoutDiscount = "";
                Map<String, Object> promotion = (Map<String, Object>) productData.get("promotion");
                if (promotion.get("amount") != null && promotion.get("type").equals("PERCENTAGE")) {
                    discountPercentage = (int) Math.round((double) promotion.get("amount"));
                    priceWithoutDiscount = ((double) Math.round((Double) productData.get("regularPrice") * 100) / 100) + " €";
                }

                // Atualizar produto
                Chain chain = chainRepository.findByName("pingo doce");
                Product product = this.createOrGetProduct(name, quantity, chain.getId());
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

            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void scrapeIntermarche(String url, String category, String requestBody) {

        try {

            // Gerado pelo Postman
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json;charset=utf-8");
            // Alterar o body para mudar as categorias, páginas, etc.
            RequestBody body = RequestBody.create(mediaType, requestBody);
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:120.0) Gecko/20100101 Firefox/120.0")
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Language", "pt-PT,pt;q=0.8,en;q=0.5,en-US;q=0.3")
                    .addHeader("Content-Type", "application/json;charset=utf-8")
                    .addHeader("x-red-device", "red_fo_desktop")
                    .addHeader("x-red-version", "3")
                    .addHeader("x-optional-oauth", "true")
                    .addHeader("x-service-name", "produits")
                    .addHeader("x-itm-device-fp", "ead3ce12-5cec-43d6-abf0-62d10b4e0bcd")
                    .addHeader("x-itm-session-id", "7ea3cd74-a97c-4ca5-87ca-d634800964fa")
                    .addHeader("x-pdv", "{\"ref\":\"03622\",\"isEcommerce\":true}")
                    .addHeader("Origin", "https://www.loja-online.intermarche.pt")
                    .addHeader("Alt-Used", "www.loja-online.intermarche.pt")
                    .addHeader("Connection", "keep-alive")
//                    .addHeader("Referer", "https://www.loja-online.intermarche.pt/shelves/frescos/padaria-e-pastelaria/pao-e-broa/10036?ordre=decroissant&page=1&trier=prix")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Site", "same-origin")
                    .addHeader("TE", "trailers")
                    .addHeader("Cookie", "datadome=YVhULd5eY1eUn9FiSaVAJpKd9fkv610A~4lh9nESWCXhI_WFFrpvSru6uOLDc4SFBP8wFuhWPr~sXKH7pGFjJluaufUz7f8t42x4xD_41ik~pauVS6fM1MJDOak7XAT2; itm_device_id=ead3ce12-5cec-43d6-abf0-62d10b4e0bcd; itm_usid=7ea3cd74-a97c-4ca5-87ca-d634800964fa; didomi_token=eyJ1c2VyX2lkIjoiMThjM2I1ZWMtMjQ1Zi02YjRiLWI1MmYtNzhhZjBjNjVjZGU5IiwiY3JlYXRlZCI6IjIwMjMtMTItMDVUMTk6MDU6MTUuNzcwWiIsInVwZGF0ZWQiOiIyMDIzLTEyLTA1VDE5OjA1OjE3LjEyOVoiLCJ2ZW5kb3JzIjp7ImVuYWJsZWQiOlsiZ29vZ2xlIiwiYzpuZXN0bGUtUUxyVEx5OXQiLCJjOnNhbGVjeWNsZSIsImM6bHVja3ljYXJ0LUxKYlBGclNqIiwiYzpiaW5nLWFkcyIsImM6bWVkaWFub2UtOEtzcFQ1UVoiLCJjOnBpbnRlcmVzdCIsImM6YWItdGFzdHkiLCJjOnF1YW50dW0tYWR2ZXJ0aXNpbmciLCJjOmNvbnRlbnRzcXVhcmUiLCJjOnVzYWJpbGxhIiwiYzpwcm9jdGVyYW4tUTNWRUpOaVkiLCJjOmdvb2dsZWFuYS1ySnh6Y2M2MyIsImM6c25hcGNoYXQtZnpOVUVpemoiLCJjOmRhdGFkb21lLWU2RGpnbXI3IiwiYzpkaWRvbWktVGZ4enRBejkiLCJjOmR5bmF0cmFjZS1RWUZtaVRNQyIsImM6cXVldWVpdC1XWVpmTFJ4TCIsImM6YWRvdG1vYiIsImM6bWF0Y2hhLWF5ejNCTEw5Il19LCJwdXJwb3NlcyI6eyJlbmFibGVkIjpbImdlb2xvY2F0aW9uX2RhdGEiLCJkZXZpY2VfY2hhcmFjdGVyaXN0aWNzIl19LCJ2ZXJzaW9uIjoyLCJhYyI6IkM4R0FHQUZrQW93THdRQUEuQUFBQSJ9; euconsent-v2=CP2UBAAP2UBAAAHABBENDgCsAP_AAAAAAB6YF5wDAAKgAZAA3AB8AIAAeACEAFIAMYAcQBEwCOALzAAAAOKgAwABEGopABgACINRKADAAEQah0AGAAIg1EIAMAARBqCQAYAAiDUMgAwABEGo.f_gAAAAAAAAA; itm_pdv={%22ref%22:%2203622%22%2C%22isEcommerce%22:true}; novaParams={%22pdvRef%22:%2203622%22}")
                    .build();


            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            JsonParser jsonParser = JsonParserFactory.getJsonParser();
            Map<String, Object> responseMap = jsonParser.parseMap(responseString);
            List<Map<String, Object>> products = (List<Map<String, Object>>) responseMap.get("produits");


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
                Chain chain = chainRepository.findByName("pingo doce");
                Product product = this.createOrGetProduct(name, quantity, chain.getId());
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

            }


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }


    public Product createOrGetProduct(String productName, String productQuantity, Long chainId) {
        Product product = productRepository.findByNameQuantityAndChain(productName, productQuantity, chainId);
        if (product == null) {
            product = new Product();
        }
        return product;
    }

}