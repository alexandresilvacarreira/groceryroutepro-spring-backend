package pt.upskill.groceryroutepro.services;

public interface EmailService {

    void sendSimpleMessage(String to, String subject, String text);

}
