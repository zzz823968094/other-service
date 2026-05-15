package com.dockerlog.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 * 遵循阿里巴巴开发手册：配置类使用@Configuration注解
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注册ServerEndpointExporter
     * 用于扫描和注册使用@ServerEndpoint注解声明的WebSocket endpoint
     *
     * @return ServerEndpointExporter
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
