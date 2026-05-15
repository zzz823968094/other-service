package com.dockerlog.service.controller;

import com.dockerlog.service.common.result.Result;
import com.dockerlog.service.dto.LogQueryDTO;
import com.dockerlog.service.service.DockerLogService;
import com.dockerlog.service.service.RealTimeLogService;
import com.dockerlog.service.vo.ContainerVO;
import com.dockerlog.service.vo.LogFileVO;
import com.dockerlog.service.vo.LogLineVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Docker日志控制器
 * 遵循阿里巴巴开发手册：RESTful风格，使用GET查询、POST操作
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Slf4j
@RestController
@RequestMapping("/api/docker")
public class DockerLogController {

    @Autowired
    private DockerLogService dockerLogService;

    @Autowired
    private RealTimeLogService realTimeLogService;

    /**
     * 获取所有容器列表
     *
     * @return 容器列表
     */
    @GetMapping("/containers")
    public Result<List<ContainerVO>> listContainers() {
        log.info("查询容器列表");
        
        List<ContainerVO> containers = dockerLogService.listContainers();
        
        return Result.success(containers);
    }

    /**
     * 查询容器日志
     *
     * @param queryDTO 查询参数
     * @return 日志行列表
     */
    @PostMapping("/logs")
    public Result<List<LogLineVO>> queryLogs(@Validated @RequestBody LogQueryDTO queryDTO) {
        log.info("查询容器日志: containerId={}", queryDTO.getContainerId());
        
        List<LogLineVO> logs = dockerLogService.queryLogs(queryDTO);
        
        return Result.success(logs);
    }

    /**
     * 验证容器是否存在
     *
     * @param containerId 容器ID或名称
     * @return true-存在，false-不存在
     */
    @GetMapping("/containers/{containerId}/validate")
    public Result<Boolean> validateContainer(@PathVariable String containerId) {
        log.info("验证容器是否存在: containerId={}", containerId);
        
        boolean exists = dockerLogService.validateContainer(containerId);
        
        return Result.success(exists);
    }

    /**
     * 获取实时日志WebSocket连接信息
     *
     * @return WebSocket连接信息
     */
    @GetMapping("/websocket/status")
    public Result<Map<String, Object>> getWebSocketStatus() {
        log.info("查询WebSocket连接状态");
        
        Map<String, Object> status = new HashMap<>();
        status.put("activeConnections", realTimeLogService.getActiveConnectionCount());
        // 生产环境使用 wss://，开发环境使用 ws://
        status.put("wsUrl", "wss://log.animeparadise.vip/ws/logs/{containerId}");
        status.put("devWsUrl", "ws://localhost:9091/ws/logs/{containerId}");
        
        return Result.success(status);
    }

    /**
     * 获取容器日志文件列表
     *
     * @param containerId 容器ID或名称
     * @return 日志文件列表
     */
    @GetMapping("/containers/{containerId}/logs")
    public Result<List<LogFileVO>> listLogFiles(@PathVariable String containerId) {
        log.info("查询容器日志文件列表: containerId={}", containerId);
        
        List<LogFileVO> logFiles = dockerLogService.listLogFiles(containerId);
        
        return Result.success(logFiles);
    }
}
