package com.eldercare.service.service;

import com.eldercare.service.dto.UserRequest;
import com.eldercare.service.dto.UserResponse;
import com.eldercare.service.entity.RoleAccessEntity;
import com.eldercare.service.entity.RoleEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.RoleAccessRepository;
import com.eldercare.service.repository.RoleRepository;
import com.eldercare.service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleAccessRepository roleAccessRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RoleAccessRepository roleAccessRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roleAccessRepository = roleAccessRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ElderCareException("Username already exists: " + request.username());
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ElderCareException("Password is required for user creation");
        }

        RoleEntity role = loadRole(request.roleId());

        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setUserType(request.userType());
        user.setActive(request.active() == null || request.active());
        userRepository.save(user);

        RoleAccessEntity access = new RoleAccessEntity();
        access.setUser(user);
        access.setRole(role);
        access.setCreatedBy("admin");
        roleAccessRepository.save(access);

        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new ElderCareException("Username already exists: " + request.username());
        }

        user.setUsername(request.username());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        user.setUserType(request.userType());
        user.setActive(request.active() == null || request.active());
        userRepository.save(user);

        RoleEntity role = loadRole(request.roleId());
        roleAccessRepository.findByUserId(user.getId()).forEach(roleAccessRepository::delete);

        RoleAccessEntity access = new RoleAccessEntity();
        access.setUser(user);
        access.setRole(role);
        access.setCreatedBy("admin");
        roleAccessRepository.save(access);

        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(false);
        userRepository.save(user);
    }

    private RoleEntity loadRole(String roleId) {
        String normalized = roleId.toUpperCase().startsWith("ROLE_") ? roleId.toUpperCase() : "ROLE_" + roleId.toUpperCase();
        return roleRepository.findByRoleId(normalized)
                .orElseThrow(() -> new ElderCareException("Role not found: " + roleId));
    }

    private UserResponse toResponse(UserEntity user) {
        List<String> roles = roleAccessRepository.findByUserId(user.getId()).stream()
                .map(access -> access.getRole().getRoleId())
                .toList();

        return new UserResponse(user.getId(), user.getUsername(), user.getUserType(),
                user.isActive(), roles, user.getCreatedOn(), user.getUpdatedOn());
    }
}
