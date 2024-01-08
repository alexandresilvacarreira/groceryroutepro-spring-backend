package pt.upskill.groceryroutepro.exceptions.types;

import org.springframework.http.HttpStatus;
import pt.upskill.groceryroutepro.exceptions.ValidationException;

public class BadRequestException extends ValidationException {
    private HttpStatus status;
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
