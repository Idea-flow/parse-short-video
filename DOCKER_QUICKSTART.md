# 🚀 Docker 快速参考

## 📦 镜像信息
- **镜像名称**: `biliw/parse-short-video:latest`
- **支持平台**: linux/amd64, linux/arm64
- **暴露端口**: 8080

## 🔧 常用命令

### 拉取和运行
```bash
docker pull biliw/parse-short-video:latest
docker run -d -p 8080:8080 --name parse-video biliw/parse-short-video:latest
```

### 日志查看
```bash
docker logs -f parse-video                # 实时查看日志
docker logs --tail 100 parse-video       # 查看最近100行
```

### 容器管理
```bash
docker ps                                 # 查看运行中的容器
docker stop parse-video                   # 停止容器
docker start parse-video                  # 启动容器
docker restart parse-video                # 重启容器
docker rm parse-video                     # 删除容器（需先停止）
```

### 镜像管理
```bash
docker images                             # 查看本地镜像
docker rmi biliw/parse-short-video:latest # 删除镜像
docker system prune -a                    # 清理所有未使用的镜像
```

## 🛠️ 本地构建

### 单平台构建
```bash
docker build -t parse-short-video:latest .
```

### 多平台构建
```bash
# 创建 builder
docker buildx create --name mybuilder --use

# 构建多平台镜像
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t biliw/parse-short-video:latest \
  --push .
```

## 🔐 GitHub Secrets 配置

在 GitHub 仓库设置中添加：
- `DOCKER_USERNAME`: Docker Hub 用户名
- `DOCKER_PASSWORD`: Docker Hub 密码或 Access Token

## 📝 Docker Compose

```yaml
version: '3.8'
services:
  parse-video:
    image: biliw/parse-short-video:latest
    ports:
      - "8080:8080"
    restart: unless-stopped
    environment:
      - JAVA_OPTS=-Xms256m -Xmx512m
```

运行: `docker-compose up -d`

## 🧪 测试命令

```bash
# 健康检查
curl http://localhost:8080

# 测试 API
curl "http://localhost:8080/video/share/url/parse?url=https://v.douyin.com/xxxxx"

# 进入容器
docker exec -it parse-video sh
```

## 📚 更多信息

详细文档请查看: [DOCKER_GUIDE.md](DOCKER_GUIDE.md)

