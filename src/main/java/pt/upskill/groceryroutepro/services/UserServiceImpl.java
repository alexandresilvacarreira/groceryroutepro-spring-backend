package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.*;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.repositories.*;
import pt.upskill.groceryroutepro.utils.Enum.EmailType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
        return this.userRepository.getByEmail(email);
    }

    @Override
    public boolean verifyEmail(String verificationCode) {
        //get the user from the verification code and set its verification to true


        // TODO devia ser assim certo??
        /*try {
            Confirmation confirmation = confirmationRepository.findByToken(verificationCode);
        } catch (NotFoundException e){
            throw NotFoundException e
        }*/
        Confirmation confirmation = confirmationRepository.findByToken(verificationCode);

        User user = userRepository.findById(confirmation.getUser().getId()).get();


        if (user!=null){
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
    public boolean getPasswordLink(String email) {
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
}