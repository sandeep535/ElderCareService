package com.eldercare.service.dto;

import java.util.List;

public record PagedAlertResponse(
        List<AlertResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {}
