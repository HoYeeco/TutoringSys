<template>
  <div class="teacher-analysis">
    <!-- 5.5.1 总览看板 -->
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
          <div ref="completionChartRef" class="chart" style="height: 320px"></div>
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

    <!-- 5.5.2 错题分析 -->
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
              placeholder="搜索题目关键词"
              class="search-input"
              clearable
              @keyup.enter="loadHighFrequencyErrors"
            >
              <template #suffix>
                <el-button type="primary" @click="loadHighFrequencyErrors">
                  <el-icon><Search /></el-icon>
                </el-button>
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
          <div ref="errorDistributionChartRef" class="chart" style="height: 360px"></div>
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
              @size-change="loadHighFrequencyErrors"
              @current-change="loadHighFrequencyErrors"
            />
          </div>
        </div>

        <el-table :data="highFrequencyErrors" style="width: 100%" class="modern-table" @row-click="viewErrorDetail">
          <el-table-column prop="courseName" label="所属课程" min-width="150" show-overflow-tooltip />
          <el-table-column prop="questionContent" label="题目" min-width="300" show-overflow-tooltip />
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

    <!-- 5.5.3 学生个体分析 -->
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
        <el-input
          v-model="studentSearch"
          placeholder="输入学生姓名或账号搜索"
          class="search-input-large"
          clearable
          @keyup.enter="searchStudent"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #suffix>
            <el-button type="primary" @click="searchStudent">搜索</el-button>
          </template>
        </el-input>
      </div>

      <div v-if="selectedStudent" class="student-chart-section">
        <div class="student-info">
          <div class="student-info__header">
            <div class="student-info__content">
              <span class="student-info__name">{{ selectedStudent.studentName }}</span>
              <span class="student-info__account">{{ selectedStudent.studentAccount }}</span>
            </div>
            <div class="student-info__actions">
              <el-select v-model="filterForm.courseId" placeholder="选择课程" class="filter-item-small" clearable @change="loadStudentTrendData">
                <el-option
                  v-for="course in courses"
                  :key="course.id"
                  :label="course.name"
                  :value="course.id"
                />
              </el-select>
            </div>
          </div>
        </div>
        <div ref="studentTrendChartRef" class="chart" style="height: 400px"></div>
      </div>

      <div v-else class="empty-student">
        <el-empty description="请搜索学生进行分析" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  TrendCharts, CircleCheck, Trophy, Grid, Medal, 
  Star, Search, PieChart, Histogram, User 
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

const studentSearch = ref('');
const selectedStudent = ref<any>(null);

const courses = ref<any[]>([]);
const assignments = ref<any[]>([]);
const classStats = ref({
  excellentRate: 0,
  passRate: 0,
  highestScore: 0,
  lowestScore: 0,
  completionRate: 0,
  totalStudents: 0,
  submittedCount: 0
});

const highFrequencyErrors = ref<any[]>([]);
const errorPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 图表引用
const completionChartRef = ref<HTMLElement | null>(null);
const scoreChartRef = ref<HTMLElement | null>(null);
const errorDistributionChartRef = ref<HTMLElement | null>(null);
const studentTrendChartRef = ref<HTMLElement | null>(null);

// 图表实例
let completionChart: echarts.ECharts | null = null;
let scoreChart: echarts.ECharts | null = null;
let errorDistributionChart: echarts.ECharts | null = null;
let studentTrendChart: echarts.ECharts | null = null;

// 获取课程列表
const loadCourses = async () => {
  try {
    const response = await request.get('/teacher/courses');
    courses.value = response.data?.records || response.data || [];
    if (courses.value.length > 0 && !filterForm.value.courseId) {
      filterForm.value.courseId = courses.value[0].id;
      errorFilter.value.courseId = courses.value[0].id;
      await loadAssignments();
      await loadAllStats();
    }
  } catch (error) {
    console.error('加载课程列表失败:', error);
  }
};

// 课程变化处理
const onCourseChange = async () => {
  filterForm.value.assignmentId = undefined;
  errorFilter.value.courseId = filterForm.value.courseId;
  await loadAssignments();
  await loadAllStats();
  await loadHighFrequencyErrors();
};

