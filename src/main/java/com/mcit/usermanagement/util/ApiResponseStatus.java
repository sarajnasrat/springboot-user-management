package com.mcit.usermanagement.util;

import org.springframework.http.HttpStatus;
import jakarta.annotation.Nullable;

public enum ApiResponseStatus {

    INVALID(400, "Invalid"),
    ERROR(500, "Error"),
    CONTINUE(100, "Continue"),
    PROCESSING(102, "Processing"),
    CHECKPOINT(103, "Checkpoint"),
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    UNKNOWN_STATUS(501, "Unknown Status"),
    CONFLICT(409, "Conflict");

    private static final ApiResponseStatus[] VALUES = values();
    private final int value;
    private final String description;

    ApiResponseStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int value() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static ApiResponseStatus valueOf(int statusCode) {
        return resolve(statusCode);
    }

    @Nullable
    public static ApiResponseStatus resolve(int statusCode) {
        for (ApiResponseStatus status : VALUES) {
            if (status.value == statusCode) {
                return status;
            }
        }
        return UNKNOWN_STATUS;
    }

    // Convert ApiResponseStatus to Spring HttpStatus
    public HttpStatus getHttpStatus() {
        return HttpStatus.resolve(this.value) != null ? HttpStatus.resolve(this.value)
                : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
