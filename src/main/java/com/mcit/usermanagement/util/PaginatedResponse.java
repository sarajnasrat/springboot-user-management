package com.mcit.usermanagement.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponse<T> {
    // The actual data returned (list of items like users, roles, etc.)
    private List<T> data;

    // Status of the response (e.g., OK, ERROR, BAD_REQUEST) using ApiResponseStatus
    // enum
    private ApiResponseStatus status;

    // Indicates whether the request was successful (true/false)
    private boolean isSuccess;

    // Message to describe the success case (e.g., "Users retrieved successfully")
    private String successMessage;

    // List of general error messages (for failures not tied to validation)
    private List<String> errors;

    // List of validation errors (field-specific errors captured from @Valid)
    private List<ValidationError> validationErrors;

    // Timestamp when the response was created (for tracking/logging)
    private LocalDateTime timestamp = LocalDateTime.now();

    // ---- Pagination fields ----

    // Current page number (0-based index, e.g., 0 = first page)
    private int page;

    // Number of items per page
    private int size;

    // Total number of records available in the database
    private long totalElements;

    // Total number of pages (calculated from totalElements / size)
    private int totalPages;

    // Flag to check if this page is the last one (true/false)
    private boolean last;

    public static <T> PaginatedResponse<T> from(Page<T> page, String message) {
        PaginatedResponse<T> response = new PaginatedResponse<>();
        response.data = page.getContent();
        response.status = ApiResponseStatus.OK;
        response.isSuccess = true;
        response.successMessage = message;
        response.page = page.getNumber();
        response.size = page.getSize();
        response.totalElements = page.getTotalElements();
        response.totalPages = page.getTotalPages();
        response.last = page.isLast();
        return response;
    }
}
