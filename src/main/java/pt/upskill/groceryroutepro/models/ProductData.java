package pt.upskill.groceryroutepro.models;

import pt.upskill.groceryroutepro.entities.Category;
import pt.upskill.groceryroutepro.entities.Price;
import pt.upskill.groceryroutepro.entities.Product;

import java.util.HashSet;
import java.util.Set;

public class ProductData {

    private Product product;
    private Price price;

    public ProductData() {
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public Price getPrice() {
        return price;
    }

}