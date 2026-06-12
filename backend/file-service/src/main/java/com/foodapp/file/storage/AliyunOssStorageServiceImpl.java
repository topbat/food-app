package com.foodapp.file.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

/**
 * 阿里云 OSS 对象存储实现。
 * 启用方式：配置 foodapp.storage.type=aliyun，并提供 endpoint / bucket / AK / SK。
 * 访问 URL 形如 https://{bucket}.{endpoint}/{objectKey}（bucket 需开通公共读）。
 */
@Service
@ConditionalOnProperty(name = "foodapp.storage.type", havingValue = "aliyun")
public class AliyunOssStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(AliyunOssStorageServiceImpl.class);

    /** OSS 不可用时的友好提示 */
    private static final String UNAVAILABLE_MSG = "对象存储服务不可用，请检查阿里云 OSS 配置与网络";

    /** OSS Endpoint（如 oss-cn-hangzhou.aliyuncs.com，可带 https:// 前缀） */
    @Value("${foodapp.storage.aliyun.endpoint:oss-cn-hangzhou.aliyuncs.com}")
    private String endpoint;

    /** OSS Bucket 名称（需设置公共读权限） */
    @Value("${foodapp.storage.aliyun.bucket:food-app}")
    private String bucket;

    /** 阿里云 AccessKey ID（通过环境变量注入，禁止硬编码） */
    @Value("${foodapp.storage.aliyun.access-key-id:}")
    private String accessKeyId;

    /** 阿里云 AccessKey Secret（通过环境变量注入，禁止硬编码） */
    @Value("${foodapp.storage.aliyun.access-key-secret:}")
    private String accessKeySecret;

    /** 懒初始化的 OSS 客户端 */
    private volatile OSS client;

    /**
     * 懒获取 OSS 客户端：双重检查锁，首次上传时创建，避免启动期即依赖外部服务。
     *
     * @return OSS 客户端实例
     */
    private OSS getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    log.info("[存储] 初始化阿里云OSS客户端: endpoint={}, bucket={}", endpoint, bucket);
                    client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
                }
            }
        }
        return client;
    }

    /**
     * 去掉 endpoint 中可能携带的协议前缀，用于拼接公网访问域名。
     *
     * @return 不含协议的 endpoint（如 oss-cn-hangzhou.aliyuncs.com）
     */
    private String plainEndpoint() {
        return endpoint.replaceFirst("^https?://", "");
    }

    /**
     * 上传文件到阿里云 OSS，返回公网访问 URL（https://{bucket}.{endpoint}/{key}）。
     *
     * @param data        文件字节内容
     * @param objectKey   对象键
     * @param contentType MIME 类型
     * @return 访问 URL
     */
    @Override
    public String upload(byte[] data, String objectKey, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(data.length);
            getClient().putObject(bucket, objectKey, new ByteArrayInputStream(data), metadata);
            String url = "https://" + bucket + "." + plainEndpoint() + "/" + objectKey;
            log.info("[存储] OSS 上传成功: key={}, size={}B", objectKey, data.length);
            return url;
        } catch (Exception e) {
            // 关键判断：OSS 调用失败（鉴权错误/网络不通），统一转友好提示
            log.warn("[存储] OSS 上传失败: key={}, 原因={}", objectKey, e.getMessage());
            throw new BusinessException(ResultCode.SERVER_ERROR, UNAVAILABLE_MSG);
        }
    }

    /**
     * 删除 OSS 中的对象（失败仅告警，不阻断业务）。
     *
     * @param objectKey 对象键
     */
    @Override
    public void delete(String objectKey) {
        try {
            getClient().deleteObject(bucket, objectKey);
            log.info("[存储] OSS 删除成功: key={}", objectKey);
        } catch (Exception e) {
            log.warn("[存储] OSS 删除失败: key={}, 原因={}", objectKey, e.getMessage());
        }
    }

    /**
     * 获取存储类型标识。
     *
     * @return "aliyun"
     */
    @Override
    public String getStorageType() {
        return "aliyun";
    }
}
