package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.Exceptions.Types.ConfirmationNotFoundException;
import pt.upskill.groceryroutepro.Exceptions.Types.UserAlreadyVerifiedException;
import pt.upskill.groceryroutepro.entities.*;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.repositories.*;
import pt.upskill.groceryroutepro.utils.Enum.EmailType;

import java.util.*;

import static pt.upskill.groceryroutepro.utils.Validations.isValidPassword;

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
    public boolean verifyEmail(String verificationCode) {
        Confirmation confirmation = confirmationRepository.findByToken(verificationCode);
        if (confirmation==null){
            throw new ConfirmationNotFoundException("Token não se encontra em base de dados");
        }

        User user = userRepository.findById(confirmation.getUser().getId()).get();


        if (user!=null){

            if (user.isVerifiedEmail()){
                throw new  UserAlreadyVerifiedException("User já está verificado");
            }
            user.setVerifiedEmail(true);
            userRepository.save(user);

            //delete confimration???
            return true;
        }

        return false;
    }


    @Override
    public User createAccount(SignUp signup) {
        if(userRepository.getByEmail(signup.getEmail()) != null) {
            throw new RuntimeException("O utilizador já existe");
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
        confirmation.setCode(UUID.randomUUID().toString());
        user.setConfirmation(confirmation);
        confirmation.setUser(user);

        // TODO: 29/12/2023 necessário isto??? user no confirmation e confimation no user?

        userRepository.save(user);
        confirmationRepository.save(confirmation);


        emailService.sendSimpleMessage(user, "GroceryRoutePro Email Confirmation", EmailType.EMAILVERIFICATION);

        return user;


        //meter o raio do codigo de confirmação
    }


    @Override
    public boolean getPasswordLinkFromEmail(String email) {
        User user = userRepository.getByEmail(email);
        PasswordLink passwordLink = new PasswordLink();
        passwordLink.setToken(UUID.randomUUID().toString().replace("-",""));

        user.getPasswordLinkList().add(passwordLink);
        passwordLink.setUser(user);

        passwordLinkRepository.save(passwordLink);
        //TODO REDUNDANTE???
        userRepository.save(user);



        emailService.sendSimpleMessage(user, "GroceryRoutePro Change Password", EmailType.PASSWORDLINK);




        return true;
    }

    @Override
    public PasswordLink getPasswordLinkFromToken(String token) {
         PasswordLink passwordLink = passwordLinkRepository.findByToken(token);
        return passwordLink;
    }


    @Override
    public void changePassword(PasswordLink passwordLink, String password) {
        if (isValidPassword(password)){
            User user = passwordLink.getUser();

            user.setPassword(password);
            userRepository.save(user);
        }

    }
}