CREATE TABLE `idempotent_record` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `key` varchar(255) NOT NULL COMMENT ' 记录键',
  `is_success` tinyint(255) DEFAULT NULL COMMENT '0-false,1-true',
  `fail_count` int(11) NOT NULL DEFAULT '0' COMMENT '已失败次数',
  `parameter_values` blob COMMENT '参数值列表',
  `parameter_types` varchar(1024) DEFAULT NULL COMMENT '参数类型列表',
  `result` blob COMMENT '结果',
  `lock_id` varchar(255) DEFAULT NULL COMMENT '当前锁id',
  `lock_expired_millis` bigint(255) DEFAULT NULL COMMENT '锁过期时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk-key` (`key`) COMMENT 'key唯一索引'
) ENGINE=InnoDB COMMENT='幂等记录表';