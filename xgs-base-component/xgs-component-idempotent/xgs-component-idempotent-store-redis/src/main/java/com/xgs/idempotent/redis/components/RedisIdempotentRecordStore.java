package com.xgs.idempotent.redis.components;

import cn.hutool.crypto.digest.DigestUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRecordStore;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;
import com.xgs.idempotent.inner.utils.GlobalConfigUtils;
import com.xgs.idempotent.redis.JsonJacksonCodecForIdempotant;
import com.xgs.idempotent.redis.LockTtlSecsUtils;
import com.xgs.idempotent.redis.RecordTtlSecsUtils;
import com.xgs.idempotent.redis.ResourceScriptSourceUtils;
import com.xgs.idempotent.redis.pojo.RedisIdempotentRecord;
import com.xgs.idempotent.redis.service.RedisIdempotentRecordService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author xiongguoshuang
 */
@Slf4j
public class RedisIdempotentRecordStore implements IdempotentRecordStore {


    /**
     * key唯一
     */
    private static final String K_RECORD_KEY = "key";

    /**
     * parameterValues
     */
    private static final String K_RECORD_PARAMETER_VALUES = "parameterValues";

    /**
     * parameterTypes
     */
    private static final String K_RECORD_PARAMETER_TYPES = "parameterTypes";

    /**
     * result
     */
    private static final String K_RECORD_RESULT = "result";

    /**
     * throwable
     */
    private static final String K_RECORD_THROWABLE = "throwable";

    /**
     * 成功次数
     */
    private static final String K_RECORD_SUCCESS_COUNT = "successCount";

    /**
     * 失败次数
     */
    private static final String K_RECORD_FAIL_COUNT = "failCount";

    /**
     * 创建时间
     */
    private static final String K_RECORD_CREATE_TIME ="createTime";

    /**
     * 更新时间
     */
    private static final String K_RECORD_UPDATE_TIME ="updateTime";

    /**
     * 过期时间
     */
    private static final String K_RECORD_EXPIRE_TIME_SECS = "recordExpireTimeSecs";


    /**
     * 锁过期时间
     */
    private static final String K_LOCK_EXPIRE_TIME_MILLIS = "lockExpireTimeMillis";

    private final RedissonClient redissonClient;

    private final RedisIdempotentRecordService redisIdempotentRecordService;

    public RedisIdempotentRecordStore(
            RedissonClient redissonClient,
            RedisIdempotentRecordService redisIdempotentRecordService
    ){
        this.redissonClient = redissonClient;
        this.redisIdempotentRecordService = redisIdempotentRecordService;
    }

    private String getRedisTableName(){
        return GlobalConfigUtils.getInstance().getAppName();
    }

    private String propertyUniqueKey(String tableName, String recordKey, String propertyName){
        return String.format("%s:%s:%s", tableName, recordKey, propertyName);
    }

    @Override
    public boolean tryLock(String lockId, String recordId, IdempotentRequestContext idempotentRequestContext) {

        String tableName = getRedisTableName();
        String lockKey = String.format("%s:%s", tableName, recordId);

        RLock lock = redissonClient.getLock(lockKey);

        try {

            long distributedLockTtlSecs = LockTtlSecsUtils.calDistributedLockTtlSecs(idempotentRequestContext);
            long tryDistributedLockTimeOutMills = LockTtlSecsUtils.calTryDistributedLockTimeOutMills(idempotentRequestContext);
            Boolean redissonLockOk = lock.tryLock(tryDistributedLockTimeOutMills, distributedLockTtlSecs * 1000,  TimeUnit.MILLISECONDS);
            return Boolean.TRUE.equals(redissonLockOk);
        } catch (InterruptedException e) {
            log.warn("tryLock fail", e);
        }

        return false;
    }

    @Override
    public boolean releaseLock(String lockId, String recordId, IdempotentRequestContext idempotentRequestContext) {
        String tableName = getRedisTableName();
        String lockKey = String.format("%s:%s", tableName, recordId);
        RLock lock = redissonClient.getLock(lockKey);
        lock.unlock();
        return true;
    }

    @Override
    public IdempotentRecord emptyRecord(String lockId, String recordId,
                                        IdempotentRequestContext idempotentRequestContext) {
        long curTime = System.currentTimeMillis();
        IdempotentRecord idempotentRecord = new IdempotentRecord();
        idempotentRecord.setKey(recordId);
        idempotentRecord.setCreateTime(curTime);
        idempotentRecord.setUpdateTime(curTime);

        RedisIdempotentRecord redisIdempotentRecord = redisIdempotentRecordService.toRedisIdempotentRecord(idempotentRecord);
        long recordExpiredSecs = RecordTtlSecsUtils.calRecordTtlSecs(idempotentRequestContext);
        boolean saveResult = luaSaveExpress(redisIdempotentRecord, recordExpiredSecs, lockId);
        if(saveResult){
            return idempotentRecord;
        }
        return null;
    }

