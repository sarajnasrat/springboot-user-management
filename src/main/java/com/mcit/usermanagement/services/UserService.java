package com.mcit.usermanagement.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mcit.usermanagement.config.JWTGenerator;
import com.mcit.usermanagement.dto.AuthenticationResponseDTO;
import com.mcit.usermanagement.dto.LoginDTO;
import com.mcit.usermanagement.dto.RoleDto;
import com.mcit.usermanagement.dto.UsereDTO;
import com.mcit.usermanagement.modal.Role;
import com.mcit.usermanagement.modal.User;
import com.mcit.usermanagement.repository.RoleRepository;
import com.mcit.usermanagement.repository.UserRepository;
import com.mcit.usermanagement.util.ApiResponseStatus;
import com.mcit.usermanagement.util.ApiResponseWrapper;
import com.mcit.usermanagement.util.PaginatedResponse;

import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.java.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTGenerator jwtGenerator;

    public ApiResponseWrapper<User> registerUser(String userJson) {
        UsereDTO userDTO;

        // 1️⃣ Parse JSON inside service
        try {
            userDTO = new ObjectMapper().readValue(userJson, UsereDTO.class);
        } catch (JsonParseException | InvalidFormatException e) {
            return ApiResponseWrapper.error(
                    "Invalid JSON format: " + e.getOriginalMessage(),
                    ApiResponseStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ApiResponseWrapper.error(
                    "Server error during JSON parsing: " + e.getMessage(),
                    ApiResponseStatus.INTERNAL_SERVER_ERROR);
        }

        // 2️⃣ Validate DTO
        if (userDTO == null) {
            return ApiResponseWrapper.error(
                    "Request body is empty or invalid",
                    ApiResponseStatus.BAD_REQUEST);
        }

        try {
            // 3️⃣ Validate required fields
            ApiResponseWrapper<User> validationResponse = validateUserDTO(userDTO, userRepository);
            if (validationResponse != null) {
                return validationResponse; // return first validation error
            }

            // 4️⃣ Convert DTO to entity
            User user = new User();
            user.setFirstName(userDTO.getFirstName().trim());
            user.setLastName(userDTO.getLastName().trim());
            user.setEmail(userDTO.getEmail().trim().toLowerCase());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setActive(userDTO.isActive());
            user.setCreatedAt(LocalDateTime.now());

            // 5️⃣ Handle roles
            if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
                return ApiResponseWrapper.error(
                        "At least one role must be provided",
                        ApiResponseStatus.BAD_REQUEST);
            }

            // ✅ Find roles by name from DB
            Set<Role> roleEntities = new HashSet<>();
            for (String roleName : userDTO.getRoles()) {
                Role role = roleRepository.findByName(roleName.trim())
                        .orElse(null);
                if (role == null) {
                    // ❌ Return error if a role doesn’t exist
                    return ApiResponseWrapper.error(
                            "Role not found: " + roleName,
                            ApiResponseStatus.BAD_REQUEST);
                }
                roleEntities.add(role);
            }

            // ✅ Set found roles to user
            user.setRoles(roleEntities);

            // 6️⃣ Save user
            User savedUser = userRepository.save(user);

            // 7️⃣ Return success
            return ApiResponseWrapper.success(savedUser, "User registered successfully with roles");

        } catch (Exception e) {
            return ApiResponseWrapper.error(
                    "Server error during user registration: " + e.getMessage(),
                    ApiResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ApiResponseWrapper<User> updateUser(String userJson, Long id) {
        UsereDTO usereDTO;
        try {
            usereDTO = new ObjectMapper().readValue(userJson, UsereDTO.class);
        } catch (Exception e) {
            return ApiResponseWrapper.error("Invalid JSON format: " + e.getMessage(),
                    ApiResponseStatus.BAD_REQUEST);
        }

        if (usereDTO == null) {
            return ApiResponseWrapper.error("Request body is empty or invalid", ApiResponseStatus.BAD_REQUEST);
        }

        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (optionalUser.isEmpty()) {
                return ApiResponseWrapper.error("User not found", ApiResponseStatus.NOT_FOUND);
            }

            User existingUser = optionalUser.get();

            // 🔹 Check if email is being changed and belongs to someone else
            if (!existingUser.getEmail().equalsIgnoreCase(usereDTO.getEmail())) {
                Optional<User> userWithSameEmail = userRepository.findByEmail(usereDTO.getEmail().trim().toLowerCase());
                if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
                    return ApiResponseWrapper.error(
                            "Email already in use by another user: " + usereDTO.getEmail(),
                            ApiResponseStatus.CONFLICT);
                }
            }

            // 🔹 Update fields
            existingUser.setFirstName(usereDTO.getFirstName().trim());
            existingUser.setLastName(usereDTO.getLastName().trim());
            existingUser.setEmail(usereDTO.getEmail().trim().toLowerCase());

            User updatedUser = userRepository.save(existingUser);
            return ApiResponseWrapper.success(updatedUser, "User updated successfully");

        } catch (Exception e) {
            return ApiResponseWrapper.error("Server error during user update: " + e.getMessage(),
                    ApiResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method inside service
    private ApiResponseWrapper<User> validateUserDTO(UsereDTO userDTO, UserRepository userRepository) {
        if (userDTO.getFirstName() == null || userDTO.getFirstName().isEmpty()) {
            return ApiResponseWrapper.error("firstName field is required", ApiResponseStatus.BAD_REQUEST);
        }
        if (userDTO.getLastName() == null || userDTO.getLastName().isEmpty()) {
            return ApiResponseWrapper.error("lastName field is required", ApiResponseStatus.BAD_REQUEST);
        }
        if (userDTO.getEmail() == null || userDTO.getEmail().isEmpty()) {
            return ApiResponseWrapper.error("email field is required", ApiResponseStatus.BAD_REQUEST);
        }
        if (!userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ApiResponseWrapper.error("Invalid email format", ApiResponseStatus.BAD_REQUEST);
        }
        if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
            return ApiResponseWrapper.error("password field is required", ApiResponseStatus.BAD_REQUEST);
        }
        if (userDTO.getPassword().length() < 6) {
            return ApiResponseWrapper.error("Password must be at least 6 characters long",
                    ApiResponseStatus.BAD_REQUEST);
        }

        // ✅ Check if email already exists
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            return ApiResponseWrapper.error("Email is already in use", ApiResponseStatus.CONFLICT);
        }

        return null; // ✅ no validation errors
    }

    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<PaginatedResponse<User>> getPaginatedResponse(Pageable pageable) {
        try {
            Page<User> userPage = userRepository.findAll(pageable);

            PaginatedResponse<User> paginatedResponse = PaginatedResponse.from(
                    userPage,
                    "Users retrieved successfully");

            return ResponseEntity.ok(paginatedResponse);
        } catch (Exception e) {
            PaginatedResponse<User> errorResponse = new PaginatedResponse<>();
            errorResponse.setStatus(ApiResponseStatus.INTERNAL_SERVER_ERROR);
            errorResponse.setSuccess(false);
            errorResponse.setErrors(List.of("Server error during fetching paginated users: " + e.getMessage()));
            return ResponseEntity.status(500).body(errorResponse);
        }

    }

    public ResponseEntity<ApiResponseWrapper<AuthenticationResponseDTO>> authenticateUser(LoginDTO request,
            HttpServletResponse response) {
        try {
            // Check if username exists
            Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
            User user = optionalUser.get();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseWrapper.error("User Not Found! : " + request.getEmail(),
                                ApiResponseStatus.NOT_FOUND));
            }

            if (!user.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponseWrapper.error("User disabled by admin. Please contact admin to enable user",
                                ApiResponseStatus.FORBIDDEN));
            }

            // Attempt authentication
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
                System.out.println("Authorities: " + authentication.getAuthorities());

                // Set security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Generate tokens
                String accessToken = jwtGenerator.generateAccessToken(authentication);
                String refreshToken = jwtGenerator.generateRefreshToken(authentication);

                // Validate token generation
                if (accessToken == null || accessToken.isEmpty() || refreshToken == null || refreshToken.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ApiResponseWrapper.error("Token generation failed", ApiResponseStatus.UNAUTHORIZED));
                }

                // Store refresh token in HTTP-only secure cookie
                ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                        .httpOnly(true)
                        .secure(false) // because no HTTPS yet
                        .sameSite("Lax") // allows sending cookie in most cases
                        .path("/")
                        .maxAge(7 * 24 * 60 * 60)
                        .build();

                response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

                // Fetch roles for the user
                Set<Role> roles = user.getRoles();
                List<RoleDto> roleDtos = roles.stream()
                        .map(role -> {
                            RoleDto dto = new RoleDto();
                            dto.setId(role.getId()); // assuming getId() exists in Role
                            dto.setName(role.getName()); // assuming getName() exists in Role
                            return dto;
                        })
                        .collect(Collectors.toList());
                // Prepare response DTO
                AuthenticationResponseDTO authResponse = new AuthenticationResponseDTO();
                authResponse.setId(String.valueOf(user.getId()));
                authResponse.setFirstName(user.getFirstName());
                authResponse.setLastName(user.getLastName());
                authResponse.setEmail(user.getEmail());
                authResponse.setRoles(roleDtos);
                authResponse.setAccessToken(accessToken);
                authResponse.setRefreshToken(refreshToken);
                authResponse.setActive(user.isActive());
                authResponse.setProfileImage(user.getProfileImage());

                // Return successful response
                return ResponseEntity.ok(ApiResponseWrapper.success(authResponse, "Login successful"));

            } catch (BadCredentialsException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponseWrapper.error("Invalid Credential", ApiResponseStatus.UNAUTHORIZED));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseWrapper.error(e.getMessage(), ApiResponseStatus.INTERNAL_SERVER_ERROR));
        }
    }

}
