package com.xgs.idempotent.components;

import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;
import com.xgs.idempotent.inner.pojo.IdempotentProcessResult;

import com.xgs.idempotent.inner.utils.IdempotentHitPolicyEnumUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 幂等请求处理模板方法类
 * @param <T> 参数化类型，
 * @author xiongguoshuang
 */
@Slf4j
public abstract class IdempotentRequestProcessorTemplate<T extends IdempotentRequestContext> {


    /**
     * 幂等处理前逻辑
     * 子类可改写
     * @param idempotentRequestContext
     */
    protected void preProcess(T idempotentRequestContext){

    }

    /**
     * 幂等处理后逻辑
     * 子类可改写
     * @param idempotentRequestContext  幂等处理上下文
     * @param normalProcessResult       幂等处理结果
     */
    protected void postProcess(T idempotentRequestContext, IdempotentProcessResult normalProcessResult){

    }

    /**
     * 解析recordKey
     */
    protected abstract String parseRecordKey(T idempotentRequestContext);

    /**
     * 解析模块名
     * @param idempotentRequestContext
     * @return
     */
    protected abstract String parseModuleName(T idempotentRequestContext);

    /**
     * 更新执行结果到幂等记录
     * @param normalProcessResult
     * @param idempotentRecord
     * @param idempotentRequestContext
     */
    protected abstract void updateResultToIdempotentRecord(
            IdempotentProcessResult normalProcessResult,
            IdempotentRecord idempotentRecord,
            T idempotentRequestContext
    );

    /**
     * 判断是否需要再试
     * @return
     * true 幂等记录是否存在且没有过期
     * false 幂等记录不存在，或已过期
     */
    protected abstract boolean isNeedRetry(IdempotentRecord idempotentRecord, T idempotentRequestContext);

    /**
     * 判断幂等记录是否存在且没有过期
     * @return
     * true 幂等记录是否存在且没有过期
     * false 幂等记录不存在，或已过期
     */
    protected abstract boolean isExistAndNotExpired(IdempotentRecord idempotentRecord, T idempotentRequestContext);

    /**
     * 创建幂等记录
     */
    protected abstract IdempotentRecord createNewIdempotentRecord(String recordId, T idempotentRequestContext);

    /**
     * 删除已过期幂等记录
     */
    protected abstract void deleteExpiredIdempotentRecord(IdempotentRecord idempotentRecord, T idempotentRequestContext);


    protected abstract IdempotentHitPolicyEnum getHitPolicy(T idempotentRequestContext);



    /**
     * 幂等处理模板方法，子类不可改写
     * @return
     * @throws Throwable
     */
    public final Object process(T idempotentRequestContext) throws Throwable {
        this.preProcess(idempotentRequestContext);
        //取出key
        String recordKey = this.parseRecordKey(idempotentRequestContext);

        if(StringUtils.isBlank(recordKey)){
            //获取锁失败
            throw IdempotentException.errorWithArguments(
                    IdempotentErrorCodeEnum.PARAM_ERROR.getCode(), "recordKey can't be empty", idempotentRequestContext);
        }
        String moduleName = this.parseModuleName(idempotentRequestContext);
        if(StringUtils.isBlank(moduleName)){
            //获取锁失败
            throw IdempotentException.errorWithArguments(
                    IdempotentErrorCodeEnum.PARAM_ERROR.getCode(), "moduleName can't be empty", idempotentRequestContext);
        }
        String recordId = moduleName + recordKey;
        String lockClientId = idempotentRequestContext.getLockClientId();

        final IdempotentRecordStore recordStore = idempotentRequestContext.getRecordStore();
        boolean isLockOk = recordStore.tryLock(
                lockClientId,
                recordId,
                idempotentRequestContext
        );
        if(!isLockOk){
            //获取锁失败
            throw IdempotentException.wrapThrowableWithArguments(
                    IdempotentErrorCodeEnum.LOCK_FAIL, (Throwable)null, "get lock fail idempotentRecord.key = " + recordId);
        }

        IdempotentProcessResult normalProcessResult = null;
        try{
            IdempotentRecord idempotentRecord = recordStore.queryRecordByKey(recordId, idempotentRequestContext);
            boolean existAndNotExpired = this.isExistAndNotExpired(idempotentRecord, idempotentRequestContext);
            if(!existAndNotExpired){
                //不存在或已过期

                //新建幂等记录如果过期则先清除旧记录
                if(idempotentRecord != null){
                    //如果过期则先清除旧记录
                    this.deleteExpiredIdempotentRecord(idempotentRecord,idempotentRequestContext);
                    idempotentRecord = null;
                }
                idempotentRecord = this.createNewIdempotentRecord(recordId, idempotentRequestContext);

                //执行业务逻辑
                normalProcessResult =
                        idempotentRequestContext.normalProcess();

                //将执行结果，及时间等（包括异常）更新到幂等记录
                this.updateResultToIdempotentRecord(normalProcessResult,idempotentRecord,idempotentRequestContext);

            }else if(isNeedRetry(idempotentRecord, idempotentRequestContext)){
                //执行业务逻辑
                //将执行结果，及时间等（包括异常）更新到幂等记录
                //执行业务逻辑
                normalProcessResult =
                        idempotentRequestContext.normalProcess();

                //将执行结果，及时间等（包括异常）更新到幂等记录
                this.updateResultToIdempotentRecord(normalProcessResult,idempotentRecord,idempotentRequestContext);
            }else{
                IdempotentHitPolicyEnum hitPolicy = this.getHitPolicy(idempotentRequestContext);
                normalProcessResult = IdempotentHitPolicyEnumUtils.handle(hitPolicy, idempotentRecord, idempotentRequestContext);
            }
            Assert.notNull(normalProcessResult,"normalProcessResult 不可能在这里能null");
            if(normalProcessResult.getThrowable()!=null){
                throw normalProcessResult.getThrowable();
            }else{
                return normalProcessResult.getResult();
            }
        }catch (Throwable throwable){
            if(normalProcessResult!= null && normalProcessResult.getThrowable() != null){
                throw normalProcessResult.getThrowable();
            }else{
                throw throwable;
            }
        }finally {
            try{
                recordStore.releaseLock(lockClientId, recordId, idempotentRequestContext);
            }catch (Throwable throwable){
                log.warn("幂等组件释放锁失败，recordId={}, lockClientId= {}", recordId, lockClientId ,throwable);
            }
            this.postProcess(idempotentRequestContext, normalProcessResult);
        }

    }
}
