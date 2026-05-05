package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "medical_history")
@Getter
@Setter
public class MedicalHistoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "surgery_name", length = 200, nullable = false)
    private String surgeryName;

    @Column(name = "surgery_date")
    private LocalDate surgeryDate;

    @Column(name = "surgery_type", length = 50)
    private String surgeryType;

    @Column(name = "surgeon", length = 200)
    private String surgeon;

    @Column(name = "hospital", length = 200)
    private String hospital;

    @Column(name = "procedure_code", length = 100)
    private String procedureCode;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;
}
