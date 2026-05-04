package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequest(
        String address,
        String city,
        String state,
        String zipCode,
        @NotBlank(message = "Type is required") String type,
        @NotNull(message = "Type ID is required") Long typeId
) {}
