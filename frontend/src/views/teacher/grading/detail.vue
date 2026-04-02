<template>
  <div class="review-page">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <div class="header-title">
          <h2>批改复核</h2>
          <span v-if="isBatchMode" class="batch-progress">
            ({{ currentIndex + 1 }} / {{ answerIds.length }})
          </span>
        </div>
      </div>
      <div class="header-info">
        <span class="info-item">
          <el-icon><User /></el-icon>
          {{ reviewDetail.studentName }}
        </span>
        <span class="info-item">
          <el-icon><Document /></el-icon>
          {{ reviewDetail.assignmentTitle }}
        </span>
        <span class="info-item">
          <el-icon><Clock /></el-icon>
          {{ formatDateTime(reviewDetail.submitTime) }}
        </span>
      </div>
    </div>

    <div class="review-content" v-loading="loading">
      <div class="question-block">
        <div class="block-main" :class="{ 'full-width': isObjectiveQuestion }">
          <div class="left-section">
            <div class="question-box">
              <div class="box-header">
                <span class="box-title">题目</span>
                <span class="score-tag">满分 {{ reviewDetail.maxScore }} 分</span>
                <el-tag size="small" :type="isObjectiveQuestion ? 'info' : 'warning'">
                  {{ getQuestionTypeLabel(reviewDetail.questionType) }}
                </el-tag>
              </div>
              <div class="box-content">
                <div class="question-text" v-html="reviewDetail.questionContent"></div>
                <div v-if="reviewDetail.questionOptions && reviewDetail.questionOptions.length > 0" class="options-list">
                  <div
                    v-for="(option, idx) in reviewDetail.questionOptions"
                    :key="idx"
                    class="option-item"
                    :class="{ 'is-correct': reviewDetail.correctAnswer?.includes(String.fromCharCode(65 + idx)) }"
                  >
                    <span class="option-letter">{{ String.fromCharCode(65 + idx) }}</span>
                    <span class="option-text">{{ option }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="answer-box">
              <div class="box-header">
                <span class="box-title">学生作答</span>
                <el-tag v-if="isObjectiveQuestion" :type="isCorrect ? 'success' : 'danger'" size="small">
                  {{ isCorrect ? '正确' : '错误' }}
                </el-tag>
                <el-tag v-else-if="!reviewDetail.studentAnswer" type="info" size="small">未作答</el-tag>
              </div>
              <div class="box-content">
                <div class="answer-text" :class="{ 'empty': !reviewDetail.studentAnswer }">
                  <pre class="formatted-answer">{{ htmlToFormattedText(reviewDetail.studentAnswer) || '学生未作答' }}</pre>
                </div>
                <div v-if="isObjectiveQuestion && reviewDetail.correctAnswer" class="correct-answer">
                  正确答案：{{ reviewDetail.correctAnswer }}
                </div>
              </div>
            </div>
          </div>

          <div v-if="!isObjectiveQuestion" class="right-section">
            <div class="ai-box">
              <div class="box-header">
                <span class="box-title">AI 批改结果</span>
                <el-button
                  type="primary"
                  size="small"
                  :loading="regrading"
                  @click="handleRegrade"
                  plain
                >
                  重新生成
                </el-button>
              </div>
              <div class="box-content">
                <div class="score-row">
                  <span class="score-label">得分</span>
                  <span class="score-value" :class="getScoreClass(reviewDetail.aiScore, reviewDetail.maxScore)">
                    {{ reviewDetail.aiScore ?? '-' }}
                  </span>
                  <span class="score-max">/ {{ reviewDetail.maxScore }}</span>
                  <el-tag v-if="regraded" type="success" size="small">已重新评分</el-tag>
                </div>

                <div v-if="reviewDetail.aiFeedback" class="ai-feedback">
                  <!-- 错误点 - Tag 形式 -->
                  <div v-if="parsedFeedback.errors.length > 0" class="error-section">
                    <div class="section-label">
                      <el-icon><Warning /></el-icon>
                      <span>错误点</span>
                    </div>
                    <div class="tag-list">
                      <el-tag
                        v-for="(error, idx) in parsedFeedback.errors"
                        :key="idx"
                        :type="error === '无' ? 'success' : 'danger'"
                        size="small"
                        effect="light"
                        class="error-tag"
                      >
                        {{ error }}
                      </el-tag>
                    </div>
                  </div>

                  <!-- 改进建议 - 文本形式 -->
                  <div v-if="parsedFeedback.suggestions" class="suggestion-section">
                    <div class="section-label">
                      <el-icon><InfoFilled /></el-icon>
                      <span>改进建议</span>
                    </div>
                    <p class="suggestion-content" :class="{ 'is-truncated': shouldTruncateSuggestion }">
                      {{ parsedFeedback.suggestions }}
                    </p>
                    <el-button
                      v-if="shouldTruncateSuggestion"
                      type="primary"
                      text
                      size="small"
                      @click="showFullFeedback"
                    >
                      查看完整内容
                    </el-button>
                  </div>
                </div>
                <div v-else class="empty-feedback">
                  <span>暂无 AI 批改结果</span>
                  <span class="hint">点击"重新生成"获取 AI 批改结果</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="!isObjectiveQuestion" class="block-footer">
          <div class="teacher-action">
            <div class="action-row">
              <span class="action-label">复核操作</span>
              <div class="action-btns">
                <el-button
                  :type="feedbackAction === 'adopt' ? 'primary' : 'default'"
                  @click="feedbackAction = 'adopt'"
                  plain
                >
                  <el-icon><Check /></el-icon>
                  采用 AI 批改结果
                </el-button>
                <el-button
                  :type="feedbackAction === 'modify' ? 'warning' : 'default'"
                  @click="feedbackAction = 'modify'"
                >
                  <el-icon><Edit /></el-icon>
                  修改分数
                </el-button>
              </div>
              <div v-if="feedbackAction === 'modify'" class="modify-score">
                <el-input-number
                  v-model="manualScore"
                  :min="0"
                  :max="reviewDetail.maxScore || 100"
                  size="small"
                />
                <span class="score-unit">分</span>
              </div>
            </div>
            <div class="feedback-row">
              <span class="action-label">教师评语</span>
              <el-input
                v-model="teacherFeedbackText"
                type="textarea"
                :rows="2"
                placeholder="可选填写教师评语"
                resize="none"
              />
            </div>
            <div class="submit-row">
              <el-button @click="goBack">取消</el-button>
              <el-button type="primary" @click="completeReview" :loading="submitting">
                确认复核
              </el-button>
            </div>
          </div>
        </div>

        <!-- 批量模式导航 -->
        <div v-if="isBatchMode" class="batch-nav">
          <el-button
            :disabled="currentIndex === 0"
            @click="goToPrev"
            size="large"
          >
            <el-icon><ArrowLeft /></el-icon>
            上一个
          </el-button>
          <div class="batch-actions">
            <el-button type="success" @click="batchAdopt" :loading="batchSubmitting">
              <el-icon><Check /></el-icon>
              批量采用
            </el-button>
            <el-button @click="batchSkip" :loading="batchSubmitting">
              <el-icon><Right /></el-icon>
              批量跳过
            </el-button>
          </div>
          <el-button
            :disabled="currentIndex === answerIds.length - 1"
            @click="goToNext"
            size="large"
          >
            下一个
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="showFeedbackDialog" title="修正建议详情" width="600px">
      <div class="dialog-content">
        <div v-if="parsedFeedback.errors.length > 0" class="dialog-section">
          <div class="section-title">核心错误点</div>
          <div class="error-tag-list">
            <el-tag v-for="(error, idx) in parsedFeedback.errors" :key="idx" type="danger" effect="plain">
              {{ error }}
            </el-tag>
          </div>
        </div>
        <div v-if="parsedFeedback.suggestions" class="dialog-section">
          <div class="section-title">修正建议</div>
          <p class="suggestion-full">{{ parsedFeedback.suggestions }}</p>
        </div>
        <div v-if="!parsedFeedback.errors.length && !parsedFeedback.suggestions && reviewDetail.aiFeedback" class="dialog-section">
          <p class="feedback-full">{{ reviewDetail.aiFeedback }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ArrowRight, User, Document, Clock, Check, Edit, Warning, InfoFilled, Right } from '@element-plus/icons-vue';
import request from '@/utils/request';

interface ReviewDetail {
  answerId: number;
  studentId: number;
  studentName: string;
  assignmentId: number;
  assignmentTitle: string;
  courseId: number;
  courseName: string;
  questionId: number;
  questionType: string;
  questionContent: string;
  questionOptions: string[];
  maxScore: number;
  studentAnswer: string;
  correctAnswer: string;
  aiScore: number;
  aiFeedback: string;
  finalScore: number;
  teacherFeedback: string;
  graderType: string;
  reviewStatus: number;
  submitTime: string;
  aiGradeTime: string;
}

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const submitting = ref(false);
const batchSubmitting = ref(false);
const regrading = ref(false);
const regraded = ref(false);
const showFeedbackDialog = ref(false);
const teacherFeedbackText = ref('');

// 批量模式相关
const isBatchMode = computed(() => answerIds.value.length > 1);
const answerIds = ref<number[]>([]);
const currentIndex = ref(0);
const currentAnswerId = computed(() => answerIds.value[currentIndex.value] || route.params.id as string);

const reviewDetail = ref<ReviewDetail>({
  answerId: 0,
  studentId: 0,
  studentName: '',
  assignmentId: 0,
  assignmentTitle: '',
  courseId: 0,
  courseName: '',
  questionId: 0,
  questionType: '',
  questionContent: '',
  questionOptions: [],
  maxScore: 0,
  studentAnswer: '',
  correctAnswer: '',
  aiScore: 0,
  aiFeedback: '',
  finalScore: 0,
  teacherFeedback: '',
  graderType: '',
  reviewStatus: 0,
  submitTime: '',
  aiGradeTime: ''
});

const feedbackAction = ref('adopt');
const manualScore = ref(0);

const isObjectiveQuestion = computed(() => {
  const type = reviewDetail.value.questionType?.toUpperCase();
  return type === 'SINGLE' || type === 'MULTIPLE' || type === 'JUDGE';
});

const isCorrect = computed(() => {
  if (!reviewDetail.value.correctAnswer || !reviewDetail.value.studentAnswer) {
    return false;
  }
  const correct = reviewDetail.value.correctAnswer.trim().toUpperCase();
  const student = reviewDetail.value.studentAnswer.trim().toUpperCase();
  return correct === student;
});

const parsedFeedback = computed(() => {
  const feedback = reviewDetail.value.aiFeedback || '';
  const errors: string[] = [];
  let suggestions = '';

  // 提取错误点 - 使用顿号、分隔
  const errorMatch = feedback.match(/【错误点】([^【]+)/);
  if (errorMatch) {
    const errorStr = errorMatch[1].trim();
    if (errorStr) {
      // 使用顿号分隔多个错误点
      errors.push(...errorStr.split('、').map(e => e.trim()).filter(e => e));
    }
  }

  // 提取改进建议
  const suggestionMatch = feedback.match(/【改进建议】(.+)$/);
  if (suggestionMatch) {
    suggestions = suggestionMatch[1].trim();
  }

  return { errors, suggestions };
});

const shouldTruncateSuggestion = computed(() => {
  return parsedFeedback.value.suggestions.length > 100;
});

const shouldTruncateFeedback = computed(() => {
  return (reviewDetail.value.aiFeedback?.length || 0) > 100;
});

const getQuestionTypeLabel = (type: string): string => {
  const typeMap: Record<string, string> = {
    SINGLE: '单选题',
    MULTIPLE: '多选题',
    JUDGE: '判断题',
    SUBJECTIVE: '主观题',
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '主观题'
  };
  return typeMap[type] || '未知题型';
};

const getScoreClass = (score: number, maxScore: number): string => {
  if (score === undefined || score === null) return '';
  const percent = (score / maxScore) * 100;
  if (percent >= 80) return 'score-high';
  if (percent >= 60) return 'score-medium';
  return 'score-low';
};

// 去除 HTML 标签（完全去除，用于纯文本显示）
const stripHtml = (html: string | null | undefined): string => {
  if (!html) return '';
  // 先替换常见的 HTML 实体
  let text = html
    .replace(/&nbsp;/g, ' ')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'");
  // 去除 HTML 标签
  text = text.replace(/<[^>]+>/g, '');
  // 去除多余空格
  text = text.replace(/\s+/g, ' ').trim();
  return text;
};

// 保留格式的 HTML 转文本（保留换行、段落等格式）
const htmlToFormattedText = (html: string | null | undefined): string => {
  if (!html) return '';
  
  let text = html;
  
  // 先处理无序列表 - 在<ul>内的<li>前添加标记
  text = text.replace(/<ul[^>]*>/gi, '\n<UL_START>\n');
  text = text.replace(/<\/ul>/gi, '\n<UL_END>\n');
  text = text.replace(/<li[^>]*>/gi, '<LI_START>');
  text = text.replace(/<\/li>/gi, '<LI_END>\n');
  
  // 处理段落标签，转换为双换行
  text = text.replace(/<p[^>]*>/gi, '\n');
  text = text.replace(/<\/p>/gi, '\n\n');
  text = text.replace(/<br\s*\/?>/gi, '\n');
  text = text.replace(/<div[^>]*>/gi, '\n');
  text = text.replace(/<\/div>/gi, '\n');
  
  // 替换 HTML 实体
  text = text
    .replace(/&nbsp;/g, ' ')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'");
  
  // 去除所有 HTML 标签（除了我们的标记）
  text = text.replace(/<(?!UL_START|UL_END|LI_START|LI_END)[^>]+>/g, '');
  
  // 处理列表标记
  let inUnorderedList = false;
  const lines = text.split('\n');
  const processedLines: string[] = [];
  
  for (let line of lines) {
    const trimmedLine = line.trim();
    
    if (trimmedLine.includes('<UL_START>')) {
      inUnorderedList = true;
      const cleanLine = trimmedLine.replace(/<UL_START>/g, '').trim();
      if (cleanLine) processedLines.push(cleanLine);
    } else if (trimmedLine.includes('<UL_END>')) {
      inUnorderedList = false;
      const cleanLine = trimmedLine.replace(/<UL_END>/g, '').trim();
      if (cleanLine) processedLines.push(cleanLine);
    } else if (trimmedLine.includes('<LI_START>')) {
      // 处理<li>内容
      let liContent = trimmedLine
        .replace(/<LI_START>/g, '')
        .replace(/<LI_END>/g, '')
        .trim();
      
      if (liContent) {
        if (inUnorderedList) {
          processedLines.push('• ' + liContent);
        } else {
          processedLines.push(liContent);
        }
      }
    } else if (trimmedLine) {
      // 普通行，清理可能残留的 LI_END 标记
      const cleanLine = trimmedLine.replace(/<LI_END>/g, '').trim();
      if (cleanLine) processedLines.push(cleanLine);
    }
  }
  
  text = processedLines.join('\n');
  
  // 清理多余空行（最多保留 2 个连续换行）
  text = text.replace(/\n{3,}/g, '\n\n');
  
  return text.trim();
};

const formatDateTime = (date: string | number[] | null): string => {
  if (!date) return '-';
  try {
    let d: Date;
    if (Array.isArray(date)) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = date;
      d = new Date(year, month - 1, day, hour, minute, second);
    } else if (typeof date === 'string') {
      if (date.includes('T')) {
        d = new Date(date);
      } else {
        d = new Date(date);
      }
    } else {
      d = new Date(date);
    }
    if (isNaN(d.getTime())) return '-';
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  } catch {
    return '-';
  }
};

