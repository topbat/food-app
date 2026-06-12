package com.foodapp.file.vo;

import com.foodapp.file.entity.FileRecord;

import java.time.LocalDateTime;

/**
 * 文件记录视图对象：上传响应与「我的上传记录」列表项共用。
 * 字段结构遵循接口契约：{id,url,thumbUrl,originalName,contentType,sizeBytes,storageType,bizType}。
 */
public class FileVO {

    /** 记录ID */
    private Long id;
    /** 文件访问地址 */
    private String url;
    /** 缩略图地址（原 URL 去扩展名 + _thumb.jpg；生成失败为 null） */
    private String thumbUrl;
    /** 原文件名 */
    private String originalName;
    /** MIME 类型 */
    private String contentType;
    /** 文件大小（字节） */
    private Long sizeBytes;
    /** 存储类型（minio/aliyun） */
    private String storageType;
    /** 业务类型（avatar/recipe/post/step/common） */
    private String bizType;
    /** 上传时间 */
    private LocalDateTime createdAt;

    /**
     * 由实体转换为视图对象。
     *
     * @param record 文件记录实体
     * @return 视图对象
     */
    public static FileVO from(FileRecord record) {
        FileVO vo = new FileVO();
        vo.setId(record.getId());
        vo.setUrl(record.getUrl());
        vo.setThumbUrl(record.getThumbUrl());
        vo.setOriginalName(record.getOriginalName());
        vo.setContentType(record.getContentType());
        vo.setSizeBytes(record.getSizeBytes());
        vo.setStorageType(record.getStorageType());
        vo.setBizType(record.getBizType());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getThumbUrl() { return thumbUrl; }
    public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
