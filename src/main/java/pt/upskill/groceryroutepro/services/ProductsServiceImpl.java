package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;
import pt.upskill.groceryroutepro.repositories.*;

import java.util.List;

@Service
public class ProductsServiceImpl implements ProductsService {

    @Autowired
    ProductRepository productRepository;

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).isPresent() ? productRepository.findById(productId).get() : null;
    }

    @Override
    public List<ProductWPriceProjection> getProductsByParams(String search, List<Long> categoryIds, List<Long> chainIds, int nbResults, int page) {
        return productRepository.findProductsByParams(search, categoryIds, chainIds, nbResults, page * nbResults);
    }
}