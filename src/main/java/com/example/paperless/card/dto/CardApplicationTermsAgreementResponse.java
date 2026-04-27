package com.example.paperless.card.dto;

import java.time.LocalDateTime;

public record CardApplicationTermsAgreementResponse(
        String applicationId,
        String status,
        String statusDescription,
        LocalDateTime termsAgreedAt
) {
}