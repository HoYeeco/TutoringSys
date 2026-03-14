<template>
  <div class="admin-monitoring">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>系统监控</span>
        </div>
      </template>
    </el-card>

    <el-tabs v-model="activeTab">
      <!-- 作业监控 -->
      <el-tab-pane label="作业监控" name="assignment">
        <div class="filter-form">
          <el-form :inline="true" :model="assignmentFilter" class="demo-form-inline">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="assignmentFilter.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
              />
            </el-form-item>
            <el-form-item label="课程">
              <el-select v-model="assignmentFilter.courseId" placeholder="选择课程">
                <el-option label="全部" value="" />
                <el-option
                  v-for="course in courses"
                  :key="course.id"
                  :label="course.name"
                  :value="course.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="批改状态">
              <el-select v-model="assignmentFilter.status" placeholder="选择状态">
                <el-option label="全部" value="" />
                <el-option label="待批改" value="pending" />
                <el-option label="已批改" value="completed" />
                <el-option label="异常" value="error" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleAssignmentSearch">查询</el-button>
              <el-button @click="resetAssignmentFilter">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-card shadow="never" class="mt-4">
          <el-table :data="assignmentSubmissions" style="width: 100%">
            <el-table-column prop="id" label="提交ID" width="100" />
            <el-table-column prop="studentName" label="学生" width="120" />
            <el-table-column prop="courseName" label="课程" width="150" />
            <el-table-column prop="assignmentName" label="作业" width="200" />
            <el-table-column prop="submitTime" label="提交时间" width="180" />
            <el-table-column prop="status" label="批改状态" width="120">
              <template #default="scope">
                <el-tag
                  :type="{
                    pending: 'warning',
                    completed: 'success',
                    error: 'danger'
                  }[scope.row.status]"
                >
                  {{ {
                    pending: '待批改',
                    completed: '已批改',
                    error: '异常'
                  }[scope.row.status] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="llmCallId" label="AI调用ID" width="200" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" @click="viewSubmissionDetail(scope.row.id)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="assignmentPage.page"
              v-model:page-size="assignmentPage.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="assignmentPage.total"
              @size-change="handleAssignmentSizeChange"
              @current-change="handleAssignmentCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 性能监控 -->
      <el-tab-pane label="性能监控" name="performance">
        <div class="performance-section">
          <h2 class="section-title">API统计</h2>
          <el-card shadow="never" class="mt-2">
            <div ref="apiStatsChartRef" class="chart" style="height: 300px"></div>
          </el-card>

          <h2 class="section-title mt-4">大模型监控</h2>
          <div class="stats-grid">
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-value">{{ llmStats.tokenConsumption }}</div>
                <div class="stat-label">Token消耗</div>
              </div>
              <div class="stat-icon">
                <el-icon class="icon-large"><ChatLineSquare /></el-icon>
              </div>
            </el-card>
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-value">{{ llmStats.costEstimate }} 元</div>
                <div class="stat-label">成本预估</div>
              </div>
              <div class="stat-icon">
                <el-icon class="icon-large"><Money /></el-icon>
              </div>
            </el-card>
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-value">{{ llmStats.failureCount }}</div>
                <div class="stat-label">失败次数</div>
              </div>
              <div class="stat-icon">
                <el-icon class="icon-large"><Close /></el-icon>
              </div>
            </el-card>
          </div>

          <h2 class="section-title mt-4">Redis监控</h2>
          <div class="stats-grid">
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-value">{{ redisStats.hitRate }}%</div>
                <div class="stat-label">命中率</div>
              </div>
              <div class="stat-icon">
                <el-icon class="icon-large"><Check /></el-icon>
              </div>
            </el-card>
            <el-card shadow="never" class="stat-card">
              <div class="stat-content">
                <div class="stat-value">{{ redisStats.memoryUsage }}</div>
                <div class="stat-label">内存使用</div>
              </div>
              <div class="stat-icon">
                <el-icon class="icon-large"><HardDrive /></el-icon>
              </div>
            </el-card>
          </div>
        </div>
      </el-tab-pane>

      <!-- 日志审计 -->
      <el-tab-pane label="日志审计" name="logs">
        <el-card shadow="never" class="mt-2">
          <template #header>
            <span>操作日志</span>
          </template>
          <el-table :data="operationLogs" style="width: 100%">
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="user" label="操作用户" width="150" />
            <el-table-column prop="action" label="操作内容" />
            <el-table-column prop="result" label="结果" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.result === '成功' ? 'success' : 'danger'">
                  {{ scope.row.result }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" class="mt-4">
          <template #header>
            <span>登录日志</span>
          </template>
          <el-table :data="loginLogs" style="width: 100%">
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="user" label="用户" width="150" />
            <el-table-column prop="ip" label="IP地址" width="150" />
            <el-table-column prop="result" label="结果" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.result === '成功' ? 'success' : 'danger'">
                  {{ scope.row.result }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card shadow="never" class="mt-4">
          <template #header>
            <span>异常日志</span>
          </template>
          <el-table :data="errorLogs" style="width: 100%">
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="api" label="接口" width="200" />
            <el-table-column prop="error" label="错误信息" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" @click="viewErrorDetail(scope.row.id)">
                  查看堆栈
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ChatLineSquare, Money, Close, Check, HardDrive } from '@element-plus/icons-vue';
import request from '@/utils/request';
import * as echarts from 'echarts';

const activeTab = ref('assignment');

// 课程列表
const courses = ref([]);

// 作业监控
const assignmentFilter = ref({
  dateRange: null,
  courseId: '',
  status: ''
});

const assignmentSubmissions = ref([]);
const assignmentPage = ref({
  page: 1,
  pageSize: 10,
  total: 0
});

// 性能监控
const apiStatsChartRef = ref<HTMLElement | null>(null);
let apiStatsChart: echarts.ECharts | null = null;

const llmStats = ref({
  tokenConsumption: 12500,
  costEstimate: 15.6,
  failureCount: 3
});

const redisStats = ref({
  hitRate: 92.5,
  memoryUsage: '450MB / 1GB'
});

// 日志审计
const operationLogs = ref([
  { time: '2026-03-14 10:30:25', user: 'admin', action: '发布作业《数据结构作业1》', result: '成功' },
  { time: '2026-03-14 09:15:42', user: 'teacher1', action: '批改作业《算法作业2》', result: '成功' },
  { time: '2026-03-13 16:45:10', user: 'admin', action: '添加课程《计算机网络》', result: '成功' },
  { time: '2026-03-13 14:20:33', user: 'student1', action: '提交作业《计算机导论作业》', result: '成功' },
  { time: '2026-03-13 11:05:17', user: 'admin', action: '修改系统配置', result: '成功' }
]);

const loginLogs = ref([
  { time: '2026-03-14 10:00:15', user: 'admin', ip: '192.168.1.100', result: '成功' },
  { time: '2026-03-14 09:30:45', user: 'teacher1', ip: '192.168.1.101', result: '成功' },
  { time: '2026-03-14 09:15:20', user: 'student1', ip: '192.168.1.102', result: '成功' },
  { time: '2026-03-13 18:45:30', user: 'student2', ip: '192.168.1.103', result: '失败' },
  { time: '2026-03-13 16:20:10', user: 'admin', ip: '192.168.1.100', result: '成功' }
]);

const errorLogs = ref([
  { time: '2026-03-14 11:30:25', api: '/api/llm/grade', error: 'AI模型调用超时' },
  { time: '2026-03-14 10:15:42', api: '/api/student/submit', error: '文件上传失败' },
  { time: '2026-03-13 15:45:10', api: '/api/teacher/assignments', error: '数据库连接异常' }
]);

// 获取课程列表
const getCourses = async () => {
  try {
    const response = await request.get('/admin/courses', {
      params: {
        page: 1,
        size: 100
      }
    });
    courses.value = response.data.records;
  } catch (error) {
    // 模拟数据
    courses.value = [
      { id: 1, name: '计算机导论' },
      { id: 2, name: '数据结构' },
      { id: 3, name: '算法设计与分析' }
    ];
  }
};

// 获取作业提交记录
const getAssignmentSubmissions = async () => {
  try {
    const response = await request.get('/admin/monitoring/assignments', {
      params: {
        page: assignmentPage.value.page,
        pageSize: assignmentPage.value.pageSize,
        startDate: assignmentFilter.value.dateRange?.[0],
        endDate: assignmentFilter.value.dateRange?.[1],
        courseId: assignmentFilter.value.courseId,
        status: assignmentFilter.value.status
      }
    });
    assignmentSubmissions.value = response.data.records;
    assignmentPage.value.total = response.data.total;
  } catch (error) {
    // 模拟数据
    assignmentSubmissions.value = [
      {
        id: 1,
        studentName: '张三',
        courseName: '数据结构',
        assignmentName: '数据结构作业1',
        submitTime: '2026-03-14 10:30:25',
        status: 'completed',
        llmCallId: 'llm-123456'
      },
      {
        id: 2,
        studentName: '李四',
        courseName: '算法设计与分析',
        assignmentName: '算法作业1',
        submitTime: '2026-03-14 09:15:42',
        status: 'completed',
        llmCallId: 'llm-123457'
      },
      {
        id: 3,
        studentName: '王五',
        courseName: '计算机导论',
        assignmentName: '计算机导论作业1',
        submitTime: '2026-03-13 16:45:10',
        status: 'pending',
        llmCallId: ''
      },
      {
        id: 4,
        studentName: '赵六',
        courseName: '数据结构',
        assignmentName: '数据结构作业2',
        submitTime: '2026-03-13 14:20:33',
        status: 'error',
        llmCallId: 'llm-123458'
      }
    ];
    assignmentPage.value.total = assignmentSubmissions.value.length;
  }
};

// 初始化API统计图表
const initApiStatsChart = () => {
  if (apiStatsChartRef.value) {
    apiStatsChart = echarts.init(apiStatsChartRef.value);
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
        data: ['/api/student/assignments', '/api/teacher/assignments', '/api/llm/grade', '/api/auth/login', '/api/admin/users']
      },
      yAxis: {
        type: 'value',
        name: '调用次数'
      },
      series: [
        {
          name: '调用次数',
          type: 'bar',
          data: [120, 80, 50, 60, 30],
          itemStyle: {
            color: '#409eff'
          }
        },
        {
          name: '平均响应时间(ms)',
          type: 'line',
          yAxisIndex: 1,
          data: [120, 150, 2000, 80, 100],
          itemStyle: {
            color: '#e6a23c'
          }
        }
      ],
      yAxis: [
        {
          type: 'value',
          name: '调用次数',
          position: 'left'
        },
        {
          type: 'value',
          name: '响应时间(ms)',
          position: 'right',
          axisLabel: {
            formatter: '{value} ms'
          }
        }
      ]
    };
    apiStatsChart.setOption(option);
  }
};

// 处理作业搜索
const handleAssignmentSearch = () => {
  assignmentPage.value.page = 1;
  getAssignmentSubmissions();
};

// 重置作业筛选
const resetAssignmentFilter = () => {
  assignmentFilter.value = {
    dateRange: null,
    courseId: '',
    status: ''
  };
  assignmentPage.value.page = 1;
  getAssignmentSubmissions();
};

// 作业分页处理
const handleAssignmentSizeChange = (size: number) => {
  assignmentPage.value.pageSize = size;
  assignmentPage.value.page = 1;
  getAssignmentSubmissions();
};

const handleAssignmentCurrentChange = (current: number) => {
  assignmentPage.value.page = current;
  getAssignmentSubmissions();
};

// 查看提交详情
const viewSubmissionDetail = (id: number) => {
  ElMessage.info('查看提交详情功能开发中');
};

// 查看错误详情
const viewErrorDetail = (id: number) => {
  ElMessageBox.alert(
    '错误堆栈信息：\n' +
    'Error: AI模型调用超时\n' +
    '    at /api/llm/grade (line 45)\n' +
    '    at async handleGrade (line 123)\n' +
    '    at async router.post (line 56)',
    '错误详情',
    {
      confirmButtonText: '确定'
    }
  );
};

// 初始化
onMounted(() => {
  getCourses();
  getAssignmentSubmissions();
  nextTick(() => {
    initApiStatsChart();
  });
  
  // 监听窗口大小变化，自适应图表
  window.addEventListener('resize', () => {
    apiStatsChart?.resize();
  });
});
</script>

<style scoped>
.admin-monitoring {
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

.filter-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 20px 0 10px;
}

.performance-section {
  padding: 20px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
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

@media (max-width: 768px) {
  .el-form--inline {
    flex-direction: column;
    align-items: stretch;
  }
  
  .el-form-item {
    margin-bottom: 15px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .pagination-container {
    justify-content: center;
  }
}
</style>