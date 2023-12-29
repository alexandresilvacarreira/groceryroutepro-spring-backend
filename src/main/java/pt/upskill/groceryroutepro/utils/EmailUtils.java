package pt.upskill.groceryroutepro.utils;

import org.springframework.beans.factory.annotation.Value;

public class EmailUtils {
    @Value("${spring.mail.verify.password.link}")
    private static String verificationLink;

    public static String verifyEmailMessage(String name, String host, String code){
        return "Olá " + name + "\n\nA tua conta foi criada. Por favor carrega no link em baixo e introduz o código" +
                " para verificar a tua conta. \n\n" + verificationLink + "\n\nCódigo:\n\n" + code;
    }
}
