package pt.upskill.groceryroutepro.Exceptions.Types;

import org.springframework.http.HttpStatus;
import pt.upskill.groceryroutepro.Exceptions.ValidationException;

public class ConfirmationNotFoundException extends ValidationException {
    public ConfirmationNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
