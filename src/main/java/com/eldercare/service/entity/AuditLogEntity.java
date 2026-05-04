package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "type_screen", nullable = false, length = 100)
    private String typeScreen;

    @Column(name = "data_json", nullable = false, columnDefinition = "LONGTEXT")
    private String dataJson;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "action_by", length = 100)
    private String actionBy;

    @PrePersist
    protected void onCreate() {
        createdOn = LocalDateTime.now();
    }
}
