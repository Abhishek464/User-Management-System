package com.example.demo.Controller;

import com.example.demo.DTO.JwtResponse;
import com.example.demo.DTO.LoginRequest;
import com.example.demo.DTO.RegisterRequest;
import com.example.demo.EventPublishing.UserEventPublisher;
import com.example.demo.Repository.UserRepository;
import com.example.demo.RoleEntity.User;
import com.example.demo.Security.JwtTokenProvider;
import com.example.demo.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserEventPublisher eventPublisher;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(
                request.getUsername(), request.getEmail(), request.getPassword());

        eventPublisher.publishUserRegisteredEvent(user.getId(), user.getEmail());

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userService.updateLastLogin(user);
        eventPublisher.publishUserLoginEvent(user.getId(), user.getEmail());

        JwtResponse resp = new JwtResponse(
                token,
                "Bearer",
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet())
        );

        return ResponseEntity.ok(resp);
    }
}
