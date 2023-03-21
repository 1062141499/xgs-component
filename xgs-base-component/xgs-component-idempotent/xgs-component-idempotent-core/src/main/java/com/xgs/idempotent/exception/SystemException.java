package com.xgs.idempotent.exception;

import lombok.Data;

/**
 * 系统异常基类
 *
 * @author xiongguoshuang
 * @version 2022-07-12
 * @since JDK1.8
 */
@Data
public class SystemException extends RuntimeException {

    private int code;

    private String msg;

    public SystemException(String msg) {
        super(msg);
        this.code = 1;
        this.msg = msg;
    }

    public SystemException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public SystemException(String msg, Throwable cause) {
        super(msg, cause);
        this.code = 1;
        this.msg = msg;
    }

    public SystemException(int code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
        this.msg = msg;
    }

    public SystemException(ErrorEnum errorEnum) {
        super(errorEnum.message);
        this.code = errorEnum.errorCode;
        this.msg = errorEnum.message;
    }

    public SystemException(ErrorEnum errorEnum, Throwable cause) {
        super(errorEnum.message, cause);
        this.code = errorEnum.errorCode;
        this.msg = errorEnum.message;
    }
}
