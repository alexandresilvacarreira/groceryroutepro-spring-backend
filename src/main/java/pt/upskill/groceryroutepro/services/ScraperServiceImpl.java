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
            // Connect to the website and get the HTML document
            Document document = Jsoup.connect(url).get();

            // Select all product elements in the HTML
            Elements productElements = document.select(".product");

            // Iterate through each product element
            for (Element productElement : productElements) {
                // Extract product details
                String name = productElement.select(".ct-tile--description").text();
                String brand = productElement.select(".col-tile--brand").text();
                String quantity = productElement.select(".pwc-tile--quantity").text();
                String discountPercentage = productElement.select(".pwc-discount-amount").text();
                String imageUrl = productElement.select(".ct-tile-image").attr("src");

                // Create a new Product instance
                Product product = new Product();
                product.setName(name);
                product.setBrand(brand);
                product.setQuantity(quantity);
                product.setDiscountPercentage(discountPercentage);
                product.setImageUrl(imageUrl);

                // Set the chain and category separately (assuming you have methods for this)
                 product.setChain(chainRepository.findByName("continente"));
                 product.setCategory(categoryRepository.findByName(category));

                // Extract price details
                String primaryValueStr = productElement.select(".ct-price-formatted").text().replaceAll("[^0-9.]", "");
                double primaryValue = Double.parseDouble(primaryValueStr);
                String primaryUnit = productElement.select(".pwc-m-unit").text();

                // Create a new Price instance
                Price price = new Price();
                price.setPrimaryValue(primaryValue);
                price.setPrimaryUnit(primaryUnit);
                price.setSecondaryValue(0.0);  // Set secondary value as needed
                price.setSecondaryUnit("");    // Set secondary unit as needed
                price.setCollectionDate(LocalDate.now());  // Set the current date

                // Associate the Price with the Product
                price.setProduct(product);

                // Add the Price to the Product's prices list
                product.getPrices().add(price);

                // Save the Product to the database (assuming you have a service or repository)
                productRepository.save(product);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }


}