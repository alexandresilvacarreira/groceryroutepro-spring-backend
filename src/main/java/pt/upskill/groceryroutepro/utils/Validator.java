package pt.upskill.groceryroutepro.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;

public class Validator {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public static boolean isExpired(LocalDateTime createdDate){
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdDate, now);
        long hoursElapsed = duration.toHours();
        return hoursElapsed > 1;
    }

    public static boolean verifyToken(String plainToken, String hashedToken) {
        return passwordEncoder.matches(plainToken, hashedToken);
    }
}
