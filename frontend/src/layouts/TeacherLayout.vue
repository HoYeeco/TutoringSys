<template>
  <AppLayout :menuItems="menuItems">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </AppLayout>
</template>

<script setup lang="ts">
import AppLayout from './AppLayout.vue';
import {
  Notebook,
  DataAnalysis,
  House,
  School,
  User,
  Bell,
  EditPen,
} from '@element-plus/icons-vue';

const menuItems = [
  {
    path: '/dashboard',
    title: '首页',
    icon: House,
  },
  {
    path: '/teacher/courses',
    title: '课程管理',
    icon: School,
  },
  {
    path: '/teacher/assignments',
    title: '作业管理',
    icon: Notebook,
  },
  {
    path: '/teacher/grading',
    title: '批改复核',
    icon: EditPen,
  },
  {
    path: '/teacher/analysis',
    title: '学情分析',
    icon: DataAnalysis,
  },
  {
    path: '/messages',
    title: '消息通知',
    icon: Bell,
  },
  {
    path: '/profile',
    title: '个人中心',
    icon: User,
  },
];
</script>
