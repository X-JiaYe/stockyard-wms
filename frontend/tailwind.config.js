/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js}'],
  theme: {
    extend: {
      colors: {
        canvas: '#f5f5f7',      // 页面底色
        surface: '#ffffff',     // 卡片/面板
        ink: '#1d1d1f',         // 主文字
        muted: '#6e6e73',       // 次级文字
        line: '#e8e8ed',        // 细分隔线
        lineStrong: '#d2d2d7',  // 强分隔线
        accent: {
          DEFAULT: '#0071e3',
          hover: '#0077ed'
        },
        success: '#34c759',
        warning: '#ff9500',
        danger: '#ff3b30'
      },
      fontFamily: {
        sans: [
          '-apple-system', 'BlinkMacSystemFont', '"SF Pro Text"', '"Segoe UI"',
          '"PingFang SC"', '"Hiragino Sans GB"', '"Microsoft YaHei"', 'sans-serif'
        ]
      },
      boxShadow: {
        soft: '0 1px 2px rgba(0,0,0,0.04), 0 8px 24px rgba(0,0,0,0.04)',
        card: '0 1px 1px rgba(0,0,0,0.03)'
      },
      borderRadius: {
        xl2: '1.25rem'
      }
    }
  },
  plugins: []
}
