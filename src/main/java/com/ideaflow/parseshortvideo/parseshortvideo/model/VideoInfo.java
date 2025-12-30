package com.ideaflow.parseshortvideo.parseshortvideo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoInfo {
    /**
     * 视频播放地址
     */
    private String videoUrl;

    /**
     * 视频封面地址
     */
    private String coverUrl;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 音乐播放地址
     */
    private String musicUrl;

    /**
     * 图集图片地址列表
     */
    @Builder.Default
    private List<ImgInfo> images = new ArrayList<>();

    /**
     * 视频作者信息
     */
    @Builder.Default
    private VideoAuthor author = new VideoAuthor();
}

