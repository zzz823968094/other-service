package com.dockerlog.service.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 日志查询请求DTO
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Data
public class LogQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 容器ID或名称
     */
    @NotBlank(message = "容器ID或名称不能为空")
    private String containerId;

    /**
     * 日志行数（默认100行）
     */
    @Min(value = 1, message = "日志行数最小为1")
    @Max(value = 10000, message = "日志行数最大为10000")
    private Integer lines = 100;

    /**
     * 是否实时跟踪（可选参数）
     */
    private Boolean follow = false;
}
