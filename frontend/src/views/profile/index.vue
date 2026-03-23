<template>
  <div class="profile">
    <h1>个人中心</h1>
    <div class="profile-card">
      <h2>个人信息</h2>
      <div class="info-row">
        <div class="info-item">
          <span class="label">账号</span>
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
      <div class="action-grid">
        <div class="action-item" @click="openAvatarModal">
          <el-icon class="action-icon"><Avatar /></el-icon>
          <span>修改头像</span>
        </div>
        <div class="action-item" @click="openPasswordModal">
          <el-icon class="action-icon"><Lock /></el-icon>
          <span>修改密码</span>
        </div>
        <div class="action-item action-item-danger" @click="handleLogout">
          <el-icon class="action-icon"><SwitchButton /></el-icon>
          <span>退出登录</span>
        </div>
      </div>
    </div>
    <el-dialog
      v-model="avatarModalVisible"
      title="修改头像"
      width="500px"
    >
      <div class="avatar-upload-section">
        <div class="current-avatar">
          <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" alt="当前头像" />
          <span v-else class="avatar-placeholder">{{ (userStore.userInfo?.realName || userStore.userInfo?.username).charAt(0) }}</span>
        </div>
        <div class="upload-options">
          <el-radio-group v-model="avatarUploadType">
            <el-radio label="local">本地上传</el-radio>
            <el-radio label="url">URL上传</el-radio>
          </el-radio-group>
          <div v-if="avatarUploadType === 'local'" class="upload-local">
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :on-change="handleAvatarUpload"
              :auto-upload="false"
            >
              <el-button type="primary">选择文件</el-button>
            </el-upload>
          </div>
          <div v-else class="upload-url">
            <el-input v-model="avatarUrl" placeholder="请输入头像URL" />
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="avatarModalVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAvatarChange">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="passwordModalVisible"
      title="修改密码"
      width="500px"
    >
      <el-form :model="passwordForm" label-width="120px">
        <el-form-item label="旧密码" prop="oldPassword" :error="passwordErrors.oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword" :error="passwordErrors.newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword" :error="passwordErrors.confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordModalVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPasswordChange">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Avatar, Lock, SwitchButton } from '@element-plus/icons-vue';
import request from '@/utils/request';

const userStore = useUserStore();
const router = useRouter();

const avatarModalVisible = ref(false);
const avatarUploadType = ref('local');
const avatarUrl = ref('');
const selectedAvatarFile = ref(null);

const passwordModalVisible = ref(false);
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});
const passwordErrors = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
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

const openAvatarModal = () => {
  avatarUploadType.value = 'local';
  avatarUrl.value = '';
  selectedAvatarFile.value = null;
  avatarModalVisible.value = true;
};

const openPasswordModal = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  passwordErrors.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  passwordModalVisible.value = true;
};

const handleAvatarUpload = (file) => {
  selectedAvatarFile.value = file.raw;
};

const confirmAvatarChange = async () => {
  try {
    let avatarData;
    if (avatarUploadType.value === 'local' && selectedAvatarFile.value) {
      const formData = new FormData();
      formData.append('avatar', selectedAvatarFile.value);
      avatarData = URL.createObjectURL(selectedAvatarFile.value);
    } else if (avatarUploadType.value === 'url' && avatarUrl.value) {
      avatarData = avatarUrl.value;
    } else {
      ElMessage.warning('请选择或输入头像');
      return;
    }

    ElMessageBox.confirm('确定要修改头像吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const response = await request.put('/user/avatar', {
        userId: userStore.userInfo?.id,
        avatarUrl: avatarData
      });
      
      const updatedUserInfo = { ...userStore.userInfo, avatar: response.data };
      userStore.setUserInfo(updatedUserInfo);
      
      ElMessage.success('头像修改成功');
      avatarModalVisible.value = false;
    });
  } catch (error) {
    console.error('修改头像失败:', error);
    ElMessage.error('修改头像失败，请稍后重试');
  }
};

const validatePasswordForm = () => {
  let isValid = true;
  
  passwordErrors.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  
  if (!passwordForm.value.oldPassword) {
    passwordErrors.value.oldPassword = '请输入旧密码';
    isValid = false;
  }
  
  if (!passwordForm.value.newPassword) {
    passwordErrors.value.newPassword = '请输入新密码';
    isValid = false;
  }
  
  if (!passwordForm.value.confirmPassword) {
    passwordErrors.value.confirmPassword = '请确认新密码';
    isValid = false;
  } else if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordErrors.value.confirmPassword = '两次输入的密码不一致';
    isValid = false;
  }
  
  return isValid;
};

const confirmPasswordChange = async () => {
  if (!validatePasswordForm()) {
    return;
  }
  
  try {
    ElMessageBox.confirm('确定要修改密码吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const response = await request.put('/user/password', {
        userId: userStore.userInfo?.id,
        oldPassword: passwordForm.value.oldPassword,
        newPassword: passwordForm.value.newPassword
      });
      
      ElMessage.success('密码修改成功');
      passwordModalVisible.value = false;
    });
  } catch (error) {
    console.error('修改密码失败:', error);
    if (error.response && error.response.data && error.response.data.msg === '旧密码错误') {
      passwordErrors.value.oldPassword = '旧密码错误';
    } else {
      ElMessage.error('修改密码失败，请稍后重试');
    }
  }
};

const handleLogout = () => {
  userStore.logout();
  router.push('/login');
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

.info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  align-items: center;
  justify-content: center;
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

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
  margin-top: 10px;
}

.action-item {
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.action-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.action-icon {
  font-size: 32px;
  color: var(--color-primary);
  margin-bottom: 10px;
}

.action-item span {
  display: block;
  font-size: 14px;
  color: var(--color-text);
}

.action-item-danger .action-icon {
  color: #f56c6c;
}

.action-item-danger:hover {
  border-color: #f56c6c;
}

.avatar-upload-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
}

.el-form {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-form-item__label {
  font-size: 14px;
  color: #666;
}

.el-input {
  width: 100%;
}

.current-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  background-color: var(--color-primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.current-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 48px;
  font-weight: bold;
  color: var(--color-primary);
}

.upload-options {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.upload-local,
.upload-url {
  margin-top: 10px;
}

@media (max-width: 768px) {
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
