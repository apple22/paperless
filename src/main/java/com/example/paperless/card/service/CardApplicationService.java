package com.example.paperless.card.service;

import com.example.paperless.card.domain.ApplicationStatus;
import com.example.paperless.card.domain.CardApplication;
import com.example.paperless.card.dto.CardApplicationCreateRequest;
import com.example.paperless.card.dto.CardApplicationCreateResponse;
import com.example.paperless.card.dto.CardApplicationDetailResponse;
import com.example.paperless.card.dto.CardApplicationSignatureResponse;
import com.example.paperless.card.dto.CardApplicationSubmitResponse;
import com.example.paperless.card.dto.CardApplicationTermsAgreementResponse;
import com.example.paperless.card.repository.CardApplicationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.paperless.card.dto.CardApplicationSummaryResponse;
import java.util.List;
import com.example.paperless.card.dto.CardApplicationSummaryResponse;
import com.example.paperless.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CardApplicationService {

    private final CardApplicationJpaRepository cardApplicationRepository;

    public CardApplicationService(CardApplicationJpaRepository cardApplicationRepository) {
        this.cardApplicationRepository = cardApplicationRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CardApplicationSummaryResponse> findAll(int page, int size) {
        validatePageRequest(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<CardApplication> cardApplicationPage = cardApplicationRepository.findAll(pageable);

        List<CardApplicationSummaryResponse> content = cardApplicationPage.getContent()
                .stream()
                .map(this::toSummaryResponse)
                .toList();

        return new PageResponse<>(
                content,
                cardApplicationPage.getNumber(),
                cardApplicationPage.getSize(),
                cardApplicationPage.getTotalElements(),
                cardApplicationPage.getTotalPages(),
                cardApplicationPage.isFirst(),
                cardApplicationPage.isLast()
        );
    }



    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page는 0 이상이어야 합니다.");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size는 1 이상 100 이하이어야 합니다.");
        }
    }

    @Transactional
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

        CardApplication savedApplication = cardApplicationRepository.save(cardApplication);

        return toCreateResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public CardApplicationDetailResponse findById(String applicationId) {
        CardApplication cardApplication = findApplicationOrThrow(applicationId);
        return toDetailResponse(cardApplication);
    }

    @Transactional
    public CardApplicationTermsAgreementResponse agreeTerms(String applicationId) {
        CardApplication cardApplication = findApplicationOrThrow(applicationId);

        cardApplication.agreeTerms(LocalDateTime.now());

        CardApplication savedApplication = cardApplicationRepository.save(cardApplication);

        return new CardApplicationTermsAgreementResponse(
                savedApplication.getApplicationId(),
                savedApplication.getStatus().getCode(),
                savedApplication.getStatus().getDescription(),
                savedApplication.getTermsAgreedAt()
        );
    }

    @Transactional
    public CardApplicationSignatureResponse sign(String applicationId) {
        CardApplication cardApplication = findApplicationOrThrow(applicationId);

        cardApplication.sign(LocalDateTime.now());

        CardApplication savedApplication = cardApplicationRepository.save(cardApplication);

        return new CardApplicationSignatureResponse(
                savedApplication.getApplicationId(),
                savedApplication.getStatus().getCode(),
                savedApplication.getStatus().getDescription(),
                savedApplication.getSignedAt()
        );
    }

    @Transactional
    public CardApplicationSubmitResponse submit(String applicationId) {
        CardApplication cardApplication = findApplicationOrThrow(applicationId);

        cardApplication.submit(LocalDateTime.now());

        CardApplication savedApplication = cardApplicationRepository.save(cardApplication);

        return new CardApplicationSubmitResponse(
                savedApplication.getApplicationId(),
                savedApplication.getStatus().getCode(),
                savedApplication.getStatus().getDescription(),
                savedApplication.getSubmittedAt()
        );
    }

    private CardApplicationSummaryResponse toSummaryResponse(CardApplication cardApplication) {
        return new CardApplicationSummaryResponse(
                cardApplication.getApplicationId(),
                cardApplication.getCustomerName(),
                cardApplication.getPhoneNumber(),
                cardApplication.getCardProductCode(),
                cardApplication.getStatus().getCode(),
                cardApplication.getStatus().getDescription(),
                cardApplication.getCreatedAt()
        );
    }
    private CardApplication findApplicationOrThrow(String applicationId) {
        return cardApplicationRepository.findById(applicationId)
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
                cardApplication.getTermsAgreedAt(),
                cardApplication.getSignedAt(),
                cardApplication.getSubmittedAt()
        );
    }
}