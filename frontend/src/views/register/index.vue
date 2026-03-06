<template>
  <div class="register-page">
    <h1>注册</h1>
    <form>
      <div>
        <label>用户名</label>
        <input type="text" v-model="username">
      </div>
      <div>
        <label>密码</label>
        <input type="password" v-model="password">
      </div>
      <div>
        <label>确认密码</label>
        <input type="password" v-model="confirmPassword">
      </div>
      <button type="submit">注册</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const username = ref('');
const password = ref('');
const confirmPassword = ref('');
</script>