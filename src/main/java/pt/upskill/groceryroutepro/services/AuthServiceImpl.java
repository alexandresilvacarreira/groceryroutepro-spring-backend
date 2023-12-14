package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.Login;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.repositories.RoleRepository;
import pt.upskill.groceryroutepro.repositories.UserRepository;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Override
    public User validateLogin(Login login) {
        User user = userRepository.getByEmail(login.getEmail());
        if(user != null && passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public User createAccount(SignUp signup) {
        if(userRepository.getByEmail(signup.getEmail()) != null) {
            return null;
        }
        User user = new User();
        user.setName(signup.getName());
        user.setEmail(signup.getEmail());
        user.setPassword(passwordEncoder.encode(signup.getPassword()));
        user.setRole(roleRepository.findById(1L).get());
        return userRepository.save(user);
    }
}