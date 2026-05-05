package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "diagnoses")
@Getter
@Setter
public class DiagnosisEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "diagnosis_name", length = 200, nullable = false)
    private String diagnosisName;

    @Column(name = "diagnosis_by", length = 150)
    private String diagnosisBy;

    @Column(name = "diagnosis_date")
    private LocalDate diagnosisDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_master_id")
    private DiagnosisMasterEntity diagnosisMaster;
}
