package com.example.demo.Service;

import com.example.demo.Repository.RoleRepository;
import com.example.demo.RoleEntity.Role;
import com.example.demo.RoleEntity.User;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public Role createRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(Role.builder().name(name).build()));
    }

    public User assignRolesToUser(Long userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Role> roles = new HashSet<>();
        for (String rn : roleNames) {
            Role r = roleRepository.findByName(rn)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + rn));
            roles.add(r);
        }
        user.getRoles().addAll(roles);
        return userRepository.save(user);
    }
}
