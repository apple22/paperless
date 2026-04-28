package com.example.paperless.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/*DTO는 Data Transfer Object : 데이터를 주고받기 위한 객체
* 보통 : DTO는 보통 Controller에서 요청/응답
* Controller는 외부 요청을 받는 입구
* 이 Controller는 직접 상태를 바꾸지 않습니다. Service에게 일을 맡깁니다.
* Service는 실제 업무 로직을 처리하는 계층
* Repository는 데이터 저장소에 접근하는 계층
* */

/*record는 데이터를 담기 위한 간단한 클래스*/
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
