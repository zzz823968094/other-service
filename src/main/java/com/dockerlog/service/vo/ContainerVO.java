package com.dockerlog.service.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 容器信息VO
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@Data
public class ContainerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 容器ID
     */
    private String containerId;

    /**
     * 容器名称
     */
    private String containerName;

    /**
     * 容器状态
     */
    private String status;

    /**
     * 镜像名称
     */
    private String imageName;
}
