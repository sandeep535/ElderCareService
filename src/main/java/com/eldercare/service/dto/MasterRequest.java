package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

public record MasterRequest(
        @NotBlank(message = "Lookup value is required") String lookupValue,
        @NotBlank(message = "Lookup item is required") String lookupItem,
        @NotBlank(message = "Lookup code is required") String lookupCode,
        @NotBlank(message = "Type is required") String type,
        Boolean active
) {}
