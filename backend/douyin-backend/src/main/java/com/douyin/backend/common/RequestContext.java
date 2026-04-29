package com.douyin.backend.common;

public final class RequestContext {

    private static final ThreadLocal<String> REQUEST_ID_HOLDER = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID_HOLDER.set(requestId);
    }

    public static String getRequestId() {
        return REQUEST_ID_HOLDER.get();
    }

    public static void clear() {
        REQUEST_ID_HOLDER.remove();
    }
}
