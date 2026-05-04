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

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "generic_name", length = 200)
    private String genericName;

    @Column(name = "strength", length = 100)
    private String strength;

    @Column(name = "form", length = 100)
    private String form;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
