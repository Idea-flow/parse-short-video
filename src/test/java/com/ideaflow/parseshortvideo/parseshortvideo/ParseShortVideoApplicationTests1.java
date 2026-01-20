package com.ideaflow.parseshortvideo.parseshortvideo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideaflow.parseshortvideo.parseshortvideo.model.ImgInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.parser.DouYin2Parser;
import com.ideaflow.parseshortvideo.parseshortvideo.parser.RedBookParser;
import com.ideaflow.parseshortvideo.parseshortvideo.util.JsonArrayExtractor;
import jakarta.annotation.Resource;
import net.minidev.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
class ParseShortVideoApplicationTests1 {

    private final RestClient restClient = RestClient.create();

    @Resource
    RedBookParser redBookParser;

    @Test
    void contextLoads() {

    }

    @Test
    void testRestClientGet() {
        // GET 请求示例
        String url = "https://www.douyin.com/note/7335767155465588006";

        // 简单的 GET 请求，附带Host与Cookie
        String response = restClient.get()
            .uri(url)
            .headers(httpHeaders -> {
                httpHeaders.add("Host", "www.douyin.com");
                httpHeaders.add("Cookie", "__ac_nonce=06950e150003bec85b6d9; __ac_signature=_02B4Z6wo00f01OzPKuAAAIDC31rcoCq6.8Ts7y5AAFJo7f;");
            })
            .retrieve()
            .body(String.class);

        System.out.println("GET Response: " + response);
//
//        // 带参数的 GET 请求
//        String responseWithParams = restClient.get()
//            .uri(uriBuilder -> uriBuilder
//                .path("https://jsonplaceholder.typicode.com/posts")
//                .queryParam("userId", 1)
//                .build())
//            .retrieve()
//            .body(String.class);
//
//        System.out.println("GET with Params Response: " + responseWithParams);
    }

    @Test
    void testRestClientPost() {
//        // POST 请求示例
//        String url = "https://jsonplaceholder.typicode.com/posts";
//
//        // 准备请求体
//        String jsonBody = "{\n" +
//            "  \"title\": \"foo\",\n" +
//            "  \"body\": \"bar\",\n" +
//            "  \"userId\": 1\n" +
//            "}";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Content-Type", "application/json");
//
//        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
//
//        // 执行 POST 请求
//        String response = restClient.post()
//            .uri(url)
//            .headers(httpHeaders -> httpHeaders.addAll(headers))
//            .body(jsonBody)
//            .retrieve()
//            .body(String.class);
//
//        System.out.println("POST Response: " + response);
    }

