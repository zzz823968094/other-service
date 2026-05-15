#!/bin/bash

# ========================================
# Docker 服务部署脚本
# 遵循阿里云容器化部署最佳实践
# ========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置变量
IMAGE_NAME="docker-log-service"
IMAGE_VERSION="1.0.0"
COMPOSE_FILE="docker-compose.yml"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Docker 服务部署脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查 Docker 是否安装
echo -e "${YELLOW}[1/5] 检查 Docker 环境...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}[错误] Docker 未安装，请先安装 Docker${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}[错误] docker-compose 未安装，请先安装 docker-compose${NC}"
    exit 1
fi

echo -e "${GREEN}[成功] Docker 版本: $(docker --version)${NC}"
echo -e "${GREEN}[成功] docker-compose 版本: $(docker-compose --version)${NC}"
echo ""

# 检查 docker-compose.yml 是否存在
echo -e "${YELLOW}[2/5] 检查配置文件...${NC}"
if [ ! -f "${COMPOSE_FILE}" ]; then
    echo -e "${RED}[错误] ${COMPOSE_FILE} 不存在${NC}"
    exit 1
fi
echo -e "${GREEN}[成功] 配置文件存在${NC}"
echo ""

# 停止并删除旧容器
echo -e "${YELLOW}[3/5] 停止旧服务...${NC}"
docker-compose -f ${COMPOSE_FILE} down 2>/dev/null || true
echo -e "${GREEN}[成功] 旧服务已停止${NC}"
echo ""

# 启动服务
echo -e "${YELLOW}[4/5] 启动服务...${NC}"
docker-compose -f ${COMPOSE_FILE} up -d

if [ $? -ne 0 ]; then
    echo -e "${RED}[错误] 服务启动失败${NC}"
    exit 1
fi

echo -e "${GREEN}[成功] 服务启动完成${NC}"
echo ""

# 等待服务启动
echo -e "${YELLOW}[5/5] 等待服务启动...${NC}"
sleep 10

# 检查容器状态
echo -e "${BLUE}容器状态:${NC}"
docker-compose -f ${COMPOSE_FILE} ps
echo ""

# 显示日志（最后20行）
echo -e "${BLUE}服务日志（最后20行）:${NC}"
docker-compose -f ${COMPOSE_FILE} logs --tail=20
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  部署完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "服务信息："
echo "  访问地址: http://localhost:9091"
echo "  默认账号: admin / 123456"
echo ""
echo "常用命令："
echo "  查看日志: docker-compose -f ${COMPOSE_FILE} logs -f"
echo "  停止服务: docker-compose -f ${COMPOSE_FILE} down"
echo "  重启服务: docker-compose -f ${COMPOSE_FILE} restart"
echo "  查看状态: docker-compose -f ${COMPOSE_FILE} ps"
echo ""