// 错题筛选变化
const onErrorFilterChange = async () => {
  errorPagination.value.currentPage = 1;
  await loadHighFrequencyErrors();
};

// 获取作业列表
const loadAssignments = async () => {
  if (!filterForm.value.courseId) return;
  try {
    const response = await request.get('/teacher/assignments', {
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

// 获取班级总览数据
const loadClassOverview = async () => {
  if (!filterForm.value.courseId) {
    // 默认加载所有课程数据
    classStats.value = {
      excellentRate: 0,
      passRate: 0,
      highestScore: 0,
      lowestScore: 0,
      completionRate: 0,
      totalStudents: 0,
      submittedCount: 0
    };
    return;
  }
  try {
    const response = await request.get('/teacher/analysis/overview', {
      params: {
        courseId: filterForm.value.courseId
      }
    });
    const data = response.data;
    classStats.value = {
      excellentRate: data.excellentRate || 0,
      passRate: data.passRate || 0,
      highestScore: data.highestScore || 0,
      lowestScore: data.lowestScore || 0,
      completionRate: data.completionRate || 0,
      totalStudents: data.totalStudents || 0,
      submittedCount: data.submittedCount || 0
    };
  } catch (error) {
    console.error('加载班级总览失败:', error);
  }
};

// 获取成绩分布数据
const loadScoreDistribution = async () => {
  if (!filterForm.value.courseId) return [];
  try {
    const response = await request.get('/teacher/analysis/score-distribution', {
      params: {
        courseId: filterForm.value.courseId,
        assignmentId: filterForm.value.assignmentId || undefined
      }
    });
    return response.data || [];
  } catch (error) {
    console.error('加载成绩分布失败:', error);
    return [];
  }
};

// 获取错题分布数据
const loadErrorDistribution = async () => {
  if (!filterForm.value.courseId) return;
  try {
    const response = await request.get('/teacher/analysis/error-distribution', {
      params: {
        courseId: filterForm.value.courseId,
        assignmentId: filterForm.value.assignmentId || undefined
      }
    });
    return response.data || [];
  } catch (error) {
    console.error('加载错题分布失败:', error);
    return [];
  }
};

// 加载高频错题
const loadHighFrequencyErrors = async () => {
  try {
    const response = await request.get('/teacher/analysis/high-frequency-errors', {
      params: {
        courseId: errorFilter.value.courseId || undefined,
        keyword: errorFilter.value.keyword || undefined,
        page: errorPagination.value.currentPage,
        size: errorPagination.value.pageSize
      }
    });
    highFrequencyErrors.value = response.data?.records || [];
    errorPagination.value.total = response.data?.total || 0;
  } catch (error) {
    console.error('加载高频错题失败:', error);
    highFrequencyErrors.value = [];
    errorPagination.value.total = 0;
  }
};

// 加载所有统计数据
const loadAllStats = async () => {
  await loadClassOverview();
  
  // 加载完成率图表
  if (classStats.value.completionRate >= 0) {
    nextTick(() => {
      initCompletionChart(classStats.value.completionRate);
    });
  }
  
  // 加载成绩分布
  const scoreData = await loadScoreDistribution();
  if (scoreData.length > 0) {
    nextTick(() => {
      initScoreChart(scoreData);
    });
  }
  
  // 加载错题分布
  const errorData = await loadErrorDistribution();
  if (errorData.length > 0) {
    nextTick(() => {
      initErrorDistributionChart(errorData);
    });
  }
  
  // 加载高频错题
  await loadHighFrequencyErrors();
};

// 初始化作业完成率图表
const initCompletionChart = (completionRate: number) => {
  if (completionChartRef.value) {
    completionChart = echarts.init(completionChartRef.value);
    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c}人 ({d}%)'
      },
      series: [
        {
          name: '完成率',
          type: 'pie',
          radius: ['50%', '70%'],
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
              fontSize: '20',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            { 
              value: classStats.value.submittedCount, 
              name: '已提交',
              itemStyle: { color: '#67c23a' }
            },
            { 
              value: classStats.value.totalStudents - classStats.value.submittedCount, 
              name: '未提交',
              itemStyle: { color: '#f56c6c' }
            }
          ]
        }
      ]
    };
    completionChart.setOption(option);
  }
};

