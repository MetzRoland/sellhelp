package org.sellhelp.backend.exceptions;

public class InvalidTotpException extends CustomException {
    public InvalidTotpException(String message) {
        super(message);
    }
}
