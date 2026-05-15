package com.dockerlog.service.service.impl;

import cn.hutool.core.util.StrUtil;
import com.dockerlog.service.common.constant.SystemConstant;
import com.dockerlog.service.common.exception.BusinessException;
import com.dockerlog.service.dto.LoginDTO;
import com.dockerlog.service.service.AuthService;
import com.dockerlog.service.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 认证服务实现类
 * 遵循阿里巴巴开发手册：ServiceImpl必须实现Service接口，使用@Service注解
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求参数
     * @return 登录响应
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        log.info("用户登录尝试: username={}", loginDTO.getUsername());
        
        // 验证用户名密码
        if (!validateCredentials(loginDTO.getUsername(), loginDTO.getPassword())) {
            log.warn("登录失败: 用户名或密码错误, username={}", loginDTO.getUsername());
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 生成Token（简化版，实际生产环境应使用JWT）
        String token = UUID.randomUUID().toString().replace("-", "");
        
        // 构建返回对象
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUsername(loginDTO.getUsername());
        
        log.info("用户登录成功: username={}", loginDTO.getUsername());
        
        return loginVO;
    }

    /**
     * 用户登出
     */
    @Override
    public void logout() {
        log.info("用户登出");
        // Session会在Controller层销毁
    }

    /**
     * 验证用户名密码
     *
     * @param username 用户名
     * @param password 密码
     * @return true-验证通过，false-验证失败
     */
    @Override
    public boolean validateCredentials(String username, String password) {
        // 简单验证：检查是否为默认管理员账号
        // 注意：生产环境应使用加密密码对比，这里仅做演示
        return StrUtil.equals(SystemConstant.DEFAULT_ADMIN_USERNAME, username) 
            && StrUtil.equals(SystemConstant.DEFAULT_ADMIN_PASSWORD, password);
    }
}
