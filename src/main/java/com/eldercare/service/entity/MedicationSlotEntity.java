package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_slot")
@Getter
@Setter
public class MedicationSlotEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_medication_id", nullable = false)
    private PatientMedicationEntity patientMedication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "given_at")
    private LocalDateTime givenAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "given_by")
    private UserEntity givenBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
