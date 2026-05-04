package com.eldercare.service.dto;

public record MedicalRequest(
        String primaryPhysician,
        String nurse,
        String allergies,
        String medicalAlerts,
        String currentMedication,
        String bloodType
) {}
