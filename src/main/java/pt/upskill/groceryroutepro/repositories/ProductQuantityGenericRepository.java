package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.ProductQuantityGeneric;
import pt.upskill.groceryroutepro.entities.ShoppingList;

import java.util.List;

@Repository
public interface ProductQuantityGenericRepository extends JpaRepository<ProductQuantityGeneric,Long> {

    List<ProductQuantityGeneric> findAllByShoppingList(ShoppingList shoppingList);

}
