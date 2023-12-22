package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.Category;
import pt.upskill.groceryroutepro.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    @Query("SELECT p FROM Product p WHERE p.name = :productName AND p.quantity = :productQuantity AND p.brand = :productBrand AND p.imageUrl = :productImageUrl AND p.chain.id = :chainId")
    Product findByAttributes(String productName, String productQuantity, String productBrand, String productImageUrl, Long chainId);

}
