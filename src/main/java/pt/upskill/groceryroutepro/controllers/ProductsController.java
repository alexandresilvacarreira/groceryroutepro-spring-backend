package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.models.ProductDetails;
import pt.upskill.groceryroutepro.models.ProductWPrices;
import pt.upskill.groceryroutepro.services.ProductsService;

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
                productDetails.setProductDetails(new ProductWPrices(product, product.getPrices()));
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
    public ResponseEntity<ProductDetails> listProducts(@PathVariable Long productId) {
        ProductDetails productDetails = new ProductDetails();
        try {
            Product product = productsService.getProductById(productId);
            if (product != null) {
                productDetails.setProductDetails(new ProductWPrices(product, product.getPrices()));
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

}
