package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "patient_journey")
@Getter
@Setter
public class PatientJourneyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private PatientEntity patient;

    @Column(name = "basic_details")
    private boolean basicDetails = false;

    @Column(name = "is_medical")
    private boolean medical = false;

    @Column(name = "is_admission")
    private boolean admission = false;

    @Column(name = "is_note")
    private boolean note = false;
}
