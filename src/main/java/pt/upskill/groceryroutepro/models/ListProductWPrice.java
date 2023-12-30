package pt.upskill.groceryroutepro.models;

import org.springframework.data.domain.Slice;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;

import java.util.List;

public class ListProductWPrice {

    private Slice<ProductWPriceProjection> products;
    private String errorMessage;
    private boolean success;
    private boolean hasNextPage;
    private boolean hasPreviousPage;

    public ListProductWPrice() {
    }

    public Slice<ProductWPriceProjection> getProducts() {
        return products;
    }

    public void setProducts(Slice<ProductWPriceProjection> products) {
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

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }
}
