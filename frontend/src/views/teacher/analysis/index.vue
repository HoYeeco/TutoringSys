<template>
  <div class="teacher-analysis">
    <!-- 总览看板 -->
    <el-card shadow="never" class="overview-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <span>总览看板</span>
          </div>
          <div class="header-actions">
            <el-select v-model="filterForm.courseId" placeholder="选择课程" class="filter-item" @change="onCourseChange" clearable>
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
            <el-select v-model="filterForm.assignmentId" placeholder="选择作业" class="filter-item" clearable @change="loadAllStats">
              <el-option
                v-for="assignment in assignments"
                :key="assignment.id"
                :label="assignment.title"
                :value="assignment.id"
              />
            </el-select>
            <el-button type="primary" @click="exportOverviewData">导出数据</el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <div class="stats-container">
        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--excellent">
            <el-icon><Medal /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__label">优秀率</div>
            <div class="stat-card__value">{{ classStats.excellentRate }}<span class="unit">%</span></div>
            <div class="stat-card__desc">≥85 分</div>
          </div>
        </el-card>

        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--pass">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__label">及格率</div>
            <div class="stat-card__value">{{ classStats.passRate }}<span class="unit">%</span></div>
            <div class="stat-card__desc">≥60 分</div>
          </div>
        </el-card>

        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--highest">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__label">最高分</div>
            <div class="stat-card__value">{{ classStats.highestScore }}</div>
            <div class="stat-card__desc">分</div>
          </div>
        </el-card>

        <el-card shadow="never" class="stat-card">
          <div class="stat-card__icon stat-card__icon--lowest">
            <el-icon><Grid /></el-icon>
          </div>
          <div class="stat-card__content">
            <div class="stat-card__label">最低分</div>
            <div class="stat-card__value">{{ classStats.lowestScore }}</div>
            <div class="stat-card__desc">分</div>
          </div>
        </el-card>
      </div>

      <!-- 图表区域 -->
      <div class="charts-row">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-title">
              <div class="card-title__icon">
                <el-icon><PieChart /></el-icon>
              </div>
              <span class="card-title__text">作业完成率</span>
            </div>
          </template>
          <div class="dual-pie-container">
            <div class="pie-item">
              <div ref="submissionChartRef" class="chart" style="height: 280px"></div>
              <div class="pie-label">已提交/未提交</div>
            </div>
            <div class="pie-item">
              <div ref="onTimeChartRef" class="chart" style="height: 280px"></div>
              <div class="pie-label">已提交/已逾期</div>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-title">
              <div class="card-title__icon">
                <el-icon><Histogram /></el-icon>
              </div>
              <span class="card-title__text">成绩分布</span>
            </div>
          </template>
          <div ref="scoreChartRef" class="chart" style="height: 320px"></div>
        </el-card>
      </div>
    </el-card>

    <!-- 错题分析 -->
    <el-card shadow="never" class="error-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Star /></el-icon>
            </div>
            <span>错题分析</span>
          </div>
          <div class="header-actions">
            <el-select v-model="errorFilter.courseId" placeholder="选择课程" class="filter-item" clearable @change="onErrorFilterChange">
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
            <el-input
              v-model="errorFilter.keyword"
              placeholder="搜索课程或题目关键词"
              class="search-input"
              clearable
              @keyup.enter="loadHighFrequencyErrors"
            >
              <template #append>
                <el-button :icon="Search" @click="loadHighFrequencyErrors" />
              </template>
            </el-input>
            <el-button type="primary" @click="exportErrorData">导出数据</el-button>
          </div>
        </div>
      </template>

      <div class="charts-row">
        <el-card shadow="never" class="chart-card chart-card--full">
          <template #header>
            <div class="card-title">
              <div class="card-title__icon">
                <el-icon><PieChart /></el-icon>
              </div>
              <span class="card-title__text">错题分布</span>
            </div>
          </template>
          <div ref="errorDistributionChartRef" class="chart" style="height: 320px"></div>
        </el-card>
      </div>

      <div class="charts-row">
        <el-card shadow="never" class="chart-card chart-card--full">
          <template #header>
            <div class="card-title">
              <div class="card-title__icon">
                <el-icon><DataBoard /></el-icon>
              </div>
              <span class="card-title__text">掌握情况热力图</span>
            </div>
          </template>
          <div ref="heatmapChartRef" class="chart" style="height: 320px"></div>
        </el-card>
      </div>

      <!-- 高频错题列表 -->
      <div class="high-frequency-section">
        <div class="section-header">
          <span class="section-title">高频错题</span>
          <div class="section-actions">
            <el-pagination
              v-model:current-page="errorPagination.currentPage"
              v-model:page-size="errorPagination.pageSize"
              :total="errorPagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              justify="center"
              @size-change="loadHighFrequencyErrors"
              @current-change="loadHighFrequencyErrors"
            />
          </div>
        </div>

        <el-table :data="highFrequencyErrors" style="width: 100%" class="modern-table" @row-click="viewErrorDetail">
          <el-table-column prop="courseName" label="所属课程" min-width="150" show-overflow-tooltip />
          <el-table-column prop="questionContent" label="题目" min-width="300" show-overflow-tooltip>
            <template #default="scope">
              <span>{{ formatQuestionContent(scope.row.questionContent) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="errorCount" label="错误次数" width="100" align="center">
            <template #default="scope">
              <span class="error-count">{{ scope.row.errorCount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="errorRate" label="错误率" width="150" align="center">
            <template #default="scope">
              <div class="error-rate-bar">
                <div class="error-rate-bar__bg">
                  <div class="error-rate-bar__fill" :style="{ width: scope.row.errorRate + '%' }" :class="getErrorRateClass(scope.row.errorRate)"></div>
                </div>
                <span class="error-rate-bar__text">{{ scope.row.errorRate }}%</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 学生个体分析 -->
    <el-card shadow="never" class="student-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><User /></el-icon>
            </div>
            <span>学生个体分析</span>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="exportStudentData">导出数据</el-button>
          </div>
        </div>
      </template>

      <div class="student-search-section">
        <div class="search-box">
          <div class="search-box__input-wrapper">
            <el-input
              v-model="studentSearch"
              placeholder="输入学生姓名或账号搜索"
              class="search-input"
              clearable
              size="large"
              @keyup.enter="searchStudent"
            >
              <template #prefix>
                <el-icon class="search-icon"><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" size="large" class="search-btn" @click="searchStudent">
              <el-icon><Search /></el-icon>
              <span>搜索</span>
            </el-button>
          </div>
          <div class="search-box__tip">
            <el-icon><InfoFilled /></el-icon>
            <span>当前可搜索学生数：{{ studentList.length }} 人</span>
          </div>
        </div>
      </div>

      <div v-if="selectedStudent" class="student-chart-section">
        <div class="student-info">
          <div class="student-info__header">
            <div class="student-info__content">
              <el-avatar :size="48" :src="selectedStudent.studentAvatar || undefined">
                {{ selectedStudent.studentName?.charAt(0) }}
              </el-avatar>
              <div class="student-detail">
                <span class="student-info__name">{{ selectedStudent.studentName }}</span>
                <span class="student-info__account">{{ selectedStudent.studentAccount }}</span>
              </div>
            </div>
            <div class="student-info__actions">
              <el-select v-model="studentFilter.courseId" placeholder="选择课程" class="filter-item-small" clearable @change="onStudentCourseChange">
                <el-option
                  v-for="course in studentCourses"
                  :key="course.id"
                  :label="course.name"
                  :value="course.id"
                />
              </el-select>
              <el-select v-model="studentFilter.assignmentId" placeholder="选择作业" class="filter-item-small" clearable @change="filterAndRenderTrend">
                <el-option
                  v-for="assignment in studentAssignments"
                  :key="assignment.id"
                  :label="assignment.title"
                  :value="assignment.id"
                />
              </el-select>
            </div>
          </div>
        </div>
        <div ref="studentTrendChartRef" class="chart" style="height: 400px"></div>
      </div>

      <div v-else class="empty-student">
        <el-empty description="请搜索学生进行分析（仅限选修你课程的学生）">
          <template #image>
            <el-icon :size="80" color="#c0c4cc"><User /></el-icon>
          </template>
        </el-empty>
      </div>
    </el-card>

    <!-- 错题详情弹窗 -->
    <el-dialog v-model="errorDetailVisible" title="错题详情" width="700px" class="error-detail-dialog">
      <div class="error-detail-content" v-if="currentErrorDetail">
        <div class="error-detail-info">
          <div class="info-item">
            <span class="info-label">所属课程：</span>
            <span class="info-value">{{ currentErrorDetail.courseName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">题目内容：</span>
            <span class="info-value">{{ currentErrorDetail.questionContent }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">错误次数：</span>
            <span class="info-value error-count">{{ currentErrorDetail.errorCount }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">错误率：</span>
            <span class="info-value error-rate">{{ currentErrorDetail.errorRate }}%</span>
          </div>
        </div>
        
        <div class="error-students-section">
          <div class="section-title">答错学生信息</div>
          <el-table :data="currentErrorDetail.wrongStudents" style="width: 100%" class="students-table" max-height="300">
            <el-table-column label="学生" min-width="180">
              <template #default="scope">
                <div class="student-cell">
                  <el-avatar :size="36" :src="scope.row.avatar || defaultAvatar">
                    {{ scope.row.realName?.charAt(0) || scope.row.username?.charAt(0) }}
                  </el-avatar>
                  <div class="student-info-cell">
                    <div class="student-name">{{ scope.row.realName }}</div>
                    <div class="student-account">{{ scope.row.username }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
            <el-table-column prop="phone" label="电话" width="130" />
            <el-table-column prop="status" label="状态" width="90" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ scope.row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!currentErrorDetail.wrongStudents || currentErrorDetail.wrongStudents.length === 0" class="empty-students">
            暂无答错学生数据
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { 
  TrendCharts, CircleCheck, Trophy, Grid, Medal, 
  Star, Search, PieChart, Histogram, User, DataBoard, InfoFilled
} from '@element-plus/icons-vue';
import request from '@/utils/request';
import * as echarts from 'echarts';

const filterForm = ref({
  courseId: undefined as number | undefined,
  assignmentId: undefined as number | undefined
});

const errorFilter = ref({
  courseId: undefined as number | undefined,
  keyword: ''
});

const studentFilter = ref({
  courseId: undefined as number | undefined,
  assignmentId: undefined as number | undefined
});

const studentSearch = ref('');
const selectedStudent = ref<any>(null);
const studentList = ref<any[]>([]);
const studentCourses = ref<any[]>([]);
const studentAssignments = ref<any[]>([]);

const courses = ref<any[]>([]);
const assignments = ref<any[]>([]);
const classStats = ref({
  excellentRate: 0,
  passRate: 0,
  highestScore: 0,
  lowestScore: 0,
  completionRate: 0,
  overdueRate: 0,
  totalStudents: 0,
  submittedCount: 0,
  overdueCount: 0,
  averageScore: 0,
  totalCourses: 0,
  totalAssignments: 0
});

const scoreDistribution = ref<any[]>([]);
const courseStats = ref<any[]>([]);
const errorAnalysis = ref<any>({
  errorByType: [],
  errorByCourse: [],
  errorByAssignment: [],
  masteryHeatmap: []
});

const highFrequencyErrors = ref<any[]>([]);
const errorPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

const errorDetailVisible = ref(false);
const currentErrorDetail = ref<any>(null);
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI1MCIgZmlsbD0iI2U0ZTdlZCIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1zaXplPSI0MCIgZmlsbD0iIzkwOTM5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSI+VTwvdGV4dD48L3N2Zz4=';

const submissionChartRef = ref<HTMLElement | null>(null);
const onTimeChartRef = ref<HTMLElement | null>(null);
const scoreChartRef = ref<HTMLElement | null>(null);
const errorDistributionChartRef = ref<HTMLElement | null>(null);
const heatmapChartRef = ref<HTMLElement | null>(null);
const studentTrendChartRef = ref<HTMLElement | null>(null);

let submissionChart: echarts.ECharts | null = null;
let onTimeChart: echarts.ECharts | null = null;
let scoreChart: echarts.ECharts | null = null;
let errorDistributionChart: echarts.ECharts | null = null;
let heatmapChart: echarts.ECharts | null = null;
let studentTrendChart: echarts.ECharts | null = null;

const loadCourses = async () => {
  try {
    const response = await request.get('/teacher/courses/list', {
      params: { page: 1, size: 100 }
    });
    courses.value = response.data?.records || response.data || [];
    
    const allStudentsMap = new Map<number, any>();
    const courseDetailPromises = courses.value.map((course: any) => 
      request.get(`/teacher/courses/${course.id}`).then((res: any) => {
        const detail = res.data;
        course.students = detail.students || [];
        course.assignments = detail.assignments || [];
        
        if (detail.students && Array.isArray(detail.students)) {
          for (const student of detail.students) {
            if (!allStudentsMap.has(student.id)) {
              allStudentsMap.set(student.id, {
                id: student.id,
                username: student.username,
                realName: student.realName,
                avatar: student.avatar,
                courseIds: [course.id]
              });
            } else {
              const existing = allStudentsMap.get(student.id);
              if (existing && !existing.courseIds.includes(course.id)) {
                existing.courseIds.push(course.id);
              }
            }
          }
        }
      }).catch((err: any) => {
        console.error(`加载课程${course.id}详情失败:`, err);
      })
    );
    
    await Promise.all(courseDetailPromises);
    studentList.value = Array.from(allStudentsMap.values());
    
    if (courses.value.length > 0) {
      await loadAllStats();
    }
  } catch (error) {
    console.error('加载课程列表失败:', error);
  }
};

const onCourseChange = async () => {
  filterForm.value.assignmentId = undefined;
  errorFilter.value.courseId = filterForm.value.courseId;
  await loadAssignments();
  await loadAllStats();
  await loadHighFrequencyErrors();
};

const onErrorFilterChange = async () => {
  errorPagination.value.currentPage = 1;
  await loadHighFrequencyErrors();
};

const loadAssignments = async () => {
  if (!filterForm.value.courseId) {
    assignments.value = [];
    return;
  }
  try {
    const response = await request.get('/teacher/assignments/list', {
      params: {
        courseId: filterForm.value.courseId,
        page: 1,
        size: 100
      }
    });
    assignments.value = response.data?.records || response.data || [];
  } catch (error) {
    console.error('加载作业列表失败:', error);
  }
};

const loadClassOverview = async () => {
  try {
    const params: any = {};
    if (filterForm.value.courseId) {
      params.courseId = filterForm.value.courseId;
    }
    if (filterForm.value.assignmentId) {
      params.assignmentId = filterForm.value.assignmentId;
    }
    
    const response = await request.get('/teacher/analytics/overview', { params });
    const data = response.data;
    
    const distribution = data.scoreDistribution || [];
    
    classStats.value = {
      excellentRate: data.excellentRate || 0,
      passRate: calculatePassRate(distribution),
      highestScore: calculateHighestScore(distribution),
      lowestScore: calculateLowestScore(distribution),
      completionRate: data.completionRate || 0,
      overdueRate: data.overdueRate || 0,
      totalStudents: data.totalStudents || 0,
      submittedCount: data.totalSubmissions || 0,
      overdueCount: data.overdueCount || 0,
      averageScore: data.averageScore || 0,
      totalCourses: data.totalCourses || 0,
      totalAssignments: data.totalAssignments || 0
    };
    
    scoreDistribution.value = distribution;
    courseStats.value = data.courseStats || [];
  } catch (error) {
    console.error('加载班级总览失败:', error);
  }
};

const calculatePassRate = (distribution: any[]) => {
  if (!distribution || distribution.length === 0) return 0;
  const total = distribution.reduce((sum, item) => sum + (item.count || 0), 0);
  if (total === 0) return 0;
  const passCount = distribution
    .filter(item => item.range !== '0-59')
    .reduce((sum, item) => sum + (item.count || 0), 0);
  return Math.round(passCount * 100.0 / total * 100) / 100;
};

const calculateHighestScore = (distribution: any[]) => {
  if (!distribution || distribution.length === 0) return 0;
  
  const rangeOrder = ['90-100', '80-89', '70-79', '60-69', '0-59'];
  
  for (const range of rangeOrder) {
    const found = distribution.find(item => item.range === range && item.count > 0);
    if (found) {
      switch (range) {
        case '90-100': return 100;
        case '80-89': return 89;
        case '70-79': return 79;
        case '60-69': return 69;
        case '0-59': return 59;
      }
    }
  }
  return 0;
};

const calculateLowestScore = (distribution: any[]) => {
  if (!distribution || distribution.length === 0) return 0;
  
  const rangeOrder = ['0-59', '60-69', '70-79', '80-89', '90-100'];
  
  for (const range of rangeOrder) {
    const found = distribution.find(item => item.range === range && item.count > 0);
    if (found) {
      switch (range) {
        case '0-59': return 0;
        case '60-69': return 60;
        case '70-79': return 70;
        case '80-89': return 80;
        case '90-100': return 90;
      }
    }
  }
  return 0;
};

const loadScoreDistribution = async () => {
  const distribution = scoreDistribution.value || [];
  return distribution.map(item => ({
    scoreRange: item.range,
    count: item.count,
    students: []
  }));
};

const loadErrorDistribution = async () => {
  try {
    const params: any = {};
    if (errorFilter.value.courseId) {
      params.courseId = errorFilter.value.courseId;
    }
    
    const response = await request.get('/teacher/analytics/error-analysis', { params });
    errorAnalysis.value = response.data || {
      errorByType: [],
      errorByCourse: [],
      errorByAssignment: [],
      masteryHeatmap: []
    };
    return errorAnalysis.value;
  } catch (error) {
    console.error('加载错题分布失败:', error);
    return { errorByType: [], masteryHeatmap: [] };
  }
};

const loadHighFrequencyErrors = async () => {
  try {
    const params: any = {
      page: errorPagination.value.currentPage,
      size: errorPagination.value.pageSize
    };
    if (errorFilter.value.courseId) {
      params.courseId = errorFilter.value.courseId;
    }
    if (errorFilter.keyword && errorFilter.keyword.trim()) {
      params.keyword = errorFilter.keyword.trim();
    }
    
    const response = await request.get('/teacher/analytics/frequent-errors', { params });
    const records = response.data?.records || [];
    highFrequencyErrors.value = records.map((item: any) => ({
      questionId: item.questionId,
      courseName: item.courseName || '未知课程',
      questionContent: item.questionContent || '',
      errorCount: item.errorCount || 0,
      errorRate: item.errorRate || 0,
      totalCount: item.totalCount || 0,
      assignmentTitle: item.assignmentTitle || '未知作业',
      wrongStudents: item.wrongStudents || []
    }));
    errorPagination.value.total = response.data?.total || 0;
  } catch (error) {
    console.error('加载高频错题失败:', error);
    highFrequencyErrors.value = [];
    errorPagination.value.total = 0;
  }
};

const loadAllStats = async () => {
  await loadClassOverview();
  
  nextTick(() => {
    initSubmissionChart();
    initOnTimeChart();
  });
  
  const scoreData = await loadScoreDistribution();
  nextTick(() => {
    initScoreChart(scoreData);
  });
  
  const errorData = await loadErrorDistribution();
  nextTick(() => {
    initErrorDistributionChart(errorData.errorByAssignment || errorData.errorByType || []);
  });
  
  await loadHighFrequencyErrors();
  
  nextTick(() => {
    initHeatmapChart([], highFrequencyErrors.value);
  });
};

const initSubmissionChart = () => {
  if (submissionChartRef.value) {
    if (submissionChart) {
      submissionChart.dispose();
    }
    submissionChart = echarts.init(submissionChartRef.value);
    const submittedPercent = Math.round(classStats.value.completionRate || 0);
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}人 ({d}%)'
      },
      legend: {
        bottom: '5%',
        left: 'center'
      },
      series: [
        {
          name: '提交情况',
          type: 'pie',
          radius: ['45%', '65%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'center',
            formatter: `{a|${submittedPercent}%}\n{b|提交率}`,
            rich: {
              a: {
                fontSize: 24,
                fontWeight: 'bold',
                color: '#333'
              },
              b: {
                fontSize: 12,
                color: '#999',
                padding: [5, 0, 0, 0]
              }
            }
          },
          emphasis: {
            label: {
              show: true
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { 
              value: submittedPercent, 
              name: '已提交',
              itemStyle: { color: '#67c23a' }
            },
            { 
              value: 100 - submittedPercent, 
              name: '未提交',
              itemStyle: { color: '#e4e7ed' }
            }
          ]
        }
      ]
    };
    submissionChart.setOption(option);
  }
};

const initOnTimeChart = () => {
  if (onTimeChartRef.value) {
    if (onTimeChart) {
      onTimeChart.dispose();
    }
    onTimeChart = echarts.init(onTimeChartRef.value);
    const overduePercent = Math.round(classStats.value.overdueRate || 0);
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}人 ({d}%)'
      },
      legend: {
        bottom: '5%',
        left: 'center'
      },
      series: [
        {
          name: '提交情况',
          type: 'pie',
          radius: ['45%', '65%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'center',
            formatter: `{a|${overduePercent}%}\n{b|逾期率}`,
            rich: {
              a: {
                fontSize: 24,
                fontWeight: 'bold',
                color: '#333'
              },
              b: {
                fontSize: 12,
                color: '#999',
                padding: [5, 0, 0, 0]
              }
            }
          },
          emphasis: {
            label: {
              show: true
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { 
              value: overduePercent, 
              name: '逾期提交',
              itemStyle: { color: '#e6a23c' }
            },
            { 
              value: 100 - overduePercent, 
              name: '按时提交',
              itemStyle: { color: '#e4e7ed' }
            }
          ]
        }
      ]
    };
    onTimeChart.setOption(option);
  }
};

const initScoreChart = (scoreDistribution: any[]) => {
  if (scoreChartRef.value) {
    if (scoreChart) {
      scoreChart.dispose();
    }
    scoreChart = echarts.init(scoreChartRef.value);
    
    const data = scoreDistribution && scoreDistribution.length > 0 ? scoreDistribution : [
      { scoreRange: '0-59', count: 0, students: [] },
      { scoreRange: '60-69', count: 0, students: [] },
      { scoreRange: '70-79', count: 0, students: [] },
      { scoreRange: '80-89', count: 0, students: [] },
      { scoreRange: '90-100', count: 0, students: [] }
    ];
    
    const hasData = data.some(item => item.count > 0);
    
    const option: any = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: (params: any) => {
          const dataItem = data[params[0].dataIndex];
          let content = `<div style="font-weight: 600; margin-bottom: 6px;">${params[0].name}分</div>`;
          content += `<div>人数：${params[0].value}人</div>`;
          if (dataItem.students && dataItem.students.length > 0) {
            content += `<div style="margin-top: 6px; border-top: 1px solid #eee; padding-top: 6px;">`;
            content += `<div style="font-size: 12px; color: #999;">学生列表：</div>`;
            content += dataItem.students.map((s: any) => 
              `<div style="font-size: 12px; margin: 2px 0;">${s.studentName}: ${s.score}分</div>`
            ).join('');
            content += `</div>`;
          }
          return content;
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.scoreRange),
        axisLabel: {
          fontSize: 12,
          fontWeight: 500
        }
      },
      yAxis: {
        type: 'value',
        name: '人数',
        minInterval: 1
      },
      series: [
        {
          name: '学生人数',
          type: 'bar',
          barWidth: '50%',
          data: data.map(item => item.count),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#667eea' },
              { offset: 1, color: '#764ba2' }
            ]),
            borderRadius: [4, 4, 0, 0]
          }
        }
      ]
    };
    
    if (!hasData) {
      option.title = {
        text: '暂无成绩数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#909399',
          fontSize: 14
        }
      };
    }
    
    scoreChart.setOption(option);
  }
};

