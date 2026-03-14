<template>
  <div class="admin-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础配置 -->
        <el-tab-pane label="基础配置" name="basic">
          <el-form :model="basicConfig" label-width="120px">
            <el-form-item label="Token 有效期(小时)">
              <el-input-number v-model="basicConfig.tokenExpiry" :min="1" :max="168" placeholder="请输入 Token 有效期" />
            </el-form-item>
            
            <el-form-item label="Token 刷新机制">
              <el-select v-model="basicConfig.tokenRefresh" placeholder="请选择刷新机制">
                <el-option label="自动刷新" value="auto" />
                <el-option label="手动刷新" value="manual" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="文件上传限制(MB)">
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
        </el-tab-pane>
        
        <!-- 大模型配置 -->
        <el-tab-pane label="大模型配置" name="llm">
          <el-form :model="llmConfig" label-width="120px">
            <el-form-item label="API 密钥">
              <el-input v-model="llmConfig.apiKey" type="password" placeholder="请输入 API 密钥" show-password />
            </el-form-item>
            
            <el-form-item label="超时时间(秒)">
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
        </el-tab-pane>
        
        <!-- 缓存配置 -->
        <el-tab-pane label="缓存配置" name="cache">
          <el-form :model="cacheConfig" label-width="120px">
            <el-form-item label="用户缓存(分钟)">
              <el-input-number v-model="cacheConfig.userCache" :min="1" :max="1440" placeholder="请输入缓存时间" />
            </el-form-item>
            
            <el-form-item label="课程缓存(分钟)">
              <el-input-number v-model="cacheConfig.courseCache" :min="1" :max="1440" placeholder="请输入缓存时间" />
            </el-form-item>
            
            <el-form-item label="作业缓存(分钟)">
              <el-input-number v-model="cacheConfig.assignmentCache" :min="1" :max="1440" placeholder="请输入缓存时间" />
            </el-form-item>
            
            <el-form-item label="统计数据缓存(分钟)">
              <el-input-number v-model="cacheConfig.statsCache" :min="1" :max="1440" placeholder="请输入缓存时间" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveCacheConfig">保存配置</el-button>
              <el-button type="danger" @click="clearCache">清除缓存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 激活的标签页
const activeTab = ref('basic')

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
  // 模拟保存操作
  console.log('保存基础配置:', basicConfig)
  ElMessage.success('基础配置保存成功')
}

// 保存大模型配置
const saveLLMConfig = () => {
  // 模拟保存操作
  console.log('保存大模型配置:', llmConfig)
  ElMessage.success('大模型配置保存成功')
}

// 保存缓存配置
const saveCacheConfig = () => {
  // 模拟保存操作
  console.log('保存缓存配置:', cacheConfig)
  ElMessage.success('缓存配置保存成功')
}

// 清除缓存
const clearCache = () => {
  // 模拟清除缓存操作
  console.log('清除缓存')
  ElMessage.success('缓存已清除')
}
</script>

<style scoped>
.admin-config {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.admin-config .el-form {
  margin-top: 20px;
}

.admin-config .el-form-item {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .admin-config {
    padding: 10px;
  }
  
  .admin-config .el-form-item {
    margin-bottom: 15px;
  }
}
</style>