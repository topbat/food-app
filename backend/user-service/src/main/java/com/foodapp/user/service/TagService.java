package com.foodapp.user.service;

import com.foodapp.common.exception.BusinessException;
import com.foodapp.common.result.ResultCode;
import com.foodapp.user.dto.AddTagRequest;
import com.foodapp.user.entity.UserTag;
import com.foodapp.user.repository.UserTagRepository;
import com.foodapp.user.vo.TagVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Z世代标签业务层：我的标签列表、添加标签、删除标签。
 */
@Service
public class TagService {

    private static final Logger log = LoggerFactory.getLogger(TagService.class);

    private final UserTagRepository tagRepository;

    /**
     * 构造注入标签仓库。
     *
     * @param tagRepository 标签仓库
     */
    public TagService(UserTagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * 查询当前用户的全部标签。
     *
     * @param userId 当前登录用户ID
     * @return 标签VO列表
     */
    public List<TagVO> listTags(Long userId) {
        return tagRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(tag -> new TagVO(tag.getId(), tag.getTagName(), tag.getTagType()))
                .toList();
    }

    /**
     * 为当前用户添加标签：同名标签防重复。
     *
     * @param userId  当前登录用户ID
     * @param request 添加入参（标签名/类型）
     * @return 新建标签VO
     * @throws BusinessException 标签已存在时抛 40900
     */
    @Transactional
    public TagVO addTag(Long userId, AddTagRequest request) {
        // 关键判断：同一用户不允许添加重复的同名标签
        if (tagRepository.existsByUserIdAndTagName(userId, request.getTagName())) {
            log.warn("[标签] 用户{}重复添加标签: {}", userId, request.getTagName());
            throw new BusinessException(ResultCode.BIZ_CONFLICT, "标签已存在");
        }
        UserTag tag = new UserTag();
        tag.setUserId(userId);
        tag.setTagName(request.getTagName());
        tag.setTagType(request.getTagType());
        tagRepository.save(tag);
        log.info("[标签] 用户{}添加标签成功: id={}, tagName={}, tagType={}",
                userId, tag.getId(), tag.getTagName(), tag.getTagType());
        return new TagVO(tag.getId(), tag.getTagName(), tag.getTagType());
    }

    /**
     * 删除当前用户的指定标签：校验标签归属，防止越权删除他人标签。
     *
     * @param userId 当前登录用户ID
     * @param tagId  标签ID
     * @throws BusinessException 标签不存在或不属于当前用户时抛 40400
     */
    @Transactional
    public void deleteTag(Long userId, Long tagId) {
        // 关键判断：标签必须存在且归属当前用户
        UserTag tag = tagRepository.findByIdAndUserId(tagId, userId).orElseThrow(() -> {
            log.warn("[标签] 用户{}删除标签失败（不存在或非本人标签）: tagId={}", userId, tagId);
            return new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        });
        tagRepository.delete(tag);
        log.info("[标签] 用户{}删除标签成功: tagId={}, tagName={}", userId, tagId, tag.getTagName());
    }
}
