package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "medication_master")
@Getter
@Setter
public class MedicationMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drug_name", length = 200, nullable = false)
    private String drugName;

    @Column(name = "generic_name", length = 200)
    private String genericName;

    @Column(name = "default_strength", length = 100)
    private String defaultStrength;

    @Column(name = "default_strength_unit", length = 50)
    private String defaultStrengthUnit;

    @Column(name = "default_dose_form", length = 100)
    private String defaultDoseForm;

    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
