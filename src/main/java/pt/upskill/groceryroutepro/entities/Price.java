package pt.upskill.groceryroutepro.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Price {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private double primaryValue;
    private String primaryUnit;
    private double secondaryValue;
    private String secondaryUnit;
    private int discountPercentage;
    private String priceWoDiscount;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime collectionDate;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne
    @JoinColumn(name = "generic_product_id", unique = true)
    @JsonIgnore
    private GenericProduct genericProduct;

    public Price() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getPrimaryValue() {
        return primaryValue;
    }

    public void setPrimaryValue(double primaryValue) {
        this.primaryValue = primaryValue;
    }

    public String getPrimaryUnit() {
        return primaryUnit;
    }

    public void setPrimaryUnit(String primaryUnit) {
        this.primaryUnit = primaryUnit;
    }

    public double getSecondaryValue() {
        return secondaryValue;
    }

    public void setSecondaryValue(double secondaryValue) {
        this.secondaryValue = secondaryValue;
    }

    public String getSecondaryUnit() {
        return secondaryUnit;
    }

    public void setSecondaryUnit(String secondaryUnit) {
        this.secondaryUnit = secondaryUnit;
    }

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getPriceWoDiscount() {
        return priceWoDiscount;
    }

    public void setPriceWoDiscount(String priceWoDiscount) {
        this.priceWoDiscount = priceWoDiscount;
    }

    public LocalDateTime getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDateTime collectionDate) {
        this.collectionDate = collectionDate;
    }

    // TODO omitir o getter para o produto, para não devolver informação repetida nos pedidos
//    public Product getProduct() {
//        return product;
//    }

    public void setProduct(Product product) {
        this.product = product;
    }

    // TODO omitir o getter para o produto, para não devolver informação repetida nos pedidos
//    public GenericProduct getGenericProduct() {
//        return genericProduct;
//    }

    public void setGenericProduct(GenericProduct genericProduct) {
        this.genericProduct = genericProduct;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } catch (Exception e) {
            return super.toString();
        }
    }

}
