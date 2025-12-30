# 🚀 快速开始指南

## 一分钟启动服务

### 步骤 1: 检查 Java 版本
```bash
java -version
# 需要显示 Java 21 或更高版本
```

如果版本不对，在 macOS 上设置：
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### 步骤 2: 编译项目
```bash
cd /Users/wangpenglong/projects/java/parse-short-video
./build.sh
```

或者手动编译：
```bash
chmod +x mvnw
./mvnw clean compile
```

### 步骤 3: 运行服务
```bash
./run.sh
```

或者手动运行：
```bash
./mvnw spring-boot:run
```

### 步骤 4: 访问服务
打开浏览器访问：http://localhost:40200

---

## 📝 API 测试

### 测试抖音视频
```bash
curl "http://localhost:40200/video/share/url/parse?url=https://v.douyin.com/iRNBho6u/"
curl "http://localhost:40200/video/share/url/parse?url=https://v.douyin.com/4uvwhxCSEGo/"
```

### 测试小红书笔记
```bash
curl "http://localhost:40200/video/share/url/parse?url=https://www.xiaohongshu.com/explore/xxxxx"
```

---

## ⚡ 常见问题

### Q: 编译报错 "类文件版本错误"
**A:** 需要使用 Java 21，运行：
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Q: 端口被占用
**A:** 修改 `src/main/resources/application.yml` 中的端口：
```yaml
server:
  port: 40200  # 改成其他端口
```

### Q: 解析失败
**A:** 查看日志：
```bash
tail -f logs/spring.log
```

---

## 📚 更多文档

- 完整文档：查看 `README.md`
- 实现总结：查看 `IMPLEMENTATION_SUMMARY.md`
- 详细计划：查看 `plan-convertPythonVideoParserToSpringBoot.prompt.md`

---

**现在就开始使用吧！** 🎉

