#!/bin/bash

# 清理 docker-test.sh 脚本创建的资源
# 此脚本只会删除测试脚本创建的容器和镜像，不影响其他资源

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 配置变量（与 docker-test.sh 保持一致）
IMAGE_NAME="parse-short-video"
IMAGE_TAG="test"
CONTAINER_NAME="parse-video-test"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}清理测试脚本创建的 Docker 资源${NC}"
echo -e "${BLUE}========================================${NC}"

echo -e "\n${YELLOW}本脚本将清理以下资源：${NC}"
echo -e "  容器: ${YELLOW}${CONTAINER_NAME}${NC}"
echo -e "  镜像: ${YELLOW}${IMAGE_NAME}:${IMAGE_TAG}${NC}"
echo -e "\n${GREEN}其他 Docker 资源将保持不变${NC}"

# 询问确认
echo -e "\n${YELLOW}是否继续？(y/n) ${NC}"
read -r response

if [[ ! "$response" =~ ^[Yy]$ ]]; then
    echo -e "${BLUE}已取消${NC}"
    exit 0
fi

echo -e "\n${YELLOW}开始清理...${NC}"

# 清理容器
echo -e "\n${BLUE}[1/2] 清理容器: ${CONTAINER_NAME}${NC}"
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "  发现容器 ${CONTAINER_NAME}"

    # 检查容器是否在运行
    if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        echo "  停止容器..."
        timeout 10 docker stop $CONTAINER_NAME 2>/dev/null || docker kill $CONTAINER_NAME 2>/dev/null || true
    fi

    echo "  删除容器..."
    docker rm -f $CONTAINER_NAME 2>/dev/null || true
    echo -e "  ${GREEN}✓ 容器已删除${NC}"
else
    echo -e "  ${BLUE}未找到容器 ${CONTAINER_NAME}${NC}"
fi

# 清理镜像
echo -e "\n${BLUE}[2/2] 清理镜像: ${IMAGE_NAME}:${IMAGE_TAG}${NC}"
if docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${IMAGE_NAME}:${IMAGE_TAG}$"; then
    echo "  发现镜像 ${IMAGE_NAME}:${IMAGE_TAG}"
    echo "  删除镜像..."
    docker rmi -f $IMAGE_NAME:$IMAGE_TAG 2>/dev/null || true
    echo -e "  ${GREEN}✓ 镜像已删除${NC}"
else
    echo -e "  ${BLUE}未找到镜像 ${IMAGE_NAME}:${IMAGE_TAG}${NC}"
fi

# 显示清理后的状态
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✅ 清理完成！${NC}"
echo -e "${BLUE}========================================${NC}"

echo -e "\n${YELLOW}验证清理结果：${NC}"
echo -e "\n容器列表（查找 ${CONTAINER_NAME}）："
docker ps -a --format "table {{.Names}}\t{{.Status}}" | grep -E "NAMES|${CONTAINER_NAME}" || echo "  未找到 ${CONTAINER_NAME}"

echo -e "\n镜像列表（查找 ${IMAGE_NAME}:${IMAGE_TAG}）："
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | grep -E "REPOSITORY|${IMAGE_NAME}" || echo "  未找到 ${IMAGE_NAME}:${IMAGE_TAG}"

echo -e "\n${GREEN}其他 Docker 资源保持不变${NC}"
echo ""

