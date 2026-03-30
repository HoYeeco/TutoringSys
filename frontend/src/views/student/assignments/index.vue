<template>
  <div class="student-assignments">
    <el-card shadow="never" class="assignments-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Document /></el-icon>
            </div>
            <span>我的作业</span>
          </div>
        </div>
      </template>

      <div class="filter-bar">
        <el-select
          v-model="filterStatus"
          placeholder="按状态筛选"
          class="filter-item"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="待提交" value="pending" />
          <el-option label="待批改" value="submitted" />
          <el-option label="已批改" value="graded" />
          <el-option label="已逾期" value="overdue" />
        </el-select>
        <el-select
          v-model="filterCourse"
          placeholder="按课程筛选"
          class="filter-item"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="course.name"
            :value="course.id"
          />
        </el-select>
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
        <div class="sort-wrapper">
          <span class="sort-label">排序：</span>
          <el-select v-model="sortBy" placeholder="排序方式" class="sort-item">
            <el-option label="发布时间（最新）" value="createTime:desc" />
            <el-option label="发布时间（最早）" value="createTime:asc" />
            <el-option label="截止时间（最近）" value="deadline:asc" />
            <el-option label="截止时间（最远）" value="deadline:desc" />
          </el-select>
        </div>
      </div>

      <el-empty
        v-if="filteredAssignments.length === 0"
        description="暂无作业"
      />
      <div v-else class="assignment-list">
        <div
          v-for="assignment in filteredAssignments"
          :key="assignment.id"
          class="assignment-card"
          :class="assignment.status"
        >
          <div class="assignment-card__header">
            <div class="assignment-icon" :class="assignment.status">
              <el-icon v-if="assignment.status === 'pending'"><Edit /></el-icon>
              <el-icon v-else-if="assignment.status === 'submitted'"><Loading /></el-icon>
              <el-icon v-else-if="assignment.status === 'graded'"><CircleCheck /></el-icon>
              <el-icon v-else><Warning /></el-icon>
            </div>
            <div class="assignment-info">
              <div class="assignment-top">
                <h4 class="assignment-title">{{ assignment.title }}</h4>
                <el-tag
                  :type="getTagType(assignment.status)"
                  effect="light"
                  class="status-tag"
                >
                  {{ getStatusText(assignment.status) }}
                </el-tag>
              </div>
              <div class="assignment-meta">
                <span class="meta-item">
                  <el-icon><Notebook /></el-icon>
                  {{ assignment.courseName }}
                </span>
                <span class="meta-item" v-if="assignment.status === 'pending' || assignment.status === 'overdue'">
                  <el-icon><Clock /></el-icon>
                  <span :class="{ 'overdue-text': assignment.status === 'overdue' }">
                    截止：{{ formatDate(assignment.deadline) }}
                  </span>
                </span>
                <span class="meta-item score-item" v-if="assignment.status === 'graded'">
                  <el-icon><Trophy /></el-icon>
                  得分：<strong>{{ assignment.finalScore }}</strong> / {{ assignment.totalScore }}
                </span>
                <span class="meta-item" v-if="assignment.status === 'submitted'">
                  <el-icon class="loading-icon"><Loading /></el-icon>
                  正在批改中...
                </span>
              </div>
            </div>
          </div>

          <div class="assignment-card__footer">
            <el-button
              v-if="assignment.status === 'pending'"
              type="primary"
              size="small"
              @click="goToSubmit(assignment)"
            >
              <el-icon><Edit /></el-icon> 写作业
            </el-button>
            <el-button
              v-else-if="assignment.status === 'graded'"
              type="success"
              size="small"
              @click="goToReport(assignment)"
            >
              <el-icon><View /></el-icon> 批改详情
            </el-button>
            <el-button
              v-else-if="assignment.status === 'overdue'"
              type="info"
              size="small"
              disabled
            >
              <el-icon><Warning /></el-icon> 已逾期
            </el-button>
            <el-button
              v-else
              type="info"
              size="small"
              disabled
            >
              <el-icon><Loading /></el-icon> 批改中
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Search,
  Document,
  Notebook,
  Clock,
  Trophy,
  Loading,
  Edit,
  View,
  Warning,
  CircleCheck,
} from '@element-plus/icons-vue';
import request from '@/utils/request';

const router = useRouter();

const filterStatus = ref('');
const filterCourse = ref('');
const searchKeyword = ref('');
const sortBy = ref('deadline:asc');

const courses = ref([]);

const getCourses = async () => {
  try {
    const response = await request.get('/student/courses');
    courses.value = response.data || [];
  } catch (error) {
    console.error('获取课程列表失败:', error);
    courses.value = [];
  }
};

