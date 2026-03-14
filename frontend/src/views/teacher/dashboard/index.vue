<template>
  <div class="teacher-dashboard">
    <h1>{{ getGreeting() }}，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h1>
    
    <!-- 数据概览卡片 -->
    <div class="stats-card">
      <h2>数据概览</h2>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ stats.courseCount || 0 }}</div>
          <div class="stat-label">负责课程数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.studentCount || 0 }}</div>
          <div class="stat-label">对应学生数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.totalStudents || 0 }}</div>
          <div class="stat-label">学生总数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.ongoingAssignments || 0 }}</div>
          <div class="stat-label">进行中作业</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.pendingReviews || 0 }}</div>
          <div class="stat-label">待复核作业</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ stats.overdueAssignments || 0 }}</div>
          <div class="stat-label">已逾期作业</div>
        </div>
      </div>
    </div>
    
    <!-- 快捷操作 -->
    <div class="quick-access">
      <h2>快捷操作</h2>
      <div class="quick-grid">
        <div class="quick-item" @click="goToCreateAssignment">
          <el-icon class="quick-icon"><Edit /></el-icon>
          <span>发布作业</span>
        </div>
        <div class="quick-item" @click="goToCourses">
          <el-icon class="quick-icon"><Book /></el-icon>
          <span>课程管理</span>
        </div>
        <div class="quick-item" @click="goToSubmissions">
          <el-icon class="quick-icon"><Ticket /></el-icon>
          <span>批改复核</span>
          <el-badge v-if="stats.pendingReviews > 0" :value="stats.pendingReviews" class="review-badge" />
        </div>
        <div class="quick-item" @click="goToDrafts">
          <el-icon class="quick-icon"><Document /></el-icon>
          <span>草稿箱</span>
        </div>
      </div>
    </div>
    
    <!-- 最近动态 -->
    <div class="recent-activities">
      <h2>最近动态</h2>
      <div class="activity-list">
        <el-card v-for="activity in recentActivities" :key="activity.id" class="activity-item">
          <div class="activity-header">
            <div class="activity-title">{{ activity.title }}</div>
            <div class="activity-time">{{ formatTime(activity.time) }}</div>
          </div>
          <div class="activity-content">{{ activity.content }}</div>
          <div class="activity-action" v-if="activity.action">
            <el-button type="primary" size="small" @click="handleActivityAction(activity)">
              {{ activity.action }}
            </el-button>
          </div>
        </el-card>
        <div v-if="recentActivities.length === 0" class="empty-activities">
          暂无动态
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';
import { Edit, Book, Ticket, Document } from '@element-plus/icons-vue';

const userStore = useUserStore();
const router = useRouter();

const stats = ref({
  courseCount: 0,
  studentCount: 0,
  totalStudents: 0,
  ongoingAssignments: 0,
  pendingReviews: 0,
  overdueAssignments: 0
});

const recentActivities = ref([]);

// 根据时间获取问候语
const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour >= 5 && hour < 9) {
    return '早上好';
  } else if (hour >= 9 && hour < 11) {
    return '上午好';
  } else if (hour >= 11 && hour < 13) {
    return '中午好';
  } else if (hour >= 13 && hour < 19) {
    return '下午好';
  } else {
    return '晚上好';
  }
};

// 获取统计数据
const getStats = async () => {
  try {
    // 模拟数据，实际项目中应该调用后端API
    stats.value = {
      courseCount: 3,
      studentCount: 45,
      totalStudents: 120,
      ongoingAssignments: 5,
      pendingReviews: 3,
      overdueAssignments: 1
    };
  } catch (error) {
    console.error('获取统计数据失败:', error);
  }
};

// 获取最近动态
const getRecentActivities = async () => {
  try {
    // 模拟数据，实际项目中应该调用后端API
    recentActivities.value = [
      {
        id: 1,
        title: '新作业发布',
        content: '数学课程发布了新作业《函数的应用》，截止时间：2026-03-20',
        time: new Date().toISOString(),
        action: '查看详情',
        url: '/teacher/assignments/1/edit'
      },
      {
        id: 2,
        title: '作业复核完成',
        content: '英语作业《阅读理解》已复核完成，共15份',
        time: new Date(Date.now() - 86400000).toISOString(),
        action: '查看报告',
        url: '/teacher/analysis'
      },
      {
        id: 3,
        title: '学生提交作业',
        content: '张三提交了数学作业《函数的应用》',
        time: new Date(Date.now() - 172800000).toISOString(),
        action: '查看作业',
        url: '/teacher/assignments/1/submissions'
      }
    ];
  } catch (error) {
    console.error('获取最近动态失败:', error);
  }
};

// 快捷操作方法
const goToCreateAssignment = () => {
  router.push('/teacher/assignments/create');
};

const goToCourses = () => {
  router.push('/teacher/courses');
};

const goToSubmissions = () => {
  router.push('/teacher/grading');
};

const goToDrafts = () => {
  // 草稿箱功能需要单独实现
  router.push('/teacher/assignments/create');
};

// 处理活动操作
const handleActivityAction = (activity) => {
  if (activity.url) {
    router.push(activity.url);
  }
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(() => {
  getStats();
  getRecentActivities();
});
</script>

<style scoped>
.teacher-dashboard {
  padding: 20px;
}

.stats-card {
  background-color: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.quick-access {
  background-color: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.recent-activities {
  background-color: var(--color-card);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.teacher-dashboard h1 {
  margin-bottom: 20px;
  color: var(--color-text);
}

.stats-card h2,
.quick-access h2,
.recent-activities h2 {
  margin-bottom: 15px;
  color: var(--color-text);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 10px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-item {
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  transition: transform 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--color-primary);
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text);
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
}

.quick-item {
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
}

.quick-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.quick-icon {
  font-size: 32px;
  color: var(--color-primary);
  margin-bottom: 10px;
}

.quick-item span {
  display: block;
  font-size: 14px;
  color: var(--color-text);
}

.review-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}

.activity-list {
  margin-top: 15px;
}

.activity-item {
  margin-bottom: 15px;
  transition: transform 0.3s ease;
}

.activity-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.activity-title {
  font-size: 16px;
  font-weight: 500;
  color: var(--color-text);
}

.activity-time {
  font-size: 12px;
  color: #999;
}

.activity-content {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 10px;
  line-height: 1.5;
}

.activity-action {
  margin-top: 10px;
  text-align: right;
}

.empty-activities {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-grid {
    grid-template-columns: 1fr;
  }
  
  .activity-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
</style>