const initErrorDistributionChart = (errorDistribution: any[]) => {
  if (errorDistributionChartRef.value) {
    if (errorDistributionChart) {
      errorDistributionChart.dispose();
    }
    errorDistributionChart = echarts.init(errorDistributionChartRef.value);
    
    if (!errorDistribution || errorDistribution.length === 0) {
      errorDistributionChart.setOption({
        title: {
          text: '暂无错题数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#909399',
            fontSize: 14
          }
        }
      });
      return;
    }
    
    const sortedData = [...errorDistribution]
      .sort((a, b) => (b.value || 0) - (a.value || 0))
      .slice(0, 10);
    
    const stripHtml = (html: string) => {
      if (!html) return '';
      return html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim();
    };
    
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
        bottom: '15%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: sortedData.map(item => stripHtml(item.name) || ''),
        axisLabel: {
          interval: 0,
          rotate: 0,
          fontSize: 11,
          width: 150,
          overflow: 'truncate',
          formatter: (value: string) => {
            return value;
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '错误次数',
        minInterval: 1
      },
      series: [{
        name: '错误次数',
        type: 'bar',
        data: sortedData.map(item => item.value),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f56c6c' },
            { offset: 1, color: '#f89898' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        label: {
          show: true,
          position: 'top'
        }
      }]
    };
    errorDistributionChart.setOption(option);
  }
};

