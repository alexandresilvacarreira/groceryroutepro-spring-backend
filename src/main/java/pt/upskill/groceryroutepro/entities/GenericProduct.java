package pt.upskill.groceryroutepro.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class GenericProduct {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String brand;
    private String quantity;
    private String processedName;
    private String processedBrand;
    private String processedQuantity;
    private double currentLowestPricePrimaryValue;
    @OneToOne(mappedBy = "genericProduct", cascade = CascadeType.ALL)
    private Price currentLowestPrice;
    @OneToOne(mappedBy = "cheapestForGenericProduct", cascade = CascadeType.ALL)
    private Product currentCheapestProduct;
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "generic_product_categories",
            joinColumns = @JoinColumn(name = "generic_product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "generic_product_chains",
            joinColumns = @JoinColumn(name = "generic_product_id"),
            inverseJoinColumns = @JoinColumn(name = "chain_id")
    )
    private Set<Chain> chains = new HashSet<>();

    @OneToMany(mappedBy = "genericProduct", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "genericProduct")
    @JsonIgnore
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ProductQuantityGeneric> productQuantityGenericList;

    public GenericProduct() {
    }

    public List<ProductQuantityGeneric> getProductQuantityGenericList() {
        return productQuantityGenericList;
    }

    public void setProductQuantityGenericList(List<ProductQuantityGeneric> productQuantityGenericList) {
        this.productQuantityGenericList = productQuantityGenericList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessedName() {
        return processedName;
    }

    public void setProcessedName(String processedName) {
        this.processedName = processedName;
    }

    public String getProcessedBrand() {
        return processedBrand;
    }

    public void setProcessedBrand(String processedBrand) {
        this.processedBrand = processedBrand;
    }

    public String getProcessedQuantity() {
        return processedQuantity;
    }

    public void setProcessedQuantity(String processedQuantity) {
        this.processedQuantity = processedQuantity;
    }

    public Price getCurrentLowestPrice() {
        return currentLowestPrice;
    }

    public void setCurrentLowestPrice(Price currentLowestPrice) {
        this.currentLowestPrice = currentLowestPrice;
    }

    public Product getCurrentCheapestProduct() {
        return currentCheapestProduct;
    }

    public void setCurrentCheapestProduct(Product currentCheapestProduct) {
        this.currentCheapestProduct = currentCheapestProduct;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Set<Chain> getChains() {
        return chains;
    }

    public void setChains(Set<Chain> chains) {
        this.chains = chains;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public double getCurrentLowestPricePrimaryValue() {
        return currentLowestPricePrimaryValue;
    }

    public void setCurrentLowestPricePrimaryValue(double currentLowestPricePrimaryValue) {
        this.currentLowestPricePrimaryValue = currentLowestPricePrimaryValue;
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
