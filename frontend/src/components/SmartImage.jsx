import { useEffect, useState } from 'react';
import { toThumbUrl } from '../api/file';
import { PLACEHOLDER_IMG } from '../utils/constants';

/**
 * 智能图片：列表/网格场景优先加载缩略图（_thumb.jpg），失败自动回退原图，再失败回退占位图
 * - thumb=false 时直接加载原图（详情大图场景）
 * - 自带懒加载 loading="lazy" 与占位底色 bg-ink/5
 * 加载链路：缩略图(0) → 原图(1) → 占位图(2)
 */
export default function SmartImage({ src, thumb = true, alt = '', className = '', ...rest }) {
  // stage: 0=缩略图 1=原图 2=占位图
  const [stage, setStage] = useState(thumb ? 0 : 1);

  // src / thumb 变化时重置加载阶段
  useEffect(() => {
    setStage(thumb ? 0 : 1);
  }, [src, thumb]);

  const thumbUrl = toThumbUrl(src);
  const current = !src
    ? PLACEHOLDER_IMG
    : stage === 0
      ? thumbUrl
      : stage === 1
        ? src
        : PLACEHOLDER_IMG;

  /** 加载失败：进入下一回退阶段（缩略图可能不存在，如旧数据或生成失败） */
  const handleError = () => {
    setStage((s) => {
      // 缩略图与原图相同（如 data: 或无扩展名场景）则直接跳占位
      if (s === 0 && thumbUrl === src) return 2;
      return Math.min(2, s + 1);
    });
  };

  return (
    <img
      src={current}
      alt={alt}
      loading="lazy"
      onError={handleError}
      className={`bg-ink/5 ${className}`}
      {...rest}
    />
  );
}
