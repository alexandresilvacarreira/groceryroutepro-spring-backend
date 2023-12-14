package pt.upskill.groceryroutepro.services;


import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.Login;
import pt.upskill.groceryroutepro.models.SignUp;

public interface AuthService {

    User validateLogin(Login login);

    User createAccount(SignUp signup);
}
