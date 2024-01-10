package pt.upskill.groceryroutepro.services;

import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.utils.Enum.EmailType;

public interface EmailService {

    void sendSimpleMessage(User user, String subject, String detail, EmailType emailType);

}
