package com.example.paperless.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CardApplicationCreateRequest(
        @NotBlank(message = "고객명은 필수입니다.")
        String customerName,

        @NotBlank(message = "휴대폰 번호는 필수입니다.")
        @Pattern(
                regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$",
                message = "휴대폰 번호 형식이 올바르지 않습니다."
        )
        String phoneNumber,

        @NotBlank(message = "생년월일은 필수입니다.")
        @Pattern(
                regexp = "^\\d{8}$",
                message = "생년월일은 yyyyMMdd 형식이어야 합니다."
        )
        String birthDate,

        @NotBlank(message = "카드 상품 코드는 필수입니다.")
        String cardProductCode

) {
}
