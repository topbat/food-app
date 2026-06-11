/** @type {import('tailwindcss').Config} */
// 新中式美学色板：宣纸底、朱砂主色、暖橙辅助、黛绿健康、赭石点缀、墨色文字
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        paper: '#F7F3EB', // 底色「宣纸」
        card: '#FFFDF8', // 卡片白
        cinnabar: {
          DEFAULT: '#B5392F', // 主色「朱砂」
          dark: '#9C2F26',
          light: '#C9544A',
        },
        warmth: '#D96E48', // 「暖橙」渐变辅助/热量元素
        jade: '#2F5D50', // 「黛绿」健康/营养元素
        ochre: '#9A6A4F', // 「赭石」
        ink: '#2B2B2B', // 文字「墨色」
        mute: '#8A8378', // 次要文字
      },
      fontFamily: {
        serif: ['"Noto Serif SC"', '"Songti SC"', 'STSong', 'SimSun', 'serif'],
      },
      boxShadow: {
        soft: '0 2px 12px rgba(43, 43, 43, 0.06)',
        lift: '0 10px 28px rgba(43, 43, 43, 0.12)',
        seal: '0 2px 8px rgba(181, 57, 47, 0.35)',
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
          '0%, 100%': { boxShadow: '0 0 0 0 rgba(181, 57, 47, 0.5)' },
          '50%': { boxShadow: '0 0 0 16px rgba(181, 57, 47, 0)' },
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
