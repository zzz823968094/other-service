#!/bin/bash

################################################################################
# Docker日志服务 - 自动化部署脚本
# 用途: 从Git仓库拉取最新代码，构建并部署到Docker容器
# 适用系统: Ubuntu 20.04/22.04/24.04 / CentOS / Debian
################################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
GIT_REPO="https://github.com/your-username/docker-log-service.git"  # 替换为你的Git仓库地址
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_FILE="${PROJECT_DIR}/logs/docker-log-deploy.log"
BACKUP_DIR="${PROJECT_DIR}/backup"

# 日志函数
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "$LOG_FILE"
}

success() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] ✓ $1${NC}" | tee -a "$LOG_FILE"
}

warning() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] ⚠ $1${NC}" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ✗ $1${NC}" | tee -a "$LOG_FILE"
}

# 打印横幅
print_banner() {
    echo -e "${GREEN}"
    echo "=========================================="
    echo "   Docker日志服务 - 自动化部署脚本"
    echo "   Docker Log Service Auto Deploy Script"
    echo "=========================================="
    echo -e "${NC}"
}

# 检查是否为root用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        warning "建议使用root用户或sudo运行此脚本以获得最佳权限"
        echo "示例: sudo bash deploy.sh"
        read -p "是否继续？(y/N) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# 检查系统要求
check_system() {
    log "检查系统要求..."

    # 检查操作系统
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$NAME
        VER=$VERSION_ID
        log "检测到系统: $OS $VER"
    else
        warning "无法检测操作系统版本"
    fi

    # 检查架构
    ARCH=$(uname -m)
    log "系统架构: $ARCH"
}

# 安装依赖
install_dependencies() {
    log "开始安装依赖..."

    # 检测包管理器
    if command -v apt-get &> /dev/null; then
        PKG_MANAGER="apt-get"
    elif command -v yum &> /dev/null; then
        PKG_MANAGER="yum"
    elif command -v dnf &> /dev/null; then
        PKG_MANAGER="dnf"
    else
        error "未找到支持的包管理器"
        exit 1
    fi

    # 更新包列表
    $PKG_MANAGER update -y

    # 安装必要工具
    if [ "$PKG_MANAGER" = "apt-get" ]; then
        $PKG_MANAGER install -y \
            git \
            curl \
            wget \
            unzip \
            openjdk-17-jdk \
            maven \
            docker.io \
            docker-compose-v2 \
            net-tools
    else
        $PKG_MANAGER install -y \
            git \
            curl \
            wget \
            unzip \
            java-17-openjdk-devel \
            maven \
            docker \
            docker-compose \
            net-tools
    fi

    # 启动并启用Docker
    systemctl start docker
    systemctl enable docker

    success "依赖安装完成"
}

# 检查Docker
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker未安装，正在安装..."
        install_dependencies
    fi

    if ! command -v docker compose &> /dev/null; then
        error "Docker Compose未安装，正在安装..."
        install_dependencies
    fi

    success "Docker版本: $(docker --version)"
    success "Docker Compose版本: $(docker compose --version)"
}

# 检查Java和Maven
check_java_maven() {
    if ! command -v java &> /dev/null; then
        error "Java未安装，正在安装..."
        install_dependencies
    fi

    if ! command -v mvn &> /dev/null; then
        error "Maven未安装，正在安装..."
        install_dependencies
    fi

    success "Java版本: $(java -version 2>&1 | head -n 1)"
    success "Maven版本: $(mvn -version 2>&1 | head -n 1)"
}

# 从Git拉取代码
pull_code() {
    log "开始从Git拉取代码..."

    if [ -d "$PROJECT_DIR/.git" ]; then
        # 已存在，执行pull
        log "项目目录已存在，执行git pull..."
        cd "$PROJECT_DIR"
        git pull origin main 2>/dev/null || git pull origin master 2>/dev/null || {
            warning "git pull失败，继续部署当前代码"
        }
    else
        warning "非Git仓库，跳过代码更新"
    fi

    cd "$PROJECT_DIR"
    success "代码检查完成"
    if [ -d ".git" ]; then
        log "当前分支: $(git branch --show-current 2>/dev/null || echo 'unknown')"
        log "最新提交: $(git log -1 --pretty=format:'%h %s' 2>/dev/null || echo 'unknown')"
    fi
}

# 创建日志目录
create_log_dir() {
    mkdir -p "${PROJECT_DIR}/logs"
    success "日志目录已创建"
}

# 备份当前运行版本
backup_current() {
    if [ -d "$PROJECT_DIR" ] && [ -f "$PROJECT_DIR/docker-compose.yml" ]; then
        log "备份当前版本..."
        mkdir -p "$BACKUP_DIR"
        BACKUP_NAME="docker-log-service-$(date +%Y%m%d_%H%M%S)"
        
        # 只备份关键文件
        cp -r "$PROJECT_DIR/src" "$BACKUP_DIR/$BACKUP_NAME/src" 2>/dev/null || true
        cp "$PROJECT_DIR/pom.xml" "$BACKUP_DIR/$BACKUP_NAME/" 2>/dev/null || true
        cp "$PROJECT_DIR/Dockerfile" "$BACKUP_DIR/$BACKUP_NAME/" 2>/dev/null || true
        cp "$PROJECT_DIR/docker-compose.yml" "$BACKUP_DIR/$BACKUP_NAME/" 2>/dev/null || true
        
        success "备份完成: $BACKUP_DIR/$BACKUP_NAME"
    fi
}

