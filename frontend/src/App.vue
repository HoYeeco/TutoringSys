<template>
  <div id="app">
    <template v-if="userStore.token">
      <component :is="currentLayout">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </component>
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
import { computed, onMounted } from 'vue';
import { useUserStore } from './stores/user';
import AdminLayout from './layouts/AdminLayout.vue';
import TeacherLayout from './layouts/TeacherLayout.vue';
import StudentLayout from './layouts/StudentLayout.vue';
import request from './utils/request';

const userStore = useUserStore();

const currentLayout = computed(() => {
  if (userStore.role === 'TEACHER') {
    return TeacherLayout;
  } else if (userStore.role === 'STUDENT') {
    return StudentLayout;
  } else if (userStore.role === 'ADMIN') {
    return AdminLayout;
  }
  return null;
});

const refreshUserInfo = async () => {
  if (userStore.token) {
    try {
      const response = await request.get('/user/profile');
      if (response.data) {
        userStore.setUserInfo({
          ...userStore.userInfo,
          ...response.data,
          token: userStore.token,
        });
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
    }
  }
};

onMounted(() => {
  refreshUserInfo();
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
