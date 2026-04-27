package com.example.paperless.card.dto;

import java.time.LocalDateTime;

public record CardApplicationSubmitResponse(
        String applicationId,
        String status,
        String statusDescription,
        LocalDateTime submittedAt
) {
}