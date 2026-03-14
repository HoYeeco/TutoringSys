<template>
  <div class="student-grades">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>成绩统计</span>
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
          <div class="stat-value">{{ overallStats.completionRate || 0 }}%</div>
          <div class="stat-label">作业完成率</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Check /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.passedCount || 0 }}/{{ overallStats.totalAssignments || 0 }}</div>
          <div class="stat-label">通过作业数</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Document /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.topRank || 0 }}</div>
          <div class="stat-label">班级排名</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Rank /></el-icon>
        </div>
      </el-card>
    </div>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>课程成绩</span>
        </div>
      </template>
      <el-table :data="courseGrades" style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" width="200" />
        <el-table-column prop="averageScore" label="平均分" width="120">
          <template #default="scope">
            {{ scope.row.averageScore || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="highestScore" label="最高分" width="120">
          <template #default="scope">
            {{ scope.row.highestScore || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="lowestScore" label="最低分" width="120">
          <template #default="scope">
            {{ scope.row.lowestScore || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="completionRate" label="完成率" width="120">
          <template #default="scope">
            {{ scope.row.completionRate || 0 }}%
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" @click="viewCourseDetails(scope.row.courseId)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>最近作业成绩</span>
        </div>
      </template>
      <el-table :data="recentAssignments" style="width: 100%">
        <el-table-column prop="assignmentTitle" label="作业名称" />
        <el-table-column prop="courseName" label="课程" width="150" />
        <el-table-column prop="score" label="得分" width="100">
          <template #default="scope">
            <span :class="getScoreClass(scope.row.score, scope.row.totalScore)">{{ scope.row.score }}/{{ scope.row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" @click="viewReport(scope.row.assignmentId)">
              查看报告
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>成绩趋势</span>
        </div>
      </template>
      <div ref="trendChartRef" class="chart" style="height: 300px"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Star, Check, Document, Rank } from '@element-plus/icons-vue';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';
import * as echarts from 'echarts';

const router = useRouter();
const trendChartRef = ref<HTMLElement | null>(null);
let trendChart: echarts.ECharts | null = null;

const overallStats = ref({
  averageScore: 85,
  completionRate: 90,
  passedCount: 18,
  totalAssignments: 20,
  topRank: 5
});

const courseGrades = ref([
  {
    courseId: 1,
    courseName: '计算机导论',
    averageScore: 88,
    highestScore: 95,
    lowestScore: 75,
    completionRate: 95
  },
  {
    courseId: 2,
    courseName: '数据结构',
    averageScore: 82,
    highestScore: 92,
    lowestScore: 65,
    completionRate: 85
  },
  {
    courseId: 3,
    courseName: '算法设计与分析',
    averageScore: 78,
    highestScore: 90,
    lowestScore: 60,
    completionRate: 80
  }
]);

const recentAssignments = ref([
  {
    assignmentId: 1,
    assignmentTitle: '第一章 计算机基础作业',
    courseName: '计算机导论',
    score: 85,
    totalScore: 100,
    submitTime: '2026-03-10 10:00:00'
  },
  {
    assignmentId: 2,
    assignmentTitle: '第二章 数据结构作业',
    courseName: '数据结构',
    score: 82,
    totalScore: 100,
    submitTime: '2026-03-08 15:30:00'
  },
  {
    assignmentId: 3,
    assignmentTitle: '第三章 算法作业',
    courseName: '算法设计与分析',
    score: 78,
    totalScore: 100,
    submitTime: '2026-03-05 09:15:00'
  }
]);

const getStudentGrades = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get('/student/grades');
    // return response.data;
    
    // 模拟数据
    return {
      overallStats: {
        averageScore: 85,
        completionRate: 90,
        passedCount: 18,
        totalAssignments: 20,
        topRank: 5
      },
      courseGrades: [
        {
          courseId: 1,
          courseName: '计算机导论',
          averageScore: 88,
          highestScore: 95,
          lowestScore: 75,
          completionRate: 95
        },
        {
          courseId: 2,
          courseName: '数据结构',
          averageScore: 82,
          highestScore: 92,
          lowestScore: 65,
          completionRate: 85
        },
        {
          courseId: 3,
          courseName: '算法设计与分析',
          averageScore: 78,
          highestScore: 90,
          lowestScore: 60,
          completionRate: 80
        }
      ],
      recentAssignments: [
        {
          assignmentId: 1,
          assignmentTitle: '第一章 计算机基础作业',
          courseName: '计算机导论',
          score: 85,
          totalScore: 100,
          submitTime: '2026-03-10 10:00:00'
        },
        {
          assignmentId: 2,
          assignmentTitle: '第二章 数据结构作业',
          courseName: '数据结构',
          score: 82,
          totalScore: 100,
          submitTime: '2026-03-08 15:30:00'
        },
        {
          assignmentId: 3,
          assignmentTitle: '第三章 算法作业',
          courseName: '算法设计与分析',
          score: 78,
          totalScore: 100,
          submitTime: '2026-03-05 09:15:00'
        }
      ],
      scoreTrend: [
        { month: '1月', score: 75 },
        { month: '2月', score: 80 },
        { month: '3月', score: 85 }
      ]
    };
  } catch (error) {
    ElMessage.error('获取成绩数据失败');
    return null;
  }
};

const { execute: fetchGrades } = useRequest(getStudentGrades);

const viewCourseDetails = (courseId: number) => {
  router.push(`/student/courses/${courseId}`);
};

const viewReport = (assignmentId: number) => {
  router.push(`/student/reports/${assignmentId}`);
};

const getScoreClass = (score: number, totalScore: number) => {
  const rate = score / totalScore;
  if (rate >= 0.9) return 'score-excellent';
  if (rate >= 0.8) return 'score-good';
  if (rate >= 0.6) return 'score-pass';
  return 'score-fail';
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

const initTrendChart = (scoreTrend: any[]) => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value);
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
        data: scoreTrend.map(item => item.month)
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100
      },
      series: [{
        data: scoreTrend.map(item => item.score),
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
    trendChart.setOption(option);
  }
};

onMounted(() => {
  fetchGrades().then(data => {
    if (data) {
      overallStats.value = data.overallStats;
      courseGrades.value = data.courseGrades;
      recentAssignments.value = data.recentAssignments;
      nextTick(() => {
        initTrendChart(data.scoreTrend);
      });
    }
  });
  
  // 监听窗口大小变化，自适应图表
  window.addEventListener('resize', () => {
    trendChart?.resize();
  });
});
</script>

<style scoped>
.student-grades {
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

.chart {
  width: 100%;
  height: 100%;
}

.score-excellent {
  color: #67c23a;
  font-weight: 500;
}

.score-good {
  color: #409eff;
  font-weight: 500;
}

.score-pass {
  color: #e6a23c;
  font-weight: 500;
}

.score-fail {
  color: #f56c6c;
  font-weight: 500;
}

@media (max-width: 768px) {
  .stats-container {
    grid-template-columns: 1fr 1fr;
  }
}
</style>