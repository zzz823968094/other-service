package com.dockerlog.service.service;

import com.dockerlog.service.dto.LogQueryDTO;
import com.dockerlog.service.vo.LogLineVO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 异步日志服务接口
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
public interface AsyncLogService {

    /**
     * 异步查询容器日志
     *
     * @param queryDTO 查询参数
     * @return CompletableFuture包含日志行列表
     */
    CompletableFuture<List<LogLineVO>> queryLogsAsync(LogQueryDTO queryDTO);
}
