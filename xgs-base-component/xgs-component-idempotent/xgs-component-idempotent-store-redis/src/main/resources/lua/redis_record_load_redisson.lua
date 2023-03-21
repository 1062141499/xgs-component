redis.log(redis.LOG_WARNING,"============================= redis_record_load start ==================================")


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

local value_record_key = redis.call('GET', key_record_key)
if not value_record_key then
    return { nil, nil, nil, nil, nil, nil, nil, nil, nil }
end
redis.log(redis.LOG_WARNING,"value_record_key = ".. value_record_key)
local value_record_parameterValues = redis.call('GET', key_record_parameterValues)
redis.log(redis.LOG_WARNING,"value_record_parameterValues = ".. value_record_parameterValues)
local value_record_parameterTypes = redis.call('GET', key_record_parameterTypes)
redis.log(redis.LOG_WARNING,"value_record_parameterTypes = ".. value_record_parameterTypes)
local value_record_result = redis.call('GET', key_record_result)
redis.log(redis.LOG_WARNING,"value_record_result = ".. value_record_result)
local value_record_throwable = redis.call('GET', key_record_throwable)
redis.log(redis.LOG_WARNING,"value_record_throwable = ".. value_record_throwable)
local value_record_successCount = redis.call('GET', key_record_successCount)
redis.log(redis.LOG_WARNING,"value_record_successCount = ".. value_record_successCount)
local value_record_failCount = redis.call('GET', key_record_failCount)
redis.log(redis.LOG_WARNING,"value_record_failCount = ".. value_record_failCount)
local value_record_createTime = redis.call('GET', key_record_createTime)
redis.log(redis.LOG_WARNING,"value_record_createTime = ".. value_record_createTime)
local value_record_updateTime = redis.call('GET', key_record_updateTime)
redis.log(redis.LOG_WARNING,"value_record_updateTime = ".. value_record_updateTime)

redis.log(redis.LOG_WARNING,"============================= redis_record_load end ==================================")

return { value_record_key, value_record_parameterValues, value_record_parameterTypes, value_record_result, value_record_throwable, value_record_successCount, value_record_failCount, value_record_createTime, value_record_updateTime }