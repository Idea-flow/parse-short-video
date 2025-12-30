package com.ideaflow.parseshortvideo.parseshortvideo.parser;

import com.ideaflow.parseshortvideo.parseshortvideo.model.ImgInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoAuthor;
import com.ideaflow.parseshortvideo.parseshortvideo.model.VideoInfo;
import com.ideaflow.parseshortvideo.parseshortvideo.util.UserAgentHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小红书视频解析器
 */
@Component
public class RedBookParser extends BaseParser {
    private final Yaml yaml = new Yaml();

    public RedBookParser(RestClient restClient, UserAgentHelper userAgentHelper) {
        super(restClient, userAgentHelper);
    }

    @Override
    public VideoInfo parseShareUrl(String shareUrl) throws Exception {
        // 使用Windows User-Agent
        String html = restClient.get()
                .uri(shareUrl)
                .headers(httpHeaders -> getWindowsHeaders().forEach(httpHeaders::add))
                .retrieve()
                .body(String.class);

        if (html == null || html.isEmpty()) {
            throw new Exception("Failed to fetch video page HTML");
        }

        // 从HTML中提取JSON数据
        Pattern pattern = Pattern.compile("window\\.__INITIAL_STATE__\\s*=\\s*(.*?)</script>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        if (!matcher.find()) {
            throw new Exception("Failed to parse video json info from html");
        }

        String jsonStr = matcher.group(1).trim();

        // 使用YAML解析器解析（小红书的JSON格式特殊）
        Map<String, Object> jsonData = yaml.load(jsonStr);

        if (jsonData == null || !jsonData.containsKey("note")) {
            throw new Exception("Invalid response data structure");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> noteData = (Map<String, Object>) jsonData.get("note");

        String noteId = (String) noteData.get("currentNoteId");
        if ("undefined".equals(noteId)) {
            throw new Exception("Parse fail: note id in response is undefined");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> noteDetailMap = (Map<String, Object>) noteData.get("noteDetailMap");

        @SuppressWarnings("unchecked")
        Map<String, Object> noteDetail = (Map<String, Object>) noteDetailMap.get(noteId);

        @SuppressWarnings("unchecked")
        Map<String, Object> note = (Map<String, Object>) noteDetail.get("note");

        return buildVideoInfo(note);
    }

    @Override
    public VideoInfo parseVideoId(String videoId) throws Exception {
        throw new UnsupportedOperationException("小红书暂不支持直接解析视频ID");
    }

    /**
     * 构建VideoInfo对象
     */
    @SuppressWarnings("unchecked")
    private VideoInfo buildVideoInfo(Map<String, Object> note) {
        // 获取视频地址
        String videoUrl = "";
        if (note.containsKey("video")) {
            Map<String, Object> video = (Map<String, Object>) note.get("video");
            if (video.containsKey("media")) {
                Map<String, Object> media = (Map<String, Object>) video.get("media");
                if (media.containsKey("stream")) {
                    Map<String, Object> stream = (Map<String, Object>) media.get("stream");
                    if (stream.containsKey("h264")) {
                        List<Map<String, Object>> h264List = (List<Map<String, Object>>) stream.get("h264");
                        if (h264List != null && !h264List.isEmpty()) {
                            videoUrl = (String) h264List.get(0).getOrDefault("masterUrl", "");
                        }
                    }
                }
            }
        }

        // 获取图集图片
        List<ImgInfo> images = new ArrayList<>();
        if (videoUrl.isEmpty() && note.containsKey("imageList")) {
            List<Map<String, Object>> imageList = (List<Map<String, Object>>) note.get("imageList");

            for (Map<String, Object> imgItem : imageList) {
                String urlDefault = (String) imgItem.get("urlDefault");

                // 处理图片去水印
                String imageId = urlDefault.split("/")[urlDefault.split("/").length - 1].split("!")[0];
                String spectrumStr = urlDefault.contains("spectrum/") ? "spectrum/" : "";
                String newUrl = "https://ci.xiaohongshu.com/notes_pre_post/"
                        + spectrumStr + imageId
                        + "?imageView2/format/jpg";

                // 如果原URL不包含notes_pre_post，使用原URL
                if (!urlDefault.contains("notes_pre_post")) {
                    newUrl = urlDefault;
                }

                // 检查是否有Live Photo
                String livePhotoUrl = "";
                if (imgItem.containsKey("livePhoto") && Boolean.TRUE.equals(imgItem.get("livePhoto"))) {
                    if (imgItem.containsKey("stream")) {
                        Map<String, Object> stream = (Map<String, Object>) imgItem.get("stream");
                        if (stream.containsKey("h264")) {
                            List<Map<String, Object>> h264List = (List<Map<String, Object>>) stream.get("h264");
                            if (h264List != null && !h264List.isEmpty()) {
                                livePhotoUrl = (String) h264List.get(0).getOrDefault("masterUrl", "");
                            }
                        }
                    }
                }

                images.add(ImgInfo.builder()
                        .url(newUrl)
                        .livePhotoUrl(livePhotoUrl)
                        .build());
            }
        }

        // 获取封面图片
        String coverUrl = "";
        if (note.containsKey("imageList")) {
            List<Map<String, Object>> imageList = (List<Map<String, Object>>) note.get("imageList");
            if (!imageList.isEmpty()) {
                coverUrl = (String) imageList.get(0).get("urlDefault");
            }
        }

        // 获取作者信息
        VideoAuthor author = VideoAuthor.builder().build();
        if (note.containsKey("user")) {
            Map<String, Object> user = (Map<String, Object>) note.get("user");
            author = VideoAuthor.builder()
                    .uid((String) user.getOrDefault("userId", ""))
                    .name((String) user.getOrDefault("nickname", ""))
                    .avatar((String) user.getOrDefault("avatar", ""))
                    .build();
        }

        return VideoInfo.builder()
                .videoUrl(videoUrl)
                .coverUrl(coverUrl)
                .title((String) note.getOrDefault("title", ""))
                .images(images)
                .author(author)
                .build();
    }
}

