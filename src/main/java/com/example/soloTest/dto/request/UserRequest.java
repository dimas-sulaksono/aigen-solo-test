package com.example.soloTest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String role;
}

