package com.example.paperless.card.controller;

import com.example.paperless.card.dto.CardApplicationCreateRequest;
import com.example.paperless.card.dto.CardApplicationCreateResponse;
import com.example.paperless.card.dto.CardApplicationDetailResponse;
import com.example.paperless.card.dto.CardApplicationTermsAgreementResponse;
import com.example.paperless.card.service.CardApplicationService;
import com.example.paperless.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/card-applications")
public class CardApplicationController {

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

    @PostMapping("/{applicationId}/terms-agreement")
    public ResponseEntity<ApiResponse<CardApplicationTermsAgreementResponse>> agreeTerms(
            @PathVariable String applicationId
    ) {
        CardApplicationTermsAgreementResponse response = cardApplicationService.agreeTerms(applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}