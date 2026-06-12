import useAuthStore from '../store/useAuthStore';

// ===== 文件服务 file-service :8087 =====
// 契约：POST /api/file/upload，multipart 字段 file + 可选 bizType（avatar/recipe/post/step）
// 成功响应 {code:0,data:{id,url,thumbUrl,originalName,contentType,sizeBytes,storageType,bizType}}
// 图片限 jpg/png/webp/gif ≤10MB；视频限 mp4/mov ≤100MB
// 缩略图推导规则：原 URL 去扩展名 + `_thumb.jpg`

/** 允许的图片类型 */
const IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
/** 允许的视频类型（mov 的 MIME 为 video/quicktime） */
const VIDEO_TYPES = ['video/mp4', 'video/quicktime'];
const MAX_IMAGE_BYTES = 10 * 1024 * 1024; // 10MB
const MAX_VIDEO_BYTES = 100 * 1024 * 1024; // 100MB

/**
 * 前置校验文件类型与大小（与后端契约一致，提前拦截减少无效请求）
 * @returns {string} 错误文案；通过返回 ''
 */
export function validateUploadFile(file) {
  if (!file) return '未选择文件';
  if (IMAGE_TYPES.includes(file.type)) {
    if (file.size > MAX_IMAGE_BYTES) return '图片不能超过 10MB';
    return '';
  }
  if (VIDEO_TYPES.includes(file.type)) {
    if (file.size > MAX_VIDEO_BYTES) return '视频不能超过 100MB';
    return '';
  }
  return '仅支持 jpg/png/webp/gif 图片或 mp4/mov 视频';
}

/**
 * 上传文件到对象存储（XMLHttpRequest 实现，带上传进度回调）
 * @param {File} file 待上传文件
 * @param {string} bizType 业务类型 avatar/recipe/post/step
 * @param {(percent:number)=>void} [onProgress] 进度回调 0-100
 * @returns {Promise<{id,url,thumbUrl,originalName,contentType,sizeBytes,storageType,bizType}>}
 */
export function uploadFile(file, bizType = 'post', onProgress) {
  return new Promise((resolve, reject) => {
    // 1) 客户端前置校验
    const msg = validateUploadFile(file);
    if (msg) return reject(new Error(msg));

    // 2) 组装 multipart 请求
    const xhr = new XMLHttpRequest();
    xhr.open('POST', '/api/file/upload');
    xhr.timeout = 5 * 60 * 1000; // 大视频留足时间
    const token = useAuthStore.getState().token;
    if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`);

    // 3) 上传进度
    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable && onProgress) onProgress(Math.round((e.loaded / e.total) * 100));
    };

    // 4) 响应处理：统一解包 {code,message,data}
    xhr.onload = () => {
      let body = null;
      try {
        body = JSON.parse(xhr.responseText || 'null');
      } catch {
        /* 非 JSON 响应按状态码兜底 */
      }
      if (xhr.status === 401 || body?.code === 40100) {
        return reject(new Error('请先登录后再上传'));
      }
      if (xhr.status >= 200 && xhr.status < 300 && body?.code === 0) {
        return resolve(body.data);
      }
      reject(new Error(body?.message || `上传失败（服务异常 ${xhr.status}）`));
    };
    // 文件服务未启动/断网时给友好提示（由调用方走统一 Toast）
    xhr.onerror = () => reject(new Error('上传服务连接失败，请稍后重试'));
    xhr.ontimeout = () => reject(new Error('上传超时，请检查网络后重试'));

    const fd = new FormData();
    fd.append('file', file);
    if (bizType) fd.append('bizType', bizType);
    xhr.send(fd);
  });
}

/**
 * 按契约推导缩略图地址：原 URL 去扩展名 + `_thumb.jpg`
 * 例：https://x/a/b.png → https://x/a/b_thumb.jpg（保留查询串）
 * data: 内联图与空值原样返回
 */
export function toThumbUrl(url) {
  if (!url || url.startsWith('data:')) return url;
  const qIdx = url.search(/[?#]/);
  const base = qIdx >= 0 ? url.slice(0, qIdx) : url;
  const suffix = qIdx >= 0 ? url.slice(qIdx) : '';
  const dot = base.lastIndexOf('.');
  const slash = base.lastIndexOf('/');
  // 无扩展名时直接拼接
  const stem = dot > slash ? base.slice(0, dot) : base;
  return `${stem}_thumb.jpg${suffix}`;
}

/** 粗略判断 URL 是否为视频（按扩展名 mp4/mov） */
export function isVideoUrl(url) {
  return /\.(mp4|mov)([?#]|$)/i.test(url || '');
}