# 停止并清理旧容器
stop_containers() {
    log "停止旧容器..."
    cd "$PROJECT_DIR"

    if [ -f docker-compose.yml ]; then
        docker compose down 2>/dev/null || true
        success "旧容器已停止"
    else
        warning "未找到docker-compose.yml"
    fi
}

# 构建Java项目
build_project() {
    log "开始构建项目..."
    cd "$PROJECT_DIR"

    # 清理旧构建
    log "清理旧的构建文件..."
    mvn clean 2>/dev/null || true

    # 编译打包（跳过测试以加快速度）
    log "使用Maven编译打包（跳过测试）..."
    mvn package -DskipTests -B

    if [ $? -eq 0 ]; then
        success "项目构建成功"
    else
        error "项目构建失败，请检查错误日志"
        exit 1
    fi
}

# 构建Docker镜像
build_docker_images() {
    log "开始构建Docker镜像..."
    cd "$PROJECT_DIR"

    # 构建Docker镜像
    docker compose build

    if [ $? -eq 0 ]; then
        success "Docker镜像构建完成"
    else
        error "Docker镜像构建失败"
        exit 1
    fi
}

# 启动服务
start_services() {
    log "启动所有服务..."
    cd "$PROJECT_DIR"

    # 使用docker compose启动
    docker compose up -d

    if [ $? -eq 0 ]; then
        success "所有服务已启动"
    else
        error "服务启动失败"
        exit 1
    fi

    # 等待服务启动
    log "等待服务启动（15秒）..."
    sleep 15
}

# 检查服务状态
check_services() {
    log "检查服务运行状态..."
    cd "$PROJECT_DIR"

    echo ""
    echo -e "${BLUE}=========================================="
    echo "   服务运行状态"
    echo "==========================================${NC}"

    docker compose ps

    echo ""
    log "检查服务端口..."

    # 检查9091端口
    if netstat -tuln 2>/dev/null | grep -q ":9091 " || ss -tuln 2>/dev/null | grep -q ":9091 "; then
        success "Docker日志服务 (端口: 9091) - 运行中"
    else
        warning "Docker日志服务 (端口: 9091) - 未检测到"
    fi

    echo ""
}

# 显示部署信息
show_deploy_info() {
    echo -e "${GREEN}"
    echo "=========================================="
    echo "   部署完成！"
    echo "=========================================="
    echo -e "${NC}"

    echo "项目目录: $PROJECT_DIR"
    echo "日志文件: $LOG_FILE"
    echo "备份目录: $BACKUP_DIR"
    echo ""

    echo -e "${YELLOW}服务访问地址:${NC}"
    echo "  API地址: http://localhost:9091"
    echo "  健康检查: http://localhost:9091/actuator/health"
    echo ""

    echo -e "${YELLOW}常用命令:${NC}"
    echo "  查看服务状态:   cd $PROJECT_DIR && docker compose ps"
    echo "  查看服务日志:   cd $PROJECT_DIR && docker compose logs -f docker-log"
    echo "  停止所有服务:   cd $PROJECT_DIR && docker compose down"
    echo "  重启服务:       cd $PROJECT_DIR && docker compose restart docker-log"
    echo "  重新部署:       sudo bash deploy.sh"
    echo ""

    echo -e "${YELLOW}Docker容器列表:${NC}"
    echo "  docker-log            - Docker日志服务 (9091)"
    echo ""

    echo -e "${YELLOW}API测试:${NC}"
    echo "  登录接口: curl -X POST http://localhost:9091/api/auth/login"
    echo "  容器列表: curl http://localhost:9091/api/docker/containers"
    echo ""
}

# 主函数
main() {
    print_banner

    log "========== 开始部署 =========="

    # 1. 检查root权限
    check_root

    # 2. 检查系统
    check_system

    # 3. 检查依赖
    check_docker
    check_java_maven

    # 4. 创建日志目录
    create_log_dir

    # 5. 拉取代码
    pull_code

    # 6. 备份当前版本
    backup_current

    # 7. 构建项目（在停止服务之前，避免打包失败影响运行中的服务）
    build_project

    # 8. 停止旧服务
    stop_containers

    # 9. 构建Docker镜像
    build_docker_images

    # 10. 启动服务
    start_services

    # 11. 检查服务状态
    check_services

    # 12. 显示部署信息
    show_deploy_info

    log "========== 部署完成 =========="

    success "部署成功！如有问题，请查看日志: tail -f $LOG_FILE"
}

# 执行主函数
main "$@"
