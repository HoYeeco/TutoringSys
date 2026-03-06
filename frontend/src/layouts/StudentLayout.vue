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
import { Book, Document, History, Ticket } from '@element-plus/icons-vue';

const menuItems = [
  {
    path: '/student/assignments',
    title: '我的作业',
    icon: Ticket
  },
  {
    path: '/student/reports',
    title: '学习报告',
    icon: Document
  },
  {
    path: '/student/history',
    title: '历史记录',
    icon: History
  }
];
</script>