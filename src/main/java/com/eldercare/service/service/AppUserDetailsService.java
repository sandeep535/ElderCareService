package com.eldercare.service.service;

import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.repository.RoleAccessRepository;
import com.eldercare.service.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private static final Logger log = LogManager.getLogger(AppUserDetailsService.class);

    private final UserRepository userRepository;
    private final RoleAccessRepository roleAccessRepository;

    public AppUserDetailsService(UserRepository userRepository, RoleAccessRepository roleAccessRepository) {
        this.userRepository = userRepository;
        this.roleAccessRepository = roleAccessRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        List<SimpleGrantedAuthority> authorities = roleAccessRepository.findByUserId(user.getId())
                .stream()
                .map(ra -> new SimpleGrantedAuthority(ra.getRole().getRoleId()))
                .toList();

        log.debug("Loaded user {} with roles {}", username, authorities);

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
