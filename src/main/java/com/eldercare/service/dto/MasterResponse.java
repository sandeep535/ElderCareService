package com.eldercare.service.dto;

public record MasterResponse(
        Long id,
        String lookupValue,
        String lookupItem,
        String lookupCode,
        String type,
        boolean active
) {}
