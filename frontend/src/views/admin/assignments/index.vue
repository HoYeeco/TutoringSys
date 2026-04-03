<template>
  <div class="admin-assignments">
    <!-- 作业列表模块 -->
    <el-card shadow="never" class="assignments-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Notebook /></el-icon>
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
        :cell-style="{ textAlign: 'center' }"
        :header-cell-style="{ textAlign: 'center' }"
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
        <el-table-column prop="teacherName" label="负责教师" width="120">
          <template #default="scope">
            <div class="teacher-info">
              <el-avatar :size="20" :src="scope.row.teacherAvatar || ''">
                {{ scope.row.teacherName?.charAt(0) || '?' }}
              </el-avatar>
              <span>{{ scope.row.teacherName || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分值" width="100" />
        <el-table-column prop="description" label="作业描述" min-width="180">
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
        <el-table-column prop="completion" label="完成情况" width="120">
          <template #default="scope">
            <div class="completion-info">
              <span>
                {{ `${scope.row.submittedCount || 0}/${scope.row.totalStudents || 0}` }}
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
              @click.stop="viewSubmissions(scope.row)"
            >
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailVisible"
      :title="currentAssignment?.title || '作业详情'"
      width="600px"
      class="detail-dialog"
      append-to-body
    >
      <div v-if="currentAssignment" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="作业名称" :span="2">
            {{ currentAssignment.title }}
          </el-descriptions-item>
          <el-descriptions-item label="所属课程">
            {{ currentAssignment.courseName }}
          </el-descriptions-item>
          <el-descriptions-item label="负责教师">
            <div class="teacher-info">
              <el-avatar
                :size="24"
                :src="currentAssignment.teacherAvatar || ''"
              >
                {{ currentAssignment.teacherName?.charAt(0) || '?' }}
              </el-avatar>
              <span>{{ currentAssignment.teacherName }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="总分值">
            {{ currentAssignment.totalScore }} 分
          </el-descriptions-item>
          <el-descriptions-item label="题目数量">
            {{ currentAssignment.questionCount || 0 }} 道
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag
              :type="
                currentAssignment.status === 'published' ? 'success' : 'warning'
              "
            >
              {{ currentAssignment.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="截止时间">
            {{ formatDeadline(currentAssignment.deadline) }}
          </el-descriptions-item>
          <el-descriptions-item label="作业描述" :span="2">
            {{ currentAssignment.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="completion-detail">
          <h4>完成情况</h4>
          <el-row :gutter="20">
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
                :value="currentAssignment.submittedCount || 0"
              >
                <template #suffix>人</template>
              </el-statistic>
            </el-col>
            <el-col :span="8">
              <el-statistic
                title="未交人数"
                :value="
                  (currentAssignment.totalStudents || 0) -
                  (currentAssignment.submittedCount || 0)
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
              :stroke-width="20"
              :text-inside="true"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="editAssignment(currentAssignment?.id)">
          编辑
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="submissionsDialogVisible"
      :title="`${currentAssignment?.title || ''} - 学生提交列表`"
      width="900px"
      class="submissions-dialog"
      append-to-body
      destroy-on-close
    >
      <div class="submissions-header">
        <el-row :gutter="16">
          <el-col :span="8">
            <div class="stat-item">
              <span class="stat-label">应交人数</span>
              <span class="stat-value">{{ currentAssignment?.totalStudents || 0 }} 人</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-item">
              <span class="stat-label">已提交</span>
              <span class="stat-value">{{ submissionsTotal }} 人</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-item">
              <span class="stat-label">提交率</span>
              <span class="stat-value">{{ submissionRate }}%</span>
            </div>
          </el-col>
        </el-row>
      </div>

      <el-table
        :data="submissions"
        style="width: 100%"
        v-loading="submissionsLoading"
        class="submissions-table"
      >
        <el-table-column label="学生信息" min-width="180">
          <template #default="scope">
            <div class="student-info">
              <el-avatar :size="36" :src="scope.row.avatar || ''">
                {{ scope.row.studentName?.charAt(0) || '?' }}
              </el-avatar>
              <div class="student-details">
                <div class="student-name">{{ scope.row.studentName }}</div>
                <div class="student-username">@{{ scope.row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160">
          <template #default="scope">
            {{ formatDeadline(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="100" align="center">
          <template #default="scope">
            <span :class="getScoreClass(scope.row.score)">
              {{ scope.row.score !== null ? scope.row.score : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="批改状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'graded' ? 'success' : 'warning'" size="small">
              {{ scope.row.status === 'graded' ? '已批改' : '待批改' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              @click="deleteSubmission(scope.row)"
              :loading="scope.row.deleting"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" v-if="submissionsTotal > 0">
        <el-pagination
          v-model:current-page="submissionsPage"
          v-model:page-size="submissionsPageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="submissionsTotal"
          @size-change="handleSubmissionsSizeChange"
          @current-change="handleSubmissionsPageChange"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search, Notebook } from '@element-plus/icons-vue';
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
  teacherId: number;
  teacherName: string;
  teacherAvatar: string;
  totalScore: number;
  description: string;
  deadline: string;
  submittedCount: number;
  totalStudents: number;
  questionCount: number;
  submissionCount: number;
  status: string;
  createTime: string;
  updateTime: string;
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

const submissionsDialogVisible = ref(false);
const submissions = ref<any[]>([]);
const submissionsLoading = ref(false);
const submissionsTotal = ref(0);
const submissionsPage = ref(1);
const submissionsPageSize = ref(10);

const submissionRate = computed(() => {
  if (!currentAssignment.value || !currentAssignment.value.totalStudents) return 0;
  return Math.round((submissionsTotal.value / currentAssignment.value.totalStudents) * 100);
});

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

const getCompletionRate = (assignment: Assignment | null): number => {
  if (!assignment || !assignment.totalStudents || assignment.totalStudents === 0) {
    return 0;
  }
  return Math.round((assignment.submittedCount || 0) / assignment.totalStudents * 100);
};

const showDetail = async (row: Assignment) => {
  currentAssignment.value = row;
  detailVisible.value = true;

  try {
    const response = await request.get(
      `/admin/assignments/${row.id}`,
    );
    if (response.data) {
      currentAssignment.value = response.data;
    }
  } catch (error) {
    console.error('获取作业详情失败:', error);
  }
};

const getCourses = async () => {
  try {
    const response = await request.get('/admin/courses', {
      params: { page: 1, size: 100 },
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
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
    };
    
    if (filterForm.value.courseId) {
      params.courseId = filterForm.value.courseId;
    }
    if (filterForm.value.status) {
      params.status = filterForm.value.status;
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value;
    }
    
    const response = await request.get('/admin/assignments', {
      params
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
  router.push('/admin/assignments/create');
};

const editAssignment = (assignmentId: number) => {
  detailVisible.value = false;
  router.push(`/admin/assignments/${assignmentId}/edit`);
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

    await request.delete(`/admin/assignments/${assignment.id}`);
    ElMessage.success('作业删除成功');
    getAssignments();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除作业失败');
    }
  }
};

const viewSubmissions = (assignment: Assignment) => {
  router.push(`/teacher/assignments/${assignment.id}/submissions`);
};

const getSubmissions = async () => {
  if (!currentAssignment.value) return;
  
  submissionsLoading.value = true;
  try {
    const response = await request.get(`/admin/assignments/${currentAssignment.value.id}/submissions`, {
      params: {
        page: submissionsPage.value,
        size: submissionsPageSize.value
      }
    });
    
    if (response.data) {
      submissionsTotal.value = response.data.total || 0;
      submissions.value = (response.data.records || []).map((record: any) => ({
        id: record.id,
        studentId: record.studentId,
        studentName: record.studentName,
        username: record.studentUsername,
        avatar: '',
        submitTime: record.submitTime,
        score: record.finalTotalScore,
        status: record.reviewStatus === 1 ? 'graded' : 'pending',
        deleting: false
      }));
    }
  } catch (error) {
    console.error('获取提交列表失败:', error);
    ElMessage.error('获取提交列表失败');
  } finally {
    submissionsLoading.value = false;
  }
};

const handleSubmissionsSizeChange = (size: number) => {
  submissionsPageSize.value = size;
  submissionsPage.value = 1;
  getSubmissions();
};

const handleSubmissionsPageChange = (page: number) => {
  submissionsPage.value = page;
  getSubmissions();
};

const getScoreClass = (score: number | null) => {
  if (score === null) return 'score-null';
  if (score >= 90) return 'score-excellent';
  if (score >= 80) return 'score-good';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
};

const deleteSubmission = async (submission: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生「${submission.studentName}」的提交记录吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    submission.deleting = true;
    await request.delete(`/admin/assignments/submissions/${submission.id}`);
    ElMessage.success('提交记录已删除');
    getSubmissions();
    getAssignments();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'));
    }
  } finally {
    submission.deleting = false;
  }
};

onMounted(() => {
  getCourses();
  getAssignments();
});
</script>

<style scoped>
.admin-assignments {
  padding: 24px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(4px);
  border-radius: 16px;
  margin: 16px;
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

/* 表格样式 */
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
  height: 60px;
}

.modern-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.modern-table :deep(.el-table__cell) {
  padding: 8px;
  font-size: 13px;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.teacher-info .el-avatar {
  flex-shrink: 0;
}

.completion-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.completion-info .draft-status {
  color: #909399;
}

.completion-progress {
  width: 60px;
}

/* 分页 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 对话框 */
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

.mt-4 {
  margin-top: 16px;
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
}

.completion-detail {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.completion-detail h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
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

/* 响应式设计 */
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
  
  .admin-assignments {
    padding: 16px;
  }
}

.submissions-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.submissions-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8faff 0%, #f0f5ff 100%);
  border-bottom: 1px solid #e8eeff;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.submissions-table {
  margin: 16px 24px;
  width: calc(100% - 48px) !important;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-details {
  text-align: left;
}

.student-name {
  font-weight: 600;
  color: #303133;
}

.student-username {
  font-size: 12px;
  color: #909399;
}

.score-null {
  color: #c0c4cc;
}

.score-excellent {
  color: #67c23a;
  font-weight: 600;
}

.score-good {
  color: #409eff;
  font-weight: 600;
}

.score-pass {
  color: #e6a23c;
  font-weight: 600;
}

.score-fail {
  color: #f56c6c;
  font-weight: 600;
}
</style>
