package pt.upskill.groceryroutepro.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.utils.Enum.EmailType;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.verify.password.link}")
    private String verificationLink;


    @Autowired
    private JavaMailSender emailSender;
    public void sendSimpleMessage(User user, String subject, String text, EmailType emailType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("groceryroutepro@gmail.com");
            message.setTo(user.getEmail());
            message.setSubject(subject);


            if (emailType==EmailType.EMAILVERIFICATION){
                text = "Olá " + user.getName() + "\n\nA tua conta foi criada. Por favor carrega no link em baixo e introduz o código" +
                        " para verificar a tua conta. \n\n" + verificationLink + "\n\nCódigo:\n\n" + user.getConfirmation().getCode();
            }

            message.setText(text);
            emailSender.send(message);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }

    }
}

