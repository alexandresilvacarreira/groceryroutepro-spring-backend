package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.ProductQuantityFastest;
import pt.upskill.groceryroutepro.entities.ProductQuantityGeneric;

@Repository
public interface ProductQuantityGenericRepository extends JpaRepository<ProductQuantityGeneric,Long> {

}
