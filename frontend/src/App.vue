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
import { computed } from 'vue';
import { useUserStore } from './stores/user';
import AdminLayout from './layouts/AdminLayout.vue';
import TeacherLayout from './layouts/TeacherLayout.vue';
import StudentLayout from './layouts/StudentLayout.vue';

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
