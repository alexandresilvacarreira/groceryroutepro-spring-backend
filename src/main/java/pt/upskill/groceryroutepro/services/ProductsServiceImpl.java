package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.Product;
import pt.upskill.groceryroutepro.entities.Role;
import pt.upskill.groceryroutepro.entities.Store;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.Login;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.repositories.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductsServiceImpl implements ProductsService {

    @Autowired
    ProductRepository productRepository;

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).isPresent() ? productRepository.findById(productId).get() : null;
    }
}