package com.mcit.usermanagement.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationError {

    private String field; // Field name that failed validation
    private String message; // Validation error message
    private String code; // Optional error code (like "NotNull", "Size")

    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }

    /**
     * Convert BindingResult errors into a list of ValidationError objects.
     */
    public static List<ValidationError> fromBindingResult(BindingResult bindingResult) {
        List<ValidationError> validationErrors = new ArrayList<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            validationErrors.add(new ValidationError(
                    error.getField(),
                    error.getDefaultMessage(),
                    error.getCode()));
        }
        return validationErrors;
    }
}
