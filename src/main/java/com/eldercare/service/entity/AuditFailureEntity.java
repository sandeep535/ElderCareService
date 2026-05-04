package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_failure")
@Getter
@Setter
public class AuditFailureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "type_screen", length = 100)
    private String typeScreen;

    @Column(name = "data_json", columnDefinition = "LONGTEXT")
    private String dataJson;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "resolved")
    private boolean resolved = false;

    @PrePersist
    protected void onCreate() {
        createdOn = LocalDateTime.now();
    }
}
