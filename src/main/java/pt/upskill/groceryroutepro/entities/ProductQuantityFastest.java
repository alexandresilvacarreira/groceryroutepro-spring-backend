package pt.upskill.groceryroutepro.entities;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;

@Entity
public class ProductQuantityFastest {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private int quantity;
    @ManyToOne
    private ShoppingList shoppingList;
    @OneToOne(mappedBy = "productQuantityFastest")
    private Product product;

    public ProductQuantityFastest() {
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
