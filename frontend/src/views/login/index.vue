<template>
  <div class="login-page">
    <h1>登录</h1>
    <form @submit.prevent="handleLogin">
      <div>
        <label>用户名</label>
        <input type="text" v-model="username" placeholder="请输入用户名">
      </div>
      <div>
        <label>密码</label>
        <input type="password" v-model="password" placeholder="请输入密码">
      </div>
      <button type="submit" :disabled="loading">
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const username = ref('');
const password = ref('');
const loading = ref(false);
const router = useRouter();
const userStore = useUserStore();

const handleLogin = async () => {
  if (!username.value || !password.value) {
    alert('请输入用户名和密码');
    return;
  }
  
  loading.value = true;
  try {
    // 模拟登录请求
    // 实际项目中应该调用后端API
    const response = await new Promise((resolve) => {
      setTimeout(() => {
        let role = 'student';
        if (username.value === 'teacher') {
          role = 'TEACHER';
        } else if (username.value === 'admin') {
          role = 'ADMIN';
        }
        resolve({
          code: 200,
          data: {
            token: 'mock-token-123456',
            userInfo: {
              id: 1,
              username: username.value,
              realName: '测试用户',
              role: role
            }
          }
        });
      }, 1000);
    });
    
    const res = response;
    if (res.code === 200) {
      userStore.setToken(res.data.token);
      userStore.setUserInfo(res.data.userInfo);
      
      // 登录成功后跳转到首页
      router.push('/dashboard');
    } else {
      alert('登录失败，请检查用户名和密码');
    }
  } catch (error) {
    console.error('登录错误:', error);
    alert('登录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page {
  max-width: 400px;
  margin: 100px auto;
  padding: 20px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background-color: var(--color-card);
}

h1 {
  margin-bottom: 20px;
  color: var(--color-text);
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
</style>