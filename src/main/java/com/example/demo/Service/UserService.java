package com.example.demo.Service;

import com.example.demo.DTO.UserResponse;
import com.example.demo.RoleEntity.Role;
import com.example.demo.RoleEntity.User;
import com.example.demo.Repository.RoleRepository;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String username, String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already in use");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default role USER not found"));

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .build();
        user.getRoles().add(userRole);

        return userRepository.save(user);
    }

    public void updateLastLogin(User user) {
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    @Cacheable("currentUser")
    @Transactional(readOnly = true)
    public UserResponse toResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return resp;
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
