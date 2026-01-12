package org.sellhelp.backend.exceptions;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(String message){
        super(message);
    }
}
