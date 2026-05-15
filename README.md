# Docker日志查询服务

## 项目简介

基于 Spring Boot 开发的 Docker 日志查询系统，提供简单的用户认证和 Docker 容器日志查看功能。

**严格遵循：**
- 《阿里巴巴 Java 开发手册（泰山版）》
- 阿里云后端工程开发规范
- RESTful API 设计规范

## 技术栈

- **后端框架**: Spring Boot 2.7.18
- **工具类库**: Hutool 5.8.22
- **代码简化**: Lombok 1.18.30
- **构建工具**: Maven
- **JDK版本**: 1.8+

## 功能特性

- ✅ 用户登录认证（默认账号：admin/123456）
- ✅ Docker 容器列表查询
- ✅ Docker 容器日志查询
- ✅ 日志行数限制（默认100行，最大10000行）
- ✅ Session 会话管理
- ✅ 统一异常处理
- ✅ 统一返回封装
- ✅ 参数校验

## 项目结构

```
other-service/
├── src/main/java/com/dockerlog/service/
│   ├── common/                    # 通用层
│   │   ├── constant/              # 常量定义
│   │   ├── exception/             # 异常处理
│   │   ├── result/                # 统一返回封装
│   │   └── util/                  # 工具类
│   ├── config/                    # 配置层
│   │   └── WebMvcConfig.java      # WebMvc配置
│   ├── controller/                # 控制层
│   │   ├── AuthController.java    # 认证控制器
│   │   └── DockerLogController.java # Docker日志控制器
│   ├── service/                   # 服务接口
│   │   ├── AuthService.java       # 认证服务
│   │   └── DockerLogService.java  # Docker日志服务
│   ├── service/impl/              # 服务实现
│   │   ├── AuthServiceImpl.java
│   │   └── DockerLogServiceImpl.java
│   ├── dto/                       # 数据传输对象
│   │   ├── LoginDTO.java          # 登录请求
│   │   └── LogQueryDTO.java       # 日志查询请求
│   ├── vo/                        # 视图对象
│   │   ├── LoginVO.java           # 登录响应
│   │   ├── ContainerVO.java       # 容器信息
│   │   └── LogLineVO.java         # 日志行
│   └── interceptor/               # 拦截器
│       └── AuthInterceptor.java   # 认证拦截器
├── src/main/resources/
│   ├── application.yml            # 主配置文件
│   ├── application-dev.yml        # 开发环境配置
│   └── application-prod.yml       # 生产环境配置
└── pom.xml                        # Maven配置文件
```

## 快速开始

### 前置要求

1. JDK 1.8 或更高版本
2. Maven 3.6+
3. Docker 已安装并运行
4. 当前用户有执行 Docker 命令的权限

### 启动步骤

#### 1. 克隆项目

```bash
cd D:\worktable\vue3\other-service
```

#### 2. 编译项目

```bash
mvn clean package -DskipTests
```

#### 3. 运行项目

```bash
java -jar target/docker-log-service-1.0.0.jar
```

或使用 Maven 直接运行：

```bash
mvn spring-boot:run
```

#### 4. 访问服务

服务启动后，访问地址：http://localhost:9091

## API 接口文档

### 基础信息

- **基础路径**: `/api`
- **端口**: 9091
- **返回格式**: JSON

### 接口列表

#### 1. 用户登录

**接口地址**: `POST /api/auth/login`

**请求参数**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

**返回示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "a1b2c3d4e5f6...",
    "username": "admin"
  },
  "timestamp": 1715760000000
}
```

#### 2. 用户登出

**接口地址**: `POST /api/auth/logout`

**返回示例**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": 1715760000000
}
```

#### 3. 获取容器列表

**接口地址**: `GET /api/docker/containers`

**请求头**: 需要携带 Session Cookie

**返回示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "containerId": "abc123",
      "containerName": "anime-gateway-prod",
      "status": "Up 2 days",
      "imageName": "anime-gateway:latest"
    }
  ],
  "timestamp": 1715760000000
}
```

#### 4. 查询容器日志

**接口地址**: `POST /api/docker/logs`

**请求参数**:
```json
{
  "containerId": "anime-gateway-prod",
  "lines": 100,
  "follow": false
}
```

**参数说明**:
- `containerId`: 容器ID或名称（必填）
- `lines`: 日志行数，默认100，最大10000（可选）
- `follow`: 是否实时跟踪，暂不支持（可选）

**返回示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "lineNumber": 1,
      "content": "2026-05-15 10:00:00 [INFO] Application started"
    },
    {
      "lineNumber": 2,
      "content": "2026-05-15 10:00:01 [INFO] Listening on port 8080"
    }
  ],
  "timestamp": 1715760000000
}
```

