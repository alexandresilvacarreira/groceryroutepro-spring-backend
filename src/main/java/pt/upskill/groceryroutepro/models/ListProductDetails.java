package pt.upskill.groceryroutepro.models;

import pt.upskill.groceryroutepro.entities.Price;
import pt.upskill.groceryroutepro.entities.Product;

import java.util.List;

public class ListProductDetails {

    private List<ProductWPrices> products;
    private String errorMessage;
    private boolean success;

    public ListProductDetails() {
    }

    public List<ProductWPrices> getProducts() {
        return products;
    }

    public void setProducts(List<ProductWPrices> products) {
        this.products = products;
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
