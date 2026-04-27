package com.example.paperless.card.domain;

import java.time.LocalDateTime;

public class CardApplication {

    private final String applicationId;
    private final String customerName;
    private final String phoneNumber;
    private final String birthDate;
    private final String cardProductCode;
    private ApplicationStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime termsAgreedAt;
    private LocalDateTime signedAt;
    private LocalDateTime submittedAt;

    public CardApplication(
            String applicationId,
            String customerName,
            String phoneNumber,
            String birthDate,
            String cardProductCode,
            ApplicationStatus status,
            LocalDateTime createdAt
    ) {
        this.applicationId = applicationId;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.cardProductCode = cardProductCode;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void agreeTerms(LocalDateTime agreedAt) {
        if (this.status != ApplicationStatus.TEMPORARY_SAVED) {
            throw new IllegalStateException(
                    "약관 동의는 임시저장 상태에서만 가능합니다. currentStatus=" + this.status.getCode()
            );
        }

        this.status = ApplicationStatus.TERMS_AGREED;
        this.termsAgreedAt = agreedAt;
    }

    public void sign(LocalDateTime signedAt) {
        if (this.status != ApplicationStatus.TERMS_AGREED) {
            throw new IllegalStateException(
                    "전자서명은 약관동의완료 상태에서만 가능합니다. currentStatus=" + this.status.getCode()
            );
        }

        this.status = ApplicationStatus.SIGNED;
        this.signedAt = signedAt;
    }

    public void submit(LocalDateTime submittedAt) {
        if (this.status != ApplicationStatus.SIGNED) {
            throw new IllegalStateException(
                    "최종 제출은 전자서명완료 상태에서만 가능합니다. currentStatus=" + this.status.getCode()
            );
        }

        this.status = ApplicationStatus.SUBMITTED;
        this.submittedAt = submittedAt;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getCardProductCode() {
        return cardProductCode;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getTermsAgreedAt() {
        return termsAgreedAt;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}