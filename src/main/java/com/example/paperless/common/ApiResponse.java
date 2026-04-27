package com.example.paperless.common;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                "SUCCESS",
                "정상 처리되었습니다.",
                data
        );
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(
                false,
                code,
                message,
                null
        );
    }
}