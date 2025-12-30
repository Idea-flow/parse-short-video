package com.ideaflow.parseshortvideo.parseshortvideo.controller;

import com.ideaflow.parseshortvideo.parseshortvideo.model.ApiResponse;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoSource;
import com.ideaflow.parseshortvideo.parseshortvideo.service.VideoParseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 视频解析控制器
 */
@RestController
@RequestMapping("/video")
public class VideoParseController {
    private static final Logger log = LoggerFactory.getLogger(VideoParseController.class);

    private final VideoParseService videoParseService;

    // URL提取正则表达式
    private static final Pattern URL_PATTERN = Pattern.compile("http[s]?://[\\w.-]+[\\w/-]*[\\w.-]*\\??[\\w=&:\\-+%]*[/]*");

    public VideoParseController(VideoParseService videoParseService) {
        this.videoParseService = videoParseService;
    }

    /**
     * 解析分享链接
     *
     * @param url 包含分享链接的文本
     * @return ApiResponse
     */
    @GetMapping("/share/url/parse")
    public ApiResponse<VideoInfo> parseShareUrl(@RequestParam String url) {
        try {
            log.info("Parsing share URL: {}", url);

            // 从文本中提取URL
            String videoShareUrl = extractUrl(url);
            if (videoShareUrl == null || videoShareUrl.isEmpty()) {
                return ApiResponse.error(400, "无法从输入文本中提取有效URL");
            }

            log.info("Extracted URL: {}", videoShareUrl);

            // 解析视频信息
            VideoInfo videoInfo = videoParseService.parseShareUrl(videoShareUrl);

            log.info("Successfully parsed video: {}", videoInfo.getTitle());

            return ApiResponse.success(videoInfo);
        } catch (Exception e) {
            log.error("Failed to parse share URL: {}", url, e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 解析视频ID
     *
     * @param source  视频来源
     * @param videoId 视频ID
     * @return ApiResponse
     */
    @GetMapping("/id/parse")
    public ApiResponse<VideoInfo> parseVideoId(
            @RequestParam VideoSource source,
            @RequestParam String videoId) {
        try {
            log.info("Parsing video ID: {} from source: {}", videoId, source);

            VideoInfo videoInfo = videoParseService.parseVideoId(source, videoId);

            log.info("Successfully parsed video: {}", videoInfo.getTitle());

            return ApiResponse.success(videoInfo);
        } catch (Exception e) {
            log.error("Failed to parse video ID: {} from source: {}", videoId, source, e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 从文本中提取URL
     */
    private String extractUrl(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        Matcher matcher = URL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }
}

