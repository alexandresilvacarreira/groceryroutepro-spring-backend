package pt.upskill.groceryroutepro.Exceptions.Types;

import org.springframework.http.HttpStatus;
import pt.upskill.groceryroutepro.Exceptions.ValidationException;

public class UserAlreadyVerifiedException extends ValidationException {
    private HttpStatus status;
    public UserAlreadyVerifiedException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
