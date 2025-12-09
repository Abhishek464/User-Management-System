package com.example.demo.Controller;

import com.example.demo.DTO.UserResponse;
import com.example.demo.Repository.UserRepository;
import com.example.demo.RoleEntity.User;
import com.example.demo.Security.CustomUserDetails;
import com.example.demo.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserResponse response = userService.toResponse(user);
        return ResponseEntity.ok(response);
    }
}
