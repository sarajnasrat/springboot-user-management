package com.mcit.usermanagement.services;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mcit.usermanagement.modal.Permission;
import com.mcit.usermanagement.modal.Role;
import com.mcit.usermanagement.repository.PermissionRepository;
import com.mcit.usermanagement.repository.RoleRepository;
import com.mcit.usermanagement.util.ApiResponseStatus;
import com.mcit.usermanagement.util.ApiResponseWrapper;
import com.mcit.usermanagement.util.PaginatedResponse;

@Service
public class RoleService {
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PermissionRepository permissionRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public ApiResponseWrapper<Role> createRole(String roleJson) {
        try {
            Role roleFromJson = new ObjectMapper().readValue(roleJson, Role.class);

            // Validate role name
            if (roleFromJson.getName() == null || roleFromJson.getName().trim().isEmpty()) {
                return ApiResponseWrapper.error("Role name is required", ApiResponseStatus.BAD_REQUEST);
            }

            // Check for duplicate role name
            if (roleRepository.findByName(roleFromJson.getName()).isPresent()) {
                return ApiResponseWrapper.error("Role already exists", ApiResponseStatus.BAD_REQUEST);
            }

            Role newRole = new Role();
            newRole.setName(roleFromJson.getName().trim());
            newRole.setCreatedAt(LocalDateTime.now());

            // ✅ If permissions are provided, validate and attach them
            if (roleFromJson.getPermissions() != null && !roleFromJson.getPermissions().isEmpty()) {
                Set<Long> permissionIds = roleFromJson.getPermissions().stream()
                        .map(Permission::getId)
                        .collect(Collectors.toSet());

                // Fetch valid permissions from DB
                List<Permission> validPermissions = permissionRepository.findAllById(permissionIds);

                if (validPermissions.size() != permissionIds.size()) {
                    return ApiResponseWrapper.error(
                            "Some permission IDs are invalid or not found",
                            ApiResponseStatus.BAD_REQUEST);
                }

                newRole.setPermissions(new HashSet<>(validPermissions));
            }

            // ✅ Save role
            Role savedRole = roleRepository.save(newRole);
            return ApiResponseWrapper.success(savedRole, "Role created successfully");

        } catch (JsonParseException | InvalidFormatException e) {
            return ApiResponseWrapper.error("Invalid JSON format: " + e.getMessage(),
                    ApiResponseStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ApiResponseWrapper.error("Internal server error: " + e.getMessage(),
                    ApiResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseWrapper<Role> updateRole(String roleJson, Long id) {
        try {
            try {
                Role role = new ObjectMapper().readValue(roleJson, Role.class);
                if (role.getName() == null || role.getName().isEmpty()) {
                    return ApiResponseWrapper.error("Role name is required", ApiResponseStatus.BAD_REQUEST);
                }
                Optional<Role> existRole = roleRepository.findById(id);
                if (existRole.isPresent()) {
                    Role roleToUpdate = existRole.get();
                    roleToUpdate.setName(role.getName());
                    roleToUpdate.setUpdatedAt(LocalDateTime.now());
                    Role updatedRole = roleRepository.save(roleToUpdate);
                    return ApiResponseWrapper.success(updatedRole, "Role updated successfully");
                } else {
                    return ApiResponseWrapper.error("Role not found", ApiResponseStatus.NOT_FOUND);
                }
            } catch (JsonParseException | InvalidFormatException e) {
                return ApiResponseWrapper.error("Invalid JSON format: " + e.getMessage(),
                        ApiResponseStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            return ApiResponseWrapper.error("Internal server error: " + e.getMessage(), ApiResponseStatus.BAD_REQUEST);
        }

    }

    public ApiResponseWrapper<Role> deleteRole(Long id) {
        try {
            Optional<Role> existRole = roleRepository.findById(id);
            if (existRole.isPresent()) {
                roleRepository.deleteById(id);
                return ApiResponseWrapper.success(null, "Role deleted successfully");
            } else {
                return ApiResponseWrapper.error("Role not found", ApiResponseStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return ApiResponseWrapper.error("Internal server error: " + e.getMessage(), ApiResponseStatus.BAD_REQUEST);
        }
    }

    public ApiResponseWrapper<Role> getRoleById(Long id) {
        try {
            Optional<Role> existRole = roleRepository.findById(id);
            if (existRole.isPresent()) {
                return ApiResponseWrapper.success(existRole.get(), "Role fetched successfully");
            } else {
                return ApiResponseWrapper.error("Role not found", ApiResponseStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return ApiResponseWrapper.error("Internal server error: " + e.getMessage(), ApiResponseStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<PaginatedResponse<Role>> getPaginatedRoles(
            org.springframework.data.domain.Pageable pageable) {
        try {
            org.springframework.data.domain.Page<Role> page = roleRepository.findAll(pageable);
            return ResponseEntity.ok(PaginatedResponse.from(page, "Roles fetched successfully"));
        } catch (Exception e) {
            PaginatedResponse<Role> errorResponse = new PaginatedResponse<>();
            errorResponse.setStatus(ApiResponseStatus.INTERNAL_SERVER_ERROR);
            errorResponse.setSuccess(false);
            errorResponse.setErrors(List.of("Server error during fetching paginated users: " + e.getMessage()));
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
