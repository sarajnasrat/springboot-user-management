package com.mcit.usermanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcit.usermanagement.dto.AuthenticationResponseDTO;
import com.mcit.usermanagement.dto.LoginDTO;
import com.mcit.usermanagement.modal.User;
import com.mcit.usermanagement.services.UserService;
import com.mcit.usermanagement.util.ApiResponseWrapper;
import com.mcit.usermanagement.util.PaginatedResponse;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponseWrapper<User>> registerUser(@RequestBody String userJon) {
        ApiResponseWrapper<User> respnse = userService.registerUser(userJon);
        return new ResponseEntity<>(respnse, HttpStatus.valueOf(respnse.getStatusCode()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseWrapper<AuthenticationResponseDTO>> login(@RequestBody LoginDTO authresponse,
            HttpServletResponse response) {
        return userService.authenticateUser(authresponse, response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponseWrapper<User>> updateUser(@RequestBody String userJson, @PathVariable Long id) {
        ApiResponseWrapper<User> response = userService.updateUser(userJson, id);
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getStatusCode()));
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/pagenated-users")
    public ResponseEntity<PaginatedResponse<User>> getPaginatedUsers(Pageable pageable) {
        ResponseEntity<PaginatedResponse<User>> paginatedUsersResponse = userService.getPaginatedResponse(pageable);
        return paginatedUsersResponse;
    }
}