const showFullFeedback = () => {
  showFeedbackDialog.value = true;
};

const getReviewDetail = async () => {
  loading.value = true;
  try {
    const response = await request.get(`/teacher/review/${currentAnswerId.value}`);
    const data = response.data;
    reviewDetail.value = {
      answerId: data.answerId || 0,
      studentId: data.studentId || 0,
      studentName: data.studentName || '',
      assignmentId: data.assignmentId || 0,
      assignmentTitle: data.assignmentTitle || '',
      courseId: data.courseId || 0,
      courseName: data.courseName || '',
      questionId: data.questionId || 0,
      questionType: data.questionType || '',
      questionContent: data.questionContent || '',
      questionOptions: data.questionOptions || [],
      maxScore: data.maxScore || 0,
      studentAnswer: data.studentAnswer || '',
      correctAnswer: data.correctAnswer || '',
      aiScore: data.aiScore,
      aiFeedback: data.aiFeedback || '',
      finalScore: data.finalScore,
      teacherFeedback: data.teacherFeedback || '',
      graderType: data.graderType || '',
      reviewStatus: data.reviewStatus || 0,
      submitTime: data.submitTime || '',
      aiGradeTime: data.aiGradeTime || ''
    };
    manualScore.value = data.aiScore || 0;
    teacherFeedbackText.value = data.teacherFeedback || '';
  } catch (error) {
    ElMessage.error('获取复核详情失败');
    router.push('/teacher/grading');
  } finally {
    loading.value = false;
  }
};

