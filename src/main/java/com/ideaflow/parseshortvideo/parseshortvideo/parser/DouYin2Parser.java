package com.ideaflow.parseshortvideo.parseshortvideo.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideaflow.parseshortvideo.parseshortvideo.model.ImgInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoAuthor;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.util.JsonArrayExtractor;
import com.ideaflow.parseshortvideo.parseshortvideo.util.UserAgentHelper;
import lombok.Data;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
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
        URI uri = new URI(shareUrl);
        String host = uri.getHost();
        Map<String , String> resultRedirectDataMap = new HashMap<>();
        String videoId;
        if (PC_DOMAIN_1.equals(host) || PC_DOMAIN_2.equals(host)) {
            // PC网页端链接
            videoId = parseVideoIdFromPath(shareUrl);
            if (videoId == null || videoId.isEmpty()) {
                throw new IllegalArgumentException("Failed to parse video ID from PC share URL");
            }
            shareUrl = getRequestUrlByVideoId(videoId);
        } else if (APP_SHARE_DOMAIN.equals(host)) {
            // App分享链接
            videoId = parseAppShareUrl(shareUrl);
//            resultRedirectDataMap = fetchDataWithRedirects(shareUrl,2, null);
//
//            videoId = resultRedirectDataMap.get("typeId");
            if (videoId == null || videoId.isEmpty()) {
                throw new IllegalArgumentException("Failed to parse video ID from app share URL");
            }
            shareUrl = getRequestUrlByVideoId(videoId);
        } else {
            throw new IllegalArgumentException("Douyin not support this host: " + host);
        }
        if (Objects.equals(resultRedirectDataMap.get("type"), "1")) {

            return parseNoteInfoByRedirectData(resultRedirectDataMap);

        }

        // 获取页面HTML
        String html = restClient.get()
                .uri(shareUrl)
                .headers(httpHeaders -> getDefaultHeaders().forEach(httpHeaders::add))
                .retrieve()
                .body(String.class);

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

    private VideoInfo parseNoteInfoByRedirectData(Map<String, String> resultRedirectDataMap) {
        VideoInfo videoInfo = new VideoInfo();
        VideoAuthor author = new VideoAuthor();
        videoInfo.setAuthor(author);

        String htmlData = resultRedirectDataMap.get("htmlData");
        Pattern p = Pattern.compile("self\\.__pace_f\\.push\\(\\s*(\\[.*?\\])\\s*\\)", Pattern.DOTALL);
        Matcher m = p.matcher(htmlData);
        ObjectMapper mapper = new ObjectMapper();
        List<JsonNode> matches = new ArrayList<>();
        while (m.find()) {
            String arrayJson = m.group(1).trim();
            try {
                JsonNode node = mapper.readTree(arrayJson);

                if (!Pattern.compile("\\\\\"awemeId\\\\\":\\\\\"(\\d+)\\\\\"").matcher(node.toString()).find()) {
                    continue;
                }
                matches.add(node);
            } catch (Exception ignore) {
                // 非合法 JSON，跳过
            }
        }
        if (matches.isEmpty()) {
            System.out.println("No matching self.__pace_f.push JSON arrays found.");
        }

        JsonNode first = matches.getFirst();

        List<ImgInfo> imagesResult = new ArrayList<>();
        videoInfo.setImages(imagesResult);


        // 检查first.get(1)是否为格式为"数字: []"的字符串
        if (first.get(1).isTextual()) {
            String inputStr = first.get(1).asText();
            // 使用JsonArrayExtractor解析格式为"数字: []"的字符串
            JsonNode extractedArray = JsonArrayExtractor.extractJsonArray(inputStr);
            if (extractedArray != null && extractedArray.isArray()) {
//                System.out.println("Extracted JSON array:");
                for (JsonNode jsonNode : extractedArray) {
                    if (jsonNode.has("awemeId") && jsonNode.has("aweme")) { //找到了想要的json数据
                        JsonNode awemeJsonNode = jsonNode.get("aweme");
                        JsonNode detailJsonNode = awemeJsonNode.get("detail");
                        JsonNode imagesJsonNode = detailJsonNode.get("images");
                        JsonNode authorInfosonNode = detailJsonNode.get("authorInfo");
                        if (imagesJsonNode != null){
                            author.setAvatar(authorInfosonNode.get("avatarUri").asText());
                            author.setName(authorInfosonNode.get("nickname").asText());
                            author.setUid(authorInfosonNode.get("uid").asText());
                        }

                        for (JsonNode imageNode : imagesJsonNode) {
                            JsonNode videoNode = imageNode.get("video");
                            // 1. 图文的video 是空
                            if (videoNode == null || videoNode.isNull()){ //存图文
                                JsonNode urlListJsonNode = imageNode.get("urlList");
                                // 检查 urlListJsonNode 是否为数组且不为空
                                if (urlListJsonNode != null && urlListJsonNode.isArray() && urlListJsonNode.size() > 0) {
                                    // 获取数组最后一个元素
                                    JsonNode lastElement = urlListJsonNode.get(urlListJsonNode.size() - 1);
                                    // 转换为字符串，使用asText()避免转义，然后解码URL中的编码字符
                                    String lastValue = lastElement.asText();

                                    imagesResult.add(new ImgInfo(lastValue,""));
                                }
                            }else {
                                JsonNode videoPlayAddrNode = videoNode.get("playAddr");
                                JsonNode cover = videoNode.get("cover");
                                // 检查 videoPlayAddrNode 是否为数组且不为空
                                if (videoPlayAddrNode != null && videoPlayAddrNode.isArray() && videoPlayAddrNode.size() > 0) {
                                    // 获取数组最后一个元素
                                    JsonNode lastElement = videoPlayAddrNode.get(videoPlayAddrNode.size() - 1);
                                    // 转换为字符串，使用asText()避免转义，然后解码URL中的编码字符
                                    String lastValue = lastElement.get("src").asText();

                                    imagesResult.add(new ImgInfo(cover.asText(), lastValue));
                                }
                            }


                        }
                    }
                }
            } else {
                System.out.println("Failed to extract JSON array or extracted content is not an array");
                // 如果不是预期格式，仍然按原有逻辑处理
                for (JsonNode jsonNode : first.get(1)) {
                    System.out.println(jsonNode.get("awemeId").asText());
                }
            }
        }

        return  videoInfo;

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
                        // 第二次及之后的请求携带指定的Cookie
                        if (finalI > 0) {
                            httpHeaders.add("Cookie", "__ac_nonce=06950e150003bec85b6d9; __ac_signature=_02B4Z6wo00f01OzPKuAAAIDC31rcoCq6.8Ts7y5AAFJo7f");
                            httpHeaders.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");
                            httpHeaders.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
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

    /**
     * 获取视频页面HTML，手动跟随最多maxRedirects次重定向
     */
    private Map<String,String> fetchDataWithRedirects(String url, int maxRedirects, String redirectCookie) throws Exception {
        Map<String,String> resultMapData = new HashMap<>();
        resultMapData.put("htmlData",null);
        resultMapData.put("type","0"); //0:默认 1:node 2 视频
        resultMapData.put("typeId",""); //0 默认 1 node 2 视频

        RestClient nonRedirectClient = restClient.mutate()
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()))
                .build();

        URI currentUri = URI.create(url);
        for (int i = 0; i <= maxRedirects; i++) {
            int finalI = i;
            URI finalCurrentUri = currentUri;
            DouYin2Parser.RedirectFetchResult result = nonRedirectClient.get()
                    .uri(currentUri)
                    .headers(httpHeaders -> {
                        // 第二次及之后的请求携带指定的Cookie
                        if (finalI > 0) {
                            httpHeaders.add("Cookie", "__ac_nonce=06950e150003bec85b6d9; __ac_signature=_02B4Z6wo00f01OzPKuAAAIDC31rcoCq6.8Ts7y5AAFJo7f");
                            httpHeaders.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");
                            httpHeaders.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
                        }
                    })
                    .exchange((request, clientResponse) -> {
                        if (clientResponse.getStatusCode().is3xxRedirection()) {
                            String location = clientResponse.getHeaders().getFirst("Location");
                            return new DouYin2Parser.RedirectFetchResult(null, location, true);
                        }
                        String path = finalCurrentUri.getPath();
                        if (path != null && path.contains("/note/")){
                            resultMapData.put("type", "1");
                            String typeId = path.substring(path.lastIndexOf('/') + 1);
                            resultMapData.put("typeId", typeId);
                        }
                        if (path != null && path.contains("/video/")){
                            resultMapData.put("type", "2");
                            String typeId = path.substring(path.lastIndexOf('/') + 1);
                            resultMapData.put("typeId", typeId);
                        }


                        String body = clientResponse.bodyTo(String.class);
                        return new DouYin2Parser.RedirectFetchResult(body, null, false);
                    });

            if (!result.isRedirect) {
                resultMapData.put("htmlData",result.body);
                return resultMapData;
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

