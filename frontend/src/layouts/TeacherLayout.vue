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
import { Book, Document, Edit, DataAnalysis, Ticket } from '@element-plus/icons-vue';

const menuItems = [
  {
    path: '/teacher/courses',
    title: '我的课程',
    icon: Book
  },
  {
    path: '/teacher/assignments/create',
    title: '创建作业',
    icon: Edit
  },
  {
    path: '/teacher/analysis',
    title: '数据分析',
    icon: DataAnalysis
  }
];
</script>