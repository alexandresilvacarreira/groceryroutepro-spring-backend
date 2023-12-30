package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.name = :productName AND p.quantity = :productQuantity AND p.brand = :productBrand AND p.imageUrl = :productImageUrl AND p.chain.id = :chainId")
    Product findByAttributes(String productName, String productQuantity, String productBrand, String productImageUrl, Long chainId);


    @Query(nativeQuery = true, value =
            "SELECT " +
            "p.id AS productId, " +
            "p.brand, " +
            "p.image_url AS imageUrl, " +
            "p.name, " +
            "p.quantity, " +
            "p.chain_id AS chainId, " +
            "pr.id AS priceId, " +
            "pr.collection_date AS priceCollectionDate, " +
            "pr.discount_percentage AS priceDiscountPercentage, " +
            "pr.price_wo_discount AS priceWoDiscount, " +
            "pr.primary_unit AS pricePrimaryUnit, " +
            "pr.primary_value AS pricePrimaryValue, " +
            "pr.secondary_unit AS priceSecondaryUnit, " +
            "pr.secondary_value AS priceSecondaryValue " +
            "FROM " +
            "product p " +
            "INNER JOIN " +
            "price pr ON p.id = pr.product_id " +
            "INNER JOIN " +
            "product_categories pc ON p.id = pc.product_id " +
            "INNER JOIN " +
            "chain c ON p.chain_id = c.id " +
            "WHERE " +
            "pr.collection_date = (SELECT MAX(pr1.collection_date) FROM price pr1 WHERE pr1.product_id = p.id) " +
            "AND p.name LIKE %:search% " +
            "AND pc.category_id IN :categoryIds " +
            "AND c.id IN :chainIds " +
            "GROUP BY " +
            "p.id " +
            "ORDER BY " +
            "pr.primary_value ASC " +
            "LIMIT :nbResults OFFSET :offs")
    List<ProductWPriceProjection> findProductsByParams(
            @Param("search") String search,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("chainIds") List<Long> chainIds,
            @Param("nbResults") int nbResults,
            @Param("offs") int offs
    );

}
