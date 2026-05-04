package com.eldercare.service.repository;

import com.eldercare.service.entity.UserDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailsRepository extends JpaRepository<UserDetailsEntity, Long> {

    Optional<UserDetailsEntity> findByUserId(Long userId);
}
