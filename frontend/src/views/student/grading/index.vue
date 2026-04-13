<template>
  <div class="student-grading">
    <el-card shadow="never" class="filter-card">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <span>成绩分析</span>
          </div>
        </div>
      </template>
      <div class="filter-bar">
        <el-select
          v-model="filterCourse"
          placeholder="选择课程"
          class="filter-item"
          clearable
        >
          <el-option label="全部课程" value="" />
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="course.name"
            :value="course.id"
          />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="filter-item date-picker"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
      </div>
    </el-card>

    <div class="stats-overview">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-label">总作业数</span>
          <span class="stat-value">{{ stats.totalAssignments }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon average">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-label">平均正确率</span>
          <span class="stat-value">{{ stats.averageAccuracy }}%</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon highest">
          <el-icon><Trophy /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-label">最高分作业</span>
          <span class="stat-value highlight">{{ stats.highestScore }}</span>
          <span class="stat-sub">{{ stats.highestAssignment || '-' }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon lowest">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <span class="stat-label">最低分作业</span>
          <span class="stat-value warning">{{ stats.lowestScore }}</span>
          <span class="stat-sub">{{ stats.lowestAssignment || '-' }}</span>
        </div>
      </div>
    </div>

    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-title">
              <el-icon><TrendCharts /></el-icon>
              <span>成绩变化趋势</span>
            </div>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-title">
              <el-icon><Histogram /></el-icon>
              <span>各课程成绩对比</span>
            </div>
          </template>
          <div ref="barChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="chart-title">
          <el-icon><List /></el-icon>
          <span>各课程统计信息</span>
        </div>
      </template>
      <el-table :data="courseStats" stripe style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="assignmentCount" label="作业数" width="100" align="center" />
        <el-table-column prop="averageScore" label="平均分" width="100" align="center">
          <template #default="{ row }">
            <span class="score-average">{{ row.averageScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="highestScore" label="最高分" width="100" align="center">
          <template #default="{ row }">
            <span class="score-high">{{ row.highestScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lowestScore" label="最低分" width="100" align="center">
          <template #default="{ row }">
            <span class="score-low">{{ row.lowestScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="latestScore" label="最近得分" width="100" align="center">
          <template #default="{ row }">
            <span class="score-latest">{{ row.latestScore || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import {
  TrendCharts,
  Document,
  Trophy,
  Warning,
  List,
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import request from '@/utils/request';

const Histogram = TrendCharts;

const filterCourse = ref('');
const dateRange = ref<string[]>([]);
const courses = ref([]);
const grades = ref([]);

const lineChartRef = ref<HTMLElement | null>(null);
const barChartRef = ref<HTMLElement | null>(null);
let lineChart: echarts.ECharts | null = null;
let barChart: echarts.ECharts | null = null;

const stats = computed(() => {
  const data = filteredGrades.value;
  if (data.length === 0) {
    return {
      totalAssignments: 0,
      averageAccuracy: 0,
      highestScore: '-',
      highestAssignment: '',
      lowestScore: '-',
      lowestAssignment: '',
    };
  }

  const percentages = data.map((item: any) => 
    getScorePercentage(item.finalTotalScore, item.totalScore)
  );
  const avgAccuracy = Math.round(
    percentages.reduce((a: number, b: number) => a + b, 0) / percentages.length
  );

  let highest = data[0];
  let lowest = data[0];
  data.forEach((item: any) => {
    if (getScorePercentage(item.finalTotalScore, item.totalScore) > 
        getScorePercentage(highest.finalTotalScore, highest.totalScore)) {
      highest = item;
    }
    if (getScorePercentage(item.finalTotalScore, item.totalScore) < 
        getScorePercentage(lowest.finalTotalScore, lowest.totalScore)) {
      lowest = item;
    }
  });

  return {
    totalAssignments: data.length,
    averageAccuracy: avgAccuracy,
    highestScore: `${highest.finalTotalScore}/${highest.totalScore}`,
    highestAssignment: highest.assignmentTitle,
    lowestScore: `${lowest.finalTotalScore}/${lowest.totalScore}`,
    lowestAssignment: lowest.assignmentTitle,
  };
});

const filteredGrades = computed(() => {
  let result = [...grades.value];

  if (filterCourse.value) {
    result = result.filter(
      (item: any) => item.courseId === Number(filterCourse.value)
    );
  }

  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]);
    const endDate = new Date(dateRange.value[1]);
    endDate.setHours(23, 59, 59, 999);
    result = result.filter((item: any) => {
      const gradeTime = new Date(item.reviewTime);
      return gradeTime >= startDate && gradeTime <= endDate;
    });
  }

  return result.sort((a: any, b: any) => 
    new Date(a.reviewTime).getTime() - new Date(b.reviewTime).getTime()
  );
});

const courseStats = computed(() => {
  const courseMap = new Map<number, any[]>();
  
  filteredGrades.value.forEach((item: any) => {
    if (!courseMap.has(item.courseId)) {
      courseMap.set(item.courseId, []);
    }
    courseMap.get(item.courseId)!.push(item);
  });

  const result: any[] = [];
  courseMap.forEach((items, courseId) => {
    const scores = items.map((item: any) => ({
      score: item.finalTotalScore,
      total: item.totalScore,
      percentage: getScorePercentage(item.finalTotalScore, item.totalScore),
      reviewTime: item.reviewTime,
    }));
    
    const avgScore = Math.round(
      scores.reduce((sum, s) => sum + s.percentage, 0) / scores.length
    );
    const highest = Math.max(...scores.map(s => s.percentage));
    const lowest = Math.min(...scores.map(s => s.percentage));
    
    const sortedByTime = [...scores].sort((a, b) => 
      new Date(b.reviewTime).getTime() - new Date(a.reviewTime).getTime()
    );
    const latest = sortedByTime[0]?.percentage || 0;

    const course = courses.value.find((c: any) => c.id === courseId);
    result.push({
      courseId,
      courseName: course?.name || '未知课程',
      assignmentCount: items.length,
      averageScore: avgScore,
      highestScore: highest,
      lowestScore: lowest,
      latestScore: latest,
    });
  });

  return result;
});

const getCourses = async () => {
  try {
    const response = await request.get('/student/courses');
    courses.value = response.data || [];
  } catch (error) {
    console.error('获取课程列表失败:', error);
    courses.value = [];
  }
};

const getGrades = async () => {
  try {
    const response = await request.get('/student/grading/list', {
      params: { page: 1, size: 100 }
    });
    grades.value = response.data.records || [];
    await nextTick();
    initCharts();
  } catch (error) {
    console.error('获取成绩列表失败:', error);
    grades.value = [];
  }
};

const handleDateChange = () => {
  nextTick(() => {
    updateCharts();
  });
};

const getScorePercentage = (score: number, total: number) => {
  if (!total) return 0;
  return Math.round((score / total) * 100);
};

const getScoreClass = (percentage: number) => {
  if (percentage >= 80) return 'score-high';
  if (percentage >= 60) return 'score-mid';
  return 'score-low';
};

const initCharts = () => {
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value);
  }
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value);
  }
  updateCharts();
};

const updateCharts = () => {
  updateLineChart();
  updateBarChart();
};

const updateLineChart = () => {
  if (!lineChart) return;

  const data = filteredGrades.value;
  const xData = data.map((item: any) => {
    const d = new Date(item.reviewTime);
    return `${d.getMonth() + 1}/${d.getDate()}`;
  });
  const yData = data.map((item: any) => 
    getScorePercentage(item.finalTotalScore, item.totalScore)
  );

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const idx = params[0].dataIndex;
        const item = data[idx];
        return `${item.assignmentTitle}<br/>得分: ${item.finalTotalScore}/${item.totalScore}<br/>正确率: ${params[0].value}%`;
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xData,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { color: '#606266' },
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLine: { show: false },
      axisLabel: { color: '#909399', formatter: '{value}%' },
      splitLine: { lineStyle: { color: '#ebeef5', type: 'dashed' } },
    },
    series: [
      {
        name: '正确率',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#67c23a' },
          ]),
        },
        itemStyle: {
          color: '#409eff',
          borderWidth: 2,
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' },
          ]),
        },
        data: yData,
      },
    ],
  };

  lineChart.setOption(option);
};

