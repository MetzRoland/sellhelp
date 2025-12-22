package org.sellhelp.backend.exceptions;

public class UserAlreadyExistException extends CustomException {
    public UserAlreadyExistException(String message) {
        super(message);
    }
}
