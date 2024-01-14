package pt.upskill.groceryroutepro.services;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import pt.upskill.groceryroutepro.entities.GenericProduct;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;

import java.util.List;

public interface GenericProductsService {

    void mergeProducts();

    void createMergedTable();

    void mergeToGenericTable(String chainName);

    GenericProduct getProductById(Long genericProductId);

    Slice<GenericProduct> getProductsByParams(String search, List<Long> categoryIds, List<Long> chainIds, Pageable pageable);

}
