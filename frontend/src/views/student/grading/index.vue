<template>
  <div class="student-grading">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>成绩批改</span>
        </div>
      </template>
    </el-card>

    <div class="stats-container">
      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.averageScore || 0 }}</div>
          <div class="stat-label">平均成绩</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Star /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.gradedCount || 0 }}</div>
          <div class="stat-label">已批改作业</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Check /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.accuracy || 0 }}%</div>
          <div class="stat-label">平均正确率</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><TrendCharts /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.errorCount || 0 }}</div>
          <div class="stat-label">错题总数</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Warning /></el-icon>
        </div>
      </el-card>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>个人成绩趋势</span>
            </div>
          </template>
          <div ref="scoreTrendChartRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>各课程成绩对比</span>
            </div>
          </template>
          <div ref="courseCompareChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>已提交作业</span>
          <span class="total-count">共 {{ filteredAssignments.length }} 份作业</span>
        </div>
      </template>
      
      <div class="filter-bar">
        <div class="filter-row">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索作业名称或课程名称"
            clearable
            class="filter-item search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="filterCourse" placeholder="按课程筛选" class="filter-item" clearable>
            <el-option label="全部课程" value="" />
            <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
          </el-select>
        </div>
        <div class="sort-row">
          <span class="sort-label">排序：</span>
          <el-select v-model="sortBy" placeholder="排序方式" class="sort-item">
            <el-option label="提交时间（最新）" value="submitTime:desc" />
            <el-option label="提交时间（最早）" value="submitTime:asc" />
            <el-option label="分数（从高到低）" value="score:desc" />
            <el-option label="分数（从低到低）" value="score:asc" />
            <el-option label="错题数量（从多到少）" value="errorCount:desc" />
            <el-option label="错题数量（从少到多）" value="errorCount:asc" />
            <el-option label="发布时间（最新）" value="createTime:desc" />
            <el-option label="截止时间（最近）" value="deadline:asc" />
          </el-select>
        </div>
      </div>

      <el-empty v-if="filteredAssignments.length === 0" description="暂无已批改作业" />
      <div v-else class="assignment-grid">
        <div
          v-for="assignment in filteredAssignments"
          :key="assignment.id"
          class="assignment-card"
          @click="goToDetail(assignment)"
        >
          <div class="card-header-section">
            <div class="assignment-title">{{ assignment.title }}</div>
            <el-tag :type="getScoreTagType(assignment.score, assignment.totalScore)" effect="dark">
              {{ assignment.score }}/{{ assignment.totalScore }}
            </el-tag>
          </div>
          
          <div class="card-body">
            <div class="info-row">
              <el-icon><School /></el-icon>
              <span>{{ assignment.courseName }}</span>
            </div>
            <div class="info-row">
              <el-icon><User /></el-icon>
              <span>{{ assignment.teacherName }}</span>
            </div>
            <div class="info-row">
              <el-icon><TrendCharts /></el-icon>
              <span>正确率：{{ assignment.accuracy }}%</span>
            </div>
            <div class="info-row">
              <el-icon><Clock /></el-icon>
              <span>批改时间：{{ formatDate(assignment.gradingTime) }}</span>
            </div>
          </div>

          <div class="card-footer">
            <div class="grading-type">
              <el-tag :type="assignment.gradingType === 'AI' ? 'primary' : 'success'" size="small">
                {{ assignment.gradingType === 'AI' ? '智能批改' : '人工复核' }}
              </el-tag>
            </div>
            <div class="error-count" v-if="assignment.errorCount > 0">
              <el-icon><Warning /></el-icon>
              <span>{{ assignment.errorCount }} 道错题</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { 
  Star, Check, TrendCharts, Warning, Search, School, User, Clock 
} from '@element-plus/icons-vue';
import request from '@/utils/request';
import * as echarts from 'echarts';

const router = useRouter();

const scoreTrendChartRef = ref<HTMLElement | null>(null);
const courseCompareChartRef = ref<HTMLElement | null>(null);
let scoreTrendChart: echarts.ECharts | null = null;
let courseCompareChart: echarts.ECharts | null = null;

const searchKeyword = ref('');
const filterCourse = ref('');
const sortBy = ref('submitTime:desc');

const courses = ref<any[]>([]);
const assignments = ref<any[]>([]);

const overallStats = ref({
  averageScore: 0,
  gradedCount: 0,
  accuracy: 0,
  errorCount: 0
});

