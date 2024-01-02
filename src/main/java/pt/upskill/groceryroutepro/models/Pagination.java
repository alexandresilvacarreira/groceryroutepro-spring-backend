package pt.upskill.groceryroutepro.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Sort;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;

import java.util.List;

public class Pagination {

    private int currentPage;
    private int size;
    private boolean hasNextPage;
    private boolean hasPreviousPage;

    public Pagination(int currentPage, int size, boolean hasNextPage, boolean hasPreviousPage) {
        this.currentPage = currentPage;
        this.size = size;
        this.hasNextPage = hasNextPage;
        this.hasPreviousPage = hasPreviousPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
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
