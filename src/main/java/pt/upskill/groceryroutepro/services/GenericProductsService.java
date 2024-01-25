package pt.upskill.groceryroutepro.services;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import pt.upskill.groceryroutepro.entities.GenericProduct;
import pt.upskill.groceryroutepro.entities.Product;

import java.util.List;

public interface GenericProductsService {

    void createMergedTable();

    void mergeToGenericTable(String chainName);

    void updateGenericProductPrices();

    GenericProduct getGenericProductById(Long genericProductId);

    Slice<GenericProduct> getGenericProductsByParams(String search, String processedSearch, List<Long> categoryIds, List<Long> chainIds, Pageable pageable);

    void updateGenericProducts(Product product);

}
