package org.sellhelp.backend.exceptions;

public class InvalidTokenException extends CustomException{
    public InvalidTokenException(String message){
        super(message);
    }
}
