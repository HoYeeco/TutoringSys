<template>
  <div class="teacher-course-detail">
    <!-- 课程信息卡片 -->
    <el-card shadow="never" class="course-header-card">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><Reading /></el-icon>
          </div>
          <span class="card-title__text">{{ course.name }}</span>
        </div>
      </template>
      <div class="course-info">
        <div class="info-row">
          <span class="info-label">课程描述：</span>
          <span class="info-value">{{ course.description || '无' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">创建时间：</span>
          <span class="info-value">{{
            formatDateTime(course.createTime)
          }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">学生人数：</span>
          <span class="info-value">{{ total }} 人</span>
        </div>
        <div class="info-actions">
          <el-button type="primary" @click="openAddStudentDialog">
            <el-icon><Plus /></el-icon> 添加学生
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 学生列表卡片 -->
    <el-card shadow="never" class="students-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><User /></el-icon>
          </div>
          <span class="card-title__text">学生列表</span>
        </div>
      </template>

      <div class="table-header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学生姓名或账号"
          clearable
          @keyup.enter="handleSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button
          type="danger"
          @click="handleBatchRemove"
          :disabled="selectedStudentIds.length === 0"
        >
          <el-icon><Delete /></el-icon> 批量移除
        </el-button>
      </div>

      <el-table
        :data="students"
        style="width: 100%"
        class="modern-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="username" label="账号" width="150" />
        <el-table-column prop="realName" label="学生姓名" width="120" />
        <el-table-column
          prop="email"
          label="邮箱"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column
          prop="completionRate"
          label="作业完成率"
          width="150"
          align="center"
        >
          <template #default="scope">
            <div class="completion-info">
              <el-progress
                :percentage="scope.row.completionRate"
                :color="getCompletionRateColor(scope.row.completionRate)"
                :stroke-width="8"
                :show-text="true"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="加入时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.joinTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button
              size="small"
              type="danger"
              @click="removeStudent(scope.row.id)"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          justify="center"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加学生弹窗 -->
    <el-dialog v-model="addStudentDialogVisible" title="添加学生" width="600px">
      <el-input
        v-model="addStudentSearch"
        placeholder="搜索学生姓名或账号"
        clearable
        @keyup.enter="filterAvailableStudents"
        class="mb-4"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select
        v-model="selectedStudents"
        multiple
        filterable
        remote
        reserve-keyword
        placeholder="选择学生"
        :remote-method="filterAvailableStudents"
        :loading="loading"
        style="width: 100%"
      >
        <el-option
          v-for="student in filteredAvailableStudents"
          :key="student.id"
          :label="`${student.username} - ${student.realName}`"
          :value="student.id"
        />
      </el-select>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addStudentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAddStudents">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Delete, Plus, Reading, User } from '@element-plus/icons-vue';
import request from '@/utils/request';
import { formatDateTime } from '@/utils/format';

interface Course {
  id: number;
  name: string;
  description: string;
  createTime: string;
}

interface Student {
  id: number;
  username: string;
  realName: string;
  email: string;
  phone: string;
  completionRate: number;
  joinTime: string;
}

interface AvailableStudent {
  id: number;
  username: string;
  realName: string;
}

const route = useRoute();
const router = useRouter();
const courseId = computed(() => route.params.id as string);

const course = ref<Course>({
  id: 0,
  name: '',
  description: '',
  createTime: '',
});

const students = ref<Student[]>([]);
const availableStudents = ref<AvailableStudent[]>([]);
const filteredAvailableStudents = ref<AvailableStudent[]>([]);

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const searchKeyword = ref('');
const addStudentSearch = ref('');
const loading = ref(false);

const addStudentDialogVisible = ref(false);
const selectedStudents = ref<number[]>([]);
const selectedStudentIds = ref<number[]>([]);

// 获取课程详情
const getCourseDetail = async () => {
  try {
    const response = await request.get(`/teacher/courses/${courseId.value}`);
    course.value = response.data || {
      id: 0,
      name: '',
      description: '',
      createTime: '',
    };
  } catch (error) {
    ElMessage.error('获取课程详情失败');
  }
};

// 获取课程学生列表
const getCourseStudents = async () => {
  try {
    const response = await request.get(
      `/teacher/courses/${courseId.value}/students`,
      {
        params: {
          page: currentPage.value,
          size: pageSize.value,
        },
      },
    );
    students.value = response.data?.records || [];
    total.value = response.data?.total || 0;
  } catch (error) {
    ElMessage.error('获取学生列表失败');
    students.value = [];
    total.value = 0;
  }
};

// 获取可选学生
const getAvailableStudents = async () => {
  try {
    const response = await request.get('/admin/users', {
      params: { role: 'STUDENT', page: 1, size: 100 },
    });
    availableStudents.value = response.data?.records || [];
    filteredAvailableStudents.value = [...availableStudents.value];
  } catch (error) {
    ElMessage.error('获取可选学生失败');
    availableStudents.value = [];
    filteredAvailableStudents.value = [];
  }
};

// 过滤可选学生
const filterAvailableStudents = (query: string) => {
  if (query) {
    loading.value = true;
    setTimeout(() => {
      filteredAvailableStudents.value = availableStudents.value.filter(
        (student) => {
          return (
            student.realName.toLowerCase().includes(query.toLowerCase()) ||
            student.username.toLowerCase().includes(query.toLowerCase())
          );
        },
      );
      loading.value = false;
    }, 200);
  } else {
    filteredAvailableStudents.value = [...availableStudents.value];
  }
};

// 添加学生到课程
const addStudents = async (studentIds: number[]) => {
  try {
    await request.post(`/teacher/courses/${courseId.value}/students`, {
      studentIds,
    });
    ElMessage.success('添加学生成功');
    addStudentDialogVisible.value = false;
    selectedStudents.value = [];
    addStudentSearch.value = '';
    getCourseStudents();
  } catch (error) {
    ElMessage.error('添加学生失败');
  }
};

// 从课程移除学生
const removeStudent = async (studentId: number) => {
  try {
    ElMessageBox.confirm('确定要移除该学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      await request.delete(
        `/teacher/courses/${courseId.value}/students/${studentId}`,
      );
      ElMessage.success('移除学生成功');
      getCourseStudents();
    });
  } catch (error) {
    ElMessage.error('移除学生失败');
  }
};

// 批量移除学生
const handleBatchRemove = () => {
  if (selectedStudentIds.value.length === 0) {
    ElMessage.warning('请选择要移除的学生');
    return;
  }

  ElMessageBox.confirm(
    `确定要移除选中的 ${selectedStudentIds.value.length} 名学生吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    },
  ).then(async () => {
    try {
      await request.post(
        `/teacher/courses/${courseId.value}/students/batch-remove`,
        {
          studentIds: selectedStudentIds.value,
        },
      );
      ElMessage.success('批量移除学生成功');
      selectedStudentIds.value = [];
      getCourseStudents();
    } catch (error) {
      ElMessage.error('批量移除学生失败');
    }
  });
};

// 处理选择变化
const handleSelectionChange = (val: any[]) => {
  selectedStudentIds.value = val.map((item) => item.id);
};

// 处理搜索
const handleSearch = () => {
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    students.value = students.value.filter((student) => {
      return (
        student.realName.toLowerCase().includes(keyword) ||
        student.username.toLowerCase().includes(keyword)
      );
    });
  } else {
    getCourseStudents();
  }
};

// 打开添加学生对话框
const openAddStudentDialog = () => {
  getAvailableStudents().then(() => {
    addStudentDialogVisible.value = true;
  });
};

// 确认添加学生
const confirmAddStudents = () => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请选择学生');
    return;
  }
  addStudents(selectedStudents.value);
};

// 获取完成率颜色
const getCompletionRateColor = (rate: number) => {
  if (rate >= 80) {
    return '#67c23a';
  } else if (rate >= 60) {
    return '#e6a23c';
  } else {
    return '#f56c6c';
  }
};

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  getCourseStudents();
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getCourseStudents();
};

// 初始化
onMounted(() => {
  getCourseDetail();
  getCourseStudents();
});
</script>

<style scoped>
.teacher-course-detail {
  padding: 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 通用卡片样式 */
:deep(.el-card) {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: none;
  background: transparent;
  overflow: hidden;
}

:deep(.el-card__body) {
  padding: 0 20px 20px;
  overflow: hidden;
}

/* 卡片标题 */
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

.card-title__text {
  color: #303133;
}

/* 课程信息卡片 */
.course-header-card {
  margin-bottom: 0;
}

.course-info {
  padding: 10px 0;
}

.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  font-size: 14px;
  line-height: 1.6;
}

.info-label {
  font-weight: 600;
  color: #606266;
  min-width: 100px;
  flex-shrink: 0;
}

.info-value {
  color: #303133;
  flex: 1;
}

.info-actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 学生列表卡片 */
.students-section {
  margin-bottom: 0;
}

.table-header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 0 20px;
}

.search-input {
  width: 300px;
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

/* 居中对齐的列 */
.modern-table :deep(.el-table__body .el-table__cell) {
  text-align: center;
}

/* 完成率 */
.completion-info {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

/* 分页 */
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

/* 响应式设计 */
@media (max-width: 1200px) {
  .table-header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .teacher-course-detail {
    padding: 16px;
  }

  .card-title {
    font-size: 14px;
  }

  .info-row {
    flex-direction: column;
  }

  .info-label {
    margin-bottom: 4px;
  }
}
</style>
