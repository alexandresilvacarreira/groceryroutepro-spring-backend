package pt.upskill.groceryroutepro.services;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.models.ProductData;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;

import java.util.List;

public interface ProductsService {

    Product getProductById(Long productId);

    Slice<ProductWPriceProjection> getProductsByParams(String search, List<Long> categoryIds, List<Long> chainIds, Pageable pageable);

    void createProduct(ProductData productData);

    void editProduct(ProductData productData);
}
