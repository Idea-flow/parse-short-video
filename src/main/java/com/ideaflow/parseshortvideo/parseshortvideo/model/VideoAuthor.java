package com.ideaflow.parseshortvideo.parseshortvideo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频作者信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoAuthor {
    /**
     * 作者ID
     */
    private String uid;

    /**
     * 作者昵称
     */
    private String name;

    /**
     * 作者头像URL
     */
    private String avatar;
}

