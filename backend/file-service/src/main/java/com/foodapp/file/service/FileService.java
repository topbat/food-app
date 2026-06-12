package com.foodapp.file.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.file.entity.FileRecord;
import com.foodapp.file.repository.FileRecordRepository;
import com.foodapp.file.storage.StorageService;
import com.foodapp.file.vo.FileVO;
import com.foodapp.file.vo.PageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传业务层：类型/大小校验 → 对象键生成 → 对象存储上传 → 缩略图生成 → 记录落库。
 */
@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    /** 允许的图片扩展名 */
    private static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    /** 允许的视频扩展名 */
    private static final Set<String> VIDEO_EXTS = Set.of("mp4", "mov");

    /** 允许的业务类型 */
    private static final Set<String> BIZ_TYPES = Set.of("avatar", "recipe", "post", "step", "common");

    /** 图片大小上限：10MB */
    private static final long IMAGE_MAX_BYTES = 10L * 1024 * 1024;

    /** 视频大小上限：100MB */
    private static final long VIDEO_MAX_BYTES = 100L * 1024 * 1024;

    /** 对象键中的日期路径格式（yyyy/MM/dd） */
    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** 扩展名 → MIME 类型映射（浏览器未携带 Content-Type 时兜底） */
    private static final Map<String, String> EXT_CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "gif", "image/gif",
            "mp4", "video/mp4",
            "mov", "video/quicktime"
    );

    private final StorageService storageService;
    private final ThumbnailGenerator thumbnailGenerator;
    private final FileRecordRepository fileRecordRepository;

    /**
     * 构造注入依赖：存储策略（条件装配，minio/aliyun 二选一）、缩略图生成器、记录仓储。
     *
     * @param storageService       对象存储策略实现
     * @param thumbnailGenerator   缩略图生成器
     * @param fileRecordRepository 上传记录仓储
     */
    public FileService(StorageService storageService,
                       ThumbnailGenerator thumbnailGenerator,
                       FileRecordRepository fileRecordRepository) {
        this.storageService = storageService;
        this.thumbnailGenerator = thumbnailGenerator;
        this.fileRecordRepository = fileRecordRepository;
        log.info("[文件服务] 当前存储策略: {}", storageService.getStorageType());
    }

    /**
     * 上传文件：校验类型与大小 → 上传原文件 → 生成并上传缩略图（失败不阻断）→ 落库。
     * 对象键命名规则（前端依赖，勿改）：{bizType}/{yyyy/MM/dd}/{uuid}.{ext}，
     * 缩略图为同路径 {uuid}_thumb.jpg（即原 URL 去扩展名 + _thumb.jpg 可推导）。
     *
     * @param userId  上传者用户ID
     * @param file    multipart 文件
     * @param bizType 业务类型（avatar/recipe/post/step/common）
     * @return 上传结果视图对象
     */
    public FileVO upload(Long userId, MultipartFile file, String bizType) {
        // 关键判断：文件为空直接拒绝
        if (file == null || file.isEmpty()) {
            log.warn("[上传] 拒绝：文件为空, userId={}", userId);
            throw new BusinessException(ResultCode.PARAM_ERROR, "上传文件不能为空");
        }
        // 关键判断：业务类型不在约定范围内
        if (!BIZ_TYPES.contains(bizType)) {
            log.warn("[上传] 拒绝：非法业务类型 bizType={}, userId={}", bizType, userId);
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "不支持的业务类型，仅支持 avatar/recipe/post/step/common");
        }

        String originalName = file.getOriginalFilename();
        String ext = extractExtension(originalName);
        boolean isImage = IMAGE_EXTS.contains(ext);
        boolean isVideo = VIDEO_EXTS.contains(ext);
        // 关键判断：扩展名不在白名单内，返回 40000「不支持的文件类型」
        if (!isImage && !isVideo) {
            log.warn("[上传] 拒绝：不支持的文件类型 ext={}, 文件名={}, userId={}", ext, originalName, userId);
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "不支持的文件类型，图片仅支持 jpg/jpeg/png/webp/gif，视频仅支持 mp4/mov");
        }
        // 关键判断：大小超限（图片 10MB / 视频 100MB）
        long size = file.getSize();
        if (isImage && size > IMAGE_MAX_BYTES) {
            log.warn("[上传] 拒绝：图片超限 size={}B, userId={}", size, userId);
            throw new BusinessException(ResultCode.PARAM_ERROR, "图片大小不能超过 10MB");
        }
        if (isVideo && size > VIDEO_MAX_BYTES) {
            log.warn("[上传] 拒绝：视频超限 size={}B, userId={}", size, userId);
            throw new BusinessException(ResultCode.PARAM_ERROR, "视频大小不能超过 100MB");
        }

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            log.error("[上传] 读取上传内容失败: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "文件读取失败，请重试");
        }

        // MIME 类型：优先取浏览器提交的 Content-Type，缺失时按扩展名兜底
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank() || "application/octet-stream".equals(contentType)) {
            contentType = EXT_CONTENT_TYPES.get(ext);
        }

        // 对象键：{bizType}/{yyyy/MM/dd}/{uuid}.{ext}（重要约定，前端依赖该规则推导缩略图）
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String datePath = LocalDate.now().format(DATE_PATH);
        String objectKey = bizType + "/" + datePath + "/" + uuid + "." + ext;

        // 1. 上传原文件（存储不可用时此处抛出 50000 友好提示）
        String url = storageService.upload(data, objectKey, contentType);
        log.info("[上传] 原文件上传成功: userId={}, key={}, size={}B, type={}", userId, objectKey, size, contentType);

        // 2. 生成并上传缩略图（失败不阻断上传，thumbUrl 置 null）
        String thumbUrl = uploadThumbnail(data, ext, isImage, bizType, datePath, uuid);

        // 3. 落库
        FileRecord record = new FileRecord();
        record.setUserId(userId);
        record.setBizType(bizType);
        record.setOriginalName(originalName);
        record.setObjectKey(objectKey);
        record.setUrl(url);
        record.setThumbUrl(thumbUrl);
        record.setContentType(contentType);
        record.setSizeBytes(size);
        record.setStorageType(storageService.getStorageType());
        fileRecordRepository.save(record);
        log.info("[上传] 记录已落库: id={}, userId={}, thumbUrl={}", record.getId(), userId, thumbUrl);

        return FileVO.from(record);
    }

    /**
     * 生成并上传缩略图：图片直接缩放，视频先抓首帧；任何失败均返回 null，不阻断主流程。
     *
     * @param data     原文件字节
     * @param ext      原文件扩展名
     * @param isImage  是否图片
     * @param bizType  业务类型
     * @param datePath 日期路径（yyyy/MM/dd）
     * @param uuid     文件 UUID
     * @return 缩略图 URL；生成或上传失败返回 null
     */
    private String uploadThumbnail(byte[] data, String ext, boolean isImage,
                                   String bizType, String datePath, String uuid) {
        try {
            byte[] thumbData = isImage
                    ? thumbnailGenerator.generateImageThumbnail(data)
                    : thumbnailGenerator.generateVideoThumbnail(data, ext);
            // 关键判断：缩略图生成失败 → thumbUrl 返回 null，不阻断上传
            if (thumbData == null) {
                log.warn("[上传] 缩略图生成失败，thumbUrl 置空: uuid={}, ext={}", uuid, ext);
                return null;
            }
            // 缩略图对象键：同路径 {uuid}_thumb.jpg（前端可由原 URL 去扩展名 + _thumb.jpg 推导）
            String thumbKey = bizType + "/" + datePath + "/" + uuid + "_thumb.jpg";
            String thumbUrl = storageService.upload(thumbData, thumbKey, "image/jpeg");
            log.info("[上传] 缩略图上传成功: key={}, size={}B", thumbKey, thumbData.length);
            return thumbUrl;
        } catch (Exception e) {
            log.warn("[上传] 缩略图处理失败（不阻断上传）: uuid={}, 原因={}", uuid, e.getMessage());
            return null;
        }
    }

    /**
     * 分页查询当前用户的上传记录（按上传时间倒序）。
     *
     * @param userId 当前登录用户ID
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @return 分页结果
     */
    public PageVO<FileVO> myFiles(Long userId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<FileRecord> result = fileRecordRepository.findByUserId(userId,
                PageRequest.of(safePage - 1, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        log.info("[查询] 我的上传记录: userId={}, page={}, size={}, total={}", userId, safePage, safeSize, result.getTotalElements());
        return new PageVO<>(result.getTotalElements(), safePage, safeSize,
                result.getContent().stream().map(FileVO::from).toList());
    }

    /**
     * 提取文件扩展名（小写，不含点）。
     *
     * @param filename 原文件名
     * @return 扩展名；无扩展名返回空字符串
     */
    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }
}
