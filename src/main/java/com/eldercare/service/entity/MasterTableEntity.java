package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "master_table")
@Getter
@Setter
public class MasterTableEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lookup_value", length = 10)
    private String lookupValue;

    @Column(name = "lookup_item", nullable = false, length = 100)
    private String lookupItem;

    @Column(name = "lookup_code", nullable = false, length = 50)
    private String lookupCode;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
