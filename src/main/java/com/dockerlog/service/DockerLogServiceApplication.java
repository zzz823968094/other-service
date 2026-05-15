package com.dockerlog.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Docker日志查询服务启动类
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
@SpringBootApplication
public class DockerLogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerLogServiceApplication.class, args);
    }

}
