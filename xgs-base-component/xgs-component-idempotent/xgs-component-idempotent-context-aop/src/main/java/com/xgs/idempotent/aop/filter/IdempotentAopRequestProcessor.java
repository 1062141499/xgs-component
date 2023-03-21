package com.xgs.idempotent.aop.filter;

import com.xgs.idempotent.aop.IdempotentAspectContext;
import com.xgs.idempotent.components.IdempotentHitPolicyEnum;
import com.xgs.idempotent.components.IdempotentRequestProcessorTemplate;
import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRecordStore;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.components.TryAgainRule;
import com.xgs.idempotent.inner.IdempotentInterfaceLevelConfig;
import com.xgs.idempotent.inner.pojo.IdempotentProcessResult;
import com.xgs.idempotent.inner.utils.DefaultConfigUtils;
import com.xgs.idempotent.inner.utils.GlobalConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

/**
 *
 * 幂等处理器，可以缓存，IdempotentProcessor必需不可变的，其所有的成员idempotentProperties也是不可变的
 * @author xiongguoshuang
 */
@Slf4j
public class IdempotentAopRequestProcessor extends IdempotentRequestProcessorTemplate<IdempotentAspectContext> {

    private final IdempotentInterfaceLevelConfig idempotentInterfaceLevelConfig;
    private final String moduleName;

    public IdempotentAopRequestProcessor(IdempotentInterfaceLevelConfig idempotentInterfaceLevelConfig,String moduleName){
        this.idempotentInterfaceLevelConfig = idempotentInterfaceLevelConfig;
        this.moduleName = moduleName;
    }


    /**
     * 子类可改写
     * @param idempotentRequestContext
     */
    @Override
    protected void preProcess(IdempotentAspectContext idempotentRequestContext){
        idempotentRequestContext.setIdempotentInterfaceLevelConfig(this.idempotentInterfaceLevelConfig);
    }

    /**
     * 解析出recordKey
     */
    @Override
    protected String parseRecordKey(IdempotentAspectContext idempotentRequestContext){
        return this.idempotentInterfaceLevelConfig.getKeyGetter().parseRecordKey(idempotentRequestContext);
    }

    /**
     * 解析出moduleName
     */
    @Override
    protected String parseModuleName(IdempotentAspectContext idempotentRequestContext) {
        return this.moduleName;
    }

    /**
     * 更新执行结果到幂等记录
     * @param normalProcessResult
     * @param idempotentRecord
     * @param idempotentRequestContext
     */
    @Override
    protected void updateResultToIdempotentRecord(
            IdempotentProcessResult normalProcessResult,
            IdempotentRecord idempotentRecord,
            IdempotentAspectContext idempotentRequestContext
    ){
        IdempotentRecord updateItem = assembleUpdateEvent(
                idempotentRecord,
                idempotentRequestContext,
                normalProcessResult.getResult(),
                normalProcessResult.getThrowable()
        );
        final IdempotentRecordStore recordStore = idempotentRequestContext.getRecordStore();
        recordStore.updateRecord(idempotentRequestContext.getLockClientId(), idempotentRecord, updateItem, idempotentRequestContext);
    }

    /**
     * 判断是否需要再试
     * @return
     * true 幂等记录是否存在且没有过期
     * false 幂等记录不存在，或已过期
     */
    @Override
    protected boolean isNeedRetry(IdempotentRecord idempotentRecord, IdempotentAspectContext idempotentRequestContext){
        Assert.notNull(idempotentRecord,"idempotentRecord 不可能为null，否则程序走不到这里");
        TryAgainRule retryRule = this.idempotentInterfaceLevelConfig.getRetryRule();
        if(retryRule == null){
            //接口级别没有配置的话，则查询全局级的的配置
            retryRule = GlobalConfigUtils.getInstance().getRetryRule();
        }
        if(retryRule == null){
            //全局级没有配置的话，则默认的配置
            retryRule = DefaultConfigUtils.getInstance().getRetryRule();
        }
        Assert.notNull(retryRule,"retryRule 不可能为null，程序已在启动时的检查");
        return retryRule.needTryAgain(idempotentRequestContext,idempotentRecord);
    }

    /**
     * 判断幂等记录是否存在且没有过期
     * @return
     * true 幂等记录是否存在且没有过期
     * false 幂等记录不存在，或已过期
     */
    @Override
    protected boolean isExistAndNotExpired(IdempotentRecord idempotentRecord, IdempotentAspectContext idempotentRequestContext){
        if(idempotentRecord == null){
            //不存在
            return false;
        }
        long expireTime = idempotentRecord.getExpireTime() == null ? 0L: idempotentRecord.getExpireTime();
        if(idempotentRequestContext.getCurrentTimeMillis() >= expireTime){
            //已过期
            return false;
        }
        //幂等记录是否存在且没有过期
        return true;
    }

    /**
     * 创建幂等记录
     */
    @Override
    protected IdempotentRecord createNewIdempotentRecord(String recordId, IdempotentAspectContext idempotentRequestContext){
        IdempotentRecord idempotentRecord = idempotentRequestContext.getRecordStore()
                .emptyRecord(idempotentRequestContext.getLockClientId(), recordId, idempotentRequestContext);
        return idempotentRecord;
    }

    /**
     * 删除已过期幂等记录
     */
    @Override
    protected void deleteExpiredIdempotentRecord(IdempotentRecord idempotentRecord, IdempotentAspectContext idempotentRequestContext){
        idempotentRequestContext.getRecordStore().deleteExpiredIdempotentRecord(
                idempotentRequestContext.getLockClientId(),
                idempotentRecord,
                idempotentRequestContext
        );
    }

    @Override
    protected IdempotentHitPolicyEnum getHitPolicy(IdempotentAspectContext idempotentRequestContext) {
        return this.idempotentInterfaceLevelConfig.getHitPolicy();
    }


    private IdempotentRecord assembleUpdateEvent(
            IdempotentRecord oldIdempotentRecord,
            IdempotentRequestContext context,
            Object result,
            Throwable processEx
    ){
        Object[] args = context.getParams();

        IdempotentRecord idempotentRecord = new IdempotentRecord();
        BeanUtils.copyProperties(oldIdempotentRecord, idempotentRecord);
        int failCount = oldIdempotentRecord.getFailCount();
        int successCount = oldIdempotentRecord.getSuccessCount();
        long curTime = System.currentTimeMillis();
        idempotentRecord.setParameterValues(args);
        idempotentRecord.setResult(result);
        idempotentRecord.setThrowable(processEx);
        boolean isOk = processEx == null;
        if(isOk){
            ++successCount;
            idempotentRecord.setSuccessCount(successCount);
        }else{
            ++ failCount;
            idempotentRecord.setFailCount(failCount);
        }
        idempotentRecord.setCreateTime(oldIdempotentRecord.getCreateTime());
        idempotentRecord.setUpdateTime(curTime);
        return idempotentRecord;
    }
}
