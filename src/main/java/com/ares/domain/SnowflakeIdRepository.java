package com.ares.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

@Repository
public interface SnowflakeIdRepository extends JpaRepository<SnowflakeIdGeneratorEntity, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT e FROM SnowflakeIdGeneratorEntity e WHERE e.bizType= :bizType and e.workerId= :workerId and e.dataCenterId= :dataCenterId ORDER BY e.id DESC limit 1")
  Optional<SnowflakeIdGeneratorEntity> findByBizType(String bizType, Long workerId,
      Long dataCenterId);

}
