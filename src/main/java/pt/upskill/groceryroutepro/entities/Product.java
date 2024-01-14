package pt.upskill.groceryroutepro.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String brand;
    private String quantity;
    private String imageUrl;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Price> prices = new ArrayList<>();
    @ManyToOne
    private Chain chain;
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "generic_product_id")
    @JsonIgnore
    private GenericProduct genericProduct;

    @OneToOne
    @JoinColumn(name = "cheapest_for_generic_product_id")
    @JsonIgnore
    private GenericProduct cheapestForGenericProduct;


    public Product() {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Price> getPrices() {
        return prices;
    }

    public void setPrices(List<Price> prices) {
        this.prices = prices;
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    // TODO omitir o getter para não repetir info na resposta
//    public GenericProduct getGenericProduct() {
//        return genericProduct;
//    }

    public void setGenericProduct(GenericProduct genericProduct) {
        this.genericProduct = genericProduct;
    }

    // TODO omitir o getter para não repetir info na resposta
//    public GenericProduct getCheapestForGenericProduct() {
//        return cheapestForGenericProduct;
//    }

    public void setCheapestForGenericProduct(GenericProduct cheapestForGenericProduct) {
        this.cheapestForGenericProduct = cheapestForGenericProduct;
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
