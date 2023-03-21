package com.xgs.idempotent.components.web;


import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应参数
 *
 * @author xiongguoshuang
 * @date 2021/06/10 17:37
 */
@Data
public class ApiResponse<V> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 成功时返回的编码
     */
    public static final int CODE_SUCCESS = 0;

    private int errorCode;

    private String message;

    private V data;

    private ApiResponse() {
    }

    private ApiResponse(int errorCode, String message, V data) {
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return 0 == this.errorCode;
    }

    public static <V> ApiResponse<V> success() {
        return new ApiResponse<>(CODE_SUCCESS, "success", null);
    }

    public static <V> ApiResponse<V> success(V data) {
        return new ApiResponse<>(CODE_SUCCESS, "success", data);
    }

    public static <V> ApiResponse<V> fail(String message) {
        return new ApiResponse<>(1, message, null);
    }

    public static <V> ApiResponse<V> fail(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