    @Override
    public boolean updateRecord(String lockId, IdempotentRecord oldRecord, @NotNull IdempotentRecord newRecord,
                                IdempotentRequestContext idempotentRequestContext) {
        long recordExpiredSecs = RecordTtlSecsUtils.calRecordTtlSecs(idempotentRequestContext);
        RedisIdempotentRecord redisIdempotentRecord = redisIdempotentRecordService.toRedisIdempotentRecord(newRecord);
        return luaUpdateExpress(redisIdempotentRecord,recordExpiredSecs,lockId);
    }


    @Override
    public IdempotentRecord queryRecordByKey(@NotBlank String recordId, IdempotentRequestContext idempotentRequestContext) {
        RedisIdempotentRecord redisIdempotentRecord = luaLoadExpress(recordId);
        if(redisIdempotentRecord == null){
            return null;
        }
        IdempotentRecord idempotentRecord = redisIdempotentRecordService.toIdempotentRecord(redisIdempotentRecord, idempotentRequestContext);
        return idempotentRecord;
    }


    /**
     * 删除过期记录
     */
    @Override
    public boolean deleteExpiredIdempotentRecord(@NotBlank String lockId, IdempotentRecord oldRecord, IdempotentRequestContext idempotentRequestContext){
        //redis过期记录会自动删除掉，不可能获取到过期记录的，所以此接口不可能有调用的机会，直接抛异常就可以
        throw new UnsupportedOperationException("redis过期记录会自动删除掉，不可能获取到过期记录的，所以此接口不可能有调用的机会，如果真被调用，一定是程序哪里弄错了");
    }


