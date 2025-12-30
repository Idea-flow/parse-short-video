
# Parse Short Video - 短视频无水印解析服务

基于 Spring Boot 3.5.9 的短视频无水印解析服务，支持抖音、小红书等平台。

## 系统要求

- Java 21+
- Maven 3.6+

## 功能特性

✅ 支持平台：
- 抖音 (Douyin)
- 小红书 (RedBook/XiaoHongShu)

✅ 功能：
- 解析视频分享链接
- 获取无水印视频地址
- 支持图集内容解析
- 获取视频封面、标题、作者信息

## 快速开始

### 1. 确保使用 Java 21

```bash
# 检查 Java 版本
java -version

# macOS 设置 Java 21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### 2. 编译项目

```bash
# 使用 Maven 编译
mvn clean compile

# 或使用 Maven Wrapper
./mvnw clean compile
```

### 3. 运行项目

```bash
# 使用 Maven 运行
mvn spring-boot:run

# 或使用 Maven Wrapper
./mvnw spring-boot:run

# 或直接运行打包后的 JAR
mvn clean package
java -jar target/parse-short-video-0.0.1-SNAPSHOT.jar
```

### 4. 访问服务

- 前端页面: http://localhost:40200
- API 文档: 见下方

## Docker 部署

### 方式1：使用预构建镜像（推荐）

```bash
# 从 Docker Hub 拉取最新镜像
docker pull biliw/parse-short-video:latest

# 运行容器
docker run -d \
  --name parse-video \
  -p 40200:8080 \
  --restart unless-stopped \
  biliw/parse-short-video:latest

# 查看日志
docker logs -f parse-video
```

### 方式2：本地构建镜像

```bash
# 构建 Docker 镜像
docker build -t parse-short-video:latest .

# 运行容器
docker run -d \
  --name parse-video \
  -p 40200:8080 \
  parse-short-video:latest
```

### 方式3：使用测试脚本

```bash
# 运行自动化测试脚本（构建 + 测试）
./docker-test.sh
```

### Docker Compose 部署

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  parse-video:
    image: biliw/parse-short-video:latest
    container_name: parse-video
    ports:
      - "40200:8080"
    restart: unless-stopped
    environment:
      - JAVA_OPTS=-Xms256m -Xmx512m
```

运行：

```bash
docker-compose up -d
```

**详细的 Docker 部署指南请参考：** [DOCKER_GUIDE.md](DOCKER_GUIDE.md)

## CI/CD 自动构建

本项目使用 GitHub Actions 自动构建和推送 Docker 镜像到 Docker Hub。

### 配置步骤：

1. Fork 本项目到您的 GitHub 账号
2. 在 GitHub 仓库设置中添加 Secrets：
   - `DOCKER_USERNAME`: 您的 Docker Hub 用户名
   - `DOCKER_PASSWORD`: 您的 Docker Hub 密码或 Access Token
3. 推送代码到 main 分支即可触发自动构建

### 支持的平台：
- linux/amd64（Intel/AMD 服务器、Intel Mac）
- linux/arm64（ARM 服务器、Apple Silicon Mac M1/M2/M3）

工作流配置文件：`.github/workflows/docker-my.yml`

## API 接口

### 1. 解析分享链接

**请求：**
```
GET /video/share/url/parse?url={分享链接或包含链接的文本}
```

**示例：**
```bash
# 抖音视频
curl "http://localhost:40200/video/share/url/parse?url=https://v.douyin.com/xxxxx"

# 小红书笔记
curl "http://localhost:40200/video/share/url/parse?url=https://www.xiaohongshu.com/explore/xxxxx"
```

**响应：**
```json
{
  "code": 200,
  "msg": "解析成功",
  "data": {
    "videoUrl": "https://...",
    "coverUrl": "https://...",
    "title": "视频标题",
    "musicUrl": "",
    "images": [],
    "author": {
      "uid": "作者ID",
      "name": "作者昵称",
      "avatar": "头像URL"
    }
  }
}
```

### 2. 解析视频ID

**请求：**
```
GET /video/id/parse?source={平台}&videoId={视频ID}
```

**支持的平台值：**
- `douyin` - 抖音
- `redbook` - 小红书（暂不支持）

**示例：**
```bash
curl "http://localhost:40200/video/id/parse?source=douyin&videoId=7424432820954598707"
```

## 项目结构

```
src/main/java/com/ideaflow/parseshortvideo/parseshortvideo/
├── ParseShortVideoApplication.java   # 主应用类
├── config/                            # 配置类
│   ├── RestClientConfig.java         # RestClient 配置
│   └── WebConfig.java                 # Web MVC 配置
├── controller/                        # 控制器
│   ├── HomeController.java           # 首页控制器
│   └── VideoParseController.java     # API 控制器
├── model/                             # 数据模型
│   ├── ApiResponse.java              # 统一响应类
│   ├── ImgInfo.java                  # 图片信息
│   ├── VideoAuthor.java              # 作者信息
│   ├── VideoInfo.java                # 视频信息
│   └── VideoSource.java              # 视频来源枚举
├── parser/                            # 解析器
│   ├── BaseParser.java               # 基础解析器
│   ├── DouYinParser.java             # 抖音解析器
│   └── RedBookParser.java            # 小红书解析器
├── service/                           # 服务层
│   └── VideoParseService.java        # 视频解析服务
└── util/                              # 工具类
    └── UserAgentHelper.java          # User-Agent 工具
```

## 技术栈

- Spring Boot 3.5.9
- Spring Web
- Spring RestClient
- Lombok
- Jackson (JSON/YAML)
- SnakeYAML
- Jsoup (HTML 解析)

## 配置说明

### application.yml

```yaml
server:
  port: 40200  # 服务端口

spring:
  application:
    name: parse-short-video
  thymeleaf:
    cache: false  # 开发环境关闭缓存

logging:
  level:
    com.ideaflow.parseshortvideo: DEBUG  # 日志级别
```

## 开发说明

### 添加新平台支持

1. 在 `VideoSource` 枚举中添加新平台
2. 创建新的 Parser 类继承 `BaseParser`
3. 在 `VideoParseService` 中注册新的解析器
4. 添加域名映射关系

## 常见问题

### 1. 解析失败

**可能原因：**
- 分享链接已过期
- 平台 API 或页面结构已变化
- 网络连接问题
- User-Agent 被限制

**解决：**
- 检查日志查看详细错误信息
- 尝试使用最新的分享链接
- 更新 User-Agent 列表

### 2. 视频URL无法访问

**原因：** 某些平台的视频 URL 有时效性或需要特定的请求头

**解决：** 在访问视频时添加相应的 Referer 和 User-Agent

## 注意事项

⚠️ **重要提示：**

1. **仅供学习研究使用**：本项目仅用于技术学习和研究，不得用于商业目的
2. **尊重版权**：解析的内容版权归原作者所有，请勿用于侵权行为
3. **遵守平台规则**：请控制请求频率，避免对平台服务造成影响
4. **及时更新**：平台 API 可能随时变化，需要持续维护更新

## License

本项目仅供学习交流使用。


