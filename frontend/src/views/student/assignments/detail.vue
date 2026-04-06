<template>
  <div class="assignment-detail">
    <el-card shadow="never" class="header-card">
      <template #header>
        <div class="card-title">
          <div class="card-title__left">
            <div class="card-title__icon">
              <el-icon><Document /></el-icon>
            </div>
            <span class="card-title__text">{{ assignment.title || '批改详情' }}</span>
          </div>
          <el-button class="back-btn" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回列表
          </el-button>
        </div>
      </template>
      <div class="assignment-summary">
        <div class="summary-item">
          <span class="summary-label">所属课程：</span>
          <span class="summary-value">{{ assignment.courseName }}</span>
        </div>
        <div class="summary-item">
          <span class="summary-label">作业状态：</span>
          <el-tag :type="getStatusTagType(assignment.status)" effect="light" size="small">
            {{ getStatusText(assignment.status) }}
          </el-tag>
        </div>
        <div class="summary-item">
          <span class="summary-label">总分：</span>
          <span class="summary-value score">
            <strong>{{ assignment.score || 0 }}</strong> / {{ assignment.totalScore || 100 }}
          </span>
        </div>
        <div class="summary-item">
          <span class="summary-label">批改时间：</span>
          <span class="summary-value">{{ formatDate(assignment.gradeTime) }}</span>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="questions-card">
      <template #header>
        <div class="card-title">
          <div class="card-title__left">
            <div class="card-title__icon">
              <el-icon><List /></el-icon>
            </div>
            <span class="card-title__text">答题详情</span>
          </div>
          <el-button
            v-if="hasWrongQuestions && !allWrongInBook"
            type="primary"
            size="small"
            :loading="batchAdding"
            @click="addAllToWrongBook"
          >
            <el-icon><Collection /></el-icon> 一键加入错题本
          </el-button>
          <el-button
            v-else-if="hasWrongQuestions && allWrongInBook"
            type="info"
            size="small"
            disabled
          >
            <el-icon><Collection /></el-icon> 已全部加入错题本
          </el-button>
        </div>
      </template>

      <div class="questions-list">
        <div
          v-for="(question, index) in questions"
          :key="question.id"
          class="question-card"
          :class="getQuestionCardClass(question)"
        >
          <div class="question-header">
            <div class="question-info">
              <span class="question-number">第{{ index + 1 }}题</span>
              <el-tag size="small" :type="getQuestionTypeTag(question.type)">
                {{ getQuestionTypeText(question.type) }}
              </el-tag>
            </div>
            <div class="question-result">
              <span class="earned-score">
                得分：<strong>{{ question.earnedScore || 0 }}</strong> / {{ question.score }} 分
              </span>
            </div>
          </div>

          <div class="question-content" v-html="question.content"></div>

          <div class="answer-section">
            <div class="answer-row-two-col">
              <div class="answer-col">
                <span class="answer-label">我的答案：</span>
                <div
                  class="answer-value"
                  :class="getAnswerClass(question)"
                >
                  <template v-if="question.type === 'essay'">
                    <div class="essay-content readonly-editor" v-html="question.studentAnswer"></div>
                  </template>
                  <template v-else>
                    {{ formatAnswer(question.studentAnswer, question.type) }}
                  </template>
                </div>
              </div>

              <div class="answer-col">
                <span class="answer-label">参考答案：</span>
                <div class="answer-value reference-answer">
                  <template v-if="question.type === 'essay'">
                    <div class="essay-content readonly-editor" v-html="question.referenceAnswer || question.correctAnswer || '暂无参考答案'"></div>
                  </template>
                  <template v-else>
                    {{ formatAnswer(question.referenceAnswer || question.correctAnswer, question.type) || '暂无参考答案' }}
                  </template>
                </div>
              </div>
            </div>

            <div class="ai-feedback" v-if="question.type === 'essay' && question.aiFeedback">
              <div class="feedback-header">
                <el-icon><Cpu /></el-icon>
                <span>AI 批改反馈</span>
              </div>
              
              <div class="feedback-section" v-if="question.aiFeedback.errorPoints?.length">
                <div class="feedback-subtitle">核心错误点</div>
                <div class="error-tags">
                  <el-tag
                    v-for="(point, idx) in question.aiFeedback.errorPoints"
                    :key="idx"
                    type="danger"
                    effect="plain"
                    class="error-tag"
                  >
                    {{ point }}
                  </el-tag>
                </div>
              </div>

              <div class="feedback-section" v-if="question.aiFeedback.suggestions">
                <div class="feedback-subtitle">修正建议</div>
                <div class="suggestion-content">
                  {{ question.aiFeedback.suggestions }}
                  <el-button
                    type="primary"
                    link
                    size="small"
                    @click="copyText(question.aiFeedback.suggestions)"
                  >
                    <el-icon><CopyDocument /></el-icon> 复制
                  </el-button>
                </div>
              </div>
            </div>

            <div class="teacher-comment" v-if="question.teacherComment">
              <div class="comment-header">
                <el-icon><User /></el-icon>
                <span>教师评语</span>
              </div>
              <div class="comment-content">{{ question.teacherComment }}</div>
            </div>
          </div>

          <div class="question-footer" v-if="(question.earnedScore || 0) < (question.score || 0)">
            <el-button
              v-if="!question.inWrongBook"
              type="primary"
              size="small"
              :loading="question.addingToWrongBook"
              @click="addToWrongBook(question)"
              plain
            >
              <el-icon><Collection /></el-icon> 加入错题本
            </el-button>
            <el-tag v-else type="success" plain size="small" class="wrong-book-tag">
              <el-icon class="wrong-book-tag__icon"><Collection /></el-icon>
              <span>已加入错题本</span>
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Document,
  List,
  ArrowLeft,
  Collection,
  Cpu,
  CopyDocument,
  User,
} from '@element-plus/icons-vue';
import request from '@/utils/request';

