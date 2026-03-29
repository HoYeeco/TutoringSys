<template>
  <div class="grading-detail">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button text @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <span class="title">{{ report.assignmentTitle }}</span>
          </div>
          <div class="header-right">
            <el-tag
              :type="getScoreTagType(report.studentScore, report.totalScore)"
              effect="dark"
              size="large"
            >
              {{ report.studentScore }}/{{ report.totalScore }}分
            </el-tag>
          </div>
        </div>
      </template>
      <div class="report-summary">
        <div class="summary-item">
          <span class="label">课程：</span>
          <span class="value">{{ report.courseName }}</span>
        </div>
        <div class="summary-item">
          <span class="label">授课老师：</span>
          <span class="value">{{ report.teacherName }}</span>
        </div>
        <div class="summary-item">
          <span class="label">正确率：</span>
          <span class="value">{{ report.accuracy }}%</span>
        </div>
        <div class="summary-item">
          <span class="label">批改时间：</span>
          <span class="value">{{ formatDate(report.gradingTime) }}</span>
        </div>
        <div class="summary-item">
          <span class="label">批改方式：</span>
          <el-tag
            :type="report.gradingType === 'AI' ? 'primary' : 'success'"
            size="small"
          >
            {{ report.gradingType === 'AI' ? '智能批改' : '人工复核' }}
          </el-tag>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4" v-if="objectiveQuestions.length > 0">
      <template #header>
        <div class="section-header">
          <span class="section-title">客观题批改详情</span>
          <el-button
            type="primary"
            size="small"
            @click="addAllToErrorBook('objective')"
          >
            <el-icon><Plus /></el-icon>
            一键加入错题本
          </el-button>
        </div>
      </template>

      <div class="question-list">
        <div
          v-for="(question, index) in objectiveQuestions"
          :key="question.questionId"
          class="question-card"
          :class="{ correct: question.isCorrect, wrong: !question.isCorrect }"
        >
          <div class="question-header">
            <div class="question-number">
              <span class="number">第 {{ index + 1 }} 题</span>
              <el-tag :type="getQuestionTypeTag(question.type)" size="small">
                {{ getQuestionTypeLabel(question.type) }}
              </el-tag>
            </div>
            <div class="question-score">
              <span
                :class="question.isCorrect ? 'score-correct' : 'score-wrong'"
              >
                {{ question.score }}分
              </span>
            </div>
          </div>

          <div class="question-content">
            <div class="content-label">题干：</div>
            <div class="content-text" v-html="question.content"></div>
          </div>

          <div class="question-options" v-if="question.options">
            <div class="options-label">选项：</div>
            <div class="options-list">
              <div
                v-for="(option, optKey) in parseOptions(question.options)"
                :key="optKey"
                class="option-item"
                :class="{
                  selected: isOptionSelected(question.studentAnswer, optKey),
                  correct: isCorrectOption(question.correctAnswer, optKey),
                  wrong:
                    isOptionSelected(question.studentAnswer, optKey) &&
                    !isCorrectOption(question.correctAnswer, optKey),
                }"
              >
                <span class="option-key">{{ optKey }}.</span>
                <span class="option-text">{{ option }}</span>
              </div>
            </div>
          </div>

          <div class="answer-section">
            <div class="answer-row">
              <span class="answer-label">学生答案：</span>
              <span
                class="answer-value"
                :class="question.isCorrect ? 'correct' : 'wrong'"
              >
                {{ formatAnswer(question.studentAnswer, question.type) }}
              </span>
              <el-icon v-if="question.isCorrect" class="correct-icon"
                ><CircleCheck
              /></el-icon>
              <el-icon v-else class="wrong-icon"><CircleClose /></el-icon>
            </div>
            <div class="answer-row" v-if="!question.isCorrect">
              <span class="answer-label">标准答案：</span>
              <span class="answer-value correct">{{
                formatAnswer(question.correctAnswer, question.type)
              }}</span>
            </div>
          </div>

          <div class="analysis-section" v-if="question.analysis">
            <div class="analysis-label">题目解析：</div>
            <div class="analysis-content">{{ question.analysis }}</div>
          </div>

          <div class="question-footer" v-if="!question.isCorrect">
            <el-button
              type="warning"
              size="small"
              @click="addToErrorBook(question)"
            >
              <el-icon><Plus /></el-icon>
              加入错题本
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4" v-if="subjectiveQuestions.length > 0">
      <template #header>
        <div class="section-header">
          <span class="section-title">主观题批改详情</span>
        </div>
      </template>

      <div class="question-list">
        <div
          v-for="(question, index) in subjectiveQuestions"
          :key="question.questionId"
          class="question-card subjective"
        >
          <div class="question-header">
            <div class="question-number">
              <span class="number">第 {{ index + 1 }} 题</span>
              <el-tag type="info" size="small">简答题</el-tag>
            </div>
            <div class="question-score">
              <span class="score-value"
                >{{ question.score }}/{{ question.maxScore }}分</span
              >
            </div>
          </div>

          <div class="question-content">
            <div class="content-label">题干：</div>
            <div class="content-text" v-html="question.content"></div>
          </div>

          <div class="student-answer-section">
            <div class="section-label">学生答案：</div>
            <div class="answer-content" v-html="question.studentAnswer"></div>
          </div>

          <div class="ai-feedback-section" v-if="question.aiFeedback">
            <div class="section-label">AI 结构化反馈：</div>

            <div
              class="feedback-tags"
              v-if="question.errorTags && question.errorTags.length > 0"
            >
              <span class="tags-label">核心错误点：</span>
              <el-tag
                v-for="tag in question.errorTags"
                :key="tag"
                type="danger"
                effect="plain"
                class="error-tag"
              >
                {{ tag }}
              </el-tag>
            </div>

            <div
              class="suggestions"
              v-if="question.suggestions && question.suggestions.length > 0"
            >
              <div class="suggestions-label">修正建议：</div>
              <ul class="suggestions-list">
                <li
                  v-for="(suggestion, idx) in question.suggestions"
                  :key="idx"
                  class="suggestion-item"
                >
                  <span class="suggestion-text">{{ suggestion }}</span>
                  <el-button
                    text
                    type="primary"
                    size="small"
                    @click="copySuggestion(suggestion)"
                  >
                    <el-icon><CopyDocument /></el-icon>
                  </el-button>
                </li>
              </ul>
            </div>
          </div>

          <div class="score-section">
            <div class="score-row">
              <span class="score-label">最终得分：</span>
              <span class="final-score">{{ question.score }}</span>
              <span class="max-score">/ {{ question.maxScore }}分</span>
            </div>
            <div class="grading-info">
              <el-tag type="primary" size="small">AI 初评</el-tag>
              <el-icon><Right /></el-icon>
              <el-tag type="success" size="small">教师复核</el-tag>
            </div>
          </div>

          <div class="teacher-comment-section" v-if="question.teacherComment">
            <div class="section-label">教师评语：</div>
            <div class="comment-content">{{ question.teacherComment }}</div>
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
  ArrowLeft,
  Plus,
  CircleCheck,
  CircleClose,
  CopyDocument,
  Right,
} from '@element-plus/icons-vue';
import request from '@/utils/request';