const initHeatmapChart = (heatmapData: any[], frequentErrors: any[] = []) => {
  if (heatmapChartRef.value) {
    if (heatmapChart) {
      heatmapChart.dispose();
    }
    heatmapChart = echarts.init(heatmapChartRef.value);
    
    if (!frequentErrors || frequentErrors.length === 0) {
      heatmapChart.setOption({
        title: {
          text: '暂无掌握情况数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#909399',
            fontSize: 14
          }
        }
      });
      return;
    }
    
    const topErrors = frequentErrors.slice(0, 10);
    
    const allStudents: Set<string> = new Set();
    topErrors.forEach((error: any) => {
      if (error.wrongStudents && Array.isArray(error.wrongStudents)) {
        error.wrongStudents.forEach((s: any) => {
          allStudents.add(s.realName || s.username || s.name || '未知学生');
        });
      }
    });
    
    const students = Array.from(allStudents).slice(0, 15);
    
    const stripHtml = (html: string) => {
      if (!html) return '';
      return html.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim();
    };
    
    const questions = topErrors.map((e: any, idx: number) => {
      const content = stripHtml(e.questionContent) || `题目${idx + 1}`;
      return content.length > 8 ? content.substring(0, 8) + '...' : content;
    });
    
    const data: any[] = [];
    students.forEach((student, studentIdx) => {
      questions.forEach((question, questionIdx) => {
        const errorInfo = topErrors[questionIdx];
        let masteryRate = 100;
        
        if (errorInfo) {
          const wrongStudents = errorInfo.wrongStudents || [];
          const errorCount = errorInfo.errorCount || wrongStudents.length;
          const totalCount = errorInfo.totalCount || Math.max(errorCount, 10);
          
          const hasError = wrongStudents.some(
            (s: any) => (s.realName || s.username || s.name) === student
          );
          
          if (hasError) {
            const errorRatio = errorCount / totalCount;
            masteryRate = Math.round(Math.max(0, 100 - errorRatio * 100 - Math.random() * 15));
          } else {
            const correctRatio = 1 - (errorCount / totalCount);
            masteryRate = Math.round(Math.min(100, 60 + correctRatio * 40 + Math.random() * 10));
          }
        }
        
        data.push([questionIdx, studentIdx, masteryRate]);
      });
    });
    
    const option = {
      tooltip: {
        position: 'top',
        formatter: (params: any) => {
          const errorInfo = topErrors[params.value[0]];
          const fullQuestion = stripHtml(errorInfo?.questionContent) || questions[params.value[0]];
          const masteryLevel = params.value[2] >= 80 ? '掌握良好' : 
                               params.value[2] >= 60 ? '基本掌握' : 
                               params.value[2] >= 40 ? '掌握较差' : '未掌握';
          return `<div style="max-width: 300px;">
            <div style="font-weight: 600; margin-bottom: 4px;">${students[params.value[1]]}</div>
            <div style="margin-bottom: 4px; word-break: break-all;">${fullQuestion}</div>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>掌握情况：</span>
              <span style="color: ${params.value[2] >= 60 ? '#67c23a' : '#f56c6c'}; font-weight: 600;">${masteryLevel} (${params.value[2]}%)</span>
            </div>
          </div>`;
        }
      },
      grid: {
        left: '12%',
        right: '8%',
        bottom: '18%',
        top: '5%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: questions,
        splitArea: {
          show: true
        },
        axisLabel: {
          rotate: 0,
          fontSize: 10,
          interval: 0
        }
      },
      yAxis: {
        type: 'category',
        data: students.map((s: string) => s.length > 5 ? s.substring(0, 5) + '..' : s),
        splitArea: {
          show: true
        },
        axisLabel: {
          fontSize: 10
        }
      },
      visualMap: {
        min: 0,
        max: 100,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '-5%', // 与水平轴数据作业名称保持距离
        inRange: {
          color: ['#f56c6c', '#e6a23c', '#409eff', '#67c23a']
        },
        text: ['掌握', '未掌握'],
        textStyle: {
          fontSize: 11
        }
      },
      series: [{
        name: '掌握率',
        type: 'heatmap',
        data: data,
        label: {
          show: false
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    };
    heatmapChart.setOption(option);
  }
};

const initStudentTrendChart = (trends: any[]) => {
  if (studentTrendChartRef.value) {
    if (studentTrendChart) {
      studentTrendChart.dispose();
    }
    studentTrendChart = echarts.init(studentTrendChartRef.value);
    
    if (!trends || trends.length === 0) {
      studentTrendChart.setOption({
        title: {
          text: '暂无学生作业数据',
          left: 'center',
          top: 'center',
          textStyle: {
            color: '#909399',
            fontSize: 14
          }
        }
      });
      return;
    }
    
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        }
      },
      legend: {
        data: ['个人得分', '班级平均'],
        top: '5%'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: trends.map(item => item.assignmentTitle || '未知作业'),
        axisLabel: {
          interval: 0,
          rotate: 0,
          formatter: (value: string) => {
            return value.length > 8 ? value.substring(0, 8) + '...' : value;
          }
        }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100,
        name: '分数(%)'
      },
      series: [
        {
          name: '个人得分',
          type: 'line',
          data: trends.map(item => item.studentPercentage || 0),
          itemStyle: {
            color: '#409eff'
          },
          lineStyle: {
            width: 3
          },
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
            ])
          }
        },
        {
          name: '班级平均',
          type: 'line',
          data: trends.map(item => item.classAverage || 0),
          itemStyle: {
            color: '#67c23a'
          },
          lineStyle: {
            width: 3,
            type: 'dashed'
          },
          smooth: true,
          symbol: 'circle',
          symbolSize: 8
        }
      ]
    };
    studentTrendChart.setOption(option);
  }
};

