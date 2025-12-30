package com.ideaflow.parseshortvideo.parseshortvideo.parser;

import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.util.UserAgentHelper;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 视频解析器基类
 */
public abstract class BaseParser {
    protected final RestClient restClient;
    protected final UserAgentHelper userAgentHelper;

    public BaseParser(RestClient restClient, UserAgentHelper userAgentHelper) {
        this.restClient = restClient;
        this.userAgentHelper = userAgentHelper;
    }

    /**
     * 解析分享链接，获取视频信息
     *
     * @param shareUrl 视频分享链接
     * @return VideoInfo
     * @throws Exception 解析异常
     */
    public abstract VideoInfo parseShareUrl(String shareUrl) throws Exception;

    /**
     * 解析视频ID，获取视频信息
     *
     * @param videoId 视频ID
     * @return VideoInfo
     * @throws Exception 解析异常
     */
    public abstract VideoInfo parseVideoId(String videoId) throws Exception;

    /**
     * 获取默认请求头
     */
    protected Map<String, String> getDefaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", userAgentHelper.getDefaultUserAgent());
        return headers;
    }

    /**
     * 获取iOS User-Agent请求头
     */
    protected Map<String, String> getIosHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", userAgentHelper.getRandomIosUserAgent());
        return headers;
    }

    /**
     * 获取Windows User-Agent请求头
     */
    protected Map<String, String> getWindowsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", userAgentHelper.getRandomWindowsUserAgent());
        return headers;
    }
}

