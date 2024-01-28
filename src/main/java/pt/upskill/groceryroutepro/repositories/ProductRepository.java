package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.Chain;
import pt.upskill.groceryroutepro.entities.GenericProduct;
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
                    "p.generic_product_id AS genericProductId, " +
                    "c.name AS chain, " +
                    "c.id AS chainId, " +
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
                    "p.id ")
    Slice<ProductWPriceProjection> findProductsByParams(
            @Param("search") String search,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("chainIds") List<Long> chainIds, Pageable pageable
    );

    List<Product> findByChain(Chain chain);


    @Query("SELECT p FROM Product p WHERE p.chain = :chain AND p.genericProduct.id IS NULL")
    List<Product> findByChainAndNullGenericProduct(Chain chain);

    List<Product> findByGenericProductIn(List<GenericProduct> genericProducts);
    @Query("SELECT p FROM Product p WHERE p.chain = :chain AND p.genericProduct.id IS NULL")
    List<Product> findByChainAndNotNullGenericProduct(Chain chain);

    List<Product> findAllByChainAndGenericProductIsNotNull(Chain chain);

}
