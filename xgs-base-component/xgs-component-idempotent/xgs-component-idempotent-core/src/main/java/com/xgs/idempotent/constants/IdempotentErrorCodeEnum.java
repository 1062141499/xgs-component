package com.xgs.idempotent.constants;

/**
 * 异常类型划分
 * 1、成功
 * 2、幂等组件的异常（系统异常），需要组件开发人员修复
 * 3、幂等组件配置不当的异常
 * 4、幂等组件运行过程中的动态异常，需要使用组件的业务系统开发人员
 * 5、业务系统异常（业务异常）
 * @author xiongguoshuang
 */
public enum IdempotentErrorCodeEnum {

    /**
     * 成功
     */
    SUCCESS("0", "success"),

    /**
     * 仅封装其它未知异常
     */
    WRAP_OTHER_UNKNOWN_ERROR("0001", "wrap other unknown error"),


    /**
     * 获取锁异常
     */
    LOCK_FAIL("00001","lock fail"),

    /**
     * 幂等命中中止
     */
    HIT_ABORT("00002","Idempotent hit abort"),


    /**
     * 幂等命中中止
     */
    HIT_UNKNOWN_HIT_POLICY("00003","Idempotent unknown hit policy"),

    /**
     * 参数错误异常
     */
    PARAM_ERROR("00004","param error"),

    /**
     * 幂等组件初始化校验异常
     */
    INITIAL_CHECK_FAIL("00005","initial check fail"),

    /**
     * redis连接失败
     */
    REDIS_CONNECT_FAIL("00006","redis connect fail"),

    /**
     * redis反序列化异常
     */
    REDIS_DECODE_FAIL("00007","redis decode fail"),

    /**
     * redis序列化异常
     */
    REDIS_ENCODE_FAIL("00008","redis encode fail"),
    ;

    /**
     * 错误码
     */
    private final String code;
    /**
     * 错误提示信息
     */
    private final String message;

    IdempotentErrorCodeEnum(String code, String message){
        this.code = code;
        this.message = message;
    }


    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static IdempotentErrorCodeEnum findByCode(String code){
        for(IdempotentErrorCodeEnum item : IdempotentErrorCodeEnum.values()){
            if(item.code.equals(code)){
                return item;
            }
        }
        return null;
    }
}
