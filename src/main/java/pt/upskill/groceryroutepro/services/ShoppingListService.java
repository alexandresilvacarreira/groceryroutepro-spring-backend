package pt.upskill.groceryroutepro.services;

import pt.upskill.groceryroutepro.entities.ShoppingList;

public interface ShoppingListService {

    ShoppingList addProduct(Long genericProductId);

    ShoppingList getCurrentShoppingList();

    ShoppingList removeProduct(Long genericProductId);

    ShoppingList removeAll(Long genericProductId);
}
