package com.example.demo.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleRequest {
    @NotBlank
    private String name;
}