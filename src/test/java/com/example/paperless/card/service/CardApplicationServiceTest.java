package com.example.paperless.card.service;

import com.example.paperless.card.dto.CardApplicationCreateRequest;
import com.example.paperless.card.dto.CardApplicationCreateResponse;
import com.example.paperless.card.dto.CardApplicationDetailResponse;
import com.example.paperless.card.dto.CardApplicationSignatureResponse;
import com.example.paperless.card.dto.CardApplicationSubmitResponse;
import com.example.paperless.card.dto.CardApplicationTermsAgreementResponse;
import com.example.paperless.card.repository.CardApplicationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import com.example.paperless.card.dto.CardApplicationSummaryResponse;
import com.example.paperless.common.PageResponse;
import com.example.paperless.card.dto.CardApplicationSummaryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CardApplicationServiceTest {

    @Autowired
    private CardApplicationService cardApplicationService;

    @Autowired
    private CardApplicationJpaRepository cardApplicationJpaRepository;

    @BeforeEach
    void setUp() {
        cardApplicationJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("카드 신청 목록을 페이징으로 조회할 수 있다")
    void findAll_paging_success() {
        // given
        cardApplicationService.create(createRequest());

        CardApplicationCreateRequest secondRequest = new CardApplicationCreateRequest(
                "김철수",
                "010-2222-3333",
                "19950202",
                "HYUNDAI_CARD_ZERO"
        );

        cardApplicationService.create(secondRequest);

        // when
        PageResponse<CardApplicationSummaryResponse> response = cardApplicationService.findAll(0, 10);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }

    @Test
    @DisplayName("page가 0보다 작으면 예외가 발생한다")
    void findAll_negativePage_fail() {
        // when & then
        assertThatThrownBy(() -> cardApplicationService.findAll(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("size가 1보다 작으면 예외가 발생한다")
    void findAll_invalidSmallSize_fail() {
        // when & then
        assertThatThrownBy(() -> cardApplicationService.findAll(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size는 1 이상 100 이하이어야 합니다.");
    }

    @Test
    @DisplayName("size가 100보다 크면 예외가 발생한다")
    void findAll_invalidLargeSize_fail() {
        // when & then
        assertThatThrownBy(() -> cardApplicationService.findAll(0, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size는 1 이상 100 이하이어야 합니다.");
    }
    @Test
    @DisplayName("카드 신청 목록을 최신 생성순으로 페이징 조회할 수 있다")
    void findAll_success() {
        // given
        CardApplicationCreateResponse firstResponse = cardApplicationService.create(createRequest());

        CardApplicationCreateRequest secondRequest = new CardApplicationCreateRequest(
                "김철수",
                "010-2222-3333",
                "19950202",
                "HYUNDAI_CARD_ZERO"
        );

        CardApplicationCreateResponse secondResponse = cardApplicationService.create(secondRequest);

        // when
        PageResponse<CardApplicationSummaryResponse> response = cardApplicationService.findAll(0, 10);
        List<CardApplicationSummaryResponse> responses = response.content();

        // then
        assertThat(responses).hasSize(2);

        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();

        assertThat(responses)
                .extracting(CardApplicationSummaryResponse::applicationId)
                .containsExactly(
                        secondResponse.applicationId(),
                        firstResponse.applicationId()
                );

        assertThat(responses.get(0).customerName()).isEqualTo("김철수");
        assertThat(responses.get(1).customerName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("카드 신청을 생성하면 DB에 TEMPORARY_SAVED 상태로 저장된다")
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

        assertThat(cardApplicationJpaRepository.count()).isEqualTo(1);
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
        assertThat(detailResponse.phoneNumber()).isEqualTo("010-1234-5678");
        assertThat(detailResponse.birthDate()).isEqualTo("19900101");
        assertThat(detailResponse.cardProductCode()).isEqualTo("HYUNDAI_CARD_M");
        assertThat(detailResponse.status()).isEqualTo("TEMPORARY_SAVED");
        assertThat(detailResponse.statusDescription()).isEqualTo("임시저장");
        assertThat(detailResponse.createdAt()).isNotNull();
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
    @DisplayName("약관 동의하면 상태가 TERMS_AGREED로 변경되고 DB에 반영된다")
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
        assertThat(detailResponse.statusDescription()).isEqualTo("약관동의완료");
        assertThat(detailResponse.termsAgreedAt()).isNotNull();
        assertThat(detailResponse.signedAt()).isNull();
        assertThat(detailResponse.submittedAt()).isNull();
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
    @DisplayName("약관 동의 후 전자서명하면 상태가 SIGNED로 변경되고 DB에 반영된다")
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
        assertThat(detailResponse.statusDescription()).isEqualTo("전자서명완료");
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
    @DisplayName("전자서명 후 최종 제출하면 상태가 SUBMITTED로 변경되고 DB에 반영된다")
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
        assertThat(detailResponse.statusDescription()).isEqualTo("최종제출");
        assertThat(detailResponse.termsAgreedAt()).isNotNull();
        assertThat(detailResponse.signedAt()).isNotNull();
        assertThat(detailResponse.submittedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 약관 동의한 신청을 다시 약관 동의하면 예외가 발생한다")
    void agreeTerms_alreadyAgreed_fail() {
        // given
        CardApplicationCreateResponse createResponse = cardApplicationService.create(createRequest());
        String applicationId = createResponse.applicationId();

        cardApplicationService.agreeTerms(applicationId);

        // when & then
        assertThatThrownBy(() -> cardApplicationService.agreeTerms(applicationId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("약관 동의는 임시저장 상태에서만 가능합니다");
    }

    @Test
    @DisplayName("이미 최종 제출된 신청을 다시 제출하면 예외가 발생한다")
    void submit_alreadySubmitted_fail() {
        // given
        CardApplicationCreateResponse createResponse = cardApplicationService.create(createRequest());
        String applicationId = createResponse.applicationId();

        cardApplicationService.agreeTerms(applicationId);
        cardApplicationService.sign(applicationId);
        cardApplicationService.submit(applicationId);

        // when & then
        assertThatThrownBy(() -> cardApplicationService.submit(applicationId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최종 제출은 전자서명완료 상태에서만 가능합니다");
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