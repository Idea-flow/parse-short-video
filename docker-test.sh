#!/bin/bash

# Docker 镜像本地构建和测试脚本
# 用于在本地环境测试 Docker 镜像构建和运行
#
# 特点：
# - 只清理和管理本脚本创建的资源
# - 容器名: parse-video-test
# - 镜像名: parse-short-video:test
# - 不会影响其他 Docker 容器和镜像

set -e  # 遇到错误立即退出

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
IMAGE_NAME="parse-short-video"
IMAGE_TAG="test"
CONTAINER_NAME="parse-video-test"
PORT="8080"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Docker 镜像本地构建和测试${NC}"
echo -e "${BLUE}========================================${NC}"

# 0. 检查 Docker 是否正常运行
echo -e "\n${YELLOW}[0/6] 检查 Docker 环境...${NC}"
echo "检查 Docker 是否运行..."
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}✗ Docker 未运行或响应超时${NC}"
    echo "请确保 Docker Desktop 已启动并运行"
    echo "提示："
    echo "  1. 打开 Docker Desktop 应用"
    echo "  2. 等待 Docker 完全启动（状态栏显示 Docker 图标）"
    echo "  3. 重新运行此脚本"
    exit 1
fi
echo -e "${GREEN}✓ Docker 运行正常${NC}"

# 1. 清理本脚本创建的旧容器和镜像（不影响其他资源）
echo -e "\n${YELLOW}[1/6] 清理本脚本创建的旧容器和镜像...${NC}"

# 只清理本脚本创建的容器 (parse-video-test)
echo "检查容器: ${CONTAINER_NAME}"
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "  发现旧容器 ${CONTAINER_NAME}，正在清理..."
    # 强制停止容器（添加超时）
    timeout 10 docker stop $CONTAINER_NAME 2>/dev/null || docker kill $CONTAINER_NAME 2>/dev/null || true
    # 强制删除容器
    docker rm -f $CONTAINER_NAME 2>/dev/null || true
    echo "  ✓ 容器 ${CONTAINER_NAME} 已清理"
else
    echo "  未发现旧容器 ${CONTAINER_NAME}"
fi

# 只清理本脚本创建的镜像 (parse-short-video:test)
echo "检查镜像: ${IMAGE_NAME}:${IMAGE_TAG}"
if docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${IMAGE_NAME}:${IMAGE_TAG}$"; then
    echo "  发现旧镜像 ${IMAGE_NAME}:${IMAGE_TAG}，正在清理..."
    docker rmi -f $IMAGE_NAME:$IMAGE_TAG 2>/dev/null || true
    echo "  ✓ 镜像 ${IMAGE_NAME}:${IMAGE_TAG} 已清理"
else
    echo "  未发现旧镜像 ${IMAGE_NAME}:${IMAGE_TAG}"
fi

echo -e "${GREEN}✓ 清理完成（仅清理本脚本创建的资源）${NC}"

# 2. 使用 Maven 构建项目
echo -e "\n${YELLOW}[2/6] 使用 Maven 构建项目...${NC}"
echo "开始编译，这可能需要几分钟..."
if mvn clean package -DskipTests -q; then
    echo -e "${GREEN}✓ Maven 构建成功${NC}"
else
    echo -e "${RED}✗ Maven 构建失败${NC}"
    exit 1
fi

# 3. 构建 Docker 镜像
echo -e "\n${YELLOW}[3/6] 构建 Docker 镜像...${NC}"
docker build -t $IMAGE_NAME:$IMAGE_TAG .

# 4. 查看镜像信息
echo -e "\n${YELLOW}[4/6] 镜像信息：${NC}"
docker images | grep $IMAGE_NAME

# 5. 运行容器
echo -e "\n${YELLOW}[5/6] 启动容器...${NC}"
docker run -d \
  --name $CONTAINER_NAME \
  -p $PORT:8080 \
  $IMAGE_NAME:$IMAGE_TAG

# 等待容器启动
echo "等待容器启动..."
sleep 10

# 6. 测试应用
echo -e "\n${YELLOW}[6/6] 测试应用...${NC}"

# 查看容器状态
if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
    echo -e "${GREEN}✅ 容器运行正常${NC}"

    # 显示容器日志
    echo -e "\n${BLUE}容器日志（最近 20 行）：${NC}"
    docker logs --tail 20 $CONTAINER_NAME

    # 测试应用端点
    echo -e "\n${BLUE}测试应用端点...${NC}"
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT | grep -q "200\|302"; then
        echo -e "${GREEN}✅ 应用响应正常${NC}"
    else
        echo -e "${RED}❌ 应用无响应${NC}"
    fi
else
    echo -e "${RED}❌ 容器启动失败${NC}"
    echo "查看容器日志："
    docker logs $CONTAINER_NAME
    exit 1
fi

# 显示使用说明
echo -e "\n${BLUE}========================================${NC}"
echo -e "${GREEN}✅ 测试完成！${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "\n${YELLOW}使用以下命令管理本脚本创建的容器：${NC}"
echo -e "  查看日志: ${GREEN}docker logs -f $CONTAINER_NAME${NC}"
echo -e "  停止容器: ${GREEN}docker stop $CONTAINER_NAME${NC}"
echo -e "  删除容器: ${GREEN}docker rm $CONTAINER_NAME${NC}"
echo -e "  删除镜像: ${GREEN}docker rmi $IMAGE_NAME:$IMAGE_TAG${NC}"
echo -e "\n${YELLOW}访问应用：${NC}"
echo -e "  浏览器打开: ${GREEN}http://localhost:$PORT${NC}"
echo -e "\n${YELLOW}查看运行状态：${NC}"
echo -e "  容器状态: ${GREEN}docker ps | grep $CONTAINER_NAME${NC}"
echo -e "  容器信息: ${GREEN}docker inspect $CONTAINER_NAME${NC}"
echo -e "\n${BLUE}ℹ️  提示：本脚本只管理自己创建的资源${NC}"
echo -e "  ${BLUE}容器名: ${CONTAINER_NAME}${NC}"
echo -e "  ${BLUE}镜像名: ${IMAGE_NAME}:${IMAGE_TAG}${NC}"
echo -e "  ${BLUE}其他 Docker 资源不受影响${NC}"
echo ""