const route = useRoute();
const router = useRouter();
const assignmentId = computed(() => route.params.id as string);

const assignment = ref({
  title: '',
  courseName: '',
  score: 0,
  totalScore: 100,
  gradeTime: '',
  status: 'graded',
});

const questions = ref<any[]>([]);
const batchAdding = ref(false);

const hasWrongQuestions = computed(() => {
  return questions.value.some(q => (q.earnedScore || 0) < (q.score || 0));
});

const allWrongInBook = computed(() => {
  const wrongQs = questions.value.filter(q => (q.earnedScore || 0) < (q.score || 0));
  return wrongQs.length > 0 && wrongQs.every(q => q.inWrongBook);
});

const getGradingDetail = async () => {
  try {
    const response = await request.get(`/student/grading/by-assignment/${assignmentId.value}`);
    const data = response.data;
    
    if (!data) {
      ElMessage.error('未找到该作业的批改记录');
      return;
    }
    
    const hasSubjective = data.answers?.some((a: any) => {
      const type = a.type?.toUpperCase();
      return type === 'ESSAY' || type === 'SUBJECTIVE';
    });
    const status = hasSubjective && data.reviewStatus === 1 ? 'submitted' : 'graded';
    
    assignment.value = {
      title: data.assignmentTitle,
      courseName: data.courseName,
      score: data.finalTotalScore,
      totalScore: data.totalScore,
      gradeTime: data.reviewTime,
      status: status,
    };
    
    questions.value = (data.answers || []).map((a: any) => ({
      id: a.questionId,
      type: a.type?.toLowerCase(),
      content: a.content,
      options: a.options,
      score: a.maxScore,
      studentAnswer: a.studentAnswer,
      correctAnswer: a.correctAnswer,
      referenceAnswer: a.referenceAnswer,
      isCorrect: a.isCorrect,
      earnedScore: a.finalScore ?? a.score,
      aiFeedback: parseAiFeedback(a.aiFeedback),
      teacherComment: a.teacherFeedback,
      addingToWrongBook: false,
      inWrongBook: false,
    }));

    await checkErrorBookStatus();
  } catch (error) {
    console.error('获取批改详情失败:', error);
    ElMessage.error('获取批改详情失败');
  }
};

