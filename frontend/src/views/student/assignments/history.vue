<template>
  <div class="student-history">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>作业历史记录</span>
        </div>
      </template>
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索作业名称或课程"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select v-model="courseId" placeholder="选择课程" clearable>
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="sortBy" placeholder="排序方式">
              <el-option label="按提交时间（最新）" value="submitTime_desc" />
              <el-option label="按提交时间（最早）" value="submitTime_asc" />
              <el-option label="按分数（最高）" value="score_desc" />
              <el-option label="按分数（最低）" value="score_asc" />
              <el-option label="按错题数量（最多）" value="errorCount_desc" />
              <el-option label="按错题数量（最少）" value="errorCount_asc" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="handleSearch">
              搜索
            </el-button>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <div class="timeline-container">
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in historyList"
            :key="item.id"
            :timestamp="formatDate(item.submitTime)"
            :type="getTimelineType(item.status)"
          >
            <el-card :body-style="{ padding: '0px' }">
              <div class="timeline-card">
                <div class="card-header">
                  <span class="assignment-title">{{ item.assignmentTitle }}</span>
                  <el-tag :type="getStatusType(item.status)">{{ item.statusText }}</el-tag>
                </div>
                <div class="card-body">
                  <div class="course-info">
                    <span class="label">课程：</span>
                    <span>{{ item.courseName }}</span>
                  </div>
                  <div class="score-info" v-if="item.score !== null">
                    <span class="label">得分：</span>
                    <span class="score">{{ item.score }}/{{ item.totalScore }}</span>
                  </div>
                  <div class="error-info" v-if="item.errorCount > 0">
                    <span class="label">错题数量：</span>
                    <span>{{ item.errorCount }}</span>
                  </div>
                  <div class="time-info">
                    <span class="label">发布时间：</span>
                    <span>{{ formatDate(item.publishTime) }}</span>
                  </div>
                  <div class="time-info">
                    <span class="label">截止时间：</span>
                    <span>{{ formatDate(item.deadline) }}</span>
                  </div>
                </div>
                <div class="card-footer">
                  <el-button size="small" type="primary" @click="viewReport(item.id)" v-if="item.status === 'graded'">
                    查看报告
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <div class="empty-state" v-else>
        <el-empty description="暂无历史记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const router = useRouter();

const searchKeyword = ref('');
const courseId = ref('');
const sortBy = ref('submitTime_desc');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

const courses = ref([
  { id: 1, name: '计算机导论' },
  { id: 2, name: '高等数学' },
  { id: 3, name: '数据结构' }
]);

const historyList = ref([
  {
    id: 1,
    assignmentTitle: '第一章 计算机基础作业',
    courseName: '计算机导论',
    status: 'graded',
    statusText: '已批改',
    score: 85,
    totalScore: 100,
    errorCount: 1,
    submitTime: '2026-03-10 14:30:00',
    publishTime: '2026-03-01 09:00:00',
    deadline: '2026-03-10 23:59:59'
  },
  {
    id: 2,
    assignmentTitle: '第二章 操作系统作业',
    courseName: '计算机导论',
    status: 'graded',
    statusText: '已批改',
    score: 92,
    totalScore: 100,
    errorCount: 0,
    submitTime: '2026-03-05 16:45:00',
    publishTime: '2026-02-25 10:00:00',
    deadline: '2026-03-05 23:59:59'
  },
  {
    id: 3,
    assignmentTitle: '第一章 函数作业',
    courseName: '高等数学',
    status: 'graded',
    statusText: '已批改',
    score: 78,
    totalScore: 100,
    errorCount: 2,
    submitTime: '2026-03-01 11:20:00',
    publishTime: '2026-02-20 09:30:00',
    deadline: '2026-03-01 23:59:59'
  }
]);

const getHistoryList = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get('/student/assignments/history', {
    //   params: {
    //     keyword: searchKeyword.value,
    //     courseId: courseId.value,
    //     sortBy: sortBy.value,
    //     page: currentPage.value,
    //     pageSize: pageSize.value
    //   }
    // });
    // return response.data;
    
    // 模拟数据
    return {
      list: historyList.value,
      total: historyList.value.length
    };
  } catch (error) {
    ElMessage.error('获取历史记录失败');
    return null;
  }
};

const { execute: fetchHistoryList } = useRequest(getHistoryList);

const getTimelineType = (status: string) => {
  switch (status) {
    case 'graded':
      return 'success';
    case 'submitted':
      return 'warning';
    default:
      return 'info';
  }
};

const getStatusType = (status: string) => {
  switch (status) {
    case 'graded':
      return 'success';
    case 'submitted':
      return 'warning';
    default:
      return 'info';
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  fetchHistoryList();
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchHistoryList();
};

const handleCurrentChange = (page: number) => {
  currentPage.value = page;
  fetchHistoryList();
};

const viewReport = (reportId: number) => {
  router.push(`/student/assignments/report/${reportId}`);
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchHistoryList().then((data: any) => {
    if (data) {
      historyList.value = data.list;
      total.value = data.total;
    }
  });
});
</script>

<style scoped>
.student-history {
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

.filter-section {
  margin-top: 16px;
}

.timeline-container {
  margin-top: 20px;
}

.timeline-card {
  padding: 16px;
}

.timeline-card .card-header {
  margin-bottom: 12px;
}

.assignment-title {
  font-weight: 500;
  font-size: 16px;
}

.card-body {
  margin-bottom: 12px;
}

.course-info,
.score-info,
.error-info,
.time-info {
  margin-bottom: 8px;
}

.label {
  color: #606266;
  margin-right: 8px;
}

.score {
  color: #409eff;
  font-weight: 500;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.pagination {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.empty-state {
  margin: 60px 0;
  text-align: center;
}
</style>