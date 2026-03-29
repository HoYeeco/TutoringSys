<template>
  <div class="messages">
    <!-- 消息通知模块 -->
    <el-card shadow="never" class="messages-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Bell /></el-icon>
            </div>
            <span>消息通知</span>
          </div>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索消息标题或内容"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="markAllAsRead" :disabled="unreadCount === 0">
              一键全部已读
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="message-tabs">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="全部" name="all">
            <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
          </el-tab-pane>
          <el-tab-pane label="未读" name="unread">
            <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
          </el-tab-pane>
          <el-tab-pane label="系统公告" name="announcement"></el-tab-pane>
          <el-tab-pane label="作业通知" name="assignment"></el-tab-pane>
          <el-tab-pane label="批改通知" name="grading"></el-tab-pane>
        </el-tabs>
      </div>
      
      <div class="message-list">
        <div v-if="messages.length === 0" class="empty-container">
          <el-empty description="暂无消息" :image-size="160" />
        </div>
        <el-card v-for="message in messages" :key="message.id" :class="{ 'message-card': true, 'unread': message.isRead === 0 }">
          <div class="message-item" @click="handleMessageClick(message)">
            <div class="message-header-item">
              <div class="message-title">
                {{ message.title }}
                <span v-if="message.isRead === 0" class="unread-dot"></span>
              </div>
              <div class="message-time">{{ formatTime(message.createTime) }}</div>
            </div>
            <div class="message-content">{{ message.content }}</div>
            <div class="message-footer">
              <span class="message-type">{{ getMessageTypeText(message.type) }}</span>
              <el-button type="text" size="small" @click.stop="markAsRead(message.id)">
                {{ message.isRead === 0 ? '标记已读' : '已读' }}
              </el-button>
            </div>
          </div>
        </el-card>
        <el-pagination
          v-if="total > 0"
          class="pagination"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Bell, Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

const userStore = useUserStore();
const router = useRouter();

// 消息相关状态
const activeTab = ref('all');
const messages = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const unreadCount = ref(0);
const searchKeyword = ref('');

// 获取消息列表
const getMessages = async () => {
  try {
    let type = null;
    if (activeTab.value === 'announcement' || activeTab.value === 'assignment' || activeTab.value === 'grading') {
      type = activeTab.value;
    }

    const response = await request.get('/message/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        type,
        keyword: searchKeyword.value || undefined
      }
    });

    let records = response.data.records || [];
    
    if (activeTab.value === 'unread') {
      records = records.filter((msg) => msg.isRead === 0);
    }
    
    messages.value = records;
    total.value = response.data.total || 0;
  } catch (error) {
    console.error('获取消息列表失败:', error);
    ElMessage.error('获取消息列表失败');
  }
};

// 获取未读消息数量
const getUnreadCount = async () => {
  try {
    const response = await request.get('/message/unread-count');
    unreadCount.value = response.data || 0;
  } catch (error) {
    console.error('获取未读消息数量失败:', error);
  }
};

// 标记消息为已读
const markAsRead = async (messageId) => {
  try {
    await request.put(`/message/read/${messageId}`);
    // 更新本地消息状态
    const message = messages.value.find(msg => msg.id === messageId);
    if (message) {
      message.isRead = 1;
    }
    getUnreadCount();
    ElMessage.success('标记成功');
  } catch (error) {
    console.error('标记消息已读失败:', error);
    ElMessage.error('标记失败');
  }
};

// 标记所有消息为已读
const markAllAsRead = async () => {
  try {
    await request.put('/message/read-all');
    // 更新本地消息状态
    messages.value.forEach(message => {
      message.isRead = 1;
    });
    unreadCount.value = 0;
    ElMessage.success('标记成功');
  } catch (error) {
    console.error('标记所有消息已读失败:', error);
    ElMessage.error('标记失败');
  }
};

// 处理消息点击
const handleMessageClick = (message) => {
  // 标记为已读
  if (message.isRead === 0) {
    markAsRead(message.id);
  }
  
  // 根据消息类型跳转到对应页面
  if (message.relatedType === 'assignment' && message.relatedId) {
    if (message.type === 'assignment') {
      // 作业发布通知，跳转到作业提交页面
      router.push(`/student/assignments/${message.relatedId}/submit`);
    } else if (message.type === 'grading') {
      // 作业批改通知，跳转到批改报告页面
      router.push(`/student/reports/${message.relatedId}`);
    }
  }
};

// 处理标签切换
const handleTabClick = () => {
  currentPage.value = 1;
  getMessages();
};

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page;
  getMessages();
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  getMessages();
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 获取消息类型文本
const getMessageTypeText = (type) => {
  switch (type) {
    case 'announcement':
      return '系统公告';
    case 'assignment':
      return '作业通知';
    case 'grading':
      return '批改通知';
    default:
      return '未知类型';
  }
};

// 页面加载时获取消息
onMounted(() => {
  getMessages();
  getUnreadCount();
});
</script>

<style scoped>
.messages {
  padding: 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
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
  flex-wrap: wrap;
  gap: 16px;
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 250px;
}

/* 消息标签 */
.message-tabs {
  margin-bottom: 20px;
}

.unread-badge {
  display: inline-block;
  background-color: #f56c6c;
  color: white;
  font-size: 12px;
  padding: 0 6px;
  border-radius: 10px;
  margin-left: 5px;
}

/* 消息列表 */
.message-list {
  margin-top: 20px;
}

.message-card {
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.message-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.message-card.unread {
  border-left: 4px solid #409eff;
}

.message-item {
  cursor: pointer;
}

.message-header-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.unread-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: #f56c6c;
  border-radius: 50%;
  margin-left: 8px;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-content {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.6;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.message-type {
  font-size: 12px;
  color: #909399;
  background-color: #f5f7fa;
  padding: 4px 10px;
  border-radius: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 空状态容器 */
.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 60px 20px;
  background: linear-gradient(135deg, #fafafa 0%, #f5f7fa 100%);
  border-radius: 16px;
}

.empty-container :deep(.el-empty__description) {
  color: #909399;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .message-title {
    font-size: 14px;
  }
  
  .message-content {
    font-size: 13px;
  }
}
</style>
