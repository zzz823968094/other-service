package com.dockerlog.service.common.result;

import com.dockerlog.service.common.constant.SystemConstant;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果封装
 * 遵循阿里巴巴开发手册：接口返回必须使用统一的Result封装格式
 *
 * @param <T> 数据类型
 * @author docker-log-service
 * @date 2026-05-15
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(SystemConstant.SUCCESS_CODE, SystemConstant.SUCCESS_MSG, null);
    }

    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SystemConstant.SUCCESS_CODE, SystemConstant.SUCCESS_MSG, data);
    }

    /**
     * 成功返回（自定义消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SystemConstant.SUCCESS_CODE, message, data);
    }

    /**
     * 失败返回
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(SystemConstant.ERROR_CODE, message, null);
    }

    /**
     * 失败返回（自定义状态码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 未授权返回
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(SystemConstant.UNAUTHORIZED_CODE, SystemConstant.UNAUTHORIZED_MSG, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return SystemConstant.SUCCESS_CODE.equals(this.code);
    }
}
