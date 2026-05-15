package com.dockerlog.service.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 日志行VO
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Data
public class LogLineVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志内容
     */
    private String content;

    /**
     * 行号
     */
    private Integer lineNumber;
}
