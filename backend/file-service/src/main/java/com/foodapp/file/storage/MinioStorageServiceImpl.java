package com.foodapp.file.storage;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

/**
 * MinIO 对象存储实现（默认策略）。
 * 设计要点：
 * 1. 客户端与 bucket 懒初始化 —— 即使本机 MinIO 未启动，服务也能正常起来，
 *    只有在真正上传时才连接 MinIO，连不上则返回 50000 + 友好中文提示；
 * 2. bucket 不存在时自动创建，并设置「公共读」策略，保证返回的 URL 可直接访问。
 */
@Service
@ConditionalOnProperty(name = "foodapp.storage.type", havingValue = "minio", matchIfMissing = true)
public class MinioStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageServiceImpl.class);

    /** MinIO 服务不可用时的友好提示（不暴露内部异常细节） */
    private static final String UNAVAILABLE_MSG = "对象存储服务不可用，请先启动 MinIO";

    /** MinIO 服务地址（dev 默认本机 9000 端口） */
    @Value("${foodapp.storage.minio.endpoint:http://localhost:9000}")
    private String endpoint;

    /** MinIO 访问密钥 */
    @Value("${foodapp.storage.minio.access-key:minioadmin}")
    private String accessKey;

    /** MinIO 私有密钥 */
    @Value("${foodapp.storage.minio.secret-key:minioadmin}")
    private String secretKey;

    /** 存储桶名称 */
    @Value("${foodapp.storage.minio.bucket:food-app}")
    private String bucket;

    /** 懒初始化的 MinIO 客户端（首次上传时创建） */
    private volatile MinioClient client;

    /** bucket 是否已确认就绪（存在且公共读策略已设置） */
    private volatile boolean bucketReady = false;

    /**
     * 懒获取 MinIO 客户端：双重检查锁，首次调用时构建。
     * 注意：MinioClient.builder() 本身不发起网络连接，构建失败仅可能是配置非法。
     *
     * @return MinIO 客户端实例
     */
    private MinioClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    log.info("[存储] 初始化MinIO客户端: endpoint={}, bucket={}", endpoint, bucket);
                    client = MinioClient.builder()
                            .endpoint(endpoint)
                            .credentials(accessKey, secretKey)
                            .build();
                }
            }
        }
        return client;
    }

    /**
     * 确保 bucket 就绪：不存在则自动创建并设置公共读策略。
     * 该步骤需要真实连接 MinIO，MinIO 未启动时在此处转为友好业务异常。
     */
    private synchronized void ensureBucket() {
        if (bucketReady) {
            return;
        }
        try {
            boolean exists = getClient().bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            // 关键判断：bucket 不存在则自动创建并开放公共读
            if (!exists) {
                log.info("[存储] bucket [{}] 不存在，自动创建并设置公共读策略", bucket);
                getClient().makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                getClient().setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucket)
                        .config(publicReadPolicy())
                        .build());
            }
            bucketReady = true;
            log.info("[存储] MinIO bucket [{}] 就绪", bucket);
        } catch (Exception e) {
            // 关键判断：连接失败（MinIO 未启动 / 网络不通），返回友好提示而非堆栈
            log.warn("[存储] MinIO 连接失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.SERVER_ERROR, UNAVAILABLE_MSG);
        }
    }

    /**
     * 生成「公共读」bucket 策略 JSON（允许匿名 GetObject）。
     *
     * @return 策略 JSON 字符串
     */
    private String publicReadPolicy() {
        return "{\"Version\":\"2012-10-17\",\"Statement\":[{"
                + "\"Effect\":\"Allow\","
                + "\"Principal\":{\"AWS\":[\"*\"]},"
                + "\"Action\":[\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]}]}";
    }

    /**
     * 上传文件到 MinIO，返回可公开访问的 URL（{endpoint}/{bucket}/{objectKey}）。
     *
     * @param data        文件字节内容
     * @param objectKey   对象键
     * @param contentType MIME 类型
     * @return 访问 URL
     */
    @Override
    public String upload(byte[] data, String objectKey, String contentType) {
        ensureBucket();
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            getClient().putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(in, data.length, -1)
                    .contentType(contentType)
                    .build());
            String url = endpoint + "/" + bucket + "/" + objectKey;
            log.info("[存储] MinIO 上传成功: key={}, size={}B", objectKey, data.length);
            return url;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // 关键判断：上传过程异常（多为 MinIO 中途宕机/网络抖动），统一转友好提示
            log.warn("[存储] MinIO 上传失败: key={}, 原因={}", objectKey, e.getMessage());
            throw new BusinessException(ResultCode.SERVER_ERROR, UNAVAILABLE_MSG);
        }
    }

    /**
     * 删除 MinIO 中的对象（失败仅告警，不阻断业务）。
     *
     * @param objectKey 对象键
     */
    @Override
    public void delete(String objectKey) {
        try {
            getClient().removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
            log.info("[存储] MinIO 删除成功: key={}", objectKey);
        } catch (Exception e) {
            log.warn("[存储] MinIO 删除失败: key={}, 原因={}", objectKey, e.getMessage());
        }
    }

    /**
     * 获取存储类型标识。
     *
     * @return "minio"
     */
    @Override
    public String getStorageType() {
        return "minio";
    }
}
