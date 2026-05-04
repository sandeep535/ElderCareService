package com.eldercare.service.controller;

import com.eldercare.service.dto.UserDetailsRequest;
import com.eldercare.service.dto.UserDetailsResponse;
import com.eldercare.service.service.UserDetailsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserDetailsController {

    private final UserDetailsService userDetailsService;

    public UserDetailsController(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDetailsResponse>> getAll() {
        return ResponseEntity.ok(userDetailsService.getAll());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDetailsResponse> getMe() {
        return ResponseEntity.ok(userDetailsService.getMe());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailsResponse> getById(@PathVariable Long userId) {
        return ResponseEntity.ok(userDetailsService.getByUserId(userId));
    }

    @PostMapping("/{userId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailsResponse> save(@PathVariable Long userId,
                                                    @Valid @RequestBody UserDetailsRequest request) {
        return ResponseEntity.ok(userDetailsService.save(userId, request));
    }

    @PutMapping("/{userId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDetailsResponse> update(@PathVariable Long userId,
                                                      @Valid @RequestBody UserDetailsRequest request) {
        return ResponseEntity.ok(userDetailsService.update(userId, request));
    }
}
