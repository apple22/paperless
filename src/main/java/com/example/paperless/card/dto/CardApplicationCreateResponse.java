package com.example.paperless.card.dto;

import java.time.LocalDateTime;

public record CardApplicationCreateResponse(
        String applicationId,
        String customerName,
        String phoneNumber,
        String birthDate,
        String cardProductCode,
        String status,
        String statusDescription,
        LocalDateTime createdAt
) {
}