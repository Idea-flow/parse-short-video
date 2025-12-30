package com.ideaflow.parseshortvideo.parseshortvideo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一API响应类
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    /**
     * 状态码
     */
    private int code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "解析成功", data);
    }

    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

    /**
     * 错误响应（默认500状态码）
     */
    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>(500, msg, null);
    }
}

