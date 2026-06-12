package com.foodapp.file.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 文件上传记录实体，对应表 file_record。
 */
@Entity
@Table(name = "file_record", indexes = {
        @Index(name = "idx_file_user_id", columnList = "user_id"),
        @Index(name = "idx_file_biz_type", columnList = "biz_type")
})
@Comment("文件上传记录表")
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("上传者用户ID")
    private Long userId;

    @Column(name = "biz_type", nullable = false, length = 20)
    @Comment("业务类型（avatar头像 recipe菜谱 post帖子 step步骤 common通用）")
    private String bizType;

    @Column(name = "original_name", length = 255)
    @Comment("原文件名")
    private String originalName;

    @Column(name = "object_key", nullable = false, length = 255)
    @Comment("对象键（命名规则：{bizType}/{yyyy/MM/dd}/{uuid}.{ext}）")
    private String objectKey;

    @Column(name = "url", nullable = false, length = 500)
    @Comment("文件访问地址")
    private String url;

    @Column(name = "thumb_url", length = 500)
    @Comment("缩略图访问地址（同路径 {uuid}_thumb.jpg；生成失败为NULL）")
    private String thumbUrl;

    @Column(name = "content_type", length = 100)
    @Comment("MIME类型（如 image/png、video/mp4）")
    private String contentType;

    @Column(name = "size_bytes")
    @Comment("文件大小（字节）")
    private Long sizeBytes;

    @Column(name = "storage_type", length = 20)
    @Comment("存储类型（minio/aliyun）")
    private String storageType;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Comment("上传时间")
    private LocalDateTime createdAt;

    /**
     * 持久化前自动填充上传时间。
     */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
