package com.dockerlog.service.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 日志文件信息VO
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Data
public class LogFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志文件名
     */
    private String fileName;

    /**
     * 日志文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件大小格式化字符串
     */
    private String fileSizeFormatted;

    /**
     * 最后修改时间戳
     */
    private Long lastModified;

    /**
     * 最后修改时间格式化字符串
     */
    private String lastModifiedFormatted;

    /**
     * 是否为当前活动日志
     */
    private Boolean isActive;
}
