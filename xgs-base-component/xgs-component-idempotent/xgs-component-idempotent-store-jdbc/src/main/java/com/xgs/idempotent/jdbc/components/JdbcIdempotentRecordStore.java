package com.xgs.idempotent.jdbc.components;

import com.xgs.idempotent.components.IdempotentRecord;
import com.xgs.idempotent.components.IdempotentRecordStore;
import com.xgs.idempotent.components.IdempotentRequestContext;
import com.xgs.idempotent.inner.IdempotentInterfaceLevelConfig;
import com.xgs.idempotent.inner.utils.DefaultConfigUtils;
import com.xgs.idempotent.inner.utils.GlobalConfigUtils;
import com.xgs.idempotent.jdbc.LockTtlSecsUtils;
import com.xgs.idempotent.jdbc.pojo.JdbcIdempotentRecord;
import com.xgs.idempotent.jdbc.service.JdbcIdempotentRecordService;
import com.xgs.idempotent.jdbc.service.JdbcService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author  xiongguoshuang
 */
public class JdbcIdempotentRecordStore implements IdempotentRecordStore {


    private final JdbcIdempotentRecordService jdbcIdempotentRecordService;

    private final JdbcService jdbcService;

    public JdbcIdempotentRecordStore(
            JdbcTemplate jdbcTemplate,
            JdbcIdempotentRecordService jdbcIdempotentRecordService,
            String tableName
    ){

        this.jdbcIdempotentRecordService = jdbcIdempotentRecordService;
        this.jdbcService = new JdbcService(jdbcTemplate,tableName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean tryLock(String lockId, String recordId, IdempotentRequestContext idempotentRequestContext) {
        if(StringUtils.isEmpty(lockId)){
            throw new RuntimeException("lockId can't be empty");
        }

        long distributedLockTtlSecs = LockTtlSecsUtils.calDistributedLockTtlSecs(idempotentRequestContext);
        long tryDistributedLockTimeOutMills = LockTtlSecsUtils.calTryDistributedLockTimeOutMills(idempotentRequestContext);

        long timeOutStamp = System.currentTimeMillis() + tryDistributedLockTimeOutMills;
        while (System.currentTimeMillis() < timeOutStamp){
            boolean lockOk = lockItem(lockId, recordId, distributedLockTtlSecs * 1000);
            if(lockOk){
                return true;
            }else{
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private boolean lockItem(String lockId, String recordKey, long lockMillis){
        long curTime = System.currentTimeMillis();
        Date curDate = new Date(curTime);
        long lockExpiredMillis = curTime + lockMillis;
        JdbcIdempotentRecord jdbcIdempotentRecord = this.jdbcService.queryByKey(recordKey);

        if(jdbcIdempotentRecord == null){
            jdbcIdempotentRecord = new JdbcIdempotentRecord();
            jdbcIdempotentRecord.setKey(recordKey);
            jdbcIdempotentRecord.setSuccessCount(0);
            jdbcIdempotentRecord.setFailCount(0);
            jdbcIdempotentRecord.setCreateTime(curDate);
            jdbcIdempotentRecord.setUpdateTime(curDate);
            jdbcIdempotentRecord.setParameterValues(null);
            jdbcIdempotentRecord.setParameterTypes(null);
            jdbcIdempotentRecord.setResult(null);
            jdbcIdempotentRecord.setLockId(lockId);
            jdbcIdempotentRecord.setLockExpiredMillis(lockExpiredMillis);

            boolean insertOk = this.jdbcService.insert(jdbcIdempotentRecord);
            return insertOk;
        }else if(
                StringUtils.isEmpty(jdbcIdempotentRecord.getLockId())
                        || lockId.equals(jdbcIdempotentRecord.getLockId())
                        || jdbcIdempotentRecord.getLockExpiredMillis() == null || jdbcIdempotentRecord.getLockExpiredMillis() < curTime
        ){
            jdbcIdempotentRecord.setLockId(lockId);
            jdbcIdempotentRecord.setLockExpiredMillis(lockExpiredMillis);
            boolean updateOk = this.jdbcService.update(jdbcIdempotentRecord);
            return updateOk;
        }
        return false;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean releaseLock(String lockId, String recordId, IdempotentRequestContext idempotentRequestContext) {
        if(StringUtils.isEmpty(lockId)){
            throw new RuntimeException("lockId can't be empty");
        }

        JdbcIdempotentRecord jdbcIdempotentRecord = this.jdbcService.queryByKey(recordId);

        if(jdbcIdempotentRecord == null
                || StringUtils.isEmpty(jdbcIdempotentRecord.getLockId())
                || !lockId.equals(jdbcIdempotentRecord.getLockId())
        ){
            return false;
        }
        jdbcIdempotentRecord.setLockId(null);
        jdbcIdempotentRecord.setLockExpiredMillis(null);
        boolean updateOk = this.jdbcService.update(jdbcIdempotentRecord);
        return updateOk;
    }


    /**
     * 计算记录超时秒数
     * @param idempotentRequestContext
     * @return
     */
    private long calRecordTtlSecs(IdempotentRequestContext idempotentRequestContext){
        IdempotentInterfaceLevelConfig idempotentInterfaceLevelConfig = idempotentRequestContext.levelConfig();
        if(idempotentInterfaceLevelConfig.getRecordTtlSecs() != null){

            return idempotentInterfaceLevelConfig.getRecordTtlSecs();

        }else if(GlobalConfigUtils.getInstance().getRecordTtlSecs() != null){
            return GlobalConfigUtils.getInstance().getRecordTtlSecs();
        }
        return DefaultConfigUtils.getInstance().getRecordTtlSecs();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IdempotentRecord emptyRecord(String lockId, String recordId, IdempotentRequestContext idempotentRequestContext) {

        if(StringUtils.isEmpty(lockId)){
            throw new RuntimeException("lockId can't be empty");
        }
        long curTime = System.currentTimeMillis();
        Date curDate = new Date(curTime);
        JdbcIdempotentRecord jdbcIdempotentRecord = this.jdbcService.queryByKey(recordId);
        if(jdbcIdempotentRecord == null || !lockId.equals(jdbcIdempotentRecord.getLockId())){
            return null;
        }
        jdbcIdempotentRecord.setKey(recordId);
        jdbcIdempotentRecord.setSuccessCount(0);
        jdbcIdempotentRecord.setFailCount(0);
        jdbcIdempotentRecord.setCreateTime(curDate);
        jdbcIdempotentRecord.setUpdateTime(curDate);
        jdbcIdempotentRecord.setParameterValues(null);
        jdbcIdempotentRecord.setParameterTypes(null);
        jdbcIdempotentRecord.setResult(null);
        jdbcIdempotentRecord.setLockId(lockId);
        long lockExpiredMillis = curTime + this.calRecordTtlSecs(idempotentRequestContext);
        jdbcIdempotentRecord.setLockExpiredMillis(lockExpiredMillis);

        boolean updateOk = this.jdbcService.update(jdbcIdempotentRecord);
        if(updateOk){
            return jdbcIdempotentRecordService.toIdempotentRecord(jdbcIdempotentRecord);
        }
        return null;
    }

    /**
     * TODO 检查是否相等
     * @param jdbcIdempotentRecord
     * @param expectIdempotentRecord
     * @return
     */
    private boolean checkEqual(JdbcIdempotentRecord jdbcIdempotentRecord,JdbcIdempotentRecord expectIdempotentRecord){
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRecord(String lockId, IdempotentRecord oldRecord, @NotNull IdempotentRecord newRecord, IdempotentRequestContext idempotentRequestContext) {

        if(StringUtils.isEmpty(lockId)){
            throw new RuntimeException("lockId can't be empty");
        }

        JdbcIdempotentRecord jdbcIdempotentRecord = this.jdbcService.queryByKey(oldRecord.getKey());

        long lockExpiredMillis = jdbcIdempotentRecord.getLockExpiredMillis() == null ? 0L : jdbcIdempotentRecord.getLockExpiredMillis();

        if(StringUtils.isEmpty(jdbcIdempotentRecord.getLockId())
                || !lockId.equals(jdbcIdempotentRecord.getLockId())
                || lockExpiredMillis < System.currentTimeMillis()
        ){
            return false;
        }

        JdbcIdempotentRecord expectOldIdempotentRecord = jdbcIdempotentRecordService.toJdbcIdempotentRecord(oldRecord);
        if(!checkEqual(jdbcIdempotentRecord,expectOldIdempotentRecord)){
            return false;
        }

        JdbcIdempotentRecord newJdbcIdempotentRecord = jdbcIdempotentRecordService.toJdbcIdempotentRecord(newRecord);
        newJdbcIdempotentRecord.setLockId(lockId);
        return this.jdbcService.update(newJdbcIdempotentRecord);
    }


    @Override
    public IdempotentRecord queryRecordByKey(@NotBlank String recordId, IdempotentRequestContext idempotentRequestContext) {
        JdbcIdempotentRecord redisIdempotentRecord = this.jdbcService.queryByKey(recordId);
        if(redisIdempotentRecord == null){
            return null;
        }
        return jdbcIdempotentRecordService.toIdempotentRecord(redisIdempotentRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteExpiredIdempotentRecord(String lockId, IdempotentRecord oldRecord, IdempotentRequestContext idempotentRequestContext) {
        if(StringUtils.isEmpty(lockId)){
            throw new RuntimeException("lockId can't be empty");
        }

        JdbcIdempotentRecord jdbcIdempotentRecord = this.jdbcService.queryByKey(oldRecord.getKey());

        long lockExpiredMillis = jdbcIdempotentRecord.getLockExpiredMillis() == null ? 0L : jdbcIdempotentRecord.getLockExpiredMillis();

        if(StringUtils.isEmpty(jdbcIdempotentRecord.getLockId())
                || !lockId.equals(jdbcIdempotentRecord.getLockId())
                || lockExpiredMillis < System.currentTimeMillis()
        ){
            return false;
        }

        JdbcIdempotentRecord expectOldIdempotentRecord = jdbcIdempotentRecordService.toJdbcIdempotentRecord(oldRecord);
        if(!checkEqual(jdbcIdempotentRecord,expectOldIdempotentRecord)){
            return false;
        }
        return this.jdbcService.deleteByKey(oldRecord.getKey());
    }


}
