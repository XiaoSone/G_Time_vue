package com.example.g_time.exception;

/**
 * 统一业务异常类
 */
public class UnifiedException extends RuntimeException {
    private final int code; // 错误码

    public UnifiedException(int code, String message) {
        super(message);
        this.code = code;
    }

    public UnifiedException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
