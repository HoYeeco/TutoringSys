<template>
  <div id="app">
    <template v-if="userStore.token">
      <AppLayout :menu-items="menuItems">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </AppLayout>
    </template>
    <template v-else>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useUserStore } from './stores/user';
import AppLayout from './layouts/AppLayout.vue';
import { HomeFilled, Document, User, Setting, School, DataAnalysis } from '@element-plus/icons-vue';

const userStore = useUserStore();

const menuItems = computed(() => {
  if (userStore.role === 'TEACHER') {
    return [
      { path: '/dashboard', title: '首页', icon: HomeFilled },
      { path: '/teacher/courses', title: '课程管理', icon: School },
      { path: '/teacher/assignments', title: '作业管理', icon: Document },
      { path: '/teacher/students', title: '学生管理', icon: User },
      { path: '/teacher/analytics', title: '数据分析', icon: DataAnalysis },
      { path: '/profile', title: '个人中心', icon: Setting }
    ];
  } else if (userStore.role === 'STUDENT') {
    return [
      { path: '/dashboard', title: '首页', icon: HomeFilled },
      { path: '/student/courses', title: '我的课程', icon: School },
      { path: '/student/assignments', title: '我的作业', icon: Document },
      { path: '/student/reports', title: '成绩报告', icon: DataAnalysis },
      { path: '/profile', title: '个人中心', icon: Setting }
    ];
  } else if (userStore.role === 'ADMIN') {
    return [
      { path: '/dashboard', title: '首页', icon: HomeFilled },
      { path: '/admin/users', title: '用户管理', icon: User },
      { path: '/admin/courses', title: '课程管理', icon: School },
      { path: '/admin/questions', title: '题库管理', icon: Document },
      { path: '/admin/stats', title: '统计分析', icon: DataAnalysis },
      { path: '/profile', title: '个人中心', icon: Setting }
    ];
  }
  return [];
});
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
