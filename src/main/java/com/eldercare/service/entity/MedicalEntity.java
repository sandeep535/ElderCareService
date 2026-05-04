package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medical")
@Getter
@Setter
public class MedicalEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_physician_id")
    private UserEntity primaryPhysician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id")
    private UserEntity nurse;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "medical_alerts", columnDefinition = "TEXT")
    private String medicalAlerts;

    @Column(name = "current_medication", columnDefinition = "TEXT")
    private String currentMedication;

    @Column(name = "blood_type", length = 20)
    private String bloodType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private PatientEntity patient;
}
