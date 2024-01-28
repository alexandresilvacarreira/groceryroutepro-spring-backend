package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.ShoppingList;
import pt.upskill.groceryroutepro.entities.User;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList,Long> {

    ShoppingList findByCurrentShoppingListForUser(User user);

}
