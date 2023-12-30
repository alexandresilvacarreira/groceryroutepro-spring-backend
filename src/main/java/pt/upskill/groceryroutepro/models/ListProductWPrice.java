package pt.upskill.groceryroutepro.models;

import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;

import java.util.List;

public class ListProductWPrice {

    private List<ProductWPriceProjection> products;
    private Pagination pagination;
    private String errorMessage;
    private boolean success;

    public ListProductWPrice() {
    }

    public List<ProductWPriceProjection> getProducts() {
        return products;
    }

    public void setProducts(List<ProductWPriceProjection> products) {
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

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
}
