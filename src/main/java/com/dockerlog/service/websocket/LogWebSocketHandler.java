package com.dockerlog.service.websocket;

import cn.hutool.json.JSONUtil;
import com.dockerlog.service.common.constant.SystemConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实时日志WebSocket处理器
 * 遵循阿里巴巴开发手册：使用@ServerEndpoint注解声明WebSocket端点
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Slf4j
@Component
@ServerEndpoint("/ws/logs/{containerId}")
public class LogWebSocketHandler {

    /**
     * 存储所有活跃的WebSocket会话
     * Key: sessionId, Value: Session
     */
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * 存储会话与容器的映射关系
     * Key: sessionId, Value: containerId
     */
    private static final Map<String, String> SESSION_CONTAINER_MAP = new ConcurrentHashMap<>();

    /**
     * 存储每个会话的日志读取进程
     * Key: sessionId, Value: Process
     */
    private static final Map<String, Process> PROCESS_MAP = new ConcurrentHashMap<>();

    /**
     * WebSocket连接建立时调用
     *
     * @param session     WebSocket会话
     * @param containerId 容器ID或名称
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("containerId") String containerId) {
        String sessionId = session.getId();
        
        log.info("WebSocket连接建立: sessionId={}, containerId={}", sessionId, containerId);
        
        // 保存会话信息
        SESSION_MAP.put(sessionId, session);
        SESSION_CONTAINER_MAP.put(sessionId, containerId);
        
        // 发送连接成功消息
        sendMessage(session, "connected", "已连接到容器: " + containerId);
        
        // 启动日志读取线程
        startLogReading(session, containerId);
    }

    /**
     * WebSocket连接关闭时调用
     *
     * @param session WebSocket会话
     */
    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();
        
        log.info("WebSocket连接关闭: sessionId={}", sessionId);
        
        // 停止日志读取进程
        stopLogReading(sessionId);
        
        // 移除会话信息
        SESSION_MAP.remove(sessionId);
        SESSION_CONTAINER_MAP.remove(sessionId);
    }

    /**
     * 收到客户端消息时调用
     *
     * @param session WebSocket会话
     * @param message 客户端消息
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        log.debug("收到客户端消息: sessionId={}, message={}", session.getId(), message);
        
        // 可以处理客户端发来的控制命令，如暂停、恢复等
        if ("pause".equals(message)) {
            stopLogReading(session.getId());
            sendMessage(session, "info", "日志读取已暂停");
        } else if ("resume".equals(message)) {
            String containerId = SESSION_CONTAINER_MAP.get(session.getId());
            if (containerId != null) {
                startLogReading(session, containerId);
                sendMessage(session, "info", "日志读取已恢复");
            }
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session   WebSocket会话
     * @param throwable 异常
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        String sessionId = session.getId();
        log.error("WebSocket发生错误: sessionId={}", sessionId, throwable);
        
        // 清理资源
        stopLogReading(sessionId);
        SESSION_MAP.remove(sessionId);
        SESSION_CONTAINER_MAP.remove(sessionId);
    }

    /**
     * 启动日志读取
     *
     * @param session     WebSocket会话
     * @param containerId 容器ID
     */
    private void startLogReading(Session session, String containerId) {
        // 如果已有进程在运行，先停止
        stopLogReading(session.getId());
        
        // 创建新线程读取日志
        Thread logThread = new Thread(() -> {
            Process process = null;
            BufferedReader reader = null;
            
            try {
                // 构建Docker日志命令（实时跟踪模式）
                String command = "docker logs -f --tail 100 " + containerId;
                
                // 根据操作系统选择命令执行方式
                String[] cmdArray;
                if (isWindows()) {
                    cmdArray = new String[]{"cmd", "/c", command};
                } else {
                    cmdArray = new String[]{"/bin/sh", "-c", command};
                }
                
                // 执行命令
                process = Runtime.getRuntime().exec(cmdArray);
                
                // 保存进程引用
                PROCESS_MAP.put(session.getId(), process);
                
                // 读取日志输出
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                int lineNumber = 1;
                
                while ((line = reader.readLine()) != null) {
                    // 检查会话是否仍然活跃
                    if (!session.isOpen()) {
                        break;
                    }
                    
                    // 发送日志到客户端
                    sendMessage(session, "log", line, lineNumber++);
                }
                
            } catch (Exception e) {
                log.error("读取日志失败: containerId={}", containerId, e);
                sendMessage(session, "error", "读取日志失败: " + e.getMessage());
            } finally {
                // 关闭资源
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        log.error("关闭资源失败", e);
                    }
                }
                if (process != null) {
                    process.destroy();
                }
                PROCESS_MAP.remove(session.getId());
            }
        });
        
        logThread.setName("log-reader-" + session.getId());
        logThread.setDaemon(true);
        logThread.start();
    }

    /**
     * 停止日志读取
     *
     * @param sessionId 会话ID
     */
    private void stopLogReading(String sessionId) {
        Process process = PROCESS_MAP.remove(sessionId);
        if (process != null) {
            process.destroy();
            log.info("已停止日志读取进程: sessionId={}", sessionId);
        }
    }

    /**
     * 发送消息到客户端
     *
     * @param session WebSocket会话
     * @param type    消息类型
     * @param content 消息内容
     */
    private void sendMessage(Session session, String type, String content) {
        sendMessage(session, type, content, null);
    }

    /**
     * 发送消息到客户端（带行号）
     *
     * @param session    WebSocket会话
     * @param type       消息类型
     * @param content    消息内容
     * @param lineNumber 行号
     */
    private void sendMessage(Session session, String type, String content, Integer lineNumber) {
        if (session == null || !session.isOpen()) {
            return;
        }
        
        try {
            // 构建JSON消息
            Map<String, Object> message = new ConcurrentHashMap<>();
            message.put("type", type);
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());
            
            if (lineNumber != null) {
                message.put("lineNumber", lineNumber);
            }
            
            String jsonMessage = JSONUtil.toJsonStr(message);
            
            // 同步发送消息
            synchronized (session) {
                session.getBasicRemote().sendText(jsonMessage);
            }
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }

    /**
     * 判断是否为Windows系统
     *
     * @return true-Windows, false-Linux/Mac
     */
    private boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows");
    }

    /**
     * 获取当前活跃连接数
     *
     * @return 活跃连接数
     */
    public static int getActiveConnectionCount() {
        return SESSION_MAP.size();
    }
}