const updateBarChart = () => {
  if (!barChart) return;

  const stats = courseStats.value;
  const xData = stats.map((item: any) => item.courseName);
  const avgData = stats.map((item: any) => item.averageScore);
  const highestData = stats.map((item: any) => item.highestScore);
  const latestData = stats.map((item: any) => item.latestScore);

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
    },
    legend: {
      data: ['平均分', '最高分', '最近得分'],
      bottom: 0,
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLine: { lineStyle: { color: '#dcdfe6' } },
      axisLabel: { 
        color: '#606266',
        interval: 0,
        formatter: function(value: string) {
          if (value.length > 10) {
            return value.substring(0, 10) + '...';
          }
          return value;
        },
      },
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLine: { show: false },
      axisLabel: { color: '#909399', formatter: '{value}' },
      splitLine: { lineStyle: { color: '#ebeef5', type: 'dashed' } },
    },
    series: [
      {
        name: '平均分',
        type: 'bar',
        barWidth: '20%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#79bbff' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
        data: avgData,
      },
      {
        name: '最高分',
        type: 'bar',
        barWidth: '20%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#67c23a' },
            { offset: 1, color: '#95d475' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
        data: highestData,
      },
      {
        name: '最近得分',
        type: 'bar',
        barWidth: '20%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#e6a23c' },
            { offset: 1, color: '#eebe77' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
        data: latestData,
      },
    ],
  };

  barChart.setOption(option);
};