#### 5. 验证容器是否存在

**接口地址**: `GET /api/docker/containers/{containerId}/validate`

**返回示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1715760000000
}
```

## 配置说明

### 端口配置

默认端口：9091

修改方式：编辑 `src/main/resources/application.yml`

```yaml
server:
  port: 9091
```

### 默认账号

- **用户名**: admin
- **密码**: 123456

修改方式：编辑 `SystemConstant.java`

```java
public static final String DEFAULT_ADMIN_USERNAME = "admin";
public static final String DEFAULT_ADMIN_PASSWORD = "123456";
```

### Session 超时时间

默认：30分钟

修改方式：编辑 `application.yml`

```yaml
server:
  servlet:
    session:
      timeout: 30m
```

## 部署说明

### Linux 服务器部署

1. 确保服务器已安装 Docker
2. 确保运行应用的用户有执行 Docker 命令的权限
3. 将 JAR 包上传到服务器
4. 运行应用：

```bash
nohup java -jar docker-log-service-1.0.0.jar --spring.profiles.active=prod > app.log 2>&1 &
```

### Docker 容器化部署（可选）

创建 `Dockerfile`:

```dockerfile
FROM openjdk:8-jre-slim
WORKDIR /app
COPY target/docker-log-service-1.0.0.jar app.jar
EXPOSE 9091
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建镜像：

```bash
docker build -t docker-log-service:1.0.0 .
```

运行容器：

```bash
docker run -d \
  -p 9091:9091 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name docker-log-service \
  docker-log-service:1.0.0
```

**注意**: 需要挂载 Docker Socket 以便容器内可以执行 Docker 命令。

## 安全说明

⚠️ **重要提示**:

1. 当前版本使用简单的 Session 认证，适合内部使用
2. 生产环境建议：
   - 修改默认密码
   - 使用 HTTPS
   - 启用 CSRF 保护
   - 考虑使用 JWT Token 替代 Session
   - 添加 IP 白名单限制
   - 记录操作日志

## 常见问题

### Q1: 提示"命令执行失败"？

**A**: 检查以下几点：
1. 确认 Docker 已安装并运行
2. 确认当前用户有执行 Docker 命令的权限
3. Linux 系统：将用户加入 docker 用户组
   ```bash
   sudo usermod -aG docker $USER
   ```

### Q2: 登录后访问其他接口提示"请先登录"？

**A**: 确保请求携带了 Session Cookie，前端需要：
1. 设置 `withCredentials: true`
2. 保存登录响应中的 Cookie
3. 后续请求自动携带 Cookie

### Q3: 如何修改端口？

**A**: 有三种方式：
1. 修改 `application.yml` 中的 `server.port`
2. 启动时指定：`java -jar app.jar --server.port=8080`
3. 设置环境变量：`SERVER_PORT=8080`

### Q4: 日志文件在哪里？

**A**: 日志文件位于项目根目录的 `logs/` 文件夹下：
- `docker-log-service.log`: 应用日志

## 开发规范

本项目严格遵循以下规范：

1. **阿里巴巴 Java 开发手册（泰山版）**
   - 命名规范：驼峰命名、常量全大写
   - 代码分层：Controller → Service → ServiceImpl
   - 异常处理：统一全局异常处理
   - 返回封装：统一 Result 格式
   - 禁止魔法值：使用常量统一管理

2. **RESTful API 设计规范**
   - GET: 查询资源
   - POST: 创建资源/执行操作
   - PUT: 更新资源
   - DELETE: 删除资源

3. **Spring Boot 最佳实践**
   - 使用 Lombok 简化代码
   - 使用 @Validated 进行参数校验
   - 使用 @Slf4j 统一日志记录
   - 多环境配置分离

## 版本历史

- **v1.0.0** (2026-05-15)
  - 初始版本发布
  - 支持用户登录认证
  - 支持 Docker 容器列表查询
  - 支持 Docker 容器日志查询

## 许可证

本项目仅供学习和内部使用。

## 联系方式

如有问题，请联系项目维护者。
