package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.*;
import pt.upskill.groceryroutepro.exceptions.types.BadRequestException;
import pt.upskill.groceryroutepro.exceptions.types.UnauthorizedException;
import pt.upskill.groceryroutepro.models.EmailVerificationToken;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.repositories.*;
import pt.upskill.groceryroutepro.utils.Enum.EmailType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static pt.upskill.groceryroutepro.utils.Validator.isExpired;
import static pt.upskill.groceryroutepro.utils.Validator.verifyToken;

@Service
public class ShoppingListServiceImpl implements ShoppingListService {

    @Autowired
    UserRepository userRepository;




}