package com.ares.service;

import com.ares.domain.SnowflakeIdRecordEntity;
import com.ares.domain.SnowflakeIdRecordRepository;
import com.ares.domain.helper.JpaQueryHelper;
import com.ares.model.PageItem;
import com.ares.model.PageResponse;
import com.ares.model.Response;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class IdGeneratorAdminService {

  private Logger logger = LoggerFactory.getLogger(IdGeneratorAdminService.class);

  private final SnowflakeIdRecordRepository SnowflakeIdRepository;

  public IdGeneratorAdminService(SnowflakeIdRecordRepository snowflakeIdRecordRepository) {
    this.SnowflakeIdRepository = snowflakeIdRecordRepository;
  }

  public Response<?> get(Pageable pageable) {
    Specification<SnowflakeIdRecordEntity> spec = JpaQueryHelper.and(
        JpaQueryHelper.lessThanOrEquals("ctime", LocalDateTime.now())
    );
    int pageNo = pageable.getPageNumber() >= 1 ? pageable.getPageNumber() - 1 : 0;
    pageable.withPage(pageNo);
    Page<SnowflakeIdRecordEntity> pages = SnowflakeIdRepository.findAll(spec, pageable);
    return new PageResponse<>(
        pages.getContent(),
        new PageItem(pages.getNumber() + 1, pages.getSize(), pages.getTotalPages(),
            pages.getTotalElements()));
  }
}
