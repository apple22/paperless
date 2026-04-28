package com.example.paperless.card.service;

import com.example.paperless.card.dto.CardApplicationCreateRequest;
import com.example.paperless.card.dto.CardApplicationCreateResponse;
import com.example.paperless.card.dto.CardApplicationDetailResponse;
import com.example.paperless.card.dto.CardApplicationSignatureResponse;
import com.example.paperless.card.dto.CardApplicationSubmitResponse;
import com.example.paperless.card.dto.CardApplicationTermsAgreementResponse;
import com.example.paperless.card.repository.CardApplicationMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardApplicationServiceTest {

    private CardApplicationMemoryRepository cardApplicationMemoryRepository;
    private CardApplicationService cardApplicationService;

    @BeforeEach
    void setUp() {
        cardApplicationMemoryRepository = new CardApplicationMemoryRepository();
        cardApplicationService = new CardApplicationService(cardApplicationMemoryRepository);
    }

    @Test
    @DisplayName("카드 신청을 생성하면 TEMPORARY_SAVED 상태로 저장된다")
    void create_success() {
        // given
        CardApplicationCreateRequest request = createRequest();

        // when
        CardApplicationCreateResponse response = cardApplicationService.create(request);

        // then
        assertThat(response.applicationId()).isNotBlank();
        assertThat(response.customerName()).isEqualTo("홍길동");
        assertThat(response.phoneNumber()).isEqualTo("010-1234-5678");
        assertThat(response.birthDate()).isEqualTo("19900101");
        assertThat(response.cardProductCode()).isEqualTo("HYUNDAI_CARD_M");
        assertThat(response.status()).isEqualTo("TEMPORARY_SAVED");
        assertThat(response.statusDescription()).isEqualTo("임시저장");
        assertThat(response.createdAt()).isNotNull();

        assertThat(cardApplicationMemoryRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("생성한 카드 신청을 applicationId로 조회할 수 있다")
    void findById_success() {
        // given
        CardApplicationCreateResponse createResponse = cardApplicationService.create(createRequest());
        String applicationId = createResponse.applicationId();

        // when
        CardApplicationDetailResponse detailResponse = cardApplicationService.findById(applicationId);

        // then
        assertThat(detailResponse.applicationId()).isEqualTo(applicationId);
        assertThat(detailResponse.customerName()).isEqualTo("홍길동");
        assertThat(detailResponse.status()).isEqualTo("TEMPORARY_SAVED");
        assertThat(detailResponse.statusDescription()).isEqualTo("임시저장");
        assertThat(detailResponse.termsAgreedAt()).isNull();
        assertThat(detailResponse.signedAt()).isNull();
        assertThat(detailResponse.submittedAt()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 applicationId로 조회하면 예외가 발생한다")
    void findById_notFound_fail() {
        // given
        String notFoundApplicationId = "not-found-id";

        // when & then
        assertThatThrownBy(() -> cardApplicationService.findById(notFoundApplicationId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("카드 신청 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("약관 동의하면 상태가 TERMS_AGREED로 변경된다")
    void agreeTerms_success() {
        // given
        CardApplicationCreateResponse createResponse = cardApplicationService.create(createRequest());
        String applicationId = createResponse.applicationId();

        // when
        CardApplicationTermsAgreementResponse termsResponse =
                cardApplicationService.agreeTerms(applicationId);

        // then
        assertThat(termsResponse.applicationId()).isEqualTo(applicationId);
        assertThat(termsResponse.status()).isEqualTo("TERMS_AGREED");
        assertThat(termsResponse.statusDescription()).isEqualTo("약관동의완료");
        assertThat(termsResponse.termsAgreedAt()).isNotNull();

        CardApplicationDetailResponse detailResponse = cardApplicationService.findById(applicationId);
        assertThat(detailResponse.status()).isEqualTo("TERMS_AGREED");
        assertThat(detailResponse.termsAgreedAt()).isNotNull();
    }

    @Test
    @DisplayName("약관 동의 없이 전자서명하면 예외가 발생한다")
    void sign_withoutTermsAgreement_fail() {
        // given
        CardApplicationCreateResponse createResponse = cardApplicationService.create(createRequest());
        String applicationId = createResponse.applicationId();

        // when & then
        assertThatThrownBy(() -> cardApplicationService.sign(applicationId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("전자서명은 약관동의완료 상태에서만 가능합니다");
    }

    @Test
    @DisplayName("약관 동의 후 전자서명하면 상태가 SIGNED로 변경된다")
    void sign_success() {
        // given
        CardApplicationCreateResponse createResponse = cardApplicationService.create(createRequest());
        String applicationId = createResponse.applicationId();

        cardApplicationService.agreeTerms(applicationId);

        // when
        CardApplicationSignatureResponse signatureResponse =
                cardApplicationService.sign(applicationId);

        // then
        assertThat(signatureResponse.applicationId()).isEqualTo(applicationId);
        assertThat(signatureResponse.status()).isEqualTo("SIGNED");
        assertThat(signatureResponse.statusDescription()).isEqualTo("전자서명완료");
        assertThat(signatureResponse.signedAt()).isNotNull();

        CardApplicationDetailResponse detailResponse = cardApplicationService.findById(applicationId);
        assertThat(detailResponse.status()).isEqualTo("SIGNED");
        assertThat(detailResponse.termsAgreedAt()).isNotNull();
        assertThat(detailResponse.signedAt()).isNotNull();
        assertThat(detailResponse.submittedAt()).isNull();
    }

    @Test
    @DisplayName("전자서명 없이 최종 제출하면 예외가 발생한다")
    void submit_withoutSignature_fail() {
        // given
        CardApplicationCreateResponse createResponse = cardApplicationService.create(createRequest());
        String applicationId = createResponse.applicationId();

        cardApplicationService.agreeTerms(applicationId);

        // when & then
        assertThatThrownBy(() -> cardApplicationService.submit(applicationId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최종 제출은 전자서명완료 상태에서만 가능합니다");
    }

    @Test
    @DisplayName("전자서명 후 최종 제출하면 상태가 SUBMITTED로 변경된다")
    void submit_success() {
        // given
        CardApplicationCreateResponse createResponse = cardApplicationService.create(createRequest());
        String applicationId = createResponse.applicationId();

        cardApplicationService.agreeTerms(applicationId);
        cardApplicationService.sign(applicationId);

        // when
        CardApplicationSubmitResponse submitResponse =
                cardApplicationService.submit(applicationId);

        // then
        assertThat(submitResponse.applicationId()).isEqualTo(applicationId);
        assertThat(submitResponse.status()).isEqualTo("SUBMITTED");
        assertThat(submitResponse.statusDescription()).isEqualTo("최종제출");
        assertThat(submitResponse.submittedAt()).isNotNull();

        CardApplicationDetailResponse detailResponse = cardApplicationService.findById(applicationId);
        assertThat(detailResponse.status()).isEqualTo("SUBMITTED");
        assertThat(detailResponse.termsAgreedAt()).isNotNull();
        assertThat(detailResponse.signedAt()).isNotNull();
        assertThat(detailResponse.submittedAt()).isNotNull();
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