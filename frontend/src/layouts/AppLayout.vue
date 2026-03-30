<template>
  <el-container class="layout-container" :class="themeClass">
    <el-aside
      :width="collapsed ? '64px' : '200px'"
      :class="{ 'mobile-hidden': isMobile }"
    >
      <el-menu :collapse="collapsed" :default-active="$route.path" router>
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-left">
          <div class="logo" @click="toggleSidebar">
            <img
              src="@/assets/images/Scoututor.png"
              alt="Scoututor"
              class="logo-img"
            />
          </div>
          <el-icon @click="toggleSidebar" v-if="!isMobile"><Fold /></el-icon>
          <el-icon @click="toggleMobileMenu" v-else><Menu /></el-icon>
        </div>
        <div class="header-right">
          <div class="user-info" @click="goToProfile">
            <div class="avatar">
              <img
                v-if="userStore.userInfo?.avatar"
                :src="userStore.userInfo.avatar"
                alt="头像"
                @error="handleAvatarError"
              />
              <span v-else class="avatar-placeholder">{{
                getAvatarText()
              }}</span>
            </div>
            {{ userStore.userInfo?.realName || userStore.userInfo?.username || '用户' }}
          </div>
          <el-badge
            :value="unreadCount"
            :hidden="unreadCount === 0"
            @click="goToMessages"
            class="message-badge"
          >
            <el-icon class="message-icon"><Bell /></el-icon>
          </el-badge>
          <el-icon class="logout-icon" @click="confirmLogout"
            ><SwitchButton
          /></el-icon>
          <el-tooltip
            :content="isDark ? '切换到白天模式' : '切换到黑夜模式'"
            placement="bottom"
          >
            <el-switch
              :model-value="isDark"
              @change="handleThemeChange"
              inline-prompt
              :active-icon="Moon"
              :inactive-icon="Sunny"
              class="theme-switch"
            />
          </el-tooltip>
        </div>
      </el-header>
      <el-main>
        <slot></slot>
      </el-main>
    </el-container>
  </el-container>
  <!-- 移动端菜单 -->
  <el-drawer
    v-model="mobileMenuVisible"
    direction="ltr"
    size="200px"
    title="菜单"
  >
    <el-menu :default-active="$route.path" router @select="handleMenuSelect">
      <el-menu-item
        v-for="item in menuItems"
        :key="item.path"
        :index="item.path"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.title }}</span>
      </el-menu-item>
    </el-menu>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useAppStore } from '@/stores/app';
import {
  Fold,
  Moon,
  Sunny,
  Menu,
  Bell,
  SwitchButton,
} from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
import request from '@/utils/request';

const props = defineProps<{ menuItems: any[] }>();

const router = useRouter();
const userStore = useUserStore();
const appStore = useAppStore();

const collapsed = computed(() => appStore.sidebarCollapsed);
const toggleSidebar = () => appStore.toggleSidebar();

const themeClass = computed(() => {
  if (userStore.role === 'STUDENT') return 'student-theme';
  if (userStore.role === 'TEACHER') return 'teacher-theme';
  if (userStore.role === 'ADMIN') return 'admin-theme';
  return '';
});

// 响应式布局
const isMobile = ref(false);
const mobileMenuVisible = ref(false);

// 未读消息数量
const unreadCount = ref(0);

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768;
};

// 处理头像加载失败
const handleAvatarError = () => {
  if (userStore.userInfo) {
    userStore.userInfo.avatar = null;
  }
};

// 获取头像文字
const getAvatarText = () => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username;
  return name ? name.charAt(0) : 'U';
};

const toggleMobileMenu = () => {
  mobileMenuVisible.value = !mobileMenuVisible.value;
};

const handleMenuSelect = () => {
  mobileMenuVisible.value = false;
};

// 主题切换
const isDark = computed(() => appStore.theme === 'dark');

const handleThemeChange = (value: boolean) => {
  appStore.setTheme(value ? 'dark' : 'light');
};

