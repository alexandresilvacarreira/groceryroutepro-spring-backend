package pt.upskill.groceryroutepro.Exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends RuntimeException {

    HttpStatus statusCode;
    public ValidationException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }
}
