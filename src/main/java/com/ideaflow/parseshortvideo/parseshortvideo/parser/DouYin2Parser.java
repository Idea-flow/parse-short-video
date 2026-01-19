package com.ideaflow.parseshortvideo.parseshortvideo.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideaflow.parseshortvideo.parseshortvideo.model.ImgInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoAuthor;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.util.UserAgentHelper;
import lombok.Data;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抖音视频解析器
 */
@Component
public class DouYin2Parser extends BaseParser {
    private static final String APP_SHARE_DOMAIN = "v.douyin.com";
    private static final String PC_DOMAIN_1 = "www.iesdouyin.com";
    private static final String PC_DOMAIN_2 = "www.douyin.com";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecureRandom random = new SecureRandom();

    public DouYin2Parser(RestClient restClient, UserAgentHelper userAgentHelper) {
        super(restClient, userAgentHelper);
    }

    @Override
    public VideoInfo parseShareUrl(String shareUrl) throws Exception {
        // 手动跟随最多两次重定向，确保多级跳转的分享链接也能获取到最终HTML
        String html = fetchHtmlWithRedirects(shareUrl, 2, "__ac_nonce=06950e150003bec85b6d9; __ac_signature=_02B4Z6wo00f01OzPKuAAAIDC31rcoCq6.8Ts7y5AAFJo7f;");

        if (html == null || html.isEmpty()) {
            throw new Exception("Failed to fetch video page HTML");
        }

        // 从HTML中提取JSON数据
        Pattern pattern = Pattern.compile("window\\._ROUTER_DATA\\s*=\\s*(.*?)</script>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        if (!matcher.find()) {
            throw new Exception("Failed to parse video json info from html");
        }

        String jsonStr = matcher.group(1).trim();
        JsonNode jsonData = objectMapper.readTree(jsonStr);

        // 解析数据
        JsonNode data = extractVideoData(jsonData);
        if (data == null) {
            throw new Exception("Failed to extract video data from response");
        }

        return buildVideoInfo(data);
    }

    @Override
    public VideoInfo parseVideoId(String videoId) throws Exception {
        String reqUrl = getRequestUrlByVideoId(videoId);
        return parseShareUrl(reqUrl);
    }

    /**
     * 从JSON数据中提取视频数据节点
     */
    private JsonNode extractVideoData(JsonNode jsonData) {
        if (!jsonData.has("loaderData")) {
            return null;
        }

        JsonNode loaderData = jsonData.get("loaderData");

        // 尝试从video_(id)/page路径获取
        for (String key : List.of("video_(id)/page", "note_(id)/page")) {
            if (loaderData.has(key)) {
                JsonNode videoInfoRes = loaderData.get(key).get("videoInfoRes");
                if (videoInfoRes != null && videoInfoRes.has("item_list")) {
                    JsonNode itemList = videoInfoRes.get("item_list");
                    if (itemList.isArray() && itemList.size() > 0) {
                        return itemList.get(0);
                    }
                }
            }
        }

        return null;
    }

    /**
     * 构建VideoInfo对象
     */
    private VideoInfo buildVideoInfo(JsonNode data) {
        // 获取图集图片
        List<ImgInfo> images = new ArrayList<>();
        if (data.has("images") && data.get("images").isArray()) {
            for (JsonNode img : data.get("images")) {
                if (img.has("url_list") && img.get("url_list").isArray()) {
                    String imageUrl = getNoWebpUrl(img.get("url_list"));
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        String livePhotoUrl = "";
                        if (img.has("video") && img.get("video").has("play_addr")
                                && img.get("video").get("play_addr").has("url_list")) {
                            JsonNode urlList = img.get("video").get("play_addr").get("url_list");
                            if (urlList.isArray() && urlList.size() > 0) {
                                livePhotoUrl = urlList.get(0).asText("");
                            }
                        }
                        images.add(ImgInfo.builder()
                                .url(imageUrl)
                                .livePhotoUrl(livePhotoUrl)
                                .build());
                    }
                }
            }
        }

        // 获取视频播放地址
        String videoUrl = "";
        if (data.has("video") && data.get("video").has("play_addr")
                && data.get("video").get("play_addr").has("url_list")) {
            JsonNode urlList = data.get("video").get("play_addr").get("url_list");
            if (urlList.isArray() && urlList.size() > 0) {
                videoUrl = urlList.get(0).asText("").replace("playwm", "play");
            }
        }

        // 如果是图集，视频地址置空
        if (!images.isEmpty()) {
            videoUrl = "";
        }

        // 获取重定向后的视频地址
        String videoMp4Url = "";
        if (!videoUrl.isEmpty()) {
            videoMp4Url = getVideoRedirectUrl(videoUrl);
        }

        // 获取封面图片
        String coverUrl = "";
        if (data.has("video") && data.get("video").has("cover")
                && data.get("video").get("cover").has("url_list")) {
            coverUrl = getNoWebpUrl(data.get("video").get("cover").get("url_list"));
        }

        // 获取作者信息
        VideoAuthor author = VideoAuthor.builder().build();
        if (data.has("author")) {
            JsonNode authorNode = data.get("author");
            String avatarUrl = "";
            if (authorNode.has("avatar_thumb") && authorNode.get("avatar_thumb").has("url_list")) {
                JsonNode urlList = authorNode.get("avatar_thumb").get("url_list");
                if (urlList.isArray() && urlList.size() > 0) {
                    avatarUrl = urlList.get(0).asText("");
                }
            }

            author = VideoAuthor.builder()
                    .uid(authorNode.has("sec_uid") ? authorNode.get("sec_uid").asText("") : "")
                    .name(authorNode.has("nickname") ? authorNode.get("nickname").asText("") : "")
                    .avatar(avatarUrl)
                    .build();
        }

