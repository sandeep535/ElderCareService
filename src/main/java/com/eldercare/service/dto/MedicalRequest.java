package com.eldercare.service.dto;

public record MedicalRequest(
        Long id,
        Long primaryPhysicianId,
        Long nurseId,
        String allergies,
        String medicalAlerts,
        String currentMedication,
        String bloodType
) {}
