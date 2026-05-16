package com.dockerlog.service.common.util;

import cn.hutool.core.util.StrUtil;
import com.dockerlog.service.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Docker命令执行工具类
 * 遵循阿里巴巴开发手册：工具类必须使用final修饰，私有构造方法
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Slf4j
public final class DockerCommandUtil {

    /**
     * 命令执行超时时间（秒）
     */
    private static final int COMMAND_TIMEOUT = 60;

    private DockerCommandUtil() {
        throw new IllegalStateException("工具类不允许实例化");
    }

    /**
     * 执行Docker命令并返回结果
     *
     * @param command 命令
     * @return 执行结果
     */
    public static String executeCommand(String command) {
        log.info("执行Docker命令: {}", command);
        
        Process process = null;
        BufferedReader reader = null;
        
        try {
            // 根据操作系统选择命令执行方式
            String[] cmdArray;
            if (isWindows()) {
                cmdArray = new String[]{"cmd", "/c", command};
            } else {
                cmdArray = new String[]{"/bin/sh", "-c", command};
            }
            
            process = Runtime.getRuntime().exec(cmdArray);
            
            // 等待命令执行完成，设置超时时间
            boolean finished = process.waitFor(COMMAND_TIMEOUT, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException("命令执行超时");
            }
            
            // 读取命令输出
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            
            // 检查命令执行是否成功
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                // 读取错误信息
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
                StringBuilder errorMsg = new StringBuilder();
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorMsg.append(errorLine).append("\n");
                }
                log.error("命令执行失败: {}, 错误信息: {}", command, errorMsg);
                throw new BusinessException("命令执行失败: " + errorMsg.toString());
            }
            
            return result.toString();
            
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("命令执行被中断: {}", command, e);
            throw new BusinessException("命令执行被中断");
        } catch (Exception e) {
            log.error("执行命令异常: {}", command, e);
            throw new BusinessException("执行命令失败: " + e.getMessage());
        } finally {
            // 关闭资源
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    log.error("关闭资源失败", e);
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 执行Docker命令并返回行列表
     *
     * @param command 命令
     * @return 结果行列表
     */
    public static List<String> executeCommandToList(String command) {
        String result = executeCommand(command);
        List<String> lines = new ArrayList<>();
        
        if (StrUtil.isNotBlank(result)) {
            String[] split = result.split("\n");
            for (String line : split) {
                if (StrUtil.isNotBlank(line)) {
                    lines.add(line.trim());
                }
            }
        }
        
        return lines;
    }

    /**
     * 判断是否为Windows系统
     *
     * @return true-Windows, false-Linux/Mac
     */
    private static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows");
    }
}
