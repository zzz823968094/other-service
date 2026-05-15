package com.dockerlog.service.controller;

import com.dockerlog.service.common.result.Result;
import com.dockerlog.service.dto.LoginDTO;
import com.dockerlog.service.service.AuthService;
import com.dockerlog.service.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * 认证控制器
 * 遵循阿里巴巴开发手册：Controller层负责接收请求、参数校验、调用Service、返回结果
 * RESTful风格：使用@PostMapping处理登录请求
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求参数
     * @param session  HTTP会话
     * @return 登录响应
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO, HttpSession session) {
        log.info("收到登录请求: username={}", loginDTO.getUsername());
        
        // 调用服务层进行登录验证
        LoginVO loginVO = authService.login(loginDTO);
        
        // 将用户信息存入Session
        session.setAttribute("currentUser", loginVO.getUsername());
        
        log.info("用户登录成功: username={}", loginVO.getUsername());
        
        return Result.success("登录成功", loginVO);
    }

    /**
     * 用户登出
     *
     * @param session HTTP会话
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        log.info("收到登出请求");
        
        // 调用服务层处理登出逻辑
        authService.logout();
        
        // 销毁Session
        session.invalidate();
        
        log.info("用户登出成功");
        
        return Result.success("登出成功", null);
    }
}
