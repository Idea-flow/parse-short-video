# /video/share/url/parse 使用说明

该接口用于解析短视频平台的分享文本/链接，提取真实视频信息（无水印视频地址、封面、标题、作者、图集等）。

- 请求方法：GET
- 路径：`/video/share/url/parse`
- 查询参数：
  - `url` 必填，包含分享链接的文本或链接。接口内部会自动从文本中提取第一个有效的 URL。

## 请求示例

- 直接传链接：

```
curl "http://localhost:40202/video/share/url/parse?url=https://v.douyin.com/xxxxxx/"
```

- 传分享文本（包含链接）：

```
curl --get "http://localhost:40202/video/share/url/parse" \
  --data-urlencode "url=复制此链接，打开【抖音短视频】，https://v.douyin.com/xxxxxx/ 观看精彩内容！"
```

接口会用正则 `http[s]?://...` 自动提取链接，无需手动截取。

## 成功响应

成功时返回统一响应结构 `ApiResponse<VideoInfo>`：

```
{
  "code": 200,
  "msg": "解析成功",
  "data": {
    "videoUrl": "https://...mp4",         // 无水印视频播放地址
    "coverUrl": "https://...jpg",         // 封面图片地址
    "title": "视频标题",
    "musicUrl": "https://...mp3",         // 背景音乐地址（部分平台可能为空）
    "images": [                             // 图集（如小红书笔记图集），无则为空数组
      { "url": "https://...jpg", "livePhotoUrl": "https://...mov" }
    ],
    "author": {                             // 作者信息
      "uid": "作者唯一ID",
      "name": "作者昵称",
      "avatar": "https://...jpg"
    }
  }
}
```

字段来源与含义：
- `videoUrl`：解析后的真实视频地址（如抖音为 mp4 重定向后的直链）。
- `coverUrl`：视频封面图或图集首图。
- `title`：视频/笔记标题或描述。
- `musicUrl`：背景音乐地址（若平台未提供则为空）。
- `images`：仅在图文/图集类内容存在；每项包含图片地址以及可选 Live Photo 视频地址。
- `author`：作者的 id、昵称、头像。

## 失败响应

- 无法从输入文本中提取有效 URL：

```
{
  "code": 400,
  "msg": "无法从输入文本中提取有效URL",
  "data": null
}
```

- 解析过程中发生异常（如不支持的域名、平台接口变化、网络异常等）：

```
{
  "code": 500,
  "msg": "错误信息",
  "data": null
}
```

## 解析逻辑简述

- 控制器 `VideoParseController.parseShareUrl` 会：
  1. 使用正则从 `url` 文本中提取第一个链接。
  2. 调用 `VideoParseService.parseShareUrl`。
- 服务层 `VideoParseService` 会：
  1. 从链接解析域名 (`URI.getHost`)。
  2. 根据域名映射到具体平台枚举 `VideoSource`（如抖音/小红书）。
  3. 路由到对应解析器（如 `DouYinParser`、`RedBookParser`）。
  4. 返回标准化的 `VideoInfo`。

## 备注

- 当前已实现抖音、小红书平台的分享链接解析；其他平台可按同样模式扩展解析器并在 `VideoParseService` 中注册。
- 接口为 GET，若分享文本较长，建议使用 `--data-urlencode` 进行 URL 编码。

