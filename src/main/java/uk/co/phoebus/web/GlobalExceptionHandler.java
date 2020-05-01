package uk.co.phoebus.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.co.phoebus.exception.KycRepositoryException;
import uk.co.phoebus.exception.KycRequestValidationException;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    public static final String DUPLICATE_RESOURCE_ERROR_MESSAGE = "Resource already exists";

    @ExceptionHandler
    public ResponseEntity<List<RestError>> constraintViolationException(DataIntegrityViolationException e) {
        log.warn("constraintViolationException", e);
        RestError error = RestError.builder()
                .message(DUPLICATE_RESOURCE_ERROR_MESSAGE)
                .build();
        return ResponseEntity.badRequest().body(newArrayList(error));
    }

    @ExceptionHandler
    public ResponseEntity<List<RestError>> validationException(KycRequestValidationException e) {
        log.warn("validationException", e);
        List<RestError> errors = e.getErrors().stream()
                .map(this::toRestError)
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler
    public ResponseEntity<List<RestError>> repositoryException(KycRepositoryException e) {
        log.warn("validationException", e);
        RestError error = RestError.builder()
                .message(e.getMessage())
                .build();
        return ResponseEntity.badRequest().body(newArrayList(error));
    }

    private RestError toRestError(FieldError fieldError) {
        return RestError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .build();
    }

}
