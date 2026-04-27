package com.example.paperless.card.dto;

import java.time.LocalDateTime;

public record CardApplicationSignatureResponse(
        String applicationId,
        String status,
        String statusDescription,
        LocalDateTime signedAt
) {
}