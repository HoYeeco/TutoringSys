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
  House,
  School,
  Bell,
  DataAnalysis,
  Notebook,
  User,
  Reading,
} from '@element-plus/icons-vue';

const menuItems = [
  {
    path: '/dashboard',
    title: '首页',
    icon: House,
  },
  {
    path: '/student/courses',
    title: '我的课程',
    icon: School,
  },
  {
    path: '/student/assignments',
    title: '我的作业',
    icon: Notebook,
  },
  {
    path: '/student/grading',
    title: '成绩批改',
    icon: DataAnalysis,
  },
  {
    path: '/student/error-book',
    title: '错题本',
    icon: Reading,
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
