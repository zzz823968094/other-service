package com.dockerlog.service.config;

import com.dockerlog.service.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类
 * 遵循阿里巴巴开发手册：配置类使用@Configuration注解，清晰分离配置逻辑
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册认证拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }

    /**
     * 创建认证拦截器Bean
     *
     * @return 认证拦截器
     */
    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }
}
