<template>
  <div class="student-error-book">
    <!-- 错题列表模块 -->
    <el-card shadow="never" class="error-list-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Notebook /></el-icon>
            </div>
            <span>错题列表</span>
          </div>
          <div class="header-actions">
            <el-dropdown @command="handleExport">
              <el-button type="primary">
                <el-icon><Download /></el-icon>
                导出错题
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="pdf">导出为 PDF</el-dropdown-item>
                  <el-dropdown-item command="excel">导出为 Excel</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索题干、答案、课程或作业名称"
              clearable
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select 
              v-model="filterCourseId" 
              placeholder="按课程筛选" 
              clearable
              class="filter-select"
            >
              <el-option label="全部课程" value="" />
              <el-option 
                v-for="course in courses" 
                :key="course.courseId" 
                :label="`${course.courseName} (${course.errorCount})`" 
                :value="course.courseId" 
              />
            </el-select>
            <el-select 
              v-model="sortBy" 
              placeholder="排序方式"
              class="sort-select"
            >
              <el-option label="错题时间（最新）" value="desc" />
              <el-option label="错题时间（最早）" value="asc" />
            </el-select>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="errorList.length === 0" class="empty-container">
        <el-empty description="暂无错题记录" :image-size="160" />
      </div>
      <div v-else class="error-list">
        <el-collapse v-model="activeNames" accordion>
          <el-collapse-item
            v-for="error in errorList"
            :key="error.id"
            :name="error.id.toString()"
          >
            <template #title>
              <div class="error-title-row">
                <div class="title-left">
                  <el-tag :type="getQuestionTypeTag(error.questionType)" size="small" class="type-tag">
                    {{ getQuestionTypeText(error.questionType) }}
                  </el-tag>
                  <span class="question-preview">{{ truncateText(error.questionContent, 60) }}</span>
                </div>
                <div class="title-right">
                  <span class="course-badge">{{ error.courseName }}</span>
                  <span class="time-text">{{ formatDate(error.createTime) }}</span>
                </div>
              </div>
            </template>
            
            <div class="error-detail">
              <div class="detail-section">
                <div class="section-title">题目内容</div>
                <div class="question-content" v-html="error.questionContent"></div>
              </div>
              
              <div class="detail-section" v-if="error.options">
                <div class="section-title">选项</div>
                <div class="options-list">
                  <div 
                    v-for="(option, key) in parseOptions(error.options)" 
                    :key="key"
                    class="option-item"
                    :class="{
                      'is-correct': isCorrectOption(error.correctAnswer, String(key)),
                      'is-wrong': isStudentAnswer(error.studentAnswer, String(key)) && !isCorrectOption(error.correctAnswer, String(key))
                    }"
                  >
                    <span class="option-key">{{ key }}.</span>
                    <span class="option-text">{{ option }}</span>
                    <el-icon v-if="isCorrectOption(error.correctAnswer, String(key))" class="correct-icon"><CircleCheck /></el-icon>
                    <el-icon v-if="isStudentAnswer(error.studentAnswer, String(key)) && !isCorrectOption(error.correctAnswer, String(key))" class="wrong-icon"><CircleClose /></el-icon>
                  </div>
                </div>
              </div>
              
              <el-row :gutter="20" class="answer-section">
                <el-col :span="12">
                  <div class="answer-box wrong-answer">
                    <div class="answer-label">
                      <el-icon><CircleClose /></el-icon>
                      你的答案
                    </div>
                    <div class="answer-content">{{ formatAnswer(error.studentAnswer, error.questionType) }}</div>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="answer-box correct-answer">
                    <div class="answer-label">
                      <el-icon><CircleCheck /></el-icon>
                      正确答案
                    </div>
                    <div class="answer-content">{{ formatAnswer(error.correctAnswer, error.questionType) }}</div>
                  </div>
                </el-col>
              </el-row>
              
              <div class="detail-section analysis-section" v-if="error.analysis">
                <div class="section-title">
                  <el-icon><Document /></el-icon>
                  题目解析
                </div>
                <div class="analysis-content">{{ error.analysis }}</div>
              </div>
              
              <div class="detail-footer">
                <div class="meta-info">
                  <span class="meta-item">
                    <el-icon><Folder /></el-icon>
                    所属作业：{{ error.assignmentTitle }}
                  </span>
                  <span class="meta-item">
                    <el-icon><School /></el-icon>
                    所属课程：{{ error.courseName }}
                  </span>
                </div>
                <el-button type="danger" size="small" @click="confirmRemove(error)">
                  <el-icon><Delete /></el-icon>
                  移除错题
                </el-button>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <div class="pagination-container" v-if="totalCount > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="totalCount"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Search, Refresh, Download, ArrowDown, CircleCheck, CircleClose, 
  Document, Folder, School, Delete, Notebook
} from '@element-plus/icons-vue';
import request from '@/utils/request';