const filteredAssignments = computed(() => {
  let result = [...assignments.value];
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(item => 
      item.title.toLowerCase().includes(keyword) ||
      item.courseName.toLowerCase().includes(keyword)
    );
  }
  
  if (filterCourse.value) {
    result = result.filter(item => item.courseId === Number(filterCourse.value));
  }
  
  if (sortBy.value) {
    const [field, order] = sortBy.value.split(':');
    result.sort((a, b) => {
      let aValue = a[field];
      let bValue = b[field];
      
      if (field === 'submitTime' || field === 'gradingTime' || field === 'createTime' || field === 'deadline') {
        aValue = new Date(aValue || 0).getTime();
        bValue = new Date(bValue || 0).getTime();
      }
      
      if (order === 'asc') {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });
  }
  
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

const getGradingList = async () => {
  try {
    const response = await request.get('/student/grading/list', {
      params: {
        page: 1,
        size: 100
      }
    });
    const data = response.data || {};
    assignments.value = data.records || [];
    overallStats.value = data.stats || overallStats.value;
    
    if (data.scoreTrend) {
      nextTick(() => initScoreTrendChart(data.scoreTrend));
    }
    if (data.courseCompare) {
      nextTick(() => initCourseCompareChart(data.courseCompare));
    }
  } catch (error) {
    console.error('获取批改列表失败:', error);
    ElMessage.error('获取批改列表失败');
    assignments.value = [];
  }
};

const initScoreTrendChart = (data: any[]) => {
  if (scoreTrendChartRef.value) {
    scoreTrendChart = echarts.init(scoreTrendChartRef.value);
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.label)
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        name: '分数'
      },
      series: [{
        data: data.map(item => item.score),
        type: 'line',
        smooth: true,
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        },
        markLine: {
          data: [{ type: 'average', name: '平均值' }],
          lineStyle: { color: '#67c23a' }
        }
      }]
    };
    scoreTrendChart.setOption(option);
  }
};

const initCourseCompareChart = (data: any[]) => {
  if (courseCompareChartRef.value) {
    courseCompareChart = echarts.init(courseCompareChartRef.value);
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.courseName),
        axisLabel: {
          interval: 0,
          rotate: 30
        }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        name: '平均分'
      },
      series: [{
        data: data.map(item => ({
          value: item.averageScore,
          itemStyle: {
            color: item.averageScore >= 80 ? '#67c23a' : 
                   item.averageScore >= 60 ? '#e6a23c' : '#f56c6c'
          }
        })),
        type: 'bar',
        barWidth: '50%',
        label: {
          show: true,
          position: 'top'
        }
      }]
    };
    courseCompareChart.setOption(option);
  }
};

const getScoreTagType = (score: number, total: number) => {
  const rate = score / total;
  if (rate >= 0.9) return 'success';
  if (rate >= 0.8) return 'primary';
  if (rate >= 0.6) return 'warning';
  return 'danger';
};

const formatDate = (date: string) => {
  if (!date) return '-';
  const d = new Date(date);
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`;
};

const goToDetail = (assignment: any) => {
  router.push(`/student/grading/${assignment.id}`);
};

const handleResize = () => {
  scoreTrendChart?.resize();
  courseCompareChart?.resize();
};

onMounted(() => {
  getCourses();
  getGradingList();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  scoreTrendChart?.dispose();
  courseCompareChart?.dispose();
});
</script>

<style scoped>
.student-grading {
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

.total-count {
  font-size: 14px;
  color: #909399;
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
  font-size: 28px;
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

.chart-card {
  margin-bottom: 20px;
}

.chart {
  width: 100%;
  height: 300px;
}

.filter-bar {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border);
}

.filter-row {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  margin-bottom: 15px;
}

.filter-item {
  min-width: 150px;
}

.search-input {
  flex: 1;
  min-width: 250px;
  max-width: 400px;
}

.sort-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-label {
  font-size: 14px;
  color: var(--color-text);
}

.sort-item {
  min-width: 180px;
}

.assignment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.assignment-card {
  padding: 20px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background-color: var(--color-card);
  cursor: pointer;
  transition: all 0.3s ease;
}

.assignment-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.assignment-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  flex: 1;
  margin-right: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-body {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.info-row:last-child {
  margin-bottom: 0;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.error-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f56c6c;
  font-size: 13px;
}

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
  }
  
  .filter-item {
    width: 100%;
  }
  
  .search-input {
    width: 100%;
    max-width: unset;
  }
  
  .sort-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .sort-item {
    width: 100%;
  }
  
  .assignment-grid {
    grid-template-columns: 1fr;
  }
}
</style>
