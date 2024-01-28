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

    @Value("${spring.mail.change.password.link}")
    private String passwordChangeLink;

    @Value("${spring.mail.username}")
    private String email;


    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(User user, String subject, String detail, EmailType emailType) {
        try {
            String text = "";
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("groceryroutepro@gmail.com");
            message.setTo(user.getEmail());
            message.setSubject(subject);


            if (emailType == EmailType.EMAILVERIFICATION) {
                text = "Olá " + user.getName() + "\n\nA tua conta foi criada com sucesso. Por favor carrega no link em baixo e introduz o código" +
                        " para verificar a tua conta. \n\n" + verificationLink + user.getEmail() + "\n\nCódigo:\n\n"
                        + detail + "\n\nBoas Compras! \n\n Equipa de apoio GroceryRoutePro";
            } else if (emailType == EmailType.PASSWORDLINK) {
                text = "Olá " + user.getName() + "\n\nRecebemos um pedido para alterar a palavra-passe associada à sua conta na" +
                        " GroceryRoutePro. Para garantir a segurança da sua conta, siga as instruções abaixo para concluir" +
                        " o processo de alteração da palavra-passe:" +
                        "\n\n1. Clique na seguinte ligação para aceder à página de reposição da palavra-passe:\n\n" +
                        passwordChangeLink + user.getEmail() + "-" + detail +
                        "\n\n2. Será direcionado para uma página segura onde poderá introduzir" +
                        " uma nova palavra-passe para a sua conta.\n\n" +
                        "3. Introduza a sua nova palavra-passe e confirme-a escrevendo-a novamente.\n\n" +
                        "Clique no botão \"Enviar\" ou \"Alterar palavra-passe\" para guardar a sua nova palavra-passe.\n\n" +
                        "Se não tiver solicitado esta alteração de palavra-passe, contacte imediatamente a nossa equipa de " +
                        "assistência através de " + email + ".\n\n A segurança da sua conta é a nossa principal prioridade e " +
                        "queremos garantir que apenas são efectuadas alterações autorizadas.\n\n" +
                        "Obrigado por nos ajudar a manter a segurança da sua conta. Se tiver alguma dúvida ou preocupação, " +
                        "não hesite em contactar a nossa equipa de apoio.\n\n" +
                        "Com os melhores cumprimentos,\n\n" +
                        "Equipa de apoio GroceryRoutePro";
            }
            message.setText(text);
            emailSender.send(message);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }

    }
}

