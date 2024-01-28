package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.ProductQuantityCheapest;

@Repository
public interface ProductQuantityCheapestRepository extends JpaRepository<ProductQuantityCheapest,Long> {

}
