<template>
  <div class="teacher-grades">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>学情分析</span>
          <div class="header-actions">
            <el-select v-model="selectedCourseId" placeholder="选择课程" @change="handleCourseChange">
              <el-option
                v-for="course in courses"
                :key="course.courseId"
                :label="course.courseName"
                :value="course.courseId"
              />
            </el-select>
            <el-button type="primary" @click="exportData">导出数据</el-button>
          </div>
        </div>
      </template>
    </el-card>

    <div class="stats-container">
      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ classStats.averageScore || 0 }}</div>
          <div class="stat-label">班级平均分</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Star /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ classStats.passRate || 0 }}%</div>
          <div class="stat-label">通过率</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Check /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ classStats.highestScore || 0 }}</div>
          <div class="stat-label">最高分</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Top /></el-icon>
        </div>
      </el-card>

      <el-card shadow="never" class="stat-card">
        <div class="stat-content">
          <div class="stat-value">{{ classStats.lowestScore || 0 }}</div>
          <div class="stat-label">最低分</div>
        </div>
        <div class="stat-icon">
          <el-icon class="icon-large"><Bottom /></el-icon>
        </div>
      </el-card>
    </div>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>学生成绩列表</span>
        </div>
      </template>
      <el-table :data="studentGrades" style="width: 100%">
        <el-table-column prop="studentName" label="学生姓名" width="150" />
        <el-table-column prop="averageScore" label="平均分" width="120" />
        <el-table-column prop="highestScore" label="最高分" width="120" />
        <el-table-column prop="lowestScore" label="最低分" width="120" />
        <el-table-column prop="completionRate" label="完成率" width="120">
          <template #default="scope">
            {{ scope.row.completionRate || 0 }}%
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewStudentDetails(scope.row.studentId)">
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
            <span>成绩分布</span>
          </div>
        </template>
        <div ref="distributionChartRef" class="chart" style="height: 300px"></div>
      </el-card>

      <el-card shadow="never" class="mt-4">
        <template #header>
          <div class="card-header">
            <span>知识点掌握情况</span>
          </div>
        </template>
        <div ref="knowledgeChartRef" class="chart" style="height: 300px"></div>
      </el-card>
    </div>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>高频错误题目</span>
        </div>
      </template>
      <el-table :data="errorQuestions" style="width: 100%">
        <el-table-column prop="questionId" label="题目ID" width="100" />
        <el-table-column prop="questionContent" label="题目内容" />
        <el-table-column prop="errorCount" label="错误次数" width="120" />
        <el-table-column prop="errorRate" label="错误率" width="120">
          <template #default="scope">
            {{ scope.row.errorRate || 0 }}%
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button size="small" @click="viewQuestionDetails(scope.row.questionId)">
              查看详情
            </el-button>
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
import { Star, Check, Top, Bottom } from '@element-plus/icons-vue';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';
import * as echarts from 'echarts';

const router = useRouter();
const distributionChartRef = ref<HTMLElement | null>(null);
const knowledgeChartRef = ref<HTMLElement | null>(null);
let distributionChart: echarts.ECharts | null = null;
let knowledgeChart: echarts.ECharts | null = null;

const selectedCourseId = ref<number | null>(null);
const courses = ref([
  { courseId: 1, courseName: '计算机导论' },
  { courseId: 2, courseName: '数据结构' },
  { courseId: 3, courseName: '算法设计与分析' }
]);

const classStats = ref({
  averageScore: 82,
  passRate: 85,
  highestScore: 95,
  lowestScore: 60
});

const studentGrades = ref([
  { studentId: 1, studentName: '张三', averageScore: 90, highestScore: 95, lowestScore: 85, completionRate: 100 },
  { studentId: 2, studentName: '李四', averageScore: 85, highestScore: 92, lowestScore: 78, completionRate: 95 },
  { studentId: 3, studentName: '王五', averageScore: 78, highestScore: 85, lowestScore: 70, completionRate: 90 },
  { studentId: 4, studentName: '赵六', averageScore: 92, highestScore: 98, lowestScore: 88, completionRate: 100 },
  { studentId: 5, studentName: '钱七', averageScore: 75, highestScore: 82, lowestScore: 65, completionRate: 85 }
]);

const errorQuestions = ref([
  { questionId: 1, questionContent: '什么是计算机网络？', errorCount: 8, errorRate: 40 },
  { questionId: 2, questionContent: '数据结构中栈的特点是什么？', errorCount: 6, errorRate: 30 },
  { questionId: 3, questionContent: '算法的时间复杂度如何计算？', errorCount: 10, errorRate: 50 },
  { questionId: 4, questionContent: '数据库的基本操作有哪些？', errorCount: 5, errorRate: 25 },
  { questionId: 5, questionContent: '操作系统的主要功能是什么？', errorCount: 7, errorRate: 35 }
]);

