package com.example.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private Set<String> roles;
}
