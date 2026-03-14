<template>
  <div class="messages">
    <h1>消息通知</h1>
    <div class="message-header">
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
      <div class="message-actions">
        <el-button type="primary" size="small" @click="markAllAsRead" :disabled="unreadCount === 0">
          一键全部已读
        </el-button>
      </div>
    </div>
    <div class="message-list">
      <el-empty v-if="messages.length === 0" description="暂无消息" />
      <el-card v-for="message in messages" :key="message.id" :class="{ 'unread': message.isRead === 0 }">
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
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

// 获取消息列表
const getMessages = async () => {
  try {
    let type = null;
    if (activeTab.value === 'announcement' || activeTab.value === 'assignment' || activeTab.value === 'grading') {
      type = activeTab.value;
    }

    const response = await request.get('/message/list', {
      params: {
        userId: userStore.userInfo?.id,
        page: currentPage.value,
        size: pageSize.value,
        type
      }
    });

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
        userId: userStore.userInfo?.id
      }
    });
    unreadCount.value = response.data || 0;
  } catch (error) {
    console.error('获取未读消息数量失败:', error);
  }
};

// 标记消息为已读
const markAsRead = async (messageId) => {
  try {
    await request.put(`/message/read/${messageId}`, null, {
      params: {
        userId: userStore.userInfo?.id
      }
    });
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
    await request.put('/message/read/all', null, {
      params: {
        userId: userStore.userInfo?.id
      }
    });
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
  if (userStore.userInfo?.id) {
    getMessages();
    getUnreadCount();
  }
});
</script>

<style scoped>
.messages {
  padding: 20px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.message-tabs {
  flex: 1;
  min-width: 300px;
}

.message-actions {
  flex-shrink: 0;
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

.message-list {
  margin-top: 20px;
}

.message-item {
  cursor: pointer;
  transition: all 0.3s;
}

.message-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.unread {
  border-left: 4px solid #409eff;
}

.message-header-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.message-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text);
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
  color: #999;
}

.message-content {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 10px;
  line-height: 1.5;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--color-border);
}

.message-type {
  font-size: 12px;
  color: #999;
  background-color: var(--color-background-light);
  padding: 2px 8px;
  border-radius: 10px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .message-tabs {
    width: 100%;
  }
  
  .message-actions {
    width: 100%;
  }
  
  .message-actions .el-button {
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
