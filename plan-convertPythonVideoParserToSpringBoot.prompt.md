# Plan: 将Python视频解析服务转换为Spring Boot应用

将现有的Python FastAPI视频解析服务转换为基于Spring Boot 3.5.9的Java应用，实现抖音和小红书平台的无水印视频解析功能。使用RestClient进行HTTP请求，采用清晰的分层架构（Controller-Service-Parser），确保代码结构易于维护和扩展。

## Steps

1. **创建核心数据模型类** - 在 `src/main/java/.../model/` 包中创建 `VideoInfo`、`VideoAuthor`、`ImgInfo`、`VideoSource` 枚举等Java数据类，使用Lombok注解简化代码，对应Python的dataclass结构

2. **实现解析器基础架构** - 在 `src/main/java/.../parser/` 包中创建 `BaseParser` 抽象类和 `DouYinParser`、`RedBookParser` 实现类，封装HTTP请求逻辑、正则表达式解析、JSON数据提取等核心功能

3. **开发Service层服务** - 在 `src/main/java/.../service/` 包中创建 `VideoParseService`，实现URL域名识别、解析器路由选择、异常处理统一封装等业务逻辑

4. **实现REST API控制器** - 在 `src/main/java/.../controller/` 包中创建 `VideoParseController`，提供 `/video/share/url/parse` 和 `/video/id/parse` 两个GET接口，返回统一JSON响应格式

5. **配置RestClient和依赖** - 在 `pom.xml` 添加Jackson YAML、SnakeYAML等依赖，在配置类中定义RestClient Bean，设置User-Agent随机生成、超时时间、重定向策略等

6. **添加前端页面支持** - 将Python项目的 `templates/index.html` 复制到 `src/main/resources/templates/`，调整API调用路径适配Spring Boot的URL结构，确保前端页面可正常访问

## Further Considerations

1. **User-Agent生成策略** - Python使用fake_useragent库动态生成，Java需要自建User-Agent池或引入第三方库（如user-agent-utils），建议创建UserAgentHelper工具类维护常用UA列表

2. **异步处理方式** - Python使用async/await异步编程，Java可使用CompletableFuture或保持同步调用（RestClient默认同步），考虑并发场景是否需要异步优化

3. **认证机制实现** - Python使用Basic Auth（通过环境变量配置），Java可用Spring Security或自定义Filter/Interceptor实现，是否需要保留认证功能？

4. **抖音a_bogus参数生成** - Python代码中使用随机字符串生成a_bogus，实际抖音API可能需要正确的签名算法，当前随机方案可能失效，需验证接口可用性

## 详细实现步骤

### 步骤1: 创建核心数据模型类

#### 1.1 创建 VideoSource 枚举
```java
package com.ideaflow.parseshortvideo.parseshortvideo.model;

public enum VideoSource {
    DOUYIN("douyin", "抖音"),
    KUAISHOU("kuaishou", "快手"),
    REDBOOK("redbook", "小红书"),
    BILIBILI("bilibili", "哔哩哔哩"),
    WEIBO("weibo", "微博");
    
    private final String code;
    private final String name;
    
    // Constructor, getters, fromCode method
}
```

#### 1.2 创建 VideoAuthor 类
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoAuthor {
    private String uid;
    private String name;
    private String avatar;
}
```

#### 1.3 创建 ImgInfo 类
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImgInfo {
    private String url;
    private String livePhotoUrl;
}
```

#### 1.4 创建 VideoInfo 类
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoInfo {
    private String videoUrl;
    private String coverUrl;
    private String title;
    private String musicUrl;
    private List<ImgInfo> images;
    private VideoAuthor author;
}
```

### 步骤2: 实现解析器基础架构

#### 2.1 创建 BaseParser 抽象类
```java
public abstract class BaseParser {
    protected final RestClient restClient;
    protected final UserAgentHelper userAgentHelper;
    
    public abstract VideoInfo parseShareUrl(String shareUrl) throws Exception;
    public abstract VideoInfo parseVideoId(String videoId) throws Exception;
    
    protected Map<String, String> getDefaultHeaders() {
        // 返回包含User-Agent的默认headers
    }
}
```

#### 2.2 实现 DouYinParser
- 支持多种URL格式：v.douyin.com, www.iesdouyin.com, www.douyin.com
- 解析app分享链接，处理重定向
- 从HTML中提取JSON数据（window._ROUTER_DATA）
- 区分视频和图集内容
- 调用slidesinfo API获取图集Live Photo信息
- 处理视频URL的重定向获取真实播放地址
- 生成web_id和a_bogus参数

#### 2.3 实现 RedBookParser
- 使用Windows User-Agent
- 从HTML中提取JSON数据（window.__INITIAL_STATE__）
- 使用YAML解析器处理JSON数据
- 处理图片域名替换（去水印）
- 支持Live Photo视频地址提取
- 验证note_id有效性

### 步骤3: 开发Service层服务

#### 3.1 创建 VideoParseService
```java
@Service
public class VideoParseService {
    private final Map<VideoSource, BaseParser> parserMap;
    private final Map<String, VideoSource> domainSourceMap;
    
