package com.ideaflow.parseshortvideo.parseshortvideo.test;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

public class RestClientTest {

    public static void main(String[] args) {
        testRestClientGet();
    }


    public static void testRestClientGet() {
        RestClient restClient = RestClient.create();
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
                .uri("https://jsonplaceholder.typicode.com/posts?userId=1")
                .retrieve()
                .body(String.class);

        System.out.println("GET with Params Response: " + responseWithParams);
    }


    void testRestClientPost() {

        RestClient restClient = RestClient.create();
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
