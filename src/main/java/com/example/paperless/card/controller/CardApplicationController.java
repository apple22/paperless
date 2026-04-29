package com.example.paperless.card.controller;

import com.example.paperless.card.dto.CardApplicationCreateRequest;
import com.example.paperless.card.dto.CardApplicationCreateResponse;
import com.example.paperless.card.dto.CardApplicationDetailResponse;
import com.example.paperless.card.dto.CardApplicationSignatureResponse;
import com.example.paperless.card.dto.CardApplicationSubmitResponse;
import com.example.paperless.card.dto.CardApplicationTermsAgreementResponse;
import com.example.paperless.card.service.CardApplicationService;
import com.example.paperless.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import com.example.paperless.card.dto.CardApplicationSummaryResponse;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.example.paperless.card.dto.CardApplicationSummaryResponse;
import com.example.paperless.common.PageResponse;

/*
이 어노테이션이 붙으면 Spring이 객체를 직접 만들어서 관리합니다.
이렇게 Spring이 관리하는 객체를 Bean이라고 합니다.
 */
@RestController
@RequestMapping("/api/card-applications")
public class CardApplicationController {

    /*Dependency Injection, 필요한 객체를 직접 만들지 않고 Spring이 넣어주는 방식*/
    /* 어노테이션은 Spring이나 Java에게 힌트를 주는 표시 */
    private final CardApplicationService cardApplicationService;

    public CardApplicationController(CardApplicationService cardApplicationService) {
        this.cardApplicationService = cardApplicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CardApplicationCreateResponse>> create(
            @Valid @RequestBody CardApplicationCreateRequest request
    ) {
        CardApplicationCreateResponse response = cardApplicationService.create(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<CardApplicationDetailResponse>> findById(
            @PathVariable String applicationId
    ) {
        CardApplicationDetailResponse response = cardApplicationService.findById(applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CardApplicationSummaryResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<CardApplicationSummaryResponse> response = cardApplicationService.findAll(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{applicationId}/terms-agreement")
    public ResponseEntity<ApiResponse<CardApplicationTermsAgreementResponse>> agreeTerms(
            @PathVariable String applicationId
    ) {
        CardApplicationTermsAgreementResponse response = cardApplicationService.agreeTerms(applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{applicationId}/signature")
    public ResponseEntity<ApiResponse<CardApplicationSignatureResponse>> sign(
            @PathVariable String applicationId
    ) {
        CardApplicationSignatureResponse response = cardApplicationService.sign(applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{applicationId}/submit")
    public ResponseEntity<ApiResponse<CardApplicationSubmitResponse>> submit(
            @PathVariable String applicationId
    ) {
        CardApplicationSubmitResponse response = cardApplicationService.submit(applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}