package com.douyin.backend.common;

public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;
    private final String requestId;
    private final long timestamp;

    private ApiResponse(int code, String message, T data, String requestId, long timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data, RequestContext.getRequestId(), System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, RequestContext.getRequestId(), System.currentTimeMillis());
    }

    public static <T> ApiResponse<T> failure(int code, String message, T data) {
        return new ApiResponse<>(code, message, data, RequestContext.getRequestId(), System.currentTimeMillis());
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getRequestId() {
        return requestId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