const route = useRoute();
const router = useRouter();
const submissionId = computed(() => route.params.id as string);

const report = ref<any>({
  assignmentTitle: '',
  courseName: '',
  teacherName: '',
  studentScore: 0,
  totalScore: 100,
  accuracy: 0,
  gradingTime: '',
  gradingType: 'AI',
  questions: [],
});

const objectiveQuestions = computed(() => {
  return report.value.questions.filter((q: any) =>
    ['single', 'multiple', 'judgment'].includes(q.type),
  );
});

const subjectiveQuestions = computed(() => {
  return report.value.questions.filter((q: any) => q.type === 'essay');
});

const getReportDetail = async () => {
  try {
    const response = await request.get(
      `/student/grading/${submissionId.value}`,
    );
    const data = response.data || {};
    report.value = {
      assignmentTitle: data.assignmentTitle || '作业批改详情',
      courseName: data.courseName || '',
      teacherName: data.teacherName || '',
      studentScore: data.studentScore || 0,
      totalScore: data.totalScore || 100,
      accuracy: data.accuracy || 0,
      gradingTime: data.gradingTime || '',
      gradingType: data.gradingType || 'AI',
      questions: data.questions || [],
    };
  } catch (error) {
    console.error('获取批改详情失败:', error);
    ElMessage.error('获取批改详情失败');
  }
};

const getScoreTagType = (score: number, total: number) => {
  const rate = score / total;
  if (rate >= 0.9) return 'success';
  if (rate >= 0.8) return 'primary';
  if (rate >= 0.6) return 'warning';
  return 'danger';
};

const getQuestionTypeTag = (type: string) => {
  const map: Record<string, string> = {
    single: 'primary',
    multiple: 'success',
    judgment: 'warning',
  };
  return map[type] || 'info';
};

const getQuestionTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
  };
  return map[type] || '未知';
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

const isOptionSelected = (studentAnswer: any, optKey: string) => {
  if (Array.isArray(studentAnswer)) {
    return studentAnswer.includes(optKey);
  }
  return studentAnswer === optKey;
};

const isCorrectOption = (correctAnswer: any, optKey: string) => {
  if (Array.isArray(correctAnswer)) {
    return correctAnswer.includes(optKey);
  }
  return correctAnswer === optKey;
};