// 获取未读消息数量
const getUnreadCount = async () => {
  if (userStore.userInfo?.id) {
    try {
      const response = await request.get('/message/unread-count', {
        params: {
          userId: userStore.userInfo?.id,
        },
      });
      unreadCount.value = response.data || 0;
    } catch (error) {
      unreadCount.value = 0;
    }
  }
};

// 跳转到消息通知页面
const goToMessages = () => {
  router.push('/messages');
};

// 跳转到个人中心
const goToProfile = () => {
  router.push('/profile');
};

// 确认退出登录
const confirmLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    userStore.logout();
    router.push('/login');
  });
};

onMounted(() => {
  checkMobile();
  window.addEventListener('resize', checkMobile);
  // 获取未读消息数量
  getUnreadCount();
  // 每30秒刷新一次未读消息数量
  const interval = setInterval(getUnreadCount, 30000);
  onUnmounted(() => {
    clearInterval(interval);
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.el-aside {
  background-color: var(--color-menu-bg, #ffffff);
  overflow: hidden;
  flex-shrink: 0;
}

.el-menu {
  height: 100%;
  border-right: none !important;
  overflow-y: auto;
  overflow-x: hidden;
}

.el-menu::-webkit-scrollbar {
  width: 4px;
}

.el-menu::-webkit-scrollbar-thumb {
  background-color: var(--color-border);
  border-radius: 2px;
}

.el-menu::-webkit-scrollbar-track {
  background: transparent;
}

.el-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--color-border);
  background-color: var(--color-header-bg, #ffffff);
  height: 60px !important;
  padding: 0 20px;
  box-sizing: border-box;
  flex-shrink: 0;
}

.el-main {
  background-color: transparent;
  padding: 20px;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  position: relative;
}

/* 背景图片层 */
.el-main::before {
  content: '';
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  z-index: -2;
}



/* 学生主题背景 */
.student-theme .el-main::before {
  background-image: url('@/assets/images/bg_student.png');
}

/* 教师主题背景 */
.teacher-theme .el-main::before {
  background-image: url('@/assets/images/bg_teacher.png');
}

/* 管理员主题背景 */
.admin-theme .el-main::before {
  background-image: url('@/assets/images/bg_admin.png');
}

.el-main::-webkit-scrollbar {
  width: 6px;
}

.el-main::-webkit-scrollbar-thumb {
  background-color: var(--color-border);
  border-radius: 3px;
}

.el-main::-webkit-scrollbar-track {
  background: transparent;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: opacity 0.3s;
}

.logo:hover {
  opacity: 0.8;
}

.logo-img {
  height: 32px;
  width: auto;
  object-fit: contain;
  transition: filter 0.3s ease;
}

html.dark .logo-img {
  filter: invert(1);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.message-badge {
  cursor: pointer;
  font-size: 20px;
  color: var(--color-text);
  display: flex;
  align-items: center;
  line-height: 1;
}

.message-icon {
  font-size: 20px;
}

.user-info {
  cursor: pointer;
  color: var(--color-text);
  display: flex;
  align-items: center;
  gap: 8px;
}

.logout-icon {
  cursor: pointer;
  font-size: 20px;
  color: var(--color-text);
  transition: color 0.3s;
}

.logout-icon:hover {
  color: #f56c6c;
}

.theme-switch {
  --el-switch-on-color: #4a5568;
  --el-switch-off-color: #e2e8f0;
}

.theme-switch :deep(.el-switch__core) {
  border: 1px solid var(--color-border);
}

.theme-switch :deep(.el-switch__inner) {
  color: var(--color-text);
}

.theme-switch :deep(.el-switch__action) {
  background-color: var(--color-text);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  background-color: var(--color-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 16px;
  font-weight: bold;
  color: var(--color-primary);
}

.mobile-hidden {
  display: none !important;
}

@media (min-width: 768px) {
  .mobile-hidden {
    display: block !important;
  }
}

@media (max-width: 767px) {
  .el-main {
    padding: 10px;
  }

  .el-header {
    padding: 0 10px;
  }
}
</style>
