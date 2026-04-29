package com.example.paperless.card.dto;

import java.time.LocalDateTime;

public record CardApplicationSummaryResponse(
        String applicationId,
        String customerName,
        String phoneNumber,
        String cardProductCode,
        String status,
        String statusDescription,
        LocalDateTime createdAt
) {
}