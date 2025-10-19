package com.mcit.usermanagement.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import br.com.fluentvalidator.context.Error;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseWrapper<T> {

    private T data;

    private ApiResponseStatus status = ApiResponseStatus.OK;

    private int statusCode = ApiResponseStatus.OK.value();

    private boolean isSuccess = true;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String successMessage = "";

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> errors = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ValidationError> validationErrors = new ArrayList<>();

    private LocalDateTime timestamp = LocalDateTime.now();

    private void setIsSuccess() {
        this.isSuccess = status == ApiResponseStatus.OK;
        this.statusCode = status.value(); // sync with status
    }

    // Constructors
    protected ApiResponseWrapper() {
    }

    public ApiResponseWrapper(T data) {
        this.data = data;
    }

    protected ApiResponseWrapper(T data, String successMessage) {
        this(data);
        this.successMessage = successMessage;
        setIsSuccess();
    }

    protected ApiResponseWrapper(ApiResponseStatus status) {
        this.status = status;
        setIsSuccess();
    }

    // Static helper methods

    public static <T> ApiResponseWrapper<T> success(T data) {
        return new ApiResponseWrapper<>(data);
    }

    public static <T> ApiResponseWrapper<T> success(T data, String successMessage) {
        return new ApiResponseWrapper<>(data, successMessage);
    }

    // Default error (500)
    public static <T> ApiResponseWrapper<T> error(String errorMessage) {
        return error(errorMessage, ApiResponseStatus.INTERNAL_SERVER_ERROR);
    }

    // Error with custom status
    public static <T> ApiResponseWrapper<T> error(String errorMessage, ApiResponseStatus status) {
        List<String> errors = new ArrayList<>();
        errors.add(errorMessage);
        ApiResponseWrapper<T> response = new ApiResponseWrapper<>(status);
        response.setErrors(errors);
        return response;
    }

    // Error with multiple messages
    public static <T> ApiResponseWrapper<T> error(List<String> errors, ApiResponseStatus status) {
        ApiResponseWrapper<T> response = new ApiResponseWrapper<>(status);
        response.setErrors(errors);
        return response;
    }

    public static <T> ApiResponseWrapper<T> invalid(List<ValidationError> validationErrors) {
        ApiResponseWrapper<T> response = new ApiResponseWrapper<>(ApiResponseStatus.BAD_REQUEST);
        response.setValidationErrors(validationErrors);
        return response;
    }

    public static <T> ApiResponseWrapper<T> invalid(Collection<Error> errors) {
        List<ValidationError> validationErrors = new ArrayList<>();
        for (Error error : errors) {
            validationErrors.add(new ValidationError(error.getField(), error.getMessage(), error.getCode()));
        }
        return invalid(validationErrors);
    }

    public static <T> ApiResponseWrapper<T> notFound(String errorMessage) {
        return error(errorMessage, ApiResponseStatus.NOT_FOUND);
    }

    public static <T> ApiResponseWrapper<T> unauthorized() {
        return new ApiResponseWrapper<>(ApiResponseStatus.UNAUTHORIZED);
    }

    public static <T> ApiResponseWrapper<T> forbidden() {
        return new ApiResponseWrapper<>(ApiResponseStatus.FORBIDDEN);
    }

    // Getters and setters

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ApiResponseStatus getStatus() {
        return status;
    }

    public ApiResponseWrapper<T> setStatus(ApiResponseStatus status) {
        this.status = status;
        setIsSuccess();
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