        return VideoInfo.builder()
                .videoUrl(videoMp4Url)
                .coverUrl(coverUrl)
                .title(data.has("desc") ? data.get("desc").asText("") : "")
                .images(images)
                .author(author)
                .build();
    }

    /**
     * 获取视频页面HTML，手动跟随最多maxRedirects次重定向
     */
    private String fetchHtmlWithRedirects(String url, int maxRedirects, String redirectCookie) throws Exception {
        RestClient nonRedirectClient = restClient.mutate()
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()))
                .build();

        URI currentUri = URI.create(url);
        for (int i = 0; i <= maxRedirects; i++) {
            int finalI = i;
            RedirectFetchResult result = nonRedirectClient.get()
                    .uri(currentUri)
                    .headers(httpHeaders -> {
                        getDefaultHeaders().forEach(httpHeaders::add);
                        // 第二次及之后的请求携带指定的Cookie
                        if (redirectCookie != null && !redirectCookie.isEmpty() && finalI > 0) {
                            httpHeaders.add("Cookie", redirectCookie);
                            httpHeaders.add("Host", "www.iesdouyin.com");
                        }
                    })
                    .exchange((request, clientResponse) -> {
                        if (clientResponse.getStatusCode().is3xxRedirection()) {
                            String location = clientResponse.getHeaders().getFirst("Location");
                            return new RedirectFetchResult(null, location, true);
                        }
                        String body = clientResponse.bodyTo(String.class);
                        return new RedirectFetchResult(body, null, false);
                    });

            if (!result.isRedirect) {
                return result.body;
            }

            if (result.location == null || result.location.isEmpty()) {
                throw new Exception("Redirect without Location header");
            }

            currentUri = currentUri.resolve(result.location);
        }

        throw new Exception("Exceeded redirect limit: " + maxRedirects);
    }

    @Data
    public static class RedirectFetchResult {
        public final String body;
        public final String location;
        public final boolean isRedirect;

        public RedirectFetchResult(String body, String location, boolean isRedirect) {
            this.body = body;
            this.location = location;
            this.isRedirect = isRedirect;
        }
    }

    /**
     * 获取视频重定向后的真实地址
     */
    private String getVideoRedirectUrl(String videoUrl) {
        try {
            // 使用RestClient处理重定向，默认会自动跟随重定向
            // 如果需要获取最终的URL，我们需要手动处理
            var response = restClient.get()
                    .uri(videoUrl)
                    .headers(httpHeaders -> getDefaultHeaders().forEach(httpHeaders::add))
                    .exchange((request, clientResponse) -> {
                        // 如果是重定向，获取Location头
                        if (clientResponse.getStatusCode().is3xxRedirection()) {
                            List<String> locations = clientResponse.getHeaders().get("Location");
                            if (locations != null && !locations.isEmpty()) {
                                return locations.get(0);
                            }
                        }
                        // 否则返回原URL
                        return videoUrl;
                    });

            return response != null ? response : videoUrl;
        } catch (Exception e) {
            return videoUrl;
        }
    }

    /**
     * 优先获取非webp格式的图片URL
     */
    private String getNoWebpUrl(JsonNode urlList) {
        if (!urlList.isArray() || urlList.size() == 0) {
            return "";
        }

        // 优先获取非webp格式
        for (JsonNode urlNode : urlList) {
            String url = urlNode.asText("");
            if (!url.isEmpty() && !url.endsWith(".webp")) {
                return url;
            }
        }

        // 没找到就返回第一个
        return urlList.get(0).asText("");
    }

    /**
     * 解析App分享链接
     */
    private String parseAppShareUrl(String shareUrl) {
        try {
            String location = restClient.get()
                    .uri(shareUrl)
                    .headers(httpHeaders -> getDefaultHeaders().forEach(httpHeaders::add))
                    .exchange((request, clientResponse) -> {
                        if (clientResponse.getStatusCode().is3xxRedirection()) {
                            List<String> locations = clientResponse.getHeaders().get("Location");
                            if (locations != null && !locations.isEmpty()) {
                                return locations.get(0);
                            }
                        }
                        return null;
                    });

            if (location == null || location.isEmpty()) {
                return "";
            }

            // 检查是否是西瓜视频链接
            if (location.contains("ixigua.com")) {
                return "";
            }

            return parseVideoIdFromPath(location);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 从URL路径中解析视频ID
     */
    private String parseVideoIdFromPath(String urlPath) {
        try {
            URI uri = new URI(urlPath);

            // 检查query参数中的modal_id
            String query = uri.getQuery();
            if (query != null && query.contains("modal_id=")) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("modal_id=")) {
                        return URLDecoder.decode(param.substring(9), StandardCharsets.UTF_8);
                    }
                }
            }

            // 从路径中提取
            String path = uri.getPath();
            if (path != null && !path.isEmpty()) {
                String[] parts = path.split("/");
                if (parts.length > 0) {
                    return parts[parts.length - 1];
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }

        return "";
    }

    /**
     * 根据视频ID构建请求URL
     */
    private String getRequestUrlByVideoId(String videoId) {
        return "https://www.iesdouyin.com/share/video/" + videoId + "/";
    }

    /**
     * 生成固定长度的随机数字ID
     */
    private String generateFixedLengthNumericId(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成随机字符串
     */
    private String randSeq(int n) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