// 初始化成绩分布柱状图
const initScoreChart = (scoreDistribution: any[]) => {
  if (scoreChartRef.value) {
    scoreChart = echarts.init(scoreChartRef.value);
    
    const data = scoreDistribution.length > 0 ? scoreDistribution : [
      { scoreRange: '0-59', count: 0, students: [] },
      { scoreRange: '60-69', count: 0, students: [] },
      { scoreRange: '70-79', count: 0, students: [] },
      { scoreRange: '80-89', count: 0, students: [] },
      { scoreRange: '90-100', count: 0, students: [] }
    ];
    
    const option = {
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
    scoreChart.setOption(option);
  }
};

// 初始化错题分布图表
const initErrorDistributionChart = (errorDistribution: any[]) => {
  if (errorDistributionChartRef.value) {
    errorDistributionChart = echarts.init(errorDistributionChartRef.value);
    
    // 按错误次数排序，取前 20 个
    const sortedData = [...errorDistribution]
      .sort((a, b) => b.errorCount - a.errorCount)
      .slice(0, 20);
    
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '10%',
        bottom: '3%',
        top: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        name: '错误次数'
      },
      yAxis: {
        type: 'category',
        data: sortedData.map(item => {
          const content = item.questionContent || '';
          return content.length > 30 ? content.substring(0, 30) + '...' : content;
        }),
        axisLabel: {
          interval: 0,
          formatter: (value: string) => {
            return value.length > 20 ? value.substring(0, 20) + '...' : value;
          }
        }
      },
      series: [{
        name: '错误次数',
        type: 'bar',
        data: sortedData.map(item => item.errorCount),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#f56c6c' },
            { offset: 1, color: '#f89898' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        label: {
          show: true,
          position: 'right'
        }
      }]
    };
    errorDistributionChart.setOption(option);
  }
};

