package com.mcit.usermanagement.dto;

import java.util.List;

import lombok.Data;

@Data
public class AuthenticationResponseDTO {
    private String Id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean active;
    private String profileImage;
    private String accessToken;
    private String refreshToken;
    private List<RoleDto> roles;

}
