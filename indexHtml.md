# 页面逻辑说明 - `src/main/resources/templates/index.html`

该页面是一个使用 Vue 3 构建的前端单页，负责输入分享链接、调用后端解析接口，并展示无水印视频/图集与作者信息，支持深色模式、日志输出和文件下载。

## 技术栈与资源
- Tailwind CSS（CDN）负责样式与暗色主题变量。
- Vue 3（CDN，全局构建）负责交互逻辑与响应式视图。
- Phosphor Icons（CDN）用于图标。
- Microsoft Clarity（埋点脚本）。

## 主要交互与状态
- 输入框 `inputUrl`：用户粘贴分享文本或链接。
- 解析按钮：触发 `handleParse()` 调用后端。
- 状态：`loading` 加载、`error` 错误提示、`result` 原始响应、`data` 解析后的展示数据。
- 主题切换：`isDark` + `toggleTheme(event)`，支持 View Transition 动画。
- 日志：`showLogs` 开关与 `logs` 列表，通过 `addLog(msg)` 在解析和下载过程中记录。
- 下载进度：`downloading` 与 `downloadProgress`，仅在下载视频时显示进度条。

## 视图结构概览
- 顶部栏：标题、帮助按钮、日志开关、主题切换。
- 输入区：链接文本输入 + 解析按钮（Enter 也可触发）。
- 错误提示：`error` 非空时显示。
- 结果区：当 `data` 存在时，按模块展示：
  - 作者信息卡：头像、昵称、UID。
  - 标题与封面：封面支持悬浮下载按钮。
  - 视频模块：`<video>` 播放 + 悬浮一键下载。
  - 图集模块：图片或 Live Photo 视频预览，提供单项下载按钮。
- 空状态：未解析时展示提示文案。
- 弹窗：使用说明（支持平台与提示）。
- 下载模态：视频下载进度展示。
- 日志面板：右下角浮层实时输出日志。

## 核心逻辑

### 1) 提取与调用解析接口
- 正则提取分享链接：
  - `extractUrl(text)` 使用正则 `http[s]?://[\w.-]+[\w/-]*[\w.-]*\??[\w=&:\-+%]*[/]*` 获取第一个 URL。
- 触发解析：
  - `handleParse()`：
    1. 校验输入并调用 `extractUrl`。
    2. 将链接 `encodeURIComponent` 并请求 `/video/share/url/parse?url=...`。
    3. 校验 `response.ok` 与 `json.code===200`，否则抛错到 `error`。
    4. 将返回数据中的字段由驼峰转为下划线以适配页面：
       - `coverUrl -> cover_url`
       - `musicUrl -> music_url`
       - `videoUrl -> video_url`
       - `images[].livePhotoUrl -> images[].live_photo_url`
    5. 保存到 `result`，由 `computed data` 暴露 `result.data` 给视图。

### 2) 主题切换与过渡动画
- 默认遵循系统暗色设置（`prefers-color-scheme: dark`）。
- `toggleTheme(event)` 若支持 View Transition，执行圆形范围过渡动画；否则直接切换 `document.documentElement.classList`。

### 3) 下载逻辑
- `downloadFile(url, filename)`：
  - 强制将 `http://` 升级为 `https://` 并记录日志。
  - 判断是否为视频文件（扩展名 `.mp4`），视频下载时显示进度 `downloading` 与 `downloadProgress`。
  - 通过 `fetch` 读取 `ReadableStream` 计算进度，合并为 `Blob`，推断扩展名（jpg/png/webp/mp4/gif）。
  - 通过临时 `a` 标签触发浏览器下载，随后 `URL.revokeObjectURL` 清理。
  - 异常处理：
    - iOS 设备：回退到通过 `a` 标签在新窗口打开原链接。
    - 非 iOS：`window.open(url, '_blank')` 回退。
- `downloadLivePhoto(imgUrl, videoUrl, index)`：
  - 为 Live Photo 只下载视频部分，命名为 `live_photo_{index}_{random}.mp4`。

### 4) 辅助与计算属性
- `generateFileName()`：生成随机 6 位字符串用于下载文件命名。
- `data`（computed）：返回 `result?.data` 并在日志中输出变更。
- `hasLivePhoto`（computed）：判断图集中是否存在 `live_photo_url`。
- URL 参数：`showLog=true/1` 可开启日志面板。

## 与后端的约定
- 接口：`GET /video/share/url/parse?url={encoded}`。
- 成功响应结构：`{ code:200, msg:'解析成功', data: VideoInfo }`。
- 失败响应结构：`{ code:400/500, msg:'错误信息', data:null }`。
- 页面对字段进行驼峰到下划线转换以统一前端展示键名。

## 关键可见字段（前端展示用）
- `data.author = { uid, name, avatar }`
- `data.title`
- `data.cover_url`
- `data.video_url`
- `data.images = [{ url, live_photo_url? }]`

## 可扩展点
- 支持更多平台：后端增加解析器并在服务层注册即可复用前端。
- 进度与断点续传：可基于 `ReadableStream` 与 Range 请求增强体验。
- 错误友好化：根据常见错误类型调整提示语与建议操作。
- 主题持久化：通过 localStorage 记忆用户选择。

## 媒体展示与交互细节
- **封面**：当 `cover_url` 存在时展示居中预览，悬浮显示“下载封面”按钮，点击后调用 `downloadFile(cover_url, 'cover_' + generateFileName())`。
- **视频**：当 `video_url` 存在时展示 `<video controls>`，悬浮右上角出现一键下载按钮，文件名模式 `video_{random}.mp4`，下载时显示进度模态并基于 `ReadableStream` 更新 `downloadProgress`。
- **图集（图片）**：`images` 数组中无 `live_photo_url` 时按瀑布流网格展示静态图，悬浮出现“下载图片”按钮，调用 `downloadFile(img.url, 'image_{index}_{random}')`。
- **图集（Live Photo）**：当 `live_photo_url` 存在时使用 `<video autoplay muted loop>` 预览，并标注 `LIVE` 角标；悬浮显示“下载实况”按钮，触发 `downloadLivePhoto(img.url, img.live_photo_url, index)`（实际下载视频流，命名 `live_photo_{index}_{random}.mp4`）。
- **下载容错**：`downloadFile` 会将 `http` 升级为 `https`，根据 `Content-Type` 推断扩展名；失败时 iOS 回退到新窗口打开链接，其他平台用 `window.open` 回退；视频下载结束后自动隐藏进度。
- **日志联动**：如果开启 `showLogs`，下载和解析关键步骤会写入 `logs` 浮层，便于排查下载/解析问题。