const getTeacherGrades = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get('/teacher/grades', { params: { courseId: selectedCourseId.value } });
    // return response.data;
    
    // 模拟数据
    return {
      classStats: {
        averageScore: 82,
        passRate: 85,
        highestScore: 95,
        lowestScore: 60
      },
      studentGrades: [
        { studentId: 1, studentName: '张三', averageScore: 90, highestScore: 95, lowestScore: 85, completionRate: 100 },
        { studentId: 2, studentName: '李四', averageScore: 85, highestScore: 92, lowestScore: 78, completionRate: 95 },
        { studentId: 3, studentName: '王五', averageScore: 78, highestScore: 85, lowestScore: 70, completionRate: 90 },
        { studentId: 4, studentName: '赵六', averageScore: 92, highestScore: 98, lowestScore: 88, completionRate: 100 },
        { studentId: 5, studentName: '钱七', averageScore: 75, highestScore: 82, lowestScore: 65, completionRate: 85 }
      ],
      errorQuestions: [
        { questionId: 1, questionContent: '什么是计算机网络？', errorCount: 8, errorRate: 40 },
        { questionId: 2, questionContent: '数据结构中栈的特点是什么？', errorCount: 6, errorRate: 30 },
        { questionId: 3, questionContent: '算法的时间复杂度如何计算？', errorCount: 10, errorRate: 50 },
        { questionId: 4, questionContent: '数据库的基本操作有哪些？', errorCount: 5, errorRate: 25 },
        { questionId: 5, questionContent: '操作系统的主要功能是什么？', errorCount: 7, errorRate: 35 }
      ],
      scoreDistribution: [
        { range: '90-100', count: 5 },
        { range: '80-89', count: 8 },
        { range: '70-79', count: 4 },
        { range: '60-69', count: 2 },
        { range: '0-59', count: 1 }
      ],
      knowledgeMastery: [
        { knowledge: '计算机基础', mastery: 85 },
        { knowledge: '数据结构', mastery: 78 },
        { knowledge: '算法', mastery: 70 },
        { knowledge: '数据库', mastery: 82 },
        { knowledge: '操作系统', mastery: 75 }
      ]
    };
  } catch (error) {
    ElMessage.error('获取成绩数据失败');
    return null;
  }
};

const { execute: fetchGrades } = useRequest(getTeacherGrades);

const handleCourseChange = () => {
  fetchGrades();
};

const exportData = () => {
  ElMessage.success('数据导出成功');
};

const viewStudentDetails = (studentId: number) => {
  router.push(`/teacher/students/${studentId}`);
};

const viewQuestionDetails = (questionId: number) => {
  router.push(`/admin/questions/${questionId}`);
};

const initDistributionChart = (scoreDistribution: any[]) => {
  if (distributionChartRef.value) {
    distributionChart = echarts.init(distributionChartRef.value);
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
        data: scoreDistribution.map(item => item.range)
      },
      yAxis: {
        type: 'value',
        min: 0
      },
      series: [{
        data: scoreDistribution.map(item => item.count),
        type: 'bar',
        itemStyle: {
          color: '#409EFF'
        }
      }]
    };
    distributionChart.setOption(option);
  }
};

const initKnowledgeChart = (knowledgeMastery: any[]) => {
  if (knowledgeChartRef.value) {
    knowledgeChart = echarts.init(knowledgeChartRef.value);
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
        type: 'value',
        min: 0,
        max: 100
      },
      yAxis: {
        type: 'category',
        data: knowledgeMastery.map(item => item.knowledge)
      },
      series: [{
        data: knowledgeMastery.map(item => item.mastery),
        type: 'bar',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#67c23a' },
            { offset: 1, color: '#409EFF' }
          ])
        }
      }]
    };
    knowledgeChart.setOption(option);
  }
};

onMounted(() => {
  selectedCourseId.value = courses.value[0].courseId;
  fetchGrades().then(data => {
    if (data) {
      classStats.value = data.classStats;
      studentGrades.value = data.studentGrades;
      errorQuestions.value = data.errorQuestions;
      nextTick(() => {
        initDistributionChart(data.scoreDistribution);
        initKnowledgeChart(data.knowledgeMastery);
      });
    }
  });
  
  // 监听窗口大小变化，自适应图表
  window.addEventListener('resize', () => {
    distributionChart?.resize();
    knowledgeChart?.resize();
  });
});
</script>

<style scoped>
.teacher-grades {
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
    justify-content: space-between;
  }
}
</style>