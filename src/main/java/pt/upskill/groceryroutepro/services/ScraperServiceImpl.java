package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.Chain;
import pt.upskill.groceryroutepro.entities.Price;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.entities.Store;
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

    @Override
    public void scrapeContinente(String url, String category) {

        try {
            // Pedido à API da loja
            Document document = Jsoup.connect(url).get();

            // Produtos desta loja estão na classe .product
            Elements productElements = document.select(".product");

            // Iterar produtos
            for (Element productElement : productElements) {

                // Inicializar produto
                String name = productElement.select(".pwc-tile--description").text();
                String quantity = productElement.select(".pwc-tile--quantity").text();
                Chain chain = chainRepository.findByName("continente");
                Product product = this.createOrGetProduct(name, quantity, chain.getId());
                product.setChain(chain);
                product.setCategory(categoryRepository.findByName(category));

                String brand = productElement.select(".col-tile--brand").text();
                String imageUrl = productElement.select(".ct-tile-image").attr("data-src");

                // Info relativa ao desconto
                Element discountPercentageElement = productElement.select("p.pwc-discount-amount.col-discount-amount:not(.pwc-info-amount-iva-zero)").first();
                int discountPercentage = 0;
                if (discountPercentageElement != null) {
                    discountPercentage = Integer.parseInt(discountPercentageElement.text().replaceAll("[^0-9]", ""));
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
                product.setDiscountPercentage(discountPercentage);
                product.setPriceWoDiscount(priceWithoutDiscount);
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
                if (quantityElement != null){
                    quantity = quantityElement.text();
                } else {
                    String[] nameWords = name.split(" ");
                    quantity = nameWords[nameWords.length-1];
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
                    priceWithoutDiscount = priceWithoutDiscountElement.select("span.strike-through, span.value").attr("content").replace(".",",") + " €";
                    // Nem todos têm unidade, adicionar só nos que têm
                    Element discountUnit = priceWithoutDiscountElement.select(".auc-avgWeight").first();
                    if (discountUnit != null){
                        priceWithoutDiscount += discountUnit.text();
                    }
                }

                // Atualizar produto
                Chain chain = chainRepository.findByName("auchan");
                Product product = this.createOrGetProduct(name, quantity, chain.getId());
                product.setChain(chain);
                product.setCategory(categoryRepository.findByName(category));
                product.setName(name);
                product.setBrand(brand);
                product.setQuantity(quantity);
                product.setDiscountPercentage(discountPercentage);
                product.setPriceWoDiscount(priceWithoutDiscount);
                product.setImageUrl(imageUrl);

                // Obter preços

                // Primário
                Element primaryPriceElement = productElement.select(".auc-product-tile__prices .sales").first();
                String primaryValueStr = primaryPriceElement.select(".value").attr("content");
                double primaryValue = Double.parseDouble(primaryValueStr);
                Element primaryUnitElement = primaryPriceElement.selectFirst(".auc-avgWeight");
                String primaryUnit = "";
                if (primaryUnitElement !=null){
                    primaryUnit = primaryUnitElement.text().replace("/","");
                }

                // Secundário (normalmente é o preço por kg)
                Element secondaryPriceElement = productElement.select(".auc-measures--price-per-unit").first();
                String secondaryValueStr = secondaryPriceElement.text().replaceAll("[^0-9.]", "");
                double secondaryValue = Double.parseDouble(secondaryValueStr);
                String secondaryUnit = secondaryPriceElement.text().substring(secondaryPriceElement.text().lastIndexOf('/') + 1).trim();;

                // Instanciar preço
                Price price = new Price();
                price.setPrimaryValue(primaryValue);
                price.setPrimaryUnit(primaryUnit);
                price.setSecondaryValue(secondaryValue);
                price.setSecondaryUnit(secondaryUnit);
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

    public Product createOrGetProduct(String productName, String productQuantity, Long chainId){
        Product product = productRepository.findByNameQuantityAndChain(productName, productQuantity, chainId);
        if (product == null) {
            product = new Product();
        }
        return product;
    }


}