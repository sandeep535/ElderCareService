package com.eldercare.service.dto;

public record MedicalResponse(
        Long id,
        String primaryPhysician,
        String nurse,
        String allergies,
        String medicalAlerts,
        String currentMedication,
        String bloodType,
        Long patientId
) {}
