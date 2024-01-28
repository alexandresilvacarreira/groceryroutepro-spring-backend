package pt.upskill.groceryroutepro.services;

import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.EmailVerificationToken;
import pt.upskill.groceryroutepro.models.SignUp;

public interface UserService {

    User getAuthenticatedUser();

    void verifyEmail(EmailVerificationToken emailVerificationToken);
    User createAccount(SignUp signup);

    void getPasswordLinkFromEmail(String email);


    void changePassword(String email, String token, String password);


}
