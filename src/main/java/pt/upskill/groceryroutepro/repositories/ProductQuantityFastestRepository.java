package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.ProductQuantityFastest;
import pt.upskill.groceryroutepro.entities.ShoppingList;

@Repository
public interface ProductQuantityFastestRepository extends JpaRepository<ProductQuantityFastest,Long> {

}
