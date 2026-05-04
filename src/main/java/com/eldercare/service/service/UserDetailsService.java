package com.eldercare.service.service;

import com.eldercare.service.dto.UserDetailsRequest;
import com.eldercare.service.dto.UserDetailsResponse;
import com.eldercare.service.entity.UserDetailsEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.RoleAccessRepository;
import com.eldercare.service.repository.UserDetailsRepository;
import com.eldercare.service.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserDetailsService {

    private static final Logger log = LogManager.getLogger(UserDetailsService.class);

    private final UserDetailsRepository userDetailsRepository;
    private final UserRepository userRepository;
    private final RoleAccessRepository roleAccessRepository;

    public UserDetailsService(UserDetailsRepository userDetailsRepository,
                              UserRepository userRepository,
                              RoleAccessRepository roleAccessRepository) {
        this.userDetailsRepository = userDetailsRepository;
        this.userRepository = userRepository;
        this.roleAccessRepository = roleAccessRepository;
    }

    public List<UserDetailsResponse> getAll() {
        return userRepository.findAll().stream()
                .map(user -> {
                    var details = userDetailsRepository.findByUserId(user.getId()).orElse(null);
                    return buildResponse(user, details);
                }).toList();
    }

    public UserDetailsResponse getMe() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        var details = userDetailsRepository.findByUserId(user.getId()).orElse(null);
        return buildResponse(user, details);
    }

    @Transactional
    public UserDetailsResponse save(Long userId, UserDetailsRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(userId)
                .orElse(new UserDetailsEntity());

        userDetails.setUser(user);
        userDetails.setFirstName(request.firstName());
        userDetails.setLastName(request.lastName());
        userDetails.setGender(request.gender());
        userDetails.setDob(request.dob());
        userDetails.setDesignation(request.designation());
        userDetails.setEmail(request.email());
        userDetails.setPhoneNumber(request.phoneNumber());
        userDetails.setQualification(request.qualification());

        userDetailsRepository.save(userDetails);

        log.info("User details saved for user {} ({})", user.getUsername(), userId);
        return toResponse(userDetails);
    }

    public UserDetailsResponse getByUserId(Long userId) {
        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User details not found for user", userId));
        return toResponse(userDetails);
    }

    @Transactional
    public UserDetailsResponse update(Long userId, UserDetailsRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        UserDetailsEntity userDetails = userDetailsRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User details not found for user", userId));

        userDetails.setFirstName(request.firstName());
        userDetails.setLastName(request.lastName());
        userDetails.setGender(request.gender());
        userDetails.setDob(request.dob());
        userDetails.setDesignation(request.designation());
        userDetails.setEmail(request.email());
        userDetails.setPhoneNumber(request.phoneNumber());
        userDetails.setQualification(request.qualification());

        userDetailsRepository.save(userDetails);

        log.info("User details updated for user {} ({})", user.getUsername(), userId);
        return toResponse(userDetails);
    }

    private UserDetailsResponse toResponse(UserDetailsEntity userDetails) {
        return buildResponse(userDetails.getUser(), userDetails);
    }

    private UserDetailsResponse buildResponse(UserEntity user, UserDetailsEntity details) {
        String roleId = roleAccessRepository.findByUserId(user.getId())
                .stream().findFirst()
                .map(access -> access.getRole().getRoleId())
                .orElse(null);

        return new UserDetailsResponse(
                user.getId(),
                user.getUsername(),
                user.getUserType(),
                details != null ? details.getFirstName() : null,
                details != null ? details.getLastName() : null,
                details != null ? details.getGender() : null,
                details != null ? details.getDob() : null,
                details != null ? details.getDesignation() : null,
                details != null ? details.getEmail() : null,
                details != null ? details.getPhoneNumber() : null,
                details != null ? details.getQualification() : null,
                roleId
        );
    }
}
