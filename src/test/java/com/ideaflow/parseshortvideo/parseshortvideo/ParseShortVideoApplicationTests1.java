package com.ideaflow.parseshortvideo.parseshortvideo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.parser.DouYin2Parser;
import com.ideaflow.parseshortvideo.parseshortvideo.parser.RedBookParser;
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
import java.util.List;
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
        String url = "https://www.iesdouyin.com/share/slides/7335767155465588006/?from_ssr=1&video_share_track_ver=&did=MS4wLjABAAAANohL7MySrJh48nT3kizleRQlYTrp9738rFE6Qe1zaowXwldK4NsYzjB9V8kx3nio&mid=7330885970806507521&ts=1768805254&share_track_info=%7B%22link_description_type%22:%22%22%7D&region=CN&share_sign=pXO7XzUk5ng_6GxXD0y_UYEPp3Jn_krtjaa4XX8wkdQ-&tt_from=share_to&with_sec_did=1&from_aid=1128&titleType=title&utm_source=share_to&utm_medium=ios&activity_info=%7B%22social_share_time%22:%221768806282%22,%22social_author_id%22:%22255526845314510%22,%22social_share_id%22:%2296511107856_1768806282%22,%22social_share_user_id%22:%2296511107856%22%7D&timestamp=1768806282&is_slides=1&share_version=360600&ug_share_id=B2FECE107F0D4A2B9728A3DAF12F9DB3&u_code=157lg8bd4&iid=MS4wLjABAAAAvXSuPYLBvlMdk9v-z7qzBEvz_2halI8SXe5-qdgdlJDcVXVmGUFxCjItgi5itY9p&utm_campaign=client_share&app=aweme&schema_type=37";

        String response = fetchHtmlWithRedirects(url, 2, "");
        Files.writeString(Path.of("src/test/java/com/ideaflow/parseshortvideo/parseshortvideo/test.html"), response);
        System.out.println("Douyin note response: " + response );


//        RestClient redirectableRestClient = restClient.mutate()
//                .requestFactory(new JdkClientHttpRequestFactory(
//                        HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()))
//                .build();
//
//        String response = redirectableRestClient.get()
//                .uri(URI.create(url.trim()))
//                .headers(httpHeaders -> {
//                    httpHeaders.add("Cookie", "__ac_nonce=06950e150003bec85b6d9; __ac_signature=_02B4Z6wo00f01OzPKuAAAIDC31rcoCq6.8Ts7y5AAFJo7f");
//                    httpHeaders.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36");
//                    httpHeaders.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
//                })
//                .retrieve()
//                .body(String.class);
//
//        System.out.println("Douyin note response: " + response);
    }

    private String fetchHtmlWithRedirects(String url, int maxRedirects, String redirectCookie) throws Exception {
        RestClient nonRedirectClient = restClient.mutate()
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()))
                .build();

        URI currentUri = URI.create(url);
        for (int i = 0; i <= maxRedirects; i++) {
            int finalI = i;
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
                        String body = clientResponse.bodyTo(String.class);
                        return new DouYin2Parser.RedirectFetchResult(body, null, false);
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

    @Test
    void testParseHtmlJsonFromFile() throws Exception {
        String html = Files.readString(Path.of("src/test/java/com/ideaflow/parseshortvideo/parseshortvideo/test.html"));

        Pattern pattern = Pattern.compile("window\\._ROUTER_DATA\\s*=\\s*(.*?)</script>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            System.out.println("No window._ROUTER_DATA found in test.html");
            return;
        }

        String jsonStr = matcher.group(1).trim();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(jsonStr);
        System.out.println(json.toPrettyString());
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
        for (JsonNode jsonNode : first.get(1)) {
            System.out.println(jsonNode.get("awemeId").asText());
        }

        System.out.println(first.toPrettyString());

    }
}
