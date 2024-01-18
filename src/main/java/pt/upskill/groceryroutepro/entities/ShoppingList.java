package pt.upskill.groceryroutepro.entities;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class ShoppingList {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String name;

    private double fastestListCost;

    private double cheapestListCost;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate;

    @ManyToOne
    private User user;

    @OneToOne
    private User currentShoppingListForUser;

    @OneToMany(mappedBy = "shoppingList")
    private List<ProductQuantityFastest> fastestProductQuantities;

    @OneToMany(mappedBy = "shoppingList")
    private List<ProductQuantityCheapest> cheapestProductQuantities;


    public ShoppingList() {
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
