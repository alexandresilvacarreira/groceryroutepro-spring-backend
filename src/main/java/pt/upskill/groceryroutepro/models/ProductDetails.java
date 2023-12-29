package pt.upskill.groceryroutepro.models;

import pt.upskill.groceryroutepro.entities.Price;
import pt.upskill.groceryroutepro.entities.Product;

import java.util.List;

public class ProductDetails {

    private ProductWPrices productDetails;
    private String errorMessage;
    private boolean success;

    public ProductDetails() {
    }

    public ProductWPrices getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(ProductWPrices productDetails) {
        this.productDetails = productDetails;
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
