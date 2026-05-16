package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "vital_metric_master")
@Getter
@Setter
public class VitalMetricMasterEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Device ID from the hardware device (1–22)
    @Column(name = "device_id", nullable = false, unique = true)
    private Integer deviceId;

    // Internal field key used in code (e.g. "systolic", "height")
    @Column(name = "field_key", nullable = false, unique = true, length = 100)
    private String fieldKey;

    // Human-readable label shown on UI
    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    // Unit of measurement (e.g. "mmHg", "kg", "%", "N.A.")
    @Column(name = "unit", length = 50)
    private String unit;

    // Category grouping (e.g. "BODY_COMPOSITION", "BLOOD_PRESSURE", "TEMPERATURE")
    @Column(name = "category", length = 100)
    private String category;

    // Whether this field is mandatory when capturing vitals
    @Column(name = "mandatory", nullable = false)
    private boolean mandatory = false;

    // Whether this metric should be shown on the vitals display screen
    @Column(name = "display", nullable = false)
    private boolean display = true;

    // Normal range — low threshold (alert if value goes below this)
    @Column(name = "low_value", precision = 10, scale = 2)
    private BigDecimal lowValue;

    // Normal range — high threshold (alert if value goes above this)
    @Column(name = "high_value", precision = 10, scale = 2)
    private BigDecimal highValue;

    // Normal range label shown on UI (e.g. "90 - 139")
    @Column(name = "normal_range", length = 100)
    private String normalRange;

    // Sort order for display on UI
    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
