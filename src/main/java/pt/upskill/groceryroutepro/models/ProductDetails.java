package pt.upskill.groceryroutepro.models;

import pt.upskill.groceryroutepro.entities.Price;
import pt.upskill.groceryroutepro.entities.Product;

import java.util.List;

public class ProductDetails {

    private Product product;

    private List<Price> prices;
    private String errorMessage;
    private boolean success;

    public ProductDetails() {
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
