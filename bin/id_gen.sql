CREATE TABLE IF NOT EXISTS `snd_snowflake_generator`
(
    `id` bigint NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `biz_type` varchar(64) NOT NULL DEFAULT 0 COMMENT '活动状态',
    `sequence` bigint NOT NULL DEFAULT 0 COMMENT '序列号',
    `worker_id` bigint NOT NULL DEFAULT 1 COMMENT '工具节点',
    `data_center_id` bigint NOT NULL DEFAULT 1 COMMENT '数据中心',
    `last_timestamp` bigint NOT NULL DEFAULT -1 COMMENT '上次更新时间',
    `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_biz_type_center_id_worker_id` ( `biz_type`, `data_center_id`, `worker_id`),
    KEY `idx_last_timestamp` ( `last_timestamp`),
    KEY `idx_ctime` ( `ctime`)
) ENGINE = InnoDB
    AUTO_INCREMENT = 1
    DEFAULT CHARSET = utf8mb4 COMMENT ='雪花算法生成器状态表';


CREATE TABLE IF NOT EXISTS `snd_snowflake_record`
(
    `id` bigint NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `gen_id` bigint NOT NULL DEFAULT 0 COMMENT '生成id值',
    `biz_type` varchar(64) NOT NULL DEFAULT 0 COMMENT '活动状态',
    `sequence` bigint NOT NULL DEFAULT 0 COMMENT '序列号',
    `worker_id` bigint NOT NULL DEFAULT 1 COMMENT '工具节点',
    `data_center_id` bigint NOT NULL DEFAULT 1 COMMENT '数据中心',
    `last_timestamp` bigint NOT NULL DEFAULT -1 COMMENT '上次更新时间',
    `ctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `mtime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_biz_type` (`biz_type`),
    KEY `idx_last_timestamp` (`last_timestamp`),
    KEY `idx_ctime` ( `ctime`)
) ENGINE = InnoDB
    AUTO_INCREMENT = 1
    DEFAULT CHARSET = utf8mb4 COMMENT ='雪花算法生成id表';

