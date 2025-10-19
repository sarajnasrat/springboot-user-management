package com.mcit.usermanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcit.usermanagement.modal.Role;
import com.mcit.usermanagement.services.RoleService;
import com.mcit.usermanagement.util.ApiResponseWrapper;
import com.mcit.usermanagement.util.PaginatedResponse;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    RoleService roleService;

    @PostMapping("/create-role")
    public ResponseEntity<ApiResponseWrapper<Role>> createRole(@RequestBody String roleJson) {
        ApiResponseWrapper<Role> response = roleService.createRole(roleJson);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusCode()));
    }

    @PutMapping("/update-role/{id}")
    public ResponseEntity<ApiResponseWrapper<Role>> updateRole(@RequestBody String roleJson, @PathVariable Long id) {
        ApiResponseWrapper<Role> response = roleService.updateRole(roleJson, id);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusCode()));
    }

    @DeleteMapping("/delete-role/{id}")
    public ResponseEntity<ApiResponseWrapper<Role>> deleteRole(@PathVariable Long id) {
        ApiResponseWrapper<Role> response = roleService.deleteRole(id);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusCode()));
    }

    @GetMapping("/get-role/{id}")
    public ResponseEntity<ApiResponseWrapper<Role>> getRoleById(@PathVariable Long id) {
        ApiResponseWrapper<Role> response = roleService.getRoleById(id);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusCode()));
    }

    @GetMapping("/paginated-roles")
    public ResponseEntity<PaginatedResponse<Role>> getPaginatedRoles(Pageable pageable) {
        return roleService.getPaginatedRoles(pageable);
    }

    @GetMapping("/all-roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

}