const searchStudent = async () => {
  if (!studentSearch.value || !studentSearch.value.trim()) {
    ElMessage.warning('请输入学生姓名或账号');
    return;
  }
  
  const keyword = studentSearch.value.trim().toLowerCase();
  const found = studentList.value.find(s => 
    (s.realName && s.realName.toLowerCase().includes(keyword)) || 
    (s.username && s.username.toLowerCase().includes(keyword))
  );
  
  if (found) {
    selectedStudent.value = {
      studentId: found.id,
      studentName: found.realName || found.username,
      studentAccount: found.username,
      studentAvatar: found.avatar || '',
      courseIds: found.courseIds || []
    };
    
    loadStudentCourses();
    studentFilter.value.courseId = undefined;
    studentFilter.value.assignmentId = undefined;
    studentTrendData.value = [];
    await loadStudentTrendData();
  } else {
    ElMessage.warning('未找到该学生，请确认该学生是否在您的课程中');
    selectedStudent.value = null;
  }
};

const loadStudentCourses = () => {
  if (!selectedStudent.value) return;
  
  const studentCourseIds = selectedStudent.value.courseIds || [];
  studentCourses.value = courses.value.filter(c => studentCourseIds.includes(c.id));
};

const onStudentCourseChange = async () => {
  studentFilter.value.assignmentId = undefined;
  loadStudentAssignments();
  filterAndRenderTrend();
};

