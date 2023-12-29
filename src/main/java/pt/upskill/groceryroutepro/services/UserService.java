package pt.upskill.groceryroutepro.services;

import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.SignUp;

public interface UserService {

    User getAuthenticatedUser();

    boolean verifyEmail(String verificationCode);
    User createAccount(SignUp signup);


}
