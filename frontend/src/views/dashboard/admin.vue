<template>
  <div class="dashboard admin-dashboard">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1 class="welcome-title">{{ getGreeting() }}，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h1>
          <p class="welcome-subtitle">感谢你维持系统有序高效运转。</p>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="stats-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <span class="card-title__text">数据概览</span>
        </div>
      </template>
      
      <div class="stats-grid">
        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--student">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ adminStats.studentCount || 0 }}</div>
            <div class="stat-card__label">学生总数</div>
          </div>
        </el-card>

        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--teacher">
            <el-icon><Avatar /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ adminStats.teacherCount || 0 }}</div>
            <div class="stat-card__label">教师总数</div>
          </div>
        </el-card>

        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--course">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ adminStats.courseCount || 0 }}</div>
            <div class="stat-card__label">课程总数</div>
          </div>
        </el-card>

        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--assignment">
            <el-icon><DocumentChecked /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__value">{{ adminStats.assignmentCount || 0 }}</div>
            <div class="stat-card__label">作业总数</div>
          </div>
        </el-card>
      </div>
    </el-card>

    <div class="charts-container">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span>数据统计</span>
        </template>
        <div ref="adminBarChartRef" class="chart" style="height: 300px"></div>
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header>
          <span>用户类型占比</span>
        </template>
        <div ref="userTypeChartRef" class="chart" style="height: 300px"></div>
      </el-card>
    </div>

    <div class="system-monitoring">
      <h2 class="section-title">系统健康监控</h2>
      <div class="monitoring-cards">
        <el-card shadow="never" class="monitoring-card llm-card">
          <template #header>
            <span>大模型监控</span>
          </template>
          <div class="llm-stats">
            <div class="llm-stat-item">
              <div class="stat-label">Token 消耗</div>
              <div class="stat-value">{{ llmStats.tokenConsumption }}</div>
            </div>
            <div class="llm-stat-item">
              <div class="stat-label">调用失败记录</div>
              <div class="stat-value">{{ llmStats.failureCount }}</div>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="monitoring-card redis-card">
          <template #header>
            <span>Redis 监控</span>
          </template>
          <div class="redis-stats">
            <div class="redis-stat-item">
              <div class="stat-label">缓存命中率</div>
              <div class="stat-value">{{ redisStats.hitRate }}%</div>
            </div>
            <div class="redis-stat-item">
              <div class="stat-label">内存使用</div>
              <div class="stat-value">{{ redisStats.memoryUsage }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <span>最近操作日志</span>
      </template>
      <el-table :data="recentLogs" style="width: 100%">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="user" label="用户" width="150" />
        <el-table-column prop="action" label="操作" />
        <el-table-column prop="result" label="结果" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.result === '成功' ? 'success' : 'danger'">
              {{ scope.row.result }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useUserStore } from '@/stores/user';
import request from '@/utils/request';
import * as echarts from 'echarts';
import { DataAnalysis, Reading, Avatar, OfficeBuilding, DocumentChecked } from '@element-plus/icons-vue';

const userStore = useUserStore();

const adminStats = ref({
  studentCount: 0,
  teacherCount: 0,
  courseCount: 0,
  assignmentCount: 0
});

const recentLogs = ref([]);

const llmStats = ref({ tokenConsumption: 0, failureCount: 0 });
const redisStats = ref({ hitRate: 0, memoryUsage: '' });

const adminBarChartRef = ref(null);
const userTypeChartRef = ref(null);

let adminBarChart = null;
let userTypeChart = null;

const getGreeting = () => {
  const hour = new Date().getHours();
  if (hour >= 5 && hour < 9) return '早上好';
  if (hour >= 9 && hour < 11) return '上午好';
  if (hour >= 11 && hour < 13) return '中午好';
  if (hour >= 13 && hour < 19) return '下午好';
  return '晚上好';
};

const getAdminStats = async () => {
  try {
    const [studentsRes, teachersRes, coursesRes] = await Promise.all([
      request.get('/admin/users', { params: { role: 'STUDENT', size: 1 } }).catch(() => ({ data: { total: 0 } })),
      request.get('/admin/users', { params: { role: 'TEACHER', size: 1 } }).catch(() => ({ data: { total: 0 } })),
      request.get('/admin/courses', { params: { size: 100 } }).catch(() => ({ data: { records: [], total: 0 } }))
    ]);
    
    const coursesData = coursesRes.data || { records: [], total: 0 };
    const totalAssignments = (coursesData.records || []).reduce(
      (sum: number, course: any) => sum + (course.assignmentCount || 0), 0
    );
    
    adminStats.value = {
      studentCount: studentsRes.data?.total || 0,
      teacherCount: teachersRes.data?.total || 0,
      courseCount: coursesData.total || 0,
      assignmentCount: totalAssignments
    };
  } catch (error) {
    console.error('获取管理员统计数据失败:', error);
  }
};

const getMonitoringData = async () => {
  try {
    const [llmRes, redisRes] = await Promise.all([
      request.get('/admin/monitor/llm').catch(() => ({ data: { totalTokens: 0, failedCalls: 0 } })),
      request.get('/admin/monitor/redis').catch(() => ({ data: { hitRate: 0, usedMemoryHuman: '' } }))
    ]);
    llmStats.value = {
      tokenConsumption: llmRes.data?.totalTokens || 0,
      failureCount: llmRes.data?.failedCalls || 0
    };
    redisStats.value = {
      hitRate: redisRes.data?.hitRate || 0,
      memoryUsage: redisRes.data?.usedMemoryHuman || ''
    };
  } catch (error) {
    console.error('获取监控数据失败:', error);
  }
};

const getRecentLogs = async () => {
  try {
    const response = await request.get('/admin/monitor/audit-logs', { params: { size: 5 } });
    const logsData = response.data || { records: [] };
    recentLogs.value = (logsData.records || []).map((log: any) => ({
      time: log.operationTime || '-',
      user: log.operator || '-',
      action: log.operationType || '-',
      result: log.success === 1 ? '成功' : '失败'
    }));
  } catch (error) {
    console.error('获取操作日志失败:', error);
    recentLogs.value = [];
  }
};

const initAdminCharts = () => {
  if (adminBarChartRef.value) {
    if (adminBarChart) adminBarChart.dispose();
    adminBarChart = echarts.init(adminBarChartRef.value);
    adminBarChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: ['学生', '教师', '课程', '作业'] },
      yAxis: { type: 'value' },
      series: [{
        data: [adminStats.value.studentCount, adminStats.value.teacherCount, adminStats.value.courseCount, adminStats.value.assignmentCount],
        type: 'bar',
        itemStyle: { color: (params: any) => ['#409eff', '#67c23a', '#e6a23c', '#f56c6c'][params.dataIndex] },
        label: { show: true, position: 'top' }
      }]
    });
  }

  if (userTypeChartRef.value) {
    if (userTypeChart) userTypeChart.dispose();
    userTypeChart = echarts.init(userTypeChartRef.value);
    userTypeChart.setOption({
      tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
      legend: { top: '5%', left: 'center' },
      series: [{
        name: '用户类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: '18', fontWeight: 'bold' } },
        labelLine: { show: false },
        data: [
          { value: adminStats.value.studentCount, name: '学生' },
          { value: adminStats.value.teacherCount, name: '教师' }
        ],
        color: ['#409eff', '#67c23a']
      }]
    });
  }
};

