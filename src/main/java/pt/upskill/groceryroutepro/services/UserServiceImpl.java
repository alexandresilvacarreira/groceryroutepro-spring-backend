package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.exceptions.types.BadRequestException;
import pt.upskill.groceryroutepro.entities.*;
import pt.upskill.groceryroutepro.exceptions.types.UnauthorizedException;
import pt.upskill.groceryroutepro.models.EmailVerificationToken;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.repositories.*;
import pt.upskill.groceryroutepro.utils.Enum.EmailType;

import java.util.*;

import static pt.upskill.groceryroutepro.utils.Validations.isValidPassword;
import static pt.upskill.groceryroutepro.utils.Validator.isExpired;
import static pt.upskill.groceryroutepro.utils.Validator.verifyToken;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    ConfirmationRepository confirmationRepository;

    @Autowired
    PasswordLinkRepository passwordLinkRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);


    @Override
    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) auth.getPrincipal();
        return this.userRepository.getByEmail(email); //TODO enviar só a informação necessária - criar UserModel/UserDto
    }

    @Override
    public void verifyEmail(EmailVerificationToken emailVerificationToken) {
        User user = userRepository.findByEmail(emailVerificationToken.getEmail());
        if (user == null) throw new BadRequestException("Link de verificação incorreto");
        Confirmation confirmation = confirmationRepository.findByUser_Id(user.getId());
        String hashToken = confirmation.getToken();
        if (!verifyToken(emailVerificationToken.getToken(), hashToken)) throw new BadRequestException("Token inválido");


        if (user.isVerifiedEmail()) {
            throw new BadRequestException("Email já está verificado");
        }
        user.setVerifiedEmail(true);
        userRepository.save(user);
    }


    @Override
    public User createAccount(SignUp signup) {
        if (userRepository.getByEmail(signup.getEmail()) != null) {
            throw new BadRequestException("O utilizador já existe");
        }
        User user = new User();
        user.setName(signup.getName());
        // if password esta com formatçao errada
        //
        user.setEmail(signup.getEmail());
        user.setPassword(passwordEncoder.encode(signup.getPassword()));
        Role role = roleRepository.findByName("USER_FREE");
        user.setRole(role);
        List<Store> storesList = storeRepository.findAll();
        Set<Store> storeSet = new HashSet<>(storesList);
        user.setStores(storeSet);

        user.setVerifiedEmail(false);

        Confirmation confirmation = new Confirmation();
        String confirmationToken = UUID.randomUUID().toString().replace("-", "");
        confirmation.setToken(passwordEncoder.encode(confirmationToken));
        user.setConfirmation(confirmation);
        confirmation.setUser(user);

        // TODO: 29/12/2023 necessário isto??? user no confirmation e confimation no user?

        userRepository.save(user);
        confirmationRepository.save(confirmation);


        emailService.sendSimpleMessage(user, "GroceryRoutePro Email Confirmation", confirmationToken, EmailType.EMAILVERIFICATION);

        return user;


        //meter o raio do codigo de confirmação
    }


    @Override
    public boolean getPasswordLinkFromEmail(String email) {
        User user = userRepository.getByEmail(email);

        if (user == null) throw new BadRequestException("Não existe nenhuma conta com este email");

        PasswordLink passwordLink = passwordLinkRepository.findByUser_Id(user.getId());

        if (passwordLink==null) passwordLink = new PasswordLink();

        String token = UUID.randomUUID().toString().replace("-", "");

        passwordLink.setToken(passwordEncoder.encode(token));

        passwordLink.setUser(user);

        passwordLinkRepository.save(passwordLink);

        userRepository.save(user);


        emailService.sendSimpleMessage(user, "GroceryRoutePro Change Password", token, EmailType.PASSWORDLINK);


        return true;
    }

    @Override
    public PasswordLink getPasswordLinkFromToken(String token) {
        String encodedToken = passwordEncoder.encode(token);
        PasswordLink passwordLink = passwordLinkRepository.findByToken(encodedToken);
        return passwordLink;
    }



    @Override
    public void changePassword(String email, String token, String password) {
        User user = userRepository.findByEmail(email);
        if (user ==null) throw new BadRequestException("Link de alteração de palavra-chave incorreto");
        PasswordLink passwordLink = passwordLinkRepository.findByUser_Id(user.getId());
        if (!verifyToken(token, passwordLink.getToken()))throw new BadRequestException("Link de alteração de palavra-chave incorreto");
       if (isExpired(passwordLink.getCreatedDate())) throw new UnauthorizedException("Link expirado");
      // if (isValidPassword(password)) throw new BadRequestException("Password não está no formato correto");

       String encondedPassword = passwordEncoder.encode(password);
       user.setPassword(encondedPassword);
       passwordLinkRepository.delete(passwordLink);
       userRepository.save(user);

    }


}