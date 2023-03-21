package com.xgs.idempotent.exception;

import lombok.Data;

/**
 * 业务异常基类
 *
 * @author xiongguoshuang
 * @version 2021-02-19 谢阳
 * @since JDK1.8
 */
@Data
public class BusinessException extends RuntimeException {

    private int code;

    private String msg;

    public BusinessException(String msg) {
        super(msg);
        this.code = 1;
        this.msg = msg;
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(String msg, Throwable cause) {
        super(msg, cause);
        this.code = 1;
        this.msg = msg;
    }

    public BusinessException(int code, String msg, Throwable cause) {
        super(msg, cause);
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(ErrorEnum errorEnum) {
        super(errorEnum.message);
        this.code = errorEnum.errorCode;
        this.msg = errorEnum.message;
    }

    public BusinessException(ErrorEnum errorEnum, Throwable cause) {
        super(errorEnum.message, cause);
        this.code = errorEnum.errorCode;
        this.msg = errorEnum.message;
    }
}
