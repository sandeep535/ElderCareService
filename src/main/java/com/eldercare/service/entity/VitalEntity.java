package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "vital")
@Getter
@Setter
public class VitalEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "systolic")
    private Integer systolic;

    @Column(name = "diastolic")
    private Integer diastolic;

    @Column(name = "hr")
    private Integer hr;

    @Column(name = "temp", precision = 4, scale = 1)
    private BigDecimal temp;

    @Column(name = "spo2")
    private Integer spo2;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "has_alert")
    private boolean hasAlert = false;

    @Column(name = "alert_resolved")
    private boolean alertResolved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;
}
