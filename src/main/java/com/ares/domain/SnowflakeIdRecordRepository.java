package com.ares.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

// 构建动态条件查询两个开源框架：querydsl与persistence
@Repository
public interface SnowflakeIdRecordRepository extends JpaRepository<SnowflakeIdRecordEntity, Long>,
    JpaSpecificationExecutor<SnowflakeIdRecordEntity> {

}
