# 第一阶段：构建阶段
# 使用 Maven 官方镜像进行构建
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 文件
COPY pom.xml .

# 下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段
# 使用更轻量的 JRE 镜像
FROM eclipse-temurin:21-jre-alpine

# 设置工作目录
WORKDIR /app

# 添加非 root 用户以提高安全性
RUN addgroup -S spring && adduser -S spring -G spring

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 更改文件所有权
RUN chown spring:spring app.jar

# 切换到非 root 用户
USER spring:spring

# 暴露应用端口（根据 application.yml 中的配置）
EXPOSE 8080

# 设置 JVM 参数
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

