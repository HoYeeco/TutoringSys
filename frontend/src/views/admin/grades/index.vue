<template>
  <div class="admin-grades">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>成绩管理</span>
          <div class="header-actions">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="handleDateChange"
            />
            <el-button type="primary" @click="exportAllData">导出所有数据</el-button>
          </div>
        </div>
      </template>
    </el-card>

    <div class="stats-container">
      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ systemStats.totalSubmissions || 0 }}</div>
          <div class="stat-label">总提交次数</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Document /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ systemStats.averageScore || 0 }}</div>
          <div class="stat-label">系统平均分</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Star /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ systemStats.completionRate || 0 }}%</div>
          <div class="stat-label">整体完成率</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Check /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ systemStats.llmCalls || 0 }}</div>
          <div class="stat-label">LLM调用次数</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Cpu /></el-icon>
        </div>
      </el-card>
    </div>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>课程成绩概览</span>
        </div>
      </template>
      <el-table :data="courseOverview" style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" width="200" />
        <el-table-column prop="studentCount" label="学生人数" width="120" />
        <el-table-column prop="averageScore" label="平均分" width="120" />
        <el-table-column prop="passRate" label="通过率" width="120">
          <template #default="scope">
            {{ scope.row.passRate || 0 }}%
          </template>
        </el-table-column>
        <el-table-column prop="submissionCount" label="提交次数" width="120" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewCourseDetails(scope.row.courseId)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div class="charts-container">
      <el-card shadow="never" class="mt-4">
        <template #header>
          <div class="card-header">
            <span>提交趋势</span>
          </div>
        </template>
        <div ref="submissionChartRef" class="chart" style="height: 300px"></div>
      </el-card>

      <el-card shadow="never" class="mt-4">
        <template #header>
          <div class="card-header">
            <span>成绩分布</span>
          </div>
        </template>
        <div ref="scoreChartRef" class="chart" style="height: 300px"></div>
      </el-card>
    </div>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>系统性能指标</span>
        </div>
      </template>
      <el-table :data="performanceMetrics" style="width: 100%">
        <el-table-column prop="metric" label="指标名称" width="200" />
        <el-table-column prop="value" label="当前值" width="150" />
        <el-table-column prop="unit" label="单位" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.updatedAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Document, Star, Check, Cpu } from '@element-plus/icons-vue';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';
import * as echarts from 'echarts';

const router = useRouter();
const submissionChartRef = ref<HTMLElement | null>(null);
const scoreChartRef = ref<HTMLElement | null>(null);
let submissionChart: echarts.ECharts | null = null;
let scoreChart: echarts.ECharts | null = null;

const dateRange = ref<[Date, Date] | null>(null);

const systemStats = ref({
  totalSubmissions: 1250,
  averageScore: 82.5,
  completionRate: 88,
  llmCalls: 890
});

const courseOverview = ref([
  { courseId: 1, courseName: '计算机导论', studentCount: 50, averageScore: 85, passRate: 90, submissionCount: 450 },
  { courseId: 2, courseName: '数据结构', studentCount: 45, averageScore: 80, passRate: 85, submissionCount: 380 },
  { courseId: 3, courseName: '算法设计与分析', studentCount: 40, averageScore: 78, passRate: 80, submissionCount: 320 },
  { courseId: 4, courseName: '数据库原理', studentCount: 55, averageScore: 83, passRate: 88, submissionCount: 420 }
]);

const performanceMetrics = ref([
  { metric: 'API响应时间', value: 120, unit: 'ms', status: '正常', updatedAt: '2026-03-10 10:00:00' },
  { metric: 'LLM调用响应时间', value: 2500, unit: 'ms', status: '正常', updatedAt: '2026-03-10 10:00:00' },
  { metric: '系统内存使用率', value: 65, unit: '%', status: '正常', updatedAt: '2026-03-10 10:00:00' },
  { metric: 'CPU使用率', value: 45, unit: '%', status: '正常', updatedAt: '2026-03-10 10:00:00' },
  { metric: '数据库连接数', value: 25, unit: '个', status: '正常', updatedAt: '2026-03-10 10:00:00' }
]);