const handleResize = () => {
  adminBarChart?.resize();
  userTypeChart?.resize();
};

onMounted(async () => {
  await getAdminStats();
  await getMonitoringData();
  await getRecentLogs();
  nextTick(() => {
    initAdminCharts();
  });
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  adminBarChart?.dispose();
  userTypeChart?.dispose();
});
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(4px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
}

.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.admin-dashboard .welcome-card {
  margin-bottom: 0;
}

.admin-dashboard .stats-section {
  margin-bottom: 0;
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
  margin-top: 10px;
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

.stat-card__icon--student {
  background: #f3e8ff;
  color: #a855f7;
}

.stat-card__icon--teacher {
  background: #dbeafe;
  color: #3b82f6;
}

.stat-card__icon--course {
  background: #dcfce7;
  color: #22c55e;
}

.stat-card__icon--assignment {
  background: #ffedd5;
  color: #f97316;
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

.charts-container {
  display: grid;
  grid-template-columns: 7fr 3fr;
  gap: 24px;
  margin-bottom: 24px;
}

.chart-card {
  border: none;
  border-radius: 16px;
  overflow: hidden;
}

.chart {
  width: 100%;
  height: 100%;
}

.system-monitoring {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}

.monitoring-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.monitoring-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

:deep(.monitoring-card .el-card__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 16px 20px;
  border-bottom: none;
  overflow: hidden;
}

:deep(.monitoring-card .el-card__header span) {
  color: #fff;
  font-weight: 600;
  font-size: 16px;
}

:deep(.monitoring-card .el-card__body) {
  padding: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.llm-stats,
.redis-stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 0;
}

.llm-stat-item,
.redis-stat-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  flex: 1;
  padding: 20px;
  height: 100%;
}

.llm-stat-item:not(:last-child),
.redis-stat-item:not(:last-child) {
  border-right: 1px solid #e4e7ed;
}

.stat-label {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
  font-weight: 500;
  width: 100%;
}

.stat-value {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
  width: 100%;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .welcome-title {
    font-size: 36px;
  }
  
  .welcome-subtitle {
    font-size: 18px;
  }
  
  .charts-container {
    grid-template-columns: 1fr;
  }
  
  .monitoring-cards {
    grid-template-columns: 1fr;
  }
  
  .llm-stats,
  .redis-stats {
    flex-direction: column;
    gap: 15px;
  }
  
  .llm-stat-item:not(:last-child),
  .redis-stat-item:not(:last-child) {
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
    padding-bottom: 15px;
  }
}
</style>
