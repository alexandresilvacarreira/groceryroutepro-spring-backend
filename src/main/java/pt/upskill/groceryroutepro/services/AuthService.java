package pt.upskill.groceryroutepro.services;


import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.Login;

public interface AuthService {

    User validateLogin(Login login);


}
