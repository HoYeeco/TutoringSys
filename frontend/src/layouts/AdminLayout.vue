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
import { User, Book, Document, DataAnalysis, Setting } from '@element-plus/icons-vue';

const menuItems = [
  {
    path: '/admin/users',
    title: '用户管理',
    icon: User
  },
  {
    path: '/admin/courses',
    title: '课程管理',
    icon: Book
  },
  {
    path: '/admin/questions',
    title: '题目管理',
    icon: Document
  },
  {
    path: '/admin/stats',
    title: '统计分析',
    icon: DataAnalysis
  }
];
</script>