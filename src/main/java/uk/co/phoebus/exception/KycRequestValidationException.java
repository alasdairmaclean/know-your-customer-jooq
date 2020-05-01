package uk.co.phoebus.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.List;

@Builder(toBuilder = true)
@Getter
public class KycRequestValidationException extends RuntimeException {

    private List<FieldError> errors;

    public KycRequestValidationException(List<FieldError> errors) {
        super(errors.toString());
        this.errors = errors;
    }
}
