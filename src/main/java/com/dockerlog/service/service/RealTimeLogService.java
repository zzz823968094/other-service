package com.dockerlog.service.service;

import com.dockerlog.service.websocket.LogWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 实时日志服务类
 * 提供WebSocket连接管理和监控功能
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Slf4j
@Service
public class RealTimeLogService {

    /**
     * 获取当前活跃的WebSocket连接数
     *
     * @return 活跃连接数
     */
    public int getActiveConnectionCount() {
        int count = LogWebSocketHandler.getActiveConnectionCount();
        log.debug("当前活跃WebSocket连接数: {}", count);
        return count;
    }

    /**
     * 检查容器是否可以通过WebSocket访问
     *
     * @param containerId 容器ID或名称
     * @return true-可以访问，false-不可访问
     */
    public boolean canAccessContainer(String containerId) {
        // 这里可以添加权限验证逻辑
        // 目前简单返回true，实际项目中应该验证用户是否有权限查看该容器日志
        return true;
    }
}