const parseAiFeedback = (aiFeedback: string | null) => {
  if (!aiFeedback) return null;
  
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

const checkErrorBookStatus = async () => {
  try {
    const wrongQs = questions.value.filter(q => (q.earnedScore || 0) < (q.score || 0));
    if (wrongQs.length === 0) return;
    const questionIds = wrongQs.map(q => q.id);
    const res = await request.get('/student/error-book/check', {
      params: { assignmentId: Number(assignmentId.value), questionIds: questionIds.join(',') }
    });
    const existingIds = new Set(res.data || []);
    questions.value.forEach(q => {
      if (existingIds.has(q.id)) q.inWrongBook = true;
    });
  } catch {
    // 静默失败，不影响主流程
  }
};

const goBack = () => {
  router.push('/student/assignments');
};

const getQuestionTypeText = (type: string) => {
  const map: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '主观题',
    // 后端返回的大写类型
    SINGLE: '单选题',
    MULTIPLE: '多选题',
    JUDGE: '判断题',
    ESSAY: '主观题',
  };
  return map[type] || type;
};

const getQuestionTypeTag = (type: string) => {
  const map: Record<string, string> = {
    single: '',
    multiple: '',
    judgment: '',
    essay: '',
    // 后端返回的大写类型
    SINGLE: '',
    MULTIPLE: '',
    JUDGE: '',
    ESSAY: '',
  };
  return map[type] || '';
};

const formatAnswer = (answer: string, type: string) => {
  if (!answer) return '-';
  const lowerType = type?.toLowerCase();
  if (lowerType === 'multiple') {
    try {
      const parsed = JSON.parse(answer);
      if (Array.isArray(parsed)) {
        return parsed.map((s: string) => s.toUpperCase()).sort().join('');
      }
    } catch {
      return answer.toUpperCase().replace(/[^A-Z]/g, '').split('').sort().join('');
    }
    return answer.toUpperCase().replace(/[^A-Z]/g, '').split('').sort().join('');
  }
  if (lowerType === 'judgment' || lowerType === 'judge') {
    const trimmed = answer.trim().toLowerCase();
    if (trimmed === 'true') return '正确';
    if (trimmed === 'false') return '错误';
    return answer;
  }
  return answer;
};

const formatDate = (date: string) => {
  if (!date) return '-';
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
};

const getStatusTagType = (status: string) => {
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

// 获取题目卡片样式类：满分绿色、零分红色、其他橙色
const getQuestionCardClass = (question: any) => {
  const earned = question.earnedScore || 0;
  const total = question.score || 0;

  if (earned === total && total > 0) {
    return 'full-score';
  } else if (earned === 0) {
    return 'zero-score';
  } else {
    return 'partial-score';
  }
};

// 获取答案区域样式类
const getAnswerClass = (question: any) => {
  if (question.type === 'essay') return '';

  const earned = question.earnedScore || 0;
  const total = question.score || 0;

  if (earned === total && total > 0) {
    return 'full-score';
  } else if (earned === 0) {
    return 'zero-score';
  } else {
    return 'partial-score';
  }
};

const copyText = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text);
    ElMessage.success('已复制到剪贴板');
  } catch {
    ElMessage.error('复制失败');
  }
};

const addToWrongBook = async (question: any) => {
  question.addingToWrongBook = true;
  try {
    await request.post('/student/error-book/add', {
      questionId: question.id,
      assignmentId: Number(assignmentId.value),
    });
    question.inWrongBook = true;
    ElMessage.success('已加入错题本');
  } catch (error: any) {
    console.error('加入错题本失败:', error);
    const msg = error.response?.data?.message || error.message || '加入错题本失败';
    ElMessage.error(msg);
  } finally {
    question.addingToWrongBook = false;
  }
};

const addAllToWrongBook = async () => {
  const wrongQuestions = questions.value.filter(q => {
    if (q.inWrongBook) return false;
    const earned = q.earnedScore || 0;
    const total = q.score || 0;
    return earned < total;
  });

  if (wrongQuestions.length === 0) {
    ElMessage.info('错题本中已存在该作业中所有错题');
    return;
  }

  batchAdding.value = true;
  try {
    const res = await request.post('/student/error-book/add-batch', {
      questions: wrongQuestions.map(q => ({
        questionId: q.id,
        assignmentId: Number(assignmentId.value),
      })),
    });
    const data = res.data || {};
    const added = data.added ?? wrongQuestions.length;

    wrongQuestions.forEach((q: any) => { q.inWrongBook = true; });
    ElMessage.success('一键添加至错题本成功');
  } catch (error: any) {
    console.error('批量加入错题本失败:', error);
    const msg = error.response?.data?.message || error.message || '批量加入错题本失败';
    ElMessage.error(msg);
  } finally {
    batchAdding.value = false;
  }
};

