package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.Category;
import pt.upskill.groceryroutepro.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

}