const handleRegrade = async () => {
  regrading.value = true;
  try {
    console.log('开始重新生成 AI 批改:', currentAnswerId.value);
    const response = await request.post(`/teacher/review/${currentAnswerId.value}/regrade`);
    console.log('AI 批改返回数据:', response.data);
    const data = response.data;
    reviewDetail.value = {
      answerId: data.answerId || 0,
      studentId: data.studentId || 0,
      studentName: data.studentName || '',
      assignmentId: data.assignmentId || 0,
      assignmentTitle: data.assignmentTitle || '',
      courseId: data.courseId || 0,
      courseName: data.courseName || '',
      questionId: data.questionId || 0,
      questionType: data.questionType || '',
      questionContent: data.questionContent || '',
      questionOptions: data.questionOptions || [],
      maxScore: data.maxScore || 0,
      studentAnswer: data.studentAnswer || '',
      correctAnswer: data.correctAnswer || '',
      aiScore: data.aiScore,
      aiFeedback: data.aiFeedback || '',
      finalScore: data.finalScore,
      teacherFeedback: data.teacherFeedback || '',
      graderType: data.graderType || '',
      reviewStatus: data.reviewStatus || 0,
      submitTime: data.submitTime || '',
      aiGradeTime: data.aiGradeTime || ''
    };
    manualScore.value = data.aiScore || 0;
    regraded.value = true;
    console.log('AI 批改结果 - aiScore:', data.aiScore, 'aiFeedback:', data.aiFeedback);
    ElMessage.success('AI 批改完成');
  } catch (error) {
    console.error('AI 批改失败:', error);
    ElMessage.error('AI 批改失败，请稍后重试');
  } finally {
    regrading.value = false;
  }
};

