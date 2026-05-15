package com.dockerlog.service.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录请求DTO
 * 遵循阿里巴巴开发手册：DTO用于接收前端参数，必须进行参数校验
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
