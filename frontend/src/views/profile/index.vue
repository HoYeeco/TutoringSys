<template>
  <div class="profile">
    <h1>个人中心</h1>
    <div class="profile-card">
      <h2>个人信息</h2>
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
          <span class="label">账号状态</span>
          <span class="value status-active">正常</span>
        </div>
      </div>
    </div>
    <div class="profile-card">
      <h2>账号设置</h2>
      <form @submit.prevent="handleUpdate">
        <div class="form-group">
          <label>密码</label>
          <input type="password" v-model="password" placeholder="请输入新密码">
        </div>
        <div class="form-group">
          <label>确认密码</label>
          <input type="password" v-model="confirmPassword" placeholder="请确认新密码">
        </div>
        <button type="submit" :disabled="loading">
          {{ loading ? '更新中...' : '更新信息' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useUserStore } from '@/stores/user';

const userStore = useUserStore();
const password = ref('');
const confirmPassword = ref('');
const loading = ref(false);

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

const handleUpdate = async () => {
  if (password.value && password.value !== confirmPassword.value) {
    alert('两次输入的密码不一致');
    return;
  }
  
  loading.value = true;
  try {
    // 模拟更新请求
    // 实际项目中应该调用后端API
    await new Promise((resolve) => {
      setTimeout(() => {
        resolve({});
      }, 1000);
    });
    
    alert('更新成功');
    password.value = '';
    confirmPassword.value = '';
  } catch (error) {
    console.error('更新错误:', error);
    alert('更新失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.profile {
  padding: 20px;
}

.profile-card {
  background-color: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.profile-card h2 {
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

.status-active {
  color: #67c23a;
}

.form-group {
  margin-bottom: 15px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  color: var(--color-text);
}

input {
  padding: 10px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background-color: var(--color-background);
  color: var(--color-text);
}

button {
  padding: 10px;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background-color: var(--color-primary);
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover {
  background-color: var(--color-primary-hover);
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>