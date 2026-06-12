import { useRef, useState } from 'react';
import { uploadFile } from '../api/file';
import { toast } from '../store/useToastStore';

/**
 * 本地上传按钮：隐藏的 input[type=file] + 进度展示 + 统一错误 Toast
 * - 多文件时逐个串行上传，每个文件各自显示进度
 * - 失败不打断后续文件，失败文案带文件名方便重选重试
 * @param {string} bizType 业务类型 avatar/recipe/post/step
 * @param {string} accept input accept 规则
 * @param {boolean} multiple 是否多选
 * @param {(data:object, file:File)=>void} onUploaded 单个文件上传成功回调（契约 data：{url,thumbUrl,...}）
 */
export default function UploadButton({
  bizType = 'post',
  accept = 'image/jpeg,image/png,image/webp,image/gif',
  multiple = false,
  onUploaded,
  className = '',
  children,
  disabled = false,
}) {
  const inputRef = useRef(null);
  const [progress, setProgress] = useState(-1); // -1=空闲，0-100=当前文件进度
  const [queueInfo, setQueueInfo] = useState(''); // 多文件时的「第 x/n 个」提示

  /** 选择文件后逐个上传 */
  const handleChange = async (e) => {
    const files = Array.from(e.target.files || []);
    e.target.value = ''; // 清空 value，允许重复选择同一文件（失败重试场景）
    if (!files.length) return;
    let okCount = 0;
    for (let i = 0; i < files.length; i++) {
      const f = files[i];
      setQueueInfo(files.length > 1 ? `${i + 1}/${files.length} ` : '');
      setProgress(0);
      try {
        const data = await uploadFile(f, bizType, setProgress);
        okCount += 1;
        onUploaded?.(data, f);
      } catch (err) {
        // 统一 Toast：失败带文件名，提示可重试，不崩页面
        toast.error(`「${f.name}」上传失败：${err.message}`);
      }
    }
    setProgress(-1);
    setQueueInfo('');
    if (files.length > 1) toast.success(`已上传 ${okCount}/${files.length} 个文件`);
  };

  const uploading = progress >= 0;
  return (
    <>
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        multiple={multiple}
        className="hidden"
        onChange={handleChange}
      />
      <button
        type="button"
        disabled={disabled || uploading}
        onClick={() => inputRef.current?.click()}
        className={className}
      >
        {uploading ? `${queueInfo}上传中 ${progress}%` : children}
      </button>
    </>
  );
}
