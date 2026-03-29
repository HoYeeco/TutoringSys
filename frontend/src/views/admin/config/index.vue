<template>
  <div class="admin-config">
    <!-- 基础配置 -->
    <el-card shadow="never" class="config-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><Setting /></el-icon>
          </div>
          <span class="card-title__text">基础配置</span>
        </div>
      </template>
      
      <el-form :model="basicConfig" label-width="140px" class="config-form">
        <el-form-item label="Token 有效期 (小时)">
          <el-input-number v-model="basicConfig.tokenExpiry" :min="1" :max="168" placeholder="请输入 Token 有效期" />
        </el-form-item>
        
        <el-form-item label="Token 刷新机制">
          <el-select v-model="basicConfig.tokenRefresh" placeholder="请选择刷新机制">
            <el-option label="自动刷新" value="auto" />
            <el-option label="手动刷新" value="manual" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="文件上传限制 (MB)">
          <el-input-number v-model="basicConfig.fileSizeLimit" :min="1" :max="100" placeholder="请输入文件大小限制" />
        </el-form-item>
        
        <el-form-item label="文件格式限制">
          <el-select v-model="basicConfig.fileFormats" multiple placeholder="请选择允许的文件格式">
            <el-option label="图片 (jpg, png, gif)" value="image" />
            <el-option label="文档 (doc, docx, pdf)" value="document" />
            <el-option label="压缩包 (zip, rar)" value="archive" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveBasicConfig">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 大模型配置 -->
    <el-card shadow="never" class="config-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><Cpu /></el-icon>
          </div>
          <span class="card-title__text">大模型配置</span>
        </div>
      </template>
      
      <el-form :model="llmConfig" label-width="140px" class="config-form">
        <el-form-item label="API 密钥">
          <el-input v-model="llmConfig.apiKey" type="password" placeholder="请输入 API 密钥" show-password />
        </el-form-item>
        
        <el-form-item label="超时时间 (秒)">
          <el-input-number v-model="llmConfig.timeout" :min="1" :max="60" placeholder="请输入超时时间" />
        </el-form-item>
        
        <el-form-item label="重试次数">
          <el-input-number v-model="llmConfig.retryCount" :min="0" :max="5" placeholder="请输入重试次数" />
        </el-form-item>
        
        <el-form-item label="模型选择">
          <el-select v-model="llmConfig.model" placeholder="请选择模型">
            <el-option label="Qwen2.5-Max" value="qwen2.5-max" />
            <el-option label="Qwen2.5-Turbo" value="qwen2.5-turbo" />
            <el-option label="Qwen2.5-0.5B" value="qwen2.5-0.5b" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveLLMConfig">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 缓存配置 -->
    <el-card shadow="never" class="config-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><DataLine /></el-icon>
          </div>
          <span class="card-title__text">缓存配置</span>
        </div>
      </template>
      
      <el-form :model="cacheConfig" label-width="140px" class="config-form">
        <el-form-item label="用户缓存 (分钟)">
          <el-input-number v-model="cacheConfig.userCache" :min="1" :max="1440" placeholder="请输入缓存时间" />
        </el-form-item>
        
        <el-form-item label="课程缓存 (分钟)">
          <el-input-number v-model="cacheConfig.courseCache" :min="1" :max="1440" placeholder="请输入缓存时间" />
        </el-form-item>
        
        <el-form-item label="作业缓存 (分钟)">
          <el-input-number v-model="cacheConfig.assignmentCache" :min="1" :max="1440" placeholder="请输入缓存时间" />
        </el-form-item>
        
        <el-form-item label="统计数据缓存 (分钟)">
          <el-input-number v-model="cacheConfig.statsCache" :min="1" :max="1440" placeholder="请输入缓存时间" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveCacheConfig">保存配置</el-button>
          <el-button type="danger" @click="clearCache">清除缓存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting, Cpu, DataLine } from '@element-plus/icons-vue'

// 基础配置
const basicConfig = reactive({
  tokenExpiry: 24,
  tokenRefresh: 'auto',
  fileSizeLimit: 10,
  fileFormats: ['image', 'document']
})

// 大模型配置
const llmConfig = reactive({
  apiKey: '',
  timeout: 30,
  retryCount: 3,
  model: 'qwen2.5-max'
})

// 缓存配置
const cacheConfig = reactive({
  userCache: 60,
  courseCache: 30,
  assignmentCache: 15,
  statsCache: 10
})

// 保存基础配置
const saveBasicConfig = () => {
  console.log('保存基础配置:', basicConfig)
  ElMessage.success('基础配置保存成功')
}

// 保存大模型配置
const saveLLMConfig = () => {
  console.log('保存大模型配置:', llmConfig)
  ElMessage.success('大模型配置保存成功')
}

// 保存缓存配置
const saveCacheConfig = () => {
  console.log('保存缓存配置:', cacheConfig)
  ElMessage.success('缓存配置保存成功')
}

// 清除缓存
const clearCache = () => {
  console.log('清除缓存')
  ElMessage.success('缓存已清除')
}
</script>

<style scoped>
.admin-config {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  max-height: calc(100vh - 100px);
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

/* 通用卡片样式 */
:deep(.el-card) {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  min-height: 400px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: none;
  background: transparent;
  overflow: hidden;
}

:deep(.el-card__body) {
  padding: 0 20px 20px;
  overflow: hidden;
}

/* 配置卡片 */
.config-section {
  margin-bottom: 0;
}

/* 卡片标题 */
.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-title__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.card-title__icon .el-icon {
  font-size: 18px;
}

.card-title__text {
  color: #303133;
}

/* 表单样式 */
.config-form {
  padding: 10px 0;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 8px;
  padding: 10px 24px;
  font-size: 14px;
}

:deep(.el-button--primary) {
  background: #667eea 0%;
  border: none;
}

:deep(.el-button--primary:hover) {
  opacity: 0.9;
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  border: none;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .config-form :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

@media (max-width: 768px) {
  .admin-config {
    padding: 16px;
  }
  
  .config-form {
    padding: 5px 0;
  }
  
  :deep(.el-form-item__label) {
    font-size: 13px;
  }
  
  :deep(.el-button) {
    padding: 8px 16px;
    font-size: 13px;
  }
}
</style>
