package com.dockerlog.service.service;

import com.dockerlog.service.dto.LogQueryDTO;
import com.dockerlog.service.vo.ContainerVO;
import com.dockerlog.service.vo.LogFileVO;
import com.dockerlog.service.vo.LogLineVO;

import java.util.List;

/**
 * Docker日志服务接口
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
public interface DockerLogService {

    /**
     * 获取所有容器列表
     *
     * @return 容器列表
     */
    List<ContainerVO> listContainers();

    /**
     * 查询容器日志
     *
     * @param queryDTO 查询参数
     * @return 日志行列表
     */
    List<LogLineVO> queryLogs(LogQueryDTO queryDTO);

    /**
     * 验证容器是否存在
     *
     * @param containerId 容器ID或名称
     * @return true-存在，false-不存在
     */
    boolean validateContainer(String containerId);

    /**
     * 获取容器日志文件列表
     *
     * @param containerId 容器ID或名称
     * @return 日志文件列表
     */
    List<LogFileVO> listLogFiles(String containerId);
}
