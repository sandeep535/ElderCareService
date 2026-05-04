package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "admission")
@Getter
@Setter
public class AdmissionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admission_date")
    private LocalDate admissionDate;

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Column(name = "bed", length = 20)
    private String bed;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "emr_contact_name", length = 150)
    private String emrContactName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private PatientEntity patient;
}
