package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.Price;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.exceptions.types.BadRequestException;
import pt.upskill.groceryroutepro.models.ProductData;
import pt.upskill.groceryroutepro.projections.ProductWPriceProjection;
import pt.upskill.groceryroutepro.repositories.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.parseDouble;

@Service
public class ProductsServiceImpl implements ProductsService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;


    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Override
    public Slice<ProductWPriceProjection> getProductsByParams(String search, List<Long> categoryIds, List<Long> chainIds, Pageable pageable) {
        return productRepository.findProductsByParams(search, categoryIds, chainIds, pageable);
    }

    @Override
    public void createProduct(ProductData productData) {

        User user = userService.getAuthenticatedUser();
        if(user== null) throw new BadRequestException("Utilizado não encontrado");

        Product product = new Product();

        product.setName(productData.getProduct().getName());
        product.setQuantity(productData.getProduct().getQuantity());
        product.setBrand(productData.getProduct().getBrand());
        product.setImageUrl(productData.getProduct().getImageUrl());
        product.setChain(productData.getProduct().getChain());

        Set<Long> productCategories = productData.getProduct().getCategories().stream().map(c -> c.getId()).collect(Collectors.toSet());

        product.setCategories(new HashSet<>(categoryRepository.findAllById(productCategories)));

        Price price = new Price();

        price.setPrimaryValue(productData.getPrice().getPrimaryValue());
        price.setSecondaryValue(productData.getPrice().getSecondaryValue());
        price.setPrimaryUnit(productData.getPrice().getPrimaryUnit());
        price.setSecondaryUnit(productData.getPrice().getSecondaryUnit());
        price.setPriceWoDiscount(productData.getPrice().getPriceWoDiscount());
        price.setCollectionDate(LocalDateTime.now());


        if (productData.getPrice().getPriceWoDiscount().isEmpty()) {
            price.setProduct(product);

            List<Price> prices = new ArrayList<>();
            prices.add(price);
            product.setPrices(prices);

            productRepository.save(product);

        } else if (!productData.getPrice().getPriceWoDiscount().isEmpty()) {
            double a = parseDouble(productData.getPrice().getPriceWoDiscount());
            double b = productData.getPrice().getPrimaryValue();

            price.setDiscountPercentage((int) Math.round((a - b) * 100 / a));

            price.setProduct(product);

            List<Price> prices = new ArrayList<>();
            prices.add(price);
            product.setPrices(prices);

            productRepository.save(product);

        } else {
            throw new BadRequestException("Produto com dados incorrectos");
        }
    }

    public void editProduct(ProductData productData) {

        User user = userService.getAuthenticatedUser();
        if(user== null) throw new BadRequestException("Utilizado não encontrado");

        Product product = productData.getProduct();

        product.setId(productData.getProduct().getId());
        product.setName(productData.getProduct().getName());
        product.setQuantity(productData.getProduct().getQuantity());
        product.setBrand(productData.getProduct().getBrand());
        product.setImageUrl(productData.getProduct().getImageUrl());
        product.setChain(productData.getProduct().getChain());


        Set<Long> productCategories = productData.getProduct().getCategories().stream().map(c -> c.getId()).collect(Collectors.toSet());

        product.setCategories(new HashSet<>(categoryRepository.findAllById(productCategories)));

        Price price = new Price();

        price.setPrimaryValue(productData.getPrice().getPrimaryValue());
        price.setSecondaryValue(productData.getPrice().getSecondaryValue());
        price.setPrimaryUnit(productData.getPrice().getPrimaryUnit());
        price.setSecondaryUnit(productData.getPrice().getSecondaryUnit());
        price.setPriceWoDiscount(productData.getPrice().getPriceWoDiscount());
        price.setCollectionDate(LocalDateTime.now());

        if (productData.getPrice().getPriceWoDiscount().isEmpty()) {
            price.setProduct(product);

            List<Price> prices = new ArrayList<>();
            prices.add(price);
            product.setPrices(prices);

            productRepository.save(product);

        } else if (!productData.getPrice().getPriceWoDiscount().isEmpty()) {
            double a = parseDouble(productData.getPrice().getPriceWoDiscount());
            double b = productData.getPrice().getPrimaryValue();
            price.setDiscountPercentage((int) Math.round((a - b) * 100 / a));

            price.setProduct(product);

            List<Price> prices = new ArrayList<>();
            prices.add(price);
            product.setPrices(prices);

            productRepository.save(product);

        } else {
            throw new BadRequestException("Produto com dados incorrectos");
        }
    }
    @Override
    public Product getProductById(Long productId) {
        Product product = productRepository.findById(productId).get();
        User user = this.userService.getAuthenticatedUser();
        if (user == null) throw new BadRequestException("Utilizador não encontrado");
        if (!user.getChain().getId().equals(product.getChain().getId())) {
            throw new BadRequestException("O utilizador não tem permissão para aceder a este produto");
        }
        return product;
    }
}
