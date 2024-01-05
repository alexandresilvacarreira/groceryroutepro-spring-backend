package pt.upskill.groceryroutepro.utils;

import org.springframework.beans.factory.annotation.Value;

public class EmailUtils {
    @Value("${spring.mail.verify.password.link}")
    private String verificationLink;

    @Value("${spring.mail.change.password.link}")
    private String passwordChangeLink;

    @Value("${spring.mail.username}")
    private String email;

    public String verifyEmailMessage(String name, String host, String code){
        return "Olá " + name + "\n\nA tua conta foi criada. Por favor carrega no link em baixo e introduz o código" +
                " para verificar a tua conta. \n\n" + verificationLink + "\n\nCódigo:\n\n" + code;
    }


    public String changePasswordMessage(String name, String token){
        return "Olá "+ name + "\n\nRecebemos um pedido para alterar a palavra-passe associada à sua conta na" +
                " GroceryRoutePro. Para garantir a segurança da sua conta, siga as instruções abaixo para concluir" +
                " o processo de alteração da palavra-passe:" +
                "\n\n1. Clique na seguinte ligação para aceder à página de reposição da palavra-passe:\n\n" +
                passwordChangeLink + token + "\n\n2. Será direcionado para uma página segura onde poderá introduzir" +
                " uma nova palavra-passe para a sua conta.\n\n" +
                "3. Introduza a sua nova palavra-passe e confirme-a escrevendo-a novamente.\n\n" +
                "Clique no botão \"Enviar\" ou \"Alterar palavra-passe\" para guardar a sua nova palavra-passe.\n\n" +
                "Se não tiver solicitado esta alteração de palavra-passe, contacte imediatamente a nossa equipa de " +
                "assistência através de " + email + ". A segurança da sua conta é a nossa principal prioridade e " +
                "queremos garantir que apenas são efectuadas alterações autorizadas.\n\n" +
                "Obrigado por nos ajudar a manter a segurança da sua conta. Se tiver alguma dúvida ou preocupação, " +
                "não hesite em contactar a nossa equipa de apoio.\n\n" +
                "Com os melhores cumprimentos,\n\n" +
                "Equipa de apoio GroceryRoutePro";
    }
}
