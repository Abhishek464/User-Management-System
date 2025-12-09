package com.example.demo.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class AdminStatusResponse {
    private long totalUsers;
    // Map<userEmail, lastLoginAt>
    private Map<String, Instant> lastLogins;
}