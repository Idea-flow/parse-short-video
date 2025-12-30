package com.ideaflow.parseshortvideo.parseshortvideo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@SpringBootTest
class ParseShortVideoApplicationTests {

    private final RestClient restClient = RestClient.create();

    @Test
    void contextLoads() {

    }

    @Test
    void testRestClientGet() {
        // GET 请求示例
        String url = "https://jsonplaceholder.typicode.com/posts/1";
        
        // 简单的 GET 请求
        String response = restClient.get()
            .uri(url)
            .retrieve()
            .body(String.class);
        
        System.out.println("GET Response: " + response);
        
        // 带参数的 GET 请求
        String responseWithParams = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("https://jsonplaceholder.typicode.com/posts")
                .queryParam("userId", 1)
                .build())
            .retrieve()
            .body(String.class);
        
        System.out.println("GET with Params Response: " + responseWithParams);
    }

    @Test
    void testRestClientPost() {
        // POST 请求示例
        String url = "https://jsonplaceholder.typicode.com/posts";
        
        // 准备请求体
        String jsonBody = "{\n" +
            "  \"title\": \"foo\",\n" +
            "  \"body\": \"bar\",\n" +
            "  \"userId\": 1\n" +
            "}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
        
        // 执行 POST 请求
        String response = restClient.post()
            .uri(url)
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .body(jsonBody)
            .retrieve()
            .body(String.class);
        
        System.out.println("POST Response: " + response);
    }
}