// 初始化学生趋势图表
const initStudentTrendChart = (trend: any[]) => {
  if (studentTrendChartRef.value) {
    studentTrendChart = echarts.init(studentTrendChartRef.value);
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
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
        data: trend.map(item => item.assignmentName || item.assignment),
        axisLabel: {
          interval: 0,
          rotate: 0,
          formatter: (value: string) => {
            return value.length > 10 ? value.substring(0, 10) + '...' : value;
          }
        }
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 100
      },
      series: [
        {
          name: '个人得分',
          type: 'line',
          data: trend.map(item => item.score || item.personalScore || 0),
          itemStyle: {
            color: '#409eff'
          },
          smooth: true,
          symbol: 'circle',
          symbolSize: 8
        },
        {
          name: '班级平均',
          type: 'line',
          data: trend.map(item => item.classAverage || item.avgScore || 0),
          itemStyle: {
            color: '#67c23a'
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

// 搜索学生
const searchStudent = async () => {
  if (!studentSearch.value) {
    ElMessage.warning('请输入学生姓名或账号');
    return;
  }
  
  try {
    const response = await request.get('/teacher/analysis/search-student', {
      params: {
        keyword: studentSearch.value
      }
    });
    
    const studentId = response.data?.id;
    if (studentId) {
      selectedStudent.value = {
        studentId: studentId,
        studentName: response.data.realName,
        studentAccount: response.data.username
      };
      await loadStudentTrendData();
    } else {
      ElMessage.warning('未找到该学生');
      selectedStudent.value = null;
    }
  } catch (error) {
    console.error('搜索学生失败:', error);
    ElMessage.error('搜索学生失败');
    selectedStudent.value = null;
  }
};

// 加载学生趋势数据
const loadStudentTrendData = async () => {
  if (!selectedStudent.value) return;
  
  try {
    const response = await request.get('/teacher/analysis/student-trend', {
      params: {
        studentId: selectedStudent.value.studentId,
        courseId: filterForm.value.courseId || undefined
      }
    });
    const trendData = response.data;
    if (trendData && trendData.trend) {
      nextTick(() => {
        initStudentTrendChart(trendData.trend);
      });
    }
  } catch (error) {
    console.error('加载学生趋势失败:', error);
  }
};

// 查看错题详情
const viewErrorDetail = (row: any) => {
  ElMessageBox.alert(
    `
    <div class="error-detail">
      <p><strong>所属课程：</strong>${row.courseName || '未知'}</p>
      <p><strong>题目内容：</strong>${row.questionContent || '未知'}</p>
      <p><strong>错误次数：</strong><span class="error-count">${row.errorCount}</span></p>
      <p><strong>错误率：</strong><span class="error-rate">${row.errorRate}%</span></p>
      <p><strong>提交人数：</strong>${row.submittedCount || 0}</p>
    </div>
    `,
    '错题详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  );
};

// 获取错误率等级
const getErrorRateClass = (rate: number): string => {
  if (rate >= 50) return 'high';
  if (rate >= 30) return 'medium';
  return 'low';
};

// 导出总览数据
const exportOverviewData = async () => {
  if (!filterForm.value.courseId) {
    ElMessage.warning('请先选择课程');
    return;
  }
  
  try {
    const response = await request.get('/teacher/analysis/export-overview', {
      params: {
        courseId: filterForm.value.courseId,
        assignmentId: filterForm.value.assignmentId || undefined
      },
      responseType: 'blob'
    });
    
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `学情总览_${new Date().getTime()}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('数据导出成功');
  } catch (error) {
    console.error('导出数据失败:', error);
    ElMessage.error('数据导出失败');
  }
};

// 导出错题数据
const exportErrorData = async () => {
  try {
    const response = await request.get('/teacher/analysis/export-errors', {
      params: {
        courseId: errorFilter.value.courseId || undefined,
        keyword: errorFilter.value.keyword || undefined,
        page: errorPagination.value.currentPage,
        size: errorPagination.value.pageSize
      },
      responseType: 'blob'
    });
    
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `错题分析_${new Date().getTime()}.xlsx`;
    link.click();
    window.URL.revokeObjectURL(url);
    
    ElMessage.success('数据导出成功');
  } catch (error) {
    console.error('导出数据失败:', error);
    ElMessage.error('数据导出失败');
  }
};

// 导出学生数据
const exportStudentData = async () => {
  if (!selectedStudent.value) {
    ElMessage.warning('请先选择学生');
    return;
  }
  
  try {
    const response = await request.get('/teacher/analysis/export-student', {
      params: {
        studentId: selectedStudent.value.studentId,
        courseId: filterForm.value.courseId || undefined
      },
      responseType: 'blob'
    });
    
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${selectedStudent.value.studentName}_学情分析_${new Date().getTime()}.xlsx`;
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
    completionChart?.resize();
    scoreChart?.resize();
    errorDistributionChart?.resize();
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

/* 通用卡片样式 */
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

/* 卡片头部 */
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  width: 180px;
}

.search-input {
  width: 240px;
}

/* 统计卡片 */
.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
  padding: 0 20px;
}

.stat-card {
  padding: 20px !important;
  border: none;
  border-radius: 12px;
  transition: all 0.3s ease;
  min-height: 120px;
  background: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
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
  background: #fff7ed;
  color: #f97316;
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

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  padding: 0 20px;
}

.chart-card {
  border: none;
  border-radius: 16px;
  overflow: visible;
}

.chart-card--full {
  grid-column: 1 / -1;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
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

.chart {
  width: 100%;
  height: 100%;
}

/* 错题分析区域 */
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

/* 现代化表格样式 */
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

/* 错误率进度条 */
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

/* 学生个体分析区域 */
.student-section {
  margin-bottom: 24px;
}

.student-search-section {
  padding: 0 20px 20px;
}

.search-input-large {
  max-width: 600px;
}

.student-chart-section {
  padding: 0 20px 20px;
}

.student-info {
  margin-bottom: 16px;
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

.student-info__name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.student-info__account {
  font-size: 14px;
  color: #909399;
}

.filter-item-small {
  width: 150px;
}

.empty-student {
  padding: 60px 0;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-row {
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
}

/* 错题详情样式 */
.error-detail p {
  margin: 12px 0;
  line-height: 1.6;
}

.error-detail strong {
  color: #606266;
}

.error-count {
  color: #f56c6c;
  font-weight: 600;
}

.error-rate {
  color: #e6a23c;
  font-weight: 600;
}
</style>
