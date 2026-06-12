package com.foodapp.file.repository;

import com.foodapp.file.entity.FileRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 文件上传记录数据访问层。
 */
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {

    /**
     * 按上传者分页查询上传记录（按上传时间倒序由调用方通过 Pageable 指定）。
     *
     * @param userId   上传者用户ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<FileRecord> findByUserId(Long userId, Pageable pageable);
}