const formatAnswer = (answer: any, type: string) => {
  if (type === 'judgment') {
    return answer === 'true' || answer === true ? '正确' : '错误';
  }
  if (Array.isArray(answer)) {
    return answer.join('、');
  }
  return answer || '-';
};

const formatDate = (date: string) => {
  if (!date) return '-';
  const d = new Date(date);
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`;
};

const addToErrorBook = async (question: any) => {
  try {
    await request.post('/student/error-book/add', {
      questionId: question.questionId,
    });
    ElMessage.success('已加入错题本');
  } catch (error) {
    console.error('加入错题本失败:', error);
    ElMessage.error('加入错题本失败');
  }
};

const addAllToErrorBook = async (type: string) => {
  const questions =
    type === 'objective' ? objectiveQuestions.value : subjectiveQuestions.value;
  const wrongQuestions = questions.filter((q: any) => !q.isCorrect);

  if (wrongQuestions.length === 0) {
    ElMessage.info('没有错题需要加入');
    return;
  }

  try {
    await request.post('/student/error-book/batch-add', {
      questionIds: wrongQuestions.map((q: any) => q.questionId),
    });
    ElMessage.success(`已将 ${wrongQuestions.length} 道错题加入错题本`);
  } catch (error) {
    console.error('批量加入错题本失败:', error);
    ElMessage.error('批量加入错题本失败');
  }
};

const copySuggestion = (text: string) => {
  navigator.clipboard
    .writeText(text)
    .then(() => {
      ElMessage.success('已复制到剪贴板');
    })
    .catch(() => {
      ElMessage.error('复制失败');
    });
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  getReportDetail();
});
</script>

<style scoped>
.grading-detail {
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

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left .title {
  font-size: 18px;
  font-weight: 600;
}

.report-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-top: 16px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-item .label {
  color: #909399;
  font-size: 14px;
}

.summary-item .value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-card {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fff;
}

.question-card.correct {
  border-left: 4px solid #67c23a;
}

.question-card.wrong {
  border-left: 4px solid #f56c6c;
}

.question-card.subjective {
  border-left: 4px solid #409eff;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.question-number {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-number .number {
  font-weight: 600;
  color: #303133;
}

.question-score {
  font-size: 16px;
  font-weight: 600;
}

.score-correct {
  color: #67c23a;
}

.score-wrong {
  color: #f56c6c;
}

.score-value {
  color: #409eff;
}

.question-content {
  margin-bottom: 16px;
}

.content-label,
.options-label,
.answer-label,
.analysis-label,
.section-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.content-text {
  font-size: 15px;
  line-height: 1.6;
  color: #303133;
}

.question-options {
  margin-bottom: 16px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  padding: 10px 12px;
  border-radius: 6px;
  background-color: #f5f7fa;
  border: 1px solid transparent;
}

.option-item.selected {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.option-item.correct {
  background-color: #f0f9eb;
  border-color: #67c23a;
}

.option-item.wrong {
  background-color: #fef0f0;
  border-color: #f56c6c;
}

.option-key {
  font-weight: 600;
  margin-right: 8px;
}

.answer-section {
  padding: 12px;
  background-color: #fafafa;
  border-radius: 6px;
  margin-bottom: 16px;
}

.answer-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.answer-row:last-child {
  margin-bottom: 0;
}

.answer-value {
  font-weight: 500;
}

.answer-value.correct {
  color: #67c23a;
}

.answer-value.wrong {
  color: #f56c6c;
}

.correct-icon {
  color: #67c23a;
  font-size: 18px;
}

.wrong-icon {
  color: #f56c6c;
  font-size: 18px;
}

.analysis-section {
  padding: 12px;
  background-color: #fdf6ec;
  border-radius: 6px;
  margin-bottom: 16px;
}

.analysis-content {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.question-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.student-answer-section {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.answer-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
}

.ai-feedback-section {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #ecf5ff;
  border-radius: 6px;
}

.feedback-tags {
  margin-bottom: 16px;
}

.tags-label {
  font-size: 14px;
  color: #909399;
  margin-right: 12px;
}

.error-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.suggestions-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.suggestions-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 12px;
  background-color: #fff;
  border-radius: 6px;
  margin-bottom: 8px;
}

.suggestion-text {
  flex: 1;
  font-size: 14px;
  line-height: 1.5;
  color: #606266;
}

.score-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f0f9eb;
  border-radius: 6px;
  margin-bottom: 16px;
}

.score-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-label {
  font-size: 14px;
  color: #909399;
}

.final-score {
  font-size: 24px;
  font-weight: bold;
  color: #67c23a;
}

.max-score {
  font-size: 16px;
  color: #909399;
}

.grading-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.teacher-comment-section {
  padding: 16px;
  background-color: #fef0f0;
  border-radius: 6px;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

@media (max-width: 768px) {
  .report-summary {
    flex-direction: column;
    gap: 12px;
  }

  .question-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .score-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
