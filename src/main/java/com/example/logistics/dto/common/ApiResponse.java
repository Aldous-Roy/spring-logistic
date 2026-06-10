package com.example.logistics.dto.common;

public record ApiResponse<T>(
        String status,
        int statusCode,
        T data
) {
    public static <T> ApiResponse<T> success(T data, int statusCode) {
        return new ApiResponse<>("success", statusCode, data);
    }
}
