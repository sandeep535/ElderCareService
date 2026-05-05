package com.eldercare.service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "clinical_note")
@Getter
@Setter
public class ClinicalNoteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_title", length = 200)
    private String noteTitle;

    @Column(name = "note_date")
    private LocalDateTime noteDate;

    @Column(name = "notes", columnDefinition = "TEXT", nullable = false)
    private String notes;

    @Column(name = "notes_type", length = 50, nullable = false)
    private String notesType;

    @Column(name = "priority", length = 50, nullable = false)
    private String priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private UserEntity recordedBy;
}