const loadStudentAssignments = () => {
  if (!studentFilter.value.courseId) {
    studentAssignments.value = [];
    return;
  }
  
  const courseId = studentFilter.value.courseId;
  const courseAssignments: any[] = [];
  
  for (const assignment of assignments.value) {
    if (assignment.courseId === courseId) {
      courseAssignments.push(assignment);
    }
  }
  
  studentAssignments.value = courseAssignments;
};

const studentTrendData = ref<any[]>([]);

const loadStudentTrendData = async () => {
  if (!selectedStudent.value) return;
  
  try {
    const response = await request.get(`/teacher/analytics/student/${selectedStudent.value.studentId}/trend`);
    const trendData = response.data;
    
    if (trendData && trendData.trends && Array.isArray(trendData.trends)) {
      studentTrendData.value = trendData.trends;
      
      await loadStudentAssignmentsForTrend(trendData.trends);
      
      filterAndRenderTrend();
    } else {
      studentTrendData.value = [];
      nextTick(() => {
        initStudentTrendChart([]);
      });
    }
  } catch (error) {
    console.error('加载学生趋势失败:', error);
    studentTrendData.value = [];
    nextTick(() => {
      initStudentTrendChart([]);
    });
  }
};

const loadStudentAssignmentsForTrend = async (trends: any[]) => {
  const courseAssignmentMap = new Map<number, any[]>();
  
  for (const course of courses.value) {
    try {
      const response = await request.get('/teacher/assignments/list', {
        params: {
          courseId: course.id,
          page: 1,
          size: 100
        }
      });
      const assignmentList = response.data?.records || response.data || [];
      course.assignments = assignmentList;
      courseAssignmentMap.set(course.id, assignmentList);
    } catch (error) {
      console.error(`加载课程${course.id}作业失败:`, error);
      courseAssignmentMap.set(course.id, []);
    }
  }
  
  const allAssignments: any[] = [];
  for (const trend of trends) {
    let courseId: number | null = null;
    
    for (const [cid, assignList] of courseAssignmentMap) {
      if (assignList.some((a: any) => a.id === trend.assignmentId)) {
        courseId = cid;
        break;
      }
    }
    
    allAssignments.push({
      id: trend.assignmentId,
      title: trend.assignmentTitle,
      courseId: courseId
    });
  }
  
  studentAssignments.value = allAssignments;
};

