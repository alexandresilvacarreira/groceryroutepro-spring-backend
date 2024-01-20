package pt.upskill.groceryroutepro.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class ShoppingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double fastestListCost;

    private double cheapestListCost;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate;

    @ManyToOne
    @JsonIgnore
    private User user;

    @OneToOne(mappedBy = "currentShoppingList")
    @JoinColumn(name = "current_shopping_list_for_user_id")
    @JsonIgnore
    private User currentShoppingListForUser;

    @OneToMany(mappedBy = "shoppingList", cascade = {CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ProductQuantityFastest> fastestProductQuantities;

    @OneToMany(mappedBy = "shoppingList", cascade = {CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ProductQuantityCheapest> cheapestProductQuantities;

    @OneToMany(mappedBy = "shoppingList", cascade = {CascadeType.ALL})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ProductQuantityGeneric> genericProductQuantities;

    public ShoppingList() {
    }

    public List<ProductQuantityGeneric> getGenericProductQuantities() {
        return genericProductQuantities;
    }

    public void setGenericProductQuantities(List<ProductQuantityGeneric> genericProductQuantities) {
        this.genericProductQuantities = genericProductQuantities;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getFastestListCost() {
        return fastestListCost;
    }

    public void setFastestListCost(double fastestListCost) {
        this.fastestListCost = fastestListCost;
    }

    public double getCheapestListCost() {
        return cheapestListCost;
    }

    public void setCheapestListCost(double cheapestListCost) {
        this.cheapestListCost = cheapestListCost;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getCurrentShoppingListForUser() {
        return currentShoppingListForUser;
    }

    public void setCurrentShoppingListForUser(User currentShoppingListForUser) {
        this.currentShoppingListForUser = currentShoppingListForUser;
    }

    public List<ProductQuantityFastest> getFastestProductQuantities() {
        return fastestProductQuantities;
    }

    public void setFastestProductQuantities(List<ProductQuantityFastest> fastestProductQuantities) {
        this.fastestProductQuantities = fastestProductQuantities;
    }

    public List<ProductQuantityCheapest> getCheapestProductQuantities() {
        return cheapestProductQuantities;
    }

    public void setCheapestProductQuantities(List<ProductQuantityCheapest> cheapestProductQuantities) {
        this.cheapestProductQuantities = cheapestProductQuantities;
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
