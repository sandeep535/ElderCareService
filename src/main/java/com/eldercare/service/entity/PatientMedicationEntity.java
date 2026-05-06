package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_medication")
@Getter
@Setter
public class PatientMedicationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private MedicationMasterEntity medication;

    @Column(name = "rx_norm", length = 50)
    private String rxNorm;

    @Column(name = "order_priority", length = 50)
    private String orderPriority;

    @Column(name = "indication", length = 300)
    private String indication;

    @Column(name = "strength_value", length = 50)
    private String strengthValue;

    @Column(name = "strength_unit", length = 50)
    private String strengthUnit;

    @Column(name = "dose_form", length = 100)
    private String doseForm;

    @Column(name = "dose_amount", length = 100)
    private String doseAmount;

    @Column(name = "route", length = 50)
    private String route;

    @Column(name = "frequency", length = 50)
    private String frequency;

    @Column(name = "prn_reason", length = 300)
    private String prnReason;

    @Column(name = "prn_max_dose", length = 100)
    private String prnMaxDose;

    @Column(name = "iv_rate", length = 50)
    private String ivRate;

    @Column(name = "iv_rate_unit", length = 50)
    private String ivRateUnit;

    @Column(name = "iv_volume", length = 50)
    private String ivVolume;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "stop_date_time")
    private LocalDateTime stopDateTime;

    @Column(name = "duration", length = 100)
    private String duration;

    @Column(name = "ordering_provider", length = 200)
    private String orderingProvider;

    @Column(name = "sig", columnDefinition = "TEXT")
    private String sig;

    @Column(name = "admin_instructions", columnDefinition = "TEXT")
    private String adminInstructions;

    @Column(name = "pharmacy_comments", columnDefinition = "TEXT")
    private String pharmacyComments;

    @Column(name = "ack_allergies_reviewed")
    private boolean ackAllergiesReviewed = false;

    @Column(name = "ack_dupe_reviewed")
    private boolean ackDupeReviewed = false;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
