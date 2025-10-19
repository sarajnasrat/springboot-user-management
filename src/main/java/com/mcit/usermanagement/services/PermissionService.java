package com.mcit.usermanagement.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mcit.usermanagement.modal.Permission;
import com.mcit.usermanagement.repository.PermissionRepository;
import com.mcit.usermanagement.util.ApiResponseStatus;
import com.mcit.usermanagement.util.ApiResponseWrapper;
import com.mcit.usermanagement.util.PaginatedResponse;

@Service
public class PermissionService {

    @Autowired
    PermissionRepository permissionRepository;

    public ResponseEntity<List<Permission>> getAllPermissions() {
        try {
            List<Permission> permissions = permissionRepository.findAll();
            return new ResponseEntity<>(permissions, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<PaginatedResponse<Permission>> getPaginatedPermissions(
            org.springframework.data.domain.Pageable pageable) {
        try {
            org.springframework.data.domain.Page<Permission> page = permissionRepository.findAll(pageable);
            return ResponseEntity.ok(PaginatedResponse.from(page, "Permissions fetched successfully"));
        } catch (Exception e) {
            PaginatedResponse<Permission> errorResponse = new PaginatedResponse<>();
            errorResponse.setStatus(ApiResponseStatus.INTERNAL_SERVER_ERROR);
            errorResponse.setSuccess(false);
            errorResponse.setErrors(List.of("Server error during fetching paginated users: " + e.getMessage()));
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    public ApiResponseWrapper<Permission> createPermission(String permissionJson) {
        try {
            try {
                Permission permission = new ObjectMapper().readValue(permissionJson, Permission.class);

                Permission savedPermission = new Permission();
                savedPermission.setPermissionName(permission.getPermissionName());
                savedPermission.setCreatedAt(java.time.LocalDateTime.now());

                return ApiResponseWrapper.success(permissionRepository.save(savedPermission),
                        "Permission created successfully");

            } catch (JsonParseException | InvalidFormatException e) {
                return ApiResponseWrapper.error("Invalid JSON format: " + e.getMessage(),
                        ApiResponseStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            return ApiResponseWrapper.error("Internal server error: " + e.getMessage(), ApiResponseStatus.BAD_REQUEST);
        }

    }

}