    public VideoInfo parseShareUrl(String shareUrl) throws Exception {
        // 1. 从URL中提取域名
        // 2. 根据域名识别VideoSource
        // 3. 获取对应的Parser
        // 4. 调用parser.parseShareUrl()
        // 5. 返回VideoInfo
    }
    
    public VideoInfo parseVideoId(VideoSource source, String videoId) throws Exception {
        // 1. 获取对应的Parser
        // 2. 调用parser.parseVideoId()
        // 3. 返回VideoInfo
    }
}
```

#### 3.2 初始化域名映射关系
```java
@PostConstruct
public void initDomainMapping() {
    domainSourceMap.put("v.douyin.com", VideoSource.DOUYIN);
    domainSourceMap.put("www.iesdouyin.com", VideoSource.DOUYIN);
    domainSourceMap.put("www.douyin.com", VideoSource.DOUYIN);
    domainSourceMap.put("www.xiaohongshu.com", VideoSource.REDBOOK);
    domainSourceMap.put("xhslink.com", VideoSource.REDBOOK);
    // ... 其他域名映射
}
```

### 步骤4: 实现REST API控制器

#### 4.1 创建 VideoParseController
```java
@RestController
@RequestMapping("/video")
public class VideoParseController {
    
    @GetMapping("/share/url/parse")
    public ApiResponse<VideoInfo> parseShareUrl(@RequestParam String url) {
        try {
            // 提取URL
            String videoShareUrl = extractUrl(url);
            VideoInfo videoInfo = videoParseService.parseShareUrl(videoShareUrl);
            return ApiResponse.success(videoInfo);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
    
    @GetMapping("/id/parse")
    public ApiResponse<VideoInfo> parseVideoId(
        @RequestParam VideoSource source,
        @RequestParam String videoId) {
        try {
            VideoInfo videoInfo = videoParseService.parseVideoId(source, videoId);
            return ApiResponse.success(videoInfo);
        } catch (Exception e) {
            return ApiResponse.error(500, e.getMessage());
        }
    }
}
```

#### 4.2 创建统一响应类
```java
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "解析成功", data);
    }
    
    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}
```

### 步骤5: 配置RestClient和依赖

#### 5.1 更新 pom.xml
```xml
<!-- SnakeYAML for parsing YAML-like JSON -->
<dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
</dependency>

<!-- Jackson for JSON processing -->
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
</dependency>

<!-- Jsoup for HTML parsing (optional) -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
```

#### 5.2 创建 RestClient 配置类
```java
@Configuration
public class RestClientConfig {
    
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
            .requestFactory(new JdkClientHttpRequestFactory())
            .build();
    }
    
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
```

#### 5.3 创建 UserAgentHelper 工具类
```java
@Component
public class UserAgentHelper {
    private final List<String> iosUserAgents = List.of(
        "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X)...",
        // ... 更多iOS UA
    );
    
    private final List<String> windowsUserAgents = List.of(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...",
        // ... 更多Windows UA
    );
    
    private final Random random = new Random();
    
    public String getRandomIosUserAgent() {
        return iosUserAgents.get(random.nextInt(iosUserAgents.size()));
    }
    
    public String getRandomWindowsUserAgent() {
        return windowsUserAgents.get(random.nextInt(windowsUserAgents.size()));
    }
}
```

### 步骤6: 添加前端页面支持

#### 6.1 创建首页控制器
```java
@Controller
public class HomeController {
    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "无水印解析");
        return "index";
    }
}
```

#### 6.2 复制并调整前端页面
- 复制 `parse-video-py-main-my/templates/index.html` 到 `src/main/resources/templates/`
- 复制 `parse-video-py-main-my/static/favicon.ico` 到 `src/main/resources/static/`
- 调整JavaScript中的API调用路径（保持不变，已经是 `/video/share/url/parse`）
- 确保Thymeleaf模板引擎正常工作

#### 6.3 添加静态资源配置
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
```

## 技术细节

### 抖音解析关键点

1. **URL格式识别**
   - App分享链接：`https://v.douyin.com/xxxxxx` → 需要跟踪重定向
   - PC网页端：`https://www.douyin.com/video/{id}` → 直接解析
   - 精选页面：`https://www.douyin.com/jingxuan?modal_id={id}` → 从query参数提取

2. **JSON数据提取**
   - 正则表达式：`window\._ROUTER_DATA\s*=\s*(.*?)</script>`
   - 数据结构：`loaderData.video_(id)/page.videoInfoRes.item_list[0]`
   - 图集数据：`loaderData.note_(id)/page.videoInfoRes.item_list[0]`

3. **图集处理**
   - 检查canonical URL是否包含 `/note/`
   - 调用slidesinfo API：`https://www.iesdouyin.com/web/api/v2/aweme/slidesinfo/`
   - 参数：web_id（15位随机数字）、a_bogus（64位随机字符串）

