<template>
  <div class="profile">
    <!-- 个人信息模块 -->
    <el-card shadow="never" class="profile-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><User /></el-icon>
            </div>
            <span>个人信息</span>
          </div>
        </div>
      </template>
      
      <div class="profile-content">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-display" @click="openAvatarModal">
            <div class="avatar-large">
              <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" alt="头像" />
              <div v-else class="avatar-placeholder">
                {{ (userStore.userInfo?.realName || userStore.userInfo?.username).charAt(0).toUpperCase() }}
              </div>
            </div>
            <div class="avatar-overlay">
              <el-icon><Camera /></el-icon>
              <span>修改头像</span>
            </div>
          </div>
        </div>

        <!-- 信息详情 -->
        <div class="info-section">
          <div class="info-grid">
            <div class="info-item">
              <div class="info-item__label">
                <el-icon><User /></el-icon>
                <span>账号</span>
              </div>
              <div class="info-item__value">{{ userStore.userInfo?.username }}</div>
            </div>

            <div class="info-item">
              <div class="info-item__label">
                <el-icon><UserFilled /></el-icon>
                <span>姓名</span>
              </div>
              <div class="info-item__value">{{ userStore.userInfo?.realName || '-' }}</div>
            </div>

            <div class="info-item">
              <div class="info-item__label">
                <el-icon><Flag /></el-icon>
                <span>角色</span>
              </div>
              <div class="info-item__value">
                <el-tag :type="getUserRoleType(userStore.userInfo?.role)">
                  {{ getUserRoleText(userStore.userInfo?.role) }}
                </el-tag>
              </div>
            </div>

            <div class="info-item">
              <div class="info-item__label">
                <el-icon><CircleCheck /></el-icon>
                <span>账号状态</span>
              </div>
              <div class="info-item__value">
                <el-tag type="success" effect="plain">正常</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 账号设置模块 -->
    <el-card shadow="never" class="settings-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Setting /></el-icon>
            </div>
            <span>账号设置</span>
          </div>
        </div>
      </template>
      
      <div class="settings-grid">
        <div class="setting-item" @click="openAvatarModal">
          <div class="setting-item__icon">
            <el-icon><Avatar /></el-icon>
          </div>
          <div class="setting-item__content">
            <div class="setting-item__title">修改头像</div>
            <div class="setting-item__desc">上传个性化头像</div>
          </div>
          <div class="setting-item__action">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="setting-item" @click="openPasswordModal">
          <div class="setting-item__icon">
            <el-icon><Lock /></el-icon>
          </div>
          <div class="setting-item__content">
            <div class="setting-item__title">修改密码</div>
            <div class="setting-item__desc">定期修改密码保障安全</div>
          </div>
          <div class="setting-item__action">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="setting-item setting-item-danger" @click="handleLogout">
          <div class="setting-item__icon">
            <el-icon><SwitchButton /></el-icon>
          </div>
          <div class="setting-item__content">
            <div class="setting-item__title">退出登录</div>
            <div class="setting-item__desc">退出当前账号</div>
          </div>
          <div class="setting-item__action">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 修改头像对话框 -->
    <el-dialog
      v-model="avatarModalVisible"
      title="修改头像"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="avatar-modal">
        <!-- 当前头像预览 -->
        <div class="avatar-preview-section">
          <div class="preview-title">当前头像</div>
          <div class="current-avatar">
            <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" alt="当前头像" />
            <div v-else class="avatar-placeholder-large">
              {{ (userStore.userInfo?.realName || userStore.userInfo?.username).charAt(0).toUpperCase() }}
            </div>
          </div>
        </div>

        <!-- 上传选项 -->
        <div class="upload-section">
          <div class="preview-title">新头像预览</div>
          <div class="new-avatar-preview">
            <div class="avatar-preview-box">
              <img v-if="previewAvatarUrl" :src="previewAvatarUrl" alt="新头像预览" />
              <div v-else class="avatar-placeholder-large">
                {{ (userStore.userInfo?.realName || userStore.userInfo?.username).charAt(0).toUpperCase() }}
              </div>
            </div>
          </div>

          <div class="upload-local">
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :on-change="handleAvatarUpload"
              :auto-upload="false"
              accept="image/*"
            >
              <el-button type="primary" size="large">
                <el-icon><Upload /></el-icon> 选择图片
              </el-button>
            </el-upload>
            <p class="upload-tip">支持 JPG、PNG、GIF、WebP 格式，大小不超过 10MB</p>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="avatarModalVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAvatarChange" :disabled="!selectedAvatarFile">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordModalVisible"
      title="修改密码"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword" :error="passwordErrors.oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword" :error="passwordErrors.newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
            size="large"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword" :error="passwordErrors.confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            size="large"
          />
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
import { ref, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  User, UserFilled, Flag, CircleCheck, Camera, Setting,
  Avatar, Lock, SwitchButton, ArrowRight, Upload, Link
} from '@element-plus/icons-vue';
import request from '@/utils/request';

const userStore = useUserStore();
const router = useRouter();

