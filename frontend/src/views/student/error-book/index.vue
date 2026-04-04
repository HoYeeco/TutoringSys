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
                  <el-dropdown-item command="excel"
                    >导出为 Excel</el-dropdown-item
                  >
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
                  <el-tag
                    :type="getQuestionTypeTag(error.type)"
                    size="small"
                    class="type-tag"
                  >
                    {{ getQuestionTypeText(error.type) }}
                  </el-tag>
                  <span class="question-preview">{{
                    truncateText(error.content, 50)
                  }}</span>
                </div>
                <div class="title-right">
                  <span class="assignment-badge">{{ error.assignmentTitle }}</span>
                  <span class="course-badge">{{ error.courseName }}</span>
                  <span class="time-text">{{
                    formatDate(error.createTime)
                  }}</span>
                </div>
              </div>
            </template>

            <div class="error-detail">
              <div class="detail-section">
                <div class="section-title">错题详情</div>
                <div
                  class="question-content"
                  v-html="error.content"
                ></div>
              </div>

              <div class="detail-section" v-if="error.options && parseOptions(error.options).length > 0">
                <div class="options-list">
                  <div
                    v-for="(option, idx) in parseOptions(error.options)"
                    :key="idx"
                    class="option-item"
                    :class="{
                      'is-correct': isCorrectOption(
                        error.correctAnswer,
                        String(idx),
                        idx,
                      ),
                      'is-wrong':
                        isStudentAnswer(error.studentAnswer, String(idx), idx) &&
                        !isCorrectOption(error.correctAnswer, String(idx), idx),
                    }"
                  >
                    <span class="option-key">{{ getOptionLabel(idx, idx) }}.</span>
                    <span class="option-text">{{ option }}</span>
                    <el-icon
                      v-if="isCorrectOption(error.correctAnswer, String(idx), idx)"
                      class="correct-icon"
                      ><CircleCheck
                    /></el-icon>
                    <el-icon
                      v-if="
                        isStudentAnswer(error.studentAnswer, String(idx), idx) &&
                        !isCorrectOption(error.correctAnswer, String(idx), idx)
                      "
                      class="wrong-icon"
                      ><CircleClose
                    /></el-icon>
                  </div>
                </div>
              </div>

              <el-row :gutter="20" class="answer-section">
                <el-col :span="12">
                  <div class="answer-box wrong-answer">
                    <div class="answer-label">
                      <el-icon><CircleClose /></el-icon>
                      我的答案
                    </div>
                    <div class="answer-content" v-if="isEssayType(error.type)" v-html="error.studentAnswer || '<span class=\'empty-text\'>暂无作答</span>'"></div>
                    <div class="answer-content" v-else>{{ formatAnswer(error.studentAnswer, error.type) }}</div>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="answer-box correct-answer">
                    <div class="answer-label">
                      <el-icon><CircleCheck /></el-icon>
                      参考答案
                    </div>
                    <div class="answer-content" v-if="isEssayType(error.type)" v-html="error.referenceAnswer || error.correctAnswer || '<span class=\'empty-text\'>教师未设置参考答案</span>'"></div>
                    <div class="answer-content" v-else>{{ formatAnswer(error.correctAnswer, error.type) }}</div>
                  </div>
                </el-col>
              </el-row>

              <div class="ai-feedback" v-if="isEssayType(error.type)">
                <div class="feedback-header">
                  <el-icon><Cpu /></el-icon>
                  <span>AI 批改反馈</span>
                </div>

                <template v-if="error.aiFeedback">
                  <div class="feedback-section" v-if="parseAiFeedback(error.aiFeedback).errorPoints?.length">
                    <div class="feedback-subtitle">核心错误点</div>
                    <div class="error-tags">
                      <el-tag
                        v-for="(point, idx) in parseAiFeedback(error.aiFeedback).errorPoints"
                        :key="idx"
                        type="danger"
                        effect="plain"
                        class="error-tag"
                      >
                        {{ point }}
                      </el-tag>
                    </div>
                  </div>

                  <div class="feedback-section" v-if="parseAiFeedback(error.aiFeedback).suggestions">
                    <div class="feedback-subtitle">修正建议</div>
                    <div class="suggestion-content">
                      {{ parseAiFeedback(error.aiFeedback).suggestions }}
                    </div>
                  </div>
                </template>
                <div class="no-feedback" v-else>
                  <el-icon><Warning /></el-icon>
                  <span>暂无AI批改结果</span>
                </div>
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
                <el-button
                  type="primary"
                  size="small"
                  @click="confirmRemove(error)"
                  plain
                >
                  <el-icon><Delete /></el-icon>
                  我已掌握
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
          justify="center"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Search,
  Refresh,
  Download,
  ArrowDown,
  CircleCheck,
  CircleClose,
  Folder,
  School,
  Delete,
  Notebook,
  Cpu,
  Warning,
} from '@element-plus/icons-vue';
import request from '@/utils/request';