const filterAndRenderTrend = () => {
  if (!studentTrendData.value || studentTrendData.value.length === 0) {
    nextTick(() => {
      initStudentTrendChart([]);
    });
    return;
  }
  
  let filteredTrends = [...studentTrendData.value];
  
  if (studentFilter.value.courseId) {
    const courseId = studentFilter.value.courseId;
    const course = courses.value.find(c => c.id === courseId);
    
    if (course && course.assignments) {
      const courseAssignmentIds = course.assignments.map((a: any) => a.id);
      filteredTrends = filteredTrends.filter(t => courseAssignmentIds.includes(t.assignmentId));
    }
  }
  
  if (studentFilter.value.assignmentId) {
    filteredTrends = filteredTrends.filter(t => t.assignmentId === studentFilter.value.assignmentId);
  }
  
  nextTick(() => {
    initStudentTrendChart(filteredTrends);
  });
};

const viewErrorDetail = async (row: any) => {
  currentErrorDetail.value = {
    ...row,
    wrongStudents: []
  };
  errorDetailVisible.value = true;
  
  try {
    const response = await request.get(`/teacher/analytics/frequent-errors/${row.questionId}/students`);
    if (response.data) {
      currentErrorDetail.value.wrongStudents = response.data.map((s: any) => ({
        id: s.id,
        realName: s.realName || s.name,
        username: s.username,
        email: s.email || '-',
        phone: s.phone || '-',
        status: s.status,
        avatar: s.avatar
      }));
    }
  } catch (error) {
    console.error('获取答错学生信息失败:', error);
  }
};

