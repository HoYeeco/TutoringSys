<template>
  <div class="teacher-assignments">
    <el-card shadow="never" class="assignments-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Document /></el-icon>
            </div>
            <span>作业管理</span>
          </div>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索作业名称"
              clearable
              @keyup.enter="handleSearch"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="filterForm.courseId"
              placeholder="按课程筛选"
              class="filter-item"
              clearable
              @change="handleFilterChange"
            >
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
            <el-select
              v-model="filterForm.status"
              placeholder="按状态筛选"
              class="filter-item"
              clearable
              @change="handleFilterChange"
            >
              <el-option label="已发布" value="published" />
              <el-option label="已逾期" value="overdue" />
            </el-select>
            <el-button type="primary" @click="createAssignment">
              <el-icon><Plus /></el-icon> 发布作业
            </el-button>
          </div>
        </div>
      </template>
      <el-table
        :data="assignments"
        style="width: 100%"
        v-loading="loading"
        @row-click="showDetail"
        class="modern-table clickable-table"
      >
        <el-table-column prop="title" label="作业名称" min-width="180">
          <template #default="scope">
            <el-tooltip
              :content="scope.row.title"
              placement="top"
              :disabled="!isOverflow(scope.row.title, 15)"
            >
              <span class="ellipsis-text">{{
                scope.row.title || '-'
              }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="所属课程" width="150">
          <template #default="scope">
            <el-tooltip
              :content="scope.row.courseName"
              placement="top"
              :disabled="!isOverflow(scope.row.courseName, 10)"
            >
              <span class="ellipsis-text">{{
                scope.row.courseName || '-'
              }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分值" width="100" />
        <el-table-column prop="description" label="作业描述" min-width="150">
          <template #default="scope">
            <span
              v-if="!scope.row.description || !scope.row.description.trim() || scope.row.description === 'null'"
              class="no-description"
            >
              暂无描述
            </span>
            <el-tooltip
              v-else
              :content="scope.row.description"
              placement="top"
              :disabled="!isOverflow(scope.row.description, 20)"
            >
              <span class="ellipsis-text">{{ scope.row.description }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="160">
          <template #default="scope">
            <span>{{ formatDeadline(scope.row.deadline) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="completion" label="完成情况" width="120" align="center">
          <template #default="scope">
            <div class="completion-info">
              <span class="completion-text">
                {{
                  `${scope.row.submissionCount || 0}/${scope.row.totalStudents || 0}`
                }}
              </span>
              <el-progress
                :percentage="getCompletionRate(scope.row)"
                :stroke-width="6"
                :show-text="false"
                class="completion-progress"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === 'published' ? 'success' : 'warning'"
              size="small"
            >
              {{ scope.row.status === 'published' ? '已发布' : '已逾期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" @click.stop>
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click.stop="goToSubmissionsPage(scope.row)"
            >
              <el-icon><Document /></el-icon>
              提交详情
            </el-button>
            <el-button
              size="small"
              @click.stop="editAssignment(scope.row.id)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click.stop="deleteAssignment(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          justify="center"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailVisible"
      title="作业提交情况"
      width="600px"
      class="detail-dialog"
      append-to-body
      :show-close="true"
    >
      <div v-if="currentAssignment" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="作业名称">
            {{ currentAssignment.title }}
          </el-descriptions-item>
          <el-descriptions-item label="所属课程">
            {{ currentAssignment.courseName }}
          </el-descriptions-item>
          <el-descriptions-item label="总分值">
            {{ currentAssignment.totalScore }} 分
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag
              :type="
                currentAssignment.status === 'published' ? 'success' : 'warning'
              "
            >
              {{
                currentAssignment.status === 'published' ? '已发布' : '已逾期'
              }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="截止时间">
            {{ formatDeadline(currentAssignment.deadline) }}
          </el-descriptions-item>
          <el-descriptions-item label="作业描述">
            {{ !currentAssignment.description || !currentAssignment.description.trim() || currentAssignment.description === 'null' ? '暂无描述' : currentAssignment.description }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="completion-detail">
          <div class="completion-header">
            <h4>完成情况</h4>
            <el-button
              type="warning"
              @click="remindAllStudents"
              :loading="remindLoading"
            >
              <el-icon><Bell /></el-icon>
              一键提醒
            </el-button>
          </div>
          <el-row :gutter="12">
            <el-col :span="8">
              <el-statistic
                title="应交人数"
                :value="currentAssignment.totalStudents || 0"
              >
                <template #suffix>人</template>
              </el-statistic>
            </el-col>
            <el-col :span="8">
              <el-statistic
                title="已交人数"
                :value="currentAssignment.submissionCount || 0"
              >
                <template #suffix>人</template>
              </el-statistic>
            </el-col>
            <el-col :span="8">
              <el-statistic
                title="未交人数"
                :value="
                  (currentAssignment.totalStudents || 0) -
                  (currentAssignment.submissionCount || 0)
                "
              >
                <template #suffix>人</template>
              </el-statistic>
            </el-col>
          </el-row>
          <div class="completion-rate">
            <span>完成率</span>
            <el-progress
              :percentage="getCompletionRate(currentAssignment)"
              :stroke-width="12"
              :text-inside="true"
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Bell, Document } from '@element-plus/icons-vue';
import request from '@/utils/request';

interface Course {
  id: number;
  name: string;
}

interface Assignment {
  id: number;
  title: string;
  courseId: number;
  courseName: string;
  totalScore: number;
  description: string;
  deadline: string;
  submissionCount: number;
  totalStudents: number;
  status: string;
}

interface StudentInfo {
  studentId: number;
  studentName: string;
  username: string;
  avatar: string;
  email: string;
  phone: string;
}

const router = useRouter();

const courses = ref<Course[]>([]);
const assignments = ref<Assignment[]>([]);
const searchKeyword = ref('');
const filterForm = ref({
  courseId: '',
  status: '',
});
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const detailVisible = ref(false);
const currentAssignment = ref<Assignment | null>(null);
const activeTab = ref('submitted');
const submittedStudents = ref<StudentInfo[]>([]);
const notSubmittedStudents = ref<StudentInfo[]>([]);
const allStudents = ref<StudentInfo[]>([]);
const remindLoading = ref(false);

const formatDeadline = (deadline: string | null): string => {
  if (!deadline) return '-';
  try {
    const date = new Date(deadline);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch {
    return deadline;
  }
};

const isOverflow = (text: string | null, maxChars: number): boolean => {
  if (!text) return false;
  return text.length > maxChars;
};

const getCompletionRate = (assignment: Assignment): number => {
  if (!assignment || !assignment.totalStudents || assignment.totalStudents === 0) {
    return 0;
  }
  return Math.round((assignment.submissionCount || 0) / assignment.totalStudents * 100);
};

const isStudentSubmitted = (studentId: number): boolean => {
  return submittedStudents.value.some((s) => s.studentId === studentId);
};

const goToSubmissionsPage = (row: Assignment) => {
  router.push(`/teacher/assignments/${row.id}/submissions`);
};

const showDetail = async (row: Assignment) => {
  currentAssignment.value = row;
  detailVisible.value = true;
  activeTab.value = 'submitted';

  try {
    const response = await request.get(
      `/teacher/assignments/${row.id}/submission-status`,
    );
    if (response.data) {
      submittedStudents.value = response.data.submittedStudents || [];
      notSubmittedStudents.value = response.data.notSubmittedStudents || [];
      allStudents.value = response.data.allStudents || [];
    }
  } catch (error) {
    console.error('获取学生提交情况失败:', error);
    submittedStudents.value = [];
    notSubmittedStudents.value = [];
    allStudents.value = [];
  }
};

const getCourses = async () => {
  try {
    const response = await request.get('/teacher/courses/list', {
      params: { page: 1, size: 100 }
    });
    courses.value = response.data?.records || [];
  } catch (error) {
    console.error('获取课程列表失败:', error);
    courses.value = [];
  }
};

const getAssignments = async () => {
  loading.value = true;
  try {
    const response = await request.get('/teacher/assignments/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        courseId: filterForm.value.courseId || undefined,
        status: filterForm.value.status || undefined,
        keyword: searchKeyword.value || undefined,
      },
    });
    assignments.value = response.data?.records || [];
    total.value = response.data?.total || 0;
  } catch (error) {
    console.error('获取作业列表失败:', error);
    ElMessage.error('获取作业列表失败: ' + (error.message || '未知错误'));
    assignments.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  getAssignments();
};

const handleFilterChange = () => {
  currentPage.value = 1;
  getAssignments();
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  getAssignments();
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getAssignments();
};

const createAssignment = () => {
  router.push('/teacher/assignments/create');
};

const editAssignment = (assignmentId: number) => {
  detailVisible.value = false;
  router.push(`/teacher/assignments/${assignmentId}/edit`);
};

const publishAssignment = async (assignmentId: number) => {
  // 此方法已删除
};

const deleteAssignment = async (assignment: Assignment) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除作业「${assignment.title}」吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    );

    await request.delete(`/teacher/assignments/${assignment.id}`);
    ElMessage.success('作业删除成功');
    getAssignments();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除作业失败');
    }
  }
};

const remindStudent = async (studentId: number) => {
  try {
    await request.post(
      `/teacher/assignments/${currentAssignment.value?.id}/remind`,
      null,
      { params: { studentId } },
    );
    ElMessage.success('提醒已发送');
  } catch (error) {
    ElMessage.error('发送提醒失败');
  }
};

const remindAllStudents = async () => {
  if (notSubmittedStudents.value.length === 0) {
    ElMessage.warning('没有需要提醒的学生');
    return;
  }

  try {
    remindLoading.value = true;
    await request.post(
      `/teacher/assignments/${currentAssignment.value?.id}/remind`,
    );
    ElMessage.success('已向所有未提交学生发送提醒');
  } catch (error) {
    ElMessage.error('发送提醒失败');
  } finally {
    remindLoading.value = false;
  }
};

onMounted(() => {
  getCourses();
  getAssignments();
});
</script>

<style scoped>
.teacher-assignments {
  padding: 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
}

.el-table-column {
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-align: center;
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
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
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

.search-input {
  width: 200px;
}

.filter-item {
  width: 150px;
}

.modern-table {
  --el-table-border-radius: 12px;
  width: 100%;
}

.modern-table :deep(.el-table__cell) {
  padding: 8px;
  font-size: 13px;
  text-align: center;
}

.modern-table :deep(.el-table__header th) {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  color: #606266;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 8px;
  text-align: center;
}

.modern-table :deep(.el-table__row) {
  height: 60px;
}

.modern-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.modern-table :deep(.el-table__cell) {
  padding: 8px;
  font-size: 13px;
}

.completion-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.completion-info .draft-status {
  color: #909399;
}

.completion-text {
  font-size: 12px;
  color: #606266;
}

.completion-progress {
  width: 80px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.clickable-table :deep(.el-table__row) {
  cursor: pointer;
}

.clickable-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.ellipsis-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-content {
  padding: 10px 0;
  max-height: calc(80vh - 100px);
  overflow-y: auto;
  overflow-x: hidden;
}

.detail-dialog :deep(.el-dialog) {
  margin: 0 !important;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.detail-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.detail-dialog :deep(.el-dialog__body) {
  flex: 1;
  overflow-x: hidden;
  padding: 20px;
}

.detail-dialog :deep(.el-dialog__footer) {
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

.detail-dialog :deep(.el-descriptions) {
  width: 100%;
  table-layout: fixed;
}

.detail-dialog :deep(.el-descriptions__cell) {
  word-wrap: break-word;
  word-break: break-word;
}

.completion-detail {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.completion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.completion-header h4 {
  margin: 0;
  color: #303133;
  font-size: 16px;
}

.completion-actions {
  display: flex;
  gap: 12px;
}

.completion-rate {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.completion-rate span {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.completion-rate .el-progress {
  flex: 1;
}

.student-lists {
  margin-top: 24px;
}

.student-list {
  max-height: 300px;
  overflow-y: auto;
}

@media (max-width: 1200px) {
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .filter-item {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .teacher-assignments {
    padding: 16px;
  }
}
</style>
