<template>
  <div class="dashboard student-dashboard">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1 class="welcome-title">{{ getGreeting() }}，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h1>
          <p class="welcome-subtitle">所有投资中，学习回报最优。</p>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="stats-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <span class="card-title__text">数据概览</span>
        </div>
      </template>
      
      <div class="stats-grid">
        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--course">
            <el-icon><School /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__label">已选课程数</div>
            <div class="stat-card__value">{{ studentStats.courseCount || 0 }}</div>
          </div>
        </el-card>

        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__label">待提交作业</div>
            <div class="stat-card__value">{{ studentStats.pendingAssignments || 0 }}</div>
          </div>
        </el-card>

        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--graded">
            <el-icon><DocumentChecked /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__label">已批改作业</div>
            <div class="stat-card__value">{{ studentStats.gradedAssignments || 0 }}</div>
          </div>
        </el-card>

        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--score">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__label">平均得分</div>
            <div class="stat-card__value">{{ studentStats.averageScore || 0 }}</div>
          </div>
        </el-card>
      </div>
    </el-card>

    <el-card shadow="never" class="chart-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><Histogram /></el-icon>
          </div>
          <span class="card-title__text">数据概览</span>
        </div>
      </template>
      <div ref="studentChartRef" class="chart" style="height: 360px"></div>
    </el-card>

    <el-card shadow="never" class="quick-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><Grid /></el-icon>
          </div>
          <span class="card-title__text">快捷入口</span>
        </div>
      </template>
      
      <div class="quick-grid">
        <div class="quick-item" @click="goToCourses">
          <div class="quick-item__icon">
            <el-icon><School /></el-icon>
          </div>
          <span class="quick-item__label">课程中心</span>
        </div>
        <div class="quick-item" @click="goToAssignments">
          <div class="quick-item__icon">
            <el-icon><Ticket /></el-icon>
          </div>
          <span class="quick-item__label">待办作业</span>
        </div>
        <div class="quick-item" @click="goToErrorBook">
          <div class="quick-item__icon">
            <el-icon><Warning /></el-icon>
          </div>
          <span class="quick-item__label">错题本</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useUserStore } from '@/stores/user';
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import * as echarts from 'echarts';
import { School, Ticket, Warning, DocumentChecked, Star, Clock, Histogram, Grid } from '@element-plus/icons-vue';

const userStore = useUserStore();
const router = useRouter();

const studentStats = ref({
  courseCount: 0,
  pendingAssignments: 0,
  gradedAssignments: 0,
  averageScore: 0
});

const studentChartRef = ref(null);
let studentChart = null;

const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour >= 5 && hour < 9) return '早上好';
  if (hour >= 9 && hour < 11) return '上午好';
  if (hour >= 11 && hour < 13) return '中午好';
  if (hour >= 13 && hour < 19) return '下午好';
  return '晚上好';
};

const goToCourses = () => router.push('/student/courses');
const goToAssignments = () => router.push('/student/assignments');
const goToErrorBook = () => router.push('/student/error-book');

const getStudentStats = async () => {
  try {
    const response = await request.get('/student/stats/overview');
    if (response.data) {
      studentStats.value = {
        courseCount: response.data.courseCount || 0,
        pendingAssignments: response.data.pendingAssignments || 0,
        gradedAssignments: response.data.gradedAssignments || 0,
        averageScore: response.data.averageScore || 0
      };
    }
  } catch (error) {
    console.error('获取学生统计数据失败:', error);
  }
};

const initStudentChart = () => {
  if (!studentChartRef.value) return;
  if (studentChart) studentChart.dispose();
  studentChart = echarts.init(studentChartRef.value);
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: ['已选课程', '待提交作业', '已批改作业', '平均得分'] },
    yAxis: { type: 'value' },
    series: [{
      data: [
        studentStats.value.courseCount,
        studentStats.value.pendingAssignments,
        studentStats.value.gradedAssignments,
        studentStats.value.averageScore
      ],
      type: 'bar',
      itemStyle: { color: (params: any) => ['#409eff', '#e6a23c', '#67c23a', '#f56c6c'][params.dataIndex] },
      label: { show: true, position: 'top' }
    }]
  };
  studentChart.setOption(option);
};

const handleResize = () => {
  studentChart?.resize();
};

onMounted(async () => {
  await getStudentStats();
  nextTick(() => {
    initStudentChart();
  });
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  studentChart?.dispose();
});
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
}

.student-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

:deep(.el-card) {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
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

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.welcome-content {
  padding: 20px;
}

.welcome-title {
  font-size: 56px;
  font-weight: 600;
  margin: 20px 0 10px;
  color: #fff;
}

.welcome-subtitle {
  font-size: 28px;
  margin: 10px 0 0;
  opacity: 0.9;
  color: rgba(255, 255, 255, 0.9);
}

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

.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  border: none;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.stat-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  flex-shrink: 0;
}

.stat-card__icon--course {
  background: #eff6ff;
  color: #3b82f6;
}

.stat-card__icon--pending {
  background: #fff7ed;
  color: #f97316;
}

.stat-card__icon--graded {
  background: #f0fdf4;
  color: #22c55e;
}

.stat-card__icon--score {
  background: #fef2f2;
  color: #ef4444;
}

.stat-card__icon .el-icon {
  font-size: 28px;
}

.stat-card__content {
  flex: 1;
}

.stat-card__label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
  font-weight: 500;
}

.stat-card__value {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.2;
  color: #111827;
}

.chart-section {
  margin-bottom: 24px;
}

.chart {
  width: 100%;
  height: 100%;
}

.quick-section {
  margin-bottom: 24px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.quick-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.quick-item__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  margin-bottom: 12px;
  position: relative;
}

.quick-item__icon .el-icon {
  font-size: 28px;
}

.quick-item__label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-grid {
    grid-template-columns: 1fr;
  }
  
  .welcome-title {
    font-size: 36px;
  }
  
  .welcome-subtitle {
    font-size: 18px;
  }
}
</style>
