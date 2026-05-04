package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "diagnosis_master")
@Getter
@Setter
public class DiagnosisMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "diagnosis_name", length = 200, nullable = false)
    private String diagnosisName;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
