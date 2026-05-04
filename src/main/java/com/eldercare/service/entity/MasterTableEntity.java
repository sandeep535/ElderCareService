package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "master_table")
@Getter
@Setter
public class MasterTableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lookup_value", length = 50, nullable = false)
    private String lookupValue;

    @Column(name = "lookup_item", length = 150, nullable = false)
    private String lookupItem;

    @Column(name = "lookup_code", length = 100, nullable = false)
    private String lookupCode;

    @Column(name = "type", length = 100, nullable = false)
    private String type;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
