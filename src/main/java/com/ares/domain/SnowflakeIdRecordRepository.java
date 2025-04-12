package com.ares.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnowflakeIdRecordRepository extends JpaRepository<SnowflakeIdRecordEntity, Long> {

}
