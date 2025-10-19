package com.mcit.usermanagement.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsereDTO {
    Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;

    private boolean active = true;
    private String profileImage;

    private Set<String> roles;

}
