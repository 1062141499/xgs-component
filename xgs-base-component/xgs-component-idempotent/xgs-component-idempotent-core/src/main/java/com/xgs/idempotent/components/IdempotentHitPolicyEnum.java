package com.xgs.idempotent.components;

import lombok.Getter;

/**
 * 幂等命中对上次结果的封装
 * @author xiongguoshuang
 */
@Getter
public enum IdempotentHitPolicyEnum {

    /**
     * 返回上次结果，如果上次执行异常则将异常尽可能原样抛出。
     */
    ReturnLastResultPolicy("ReturnLastResultPolicy", "返回上次结果"),


    /**
     * 返回上次结果，如果上次执行异常，则先包装一层幂等的异常后再抛出。
     */
    ReturnLastResultAndWrapperExceptionPolicy("ReturnLastResultAndWrapperExceptionPolicy", "返回上次结果，如果上次执行异常，则先包装一层再抛出"),

    /**
     * 直接抛出幂等组件异常
     */
    ThrowIdemException("ThrowIdemException", "直接抛出幂等组件异常"),

    ;


    /**
     * 编码
     */
    private String code;

    /**
     * 描述
     */
    private String desc;


    IdempotentHitPolicyEnum(String code, String desc){
        this.code = code;
        this.desc = desc;
    }



    public static IdempotentHitPolicyEnum findByCode(String code){
        for (IdempotentHitPolicyEnum policyEnum : IdempotentHitPolicyEnum.values()) {
            if(policyEnum.code.equals(code)){
                return policyEnum;
            }
        }
        return null;
    }

}
