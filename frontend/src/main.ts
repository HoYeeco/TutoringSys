import { createApp } from 'vue';
import { createPinia } from 'pinia';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/dist/locale/zh-cn.mjs';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import './style.css';
import './styles/theme.css';
import { permission } from './directives/permission';
import { watermark } from './directives/watermark';
import { lazy } from './directives/lazy';
import VueVirtualScroller from 'vue-virtual-scroller';
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css';
import { useAppStore } from './stores/app';

const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);

const app = createApp(App);
app.use(pinia);
app.use(router);
app.use(ElementPlus, { locale: zhCn });
app.use(VueVirtualScroller);
app.directive('permission', permission);
app.directive('watermark', watermark);
app.directive('lazy', lazy);

// 应用主题设置
const appStore = useAppStore();
appStore.applyTheme();

// 检测系统主题
appStore.detectSystemTheme();

// 监听系统主题变化
if (window.matchMedia) {
  window
    .matchMedia('(prefers-color-scheme: dark)')
    .addEventListener('change', () => {
      appStore.detectSystemTheme();
    });
}

app.mount('#app');
