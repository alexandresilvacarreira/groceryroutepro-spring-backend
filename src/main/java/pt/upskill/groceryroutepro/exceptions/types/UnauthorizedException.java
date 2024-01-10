package pt.upskill.groceryroutepro.exceptions.types;

import org.springframework.http.HttpStatus;
import pt.upskill.groceryroutepro.exceptions.ValidationException;

public class UnauthorizedException extends ValidationException {
    private HttpStatus status;
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
