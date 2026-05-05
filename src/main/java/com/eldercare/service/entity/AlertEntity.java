package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "alert")
@Getter
@Setter
public class AlertEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_of_screen", length = 100, nullable = false)
    private String typeOfScreen;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "value", length = 100)
    private String value;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "priority", length = 20, nullable = false)
    private String priority;

    @Column(name = "resolved", nullable = false)
    private boolean resolved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;
}
