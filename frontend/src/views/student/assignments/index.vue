<template>
  <div class="student-assignments">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>我的作业</span>
        </div>
      </template>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <div class="filter-bar">
        <div class="filter-row">
          <el-select v-model="filterStatus" placeholder="按状态筛选" class="filter-item" clearable>
            <el-option label="全部" value="" />
            <el-option label="待提交" value="pending" />
            <el-option label="待批改" value="grading" />
            <el-option label="已批改" value="graded" />
            <el-option label="已逾期" value="overdue" />
          </el-select>
          <el-select v-model="filterCourse" placeholder="按课程筛选" class="filter-item" clearable>
            <el-option label="全部" value="" />
            <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
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
        </div>
        <div class="sort-row">
          <span class="sort-label">排序：</span>
          <el-select v-model="sortBy" placeholder="排序方式" class="sort-item">
            <el-option label="发布时间（最新）" value="createTime:desc" />
            <el-option label="发布时间（最早）" value="createTime:asc" />
            <el-option label="截止时间（最近）" value="deadline:asc" />
            <el-option label="截止时间（最远）" value="deadline:desc" />
          </el-select>
        </div>
      </div>

      <el-empty v-if="filteredAssignments.length === 0" description="暂无作业" />
      <div v-else class="assignment-list">
        <div
          v-for="assignment in filteredAssignments"
          :key="assignment.id"
          class="assignment-card"
        >
          <div class="assignment-header">
            <span class="assignment-title">{{ assignment.title }}</span>
            <el-tag 
              :type="getTagType(assignment.status)" 
              :class="{ 'overdue-tag': assignment.status === 'overdue' }"
              effect="dark"
            >
              {{ getStatusText(assignment.status) }}
            </el-tag>
          </div>
          <div class="assignment-content">
            <div class="info-row">
              <span class="info-item">
                <el-icon><Notebook /></el-icon>
                {{ assignment.courseName }}
              </span>
            </div>
            <div class="info-row" v-if="assignment.status === 'pending' || assignment.status === 'overdue'">
              <span class="info-item deadline-info" :class="{ 'overdue-info': assignment.status === 'overdue' }">
                <el-icon><Clock /></el-icon>
                截止时间：{{ formatDate(assignment.deadline) }}
              </span>
            </div>
            <div class="info-row" v-if="assignment.status === 'graded'">
              <span class="info-item score-info">
                <el-icon><Trophy /></el-icon>
                得分：<span class="score-value">{{ assignment.score }}</span> / {{ assignment.totalScore }}
              </span>
            </div>
            <div class="info-row" v-if="assignment.status === 'grading'">
              <span class="info-item">
                <el-icon><Loading /></el-icon>
                批改中
              </span>
            </div>
          </div>
          <div class="assignment-footer">
            <el-button
              v-if="assignment.status === 'pending'"
              type="primary"
              @click="goToSubmit(assignment)"
            >
              <el-icon><Edit /></el-icon> 写作业
            </el-button>
            <el-button
              v-else-if="assignment.status === 'graded'"
              type="success"
              @click="goToReport(assignment)"
            >
              <el-icon><View /></el-icon> 查看报告
            </el-button>
            <el-button
              v-else-if="assignment.status === 'overdue'"
              type="danger"
              disabled
            >
              <el-icon><Warning /></el-icon> 已逾期
            </el-button>
            <el-button
              v-else
              type="info"
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
import { Search, Notebook, Clock, Trophy, Loading, Edit, View, Warning } from '@element-plus/icons-vue';
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
    result = result.filter(item => item.status === filterStatus.value);
  }
  
  if (filterCourse.value) {
    result = result.filter(item => item.courseId === Number(filterCourse.value));
  }
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(item => {
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
      size: 100
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
  router.push(`/student/assignments/report/${assignment.id}`);
};

const getTagType = (status: string) => {
  switch (status) {
    case 'pending':
      return 'warning';
    case 'grading':
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
    case 'grading':
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

.assignment-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
}

.assignment-card {
  padding: 20px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background-color: var(--color-card);
  transition: all 0.3s ease;
}

.assignment-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.assignment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  white-space: nowrap;
}

.overdue-tag {
  background-color: #f56c6c !important;
  border-color: #f56c6c !important;
}

.assignment-content {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.deadline-info {
  color: #e6a23c;
}

.deadline-info.overdue-info {
  color: #f56c6c;
}

.score-info {
  color: #67c23a;
}

.score-value {
  font-size: 18px;
  font-weight: 600;
  color: #67c23a;
}

.assignment-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
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
  
  .assignment-list {
    grid-template-columns: 1fr;
  }
}
</style>
