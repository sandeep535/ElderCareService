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

    // Body Composition Fields
    @Column(name = "height", precision = 5, scale = 2)
    private BigDecimal height;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "bmi", precision = 4, scale = 2)
    private BigDecimal bmi;

    @Column(name = "body_fat_percentage", precision = 5, scale = 2)
    private BigDecimal bodyFatPercentage;

    @Column(name = "body_fat_mass", precision = 5, scale = 2)
    private BigDecimal bodyFatMass;

    @Column(name = "skeletal_muscle_percentage", precision = 5, scale = 2)
    private BigDecimal skeletalMusclePercentage;

    @Column(name = "body_water_percentage", precision = 5, scale = 2)
    private BigDecimal bodyWaterPercentage;

    @Column(name = "total_moisture", precision = 5, scale = 2)
    private BigDecimal totalMoisture;

    @Column(name = "extracellular_water_pct", precision = 5, scale = 2)
    private BigDecimal extracellularWaterPct;

    @Column(name = "intracellular_water_pct", precision = 5, scale = 2)
    private BigDecimal intracellularWaterPct;

    @Column(name = "basal_metabolism")
    private Integer basalMetabolism;

    @Column(name = "visceral_fat_level", precision = 4, scale = 1)
    private BigDecimal visceralFatLevel;

    @Column(name = "protein", precision = 5, scale = 2)
    private BigDecimal protein;

    @Column(name = "mineral", precision = 5, scale = 2)
    private BigDecimal mineral;

    @Column(name = "body_age")
    private Integer bodyAge;

    @Column(name = "overall")
    private Integer overall;

    // Clinical Vitals Fields
    @Column(name = "temperature", precision = 4, scale = 1)
    private BigDecimal temperature;

    @Column(name = "systolic")
    private Integer systolic;

    @Column(name = "diastolic")
    private Integer diastolic;

    @Column(name = "bp_heart_rate")
    private Integer bpHeartRate;

    @Column(name = "spo2")
    private Integer spo2;

    @Column(name = "spo2_heart_rate")
    private Integer spo2HeartRate;

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