const goBack = () => {
  router.push('/teacher/grading');
};

const completeReview = async () => {
  submitting.value = true;
  try {
    if (feedbackAction.value === 'adopt') {
      await request.post(`/teacher/review/${currentAnswerId.value}/accept`, null, {
        params: { teacherFeedback: teacherFeedbackText.value }
      });
    } else {
      await request.post(`/teacher/review/${currentAnswerId.value}/modify`, null, {
        params: {
          newScore: manualScore.value,
          teacherFeedback: teacherFeedbackText.value
        }
      });
    }

    ElMessage.success('复核完成');

    // 批量模式下自动跳到下一个
    if (isBatchMode.value && currentIndex.value < answerIds.value.length - 1) {
      goToNext();
    } else {
      router.push('/teacher/grading');
    }
  } catch (error) {
    ElMessage.error('操作失败');
  } finally {
    submitting.value = false;
  }
};

// 批量模式方法
const goToPrev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
    getReviewDetail();
  }
};

const goToNext = () => {
  if (currentIndex.value < answerIds.value.length - 1) {
    currentIndex.value++;
    getReviewDetail();
  }
};

const batchAdopt = async () => {
  batchSubmitting.value = true;
  try {
    // 采用当前AI评分
    await request.post(`/teacher/review/${currentAnswerId.value}/accept`, null, {
      params: { teacherFeedback: teacherFeedbackText.value }
    });
    ElMessage.success('已采用AI评分');

    // 自动跳到下一个
    if (currentIndex.value < answerIds.value.length - 1) {
      goToNext();
    } else {
      ElMessage.success('所有复核已完成');
      router.push('/teacher/grading');
    }
  } catch (error) {
    ElMessage.error('操作失败');
  } finally {
    batchSubmitting.value = false;
  }
};

