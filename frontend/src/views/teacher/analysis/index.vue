<template>
  <div class="teacher-analysis">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>学情分析</span>
          <el-select v-model="selectedCourse" placeholder="选择课程">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </div>
      </template>
    </el-card>

    <div class="charts-container">
      <el-card shadow="never" class="chart-card">
        <template #header>
          <span>作业完成率</span>
        </template>
        <div ref="completionChartRef" class="chart" style="height: 300px"></div>
      </el-card>

      <el-card shadow="never" class="chart-card">
        <template #header>
          <span>平均分趋势</span>
        </template>
        <div ref="scoreChartRef" class="chart" style="height: 300px"></div>
      </el-card>
    </div>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <span>高频错题</span>
      </template>
      <el-table :data="errorQuestions" style="width: 100%">
        <el-table-column prop="questionId" label="题目ID" width="100" />
        <el-table-column prop="questionContent" label="题目内容" />
        <el-table-column prop="errorCount" label="错误次数" width="120" />
        <el-table-column prop="errorRate" label="错误率" width="120">
          <template #default="scope">
            {{ scope.row.errorRate }}%
          </template>
        </el-table-column>
        <el-table-column prop="errorType" label="错误类型" width="120">
          <template #default="scope">
            <el-tag
              :type="getErrorTypeColor(scope.row.errorType)"
            >
              {{ scope.row.errorType }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';
import * as echarts from 'echarts';

const selectedCourse = ref(1);
const courses = ref([
  { id: 1, name: '计算机导论' },
  { id: 2, name: '数据结构' },
  { id: 3, name: '算法设计与分析' }
]);

const completionChartRef = ref<HTMLElement | null>(null);
const scoreChartRef = ref<HTMLElement | null>(null);
let completionChart: echarts.ECharts | null = null;
let scoreChart: echarts.ECharts | null = null;

const errorQuestions = ref([
  {
    questionId: 1,
    questionContent: '计算机的基本组成部分不包括以下哪项？',
    errorCount: 5,
    errorRate: 62.5,
    errorType: '概念错误'
  },
  {
    questionId: 2,
    questionContent: '以下哪些是计算机的输入设备？',
    errorCount: 3,
    errorRate: 37.5,
    errorType: '概念错误'
  },
  {
    questionId: 4,
    questionContent: '请简述计算机的发展历程。',
    errorCount: 2,
    errorRate: 25,
    errorType: '逻辑错误'
  }
]);

const getCourseStats = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get(`/teacher/courses/${selectedCourse.value}/stats`);
    // return response.data;
    
    // 模拟数据
    return {
      completionRate: 87.5,
      assignments: [
        {
          id: 1,
          title: '第一章 计算机基础作业',
          averageScore: 85,
          highestScore: 95,
          lowestScore: 70,
          passRate: 90
        },
        {
          id: 2,
          title: '第二章 数据结构作业',
          averageScore: 82,
          highestScore: 92,
          lowestScore: 65,
          passRate: 85
        },
        {
          id: 3,
          title: '第三章 算法作业',
          averageScore: 78,
          highestScore: 90,
          lowestScore: 60,
          passRate: 80
        }
      ],
      errorQuestions: [
        {
          questionId: 1,
          questionContent: '计算机的基本组成部分不包括以下哪项？',
          errorCount: 5,
          errorRate: 62.5,
          errorType: '概念错误'
        },
        {
          questionId: 2,
          questionContent: '以下哪些是计算机的输入设备？',
          errorCount: 3,
          errorRate: 37.5,
          errorType: '概念错误'
        },
        {
          questionId: 4,
          questionContent: '请简述计算机的发展历程。',
          errorCount: 2,
          errorRate: 25,
          errorType: '逻辑错误'
        }
      ]
    };
  } catch (error) {
    ElMessage.error('获取学情数据失败');
    return null;
  }
};

const { execute: fetchCourseStats } = useRequest(getCourseStats);

const initCompletionChart = (completionRate: number) => {
  if (completionChartRef.value) {
    completionChart = echarts.init(completionChartRef.value);
    const option = {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        top: '5%',
        left: 'center'
      },
      series: [
        {
          name: '完成率',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { value: completionRate, name: '已完成' },
            { value: 100 - completionRate, name: '未完成' }
          ],
          color: ['#67c23a', '#f56c6c']
        }
      ]
    };
    completionChart.setOption(option);
  }
};

const initScoreChart = (assignments: any[]) => {
  if (scoreChartRef.value) {
    scoreChart = echarts.init(scoreChartRef.value);
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
        data: assignments.map(a => a.title)
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100
      },
      series: [
        {
          name: '平均分',
          type: 'bar',
          data: assignments.map(a => a.averageScore),
          itemStyle: {
            color: '#409eff'
          }
        }
      ]
    };
    scoreChart.setOption(option);
  }
};

const getErrorTypeColor = (errorType: string) => {
  const colorMap: Record<string, string> = {
    '概念错误': 'danger',
    '逻辑错误': 'warning',
    '格式错误': 'primary'
  };
  return colorMap[errorType] || 'info';
};

const loadStats = () => {
  fetchCourseStats().then(data => {
    if (data) {
      nextTick(() => {
        initCompletionChart(data.completionRate);
        initScoreChart(data.assignments);
        errorQuestions.value = data.errorQuestions;
      });
    }
  });
};

watch(selectedCourse, () => {
  loadStats();
});

onMounted(() => {
  loadStats();
  
  // 监听窗口大小变化，自适应图表
  window.addEventListener('resize', () => {
    completionChart?.resize();
    scoreChart?.resize();
  });
});
</script>

<style scoped>
.teacher-analysis {
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

.charts-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  height: 350px;
}

.chart {
  width: 100%;
  height: 100%;
}

@media (max-width: 768px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
}
</style>