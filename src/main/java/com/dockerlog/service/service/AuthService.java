package com.dockerlog.service.service;

import com.dockerlog.service.dto.LoginDTO;
import com.dockerlog.service.vo.LoginVO;

/**
 * 认证服务接口
 * 遵循阿里巴巴开发手册：Service层定义业务接口，Impl层实现具体逻辑
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求参数
     * @return 登录响应
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 验证用户名密码
     *
     * @param username 用户名
     * @param password 密码
     * @return true-验证通过，false-验证失败
     */
    boolean validateCredentials(String username, String password);
}
