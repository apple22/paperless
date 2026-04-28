package com.example.paperless.card.domain;

public enum ApplicationStatus {
  /*enum : 우리가 만든 상태값 정해진 값만 쓰게 하는 타입*/
    TEMPORARY_SAVED("임시저장"),
    TERMS_AGREED("약관동의완료"),
    SIGNED("전자서명완료"),
    SUBMITTED("최종제출"),
    UNDER_REVIEW("심사중"),
    APPROVED("승인"),
    REJECTED("거절"),
    CANCELED("취소");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getCode() {
        return name();
    }

    public String getDescription() {
        return description;
    }
}