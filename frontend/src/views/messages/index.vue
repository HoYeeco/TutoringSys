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
            <el-button
              type="primary"
              @click="markAllAsRead"
              :disabled="unreadCount === 0"
            >
              一键全部已读
            </el-button>
          </div>
        </div>
      </template>

      <div class="message-tabs">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="全部" name="all"></el-tab-pane>
          <el-tab-pane label="未读" name="unread"></el-tab-pane>
          <el-tab-pane label="已读" name="read"></el-tab-pane>
        </el-tabs>
      </div>

      <div class="message-list">
        <div v-if="messages.length === 0" class="empty-container">
          <el-empty description="暂无消息" :image-size="160" />
        </div>
        <el-card
          v-for="message in messages"
          :key="message.id"
          :class="{ 'message-card': true, unread: message.isRead === 0 }"
        >
          <div class="message-item" @click="handleMessageClick(message)">
            <div class="message-body">
              <div class="message-header-row">
                <div class="message-title">
                  {{ message.title }}
                  <span v-if="message.isRead === 0" class="unread-tag">未读</span>
                </div>
                <div class="message-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(message.createTime) }}
                </div>
              </div>
              <div class="message-content">{{ message.content }}</div>
              <div class="message-footer">
                <el-button
                  v-if="message.isRead === 0"
                  type="primary"
                  link
                  size="small"
                  @click.stop="markAsRead(message)"
                >
                  标记已读
                </el-button>
                <span v-else class="read-status">
                  <el-icon><Check /></el-icon>
                  已读
                </span>
              </div>
            </div>
          </div>
        </el-card>
        <div class="pagination-container">
          <el-pagination
            v-if="total > 0"
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Bell, Search, Clock, Check } from '@element-plus/icons-vue';
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
  if (!userStore.userInfo?.id) return;
  
  try {
    const params = {
      userId: userStore.userInfo?.id,
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
    };

    // 处理标签页筛选
    if (activeTab.value === 'unread') {
      params.readStatus = 0;
    } else if (activeTab.value === 'read') {
      params.readStatus = 1;
    }

    const response = await request.get('/message/list', { params });

    messages.value = response.data.records || [];
    total.value = response.data.total || 0;
  } catch (error) {
    console.error('获取消息列表失败:', error);
    ElMessage.error('获取消息列表失败');
  }
};

// 获取未读消息数量
const getUnreadCount = async () => {
  try {
    const response = await request.get('/message/unread-count', {
      params: {
        userId: userStore.userInfo?.id,
      },
    });
    unreadCount.value = response.data || 0;
  } catch (error) {
    console.error('获取未读消息数量失败:', error);
  }
};

// 标记消息为已读
const markAsRead = async (message) => {
  try {
    await request.put(`/message/read/${message.messageId}`, null, {
      params: {
        userId: userStore.userInfo?.id,
      },
    });
    // 更新本地消息状态
    const localMessage = messages.value.find((msg) => msg.id === message.id);
    if (localMessage) {
      localMessage.isRead = 1;
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
    await request.put('/message/read/all', null, {
      params: {
        userId: userStore.userInfo?.id,
      },
    });
    // 更新本地消息状态
    messages.value.forEach((message) => {
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
    markAsRead(message);
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

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
  getMessages();
};

// 处理标签切换 - 只重置页码，筛选由 watch 处理
const handleTabClick = () => {
  currentPage.value = 1;
};

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  getMessages();
};

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page;
  getMessages();
};

// 监听标签切换，触发筛选
watch(activeTab, () => {
  getMessages();
});

// 页面加载时获取消息和未读数量
onMounted(() => {
  if (userStore.userInfo?.id) {
    getMessages();
    getUnreadCount();
  }
});

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  
  // 小于1小时显示"X分钟前"
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000));
    return minutes < 1 ? '刚刚' : `${minutes}分钟前`;
  }
  
  // 小于24小时显示"X小时前"
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000));
    return `${hours}小时前`;
  }
  
  // 小于7天显示"X天前"
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000));
    return `${days}天前`;
  }
  
  // 否则显示具体日期
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};
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
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
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

/* 消息列表 */
.message-list {
  margin-top: 20px;
}

.message-card {
  margin-bottom: 16px;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.message-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.message-card.unread {
  border-left-color: #409eff;
  background: linear-gradient(135deg, #f0f7ff 0%, #ffffff 100%);
}

.message-item {
  cursor: pointer;
  padding: 10px 4px 4px 4px;
}

.message-body {
  flex: 1;
  min-width: 0;
}

.message-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 12px;
}

.message-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-tag {
  font-size: 11px;
  color: #fff;
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.message-time {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.message-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.message-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.read-status {
  font-size: 13px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 分页 */
.pagination-container {
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
  
  .message-header-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
