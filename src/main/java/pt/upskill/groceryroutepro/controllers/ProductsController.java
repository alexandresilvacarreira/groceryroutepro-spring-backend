package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import pt.upskill.groceryroutepro.entities.GenericProduct;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.exceptions.ValidationException;
import pt.upskill.groceryroutepro.models.ProductWPriceList;
import pt.upskill.groceryroutepro.models.Pagination;
import pt.upskill.groceryroutepro.models.ProductDetails;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;
import pt.upskill.groceryroutepro.services.GenericProductsService;
import pt.upskill.groceryroutepro.services.ProductsService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Component
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    ProductsService productsService;

    @Autowired
    GenericProductsService genericProductsService;

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
    public ResponseEntity<ProductWPriceList> listProducts(@RequestParam(defaultValue = "") String search,
                                                          @RequestParam(defaultValue = "0") Integer page,
                                                          @RequestParam(defaultValue = "10") Integer size,
                                                          @RequestParam(defaultValue = "1,2,3,4,5,6,7") List<Long> chains,
                                                          @RequestParam(defaultValue = "1,2,3,4,5,6,7,8,9,10") List<Long> categories,
                                                          @RequestParam(defaultValue = "pricePrimaryValue,asc") String sort) {

        ProductWPriceList results = new ProductWPriceList();
        String[] sortParams = sort.split(",");

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortParams[1].equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortParams[0]));

        try {
            Slice<ProductWPriceProjection> products = productsService.getProductsByParams(search, categories, chains, pageable);
            results.setProducts(products.getContent());
            results.setSuccess(true);
            Pagination pagination = new Pagination(products.getNumber(), products.getNumberOfElements(), products.hasNext(), products.hasPrevious());
            results.setPagination(pagination);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            String errorMessage = "Erro ao obter produtos: " + e.getMessage();
            results.setErrorMessage(errorMessage);
            results.setSuccess(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(results);
        }

    }

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productsService.createProduct(product);
            return ResponseEntity.ok(createdProduct);
        } catch (Exception e) {
            String errorMessage = "Erro ao criar o produto: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
    @PostMapping("/merge-products")
    public ResponseEntity mergeProducts() {
        try {
            genericProductsService.mergeProducts();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/generic-products/{genericProductId}")
    public ResponseEntity getGenericProduct(@PathVariable Long genericProductId) {
        Map<String, Object> response = new HashMap<>();
        try {
            GenericProduct genericProduct = genericProductsService.getGenericProductById(genericProductId);
            Map<String, Object> data = new HashMap<>();
            data.put("genericProduct", genericProduct);
            response.put("data", data);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        }
    }


    @GetMapping("generic-products/list")
    public ResponseEntity listGenericProducts(@RequestParam(defaultValue = "") String search,
                                              @RequestParam(defaultValue = "0") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(defaultValue = "1,2,3,4,5,6,7") List<Long> chains,
                                              @RequestParam(defaultValue = "1,2,3,4,5,6,7,8,9,10") List<Long> categories,
                                              @RequestParam(defaultValue = "name,asc") String sort) {

        String[] sortParams = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortParams[1].equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortParams[0]));
        Map<String, Object> response = new HashMap<>();
        String processedSearch = search.toLowerCase().replaceAll("\\s","");

        try {
            Slice<GenericProduct> genericProducts = genericProductsService.getGenericProductsByParams(processedSearch, categories, chains, pageable);
            Map<String, Object> data = new HashMap<>();
            data.put("genericProducts", genericProducts.getContent());
            data.put("pagination", new Pagination(genericProducts.getNumber(), genericProducts.getNumberOfElements(), genericProducts.hasNext(), genericProducts.hasPrevious()));
            response.put("data", data);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        }

    }

    @PostMapping("/create-merged-table")
    public ResponseEntity createMergedTable() {
        try {
            genericProductsService.createMergedTable();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/merge-to-generic-table/{chainName}")
    public ResponseEntity mergeToGenericTable(@PathVariable String chainName) {
        try {
            genericProductsService.mergeToGenericTable(chainName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/merge-all-to-generic-table")
    public ResponseEntity mergeAllToGenericTable() {
        try {
            List<String> chainNames = Arrays.asList("continente", "intermarché", "minipreço", "auchan");
            for (String chainName : chainNames){
                genericProductsService.mergeToGenericTable(chainName);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/update-generic-products-prices")
    public ResponseEntity updateGenericProductPrices() {
        try {
            genericProductsService.updateGenericProductPrices();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
