/** @type {import('tailwindcss').Config} */
// 新中式美学色板：宣纸底、朱砂主色、暖橙辅助、黛绿健康、赭石/金棕点缀、墨色文字
// 颜色统一引用 index.css 中的 CSS 变量（:root 亮色 / .dark 暗色），
// 写法 rgb(var(--xx) / <alpha-value>) 以保留 Tailwind 的透明度修饰符能力（如 bg-ink/5）
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  darkMode: 'class', // 暗黑模式：html 根节点挂 .dark 类
  theme: {
    extend: {
      colors: {
        paper: 'rgb(var(--c-paper) / <alpha-value>)', // 底色「宣纸」/ 暗色为暖墨黑
        card: 'rgb(var(--c-card) / <alpha-value>)', // 卡片白 / 暗色为深暖灰
        cinnabar: {
          DEFAULT: 'rgb(var(--c-cinnabar) / <alpha-value>)', // 主色「朱砂」
          dark: 'rgb(var(--c-cinnabar-deep) / <alpha-value>)',
          light: 'rgb(var(--c-cinnabar-soft) / <alpha-value>)',
        },
        warmth: 'rgb(var(--c-warmth) / <alpha-value>)', // 「暖橙」渐变辅助/热量元素
        jade: 'rgb(var(--c-jade) / <alpha-value>)', // 「黛绿」健康/营养元素，暗色转低饱和青玉
        ochre: 'rgb(var(--c-ochre) / <alpha-value>)', // 「赭石」
        gold: 'rgb(var(--c-gold) / <alpha-value>)', // 「金棕/琥珀」点缀色
        ink: 'rgb(var(--c-ink) / <alpha-value>)', // 文字「墨色」/ 暗色为宣纸白
        mute: 'rgb(var(--c-mute) / <alpha-value>)', // 次要文字
        scrim: 'rgb(var(--c-scrim) / <alpha-value>)', // 图片角标/弹层遮罩专用：两种主题下都保持暗色
      },
      fontFamily: {
        serif: ['"Noto Serif SC"', '"Songti SC"', 'STSong', 'SimSun', 'serif'],
      },
      // 阴影走 CSS 变量：暗色下降低透明度 / 以描边替代
      boxShadow: {
        soft: 'var(--shadow-soft)',
        lift: 'var(--shadow-lift)',
        seal: 'var(--shadow-seal)',
      },
      keyframes: {
        fadeUp: {
          '0%': { opacity: '0', transform: 'translateY(14px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        pop: {
          '0%': { opacity: '0', transform: 'scale(0.4) rotate(-8deg)' },
          '70%': { transform: 'scale(1.1) rotate(2deg)' },
          '100%': { opacity: '1', transform: 'scale(1) rotate(0deg)' },
        },
        pulseRing: {
          '0%, 100%': { boxShadow: '0 0 0 0 rgb(var(--c-cinnabar) / 0.5)' },
          '50%': { boxShadow: '0 0 0 16px rgb(var(--c-cinnabar) / 0)' },
        },
        flicker: {
          '0%, 100%': { opacity: '1', transform: 'scale(1)' },
          '50%': { opacity: '0.6', transform: 'scale(0.92)' },
        },
        slideUp: {
          '0%': { transform: 'translateY(100%)' },
          '100%': { transform: 'translateY(0)' },
        },
      },
      animation: {
        'fade-up': 'fadeUp 0.4s ease-out both',
        pop: 'pop 0.5s cubic-bezier(0.22, 1.2, 0.36, 1) both',
        'pulse-ring': 'pulseRing 1.2s ease-in-out infinite',
        flicker: 'flicker 0.9s ease-in-out infinite',
        'slide-up': 'slideUp 0.28s ease-out both',
      },
    },
  },
  plugins: [],
};
