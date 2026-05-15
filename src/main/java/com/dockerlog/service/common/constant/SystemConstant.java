package com.dockerlog.service.common.constant;

/**
 * 系统常量定义
 * 遵循阿里巴巴开发手册：所有常量必须使用大写，单词间用下划线隔开
 *
 * @author docker-log-service
 * @date 2026-05-15
 */
public class SystemConstant {

    /**
     * 默认管理员用户名
     */
    public static final String DEFAULT_ADMIN_USERNAME = "admin";

    /**
     * 默认管理员密码
     */
    public static final String DEFAULT_ADMIN_PASSWORD = "123456";

    /**
     * Token请求头名称
     */
    public static final String TOKEN_HEADER_NAME = "Authorization";

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Session中用户名的Key
     */
    public static final String SESSION_USER_KEY = "currentUser";

    /**
     * Docker命令 - 列出容器
     */
    public static final String DOCKER_CMD_LIST_CONTAINERS = "docker ps -a --format \"{{.ID}}|{{.Names}}|{{.Status}}|{{.Image}}\"";

    /**
     * Docker命令 - 查看日志
     */
    public static final String DOCKER_CMD_LOGS = "docker logs %s";

    /**
     * Docker命令 - 查看实时日志
     */
    public static final String DOCKER_CMD_LOGS_FOLLOW = "docker logs -f %s";

    /**
     * Docker命令 - 查看最后N行日志
     */
    public static final String DOCKER_CMD_LOGS_TAIL = "docker logs --tail %d %s";

    /**
     * 成功状态码
     */
    public static final Integer SUCCESS_CODE = 200;

    /**
     * 失败状态码
     */
    public static final Integer ERROR_CODE = 500;

    /**
     * 未授权状态码
     */
    public static final Integer UNAUTHORIZED_CODE = 401;

    /**
     * 禁止访问状态码
     */
    public static final Integer FORBIDDEN_CODE = 403;

    /**
     * 成功消息
     */
    public static final String SUCCESS_MSG = "操作成功";

    /**
     * 失败消息
     */
    public static final String ERROR_MSG = "操作失败";

    /**
     * 未登录消息
     */
    public static final String UNAUTHORIZED_MSG = "请先登录";

    /**
     * 日志最大行数限制
     */
    public static final Integer MAX_LOG_LINES = 10000;

    /**
     * 默认日志行数
     */
    public static final Integer DEFAULT_LOG_LINES = 100;

    private SystemConstant() {
        throw new IllegalStateException("常量类不允许实例化");
    }
}
