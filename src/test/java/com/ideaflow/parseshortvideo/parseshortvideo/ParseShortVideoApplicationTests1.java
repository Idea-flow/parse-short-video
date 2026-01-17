package com.ideaflow.parseshortvideo.parseshortvideo;

import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.parser.RedBookParser;
import jakarta.annotation.Resource;
import net.minidev.json.JSONUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

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
//        // GET 请求示例
//        String url = "https://jsonplaceholder.typicode.com/posts/1";
//
//        // 简单的 GET 请求
//        String response = restClient.get()
//            .uri(url)
//            .retrieve()
//            .body(String.class);
//
//        System.out.println("GET Response: " + response);
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
}
