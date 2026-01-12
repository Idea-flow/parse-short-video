#!/bin/bash
# -----------------------------------------------------------------------------
# 构建 Spring Boot GraalVM Native Image 多架构脚本
# 适配环境: macOS (Apple Silicon/M4)
# 目标: 打包 linux/amd64 和 linux/arm64 镜像
# -----------------------------------------------------------------------------

IMAGE_NAME="parse-short-video-graalvm-2"
TAG="latest"
DOCKERFILE="Dockerfile.graalvm"

echo "=== 开始构建多架构 Native Image: ${IMAGE_NAME}:${TAG} ==="
echo "=== 注意: 这是一个耗时操作，特别是 linux/amd64 架构在 ARM 芯片上构建时 (需要 QEMU 模拟) ==="

# 1. 确保 docker buildx 已启用并创建构建器 instance
# 如果不存在名为 'container-builder' 的 builder，则创建一个
if ! docker buildx inspect container-builder > /dev/null 2>&1; then
    echo "创建并引导新的 docker buildx builder..."
    docker buildx create --name container-builder --driver docker-container --bootstrap --use
else
    echo "使用现有的 docker buildx builder: container-builder"
    docker buildx use container-builder
fi

# 2. 执行构建
# --platform: 指定目标架构
# --load: 构建完成后加载到本地 Docker daemon (仅支持单一架构，多架构通常需要 --push 到仓库)
# 鉴于本地测试需求，这里演示 --load linux/arm64 (本机架构)，
# 如果要同时构建并推送多架构，请使用: --platform linux/amd64,linux/arm64 --push

# 选项 A: 仅构建本机架构 (快速，用于测试)
#echo "=== 构建 linux/arm64 (本机架构) ==="
#docker buildx build \
#  --platform linux/arm64 \
#  -t ${IMAGE_NAME}:${TAG} \
#  -f ${DOCKERFILE} \
#  --load \
#  .

# 选项 B: 构建多架构并推送到仓库 (生产用)
# 取消注释以下行以启用多架构推送 (需要先 docker login)
echo "=== 构建 linux/amd64,linux/arm64 多架构 ==="
 REPO="biliw"
 docker buildx build \
   --platform linux/arm64,linux/amd64 \
   -t ${REPO}/${IMAGE_NAME}:${TAG} \
   -f ${DOCKERFILE} \
   --push \
   .

echo "=== 构建完成 ==="
echo "运行命令: docker run --rm -p 40203:40203 ${IMAGE_NAME}:${TAG}"

