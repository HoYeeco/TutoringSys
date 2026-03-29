<template>
  <div class="teacher-grading">
    <el-card shadow="never" class="grading-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><EditPen /></el-icon>
            </div>
            <span>批改复核</span>
          </div>
          <div class="header-actions">
            <el-select v-model="filterForm.courseId" placeholder="按课程筛选" class="filter-item" clearable @change="handleFilterChange">
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
            <el-input
              v-model="filterForm.keyword"
              placeholder="按作业名、学生姓名搜索"
              class="search-input"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </template>
      
      <div class="batch-actions" v-if="selectedRows.length > 0">
        <span class="selected-count">已选择 {{ selectedRows.length }} 项</span>
        <el-button type="success" @click="handleBatchAccept">批量采用AI评分</el-button>
        <el-button type="warning" @click="showBatchModifyDialog">批量修改分数</el-button>
        <el-button @click="clearSelection">取消选择</el-button>
      </div>
      
      <el-table
        ref="tableRef"
        :data="submissions"
        style="width: 100%"
        class="modern-table"
        v-loading="loading"
        :cell-style="{ textAlign: 'center' }"
        :header-cell-style="{ textAlign: 'center' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="assignmentTitle" label="作业名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="courseName" label="所属课程" min-width="150" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="questionType" label="题目类型" width="100">
          <template #default="scope">
            <el-tag size="small" :type="getQuestionTypeTag(scope.row.questionTypeCategory)">
              {{ scope.row.questionTypeCategory || getQuestionTypeLabel(scope.row.questionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="aiScore" label="AI评分" width="100" />
        <el-table-column prop="finalScore" label="最终得分" width="100" />
        <el-table-column prop="submitTime" label="提交时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="reviewStatus" label="复核状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.reviewStatus === 2 ? 'success' : 'warning'" size="small">
              {{ scope.row.reviewStatus === 2 ? '已复核' : '待复核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="viewReviewDetail(scope.row.answerId)"
            >
              复核
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
    
    <el-dialog v-model="batchModifyDialogVisible" title="批量修改分数" width="400px">
      <el-form label-width="100px">
        <el-form-item label="新分数">
          <el-input-number v-model="batchModifyScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="教师评语">
          <el-input v-model="batchTeacherFeedback" type="textarea" :rows="3" placeholder="可选填写教师评语" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchModifyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchModify">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, EditPen } from '@element-plus/icons-vue';
import request from '@/utils/request';

const router = useRouter();
const tableRef = ref();

const filterForm = ref({
  courseId: '',
  keyword: ''
});

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);

const courses = ref<any[]>([]);
const submissions = ref<any[]>([]);
const selectedRows = ref<any[]>([]);

const batchModifyDialogVisible = ref(false);
const batchModifyScore = ref(0);
const batchTeacherFeedback = ref('');

const getQuestionTypeTag = (type: string): string => {
  const typeMap: Record<string, string> = {
    '客观题': '',
    '主观题': 'warning',
    '综合题': 'success'
  };
  return typeMap[type] || '';
};

const getQuestionTypeLabel = (type: string): string => {
  const typeMap: Record<string, string> = {
    SINGLE: '客观题',
    MULTIPLE: '客观题',
    JUDGE: '客观题',
    SUBJECTIVE: '主观题',
    single: '客观题',
    multiple: '客观题',
    judgment: '客观题',
    essay: '主观题'
  };
  return typeMap[type] || type;
};

const formatDateTime = (date: string | number[] | null): string => {
  if (!date) return '-';
  try {
    let d: Date;
    if (Array.isArray(date)) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = date;
      d = new Date(year, month - 1, day, hour, minute, second);
    } else {
      d = new Date(date);
    }
    if (isNaN(d.getTime())) return '-';
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  } catch {
    return '-';
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
  }
};

const getSubmissions = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (filterForm.value.courseId) {
      params.courseId = filterForm.value.courseId;
    }
    if (filterForm.value.keyword) {
      params.keyword = filterForm.value.keyword;
    }
    const response = await request.get('/teacher/review/list', { params });
    submissions.value = (response.data?.records || []).map((item: any) => ({
      ...item,
      questionTypeCategory: categorizeQuestionType(item.questionType)
    }));
    total.value = response.data?.total || 0;
  } catch (error) {
    ElMessage.error('获取待复核列表失败');
    submissions.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const categorizeQuestionType = (type: string): string => {
  const objectiveTypes = ['SINGLE', 'MULTIPLE', 'JUDGE', 'single', 'multiple', 'judgment'];
  const subjectiveTypes = ['SUBJECTIVE', 'subjective', 'essay'];
  
  if (objectiveTypes.includes(type)) {
    return '客观题';
  } else if (subjectiveTypes.includes(type)) {
    return '主观题';
  }
  return '综合题';
};

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows;
};

const clearSelection = () => {
  tableRef.value?.clearSelection();
  selectedRows.value = [];
};

const handleBatchAccept = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要操作的记录');
    return;
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要批量采用 ${selectedRows.value.length} 条记录的AI评分吗？`,
      '确认操作',
      { type: 'warning' }
    );
    
    const answerIds = selectedRows.value.map(row => row.answerId);
    await request.post('/teacher/review/batch/accept', answerIds, {
      params: { teacherFeedback: '' }
    });
    
    ElMessage.success('批量采用成功');
    clearSelection();
    getSubmissions();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('批量操作失败');
    }
  }
};

const showBatchModifyDialog = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要操作的记录');
    return;
  }
  batchModifyScore.value = 0;
  batchTeacherFeedback.value = '';
  batchModifyDialogVisible.value = true;
};

const handleBatchModify = async () => {
  try {
    const answerIds = selectedRows.value.map(row => row.answerId);
    await request.post('/teacher/review/batch/modify', answerIds, {
      params: { 
        newScore: batchModifyScore.value,
        teacherFeedback: batchTeacherFeedback.value || ''
      }
    });
    
    ElMessage.success('批量修改成功');
    batchModifyDialogVisible.value = false;
    clearSelection();
    getSubmissions();
  } catch (error) {
    ElMessage.error('批量操作失败');
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  getSubmissions();
};

const handleFilterChange = () => {
  currentPage.value = 1;
  getSubmissions();
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  getSubmissions();
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getSubmissions();
};

const viewReviewDetail = (answerId: number) => {
  router.push(`/teacher/review/${answerId}`);
};

onMounted(() => {
  getCourses();
  getSubmissions();
});
</script>

<style scoped>
.teacher-grading {
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
  width: 150px;
}

.search-input {
  width: 250px;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f0f9eb;
  border-radius: 8px;
}

.selected-count {
  font-weight: 500;
  color: #67c23a;
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
}

.modern-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.modern-table :deep(.el-table__cell) {
  padding: 8px;
  font-size: 13px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .filter-item,
  .search-input {
    width: 100%;
  }
  
  .teacher-grading {
    padding: 16px;
  }
  
  .batch-actions {
    flex-wrap: wrap;
  }
}
</style>
