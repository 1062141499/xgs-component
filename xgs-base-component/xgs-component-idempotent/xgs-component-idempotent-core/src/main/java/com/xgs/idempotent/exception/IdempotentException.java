package com.xgs.idempotent.exception;

import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;

import java.util.Objects;

/**
 * 幂等通用异常
 * @author xiongguoshuang
 */
public class IdempotentException extends RuntimeException{


    private final String code;
    private final String message;
    private final Object[] arguments;

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Object[] getArguments() {
        return arguments;
    }

    private IdempotentException(String code, String message, Throwable cause, Object... arguments) {
        super(code + (Objects.isNull(message) ? "" : ", " + message), cause);
        this.code = code;
        this.message = message;
        this.arguments = arguments;
    }

    public static IdempotentException error(IdempotentErrorCodeEnum errorCodeEnum){
        return new IdempotentException(errorCodeEnum.getCode(),errorCodeEnum.getMessage(), (Throwable)null , null);
    }

    public static IdempotentException error(String code, String message){
        return new IdempotentException(code,message, (Throwable)null , null);
    }

    public static IdempotentException errorWithArguments(IdempotentErrorCodeEnum errorCodeEnum, Object... arguments){
        return new IdempotentException(errorCodeEnum.getCode(),errorCodeEnum.getMessage(), (Throwable)null , arguments);
    }

    public static IdempotentException errorWithArguments(String code, String message, Object... arguments){
        return new IdempotentException(code, message, (Throwable)null , arguments);
    }

    /**
     * 包装其它异常
     * @param cause
     * @return
     */
    public static IdempotentException wrapThrowable(Throwable cause){
        return new IdempotentException(IdempotentErrorCodeEnum.WRAP_OTHER_UNKNOWN_ERROR.getCode(),
                IdempotentErrorCodeEnum.WRAP_OTHER_UNKNOWN_ERROR.getMessage(),
                cause,
                cause.getMessage()
        );
    }

    /**
     * 包装其它异常
     * @param cause
     * @return
     */
    public static IdempotentException wrapThrowableWithArguments(Throwable cause, Object... arguments){
        return new IdempotentException(IdempotentErrorCodeEnum.WRAP_OTHER_UNKNOWN_ERROR.getCode(),
                IdempotentErrorCodeEnum.WRAP_OTHER_UNKNOWN_ERROR.getMessage(),
                cause,
                arguments
        );
    }


    /**
     * 包装异常
     * @param cause
     * @return
     */
    public static IdempotentException wrapThrowableWithArguments(IdempotentErrorCodeEnum errorCodeEnum, Throwable cause, Object... arguments){
        return new IdempotentException(errorCodeEnum.getCode(), errorCodeEnum.getMessage(), cause, arguments);
    }


    /**
     * 包装异常
     * @param cause
     * @return
     */
    public static IdempotentException wrapThrowableWithArguments(String code, String message, Throwable cause, Object... arguments){
        return new IdempotentException(code, message, cause, arguments);
    }




}
