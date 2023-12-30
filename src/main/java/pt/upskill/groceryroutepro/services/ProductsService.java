package pt.upskill.groceryroutepro.services;

import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;

import java.util.List;

public interface ProductsService {

    Product getProductById(Long productId);

    List<ProductWPriceProjection> getProductsByParams(String search, List<Long> categoryIds, List<Long> chainIds, int nbResults, int page);

}