    private RedisIdempotentRecord luaLoadExpress(String recordKey) {

        String tableName = getRedisTableName();

        List<Object> keyList = new ArrayList<>();
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_KEY));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_PARAMETER_VALUES));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_PARAMETER_TYPES));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_RESULT));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_THROWABLE));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_SUCCESS_COUNT));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_FAIL_COUNT));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_CREATE_TIME));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_UPDATE_TIME));


        ResourceScriptSource resourceScriptSource =
                ResourceScriptSourceUtils.get("lua/redis_record_load_redisson.lua");

        String scriptAsString =
                ResourceScriptSourceUtils.getScriptAsString(resourceScriptSource);
        String sha1 = DigestUtil.sha1Hex(scriptAsString, StandardCharsets.UTF_8.toString()).toLowerCase();
        RScript rScript = redissonClient.getScript(JsonJacksonCodecForIdempotant.INSTANCE);
        List<Boolean> booleans = rScript.scriptExists(sha1);
        if(CollectionUtils.isEmpty(booleans) || !Boolean.TRUE.equals(booleans.get(0))){
            rScript.scriptLoad(scriptAsString);
        }

        List<Object> result = rScript.evalSha(
                RScript.Mode.READ_WRITE,
                sha1,
                RScript.ReturnType.MULTI,
                keyList
        );

        if(result== null || result.isEmpty() || StringUtils.isEmpty(result.get(0))){
            return null;
        }
        RedisIdempotentRecord redisIdempotentRecord = new RedisIdempotentRecord();
        redisIdempotentRecord.setKey((String)result.get(0));
        redisIdempotentRecord.setParameterValues((Object[])result.get(1));
        redisIdempotentRecord.setParameterTypes((String)result.get(2));
        redisIdempotentRecord.setResult(result.get(3));
        if(result.get(4) instanceof JsonJacksonCodecForIdempotant.FailOnDecoderObj){
            JsonJacksonCodecForIdempotant.FailOnDecoderObj failOnDecoderObj =(JsonJacksonCodecForIdempotant.FailOnDecoderObj)result.get(4);
            String rowMsg = null;
            try{
                rowMsg = new String(failOnDecoderObj.getBytes(),StandardCharsets.UTF_8);
            }catch (Exception ex){
                log.warn("get rowMsg error", ex);
                rowMsg = "get rowMsg error" + ex.getMessage();
            }
            redisIdempotentRecord.setThrowable(IdempotentException.errorWithArguments(
                    IdempotentErrorCodeEnum.REDIS_DECODE_FAIL,
                    rowMsg
            ));
        }else {
            redisIdempotentRecord.setThrowable((Throwable) result.get(4));
        }

        redisIdempotentRecord.setSuccessCount((int)result.get(5));
        redisIdempotentRecord.setFailCount((int)result.get(6));
        redisIdempotentRecord.setCreateTime((long)result.get(7));
        redisIdempotentRecord.setUpdateTime((long)result.get(8));
        return redisIdempotentRecord;


    }

    private boolean luaSaveExpress(RedisIdempotentRecord redisIdempotentRecord, long expireTimeSecs, String lockId) {

        String tableName = getRedisTableName();
        String recordKey = redisIdempotentRecord.getKey();

        List<Object> keyList = new ArrayList<>();
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_KEY));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_PARAMETER_VALUES));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_PARAMETER_TYPES));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_RESULT));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_THROWABLE));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_SUCCESS_COUNT));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_FAIL_COUNT));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_CREATE_TIME));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_UPDATE_TIME));
        keyList.add(K_RECORD_EXPIRE_TIME_SECS);

        ResourceScriptSource resourceScriptSource =
                ResourceScriptSourceUtils.get("lua/redis_record_save_redisson.lua");

        String scriptAsString =
                ResourceScriptSourceUtils.getScriptAsString(resourceScriptSource);
        String sha1 = DigestUtil.sha1Hex(scriptAsString, StandardCharsets.UTF_8.toString()).toLowerCase();

        RScript rScript = redissonClient.getScript(JsonJacksonCodecForIdempotant.INSTANCE);
        List<Boolean> booleans = rScript.scriptExists(sha1);
        if(CollectionUtils.isEmpty(booleans) || !Boolean.TRUE.equals(booleans.get(0))){
            rScript.scriptLoad(scriptAsString);
        }
        Boolean result = rScript.evalSha(
                RScript.Mode.READ_WRITE,
                sha1,
                RScript.ReturnType.BOOLEAN,
                keyList,

                redisIdempotentRecord.getKey(),
                redisIdempotentRecord.getParameterValues(),
                redisIdempotentRecord.getParameterTypes(),
                redisIdempotentRecord.getResult(),
                redisIdempotentRecord.getThrowable(),
                redisIdempotentRecord.getSuccessCount(),
                redisIdempotentRecord.getFailCount(),
                redisIdempotentRecord.getCreateTime(),
                redisIdempotentRecord.getUpdateTime(),
//                lockId,
                (int)expireTimeSecs
        );



        return Boolean.TRUE.equals(result);
    }

    private boolean luaUpdateExpress( RedisIdempotentRecord redisIdempotentRecord, long expireTimeSecs, String lockId) {

        String tableName = getRedisTableName();
        String recordKey = redisIdempotentRecord.getKey();

        List<Object> keyList = new ArrayList<>();
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_KEY));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_PARAMETER_VALUES));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_PARAMETER_TYPES));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_RESULT));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_THROWABLE));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_SUCCESS_COUNT));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_FAIL_COUNT));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_CREATE_TIME));
        keyList.add(propertyUniqueKey(tableName, recordKey, K_RECORD_UPDATE_TIME));
        keyList.add(K_RECORD_EXPIRE_TIME_SECS);

        ResourceScriptSource resourceScriptSource =
                ResourceScriptSourceUtils.get("lua/redis_record_update_redisson.lua");

        String scriptAsString =
                ResourceScriptSourceUtils.getScriptAsString(resourceScriptSource);
        String sha1 = DigestUtil.sha1Hex(scriptAsString, StandardCharsets.UTF_8.toString()).toLowerCase();



        RScript rScript = redissonClient.getScript(JsonJacksonCodecForIdempotant.INSTANCE);
        List<Boolean> booleans = rScript.scriptExists(sha1);
        if(CollectionUtils.isEmpty(booleans) || !Boolean.TRUE.equals(booleans.get(0))){
            rScript.scriptLoad(scriptAsString);
        }
        Boolean result = rScript.evalSha(
                RScript.Mode.READ_WRITE,
                sha1,
                RScript.ReturnType.BOOLEAN,
                keyList,

                redisIdempotentRecord.getKey(),
                redisIdempotentRecord.getParameterValues(),
                redisIdempotentRecord.getParameterTypes(),
                redisIdempotentRecord.getResult(),
                redisIdempotentRecord.getThrowable(),
                redisIdempotentRecord.getSuccessCount(),
                redisIdempotentRecord.getFailCount(),
                redisIdempotentRecord.getCreateTime(),
                redisIdempotentRecord.getUpdateTime(),
//                lockId,
                (int)expireTimeSecs
        );
        return Boolean.TRUE.equals(result);
    }
}
