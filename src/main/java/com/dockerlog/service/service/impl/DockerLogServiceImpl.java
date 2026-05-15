package com.dockerlog.service.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.dockerlog.service.common.constant.SystemConstant;
import com.dockerlog.service.common.exception.BusinessException;
import com.dockerlog.service.common.util.DockerCommandUtil;
import com.dockerlog.service.dto.LogQueryDTO;
import com.dockerlog.service.service.DockerLogService;
import com.dockerlog.service.vo.ContainerVO;
import com.dockerlog.service.vo.LogFileVO;
import com.dockerlog.service.vo.LogLineVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Docker日志服务实现类
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Slf4j
@Service
public class DockerLogServiceImpl implements DockerLogService {

    /**
     * 获取所有容器列表
     *
     * @return 容器列表
     */
    @Override
    public List<ContainerVO> listContainers() {
        log.info("查询Docker容器列表");
        
        try {
            // 执行Docker命令获取容器列表
            List<String> lines = DockerCommandUtil.executeCommandToList(
                SystemConstant.DOCKER_CMD_LIST_CONTAINERS);
            
            List<ContainerVO> containerList = new ArrayList<>();
            
            for (String line : lines) {
                // 解析每行数据，格式: ID|Names|Status|Image
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    ContainerVO containerVO = new ContainerVO();
                    containerVO.setContainerId(parts[0].trim());
                    containerVO.setContainerName(parts[1].trim());
                    containerVO.setStatus(parts[2].trim());
                    containerVO.setImageName(parts[3].trim());
                    containerList.add(containerVO);
                }
            }
            
            log.info("查询到{}个容器", containerList.size());
            return containerList;
            
        } catch (Exception e) {
            log.error("查询容器列表失败", e);
            throw new BusinessException("查询容器列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询容器日志
     *
     * @param queryDTO 查询参数
     * @return 日志行列表
     */
    @Override
    public List<LogLineVO> queryLogs(LogQueryDTO queryDTO) {
        log.info("查询容器日志: containerId={}, lines={}", 
            queryDTO.getContainerId(), queryDTO.getLines());
        
        // 验证容器是否存在
        if (!validateContainer(queryDTO.getContainerId())) {
            throw new BusinessException("容器不存在: " + queryDTO.getContainerId());
        }
        
        // 限制日志行数
        Integer lines = queryDTO.getLines();
        if (lines == null || lines <= 0) {
            lines = SystemConstant.DEFAULT_LOG_LINES;
        }
        if (lines > SystemConstant.MAX_LOG_LINES) {
            lines = SystemConstant.MAX_LOG_LINES;
        }
        
        try {
            // 构建Docker日志查询命令
            String command = String.format(
                SystemConstant.DOCKER_CMD_LOGS_TAIL, 
                lines, 
                queryDTO.getContainerId());
            
            // 执行命令获取日志
            List<String> logLines = DockerCommandUtil.executeCommandToList(command);
            
            // 转换为VO对象
            List<LogLineVO> result = new ArrayList<>();
            int lineNumber = 1;
            for (String logLine : logLines) {
                LogLineVO logLineVO = new LogLineVO();
                logLineVO.setLineNumber(lineNumber++);
                logLineVO.setContent(logLine);
                result.add(logLineVO);
            }
            
            log.info("查询到{}行日志", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("查询容器日志失败: containerId={}", queryDTO.getContainerId(), e);
            throw new BusinessException("查询容器日志失败: " + e.getMessage());
        }
    }

    /**
     * 验证容器是否存在
     *
     * @param containerId 容器ID或名称
     * @return true-存在，false-不存在
     */
    @Override
    public boolean validateContainer(String containerId) {
        if (StrUtil.isBlank(containerId)) {
            return false;
        }
        
        try {
            // 检查容器是否存在
            String command = "docker inspect " + containerId;
            String result = DockerCommandUtil.executeCommand(command);
            return StrUtil.isNotBlank(result);
        } catch (Exception e) {
            log.warn("容器不存在: {}", containerId);
            return false;
        }
    }

    /**
     * 获取容器日志文件列表
     *
     * @param containerId 容器ID或名称
     * @return 日志文件列表
     */
    @Override
    public List<LogFileVO> listLogFiles(String containerId) {
        log.info("查询容器日志文件列表: containerId={}", containerId);
        
        // 验证容器是否存在
        if (!validateContainer(containerId)) {
            throw new BusinessException("容器不存在: " + containerId);
        }
        
        try {
            // 获取容器的日志路径
            String inspectCommand = "docker inspect --format=\"{{.LogPath}}\" " + containerId;
            String logPath = DockerCommandUtil.executeCommand(inspectCommand).trim();
            
            if (StrUtil.isBlank(logPath) || "null".equals(logPath)) {
                log.warn("容器没有日志文件: {}", containerId);
                return new ArrayList<>();
            }
            
            // 获取日志目录
            File logFile = new File(logPath);
            File logDir = logFile.getParentFile();
            
            if (logDir == null || !logDir.exists()) {
                log.warn("日志目录不存在: {}", logPath);
                return new ArrayList<>();
            }
            
            // 列出所有日志文件
            File[] files = logDir.listFiles((dir, name) -> 
                name.startsWith(containerId) && name.endsWith(".json") || name.endsWith(".log")
            );
            
            List<LogFileVO> logFileList = new ArrayList<>();
            
            if (files != null) {
                for (File file : files) {
                    LogFileVO logFileVO = new LogFileVO();
                    logFileVO.setFileName(file.getName());
                    logFileVO.setFileSize(file.length());
                    logFileVO.setFileSizeFormatted(formatFileSize(file.length()));
                    logFileVO.setLastModified(file.lastModified());
                    logFileVO.setLastModifiedFormatted(DateUtil.formatDateTime(new java.util.Date(file.lastModified())));
                    logFileVO.setIsActive(file.getName().equals(logFile.getName()));
                    
                    logFileList.add(logFileVO);
                }
            }
            
            // 按最后修改时间降序排序
            logFileList.sort((a, b) -> b.getLastModified().compareTo(a.getLastModified()));
            
            log.info("查询到{}个日志文件", logFileList.size());
            return logFileList;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询日志文件列表失败: containerId={}", containerId, e);
            throw new BusinessException("查询日志文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
