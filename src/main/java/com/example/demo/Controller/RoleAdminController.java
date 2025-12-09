package com.example.demo.Controller;

import com.example.demo.DTO.RoleRequest;
import com.example.demo.Service.RoleService;
import com.example.demo.DTO.AdminStatusResponse;
import com.example.demo.DTO.AssignRoleRequest;
import com.example.demo.RoleEntity.User;
import com.example.demo.Repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class RoleAdminController {

    private final RoleService roleService;
    private final UserRepository userRepository;

    @PostMapping("/api/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleRequest request) {
        roleService.createRole(request.getName());
        return ResponseEntity.ok("Role created");
    }

    @PostMapping("/api/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRoles(@PathVariable Long userId,
                                         @Valid @RequestBody AssignRoleRequest request) {
        roleService.assignRolesToUser(userId, request.getRoleNames());
        return ResponseEntity.ok("Roles assigned to user");
    }

    @GetMapping("/api/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatusResponse> getAdminStats() {
        var users = userRepository.findAll();
        long totalUsers = users.size();
        Map<String, java.time.Instant> lastLogins = users.stream()
                .collect(Collectors.toMap(User::getEmail, User::getLastLoginAt));

        AdminStatusResponse response = AdminStatusResponse.builder()
                .totalUsers(totalUsers)
                .lastLogins(lastLogins)
                .build();

        return ResponseEntity.ok(response);
    }
}

