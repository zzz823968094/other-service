package com.dockerlog.service.interceptor;

import cn.hutool.core.util.StrUtil;
import com.dockerlog.service.common.constant.SystemConstant;
import com.dockerlog.service.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

/**
 * 认证拦截器
 * 遵循阿里巴巴开发手册：拦截器用于统一处理权限验证、日志记录等横切关注点
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * 预处理方法，在Controller方法调用前执行
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return true-继续执行，false-中断执行
     * @throws Exception 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Session
        HttpSession session = request.getSession(false);
        
        // 检查Session中是否存在用户信息
        if (session == null || session.getAttribute(SystemConstant.SESSION_USER_KEY) == null) {
            log.warn("未授权访问: {}", request.getRequestURI());
            
            // 返回未授权错误
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter writer = response.getWriter();
            writer.write(cn.hutool.json.JSONUtil.toJsonStr(Result.unauthorized()));
            writer.flush();
            writer.close();
            
            return false;
        }
        
        // 已登录，继续执行
        String username = (String) session.getAttribute(SystemConstant.SESSION_USER_KEY);
        log.debug("用户{}访问: {}", username, request.getRequestURI());
        
        return true;
    }
}