const batchSkip = () => {
  // 跳过当前，跳到下一个
  if (currentIndex.value < answerIds.value.length - 1) {
    goToNext();
  } else {
    ElMessage.info('已经是最后一个了');
  }
};

onMounted(() => {
  // 解析路由参数
  const idsParam = route.query.ids as string;
  if (idsParam) {
    // 批量模式：ids=1,2,3,4
    answerIds.value = idsParam.split(',').map(id => parseInt(id)).filter(id => !isNaN(id));
    const idxParam = route.query.index as string;
    if (idxParam) {
      currentIndex.value = parseInt(idxParam) || 0;
    }
  } else {
    // 单条模式
    answerIds.value = [parseInt(route.params.id as string)];
  }
  getReviewDetail();
});
</script>

<style scoped>
.review-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  padding: 16px 24px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  color: rgba(255, 255, 255, 0.9);
  background-color: transparent !important;
  
  &:hover {
    background-color: #022aa1 !important;
  }
  
  &:active {
    background-color: #022aa1 !important;
  }
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.batch-progress {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.15);
  padding: 2px 10px;
  border-radius: 12px;
}

.header-info {
  display: flex;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

.review-content {
  padding: 20px;
}

.question-block {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.block-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 400px;
}

.block-main.full-width {
  grid-template-columns: 1fr;
}

.left-section,
.right-section {
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.left-section {
  border-right: 1px solid #f0f0f0;
  gap: 16px;
}

.block-main.full-width .left-section {
  border-right: none;
}

.question-box,
.answer-box,
.ai-box {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.box-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.box-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.score-tag {
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  color: #fff;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.box-content {
  flex: 1;
  overflow: auto;
}

.question-text {
  font-size: 14px;
  line-height: 1.7;
  color: #303133;
  margin-bottom: 12px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 12px;
  background: #fafafa;
  border-radius: 6px;
  font-size: 13px;
}

.option-item.is-correct {
  background: #f0f9eb;
}

.option-letter {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e4e7ed;
  border-radius: 50%;
  font-weight: 600;
  font-size: 12px;
  margin-right: 10px;
  flex-shrink: 0;
}

.option-item.is-correct .option-letter {
  background: #67c23a;
  color: #fff;
}

.option-text {
  color: #303133;
  line-height: 1.5;
}

.answer-text {
  font-size: 14px;
  line-height: 1.7;
  color: #303133;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  min-height: 60px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.formatted-answer {
  font-family: inherit;
  font-size: inherit;
  line-height: inherit;
  color: inherit;
  background: transparent;
  border: none;
  padding: 0;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  width: 100%;
  max-width: 600px;
  text-align: left;
}

.answer-text.empty {
  color: #909399;
  background: #fef0f0;
}

.correct-answer {
  margin-top: 10px;
  padding: 10px 12px;
  background: #f0f9eb;
  border-radius: 6px;
  font-size: 13px;
  color: #67c23a;
}

.score-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 16px;
}

.score-label {
  font-size: 14px;
  color: #909399;
}

.score-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.score-value.score-high { color: #67c23a; }
.score-value.score-medium { color: #e6a23c; }
.score-value.score-low { color: #f56c6c; }

.score-max {
  font-size: 16px;
  color: #909399;
}

.ai-feedback {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-summary {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.summary-content {
  font-size: 14px;
  line-height: 1.7;
  color: #303133;
  margin: 0;
}

.error-section,
.suggestion-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.section-label .el-icon {
  font-size: 16px;
}

.error-section .section-label {
  color: #f56c6c;
}

.suggestion-section .section-label {
  color: #409eff;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.error-tag {
  font-size: 13px;
  padding: 4px 10px;
}

.suggestion-content {
  font-size: 14px;
  line-height: 1.7;
  color: #303133;
  margin: 0;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

.suggestion-content.is-truncated {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.feedback-raw {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  margin: 0;
}

.feedback-raw.is-truncated {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.empty-feedback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  color: #909399;
  font-size: 14px;
}

.empty-feedback .hint {
  font-size: 12px;
  margin-top: 8px;
}

.block-footer {
  border-top: 1px solid #f0f0f0;
  padding: 20px;
  background: #fafafa;
}

.teacher-action {
  max-width: 800px;
  margin: 0 auto;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.action-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  min-width: 70px;
}

.action-btns {
  display: flex;
  gap: 10px;
}

.modify-score {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 16px;
}

.score-unit {
  font-size: 14px;
  color: #909399;
}

.feedback-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.feedback-row .el-textarea {
  flex: 1;
}

.submit-row {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 批量导航 */
.batch-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f5f7fa;
  border-top: 1px solid #e4e7ed;
}

.batch-actions {
  display: flex;
  gap: 12px;
}

.dialog-content {
  padding: 10px 0;
}

.dialog-section {
  margin-bottom: 20px;
}

.dialog-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.error-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.suggestion-full,
.feedback-full {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
  margin: 0;
}

@media (max-width: 900px) {
  .block-main {
    grid-template-columns: 1fr;
  }

  .left-section {
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }

  .header-info {
    display: none;
  }

  .batch-nav {
    flex-direction: column;
    gap: 12px;
  }

  .batch-actions {
    order: -1;
  }
}
</style>
