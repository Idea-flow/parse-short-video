package com.ideaflow.parseshortvideo.parseshortvideo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图集图片信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImgInfo {
    /**
     * 图片URL
     */
    private String url;

    /**
     * Live Photo视频地址
     */
    private String livePhotoUrl;
}

