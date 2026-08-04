package com.plantcultivation.exception;

/**
 * 业务异常：携带面向用户的消息与 HTTP 状态码。
 * <p>由 GlobalExceptionHandler 统一转换为 ResultVO 响应，
 * 替代原先散落的 RuntimeException + 消息字符串约定。</p>
 */
public class BusinessException extends RuntimeException {

    private final int status;

    public BusinessException(String message) {
        this(message, 400);
    }

    public BusinessException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
