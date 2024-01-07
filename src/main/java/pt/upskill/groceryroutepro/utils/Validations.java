package pt.upskill.groceryroutepro.utils;

import org.springframework.context.annotation.Bean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Validations {

    public static boolean isValidPassword(String password) {
        // Copyued from frontend validators
        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{5,}$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(password);
        return matcher.matches();
    }
}
