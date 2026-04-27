package com.example.paperless.card.service;

import com.example.paperless.card.domain.ApplicationStatus;
import com.example.paperless.card.domain.CardApplication;
import com.example.paperless.card.dto.CardApplicationCreateRequest;
import com.example.paperless.card.dto.CardApplicationCreateResponse;
import com.example.paperless.card.dto.CardApplicationDetailResponse;
import com.example.paperless.card.dto.CardApplicationTermsAgreementResponse;
import com.example.paperless.card.repository.CardApplicationMemoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CardApplicationService {

    private final CardApplicationMemoryRepository cardApplicationMemoryRepository;

    public CardApplicationService(CardApplicationMemoryRepository cardApplicationMemoryRepository) {
        this.cardApplicationMemoryRepository = cardApplicationMemoryRepository;
    }

    public CardApplicationCreateResponse create(CardApplicationCreateRequest request) {
        ApplicationStatus status = ApplicationStatus.TEMPORARY_SAVED;

        CardApplication cardApplication = new CardApplication(
                UUID.randomUUID().toString(),
                request.customerName(),
                request.phoneNumber(),
                request.birthDate(),
                request.cardProductCode(),
                status,
                LocalDateTime.now()
        );

        CardApplication savedApplication = cardApplicationMemoryRepository.save(cardApplication);

        return toCreateResponse(savedApplication);
    }

    public CardApplicationDetailResponse findById(String applicationId) {
        CardApplication cardApplication = findApplicationOrThrow(applicationId);
        return toDetailResponse(cardApplication);
    }

    public CardApplicationTermsAgreementResponse agreeTerms(String applicationId) {
        CardApplication cardApplication = findApplicationOrThrow(applicationId);

        cardApplication.agreeTerms(LocalDateTime.now());

        CardApplication savedApplication = cardApplicationMemoryRepository.save(cardApplication);

        return new CardApplicationTermsAgreementResponse(
                savedApplication.getApplicationId(),
                savedApplication.getStatus().getCode(),
                savedApplication.getStatus().getDescription(),
                savedApplication.getTermsAgreedAt()
        );
    }

    private CardApplication findApplicationOrThrow(String applicationId) {
        return cardApplicationMemoryRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "카드 신청 정보를 찾을 수 없습니다. applicationId=" + applicationId
                ));
    }

    private CardApplicationCreateResponse toCreateResponse(CardApplication cardApplication) {
        return new CardApplicationCreateResponse(
                cardApplication.getApplicationId(),
                cardApplication.getCustomerName(),
                cardApplication.getPhoneNumber(),
                cardApplication.getBirthDate(),
                cardApplication.getCardProductCode(),
                cardApplication.getStatus().getCode(),
                cardApplication.getStatus().getDescription(),
                cardApplication.getCreatedAt()
        );
    }

    private CardApplicationDetailResponse toDetailResponse(CardApplication cardApplication) {
        return new CardApplicationDetailResponse(
                cardApplication.getApplicationId(),
                cardApplication.getCustomerName(),
                cardApplication.getPhoneNumber(),
                cardApplication.getBirthDate(),
                cardApplication.getCardProductCode(),
                cardApplication.getStatus().getCode(),
                cardApplication.getStatus().getDescription(),
                cardApplication.getCreatedAt(),
                cardApplication.getTermsAgreedAt()
        );
    }
}