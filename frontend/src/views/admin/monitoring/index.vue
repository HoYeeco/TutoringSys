<template>
  <div class="admin-monitoring">
    <!-- 系统监控板块 -->
    <el-card shadow="never" class="monitoring-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <span>系统监控</span>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="monitoring-tabs">
        <!-- 作业监控 -->
        <el-tab-pane name="assignment">
          <template #label>
            <span class="tab-label">
              <el-icon><Document /></el-icon>
              作业监控
            </span>
          </template>

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
                <el-select v-model="assignmentFilter.courseId" placeholder="选择课程" clearable @change="handleFilterChange">
                  <el-option
                    v-for="course in courses"
                    :key="course.id"
                    :label="course.name"
                    :value="course.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="批改状态">
                <el-select v-model="assignmentFilter.status" placeholder="选择状态" clearable @change="handleFilterChange">
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

          <el-table
            :data="assignmentSubmissions"
            style="width: 100%"
            v-loading="assignmentLoading"
            class="modern-table"
            :cell-style="{ textAlign: 'center' }"
            :header-cell-style="{ textAlign: 'center' }"
          >
            <el-table-column prop="studentName" label="学生" min-width="120" />
            <el-table-column prop="courseName" label="课程" min-width="280" />
            <el-table-column prop="assignmentName" label="作业" min-width="280" />
            <el-table-column label="提交时间" min-width="180">
              <template #default="scope">
                {{ formatDateTime(scope.row.submitTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="批改状态" min-width="120">
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
            <el-table-column prop="llmCallId" label="AI 调用 ID" min-width="160" />
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
        </el-tab-pane>

        <!-- 性能监控 -->
        <el-tab-pane name="performance">
          <template #label>
            <span class="tab-label">
              <el-icon><DataAnalysis /></el-icon>
              性能监控
            </span>
          </template>

          <div class="performance-section">
            <h4 class="sub-section-title">API 统计</h4>
            <div class="chart-container">
              <div ref="apiStatsChartRef" class="chart" style="height: 350px"></div>
            </div>

            <h4 class="sub-section-title">大模型监控</h4>
            <div class="stats-grid">
              <el-card shadow="never" class="stat-card">
                <div class="stat-content">
                  <div class="stat-value">{{ formatNumber(llmStats.totalTokens) }}</div>
                  <div class="stat-label">Token 消耗</div>
                </div>
                <div class="stat-icon">
                  <el-icon class="icon-large"><ChatLineSquare /></el-icon>
                </div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-content">
                  <div class="stat-value">{{ formatNumber(llmStats.failedCalls) }}</div>
                  <div class="stat-label">失败次数</div>
                </div>
                <div class="stat-icon">
                  <el-icon class="icon-large"><Close /></el-icon>
                </div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-content">
                  <div class="stat-value">{{ formatNumber(llmStats.totalCalls) }}</div>
                  <div class="stat-label">总调用次数</div>
                </div>
                <div class="stat-icon">
                  <el-icon class="icon-large"><DataAnalysis /></el-icon>
                </div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-content">
                  <div class="stat-value">{{ llmStats.successRate || 0 }}%</div>
                  <div class="stat-label">成功率</div>
                </div>
                <div class="stat-icon">
                  <el-icon class="icon-large"><Check /></el-icon>
                </div>
              </el-card>
            </div>

            <h4 class="sub-section-title">Redis 监控</h4>
            <div class="stats-grid">
              <el-card shadow="never" class="stat-card">
                <div class="stat-content">
                  <div class="stat-value">{{ redisStats.hitRate || 0 }}%</div>
                  <div class="stat-label">命中率</div>
                </div>
                <div class="stat-icon">
                  <el-icon class="icon-large"><Check /></el-icon>
                </div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-content">
                  <div class="stat-value">{{ redisStats.usedMemoryHuman || '-' }}</div>
                  <div class="stat-label">内存使用</div>
                </div>
                <div class="stat-icon">
                  <el-icon class="icon-large"><Monitor /></el-icon>
                </div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-content">
                  <div class="stat-value">{{ formatNumber(redisStats.dbSize) }}</div>
                  <div class="stat-label">缓存键数量</div>
                </div>
                <div class="stat-icon">
                  <el-icon class="icon-large"><Document /></el-icon>
                </div>
              </el-card>
              <el-card shadow="never" class="stat-card">
                <div class="stat-content">
                  <div class="stat-value">{{ formatNumber(redisStats.connectedClients) }}</div>
                  <div class="stat-label">连接客户端</div>
                </div>
                <div class="stat-icon">
                  <el-icon class="icon-large"><ChatLineSquare /></el-icon>
                </div>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 日志审计 -->
        <el-tab-pane name="logs">
          <template #label>
            <span class="tab-label">
              <el-icon><DocumentCopy /></el-icon>
              日志审计
            </span>
          </template>

          <div class="logs-section">
            <h4 class="sub-section-title">操作日志</h4>
            <el-table :data="operationLogs" style="width: 100%" v-loading="logsLoading">
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
          </div>

          <div class="logs-section">
            <h4 class="sub-section-title">登录日志</h4>
            <el-table :data="loginLogs" style="width: 100%" v-loading="loginLogsLoading">
              <el-table-column prop="time" label="时间" width="180" />
              <el-table-column prop="user" label="用户" width="150" />
              <el-table-column prop="ip" label="IP 地址"/>
              <el-table-column prop="result" label="登录结果" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.result === '成功' ? 'success' : 'danger'">
                    {{ scope.row.result }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="logs-section">
            <h4 class="sub-section-title">异常日志</h4>
            <template v-if="errorLogsLoading || errorLogs.length > 0">
              <el-table :data="errorLogs" style="width: 100%" v-loading="errorLogsLoading">
                <el-table-column prop="time" label="时间" width="180" />
                <el-table-column prop="api" label="接口" width="150" />
                <el-table-column prop="error" label="错误信息" />
              </el-table>
            </template>
            <el-empty v-else description="暂无异常日志" :image-size="80" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { ChatLineSquare, Money, Close, Check, Monitor, Document, DataAnalysis, DocumentCopy } from '@element-plus/icons-vue';
import request from '@/utils/request';
import * as echarts from 'echarts';
import { formatDateTime } from '@/utils/format';

const activeTab = ref('assignment');

const courses = ref([]);
const assignmentLoading = ref(false);
const logsLoading = ref(false);
const loginLogsLoading = ref(false);
const errorLogsLoading = ref(false);

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

const apiStatsChartRef = ref<HTMLElement | null>(null);
let apiStatsChart: echarts.ECharts | null = null;

const llmStats = ref({
  totalCalls: 0,
  successCalls: 0,
  failedCalls: 0,
  successRate: 0,
  totalPromptTokens: 0,
  totalCompletionTokens: 0,
  totalTokens: 0
});

const redisStats = ref({
  version: '',
  mode: '',
  uptimeInSeconds: 0,
  connectedClients: 0,
  totalConnectionsReceived: 0,
  totalCommandsProcessed: 0,
  keyspaceHits: 0,
  keyspaceMisses: 0,
  hitRate: 0,
  usedMemory: '',
  usedMemoryPeak: '',
  usedMemoryHuman: '',
  dbSize: 0
});

const operationLogs = ref([]);
const loginLogs = ref([]);
const errorLogs = ref([]);

const getCourses = async () => {
  try {
    const response = await request.get('/admin/courses', {
      params: { page: 1, size: 100 }
    });
    courses.value = response.data?.records || [];
  } catch (error) {
    console.error('获取课程列表失败:', error);
  }
};

const getAssignmentSubmissions = async () => {
  assignmentLoading.value = true;
  try {
    const params: any = {
      page: assignmentPage.value.page,
      size: assignmentPage.value.pageSize,
    };
    
    if (assignmentFilter.value.courseId) {
      params.courseId = assignmentFilter.value.courseId;
    }
    if (assignmentFilter.value.status) {
      params.reviewStatus = assignmentFilter.value.status === 'pending' ? 0 : 
                           assignmentFilter.value.status === 'completed' ? 1 : undefined;
    }
    
    const response = await request.get('/admin/submissions', { params });
    assignmentSubmissions.value = (response.data?.records || []).map((item: any) => ({
      ...item,
      assignmentName: item.assignmentTitle,
      status: item.reviewStatus === 0 ? 'pending' : 
              item.reviewStatus === 1 ? 'completed' : 'pending',
      llmCallId: item.submissionId || '-',
    }));
    assignmentPage.value.total = response.data?.total || 0;
  } catch (error) {
    console.error('获取作业提交记录失败:', error);
  } finally {
    assignmentLoading.value = false;
  }
};

const getOperationLogs = async () => {
  logsLoading.value = true;
  try {
    const response = await request.get('/admin/monitor/audit-logs', {
      params: { page: 1, size: 20 },
    });
    const logsData = response.data || { records: [] };
    operationLogs.value = (logsData.records || []).map((log: any) => ({
      time: log.operationTime || '-',
      user: log.operator || '-',
      action: log.operationContent || log.operationType || '-',
      result: log.success === 1 ? '成功' : '失败',
    }));
  } catch (error) {
    console.error('获取操作日志失败:', error);
    operationLogs.value = [];
  } finally {
    logsLoading.value = false;
  }
};

const getLoginLogs = async () => {
  loginLogsLoading.value = true;
  try {
    const response = await request.get('/admin/monitor/login-records', {
      params: { page: 1, size: 20 },
    });
    const logsData = response.data || { records: [] };
    loginLogs.value = (logsData.records || []).map((log: any) => ({
      time: log.loginTime || '-',
      user: log.realName || log.username || '-',
      ip: log.ipAddress || '-',
      result: log.success === 1 ? '成功' : '失败',
    }));
  } catch (error) {
    console.error('获取登录日志失败:', error);
    loginLogs.value = [];
  } finally {
    loginLogsLoading.value = false;
  }
};

const getErrorLogs = async () => {
  errorLogsLoading.value = true;
  try {
    errorLogs.value = [];
  } catch (error) {
    console.error('获取异常日志失败:', error);
    errorLogs.value = [];
  } finally {
    errorLogsLoading.value = false;
  }
};

const getLlmStats = async () => {
  try {
    const response = await request.get('/admin/monitor/llm');
    if (response.data) {
      llmStats.value = {
        totalCalls: response.data.totalCalls || 0,
        successCalls: response.data.successCalls || 0,
        failedCalls: response.data.failedCalls || 0,
        successRate: response.data.successRate || 0,
        totalPromptTokens: response.data.totalPromptTokens || 0,
        totalCompletionTokens: response.data.totalCompletionTokens || 0,
        totalTokens: response.data.totalTokens || 0
      };
    }
  } catch (error) {
    console.error('获取LLM统计失败:', error);
  }
};

const getRedisStats = async () => {
  try {
    const response = await request.get('/admin/monitor/redis');
    if (response.data) {
      redisStats.value = {
        version: response.data.version || '',
        mode: response.data.mode || '',
        uptimeInSeconds: response.data.uptimeInSeconds || 0,
        connectedClients: response.data.connectedClients || 0,
        totalConnectionsReceived: response.data.totalConnectionsReceived || 0,
        totalCommandsProcessed: response.data.totalCommandsProcessed || 0,
        keyspaceHits: response.data.keyspaceHits || 0,
        keyspaceMisses: response.data.keyspaceMisses || 0,
        hitRate: response.data.hitRate || 0,
        usedMemory: response.data.usedMemory || '',
        usedMemoryPeak: response.data.usedMemoryPeak || '',
        usedMemoryHuman: response.data.usedMemoryHuman || '',
        dbSize: response.data.dbSize || 0
      };
    }
  } catch (error) {
    console.error('获取Redis统计失败:', error);
  }
};

const initApiStatsChart = async () => {
  try {
    if (apiStatsChartRef.value && apiStatsChartRef.value.clientWidth > 0) {
      if (apiStatsChart) {
        apiStatsChart.dispose();
      }
      apiStatsChart = echarts.init(apiStatsChartRef.value);
      
      const response = await request.get('/admin/monitor/audit-logs', {
        params: { page: 1, size: 1000 },
      });
      
      const logs = response.data?.records || [];
      
      const last10Days: string[] = [];
      const callCounts: number[] = [];
      
      for (let i = 9; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        const dateStr = `${date.getMonth() + 1}/${date.getDate()}`;
        last10Days.push(dateStr);
        
        const dayStart = new Date(date);
        dayStart.setHours(0, 0, 0, 0);
        const dayEnd = new Date(date);
        dayEnd.setHours(23, 59, 59, 999);
        
        const count = logs.filter((log: any) => {
          const logTime = new Date(log.operationTime);
          return logTime >= dayStart && logTime <= dayEnd;
        }).length;
        
        callCounts.push(count);
      }
      
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
          data: last10Days,
          axisLabel: {
            fontSize: 12
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '调用次数',
            position: 'left',
            axisLabel: {
              fontSize: 12
            }
          }
        ],
        series: [
          {
            name: '调用次数',
            type: 'bar',
            data: callCounts,
            itemStyle: {
              color: '#409eff'
            },
            label: {
              show: true,
              position: 'top',
              fontSize: 12
            }
          }
        ]
      };
      apiStatsChart.setOption(option);
    }
  } catch (error) {
    console.error('初始化API统计图表失败:', error);
  }
};

const handleAssignmentSearch = () => {
  assignmentPage.value.page = 1;
  getAssignmentSubmissions();
};

const handleFilterChange = () => {
  assignmentPage.value.page = 1;
  getAssignmentSubmissions();
};

const resetAssignmentFilter = () => {
  assignmentFilter.value = {
    dateRange: null,
    courseId: '',
    status: ''
  };
  assignmentPage.value.page = 1;
  getAssignmentSubmissions();
};

const handleAssignmentSizeChange = (size: number) => {
  assignmentPage.value.pageSize = size;
  assignmentPage.value.page = 1;
  getAssignmentSubmissions();
};

const handleAssignmentCurrentChange = (current: number) => {
  assignmentPage.value.page = current;
  getAssignmentSubmissions();
};

const viewSubmissionDetail = (id: number) => {
  ElMessage.info('查看提交详情功能开发中');
};

const formatNumber = (num: number): string => {
  if (num === undefined || num === null) return '0';
  return num.toLocaleString('zh-CN');
};

onMounted(() => {
  getCourses();
  getAssignmentSubmissions();
  
  window.addEventListener('resize', () => {
    apiStatsChart?.resize();
  });
});

watch(activeTab, (newVal) => {
  if (newVal === 'performance') {
    nextTick(() => {
      setTimeout(() => {
        initApiStatsChart();
        getLlmStats();
        getRedisStats();
      }, 100);
    });
  } else if (newVal === 'logs') {
    getOperationLogs();
    getLoginLogs();
    getErrorLogs();
  }
});
</script>

<style scoped>
.admin-monitoring {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 84px);
}

/* 通用卡片样式 */
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

/* 监控子板块 */
.monitoring-subsection {
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid #ebeef5;
}

.monitoring-subsection:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

/* Tabs 样式 */
.monitoring-tabs {
  margin-top: 0;
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.tab-label .el-icon {
  font-size: 18px;
}

.sub-section-title {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  margin: 24px 0 16px;
  padding-left: 12px;
  border-left: 3px solid #409eff;
}

.sub-section-title:first-child {
  margin-top: 0;
}

/* 图表容器 */
.chart-container {
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 12px;
}

/* 日志区块 */
.logs-section {
  margin-top: 24px;
}

/* 筛选表单 */
.filter-form {
  margin-bottom: 24px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 24px;
}

.filter-form :deep(.el-form-item:last-child) {
  margin-right: 0;
}

.filter-form :deep(.el-select) {
  width: 180px;
}

/* 表格样式 */
.modern-table {
  width: 100%;
}

.modern-table :deep(.el-table__header th) {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  color: #606266;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 8px;
}

.modern-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.modern-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.modern-table :deep(.el-table__cell) {
  text-align: center;
}

/* 分页 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 状态标签 */
.status-tag {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

/* 详情对话框 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .filter-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .admin-monitoring {
    padding: 16px;
  }
  
  :deep(.el-tabs__item) {
    padding: 0 16px;
    font-size: 14px;
  }
}

.performance-section {
  padding: 20px 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin: 20px 16px 0;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 32px;
  min-height: 120px;
  box-sizing: border-box;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0;
}

.stat-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 24px;
  color: #409eff;
  flex-shrink: 0;
}

.icon-large {
  font-size: 48px;
  opacity: 0.6;
}

.chart {
  width: 100%;
  height: 100%;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
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