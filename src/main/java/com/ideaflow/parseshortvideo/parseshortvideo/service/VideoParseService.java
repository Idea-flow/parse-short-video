package com.ideaflow.parseshortvideo.parseshortvideo.service;

import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoSource;
import com.ideaflow.parseshortvideo.parseshortvideo.parser.BaseParser;
import com.ideaflow.parseshortvideo.parseshortvideo.parser.DouYin2Parser;
import com.ideaflow.parseshortvideo.parseshortvideo.parser.DouYinParser;
import com.ideaflow.parseshortvideo.parseshortvideo.parser.RedBookParser;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 视频解析服务
 */
@Service
public class VideoParseService {
    private final DouYinParser douYinParser;
    private final RedBookParser redBookParser;
    @Resource
    private DouYin2Parser douYin2Parser;

    private final Map<VideoSource, BaseParser> parserMap = new HashMap<>();
    private final Map<String, VideoSource> domainSourceMap = new HashMap<>();

    public VideoParseService(DouYinParser douYinParser, RedBookParser redBookParser) {
        this.douYinParser = douYinParser;
        this.redBookParser = redBookParser;
    }

    /**
     * 初始化解析器映射和域名映射
     */
    @PostConstruct
    public void init() {
        // 初始化解析器映射
        parserMap.put(VideoSource.DOUYIN, douYin2Parser);
        parserMap.put(VideoSource.REDBOOK, redBookParser);

        // 初始化域名映射
        // 抖音
        domainSourceMap.put("v.douyin.com", VideoSource.DOUYIN);
        domainSourceMap.put("www.iesdouyin.com", VideoSource.DOUYIN);
        domainSourceMap.put("www.douyin.com", VideoSource.DOUYIN);

        // 小红书
        domainSourceMap.put("www.xiaohongshu.com", VideoSource.REDBOOK);
        domainSourceMap.put("xhslink.com", VideoSource.REDBOOK);
    }

    /**
     * 解析分享链接
     *
     * @param shareUrl 分享链接
     * @return VideoInfo
     * @throws Exception 解析异常
     */
    public VideoInfo parseShareUrl(String shareUrl) throws Exception {
        if (shareUrl == null || shareUrl.isEmpty()) {
            throw new IllegalArgumentException("Share URL cannot be empty");
        }

        // 从URL中提取域名
        URI uri = new URI(shareUrl);
        String host = uri.getHost();

        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("Invalid share URL: cannot extract host");
        }

        // 根据域名识别VideoSource
        VideoSource source = domainSourceMap.get(host);
        if (source == null) {
            throw new IllegalArgumentException("Unsupported video source: " + host);
        }

        // 获取对应的Parser
        BaseParser parser = parserMap.get(source);
        if (parser == null) {
            throw new IllegalArgumentException("No parser found for source: " + source);
        }

        // 调用parser解析
        return parser.parseShareUrl(shareUrl);
    }

    /**
     * 解析视频ID
     *
     * @param source  视频来源
     * @param videoId 视频ID
     * @return VideoInfo
     * @throws Exception 解析异常
     */
    public VideoInfo parseVideoId(VideoSource source, String videoId) throws Exception {
        if (source == null) {
            throw new IllegalArgumentException("Video source cannot be null");
        }

        if (videoId == null || videoId.isEmpty()) {
            throw new IllegalArgumentException("Video ID cannot be empty");
        }

        // 获取对应的Parser
        BaseParser parser = parserMap.get(source);
        if (parser == null) {
            throw new IllegalArgumentException("No parser found for source: " + source);
        }

        // 调用parser解析
        return parser.parseVideoId(videoId);
    }
}

