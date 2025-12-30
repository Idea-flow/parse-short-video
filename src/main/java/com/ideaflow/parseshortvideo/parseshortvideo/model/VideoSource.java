package com.ideaflow.parseshortvideo.parseshortvideo.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 视频来源枚举
 */
public enum VideoSource {
    DOUYIN("douyin", "抖音"),
    KUAISHOU("kuaishou", "快手"),
    REDBOOK("redbook", "小红书"),
    BILIBILI("bilibili", "哔哩哔哩"),
    WEIBO("weibo", "微博"),
    WEISHI("weishi", "微视"),
    PIPIXIA("pipixia", "皮皮虾"),
    XIGUA("xigua", "西瓜视频"),
    HUYA("huya", "虎牙");

    private final String code;
    private final String name;

    VideoSource(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据code获取枚举
     */
    public static VideoSource fromCode(String code) {
        for (VideoSource source : values()) {
            if (source.code.equals(code)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown video source code: " + code);
    }
}

