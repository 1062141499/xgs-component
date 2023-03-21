package com.xgs.idempotent.jdbc.service;


import com.xgs.idempotent.jdbc.pojo.JdbcIdempotentRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcService {

    private final JdbcTemplate jdbcTemplate;

    private final String tableName;

    private final static String INSERT_SQL = "insert %s(`key`, `is_success`, `fail_count`, `create_time`, `update_time`, `parameter_values`, `parameter_types`, `result`, `lock_id`, `lock_expired_millis`) " +
            "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    private final static String UPDATE_SQL = "update %s set `is_success`=?, `fail_count`=?, `update_time`=?, `parameter_values`=?, `parameter_types`=?, `result`=?, `lock_id`=?, `lock_expired_millis`=? " +
            "where `key`=?";

    private final static String SELECT_SQL = "select `id`, `key`, `is_success`, `fail_count`, `create_time`, `update_time`, `parameter_values`, `parameter_types`, `result`, `lock_id`, `lock_expired_millis` " +
            "from %s where `key`=?";


    private final static String DELETE_SQL = "delete " +
            "from %s where `key`=?";

    public JdbcService(JdbcTemplate jdbcTemplate,String tableName){
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }

    public boolean insert(JdbcIdempotentRecord jdbcIdempotentRecord){

        String sql = String.format(INSERT_SQL,tableName);
        int updateCount = jdbcTemplate.update(
                sql,
                jdbcIdempotentRecord.getKey(),
                jdbcIdempotentRecord.getSuccessCount(),
                jdbcIdempotentRecord.getFailCount(),
                jdbcIdempotentRecord.getCreateTime(),
                jdbcIdempotentRecord.getUpdateTime(),
                jdbcIdempotentRecord.getParameterValues(),
                jdbcIdempotentRecord.getParameterTypes(),
                jdbcIdempotentRecord.getResult(),
                jdbcIdempotentRecord.getLockId(),
                jdbcIdempotentRecord.getLockExpiredMillis()
        );
        return updateCount == 1;
    }
    public boolean update(JdbcIdempotentRecord jdbcIdempotentRecord){
        String sql = String.format(UPDATE_SQL,tableName);
        int updateCount = jdbcTemplate.update(
                sql,
                jdbcIdempotentRecord.getSuccessCount(),
                jdbcIdempotentRecord.getFailCount(),
                jdbcIdempotentRecord.getUpdateTime(),
                jdbcIdempotentRecord.getParameterValues(),
                jdbcIdempotentRecord.getParameterTypes(),
                jdbcIdempotentRecord.getResult(),
                jdbcIdempotentRecord.getLockId(),
                jdbcIdempotentRecord.getLockExpiredMillis(),


                jdbcIdempotentRecord.getKey()
        );
        return updateCount == 1;
    }
    public JdbcIdempotentRecord queryByKey(String key){
        String sql = String.format(SELECT_SQL,tableName);
        List<JdbcIdempotentRecord> recordList = jdbcTemplate.query(
                sql,
                new RowMapper<JdbcIdempotentRecord>(){
                    @Override
                    public JdbcIdempotentRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
                        JdbcIdempotentRecord result = new JdbcIdempotentRecord();
                        result.setId(rs.getLong("id"));
                        result.setKey(rs.getString("key"));
                        result.setSuccessCount(rs.getInt("success_count"));
                        result.setFailCount(rs.getInt("fail_count"));
                        result.setCreateTime(rs.getDate("create_time"));
                        result.setUpdateTime(rs.getDate("update_time"));
                        result.setParameterValues(rs.getBytes("parameter_values"));
                        result.setParameterTypes(rs.getString("parameter_types"));
                        result.setResult(rs.getBytes("result"));
                        result.setLockId(rs.getString("lock_id"));
                        result.setLockExpiredMillis(rs.getLong("lock_expired_millis"));
                        return result;
                    }
                },
                key
        );
        return recordList != null && !recordList.isEmpty() ? recordList.get(0): null;
    }


    public boolean deleteByKey(String key){
        String sql = String.format(DELETE_SQL,tableName);
        return jdbcTemplate.update(sql,key) >0;
    }
}