onMounted(() => {
  getGradingDetail();
});
</script>

<style scoped>
.assignment-detail {
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
}

:deep(.el-card__body) {
  padding: 0 20px 20px;
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-title__left {
  display: flex;
  align-items: center;
  gap: 10px;
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

.back-btn {
  padding: 6px 12px;
  font-size: 13px;
  border-radius: 8px;
}

.back-btn .el-icon {
  margin-right: 4px;
}

.wrong-book-tag {
  display: inline-flex;
  align-items: center;
}

.wrong-book-tag :deep(.el-tag__content) {
  display: inline-flex;
  align-items: center;
}

.wrong-book-tag__icon {
  margin-right: 4px;
  display: inline-flex;
  align-items: center;
}

.assignment-summary {
  display: flex;
  gap: 40px;
  padding: 10px 0;
  flex-wrap: wrap;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.summary-label {
  color: #909399;
}

.summary-value {
  color: #303133;
  font-weight: 500;
}

.summary-value.score strong {
  color: #67c23a;
  font-size: 20px;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  padding: 20px;
  transition: all 0.3s;
}

.question-card.correct {
  border-left: 4px solid #67c23a;
}

.question-card.incorrect {
  border-left: 4px solid #f56c6c;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.question-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-number {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.question-score {
  font-size: 14px;
  color: #f56c6c;
}

.question-result {
  display: flex;
  align-items: center;
  gap: 12px;
}

.earned-score {
  font-size: 14px;
  color: #606266;
}

.earned-score strong {
  color: #67c23a;
  font-size: 18px;
}

.question-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #fafafa 0%, #f5f7fa 100%);
  border-radius: 12px;
}

.answer-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.answer-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 双列布局：学生答案和参考答案并排 */
.answer-row-two-col {
  display: flex;
  gap: 16px;
  width: 100%;
  justify-content: center;
}

.answer-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  max-width: 50%;
}

.answer-col .answer-value {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.answer-label {
  font-size: 14px;
  color: rgb(64, 158, 255);
  font-weight: 500;
  text-align: left;
}

.answer-value {
  font-size: 14px;
  padding: 12px 16px;
  border-radius: 8px;
  background: #f5f7fa;
  text-align: left;
  min-height: 60px;
}

.answer-value.full-score {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67c23a;
}

.answer-value.zero-score {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
  color: #f56c6c;
}

.answer-value.partial-score {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #e6a23c;
}

.answer-value.reference-answer {
  background: linear-gradient(135deg, #fafafa 0%, #f5f7fa 100%);
  color: #303133;
}

.essay-content {
  line-height: 1.8;
  color: #303133;
  text-align: left;
}

.readonly-editor {
  background: transparent;
  border: none;
  border-radius: 0;
  padding: 0;
  min-height: auto;
  flex: 1;
}

.readonly-editor :deep(p) {
  margin: 0 0 12px 0;
}

.readonly-editor :deep(p:last-child) {
  margin-bottom: 0;
}

.readonly-editor :deep(ul),
.readonly-editor :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.readonly-editor :deep(li) {
  margin: 4px 0;
}

.readonly-editor :deep(strong) {
  font-weight: 600;
}

.readonly-editor :deep(em) {
  font-style: italic;
}

.readonly-editor :deep(u) {
  text-decoration: underline;
}

.readonly-editor :deep(s) {
  text-decoration: line-through;
}

.readonly-editor :deep(.ql-katex) {
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  margin: 8px 0;
  overflow-x: auto;
}

.ai-feedback {
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f7ff 100%);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #d9ecff;
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
  color: rgb(64, 158, 255);
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
  display: flex;
  align-items: flex-start;
  gap: 12px;
  justify-content: center;
  text-align: left;
}

.suggestion-content .el-button {
  flex-shrink: 0;
}

.teacher-comment {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #faecd8;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #e6a23c;
  margin-bottom: 12px;
}

.comment-content {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
}

.question-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .assignment-detail {
    padding: 16px;
    margin: 12px;
  }

  .assignment-summary {
    flex-direction: column;
    gap: 12px;
  }

  .question-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
