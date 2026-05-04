package com.eldercare.service.dto;

public record MedicalResponse(
        Long id,
        Long primaryPhysicianId,
        String primaryPhysicianName,
        Long nurseId,
        String nurseName,
        String allergies,
        String medicalAlerts,
        String currentMedication,
        String bloodType,
        String bloodTypeDisplay,
        Long patientId
) {}
