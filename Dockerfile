# ========================================
# Dockerfile - Docker日志查询服务
# 遵循阿里云容器化部署最佳实践
# ========================================

# 第一阶段：构建阶段
FROM maven:3.8.6-jdk-8-slim AS builder

# 设置工作目录
WORKDIR /build

# 复制 pom.xml 并下载依赖（利用 Docker 缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并编译打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# 第二阶段：运行阶段
FROM openjdk:8-jre-slim

# 设置维护者信息
LABEL maintainer="docker-log-service"
LABEL version="1.0.0"
LABEL description="Docker日志查询服务"

# 设置时区为上海
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用用户（安全最佳实践：不以 root 运行）
RUN groupadd -r appuser && useradd -r -g appuser -d /home/appuser -s /sbin/nologin appuser \
    && mkdir -p /home/appuser \
    && chown -R appuser:appuser /home/appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 包
COPY --from=builder /build/target/docker-log-service-1.0.0.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs \
    && chown -R appuser:appuser /app

# 切换到应用用户
USER appuser

# 暴露端口
EXPOSE 9091

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9091/actuator/health || exit 1

# JVM 参数优化（遵循阿里云 JVM 调优建议）
ENV JAVA_OPTS="-Xms256m -Xmx512m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}"]
