package com.example.paperless.card.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "card_application")
public class CardApplication {

    @Id
    @Column(name = "application_id", nullable = false, length = 36)
    private String applicationId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "birth_date", nullable = false, length = 8)
    private String birthDate;

    @Column(name = "card_product_code", nullable = false, length = 50)
    private String cardProductCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ApplicationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "terms_agreed_at")
    private LocalDateTime termsAgreedAt;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    protected CardApplication() {
    }

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