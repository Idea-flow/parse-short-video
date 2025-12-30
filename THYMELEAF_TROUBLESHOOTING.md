# Thymeleaf 问题排查指南

## 已修复的问题

✅ **HomeController 文件为空** - 已重新创建完整的 HomeController
✅ **HTML Thymeleaf 命名空间** - 已添加 `xmlns:th="http://www.thymeleaf.org"`
✅ **静态资源路径** - 已使用 Thymeleaf 语法 `th:href="@{/static/favicon.ico}"`
✅ **动态标题** - 已使用 `th:text="${title}"` 支持动态标题

## 配置检查清单

### 1. Thymeleaf 配置 (application.yml)
```yaml
spring:
  thymeleaf:
    cache: false              # ✅ 开发环境关闭缓存
    prefix: classpath:/templates/  # ✅ 模板路径
    suffix: .html            # ✅ 文件后缀
    encoding: UTF-8          # ✅ 编码
    mode: HTML              # ✅ 模式
```

### 2. 文件位置检查
- ✅ Controller: `src/main/java/.../controller/HomeController.java`
- ✅ Template: `src/main/resources/templates/index.html`
- ✅ Static: `src/main/resources/static/favicon.ico`

### 3. 依赖检查 (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

## 测试步骤

### 步骤 1: 编译项目
```bash
./build.sh
# 或
./mvnw clean compile
```

### 步骤 2: 启动服务
```bash
./run.sh
# 或
./mvnw spring-boot:run
```

### 步骤 3: 访问页面
打开浏览器访问: http://localhost:40200

### 步骤 4: 检查日志
查看控制台日志，确认：
- ✅ Tomcat 启动在端口 40200
- ✅ 没有 Thymeleaf 相关错误
- ✅ HomeController 被正确扫描和注册

## 常见问题排查

### 问题 1: 404 Not Found
**症状:** 访问 http://localhost:40200 返回 404

**检查:**
```bash
# 确认 HomeController 是否被扫描
# 日志中应该有类似信息:
# Mapped "{[/]}" onto public java.lang.String ...HomeController.index(...)
```

**解决:**
- 确认 `HomeController` 在正确的包下
- 确认 `@Controller` 注解存在
- 确认主应用类的包路径能扫描到 Controller

### 问题 2: Whitelabel Error Page
**症状:** 显示 Spring Boot 的默认错误页面

**检查:**
```bash
# 查看日志中的错误信息
# 通常会显示具体的 Thymeleaf 错误
```

**可能原因:**
1. 模板文件路径不正确
2. 模板文件语法错误
3. 返回的视图名称与文件名不匹配

**解决:**
```bash
# 确认文件存在
ls -la src/main/resources/templates/index.html

# 确认 Controller 返回 "index"
# 对应文件 templates/index.html
```

### 问题 3: 静态资源 404
**症状:** favicon.ico 或其他静态资源无法加载

**检查:**
```bash
# 确认文件存在
ls -la src/main/resources/static/favicon.ico

# 确认 WebConfig 配置正确
```

**解决:**
- 静态资源应该放在 `src/main/resources/static/` 目录下
- 访问路径: `http://localhost:40200/static/favicon.ico`
- HTML 中使用 Thymeleaf 语法: `th:href="@{/static/favicon.ico}"`

### 问题 4: JavaScript 不工作
**症状:** 页面显示但功能不正常

**检查浏览器控制台:**
- F12 打开开发者工具
- 查看 Console 标签页是否有 JavaScript 错误
- 查看 Network 标签页 API 请求是否正常

**常见错误:**
1. **CORS 错误** - API 请求被跨域限制
2. **404 错误** - API 路径不正确
3. **500 错误** - 后端解析失败

### 问题 5: API 调用失败
**症状:** 点击解析按钮没有反应或显示错误

**测试 API:**
```bash
# 测试 API 是否正常
curl "http://localhost:40200/video/share/url/parse?url=https://v.douyin.com/test"
```

**检查响应:**
```json
{
  "code": 200,
  "msg": "解析成功",
  "data": { ... }
}
```

或错误响应:
```json
{
  "code": 500,
  "msg": "错误信息"
}
```

## 调试技巧

### 1. 启用详细日志
修改 `application.yml`:
```yaml
logging:
  level:
    com.ideaflow.parseshortvideo: DEBUG
    org.springframework.web: DEBUG
    org.thymeleaf: DEBUG
```

### 2. 查看模板解析过程
添加日志:
```yaml
logging:
  level:
    org.thymeleaf: TRACE
```

### 3. 测试简化版本
创建一个最简单的测试页面:

**test.html:**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Test</title>
</head>
<body>
    <h1 th:text="${title}">Default Title</h1>
    <p>Current time: <span th:text="${#temporals.format(#temporals.createNow(), 'yyyy-MM-dd HH:mm:ss')}"></span></p>
</body>
</html>
```

**Controller:**
```java
@GetMapping("/test")
public String test(Model model) {
    model.addAttribute("title", "Test Page");
    return "test";
}
```

访问: http://localhost:40200/test

## 完整的启动检查清单

- [ ] Java 21 已安装并配置
- [ ] Maven 依赖已下载
- [ ] 项目编译成功 (`./mvnw clean compile`)
- [ ] HomeController 文件不为空
- [ ] index.html 文件存在于 templates 目录
- [ ] favicon.ico 文件存在于 static 目录
- [ ] application.yml 配置正确
- [ ] 服务启动成功，端口 40200
- [ ] 日志中没有错误信息
- [ ] 浏览器可以访问 http://localhost:40200
- [ ] 浏览器控制台没有 JavaScript 错误
- [ ] API 接口可以正常调用

## 如果仍然有问题

1. **重新编译:**
   ```bash
   ./mvnw clean compile
   ```

2. **清理 IDE 缓存:**
   - IntelliJ IDEA: File → Invalidate Caches / Restart

3. **检查端口占用:**
   ```bash
   lsof -i :40200
   # 如果被占用，修改 application.yml 中的端口
   ```

4. **查看完整日志:**
   ```bash
   ./mvnw spring-boot:run > app.log 2>&1
   cat app.log
   ```

5. **提供错误信息:**
   - 浏览器显示的错误页面截图
   - 浏览器控制台的错误信息
   - 服务器日志中的错误堆栈

## 当前状态

✅ **已修复:**
- HomeController 已重新创建
- HTML 模板已添加 Thymeleaf 命名空间
- 静态资源路径已使用 Thymeleaf 语法

🔄 **请执行:**
1. 重新编译项目: `./mvnw clean compile`
2. 启动服务: `./mvnw spring-boot:run`
3. 访问页面: http://localhost:40200
4. 如果有问题，查看日志并反馈具体错误信息

---

**提示:** 如果页面能打开但功能不正常，很可能是 API 调用的问题，而不是 Thymeleaf 的问题。请提供具体的错误信息以便进一步诊断。