const assignments = ref([]);

const filteredAssignments = computed(() => {
  let result = [...assignments.value];

  if (filterStatus.value) {
    result = result.filter((item) => item.status === filterStatus.value);
  }

  if (filterCourse.value) {
    result = result.filter(
      (item) => item.courseId === Number(filterCourse.value),
    );
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter((item) => {
      return (
        item.title.toLowerCase().includes(keyword) ||
        item.courseName.toLowerCase().includes(keyword)
      );
    });
  }

  if (sortBy.value) {
    const [field, order] = sortBy.value.split(':');
    result.sort((a, b) => {
      let aValue = a[field];
      let bValue = b[field];

      if (field === 'deadline' || field === 'createTime') {
        aValue = new Date(aValue).getTime();
        bValue = new Date(bValue).getTime();
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

const getAssignments = async () => {
  try {
    const params: any = {
      page: 1,
      size: 100,
    };
    if (filterStatus.value) params.status = filterStatus.value;
    if (filterCourse.value) params.courseId = filterCourse.value;
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (sortBy.value) {
      const [field, order] = sortBy.value.split(':');
      params.sortBy = field;
      params.sortOrder = order;
    }

    const response = await request.get('/student/assignments/list', { params });
    assignments.value = response.data.records || [];
  } catch (error) {
    console.error('获取作业列表失败:', error);
    assignments.value = [];
  }
};

const goToSubmit = (assignment: any) => {
  router.push(`/student/assignments/${assignment.id}/submit`);
};

const goToReport = (assignment: any) => {
  router.push(`/student/assignments/${assignment.id}/detail`);
};

const getTagType = (status: string) => {
  switch (status) {
    case 'pending':
      return 'warning';
    case 'submitted':
      return 'info';
    case 'graded':
      return 'success';
    case 'overdue':
      return 'danger';
    default:
      return 'default';
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case 'pending':
      return '待提交';
    case 'submitted':
      return '待批改';
    case 'graded':
      return '已批改';
    case 'overdue':
      return '已逾期';
    default:
      return '未知';
  }
};

const formatDate = (date: string) => {
  if (!date) return '-';
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`;
};

onMounted(() => {
  getCourses();
  getAssignments();
});
</script>

<style scoped>
.student-assignments {
  padding: 24px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(4px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
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

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  flex-wrap: nowrap;
}

.filter-item {
  width: 140px;
  flex-shrink: 0;
}

.search-input {
  width: 220px;
  flex-shrink: 0;
}

.sort-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  flex-shrink: 0;
}

.sort-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.sort-item {
  width: 160px;
}

.assignment-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.assignment-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  padding: 20px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  min-height: 180px;
}

.assignment-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: transparent;
}

.assignment-card.pending {
  border-left: 4px solid #e6a23c;
}

.assignment-card.submitted {
  border-left: 4px solid #909399;
}

.assignment-card.graded {
  border-left: 4px solid #67c23a;
}

.assignment-card.overdue {
  border-left: 4px solid #f56c6c;
}

.assignment-card__header {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.assignment-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  flex-shrink: 0;
}

.assignment-icon .el-icon {
  font-size: 22px;
}

.assignment-icon.pending {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #e6a23c;
}

.assignment-icon.submitted {
  background: linear-gradient(135deg, #f4f4f5 0%, #e9e9eb 100%);
  color: #909399;
}

.assignment-icon.graded {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67c23a;
}

.assignment-icon.overdue {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #f56c6c;
}

.assignment-info {
  flex: 1;
  min-width: 0;
}

.assignment-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.assignment-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.status-tag {
  flex-shrink: 0;
}

.assignment-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.meta-item .el-icon {
  font-size: 14px;
}

.meta-item .overdue-text {
  color: #f56c6c;
  font-weight: 500;
}

.meta-item.score-item strong {
  color: #67c23a;
  font-size: 15px;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.assignment-card__footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

:deep(.el-empty) {
  padding: 60px 0;
}

@media (max-width: 768px) {
  .student-assignments {
    padding: 16px;
    margin: 12px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-item {
    width: 100%;
  }

  .search-input {
    max-width: unset;
  }

  .sort-wrapper {
    margin-left: 0;
    width: 100%;
  }

  .sort-item {
    flex: 1;
  }

  .assignment-list {
    grid-template-columns: 1fr;
  }

  .assignment-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .assignment-card__header {
    flex-direction: column;
  }

  .assignment-icon {
    width: 40px;
    height: 40px;
  }
}
</style>