    @Test
    void testRestClientPut() {
        String url = "https://www.xiaohongshu.com/explore/69511d3f000000001f009e5f?app_platform=ios&app_version=9.9&share_from_user_hidden=true&xsec_source=app_share&type=normal&xsec_token=CB3-KUKZW8MuSRL-KNVoa2xI6QozLHw-dNgi6G0yLdq4M=&author_share=1&xhsshare=CopyLink&shareRedId=ODs6Njk9RT42NzUyOTgwNjdHOTc1PTc7&apptime=1768646010&share_id=6976871b1d3f434ba2a83b0263ab269c";

        String  urlOr = "http://xhslink.com/o/Ay0WlBPXsx1";

        try {
            VideoInfo videoInfo = redBookParser.parseShareUrl(url);

            System.out.println(videoInfo.getImages());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void testDouyinNoteFetchWithCookie() throws Exception {
//        String url = "https://www.douyin.com/note/7574825499164298539";
//        String url = "https://www.iesdouyin.com/share/slides/7335767155465588006/?from_ssr=1&video_share_track_ver=&did=MS4wLjABAAAANohL7MySrJh48nT3kizleRQlYTrp9738rFE6Qe1zaowXwldK4NsYzjB9V8kx3nio&mid=7330885970806507521&ts=1768805254&share_track_info=%7B%22link_description_type%22:%22%22%7D&region=CN&share_sign=pXO7XzUk5ng_6GxXD0y_UYEPp3Jn_krtjaa4XX8wkdQ-&tt_from=share_to&with_sec_did=1&from_aid=1128&titleType=title&utm_source=share_to&utm_medium=ios&activity_info=%7B%22social_share_time%22:%221768806282%22,%22social_author_id%22:%22255526845314510%22,%22social_share_id%22:%2296511107856_1768806282%22,%22social_share_user_id%22:%2296511107856%22%7D&timestamp=1768806282&is_slides=1&share_version=360600&ug_share_id=B2FECE107F0D4A2B9728A3DAF12F9DB3&u_code=157lg8bd4&iid=MS4wLjABAAAAvXSuPYLBvlMdk9v-z7qzBEvz_2halI8SXe5-qdgdlJDcVXVmGUFxCjItgi5itY9p&utm_campaign=client_share&app=aweme&schema_type=37";
//        String url = "https://v.douyin.com/uvZbBZU3tXg/";
//        String url = "https://v.douyin.com/tGrucI0IQhg";
        String url = "https://v.douyin.com/tGrucI0IQhg"; // 纯视频-单个

        String response = fetchHtmlWithRedirects(url, 2, "");


//        Files.writeString(Path.of("src/test/java/com/ideaflow/parseshortvideo/parseshortvideo/testVideo.html"), response);
//        System.out.println("Douyin note response: " + response );

    }

    @Test
    void testDouyinNoteFetchWithCookie2() throws Exception {
//        String url = "https://www.douyin.com/note/7574825499164298539";
//        String url = "https://www.iesdouyin.com/share/slides/7335767155465588006/?from_ssr=1&video_share_track_ver=&did=MS4wLjABAAAANohL7MySrJh48nT3kizleRQlYTrp9738rFE6Qe1zaowXwldK4NsYzjB9V8kx3nio&mid=7330885970806507521&ts=1768805254&share_track_info=%7B%22link_description_type%22:%22%22%7D&region=CN&share_sign=pXO7XzUk5ng_6GxXD0y_UYEPp3Jn_krtjaa4XX8wkdQ-&tt_from=share_to&with_sec_did=1&from_aid=1128&titleType=title&utm_source=share_to&utm_medium=ios&activity_info=%7B%22social_share_time%22:%221768806282%22,%22social_author_id%22:%22255526845314510%22,%22social_share_id%22:%2296511107856_1768806282%22,%22social_share_user_id%22:%2296511107856%22%7D&timestamp=1768806282&is_slides=1&share_version=360600&ug_share_id=B2FECE107F0D4A2B9728A3DAF12F9DB3&u_code=157lg8bd4&iid=MS4wLjABAAAAvXSuPYLBvlMdk9v-z7qzBEvz_2halI8SXe5-qdgdlJDcVXVmGUFxCjItgi5itY9p&utm_campaign=client_share&app=aweme&schema_type=37";
//        String url = "https://v.douyin.com/uvZbBZU3tXg/"; 图文 + 实况
//        String url = "https://v.douyin.com/IbEUYfVylvw"; // 图文 + 实况
//        String url = "https://v.douyin.com/3_eHCKlzOW4/"; // 纯图文
        String url = "https://v.douyin.com/tGrucI0IQhg"; // 纯视频-单个

        String response = fetchHtmlWithRedirects(url, 2, "");

        Pattern p = Pattern.compile("self\\.__pace_f\\.push\\(\\s*(\\[.*?\\])\\s*\\)", Pattern.DOTALL);
        Matcher m = p.matcher(response);
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

        VideoInfo videoInfo = new VideoInfo();

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

        System.out.println("imagesResult: "+videoInfo);


    }


    @Test
    void testExtractPaceFPushJsonArrays() throws Exception {
        String html = Files.readString(Path.of("src/test/java/com/ideaflow/parseshortvideo/parseshortvideo/test.html"));
        Pattern p = Pattern.compile("self\\.__pace_f\\.push\\(\\s*(\\[.*?\\])\\s*\\)", Pattern.DOTALL);
        Matcher m = p.matcher(html);
        ObjectMapper mapper = new ObjectMapper();
        List<JsonNode> matches = new ArrayList<>();
        while (m.find()) {
            String arrayJson = m.group(1).trim();
            try {
                JsonNode node = mapper.readTree(arrayJson);
                if (!node.toString().contains("\\\"awemeId\\\":\\\"7335767155465588006\\\"")) {
                    continue;
                }
                matches.add(node);
            } catch (Exception ignore) {
                // 非合法 JSON，跳过
            }
        }
        if (matches.isEmpty()) {
            System.out.println("No matching self.__pace_f.push JSON arrays found.");
            return;
        }

        JsonNode first = matches.getFirst();
        VideoInfo videoInfo = new VideoInfo();

        List<ImgInfo> imagesResult = new ArrayList<>();
        videoInfo.setImages(imagesResult);
        // 检查first.get(1)是否为格式为"数字: []"的字符串
        if (first.get(1).isTextual()) {
            String inputStr = first.get(1).asText();
//            System.out.println("Input string: " + inputStr);
            
            // 使用JsonArrayExtractor解析格式为"数字: []"的字符串
            JsonNode extractedArray = JsonArrayExtractor.extractJsonArray(inputStr);
            if (extractedArray != null && extractedArray.isArray()) {
                System.out.println("Extracted JSON array:");
                for (JsonNode jsonNode : extractedArray) {
                    if (jsonNode.has("awemeId") && jsonNode.has("aweme")) { //找到了想要的json数据
                        JsonNode awemeJsonNode = jsonNode.get("aweme");
                        JsonNode detailJsonNode = awemeJsonNode.get("detail");
                        JsonNode imagesJsonNode = detailJsonNode.get("images");
                        for (JsonNode imageNode : imagesJsonNode) {
                            JsonNode videoNode = imageNode.get("video");
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
            } else {
                System.out.println("Failed to extract JSON array or extracted content is not an array");
                // 如果不是预期格式，仍然按原有逻辑处理
                for (JsonNode jsonNode : first.get(1)) {
                    System.out.println(jsonNode.get("awemeId").asText());
                }
            }
        } else {
            // 如果first.get(1)不是文本而是直接的数组，则按原逻辑处理
            for (JsonNode jsonNode : first.get(1)) {
                System.out.println(jsonNode.get("awemeId").asText());
            }
        }

        System.out.println(videoInfo);

    }


    private String fetchHtmlWithRedirects(String url, int maxRedirects, String redirectCookie) throws Exception {
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
                return result.body;
            }

            if (result.location == null || result.location.isEmpty()) {
                throw new Exception("Redirect without Location header");
            }

            currentUri = currentUri.resolve(result.location);
        }

        throw new Exception("Exceeded redirect limit: " + maxRedirects);
    }
}
