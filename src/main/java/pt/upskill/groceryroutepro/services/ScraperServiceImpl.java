package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.Price;
import pt.upskill.groceryroutepro.entities.Product;
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
                // Obter informação dos produtos
                String name = productElement.select(".pwc-tile--description").text();
                String brand = productElement.select(".col-tile--brand").text();
                String quantity = productElement.select(".pwc-tile--quantity").text();
                String imageUrl = productElement.select(".ct-tile-image").attr("data-src");

                // Info relativa ao desconto
                Element discountPercentageElement = productElement.select("p.pwc-discount-amount.col-discount-amount:not(.pwc-info-amount-iva-zero)").first();
                String discountPercentage = "";
                if (discountPercentageElement != null) {
                    discountPercentage = discountPercentageElement.text().replaceAll("[^0-9%]", "");
                }
                Element priceWithoutDiscountElement = productElement.select(".pwc-tile--price-dashed").first();
                String priceWithoutDiscount = "";
                if (priceWithoutDiscountElement != null) {
                    priceWithoutDiscount = priceWithoutDiscountElement.select(".pwc-tile--price-value").text().replaceAll("[^0-9,]", "") +" €"+
                            productElement.select(".pwc-m-unit").first().text();
                }

                // Instanciar produto
                Product product = new Product();
                product.setName(name);
                product.setBrand(brand);
                product.setQuantity(quantity);
                product.setDiscountPercentage(discountPercentage);
                product.setPriceWoDiscount(priceWithoutDiscount);
                product.setImageUrl(imageUrl);

                // Cadeia e categoria inseridas manualmente
                product.setChain(chainRepository.findByName("continente"));
                product.setCategory(categoryRepository.findByName(category));

                // Obter preços

                // Primário
                Element primaryPriceElement = productElement.select(".pwc-tile--price-primary").first();
                String primaryValueStr = primaryPriceElement.select(".ct-price-formatted").text().replaceAll("[^0-9,]", "");
                double primaryValue = Double.parseDouble(primaryValueStr.replace(",", "."));
                String primaryUnit = primaryPriceElement.select(".pwc-m-unit").text().replace("/","");;

                // Secundário (normalmente é o preço por kg)
                Element secondaryPriceElement = productElement.select(".pwc-tile--price-secondary").first();
                String secondaryValueStr = secondaryPriceElement.select(".ct-price-value").text().replaceAll("[^0-9,]", "");
                double secondaryValue = Double.parseDouble(secondaryValueStr.replace(",", "."));
                String secondaryUnit = secondaryPriceElement.select(".pwc-m-unit").text().replace("/","");

                // Instanciar preço
                Price price = new Price();
                price.setPrimaryValue(primaryValue);
                price.setPrimaryUnit(primaryUnit);
                price.setSecondaryValue(secondaryValue);  // Set secondary value as needed
                price.setSecondaryUnit(secondaryUnit);    // Set secondary unit as needed
                price.setCollectionDate(LocalDate.now());  // Set the current date

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
        // TODO quantidade: ir pela quantidade mínima, se não houver, usar a última palavra do nome
    }


}