4. **视频URL处理**
   - 原始URL包含 `playwm`（有水印）→ 替换为 `play`（无水印）
   - 需要跟踪重定向获取最终的MP4地址
   - 图集时视频URL为空

### 小红书解析关键点

1. **User-Agent要求**
   - 必须使用Windows User-Agent
   - 推荐：`Mozilla/5.0 (Windows NT 10.0; Win64; x64) ...`

2. **JSON数据提取**
   - 正则表达式：`window\.__INITIAL_STATE__\s*=\s*(.*?)</script>`
   - 使用YAML解析器而非JSON解析器（因为格式特殊）
   - 数据路径：`note.noteDetailMap[{note_id}].note`

3. **图片去水印**
   - 原图URL：`https://sns-img-*.xhscdn.com/...`
   - 无水印URL：`https://ci.xiaohongshu.com/notes_pre_post/{image_id}?imageView2/format/jpg`
   - 注意：仅当原URL包含 `notes_pre_post` 时才能替换域名

4. **Live Photo处理**
   - 检查 `img_item.livePhoto` 字段
   - 提取 `img_item.stream.h264[0].masterUrl`

## 测试建议

1. **单元测试**
   - 测试各个Parser的URL解析逻辑
   - 测试JSON数据提取正则表达式
   - 测试域名识别和路由选择

2. **集成测试**
   - 使用真实的分享链接测试完整流程
   - 验证视频URL的可访问性
   - 测试异常情况处理

3. **测试用例**
   - 抖音视频：`https://v.douyin.com/xxxxx`
   - 抖音图集：包含 `/note/` 路径的URL
   - 小红书视频：`https://www.xiaohongshu.com/explore/xxxxx`
   - 小红书图集：多图笔记链接
   - 过期链接：验证错误处理

## 潜在问题和解决方案

1. **抖音API签名问题**
   - 问题：a_bogus参数可能需要正确的签名算法
   - 解决：先用随机字符串测试，失效后研究JS逆向或使用第三方签名服务

2. **平台反爬虫策略**
   - 问题：频繁请求可能被限流或封禁
   - 解决：添加请求间隔、IP代理池、User-Agent轮换

3. **JSON数据结构变化**
   - 问题：平台更新可能导致数据结构改变
   - 解决：添加多版本兼容处理、详细的错误日志

4. **重定向处理**
   - 问题：某些平台的短链接重定向次数多
   - 解决：配置RestClient支持自动重定向，设置合理的最大重定向次数

5. **图片域名有效性**
   - 问题：小红书替换域名的策略可能失效
   - 解决：先尝试无水印域名，失败后回退到原始URL

## 项目结构

```
src/main/java/com/ideaflow/parseshortvideo/parseshortvideo/
├── ParseShortVideoApplication.java
├── controller/
│   ├── VideoParseController.java
│   └── HomeController.java
├── service/
│   └── VideoParseService.java
├── parser/
│   ├── BaseParser.java
│   ├── DouYinParser.java
│   └── RedBookParser.java
├── model/
│   ├── VideoSource.java
│   ├── VideoInfo.java
│   ├── VideoAuthor.java
│   ├── ImgInfo.java
│   └── ApiResponse.java
├── config/
│   ├── RestClientConfig.java
│   └── WebConfig.java
└── util/
    └── UserAgentHelper.java

src/main/resources/
├── application.yml
├── templates/
│   └── index.html
└── static/
    └── favicon.ico
```

## 配置文件更新

### application.yml
```yaml
server:
  port: 40200

spring:
  application:
    name: parse-short-video
  thymeleaf:
    cache: false
    prefix: classpath:/templates/
    suffix: .html

# RestClient配置
rest-client:
  connect-timeout: 10000
  read-timeout: 30000
  max-redirects: 10

# 日志配置
logging:
  level:
    com.ideaflow.parseshortvideo: DEBUG
    org.springframework.web: INFO
```

## 实施优先级

### 第一阶段（核心功能）
1. 创建所有数据模型类
2. 实现DouYinParser（仅支持基础视频解析）
3. 实现RedBookParser（仅支持基础视频解析）
4. 创建VideoParseService和Controller
5. 基本测试验证

### 第二阶段（完善功能）
1. 添加抖音图集支持
2. 添加小红书图集和Live Photo支持
3. 完善错误处理和日志
4. 添加前端页面
5. 完整测试

### 第三阶段（优化增强）
1. 添加更多平台支持（快手、B站等）
2. 实现请求缓存
3. 添加监控和统计
4. 性能优化
5. 部署文档

## 注意事项

1. **遵守法律法规**：视频解析功能仅供学习研究使用，不得用于商业目的或侵犯版权

2. **尊重平台规则**：控制请求频率，避免对平台服务造成影响

3. **数据安全**：不存储用户数据和视频内容，仅提供解析服务

4. **及时更新维护**：平台API和页面结构可能随时变化，需要持续维护更新

5. **错误处理**：提供友好的错误提示，帮助用户理解解析失败的原因

