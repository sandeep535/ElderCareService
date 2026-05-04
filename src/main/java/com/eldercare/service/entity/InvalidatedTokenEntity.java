package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "invalidated_token")
@Getter
@Setter
public class InvalidatedTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "invalidated_at", nullable = false)
    private LocalDateTime invalidatedAt;

    @PrePersist
    protected void onCreate() {
        invalidatedAt = LocalDateTime.now();
    }
}
