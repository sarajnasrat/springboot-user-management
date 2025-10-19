package com.mcit.usermanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcit.usermanagement.modal.Permission;
import com.mcit.usermanagement.services.PermissionService;
import com.mcit.usermanagement.util.ApiResponseWrapper;
import com.mcit.usermanagement.util.PaginatedResponse;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    PermissionService permissionService;

    @GetMapping("/all")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    @GetMapping("/paginated")
    public ResponseEntity<PaginatedResponse<Permission>> getPaginatedPermissions(Pageable pageable) {
        return permissionService.getPaginatedPermissions(pageable);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponseWrapper<Permission>> createPermission(@RequestBody String permissionJson) {
        ApiResponseWrapper<Permission> response = permissionService.createPermission(permissionJson);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusCode()));
    }

}