const handleResize = () => {
  lineChart?.resize();
  barChart?.resize();
};

watch(filterCourse, () => {
  nextTick(() => {
    updateCharts();
  });
});

onMounted(async () => {
  await getCourses();
  await getGrades();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  lineChart?.dispose();
  barChart?.dispose();
});
</script>

<style scoped>
.student-grading {
  padding: 24px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(4px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

:deep(.el-card) {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: none;
  background: transparent;
}

:deep(.el-card__body) {
  padding: 20px;
}

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
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  color: #fff;
}

.card-header__icon .el-icon {
  font-size: 24px;
}

.filter-card {
  flex-shrink: 0;
}

.filter-bar {
  display: flex;
  gap: 16px;
  align-items: center;
}

.filter-item {
  flex: 1;
  min-width: 0;
}

.date-picker {
  flex: 1;
  min-width: 0;
  width: auto;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 14px;
  flex-shrink: 0;
}

.stat-icon .el-icon {
  font-size: 28px;
}

.stat-icon.total {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  color: #409eff;
}

.stat-icon.average {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67c23a;
}

.stat-icon.highest {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #e6a23c;
}

.stat-icon.lowest {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #f56c6c;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-value.highlight {
  color: #67c23a;
}

.stat-value.warning {
  color: #f56c6c;
}

.stat-sub {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
}

.charts-row {
  flex: 1;
}

.chart-card {
  height: 100%;
  min-height: 380px;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-title .el-icon {
  color: #409eff;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.table-card {
  flex-shrink: 0;
}

.score-high {
  color: #67c23a;
  font-weight: 600;
}

.score-mid {
  color: #e6a23c;
  font-weight: 600;
}

.score-low {
  color: #f56c6c;
  font-weight: 600;
}

.score-average {
  color: #409eff;
  font-weight: 600;
}

.score-latest {
  color: #e6a23c;
  font-weight: 600;
}

:deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: #f5f7fa;
  font-weight: 600;
  color: #303133;
}

:deep(.el-table td) {
  padding: 14px 0;
}

@media (max-width: 768px) {
  .student-grading {
    padding: 16px;
    margin: 12px;
  }

  .filter-bar {
    flex-direction: column;
  }

  .filter-item {
    width: 100%;
  }

  .date-picker {
    width: 100%;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }

  .chart-container {
    height: 250px;
  }
}
</style>
