package com.dockerlog.service.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 遵循阿里巴巴开发手册：自定义异常必须继承RuntimeException，提供明确的错误码和错误信息
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
