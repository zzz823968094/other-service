package com.dockerlog.service.service.impl;

import com.dockerlog.service.dto.LogQueryDTO;
import com.dockerlog.service.service.AsyncLogService;
import com.dockerlog.service.service.DockerLogService;
import com.dockerlog.service.vo.LogLineVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步日志服务实现类
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Slf4j
@Service
public class AsyncLogServiceImpl implements AsyncLogService {

    @Autowired
    private DockerLogService dockerLogService;

    /**
     * 异步查询容器日志
     *
     * @param queryDTO 查询参数
     * @return CompletableFuture包含日志行列表
     */
    @Override
    @Async("dockerLogTaskExecutor")
    public CompletableFuture<List<LogLineVO>> queryLogsAsync(LogQueryDTO queryDTO) {
        log.info("开始异步查询容器日志: containerId={}", queryDTO.getContainerId());
        
        try {
            List<LogLineVO> logs = dockerLogService.queryLogs(queryDTO);
            log.info("异步查询容器日志完成: containerId={}, 行数={}", 
                queryDTO.getContainerId(), logs.size());
            return CompletableFuture.completedFuture(logs);
        } catch (Exception e) {
            log.error("异步查询容器日志失败: containerId={}", queryDTO.getContainerId(), e);
            // 返回一个异常的CompletableFuture
            CompletableFuture<List<LogLineVO>> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}
