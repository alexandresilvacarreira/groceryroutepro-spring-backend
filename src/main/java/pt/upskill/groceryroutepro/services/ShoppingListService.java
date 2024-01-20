package pt.upskill.groceryroutepro.services;

import pt.upskill.groceryroutepro.entities.ShoppingList;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.EmailVerificationToken;
import pt.upskill.groceryroutepro.models.SignUp;

public interface ShoppingListService {

    void addProduct(Long genericProductId);

    ShoppingList getCurrentShoppingList();

    void removeProduct(Long genericProductId);
}
