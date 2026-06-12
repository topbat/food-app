package com.foodapp.file.controller;

import com.foodapp.common.auth.UserContext;
import com.foodapp.common.result.Result;
import com.foodapp.file.service.FileService;
import com.foodapp.file.vo.FileVO;
import com.foodapp.file.vo.PageVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器（全部接口需鉴权）。
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    /**
     * 构造注入文件业务层。
     *
     * @param fileService 文件业务层
     */
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 上传文件（multipart/form-data）。
     * 图片仅 jpg/jpeg/png/webp/gif（≤10MB）；视频仅 mp4/mov（≤100MB）。
     *
     * @param file    文件字段（必填）
     * @param bizType 业务类型（可选：avatar/recipe/post/step，默认 common）
     * @return 上传结果：{id,url,thumbUrl,originalName,contentType,sizeBytes,storageType,bizType}
     */
    @PostMapping("/upload")
    public Result<FileVO> upload(@RequestPart("file") MultipartFile file,
                                 @RequestParam(value = "bizType", defaultValue = "common") String bizType) {
        Long userId = UserContext.requireUserId();
        return Result.success(fileService.upload(userId, file, bizType));
    }

    /**
     * 分页查询当前用户的上传记录（按上传时间倒序）。
     *
     * @param page 页码（从1开始，默认1）
     * @param size 每页条数（默认10，上限100）
     * @return 分页结果：{total,page,size,list:[FileVO]}
     */
    @GetMapping("/my")
    public Result<PageVO<FileVO>> myFiles(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.requireUserId();
        return Result.success(fileService.myFiles(userId, page, size));
    }
}
