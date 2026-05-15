#!/bin/bash

# ========================================
# Docker 服务停止脚本
# ========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

COMPOSE_FILE="docker-compose.yml"

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}  Docker 服务停止脚本${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""

# 停止服务
echo -e "${YELLOW}[1/2] 停止服务...${NC}"
docker-compose -f ${COMPOSE_FILE} down

if [ $? -ne 0 ]; then
    echo -e "${RED}[错误] 服务停止失败${NC}"
    exit 1
fi

echo -e "${GREEN}[成功] 服务已停止${NC}"
echo ""

# 清理未使用的镜像（可选）
read -p "是否清理未使用的 Docker 镜像？(y/n): " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}[2/2] 清理未使用的镜像...${NC}"
    docker image prune -f
    echo -e "${GREEN}[成功] 清理完成${NC}"
else
    echo -e "${YELLOW}[跳过] 镜像清理${NC}"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  停止完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