const getAdminGrades = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get('/admin/grades', { params: { dateRange: dateRange.value } });
    // return response.data;
    
    // 模拟数据
    return {
      systemStats: {
        totalSubmissions: 1250,
        averageScore: 82.5,
        completionRate: 88,
        llmCalls: 890
      },
      courseOverview: [
        { courseId: 1, courseName: '计算机导论', studentCount: 50, averageScore: 85, passRate: 90, submissionCount: 450 },
        { courseId: 2, courseName: '数据结构', studentCount: 45, averageScore: 80, passRate: 85, submissionCount: 380 },
        { courseId: 3, courseName: '算法设计与分析', studentCount: 40, averageScore: 78, passRate: 80, submissionCount: 320 },
        { courseId: 4, courseName: '数据库原理', studentCount: 55, averageScore: 83, passRate: 88, submissionCount: 420 }
      ],
      performanceMetrics: [
        { metric: 'API响应时间', value: 120, unit: 'ms', status: '正常', updatedAt: '2026-03-10 10:00:00' },
        { metric: 'LLM调用响应时间', value: 2500, unit: 'ms', status: '正常', updatedAt: '2026-03-10 10:00:00' },
        { metric: '系统内存使用率', value: 65, unit: '%', status: '正常', updatedAt: '2026-03-10 10:00:00' },
        { metric: 'CPU使用率', value: 45, unit: '%', status: '正常', updatedAt: '2026-03-10 10:00:00' },
        { metric: '数据库连接数', value: 25, unit: '个', status: '正常', updatedAt: '2026-03-10 10:00:00' }
      ],
      submissionTrend: [
        { date: '1月', count: 320 },
        { date: '2月', count: 450 },
        { date: '3月', count: 480 }
      ],
      scoreDistribution: [
        { range: '90-100', count: 320 },
        { range: '80-89', count: 450 },
        { range: '70-79', count: 300 },
        { range: '60-69', count: 120 },
        { range: '0-59', count: 60 }
      ]
    };
  } catch (error) {
    ElMessage.error('获取成绩数据失败');
    return null;
  }
};

const { execute: fetchGrades } = useRequest(getAdminGrades);

const handleDateChange = () => {
  fetchGrades();
};

const exportAllData = () => {
  ElMessage.success('数据导出成功');
};

const viewCourseDetails = (courseId: number) => {
  router.push(`/admin/courses/${courseId}`);
};

const getStatusType = (status: string) => {
  switch (status) {
    case '正常':
      return 'success';
    case '警告':
      return 'warning';
    case '异常':
      return 'danger';
    default:
      return 'info';
  }
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

const initSubmissionChart = (submissionTrend: any[]) => {
  if (submissionChartRef.value) {
    submissionChart = echarts.init(submissionChartRef.value);
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: submissionTrend.map(item => item.date)
      },
      yAxis: {
        type: 'value',
        min: 0
      },
      series: [{
        data: submissionTrend.map(item => item.count),
        type: 'line',
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      }]
    };
    submissionChart.setOption(option);
  }
};

const initScoreChart = (scoreDistribution: any[]) => {
  if (scoreChartRef.value) {
    scoreChart = echarts.init(scoreChartRef.value);
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 10,
        data: scoreDistribution.map(item => item.range)
      },
      series: [{
        name: '成绩分布',
        type: 'pie',
        radius: '60%',
        center: ['50%', '50%'],
        data: scoreDistribution.map(item => ({ name: item.range, value: item.count })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    };
    scoreChart.setOption(option);
  }
};

onMounted(() => {
  fetchGrades().then(data => {
    if (data) {
      systemStats.value = data.systemStats;
      courseOverview.value = data.courseOverview;
      performanceMetrics.value = data.performanceMetrics;
      nextTick(() => {
        initSubmissionChart(data.submissionTrend);
        initScoreChart(data.scoreDistribution);
      });
    }
  });
  
  // 监听窗口大小变化，自适应图表
  window.addEventListener('resize', () => {
    submissionChart?.resize();
    scoreChart?.resize();
  });
});
</script>

<style scoped>
.admin-grades {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  height: 120px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-icon {
  margin-left: 20px;
  color: #409eff;
}

.icon-large {
  font-size: 48px;
  opacity: 0.6;
}

.charts-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart {
  width: 100%;
  height: 100%;
}

@media (max-width: 1200px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-container {
    grid-template-columns: 1fr 1fr;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
    flex-direction: column;
  }
  
  .el-date-picker {
    width: 100%;
  }
}
</style>