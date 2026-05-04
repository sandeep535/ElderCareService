package com.eldercare.service.service;

import com.eldercare.service.dto.LoginRequest;
import com.eldercare.service.dto.LoginResponse;
import com.eldercare.service.dto.SignupRequest;
import com.eldercare.service.entity.InvalidatedTokenEntity;
import com.eldercare.service.entity.RoleAccessEntity;
import com.eldercare.service.entity.UserDetailsEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.repository.InvalidatedTokenRepository;
import com.eldercare.service.repository.RoleRepository;
import com.eldercare.service.repository.UserDetailsRepository;
import com.eldercare.service.repository.UserRepository;
import com.eldercare.service.repository.RoleAccessRepository;
import com.eldercare.service.utils.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LogManager.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleAccessRepository roleAccessRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration.ms}")
    private long expirationMs;

    public AuthService(AuthenticationManager authenticationManager,
                       AppUserDetailsService userDetailsService,
                       JwtUtil jwtUtil,
                       InvalidatedTokenRepository invalidatedTokenRepository,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       RoleAccessRepository roleAccessRepository,
                       UserDetailsRepository userDetailsRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roleAccessRepository = roleAccessRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, String> signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ElderCareException("Username already exists: " + request.username());
        }

        String roleId = request.roleId().toUpperCase().startsWith("ROLE_")
                ? request.roleId().toUpperCase()
                : "ROLE_" + request.roleId().toUpperCase();

        var role = roleRepository.findByRoleId(roleId)
                .orElseThrow(() -> new ElderCareException("Role not found: " + roleId));

        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setUserType(request.userType());
        user.setCreatedBy("signup");
        userRepository.save(user);

        UserDetailsEntity details = new UserDetailsEntity();
        details.setUser(user);
        details.setFirstName(request.firstName());
        details.setLastName(request.lastName());
        details.setGender(request.gender());
        details.setDob(request.dob());
        details.setDesignation(request.designation());
        details.setEmail(request.email());
        details.setPhoneNumber(request.phoneNumber());
        details.setQualification(request.qualification());
        details.setCreatedBy("signup");
        userDetailsRepository.save(details);

        RoleAccessEntity roleAccess = new RoleAccessEntity();
        roleAccess.setUser(user);
        roleAccess.setRole(role);
        roleAccess.setCreatedBy("signup");
        roleAccessRepository.save(roleAccess);

        log.info("User {} signed up with role {}", request.username(), roleId);
        return Map.of("message", "User created successfully", "username", request.username(), "role", roleId);
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {}", request.username());
            throw new ElderCareException("Invalid username or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtUtil.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        log.info("User {} logged in successfully", request.username());
        return new LoginResponse(token, request.username(), roles, expirationMs);
    }

    public void logout(String token) {
        if (invalidatedTokenRepository.existsByToken(token)) {
            throw new ElderCareException("Token is already invalidated");
        }
        InvalidatedTokenEntity invalidated = new InvalidatedTokenEntity();
        invalidated.setToken(token);
        invalidatedTokenRepository.save(invalidated);
        log.info("Token invalidated for logout");
    }
}