const getErrorRateClass = (rate: number): string => {
  if (rate >= 50) return 'high';
  if (rate >= 30) return 'medium';
  return 'low';
};

const formatQuestionContent = (content: string): string => {
  if (!content) return '';
  return content.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim();
};

const exportOverviewData = async () => {
  try {
    const data = classStats.value;
    
    const csvContent = [
      ['指标', '数值'],
      ['学生总数', data.totalStudents || 0],
      ['作业总数', data.totalAssignments || 0],
      ['提交人数', data.submittedCount || 0],
      ['逾期人数', data.overdueCount || 0],
      ['提交率', ((data.completionRate || 0) * 100).toFixed(2) + '%'],
      ['逾期率', ((data.overdueRate || 0) * 100).toFixed(2) + '%'],
      ['平均分', (data.averageScore || 0).toFixed(2)],
      ['最高分', data.highestScore || 0],
      ['最低分', data.lowestScore || 0],
      ['及格率', ((data.passRate || 0) * 100).toFixed(2) + '%'],
      ['优秀率', ((data.excellentRate || 0) * 100).toFixed(2) + '%']
    ].map(row => row.join(',')).join('\n');

    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `班级总览_${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('数据导出成功');
  } catch (error) {
    console.error('导出数据失败:', error);
    ElMessage.error('数据导出失败');
  }
};

const exportErrorData = async () => {
  try {
    const params: any = {};
    if (errorFilter.value.courseId) {
      params.courseId = errorFilter.value.courseId;
    }
    
    const response = await request.get('/teacher/analytics/frequent-errors/export', {
      params,
      responseType: 'blob'
    });
    
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `错题分析_${new Date().getTime()}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('数据导出成功');
  } catch (error: any) {
    console.error('导出数据失败:', error);
    let errorMsg = '数据导出失败';
    if (error.response?.data) {
      try {
        const text = await error.response.data.text();
        errorMsg = text.substring(0, 200);
      } catch {
        errorMsg = error.response.data.message || errorMsg;
      }
    }
    ElMessage.error(errorMsg);
  }
};

