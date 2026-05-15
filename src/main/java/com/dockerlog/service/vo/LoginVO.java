package com.dockerlog.service.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应VO
 * 遵循阿里巴巴开发手册：VO用于向前端返回数据
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Token
     */
    private String token;

    /**
     * 用户名
     */
    private String username;
}