const searchKeyword = ref('');
const filterCourseId = ref('');
const sortBy = ref('desc');
const activeNames = ref<string[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalCount = ref(0);

const errorList = ref<any[]>([]);
const courses = ref<any[]>([]);

const getCourses = async () => {
  try {
    const response = await request.get('/student/error-book/by-course');
    courses.value = response.data || [];
  } catch (error) {
    console.error('获取课程列表失败:', error);
    courses.value = [];
  }
};

const getErrorList = async () => {
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
      sortBy: sortBy.value
    };
    
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value;
    }
    if (filterCourseId.value) {
      params.courseId = filterCourseId.value;
    }
    
    const response = await request.get('/student/error-book', { params });
    const data = response.data || {};
    errorList.value = data.records || [];
    totalCount.value = data.total || 0;
  } catch (error) {
    console.error('获取错题列表失败:', error);
    ElMessage.error('获取错题列表失败');
    errorList.value = [];
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  getErrorList();
};

const resetFilters = () => {
  searchKeyword.value = '';
  filterCourseId.value = '';
  sortBy.value = 'desc';
  currentPage.value = 1;
  getErrorList();
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  getErrorList();
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  getErrorList();
};

const handleExport = async (format: string) => {
  try {
    const params: any = {};
    if (filterCourseId.value) {
      params.courseId = filterCourseId.value;
    }
    
    const response = await request.get('/student/error-book/export', { 
      params,
      responseType: 'blob'
    });
    
    const blob = new Blob([response.data as BlobPart], {
      type: format === 'pdf' ? 'application/pdf' : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `错题本_${new Date().toLocaleDateString()}.${format === 'pdf' ? 'pdf' : 'xlsx'}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    
    ElMessage.success(`错题本已导出为${format.toUpperCase()}格式`);
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败，请稍后重试');
  }
};

const confirmRemove = (error: any) => {
  ElMessageBox.confirm(
    '确定要移除这道错题吗？移除后将无法恢复。',
    '移除确认',
    {
      confirmButtonText: '确定移除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    removeError(error.id);
  }).catch(() => {});
};

const removeError = async (errorId: number) => {
  try {
    await request.delete(`/student/error-book/${errorId}`);
    ElMessage.success('错题已移除');
    getErrorList();
    getCourses();
  } catch (error) {
    console.error('移除错题失败:', error);
    ElMessage.error('移除错题失败');
  }
};

const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '简答题'
  };
  return typeMap[type] || type;
};

const getQuestionTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    single: 'primary',
    multiple: 'success',
    judgment: 'warning',
    essay: 'info'
  };
  return tagMap[type] || 'info';
};

const truncateText = (text: string, maxLength: number) => {
  if (!text) return '';
  const plainText = text.replace(/<[^>]+>/g, '');
  if (plainText.length <= maxLength) return plainText;
  return plainText.substring(0, maxLength) + '...';
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
};

const parseOptions = (options: string) => {
  try {
    if (typeof options === 'string') {
      return JSON.parse(options);
    }
    return options || {};
  } catch {
    return {};
  }
};

const isCorrectOption = (correctAnswer: string, key: string) => {
  if (!correctAnswer) return false;
  const answers = correctAnswer.toUpperCase().split(',');
  return answers.includes(key.toUpperCase());
};

const isStudentAnswer = (studentAnswer: string, key: string) => {
  if (!studentAnswer) return false;
  const answers = studentAnswer.toUpperCase().split(',');
  return answers.includes(key.toUpperCase());
};

const formatAnswer = (answer: string, type: string) => {
  if (!answer) return '-';
  if (type === 'judgment') {
    return answer === 'true' || answer === '正确' ? '正确' : '错误';
  }
  return answer;
};

onMounted(() => {
  getCourses();
  getErrorList();
});
</script>

<style scoped>
.student-error-book {
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
  margin-bottom: 24px;
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 250px;
}

.filter-select {
  width: 150px;
}

.sort-select {
  width: 160px;
}

/* 错题列表 */
.error-list-section {
  margin-bottom: 0;
}

.error-list {
  margin-top: 8px;
}

.error-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 16px;
}

.title-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.type-tag {
  flex-shrink: 0;
}

.question-preview {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.title-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.course-badge {
  background-color: #f0f2f5;
  color: #606266;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
}

.time-text {
  color: #909399;
  font-size: 13px;
}

.error-detail {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 12px;
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.question-content {
  line-height: 1.8;
  color: #606266;
  font-size: 14px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.option-item.is-correct {
  background-color: rgba(103, 194, 58, 0.1);
  border-color: #67c23a;
}

.option-item.is-wrong {
  background-color: rgba(245, 108, 108, 0.1);
  border-color: #f56c6c;
}

.option-key {
  font-weight: 600;
  margin-right: 10px;
  min-width: 24px;
}

.option-text {
  flex: 1;
}

.correct-icon {
  color: #67c23a;
  margin-left: auto;
  font-size: 18px;
}

.wrong-icon {
  color: #f56c6c;
  margin-left: auto;
  font-size: 18px;
}

.answer-section {
  margin-bottom: 20px;
}

.answer-box {
  padding: 16px;
  border-radius: 8px;
}

.answer-box.wrong-answer {
  background-color: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.3);
}

.answer-box.correct-answer {
  background-color: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.3);
}

.answer-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  margin-bottom: 10px;
  font-size: 14px;
}

.answer-box.wrong-answer .answer-label {
  color: #f56c6c;
}

.answer-box.correct-answer .answer-label {
  color: #67c23a;
}

.answer-content {
  line-height: 1.6;
  font-size: 14px;
}

.analysis-section {
  background-color: rgba(64, 158, 255, 0.1);
  padding: 16px;
  border-radius: 8px;
  border: 1px solid rgba(64, 158, 255, 0.3);
}

.analysis-content {
  line-height: 1.8;
  color: #606266;
  font-size: 14px;
}

.detail-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
  margin-top: 16px;
}

.meta-info {
  display: flex;
  gap: 24px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #909399;
  font-size: 13px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

/* 空状态容器 */
.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 60px 20px;
  background: linear-gradient(135deg, #fafafa 0%, #f5f7fa 100%);
  border-radius: 16px;
}

.empty-container :deep(.el-empty__description) {
  color: #909399;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input,
  .filter-select,
  .sort-select {
    width: 100%;
  }
  
  .empty-container {
    min-height: 300px;
    padding: 40px 16px;
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .stats-grid {
    flex-direction: column;
    gap: 16px;
  }
  
  .error-title-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .title-right {
    width: 100%;
    justify-content: space-between;
  }
  
  .detail-footer {
    flex-direction: column;
    gap: 12px;
  }
  
  .meta-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .answer-section .el-col {
    margin-bottom: 12px;
  }
}
</style>
