package com.example.paperless.card.controller;

import com.example.paperless.card.dto.CardApplicationCreateRequest;
import com.example.paperless.card.repository.CardApplicationMemoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;


import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CardApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardApplicationMemoryRepository cardApplicationMemoryRepository;

    @BeforeEach
    void setUp() {
        cardApplicationMemoryRepository.clear();
    }

    @Test
    @DisplayName("카드 신청 생성 API를 호출하면 TEMPORARY_SAVED 상태로 응답한다")
    void create_success() throws Exception {
        // given
        CardApplicationCreateRequest request = createRequest();

        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/card-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("정상 처리되었습니다."))
                .andExpect(jsonPath("$.data.applicationId").isNotEmpty())
                .andExpect(jsonPath("$.data.customerName").value("홍길동"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.birthDate").value("19900101"))
                .andExpect(jsonPath("$.data.cardProductCode").value("HYUNDAI_CARD_M"))
                .andExpect(jsonPath("$.data.status").value("TEMPORARY_SAVED"))
                .andExpect(jsonPath("$.data.statusDescription").value("임시저장"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());

        assertThat(cardApplicationMemoryRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("카드 신청 생성 후 applicationId로 조회할 수 있다")
    void findById_success() throws Exception {
        // given
        String applicationId = createApplicationAndGetId();

        // when & then
        mockMvc.perform(get("/api/card-applications/{applicationId}", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.applicationId").value(applicationId))
                .andExpect(jsonPath("$.data.customerName").value("홍길동"))
                .andExpect(jsonPath("$.data.status").value("TEMPORARY_SAVED"))
                .andExpect(jsonPath("$.data.statusDescription").value("임시저장"))
                .andExpect(jsonPath("$.data.termsAgreedAt").doesNotExist())
                .andExpect(jsonPath("$.data.signedAt").doesNotExist())
                .andExpect(jsonPath("$.data.submittedAt").doesNotExist());
    }

    @Test
    @DisplayName("존재하지 않는 applicationId로 조회하면 BAD_REQUEST 응답을 반환한다")
    void findById_notFound_fail() throws Exception {
        // given
        String notFoundApplicationId = "not-found-id";

        // when & then
        mockMvc.perform(get("/api/card-applications/{applicationId}", notFoundApplicationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("카드 신청 정보를 찾을 수 없습니다. applicationId=not-found-id"));
    }

    @Test
    @DisplayName("약관 동의 API를 호출하면 상태가 TERMS_AGREED로 변경된다")
    void agreeTerms_success() throws Exception {
        // given
        String applicationId = createApplicationAndGetId();

        // when & then
        mockMvc.perform(post("/api/card-applications/{applicationId}/terms-agreement", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.applicationId").value(applicationId))
                .andExpect(jsonPath("$.data.status").value("TERMS_AGREED"))
                .andExpect(jsonPath("$.data.statusDescription").value("약관동의완료"))
                .andExpect(jsonPath("$.data.termsAgreedAt").isNotEmpty());

        mockMvc.perform(get("/api/card-applications/{applicationId}", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("TERMS_AGREED"))
                .andExpect(jsonPath("$.data.termsAgreedAt").isNotEmpty());
    }

    @Test
    @DisplayName("약관 동의 없이 전자서명 API를 호출하면 INVALID_STATE 응답을 반환한다")
    void sign_withoutTermsAgreement_fail() throws Exception {
        // given
        String applicationId = createApplicationAndGetId();

        // when & then
        mockMvc.perform(post("/api/card-applications/{applicationId}/signature", applicationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_STATE"))
                .andExpect(jsonPath("$.message").value("전자서명은 약관동의완료 상태에서만 가능합니다. currentStatus=TEMPORARY_SAVED"));
    }

    @Test
    @DisplayName("약관 동의 후 전자서명 API를 호출하면 상태가 SIGNED로 변경된다")
    void sign_success() throws Exception {
        // given
        String applicationId = createApplicationAndGetId();

        mockMvc.perform(post("/api/card-applications/{applicationId}/terms-agreement", applicationId))
                .andExpect(status().isOk());

        // when & then
        mockMvc.perform(post("/api/card-applications/{applicationId}/signature", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.applicationId").value(applicationId))
                .andExpect(jsonPath("$.data.status").value("SIGNED"))
                .andExpect(jsonPath("$.data.statusDescription").value("전자서명완료"))
                .andExpect(jsonPath("$.data.signedAt").isNotEmpty());

        mockMvc.perform(get("/api/card-applications/{applicationId}", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SIGNED"))
                .andExpect(jsonPath("$.data.termsAgreedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.signedAt").isNotEmpty());
    }

    @Test
    @DisplayName("전자서명 없이 최종 제출 API를 호출하면 INVALID_STATE 응답을 반환한다")
    void submit_withoutSignature_fail() throws Exception {
        // given
        String applicationId = createApplicationAndGetId();

        mockMvc.perform(post("/api/card-applications/{applicationId}/terms-agreement", applicationId))
                .andExpect(status().isOk());

        // when & then
        mockMvc.perform(post("/api/card-applications/{applicationId}/submit", applicationId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_STATE"))
                .andExpect(jsonPath("$.message").value("최종 제출은 전자서명완료 상태에서만 가능합니다. currentStatus=TERMS_AGREED"));
    }

    @Test
    @DisplayName("전자서명 후 최종 제출 API를 호출하면 상태가 SUBMITTED로 변경된다")
    void submit_success() throws Exception {
        // given
        String applicationId = createApplicationAndGetId();

        mockMvc.perform(post("/api/card-applications/{applicationId}/terms-agreement", applicationId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/card-applications/{applicationId}/signature", applicationId))
                .andExpect(status().isOk());

        // when & then
        mockMvc.perform(post("/api/card-applications/{applicationId}/submit", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.applicationId").value(applicationId))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.statusDescription").value("최종제출"))
                .andExpect(jsonPath("$.data.submittedAt").isNotEmpty());

        mockMvc.perform(get("/api/card-applications/{applicationId}", applicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.termsAgreedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.signedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.submittedAt").isNotEmpty());
    }

    @Test
    @DisplayName("잘못된 휴대폰 번호로 카드 신청 생성 API를 호출하면 VALIDATION_ERROR 응답을 반환한다")
    void create_invalidPhoneNumber_fail() throws Exception {
        // given
        CardApplicationCreateRequest request = new CardApplicationCreateRequest(
                "홍길동",
                "1234",
                "19900101",
                "HYUNDAI_CARD_M"
        );

        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/card-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("phoneNumber: 휴대폰 번호 형식이 올바르지 않습니다."));
    }

    private String createApplicationAndGetId() throws Exception {
        CardApplicationCreateRequest request = createRequest();
        String requestBody = objectMapper.writeValueAsString(request);

        String responseBody = mockMvc.perform(post("/api/card-applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        JsonNode jsonNode = objectMapper.readTree(responseBody);

        return jsonNode
                .path("data")
                .path("applicationId")
                .asText();
    }

    private CardApplicationCreateRequest createRequest() {
        return new CardApplicationCreateRequest(
                "홍길동",
                "010-1234-5678",
                "19900101",
                "HYUNDAI_CARD_M"
        );
    }
}