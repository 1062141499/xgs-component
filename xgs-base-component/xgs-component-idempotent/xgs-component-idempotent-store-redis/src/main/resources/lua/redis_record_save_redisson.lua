
redis.log(redis.LOG_WARNING,"============================= redis_record_save start ==================================")


local key_record_key = KEYS[1]
redis.log(redis.LOG_WARNING,"key_record_key = ".. key_record_key)
local key_record_parameterValues = KEYS[2]
redis.log(redis.LOG_WARNING,"key_record_parameterValues = ".. key_record_parameterValues)
local key_record_parameterTypes = KEYS[3]
redis.log(redis.LOG_WARNING,"key_record_parameterTypes = ".. key_record_parameterTypes)
local key_record_result = KEYS[4]
redis.log(redis.LOG_WARNING,"key_record_result = ".. key_record_result)
local key_record_throwable = KEYS[5]
redis.log(redis.LOG_WARNING,"key_record_throwable = ".. key_record_throwable)
local key_record_successCount = KEYS[6]
redis.log(redis.LOG_WARNING,"key_record_successCount = ".. key_record_successCount)
local key_record_failCount = KEYS[7]
redis.log(redis.LOG_WARNING,"key_record_failCount = ".. key_record_failCount)
local key_record_createTime = KEYS[8]
redis.log(redis.LOG_WARNING,"key_record_createTime = ".. key_record_createTime)
local key_record_updateTime = KEYS[9]
redis.log(redis.LOG_WARNING,"key_record_updateTime = ".. key_record_updateTime)
-- local key_record_lock = KEYS[10]
-- redis.log(redis.LOG_WARNING,"key_record_lock = ".. key_record_lock)
local key_record_expireTimeSecs = KEYS[10]
redis.log(redis.LOG_WARNING,"key_record_expireTimeSecs = ".. key_record_expireTimeSecs)

local value_record_key = ARGV[1]
redis.log(redis.LOG_WARNING,"value_record_key = ".. value_record_key)
local value_record_parameterValues = ARGV[2]
redis.log(redis.LOG_WARNING,"value_record_parameterValues = ".. value_record_parameterValues)
local value_record_parameterTypes = ARGV[3]
redis.log(redis.LOG_WARNING,"value_record_parameterTypes = ".. value_record_parameterTypes)
local value_record_result = ARGV[4]
redis.log(redis.LOG_WARNING,"value_record_result = ".. value_record_result)
local value_record_throwable = ARGV[5]
redis.log(redis.LOG_WARNING,"value_record_throwable = ".. value_record_throwable)
local value_record_successCount = ARGV[6]
redis.log(redis.LOG_WARNING,"value_record_successCount = ".. value_record_successCount)
local value_record_failCount = ARGV[7]
redis.log(redis.LOG_WARNING,"value_record_failCount = ".. value_record_failCount)
local value_record_createTime = ARGV[8]
redis.log(redis.LOG_WARNING,"value_record_createTime = ".. value_record_createTime)
local value_record_updateTime = ARGV[9]
redis.log(redis.LOG_WARNING,"value_record_updateTime = ".. value_record_updateTime)
-- local value_record_lock = ARGV[10]
-- redis.log(redis.LOG_WARNING,"value_record_lock = ".. value_record_lock)
local value_record_expireTimeSecs = ARGV[10]
redis.log(redis.LOG_WARNING,"value_record_expireTimeSecs = ".. value_record_expireTimeSecs)


-- local value_current_lock = redis.call('GET', key_record_lock)
-- --在lua中，除了nil和false，其他的值都为真，包括0，可以通过nil为false这一点来判断是否为空
-- if not value_current_lock then
--     value_current_lock =''
-- end
-- redis.log(redis.LOG_WARNING,"value_current_lock = "..value_current_lock)

local result=0;
-- if value_current_lock~=value_record_lock then
--     result=0
-- else
--     redis.call('SETEX', key_record_key, value_record_expireTimeSecs, value_record_key)
--     redis.call('SETEX', key_record_parameterValues, value_record_expireTimeSecs, value_record_parameterValues)
--     redis.call('SETEX', key_record_parameterTypes, value_record_expireTimeSecs, value_record_parameterTypes)
--     redis.call('SETEX', key_record_result, value_record_expireTimeSecs, value_record_result)
--     redis.call('SETEX', key_record_throwable, value_record_expireTimeSecs, value_record_throwable)
--     redis.call('SETEX', key_record_successCount, value_record_expireTimeSecs, value_record_successCount)
--     redis.call('SETEX', key_record_failCount, value_record_expireTimeSecs, value_record_failCount)
--     redis.call('SETEX', key_record_createTime, value_record_expireTimeSecs, value_record_createTime)
--     redis.call('SETEX', key_record_updateTime, value_record_expireTimeSecs, value_record_updateTime)
--
--     result=1
-- end
redis.call('SETEX', key_record_key, value_record_expireTimeSecs, value_record_key)
redis.call('SETEX', key_record_parameterValues, value_record_expireTimeSecs, value_record_parameterValues)
redis.call('SETEX', key_record_parameterTypes, value_record_expireTimeSecs, value_record_parameterTypes)
redis.call('SETEX', key_record_result, value_record_expireTimeSecs, value_record_result)
redis.call('SETEX', key_record_throwable, value_record_expireTimeSecs, value_record_throwable)
redis.call('SETEX', key_record_successCount, value_record_expireTimeSecs, value_record_successCount)
redis.call('SETEX', key_record_failCount, value_record_expireTimeSecs, value_record_failCount)
redis.call('SETEX', key_record_createTime, value_record_expireTimeSecs, value_record_createTime)
redis.call('SETEX', key_record_updateTime, value_record_expireTimeSecs, value_record_updateTime)
result=1

redis.log(redis.LOG_WARNING,"============================= redis_record_save end ==================================")
return result