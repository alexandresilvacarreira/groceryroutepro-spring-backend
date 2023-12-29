package pt.upskill.groceryroutepro.models;

import pt.upskill.groceryroutepro.entities.Price;
import pt.upskill.groceryroutepro.entities.Product;

import java.util.List;

public class ProductWPrices {

    private Product product;
    private List<Price> prices;

    public ProductWPrices(Product product, List<Price> prices) {
        this.product = product;
        this.prices = prices;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<Price> getPrices() {
        return prices;
    }

    public void setPrices(List<Price> prices) {
        this.prices = prices;
    }
}
