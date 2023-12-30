package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.models.ListProductWPrice;
import pt.upskill.groceryroutepro.models.ProductDetails;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;
import pt.upskill.groceryroutepro.services.ProductsService;

import java.util.Arrays;
import java.util.List;

@RestController
@Component
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    ProductsService productsService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetails> getProduct(@PathVariable Long productId) {
        ProductDetails productDetails = new ProductDetails();
        try {
            Product product = productsService.getProductById(productId);
            if (product != null) {
                productDetails.setProduct(product);
                productDetails.setPrices(product.getPrices());
                productDetails.setSuccess(true);
                return ResponseEntity.ok(productDetails);
            } else {
                String errorMessage = "Produto " + productId + " não encontrado.";
                productDetails.setErrorMessage(errorMessage);
                productDetails.setSuccess(false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(productDetails);
            }
        } catch (Exception e) {
            String errorMessage = "Erro ao obter produto: " + e.getMessage();
            productDetails.setErrorMessage(errorMessage);
            productDetails.setSuccess(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(productDetails);
        }

    }

    @GetMapping("/list")
    public ResponseEntity<ListProductWPrice> listProducts(@RequestParam(defaultValue = "") String search,
                                                          @RequestParam(defaultValue = "0") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size,
                                                          @RequestParam(defaultValue = "1,2,3,4,5,6,7") List<Long> chains,
                                                          @RequestParam(defaultValue = "1,2,3,4,5,6,7,8,9,10") List<Long> categories,
                                                          @RequestParam(defaultValue = "pricePrimaryValue,asc") String sort) {

        ListProductWPrice results = new ListProductWPrice();

        String[] sortParams = sort.split(",");

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortParams[1].equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortParams[0]));

//
//        Pageable pageable;
//        if (sortParams[1].equals("desc")){
//            pageable = PageRequest.of(page, size, Sort.by(sortParams[0]).descending());
//        } else {
//            pageable = PageRequest.of(page, size, Sort.by(sortParams[0]).ascending());
//        }

        try {
            Slice<ProductWPriceProjection> products = productsService.getProductsByParams(search, categories, chains, pageable);
            results.setProducts(products);
            results.setSuccess(true);
            results.setHasNextPage(products.hasNext());
            results.setHasPreviousPage(products.hasPrevious());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            String errorMessage = "Erro ao obter produtos: " + e.getMessage();
            results.setErrorMessage(errorMessage);
            results.setSuccess(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(results);
        }

    }

}
