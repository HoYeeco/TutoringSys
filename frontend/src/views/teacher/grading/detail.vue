<template>
  <div class="teacher-grading-detail">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>复核详情</span>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>
    </el-card>

    <el-card shadow="never" class="info-card">
      <div class="info-row">
        <div class="info-item">
          <span class="info-label">作业名称：</span>
          <span class="info-value">{{ submissionInfo.assignmentName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">所属课程：</span>
          <span class="info-value">{{ submissionInfo.courseName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">学生姓名：</span>
          <span class="info-value">{{ submissionInfo.studentName }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">提交时间：</span>
          <span class="info-value">{{ submissionInfo.submitTime }}</span>
        </div>
      </div>
    </el-card>

    <div class="review-container">
      <div class="questions-list">
        <el-card
          v-for="(question, index) in questions"
          :key="question.questionId"
          shadow="never"
          class="question-card"
        >
          <template #header>
            <div class="question-header">
              <span class="question-title">
                {{ index + 1 }}. {{ question.questionType === 'single' ? '单选题' : question.questionType === 'multiple' ? '多选题' : question.questionType === 'truefalse' ? '判断题' : '主观题' }}
              </span>
              <span class="question-score">{{ question.score }}分</span>
            </div>
          </template>
          <div class="question-content" v-html="question.content"></div>
          
          <!-- 客观题选项 -->
          <div v-if="['single', 'multiple', 'truefalse'].includes(question.questionType)" class="options-list">
            <div
              v-for="(option, optionIndex) in question.options"
              :key="optionIndex"
              class="option-item"
              :class="{
                'selected': studentAnswer.selectedOptions.includes(String.fromCharCode(65 + optionIndex)),
                'correct': question.correctAnswer?.includes(String.fromCharCode(65 + optionIndex)),
                'incorrect': studentAnswer.selectedOptions.includes(String.fromCharCode(65 + optionIndex)) && !question.correctAnswer?.includes(String.fromCharCode(65 + optionIndex))
              }"
            >
              <span class="option-label">{{ String.fromCharCode(65 + optionIndex) }}.</span>
              <span class="option-content">{{ option }}</span>
            </div>
          </div>
          
          <!-- 主观题答案 -->
          <div v-else class="subjective-answer">
            <div class="answer-label">学生答案：</div>
            <div class="answer-content" v-html="studentAnswer.subjectiveAnswer"></div>
          </div>
        </el-card>
      </div>

      <div class="ai-feedback">
        <el-card shadow="never" class="feedback-card">
          <template #header>
            <span>智能批改结果</span>
          </template>
          
          <!-- 客观题批改结果 -->
          <div v-for="(question, index) in questions" :key="question.questionId" class="feedback-item">
            <div class="feedback-header">
              <span class="feedback-title">{{ index + 1 }}. {{ question.questionType === 'single' ? '单选题' : question.questionType === 'multiple' ? '多选题' : question.questionType === 'truefalse' ? '判断题' : '主观题' }}</span>
            </div>
            
            <!-- 客观题反馈 -->
            <div v-if="['single', 'multiple', 'truefalse'].includes(question.questionType)" class="objective-feedback">
              <div class="feedback-info">
                <span class="feedback-label">正确答案：</span>
                <span class="feedback-value">{{ question.correctAnswer?.join(', ') }}</span>
              </div>
              <div class="feedback-info">
                <span class="feedback-label">学生答案：</span>
                <span class="feedback-value">{{ studentAnswer.selectedOptions.join(', ') }}</span>
              </div>
              <div class="feedback-info">
                <span class="feedback-label">得分：</span>
                <span class="feedback-value" :class="{ 'correct': aiResult.questionScores[index] === question.score, 'incorrect': aiResult.questionScores[index] < question.score }">
                  {{ aiResult.questionScores[index] || 0 }}/{{ question.score }}
                </span>
              </div>
            </div>
            
            <!-- 主观题反馈 -->
            <div v-else class="subjective-feedback">
              <div class="feedback-info">
                <span class="feedback-label">AI评分：</span>
                <span class="feedback-value">{{ aiResult.questionScores[index] }}/{{ question.score }}</span>
              </div>
              <div class="feedback-info">
                <span class="feedback-label">错误点标注：</span>
              </div>
              <div class="feedback-content" v-html="aiResult.feedback[index]?.errorPoints"></div>
              <div class="feedback-info">
                <span class="feedback-label">修正建议：</span>
              </div>
              <div class="feedback-content" v-html="aiResult.feedback[index]?.suggestions"></div>
            </div>
          </div>
        </el-card>

        <!-- 教师操作区 -->
        <el-card shadow="never" class="action-card">
          <template #header>
            <span>教师操作</span>
          </template>
          
          <div class="action-section">
            <div class="action-item">
              <span class="action-label">AI反馈：</span>
              <div class="action-buttons">
                <el-button
                  type="primary"
                  :class="{ 'active': feedbackAction === 'adopt' }"
                  @click="feedbackAction = 'adopt'"
                >
                  采用
                </el-button>
                <el-button
                  :class="{ 'active': feedbackAction === 'modify' }"
                  @click="feedbackAction = 'modify'"
                >
                  修改
                </el-button>
              </div>
            </div>
            
            <!-- 人工评分 -->
            <div v-if="feedbackAction === 'modify'" class="action-item">
              <span class="action-label">人工评分：</span>
              <el-input-number
                v-model="manualScore"
                :min="0"
                :max="totalScore"
                step="1"
                class="score-input"
              />
            </div>
            
            <!-- 教师评语 -->
            <div class="action-item">
              <span class="action-label">教师评语：</span>
              <div class="editor-container">
                <div ref="editorRef" class="editor"></div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <div class="bottom-actions">
      <el-button @click="goBack">返回列表</el-button>
      <el-button @click="saveDraft">保存草稿</el-button>
      <el-button type="primary" @click="completeReview">复核完成</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useRequest } from '@/composables/useRequest';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';

interface SubmissionInfo {
  assignmentName: string;
  courseName: string;
  studentName: string;
  submitTime: string;
}

interface Question {
  questionId: number;
  questionType: string;
  content: string;
  options?: string[];
  correctAnswer?: string[];
  score: number;
}

interface StudentAnswer {
  selectedOptions: string[];
  subjectiveAnswer: string;
}

interface Feedback {
  errorPoints?: string;
  suggestions?: string;
}

interface AIResult {
  questionScores: number[];
  feedback: Feedback[];
}

const router = useRouter();
const route = useRoute();
const editorRef = ref<HTMLElement | null>(null);

const submissionId = computed(() => route.params.id as string);

const submissionInfo = ref<SubmissionInfo>({
  assignmentName: '数据结构作业1',
  courseName: '数据结构',
  studentName: '张三',
  submitTime: '2024-01-15 14:30:00'
});

const questions = ref<Question[]>([
  {
    questionId: 1,
    questionType: 'single',
    content: '数据结构中，栈的特点是？',
    options: ['先进先出', '后进先出', '随机访问', '顺序访问'],
    correctAnswer: ['B'],
    score: 5
  },
  {
    questionId: 2,
    questionType: 'multiple',
    content: '以下哪些是线性数据结构？',
    options: ['数组', '链表', '树', '图'],
    correctAnswer: ['A', 'B'],
    score: 8
  },
  {
    questionId: 3,
    questionType: 'subjective',
    content: '请简述冒泡排序的基本原理。',
    score: 12
  }
]);

const studentAnswer = ref<StudentAnswer>({
  selectedOptions: ['B', 'A'],
  subjectiveAnswer: '<p>冒泡排序是一种简单的排序算法，它重复地走访过要排序的数列，一次比较两个元素，如果它们的顺序错误就把它们交换过来。</p><p>走访数列的工作是重复地进行直到没有再需要交换的元素，也就是说该数列已经排序完成。</p>'
});

const aiResult = ref<AIResult>({
  questionScores: [5, 4, 10],
  feedback: [
    {},
    {},
    {
      errorPoints: '<p>· 缺少对冒泡排序时间复杂度的分析</p><p>· 未提及冒泡排序的优化方法</p>',
      suggestions: '<p>1. 可以添加时间复杂度分析：最好情况O(n)，最坏情况O(n²)</p><p>2. 可以提到优化方法，如设置标志位判断是否已经有序</p>'
    }
  ]
});

const totalScore = computed(() => {
  return questions.value.reduce((sum, q) => sum + q.score, 0);
});

const feedbackAction = ref('adopt');
const manualScore = ref(0);

const getSubmissionDetail = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get(`/teacher/grading/submissions/${submissionId.value}`);
    // return response.data;
    
    // 模拟数据
    return {
      submissionInfo: submissionInfo.value,
      questions: questions.value,
      studentAnswer: studentAnswer.value,
      aiResult: aiResult.value
    };
  } catch (error) {
    ElMessage.error('获取提交详情失败');
    return null;
  }
};

const { execute: fetchSubmissionDetail } = useRequest(getSubmissionDetail);

const goBack = () => {
  router.push('/teacher/grading');
};

const saveDraft = () => {
  ElMessage.success('草稿保存成功');
};

const completeReview = () => {
  ElMessage.success('复核完成');
  router.push('/teacher/grading');
};

onMounted(() => {
  fetchSubmissionDetail();
  
  // 初始化Quill编辑器
  if (editorRef.value) {
    new Quill(editorRef.value, {
      theme: 'snow',
      modules: {
        toolbar: [
          ['bold', 'italic', 'underline'],
          ['list', 'bullet']
        ]
      }
    });
  }
});
</script>

<style scoped>
.teacher-grading-detail {
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

.info-card {
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: bold;
  margin-right: 8px;
  color: #606266;
}

.review-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-title {
  font-weight: bold;
  color: #303133;
}

.question-score {
  color: #409eff;
  font-weight: bold;
}

.question-content {
  margin: 15px 0;
  line-height: 1.6;
}

.options-list {
  margin-top: 15px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  margin-bottom: 8px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.option-item.selected {
  background-color: #ecf5ff;
  border-color: #c6e2ff;
}

.option-item.correct {
  background-color: #f0f9eb;
  border-color: #b7eb8f;
}

.option-item.incorrect {
  background-color: #fef0f0;
  border-color: #ffccc7;
}

.option-label {
  font-weight: bold;
  margin-right: 10px;
  width: 20px;
}

.subjective-answer {
  margin-top: 15px;
}

.answer-label {
  font-weight: bold;
  margin-bottom: 8px;
  color: #303133;
}

.answer-content {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #f9f9f9;
  line-height: 1.6;
}

.ai-feedback {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feedback-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.feedback-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.feedback-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.feedback-header {
  margin-bottom: 10px;
}

.feedback-title {
  font-weight: bold;
  color: #303133;
}

.feedback-info {
  margin-bottom: 8px;
  display: flex;
  align-items: flex-start;
}

.feedback-label {
  font-weight: bold;
  margin-right: 8px;
  color: #606266;
  min-width: 80px;
}

.feedback-value.correct {
  color: #67c23a;
  font-weight: bold;
}

.feedback-value.incorrect {
  color: #f56c6c;
  font-weight: bold;
}

.feedback-content {
  margin-left: 88px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #f9f9f9;
  line-height: 1.6;
}

.action-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.action-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.action-label {
  font-weight: bold;
  color: #303133;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.action-buttons .active {
  border-color: #409eff;
  background-color: #ecf5ff;
  color: #409eff;
}

.score-input {
  width: 120px;
}

.editor-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.editor {
  min-height: 100px;
}

.bottom-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .review-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .info-row {
    flex-direction: column;
    gap: 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .bottom-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>