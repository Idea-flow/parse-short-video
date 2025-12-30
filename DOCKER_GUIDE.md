# Docker 镜像构建和部署指南

## 📋 目录
- [前置要求](#前置要求)
- [GitHub Secrets 配置](#github-secrets-配置)
- [本地构建和测试](#本地构建和测试)
- [GitHub Actions 自动构建](#github-actions-自动构建)
- [部署运行](#部署运行)
- [故障排查](#故障排查)

## 🔧 前置要求

### 本地开发环境
- Java 21 或更高版本
- Maven 3.6 或更高版本
- Docker Desktop（用于本地构建和测试）

### Docker Hub 账号
- 注册 Docker Hub 账号：https://hub.docker.com
- 记录您的用户名和密码（用于配置 GitHub Secrets）

## 🔐 GitHub Secrets 配置

在 GitHub Actions 能够推送镜像到 Docker Hub 之前，需要配置以下 Secrets：

1. 打开您的 GitHub 仓库
2. 点击 **Settings** → **Secrets and variables** → **Actions**
3. 点击 **New repository secret** 添加以下两个 Secret：

   | Secret 名称 | 值 | 说明 |
   |------------|-----|------|
   | `DOCKER_USERNAME` | 您的 Docker Hub 用户名 | 例如：biliw |
   | `DOCKER_PASSWORD` | 您的 Docker Hub 密码或 Access Token | 建议使用 Access Token |

### 创建 Docker Hub Access Token（推荐）

为了安全，建议使用 Access Token 而不是密码：

1. 登录 Docker Hub
2. 点击右上角头像 → **Account Settings**
3. 选择 **Security** → **New Access Token**
4. 输入描述（例如：GitHub Actions）
5. 点击 **Generate**
6. 复制生成的 Token（只显示一次）
7. 将 Token 作为 `DOCKER_PASSWORD` 的值

## 💻 本地构建和测试

### 1. 构建 JAR 文件

```bash
# 清理并构建项目
mvn clean package -DskipTests
```

### 2. 构建 Docker 镜像

```bash
# 构建镜像（单平台）
docker build -t parse-short-video:latest .

# 查看构建的镜像
docker images | grep parse-short-video
```

### 3. 本地运行测试

```bash
# 运行容器
docker run -d -p 8080:8080 --name parse-video parse-short-video:latest

# 查看日志
docker logs -f parse-video

# 测试应用
curl http://localhost:8080

# 停止并删除容器
docker stop parse-video
docker rm parse-video
```

### 4. 多平台构建（本地测试）

```bash
# 创建 buildx builder
docker buildx create --name mybuilder --use
docker buildx inspect --bootstrap

# 构建多平台镜像（不推送）
docker buildx build --platform linux/amd64,linux/arm64 -t parse-short-video:latest .

# 构建并推送到 Docker Hub
docker login
docker buildx build --platform linux/amd64,linux/arm64 -t biliw/parse-short-video:latest --push .
```

## 🚀 GitHub Actions 自动构建

### 触发构建

工作流会在以下情况自动触发：

1. **推送代码到 main 分支**
   ```bash
   git add .
   git commit -m "update code"
   git push origin main
   ```
   
2. **创建 Pull Request 到 main 分支**
   - PR 时会构建但不推送镜像
   - 合并后会构建并推送镜像

### 查看构建进度

1. 打开 GitHub 仓库
2. 点击 **Actions** 标签
3. 选择最新的工作流运行
4. 查看各个步骤的执行情况和日志

### 构建产物

每次成功构建会生成两个标签的镜像：

- `biliw/parse-short-video:latest` - 最新版本
- `biliw/parse-short-video:<commit-sha>` - 特定提交版本

支持的平台：
- `linux/amd64` - Intel/AMD 处理器（服务器、Intel Mac）
- `linux/arm64` - ARM 处理器（Apple Silicon Mac M1/M2/M3）

## 🌐 部署运行

### 在 Linux 服务器上部署

```bash
# 拉取最新镜像
docker pull biliw/parse-short-video:latest

# 运行容器
docker run -d \
  --name parse-video \
  -p 8080:8080 \
  --restart unless-stopped \
  biliw/parse-short-video:latest

# 查看日志
docker logs -f parse-video
```

### 在 Mac 上部署（Apple Silicon）

```bash
# Docker 会自动选择 ARM64 镜像
docker pull biliw/parse-short-video:latest

# 运行容器
docker run -d \
  --name parse-video \
  -p 8080:8080 \
  biliw/parse-short-video:latest
```

### 使用 Docker Compose

创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  parse-video:
    image: biliw/parse-short-video:latest
    container_name: parse-video
    ports:
      - "8080:8080"
    restart: unless-stopped
    environment:
      - JAVA_OPTS=-Xms256m -Xmx512m
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

运行：

```bash
# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 🔍 故障排查

### 问题 1：GitHub Actions 构建失败

**症状**：GitHub Actions 工作流执行失败

**可能原因和解决方法**：

1. **Docker Hub 登录失败**
   - 检查 `DOCKER_USERNAME` 和 `DOCKER_PASSWORD` 是否正确配置
   - 如果使用 Access Token，确保 Token 有推送权限

2. **Maven 构建失败**
   - 检查 `pom.xml` 配置
   - 查看 Actions 日志中的详细错误信息

3. **多平台构建失败**
   - 某些依赖可能不支持 ARM64 平台
   - 可以暂时只构建 `linux/amd64` 平台

### 问题 2：容器启动失败

**症状**：容器启动后立即退出

**排查步骤**：

```bash
# 查看容器日志
docker logs parse-video

# 查看容器状态
docker ps -a | grep parse-video

# 进入容器调试
docker run -it --rm biliw/parse-short-video:latest sh
```

**常见原因**：
- 端口被占用：更换宿主机端口 `-p 8081:8080`
- 内存不足：调整 JVM 参数 `-e JAVA_OPTS="-Xms128m -Xmx256m"`

### 问题 3：应用无法访问

**症状**：容器运行正常但无法通过浏览器访问

**排查步骤**：

```bash
# 检查端口映射
docker port parse-video

# 检查容器网络
docker inspect parse-video | grep IPAddress

# 在容器内测试
docker exec parse-video wget -O- http://localhost:8080
```

## 📊 性能优化建议

### 1. 调整 JVM 参数

根据服务器配置调整内存：

```bash
docker run -d -p 8080:8080 \
  -e JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC" \
  biliw/parse-short-video:latest
```

### 2. 使用特定版本标签

生产环境建议使用特定版本而不是 `latest`：

```bash
docker pull biliw/parse-short-video:abc123def
```

### 3. 配置健康检查

添加健康检查确保服务可用性（见 Docker Compose 示例）

## 📝 相关文件说明

- `Dockerfile` - Docker 镜像构建文件
- `.dockerignore` - Docker 构建时忽略的文件
- `.github/workflows/docker-my.yml` - GitHub Actions 工作流配置

## 🤝 贡献指南

如果您在使用过程中遇到问题或有改进建议，欢迎：

1. 提交 Issue
2. 创建 Pull Request
3. 完善文档

## 📞 获取帮助

- 查看 GitHub Actions 日志
- 查看 Docker 容器日志
- 参考 Spring Boot 官方文档
- 参考 Docker 官方文档

