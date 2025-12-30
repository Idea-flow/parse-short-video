# 🧹 Docker 测试脚本资源管理说明

## ✅ 改进完成

`docker-test.sh` 脚本现在**只管理它自己创建的资源**，不会影响您的其他 Docker 容器和镜像！

---

## 📦 脚本管理的资源

### docker-test.sh 只会创建和清理：

| 资源类型 | 名称 | 说明 |
|---------|------|------|
| 容器 | `parse-video-test` | 测试容器 |
| 镜像 | `parse-short-video:test` | 测试镜像 |

**其他所有 Docker 资源都不会被触碰！** ✅

---

## 🔍 改进细节

### 1. 精确的容器清理
```bash
# 只查找名为 parse-video-test 的容器
docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"
```

### 2. 精确的镜像清理
```bash
# 只查找 parse-short-video:test 镜像
docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^${IMAGE_NAME}:${IMAGE_TAG}$"
```

### 3. 安全的删除逻辑
- 使用 `-f` (force) 选项确保删除成功
- 添加 `|| true` 避免因资源不存在而报错
- 添加超时机制防止卡住

---

## 📝 使用示例

### 运行测试脚本
```bash
./docker-test.sh
```

**输出示例**：
```
[1/6] 清理本脚本创建的旧容器和镜像...
检查容器: parse-video-test
  未发现旧容器 parse-video-test
检查镜像: parse-short-video:test
  发现旧镜像 parse-short-video:test，正在清理...
  ✓ 镜像 parse-short-video:test 已清理
✓ 清理完成（仅清理本脚本创建的资源）
```

---

## 🗑️ 手动清理资源

### 方式1：使用专用清理脚本（推荐）

```bash
./docker-cleanup.sh
```

这个脚本会：
- ✅ 询问确认
- ✅ 只删除测试脚本创建的资源
- ✅ 显示清理结果
- ✅ 验证其他资源未受影响

### 方式2：手动命令

```bash
# 停止并删除容器
docker stop parse-video-test 2>/dev/null || true
docker rm parse-video-test 2>/dev/null || true

# 删除镜像
docker rmi parse-short-video:test 2>/dev/null || true
```

---

## 🔐 安全保证

### 脚本不会：
- ❌ 删除其他容器（如生产容器、开发容器等）
- ❌ 删除其他镜像（如 nginx、mysql、redis 等）
- ❌ 清理 Docker 卷（volumes）
- ❌ 清理 Docker 网络（networks）
- ❌ 影响正在运行的服务

### 脚本只会：
- ✅ 创建容器 `parse-video-test`
- ✅ 创建镜像 `parse-short-video:test`
- ✅ 在重新运行时清理上述资源
- ✅ 保持其他所有资源不变

---

## 📊 验证示例

### 运行前的 Docker 状态
```bash
$ docker ps -a
CONTAINER ID   IMAGE          COMMAND       NAMES
abc123def      mysql:latest   ...           my-mysql
def456ghi      redis:latest   ...           my-redis
```

### 运行 docker-test.sh 后
```bash
$ docker ps -a
CONTAINER ID   IMAGE                      COMMAND       NAMES
xyz789abc      parse-short-video:test     ...           parse-video-test  ← 新增
abc123def      mysql:latest               ...           my-mysql          ← 保留
def456ghi      redis:latest               ...           my-redis          ← 保留
```

**看到了吗？** 您的 mysql 和 redis 容器完全不受影响！ ✅

---

## 🆚 与其他清理命令的对比

| 命令 | 影响范围 | 安全性 |
|------|----------|--------|
| `docker system prune -a` | 删除**所有**未使用的资源 | ⚠️ 危险 |
| `docker rm $(docker ps -aq)` | 删除**所有**容器 | ❌ 非常危险 |
| `docker rmi $(docker images -q)` | 删除**所有**镜像 | ❌ 非常危险 |
| `./docker-test.sh` | 只删除测试脚本的资源 | ✅ 安全 |
| `./docker-cleanup.sh` | 只删除测试脚本的资源 | ✅ 安全 |

---

## 💡 最佳实践

### 1. 运行测试前
```bash
# 查看当前容器
docker ps -a

# 运行测试
./docker-test.sh

# 验证其他容器未受影响
docker ps -a
```

### 2. 测试完成后
```bash
# 选项A: 保留测试容器继续使用
# 不做任何操作，容器会继续运行

# 选项B: 清理测试资源
./docker-cleanup.sh
```

### 3. 多次测试
```bash
# 每次运行测试脚本都会自动清理上次的测试资源
./docker-test.sh  # 第一次
./docker-test.sh  # 第二次（自动清理第一次的资源）
./docker-test.sh  # 第三次（自动清理第二次的资源）
```

---

## 📁 相关文件

| 文件 | 用途 |
|------|------|
| `docker-test.sh` | 构建和测试 Docker 镜像（只管理自己的资源） |
| `docker-cleanup.sh` | 清理测试脚本创建的资源 |
| `docker-diagnose.sh` | 诊断 Docker 环境 |

---

## 🎯 总结

✅ **改进完成**：`docker-test.sh` 现在是一个**安全的、隔离的**测试脚本

✅ **资源隔离**：只管理特定名称的容器和镜像

✅ **其他资源安全**：您的生产容器、开发环境、数据库等完全不受影响

✅ **可重复运行**：多次运行脚本不会产生冲突

✅ **易于清理**：提供专门的清理脚本

---

**现在您可以放心地使用测试脚本了！** 🎉

