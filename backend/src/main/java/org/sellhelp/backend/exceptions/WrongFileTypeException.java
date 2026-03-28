package org.sellhelp.backend.exceptions;

public class WrongFileTypeException extends RuntimeException {
    public WrongFileTypeException(String message) {
        super(message);
    }
}