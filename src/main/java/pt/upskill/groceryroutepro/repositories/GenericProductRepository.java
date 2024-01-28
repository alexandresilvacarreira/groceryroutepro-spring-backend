package pt.upskill.groceryroutepro.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.upskill.groceryroutepro.entities.GenericProduct;

import java.util.List;


@Repository
public interface GenericProductRepository extends JpaRepository<GenericProduct, Long> {


    @Query("SELECT DISTINCT p " +
            "FROM GenericProduct p " +
            "JOIN p.categories c " +
            "JOIN p.chains s " +
            "WHERE (p.name LIKE %:search% " +
            "OR p.brand LIKE %:search% " +
            "OR p.processedName LIKE %:processedSearch% " +
            "OR p.processedBrand LIKE %:processedSearch%) " +
            "AND c.id IN :categoryIds " +
            "AND s.id IN :chainIds")
    Slice<GenericProduct> findGenericProductByParams(
            @Param("search") String search,
            @Param("processedSearch") String processedSearch,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("chainIds") List<Long> chainIds,
            Pageable pageable
    );



    List<GenericProduct> findAllByCurrentCheapestProductIsNull();


}