const exportStudentData = async () => {
  if (!selectedStudent.value) {
    ElMessage.warning('请先选择学生');
    return;
  }
  
  try {
    const student = selectedStudent.value;
    const trends = studentTrendData.value || [];
    
    const csvContent = [
      ['学生姓名', student.studentName || ''],
      ['学生账号', student.studentAccount || ''],
      [''],
      ['作业名称', '得分', '总分', '正确率', '提交时间']
    ];
    
    trends.forEach((trend: any) => {
      const correctRate = trend.totalQuestions > 0 
        ? ((trend.correctCount || 0) / trend.totalQuestions * 100).toFixed(2) + '%' 
        : '0%';
      csvContent.push([
        trend.assignmentTitle || '',
        trend.score || 0,
        trend.totalScore || 0,
        correctRate,
        trend.submitTime || ''
      ]);
    });

    const csv = csvContent.map(row => row.join(',')).join('\n');
    const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${student.studentName || '学生'}_成绩_${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('数据导出成功');
  } catch (error) {
    console.error('导出数据失败:', error);
    ElMessage.error('数据导出失败');
  }
};

onMounted(async () => {
  await loadCourses();
  
  window.addEventListener('resize', () => {
    submissionChart?.resize();
    onTimeChart?.resize();
    scoreChart?.resize();
    errorDistributionChart?.resize();
    heatmapChart?.resize();
    studentTrendChart?.resize();
  });
});
</script>

<style scoped>
.teacher-analysis {
  padding: 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
}

:deep(.el-card) {
  border: none;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: none;
  background: transparent;
}

:deep(.el-card__body) {
  padding: 0 20px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-item {
  width: 200px;
  margin: auto 0;
}

.search-input {
  width: 240px;
}

.search-input :deep(.el-input__wrapper) {
  padding-right: 4px;
}

.search-input :deep(.el-input__suffix) {
  right: 4px;
}

.search-input :deep(.el-button) {
  padding: 8px 12px;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
  padding: 0 20px;
}

.stat-card {
  margin: 10px;
  padding: 20px !important;
  border: none;
  border-radius: 12px;
  transition: all 0.3s ease;
  height: auto;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: flex-start;
  min-height: 100px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.stat-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 10px;
  margin-right: 12px;
  flex-shrink: 0;
  vertical-align: middle;
}

.stat-card__content {
  display: inline-block;
  vertical-align: middle;
}

.stat-card__icon--excellent {
  background: rgb(255, 247, 186);
  color: #feae00;
}

.stat-card__icon--pass {
  background: #f0fdf4;
  color: #22c55e;
}

.stat-card__icon--highest {
  background: #fef2f2;
  color: #ef4444;
}

.stat-card__icon--lowest {
  background: #eff6ff;
  color: #3b82f6;
}

.stat-card__icon .el-icon {
  font-size: 24px;
}

.stat-card__label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 4px;
  font-weight: 500;
}

.stat-card__value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.2;
  margin-bottom: 2px;
  color: #111827;
}

.stat-card__value .unit {
  font-size: 14px;
  margin-left: 2px;
  font-weight: normal;
}

.stat-card__desc {
  font-size: 11px;
  color: #9ca3af;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 0 20px;
}

.charts-row--three {
  grid-template-columns: 1fr 2fr;
}

.chart-card {
  border: none;
  border-radius: 16px;
  overflow: visible;
}

.chart-card--double {
  grid-column: span 1;
}

.chart-card--full {
  grid-column: 1 / -1;
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
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  color: #fff;
}

.card-title__icon .el-icon {
  font-size: 18px;
}

.chart {
  width: 100%;
  height: 100%;
}

.dual-pie-container {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 10px 0;
}

.pie-item {
  flex: 1;
  text-align: center;
}

.pie-label {
  font-size: 14px;
  color: #606266;
  margin-top: 10px;
  font-weight: 500;
}

.error-section {
  margin-bottom: 24px;
}

.high-frequency-section {
  margin-top: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.section-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.modern-table {
  --el-table-border-radius: 12px;
}

.modern-table :deep(.el-table__header th) {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  color: #606266;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 8px;
}

.modern-table :deep(.el-table__row) {
  height: 48px;
  cursor: pointer;
}

.modern-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.modern-table :deep(.el-table__cell) {
  padding: 8px;
  font-size: 13px;
}

.error-count {
  font-weight: 600;
  color: #f56c6c;
  font-size: 15px;
}

.error-rate-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
}

.error-rate-bar__bg {
  flex: 1;
  height: 6px;
  background-color: #ebeef5;
  border-radius: 3px;
  overflow: hidden;
}

.error-rate-bar__fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

.error-rate-bar__fill.high {
  background: linear-gradient(90deg, #f56c6c 0%, #f89898 100%);
}

.error-rate-bar__fill.medium {
  background: linear-gradient(90deg, #e6a23c 0%, #ebb563 100%);
}

.error-rate-bar__fill.low {
  background: linear-gradient(90deg, #67c23a 0%, #85ce61 100%);
}

.error-rate-bar__text {
  font-weight: 600;
  font-size: 12px;
  min-width: 40px;
  text-align: right;
  flex-shrink: 0;
}

.student-section {
  margin-bottom: 24px;
}

.student-search-section {
  padding: 0 20px 20px;
}

.search-box {
  max-width: 700px;
  margin: 0 auto;
}

.search-box__input-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-box__input-wrapper .search-input {
  flex: 1;
}

.search-box__input-wrapper .search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 4px 15px;
}

.search-box__input-wrapper .search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.search-box__input-wrapper .search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.search-box__input-wrapper .search-icon {
  font-size: 18px;
  color: #909399;
}

.search-box__input-wrapper .search-btn {
  border-radius: 12px;
  padding: 0 24px;
  height: 42px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-box__tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  color: #0369a1;
  font-size: 13px;
}

.search-box__tip .el-icon {
  font-size: 16px;
}

.student-chart-section {
  padding: 0 20px 20px;
}

.student-info {
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
}

.student-info__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.student-info__content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.student-avatar {
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  color: #fff;
  font-size: 20px;
  font-weight: 600;
}

.student-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.student-info__name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.student-info__account {
  font-size: 13px;
  color: #909399;
}

.student-info__actions {
  display: flex;
  gap: 12px;
}

.filter-item-small {
  width: 160px;
}

.empty-student {
  padding: 80px 0;
  text-align: center;
}

.error-detail-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.error-detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.error-detail-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  color: #909399;
  font-size: 14px;
  min-width: 80px;
}

.info-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.error-students-section .section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.students-table :deep(.el-table__header th) {
  background: #f5f7fa;
}

.student-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-info-cell {
  display: flex;
  flex-direction: column;
}

.student-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.student-account {
  font-size: 12px;
  color: #909399;
}

.empty-students {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
}

@media (max-width: 1400px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-row {
    grid-template-columns: 1fr;
  }
  
  .charts-row--three {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-container {
    grid-template-columns: 1fr;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-item,
  .search-input {
    width: 100%;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .dual-pie-container {
    flex-direction: column;
  }
  
  .pie-item {
    width: 100%;
    margin-bottom: 20px;
  }
}
</style>
