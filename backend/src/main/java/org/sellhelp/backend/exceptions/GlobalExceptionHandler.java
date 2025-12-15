package org.sellhelp.backend.exceptions;

import org.sellhelp.backend.dtos.responses.GeneralErrorDTO;
import org.sellhelp.backend.dtos.responses.ValidationErrorsDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorsDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ValidationErrorsDTO validationErrorsDTO = new ValidationErrorsDTO();

        validationErrorsDTO.setTimestamp(LocalDateTime.now());
        validationErrorsDTO.setStatus(HttpStatus.BAD_REQUEST.value());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        validationErrorsDTO.setErrors(fieldErrors);

        return new ResponseEntity<>(validationErrorsDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GeneralErrorDTO> handleAuthenticationException(AuthenticationException ex) {
        GeneralErrorDTO errorDTO = new GeneralErrorDTO();

        errorDTO.setStatus(HttpStatus.UNAUTHORIZED.value());
        errorDTO.setTimestamp(LocalDateTime.now());
        errorDTO.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDTO);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GeneralErrorDTO> handleRuntimeException(RuntimeException ex) {
        GeneralErrorDTO errorDTO = new GeneralErrorDTO();

        errorDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        errorDTO.setTimestamp(LocalDateTime.now());
        errorDTO.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GeneralErrorDTO> handleInvalidJson(
            HttpMessageNotReadableException ex) {

        GeneralErrorDTO errorDTO = new GeneralErrorDTO();

        errorDTO.setStatus(HttpStatus.BAD_REQUEST.value());
        errorDTO.setTimestamp(LocalDateTime.now());
        errorDTO.setMessage("Invalid JSON syntax!");

        return ResponseEntity.badRequest().body(errorDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GeneralErrorDTO> handleException(Exception ex) {
        GeneralErrorDTO errorDTO = new GeneralErrorDTO();

        errorDTO.setTimestamp(LocalDateTime.now());
        errorDTO.setMessage(ex.getMessage());
        errorDTO.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}

