#!/bin/bash

# ========================================
# Docker 服务更新脚本
# 遵循阿里云容器化部署最佳实践
# ========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

COMPOSE_FILE="docker-compose.yml"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Docker 服务更新脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 停止旧服务
echo -e "${YELLOW}[1/4] 停止旧服务...${NC}"
docker-compose -f ${COMPOSE_FILE} down
echo -e "${GREEN}[成功] 旧服务已停止${NC}"
echo ""

# 重新构建镜像
echo -e "${YELLOW}[2/4] 重新构建镜像...${NC}"
docker-compose -f ${COMPOSE_FILE} build --no-cache

if [ $? -ne 0 ]; then
    echo -e "${RED}[错误] 镜像构建失败${NC}"
    exit 1
fi

echo -e "${GREEN}[成功] 镜像构建完成${NC}"
echo ""

# 启动新服务
echo -e "${YELLOW}[3/4] 启动新服务...${NC}"
docker-compose -f ${COMPOSE_FILE} up -d

if [ $? -ne 0 ]; then
    echo -e "${RED}[错误] 服务启动失败${NC}"
    exit 1
fi

echo -e "${GREEN}[成功] 新服务启动完成${NC}"
echo ""

# 等待服务启动
echo -e "${YELLOW}[4/4] 等待服务启动...${NC}"
sleep 10

# 检查容器状态
echo "容器状态:"
docker-compose -f ${COMPOSE_FILE} ps
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  更新完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "服务信息："
echo "  访问地址: http://localhost:9091"
echo "  默认账号: admin / 123456"
echo ""
