<template>
  <div class="dashboard">
    <h1>欢迎回来，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h1>
    <div class="welcome-card">
      <h2>账号信息</h2>
      <div class="info-grid">
        <div class="info-item">
          <span class="label">用户名</span>
          <span class="value">{{ userStore.userInfo?.username }}</span>
        </div>
        <div class="info-item">
          <span class="label">姓名</span>
          <span class="value">{{ userStore.userInfo?.realName }}</span>
        </div>
        <div class="info-item">
          <span class="label">角色</span>
          <span class="value">{{ getUserRoleText(userStore.userInfo?.role) }}</span>
        </div>
        <div class="info-item">
          <span class="label">登录时间</span>
          <span class="value">{{ formattedLoginTime }}</span>
        </div>
      </div>
    </div>
    <div class="stats-card">
      <h2>系统概览</h2>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">12</div>
          <div class="stat-label">总课程数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">45</div>
          <div class="stat-label">总学生数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">8</div>
          <div class="stat-label">总教师数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">120</div>
          <div class="stat-label">总作业数</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();

const formattedLoginTime = computed(() => {
  const now = new Date();
  return now.toLocaleString('zh-CN');
});

const getUserRoleText = (role) => {
  switch (role) {
    case 'TEACHER':
      return '教师';
    case 'STUDENT':
      return '学生';
    case 'ADMIN':
      return '管理员';
    default:
      return '未知';
  }
};
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-card,
.stats-card {
  background-color: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.welcome-card h1 {
  margin-bottom: 20px;
  color: var(--color-text);
}

.welcome-card h2,
.stats-card h2 {
  margin-bottom: 15px;
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 10px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.label {
  font-size: 14px;
  color: #666;
}

.value {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.stat-item {
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-primary);
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text);
}

@media (max-width: 768px) {
  .info-grid,
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>