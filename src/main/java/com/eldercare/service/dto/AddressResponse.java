package com.eldercare.service.dto;

public record AddressResponse(
        Long id,
        String address,
        String city,
        String state,
        String zipCode,
        String type,
        Long typeId
) {}
