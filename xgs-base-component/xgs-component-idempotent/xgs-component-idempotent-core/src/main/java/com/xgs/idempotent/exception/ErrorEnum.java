package com.xgs.idempotent.exception;

/**
 * 错误枚举
 *
 * @author xiongguoshuang
 * @date 2021/09/06 16:39
 */
public enum ErrorEnum {
    /**
     * 通用成功
     */
    SUCCESS(0, "成功"),

    /**
     * 通用失败
     */
    FAIL(1, "失败");

    public final int errorCode;
    public final String message;

    ErrorEnum(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