const searchKeyword = ref('');
const filterCourseId = ref('');
const activeNames = ref<string[]>([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalCount = ref(0);

const errorList = ref<any[]>([]);
const courses = ref<any[]>([]);

const getCourses = async () => {
  try {
    const response = await request.get('/student/error-book/stats');
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
    };

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value;
    }
    if (filterCourseId.value) {
      params.courseId = filterCourseId.value;
    }

    const response = await request.get('/student/error-book/list', { params });
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
      responseType: 'blob',
    });

    const blob = new Blob([response.data as BlobPart], {
      type:
        format === 'pdf'
          ? 'application/pdf'
          : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
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
  ElMessageBox.confirm('确定要移除这道错题吗？移除后将无法恢复。', '移除确认', {
    confirmButtonText: '确定移除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      removeError(error.id);
    })
    .catch(() => {});
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

const isEssayType = (type: string) => {
  const lowerType = type?.toLowerCase();
  return lowerType === 'essay' || lowerType === 'subjective' || lowerType === 'ess';
};

const parseAiFeedback = (aiFeedback: string | null) => {
  if (!aiFeedback) return { errorPoints: undefined, suggestions: undefined };
  
  const errorPoints: string[] = [];
  let suggestions = '';

  const errorMatch = aiFeedback.match(/【错误点】([^【]+)/);
  if (errorMatch) {
    const errorStr = errorMatch[1].trim();
    if (errorStr) {
      errorPoints.push(...errorStr.split('、').map((e: string) => e.trim()).filter((e: string) => e));
    }
  }

  const suggestionMatch = aiFeedback.match(/【改进建议】(.+)$/);
  if (suggestionMatch) {
    suggestions = suggestionMatch[1].trim();
  }

  if (errorPoints.length === 0 && !suggestions) {
    suggestions = aiFeedback;
  }

  return {
    errorPoints: errorPoints.length > 0 ? errorPoints : undefined,
    suggestions: suggestions.trim() || undefined,
  };
};

const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    judge: '判断题',
    essay: '主观题',
    SINGLE: '单选题',
    MULTIPLE: '多选题',
    JUDGE: '判断题',
    JUDGMENT: '判断题',
    ESSAY: '主观题',
    SUBJECTIVE: '主观题',
  };
  return typeMap[type] || type;
};

const getQuestionTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    single: 'primary',
    multiple: 'primary',
    judgment: 'primary',
    judge: 'primary',
    essay: 'primary',
    SINGLE: 'primary',
    MULTIPLE: 'primary',
    JUDGE: 'primary',
    JUDGMENT: 'primary',
    ESSAY: 'primary',
    SUBJECTIVE: 'primary',
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
    day: '2-digit',
  });
};

const parseOptions = (options: any) => {
  if (Array.isArray(options)) {
    return options;
  }
  try {
    if (typeof options === 'string') {
      const parsed = JSON.parse(options);
      if (Array.isArray(parsed)) {
        return parsed;
      }
      return parsed || {};
    }
    return options || {};
  } catch {
    return {};
  }
};

const getOptionLabel = (key: string | number, index: number) => {
  const keyStr = String(key);
  if (/^[A-Da-d]$/.test(keyStr)) {
    return keyStr.toUpperCase();
  }
  return String.fromCharCode(65 + index);
};

const isCorrectOption = (correctAnswer: string, key: string, index: number) => {
  if (!correctAnswer) return false;
  const answers = correctAnswer.toUpperCase().split(',');
  const optionLabel = getOptionLabel(key, index);
  return answers.includes(optionLabel);
};

const isStudentAnswer = (studentAnswer: string, key: string, index: number) => {
  if (!studentAnswer) return false;
  const answers = studentAnswer.toUpperCase().split(',');
  const optionLabel = getOptionLabel(key, index);
  return answers.includes(optionLabel);
};

const formatAnswer = (answer: string, type: string) => {
  if (!answer) return '-';
  const lowerType = type?.toLowerCase();
  if (lowerType === 'judgment' || lowerType === 'judge') {
    return answer === 'true' || answer === '正确' ? '正确' : '错误';
  }
  if (lowerType === 'multiple') {
    if (answer.startsWith('[')) {
      try {
        const arr = JSON.parse(answer);
        if (Array.isArray(arr)) {
          return arr
            .filter((a: string) => /^[A-Da-d]$/.test(a))
            .map((a: string) => a.toUpperCase())
            .sort()
            .join('');
        }
      } catch {
        // 解析失败，继续处理
      }
    }
    return answer.toUpperCase().split('').filter((c: string) => /[A-D]/.test(c)).sort().join('');
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
  width: 250px;
}

.filter-select {
  width: 150px;
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
  background-color: #fff;
  color: #606266;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  border: 1px solid #dcdfe6;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 20px;
  width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.assignment-badge {
  background-color: #fff;
  color: #606266;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  border: 1px solid #dcdfe6;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 20px;
  width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  flex-wrap: wrap;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  flex: 1;
  min-width: 200px;
  max-width: calc(50% - 5px);
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
  display: flex;
}

.answer-section .el-col {
  display: flex;
}

.answer-box {
  padding: 16px;
  border-radius: 8px;
  flex: 1;
  display: flex;
  flex-direction: column;
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
  flex: 1;
  text-align: left;
}

.answer-content :deep(p) {
  margin: 0 0 8px 0;
}

.answer-content :deep(p:last-child) {
  margin-bottom: 0;
}

.answer-content :deep(ul),
.answer-content :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.answer-content :deep(li) {
  margin: 4px 0;
}

.answer-content :deep(strong) {
  font-weight: 600;
}

.answer-content :deep(em) {
  font-style: italic;
}

.answer-content :deep(u) {
  text-decoration: underline;
}

.ai-feedback {
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f7ff 100%);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #d9ecff;
  margin-bottom: 20px;
}

.feedback-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 16px;
}

.feedback-section {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.feedback-section:last-child {
  margin-bottom: 0;
}

.feedback-subtitle {
  font-size: 13px;
  color: #409eff;
  font-weight: 800;
  margin-bottom: 8px;
  text-align: center;
  width: 100%;
}

.error-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.error-tag {
  font-size: 12px;
}

.suggestion-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  text-align: left;
}

.no-feedback {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
  padding: 16px 0;
}

.empty-text {
  color: #c0c4cc;
  font-style: italic;
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
  .filter-select {
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
