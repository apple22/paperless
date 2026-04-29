package com.example.paperless.card.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardApplicationTest {

    @Test
    @DisplayName("카드 신청 생성 시 임시저장 상태로 생성된다")
    void create_temporarySavedApplication_success() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 27, 9, 0);

        // when
        CardApplication cardApplication = createTemporarySavedApplication(createdAt);

        // then
        assertThat(cardApplication.getApplicationId()).isEqualTo("application-001");
        assertThat(cardApplication.getCustomerName()).isEqualTo("홍길동");
        assertThat(cardApplication.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(cardApplication.getBirthDate()).isEqualTo("19900101");
        assertThat(cardApplication.getCardProductCode()).isEqualTo("HYUNDAI_CARD_M");
        assertThat(cardApplication.getStatus()).isEqualTo(ApplicationStatus.TEMPORARY_SAVED);
        assertThat(cardApplication.getCreatedAt()).isEqualTo(createdAt);
        assertThat(cardApplication.getTermsAgreedAt()).isNull();
        assertThat(cardApplication.getSignedAt()).isNull();
        assertThat(cardApplication.getSubmittedAt()).isNull();
    }

    @Test
    @DisplayName("임시저장 상태에서 약관 동의하면 상태가 TERMS_AGREED로 변경된다")
    void agreeTerms_success() {
        // given
        CardApplication cardApplication = createTemporarySavedApplication();
        LocalDateTime agreedAt = LocalDateTime.of(2026, 4, 27, 10, 0);

        // when
        cardApplication.agreeTerms(agreedAt);

        // then
        assertThat(cardApplication.getStatus()).isEqualTo(ApplicationStatus.TERMS_AGREED);
        assertThat(cardApplication.getTermsAgreedAt()).isEqualTo(agreedAt);
        assertThat(cardApplication.getSignedAt()).isNull();
        assertThat(cardApplication.getSubmittedAt()).isNull();
    }

    @Test
    @DisplayName("이미 약관동의완료 상태에서 다시 약관 동의하면 예외가 발생한다")
    void agreeTerms_alreadyAgreed_fail() {
        // given
        CardApplication cardApplication = createTemporarySavedApplication();
        cardApplication.agreeTerms(LocalDateTime.of(2026, 4, 27, 10, 0));

        // when & then
        assertThatThrownBy(() -> cardApplication.agreeTerms(LocalDateTime.of(2026, 4, 27, 10, 5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("약관 동의는 임시저장 상태에서만 가능합니다")
                .hasMessageContaining("currentStatus=TERMS_AGREED");
    }

    @Test
    @DisplayName("약관 동의 없이 전자서명하면 예외가 발생한다")
    void sign_withoutTermsAgreement_fail() {
        // given
        CardApplication cardApplication = createTemporarySavedApplication();

        // when & then
        assertThatThrownBy(() -> cardApplication.sign(LocalDateTime.of(2026, 4, 27, 10, 5)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("전자서명은 약관동의완료 상태에서만 가능합니다")
                .hasMessageContaining("currentStatus=TEMPORARY_SAVED");

        assertThat(cardApplication.getStatus()).isEqualTo(ApplicationStatus.TEMPORARY_SAVED);
        assertThat(cardApplication.getSignedAt()).isNull();
    }

    @Test
    @DisplayName("약관동의완료 상태에서 전자서명하면 상태가 SIGNED로 변경된다")
    void sign_success() {
        // given
        CardApplication cardApplication = createTemporarySavedApplication();

        LocalDateTime agreedAt = LocalDateTime.of(2026, 4, 27, 10, 0);
        LocalDateTime signedAt = LocalDateTime.of(2026, 4, 27, 10, 5);

        cardApplication.agreeTerms(agreedAt);

        // when
        cardApplication.sign(signedAt);

        // then
        assertThat(cardApplication.getStatus()).isEqualTo(ApplicationStatus.SIGNED);
        assertThat(cardApplication.getTermsAgreedAt()).isEqualTo(agreedAt);
        assertThat(cardApplication.getSignedAt()).isEqualTo(signedAt);
        assertThat(cardApplication.getSubmittedAt()).isNull();
    }

    @Test
    @DisplayName("전자서명완료 상태에서 다시 전자서명하면 예외가 발생한다")
    void sign_alreadySigned_fail() {
        // given
        CardApplication cardApplication = createTemporarySavedApplication();

        cardApplication.agreeTerms(LocalDateTime.of(2026, 4, 27, 10, 0));
        cardApplication.sign(LocalDateTime.of(2026, 4, 27, 10, 5));

        // when & then
        assertThatThrownBy(() -> cardApplication.sign(LocalDateTime.of(2026, 4, 27, 10, 10)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("전자서명은 약관동의완료 상태에서만 가능합니다")
                .hasMessageContaining("currentStatus=SIGNED");
    }

    @Test
    @DisplayName("전자서명 없이 최종 제출하면 예외가 발생한다")
    void submit_withoutSignature_fail() {
        // given
        CardApplication cardApplication = createTemporarySavedApplication();
        cardApplication.agreeTerms(LocalDateTime.of(2026, 4, 27, 10, 0));

        // when & then
        assertThatThrownBy(() -> cardApplication.submit(LocalDateTime.of(2026, 4, 27, 10, 10)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최종 제출은 전자서명완료 상태에서만 가능합니다")
                .hasMessageContaining("currentStatus=TERMS_AGREED");

        assertThat(cardApplication.getStatus()).isEqualTo(ApplicationStatus.TERMS_AGREED);
        assertThat(cardApplication.getSubmittedAt()).isNull();
    }

    @Test
    @DisplayName("전자서명완료 상태에서 최종 제출하면 상태가 SUBMITTED로 변경된다")
    void submit_success() {
        // given
        CardApplication cardApplication = createTemporarySavedApplication();

        LocalDateTime agreedAt = LocalDateTime.of(2026, 4, 27, 10, 0);
        LocalDateTime signedAt = LocalDateTime.of(2026, 4, 27, 10, 5);
        LocalDateTime submittedAt = LocalDateTime.of(2026, 4, 27, 10, 10);

        cardApplication.agreeTerms(agreedAt);
        cardApplication.sign(signedAt);

        // when
        cardApplication.submit(submittedAt);

        // then
        assertThat(cardApplication.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(cardApplication.getTermsAgreedAt()).isEqualTo(agreedAt);
        assertThat(cardApplication.getSignedAt()).isEqualTo(signedAt);
        assertThat(cardApplication.getSubmittedAt()).isEqualTo(submittedAt);
    }

    @Test
    @DisplayName("이미 최종제출 상태에서 다시 제출하면 예외가 발생한다")
    void submit_alreadySubmitted_fail() {
        // given
        CardApplication cardApplication = createTemporarySavedApplication();

        cardApplication.agreeTerms(LocalDateTime.of(2026, 4, 27, 10, 0));
        cardApplication.sign(LocalDateTime.of(2026, 4, 27, 10, 5));
        cardApplication.submit(LocalDateTime.of(2026, 4, 27, 10, 10));

        // when & then
        assertThatThrownBy(() -> cardApplication.submit(LocalDateTime.of(2026, 4, 27, 10, 15)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("최종 제출은 전자서명완료 상태에서만 가능합니다")
                .hasMessageContaining("currentStatus=SUBMITTED");
    }

    @Test
    @DisplayName("정상 흐름으로 약관동의, 전자서명, 최종제출까지 진행할 수 있다")
    void fullProcess_success() {
        // given
        CardApplication cardApplication = createTemporarySavedApplication();

        LocalDateTime agreedAt = LocalDateTime.of(2026, 4, 27, 10, 0);
        LocalDateTime signedAt = LocalDateTime.of(2026, 4, 27, 10, 5);
        LocalDateTime submittedAt = LocalDateTime.of(2026, 4, 27, 10, 10);

        // when
        cardApplication.agreeTerms(agreedAt);
        cardApplication.sign(signedAt);
        cardApplication.submit(submittedAt);

        // then
        assertThat(cardApplication.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(cardApplication.getTermsAgreedAt()).isEqualTo(agreedAt);
        assertThat(cardApplication.getSignedAt()).isEqualTo(signedAt);
        assertThat(cardApplication.getSubmittedAt()).isEqualTo(submittedAt);
    }

    private CardApplication createTemporarySavedApplication() {
        return createTemporarySavedApplication(LocalDateTime.of(2026, 4, 27, 9, 0));
    }

    private CardApplication createTemporarySavedApplication(LocalDateTime createdAt) {
        return new CardApplication(
                "application-001",
                "홍길동",
                "010-1234-5678",
                "19900101",
                "HYUNDAI_CARD_M",
                ApplicationStatus.TEMPORARY_SAVED,
                createdAt
        );
    }
}