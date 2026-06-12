package com.foodapp.file.service;

import net.coobird.thumbnailator.Thumbnails;
import org.jcodec.api.awt.AWTFrameGrab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 缩略图生成器（前端列表页性能优化核心）。
 * 图片：缩放为宽 400px 的 JPG（质量 0.8）；
 * 视频：jcodec 抓取第一帧后再缩放为宽 400px 的 JPG。
 * 所有方法生成失败均返回 null（调用方据此置 thumbUrl=null），不抛异常阻断上传主流程。
 */
@Component
public class ThumbnailGenerator {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailGenerator.class);

    /** 缩略图目标宽度（像素） */
    private static final int THUMB_WIDTH = 400;

    /** 缩略图 JPG 输出质量 */
    private static final double THUMB_QUALITY = 0.8;

    /**
     * 生成图片缩略图。
     *
     * @param data 原图字节内容（jpg/jpeg/png/webp/gif）
     * @return 缩略图 JPG 字节；生成失败（如 webp 无解码器）返回 null
     */
    public byte[] generateImageThumbnail(byte[] data) {
        try {
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(data));
            // 关键判断：ImageIO 无法解码（如 webp 缺少解码器、文件损坏）则放弃生成
            if (source == null) {
                log.warn("[缩略图] 图片解码失败（格式不受 ImageIO 支持或文件损坏），跳过缩略图生成");
                return null;
            }
            return scaleToJpg(source);
        } catch (Exception e) {
            log.warn("[缩略图] 图片缩略图生成失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成视频缩略图：抓取第一帧后缩放为 JPG。
     * jcodec 需要基于文件随机读取，因此先落临时文件再抓帧，结束后删除。
     *
     * @param data 视频字节内容（mp4/mov）
     * @param ext  视频扩展名（用于临时文件后缀）
     * @return 缩略图 JPG 字节；抓帧失败（编码不支持等）返回 null
     */
    public byte[] generateVideoThumbnail(byte[] data, String ext) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("foodapp-video-", "." + ext);
            Files.write(tempFile, data);
            File videoFile = tempFile.toFile();
            BufferedImage frame = AWTFrameGrab.getFrame(videoFile, 0);
            // 关键判断：抓帧失败（视频编码不被 jcodec 支持）则放弃生成
            if (frame == null) {
                log.warn("[缩略图] 视频首帧抓取失败（编码可能不受支持），跳过缩略图生成");
                return null;
            }
            return scaleToJpg(frame);
        } catch (Throwable e) {
            // jcodec 解析异常类型繁杂（含 Error），统一兜底，绝不阻断上传
            log.warn("[缩略图] 视频缩略图生成失败: {}", e.getMessage());
            return null;
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ignore) {
                    log.warn("[缩略图] 临时视频文件删除失败: {}", tempFile);
                }
            }
        }
    }

    /**
     * 将 BufferedImage 缩放为宽 400px、质量 0.8 的 JPG 字节。
     * 带透明通道的图片（PNG/GIF）先平铺到白色底，避免 JPG 编码透明区变黑或报错。
     *
     * @param source 源图
     * @return JPG 字节
     * @throws Exception 缩放或编码异常
     */
    private byte[] scaleToJpg(BufferedImage source) throws Exception {
        BufferedImage rgb = source;
        // 关键判断：JPG 不支持透明通道，先将 ARGB 图平铺到白底 RGB
        if (source.getTransparency() != BufferedImage.OPAQUE) {
            rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgb.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, source.getWidth(), source.getHeight());
            g.drawImage(source, 0, 0, null);
            g.dispose();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(rgb)
                .width(THUMB_WIDTH)
                .outputFormat("jpg")
                .outputQuality(THUMB_QUALITY)
                .toOutputStream(out);
        return out.toByteArray();
    }
}
