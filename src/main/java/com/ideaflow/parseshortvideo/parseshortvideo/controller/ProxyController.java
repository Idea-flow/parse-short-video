package com.ideaflow.parseshortvideo.parseshortvideo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

@RestController
public class ProxyController {
    private static final Logger log = LoggerFactory.getLogger(ProxyController.class);

    // Simple proxy that streams remote media (image/video) to the client.
    @GetMapping("/proxy")
    public ResponseEntity<StreamingResponseBody> proxy(@RequestParam("url") String urlParam,
                                                       HttpServletRequest request) {
        if (!StringUtils.hasText(urlParam)) {
            return ResponseEntity.badRequest().build();
        }

        final URI uri;
        try {
            uri = URI.create(urlParam);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout((int) Duration.ofSeconds(8).toMillis());
            conn.setReadTimeout((int) Duration.ofSeconds(30).toMillis());

            // Basic SSRF guard: only allow http/https hosts (already checked) and non-empty host.
            if (!StringUtils.hasText(uri.getHost())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Explicitly set Host to the target host (with port when present).
            String hostHeader = uri.getPort() > 0 ? uri.getHost() + ":" + uri.getPort() : uri.getHost();
            conn.setRequestProperty(HttpHeaders.HOST, hostHeader);

            // Forward limited safe headers; avoid referer/forwarded/CF headers.
            String range = request.getHeader(HttpHeaders.RANGE);
            if (StringUtils.hasText(range)) {
                conn.setRequestProperty(HttpHeaders.RANGE, range);
            }
            String ua = request.getHeader(HttpHeaders.USER_AGENT);
            conn.setRequestProperty(HttpHeaders.USER_AGENT, StringUtils.hasText(ua) ? ua : "Mozilla/5.0");
            String accept = request.getHeader(HttpHeaders.ACCEPT);
            if (StringUtils.hasText(accept)) {
                conn.setRequestProperty(HttpHeaders.ACCEPT, accept);
            }
            String acceptLang = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
            if (StringUtils.hasText(acceptLang)) {
                conn.setRequestProperty(HttpHeaders.ACCEPT_LANGUAGE, acceptLang);
            }

            conn.connect();

            int status = conn.getResponseCode();
            InputStream inputStream = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (inputStream == null) {
                conn.disconnect();
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            String contentType = conn.getContentType();
            if (StringUtils.hasText(contentType)) {
                responseHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
            }
            long contentLength = conn.getContentLengthLong();
            if (contentLength >= 0) {
                responseHeaders.setContentLength(contentLength);
            }
            String contentDisposition = conn.getHeaderField(HttpHeaders.CONTENT_DISPOSITION);
            if (StringUtils.hasText(contentDisposition)) {
                responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            }
            String acceptRanges = conn.getHeaderField("Accept-Ranges");
            if (StringUtils.hasText(acceptRanges)) {
                responseHeaders.add("Accept-Ranges", acceptRanges);
            }
            String contentRange = conn.getHeaderField("Content-Range");
            if (StringUtils.hasText(contentRange)) {
                responseHeaders.add("Content-Range", contentRange);
            }
            String cacheControl = conn.getHeaderField(HttpHeaders.CACHE_CONTROL);
            if (StringUtils.hasText(cacheControl)) {
                responseHeaders.add(HttpHeaders.CACHE_CONTROL, cacheControl);
            }

            // CORS and custom header per requirement/reference.
            responseHeaders.add("Access-Control-Allow-Origin", "*");
            responseHeaders.add("Access-Control-Allow-Methods", "GET,HEAD");
            responseHeaders.add("Access-Control-Allow-Headers", "*");
            responseHeaders.add("bi-version", "1.0.2");

            StreamingResponseBody body = outputStream -> {
                try (InputStream in = inputStream) {
                    StreamUtils.copy(in, outputStream);
                } finally {
                    conn.disconnect();
                }
            };

            HttpStatus httpStatus = HttpStatus.resolve(status);
            if (httpStatus == null) {
                httpStatus = HttpStatus.BAD_GATEWAY;
            }

            return ResponseEntity.status(httpStatus).headers(responseHeaders).body(body);
        } catch (Exception ex) {
            log.error("Proxy fetch failed for url: {}", urlParam, ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
