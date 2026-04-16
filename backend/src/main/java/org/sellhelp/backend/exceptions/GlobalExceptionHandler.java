package org.sellhelp.backend.exceptions;

import jakarta.persistence.EntityNotFoundException;
import org.sellhelp.backend.dtos.responses.GeneralErrorDTO;
import org.sellhelp.backend.dtos.responses.NotFoundErrorDTO;
import org.sellhelp.backend.dtos.responses.ValidationErrorsDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private GeneralErrorDTO createErrorDto(String errorMessage, HttpStatus httpStatus){
        GeneralErrorDTO errorDTO = new GeneralErrorDTO();

        errorDTO.setTimestamp(LocalDateTime.now());
        errorDTO.setMessage(errorMessage);
        errorDTO.setStatus(httpStatus.value());

        return errorDTO;
    }

    private NotFoundErrorDTO createNotFoundErrorDto(String errorMessage, HttpStatus httpStatus, String path){
        NotFoundErrorDTO errorDTO = new NotFoundErrorDTO();

        errorDTO.setTimestamp(LocalDateTime.now());
        errorDTO.setMessage(errorMessage);
        errorDTO.setStatus(httpStatus.value());
        errorDTO.setPath(path);

        return errorDTO;
    }

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

    @ExceptionHandler({LoginCredentialsException.class, UserBannedException.class,
            InvalidTotpException.class, InvalidTokenException.class})
    public ResponseEntity<GeneralErrorDTO> handleAuthenticationException(CustomException ex) {
        GeneralErrorDTO errorDTO = createErrorDto(ex.getMessage(), HttpStatus.UNAUTHORIZED);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(IncorrectUserRoleException.class)
    public ResponseEntity<GeneralErrorDTO> handleInvalidPermissionException(CustomException ex) {
        GeneralErrorDTO errorDTO = createErrorDto(ex.getMessage(), HttpStatus.FORBIDDEN);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GeneralErrorDTO> handleUserNotFoundException(CustomException ex) {
        GeneralErrorDTO errorDTO = createErrorDto(ex.getMessage(), HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<GeneralErrorDTO> handleEntityNotFoundException(EntityNotFoundException ex) {
        GeneralErrorDTO errorDTO = createErrorDto(ex.getMessage(), HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<GeneralErrorDTO> handleUserAlreadyExistException(CustomException ex) {
        GeneralErrorDTO errorDTO = createErrorDto(ex.getMessage(), HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<GeneralErrorDTO> handleMessagingException(EmailException ex) {
        GeneralErrorDTO errorDTO = createErrorDto(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GeneralErrorDTO> handleRuntimeException(RuntimeException ex) {
        GeneralErrorDTO errorDTO = createErrorDto(ex.getMessage(), HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GeneralErrorDTO> handleInvalidJson(
            HttpMessageNotReadableException ex) {

        GeneralErrorDTO errorDTO = createErrorDto("Invalid JSON syntax!", HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GeneralErrorDTO> handleException(Exception ex) {
        GeneralErrorDTO errorDTO = createErrorDto(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GeneralErrorDTO> handleIllegalArgumentException(IllegalArgumentException ex) {
        GeneralErrorDTO errorDTO = createErrorDto(ex.getMessage(), HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<NotFoundErrorDTO> handleNotFoundException(NoHandlerFoundException ex) {
        NotFoundErrorDTO errorDTO = createNotFoundErrorDto("Az oldal nem létezik!", HttpStatus.NOT_FOUND, ex.getRequestURL());

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(WrongFileTypeException.class)
    public ResponseEntity<GeneralErrorDTO> handleWrongFileTypeException(WrongFileTypeException ex) {
        GeneralErrorDTO errorDTO = createErrorDto(ex.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);

        return ResponseEntity.status(errorDTO.getStatus()).body(errorDTO);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleMultipartException(MultipartException ex) {
        if (ex.getCause() instanceof org.springframework.web.multipart.MaxUploadSizeExceededException) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body("A fájl mérete nem lehet nagyobb, mint 10 MB!");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Hibás fájl feltöltés!");
    }
}

