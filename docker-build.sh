#!/bin/bash

# ========================================
# Docker 镜像构建脚本
# 遵循阿里云容器化部署最佳实践
# ========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
IMAGE_NAME="docker-log-service"
IMAGE_VERSION="1.0.0"
FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_VERSION}"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Docker 镜像构建脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查 Docker 是否安装
echo -e "${YELLOW}[1/4] 检查 Docker 环境...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}[错误] Docker 未安装，请先安装 Docker${NC}"
    exit 1
fi
echo -e "${GREEN}[成功] Docker 版本: $(docker --version)${NC}"
echo ""

# 检查 Dockerfile 是否存在
echo -e "${YELLOW}[2/4] 检查 Dockerfile...${NC}"
if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}[错误] Dockerfile 不存在${NC}"
    exit 1
fi
echo -e "${GREEN}[成功] Dockerfile 存在${NC}"
echo ""

# 构建镜像
echo -e "${YELLOW}[3/4] 构建 Docker 镜像...${NC}"
echo "镜像名称: ${FULL_IMAGE_NAME}"
echo ""

docker build -t ${FULL_IMAGE_NAME} .

if [ $? -ne 0 ]; then
    echo -e "${RED}[错误] 镜像构建失败${NC}"
    exit 1
fi

echo -e "${GREEN}[成功] 镜像构建完成${NC}"
echo ""

# 显示镜像信息
echo -e "${YELLOW}[4/4] 镜像信息:${NC}"
docker images | grep ${IMAGE_NAME}
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  构建完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "下一步操作："
echo "  启动服务: ./deploy.sh"
echo "  运行容器: docker run -d -p 9091:9091 -v /var/run/docker.sock:/var/run/docker.sock ${FULL_IMAGE_NAME}"
echo ""