const avatarModalVisible = ref(false);
const selectedAvatarFile = ref(null);
const previewAvatarUrl = ref('');

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

const getUserRoleType = (role) => {
  switch (role) {
    case 'TEACHER':
      return 'warning';
    case 'STUDENT':
      return 'success';
    case 'ADMIN':
      return 'danger';
    default:
      return 'info';
  }
};

const openAvatarModal = () => {
  selectedAvatarFile.value = null;
  previewAvatarUrl.value = userStore.userInfo?.avatar || '';
  avatarModalVisible.value = true;
};

const handleAvatarUpload = (file) => {
  const isImage = file.raw.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片文件！');
    return;
  }

  const isLt10M = file.raw.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB！');
    return;
  }

  selectedAvatarFile.value = file.raw;
  
  const reader = new FileReader();
  reader.onload = (e) => {
    previewAvatarUrl.value = e.target.result;
  };
  reader.readAsDataURL(file.raw);
};

const confirmAvatarChange = async () => {
  try {
    if (selectedAvatarFile.value) {
      const formData = new FormData();
      formData.append('file', selectedAvatarFile.value);
      
      const response = await request.post('/user/avatar', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      const newAvatarUrl = response.data;
      userStore.setUserInfo({
        ...userStore.userInfo,
        avatar: newAvatarUrl
      });
      
      avatarModalVisible.value = false;
      ElMessage.success('头像修改成功');
    } else {
      ElMessage.warning('请选择图片上传');
    }
  } catch (error) {
    console.error('修改头像失败:', error);
    ElMessage.error('修改头像失败');
  }
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

const confirmPasswordChange = async () => {
  // 验证
  let hasError = false;
  const errors = { oldPassword: '', newPassword: '', confirmPassword: '' };

  if (!passwordForm.value.oldPassword) {
    errors.oldPassword = '请输入旧密码';
    hasError = true;
  }

  if (!passwordForm.value.newPassword) {
    errors.newPassword = '请输入新密码';
    hasError = true;
  } else if (passwordForm.value.newPassword.length < 6) {
    errors.newPassword = '密码长度至少 6 位';
    hasError = true;
  }

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致';
    hasError = true;
  }

  passwordErrors.value = errors;

  if (hasError) return;

  try {
    await request.put('/user/password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    });

    passwordModalVisible.value = false;
    ElMessage.success('密码修改成功，请重新登录');
    
    // 退出登录
    setTimeout(() => {
      userStore.logout();
      router.push('/login');
    }, 1500);
  } catch (error) {
    console.error('修改密码失败:', error);
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message);
    } else {
      ElMessage.error('修改密码失败');
    }
  }
};

const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    userStore.logout();
    router.push('/login');
    ElMessage.success('已退出登录');
  }).catch(() => {});
};
</script>

<style scoped>
.profile {
  padding: 24px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(4px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
}

/* 通用卡片样式 */
:deep(.el-card) {
  border: none;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: none;
  background: transparent;
}

:deep(.el-card__body) {
  padding: 0 20px 20px;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header__title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.card-header__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.card-header__icon .el-icon {
  font-size: 24px;
}

/* 个人信息区域 */
.profile-content {
  display: flex;
  gap: 40px;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  flex-shrink: 0;
}

.avatar-display {
  position: relative;
  cursor: pointer;
  margin-bottom: 20px;
}

.avatar-large {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border: 4px solid #fff;
}

.avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 72px;
  font-weight: bold;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 50%;
}

.avatar-display:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

/* 信息详情区域 */
.info-section {
  flex: 1;
  padding: 20px 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.info-item {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.info-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.info-item__label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
  font-weight: 500;
}

.info-item__label .el-icon {
  font-size: 16px;
}

.info-item__value {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

/* 账号设置区域 */
.settings-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.setting-item {
  display: flex;
  align-items: center;
  padding: 24px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.3s ease;
}

.setting-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.12);
  transform: translateY(-2px);
}

.setting-item-danger:hover {
  border-color: #f56c6c;
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.12);
}

.setting-item__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}

.setting-item-danger .setting-item__icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.setting-item__icon .el-icon {
  font-size: 24px;
}

.setting-item__content {
  flex: 1;
}

.setting-item__title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.setting-item__desc {
  font-size: 13px;
  color: #909399;
}

.setting-item__action {
  color: #909399;
  font-size: 20px;
}

/* 头像对话框 */
.avatar-modal {
  display: flex;
  gap: 40px;
}

.avatar-preview-section,
.upload-section {
  flex: 1;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  text-align: center;
}

.current-avatar,
.avatar-preview-box {
  width: 180px;
  height: 180px;
  margin: 0 auto 20px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  border: 4px solid #ebeef5;
}

.current-avatar img,
.avatar-preview-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder-large {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 72px;
  font-weight: bold;
}

.upload-type-selector {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.upload-local,
.upload-url {
  text-align: center;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 12px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .profile-content {
    flex-direction: column;
    align-items: center;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .settings-grid {
    grid-template-columns: 1fr;
  }

  .avatar-modal {
    flex-direction: column;
  }
}
</style>
