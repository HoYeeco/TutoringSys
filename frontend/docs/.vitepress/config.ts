import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "家教系统组件库",
  description: "家教系统前端组件库文档",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: '首页', link: '/' },
      { text: '组件', link: '/components/question-card' }
    ],

    sidebar: [
      {
        text: '首页',
        link: '/'
      },
      {
        text: '组件',
        items: [
          { text: 'QuestionCard', link: '/components/question-card' },
          { text: 'QuestionReportCard', link: '/components/question-report-card' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/vuejs/vitepress' }
    ]
  }
})
