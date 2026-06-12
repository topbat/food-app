package com.foodapp.file.storage;

/**
 * 对象存储策略接口。
 * 通过配置 foodapp.storage.type（minio / aliyun）选择具体实现（条件装配，二选一生效），
 * 业务层只依赖本接口，切换云厂商无需改动业务代码。
 */
public interface StorageService {

    /**
     * 上传文件到对象存储。
     *
     * @param data        文件字节内容
     * @param objectKey   对象键（命名规则：{bizType}/{yyyy/MM/dd}/{uuid}.{ext}）
     * @param contentType MIME 类型（如 image/png、video/mp4）
     * @return 可公开访问的文件 URL
     */
    String upload(byte[] data, String objectKey, String contentType);

    /**
     * 删除对象存储中的文件（删除失败仅记录日志，不抛异常阻断业务）。
     *
     * @param objectKey 对象键
     */
    void delete(String objectKey);

    /**
     * 获取当前存储类型标识（用于落库 storage_type 字段）。
     *
     * @return 存储类型（minio / aliyun）
     */
    String getStorageType();
}
