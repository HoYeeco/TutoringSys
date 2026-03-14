<template>
  <div class="student-report">
    <!-- 报告头部 -->
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>{{ report.assignmentTitle }}</span>
          <el-tag type="info">{{ report.courseName }}</el-tag>
        </div>
      </template>
      <div class="report-header">
        <div class="score-card">
          <div class="score-item">
            <span class="score-label">总分</span>
            <span class="score-value">{{ report.totalScore }}</span>
          </div>
          <div class="score-item">
            <span class="score-label">得分</span>
            <span class="score-value score">{{ report.score }}</span>
          </div>
          <div class="score-item">
            <span class="score-label">正确率</span>
            <span class="score-value">{{ report.accuracy }}%</span>
          </div>
        </div>
        <div class="report-meta">
          <p>批改时间：{{ formatDate(report.gradingTime) }}</p>
          <p>批改方式：{{ report.gradingMethod }}</p>
        </div>
      </div>
    </el-card>

    <!-- 客观题报告区 -->
    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>客观题批改结果</span>
          <el-button size="small" type="primary" @click="addAllToErrorBook">
            一键加入错题本
          </el-button>
        </div>
      </template>
      <div v-for="(question, index) in report.objectiveQuestions" :key="question.id" class="question-item">
        <div class="question-header">
          <span class="question-number">{{ index + 1 }}.</span>
          <span class="question-type">{{ getQuestionTypeText(question.type) }}</span>
          <span class="question-score">({{ question.score }}分)</span>
          <span class="question-status" :class="{ 'correct': question.isCorrect, 'incorrect': !question.isCorrect }">
            {{ question.isCorrect ? '正确' : '错误' }}
          </span>
        </div>
        <div class="question-content">
          {{ question.content }}
        </div>
        <div class="question-answer" v-if="question.type === 'single' || question.type === 'multiple'">
          <div class="answer-item">
            <span class="answer-label">你的答案：</span>
            <span class="answer-content">{{ question.studentAnswer }}</span>
          </div>
          <div class="answer-item">
            <span class="answer-label">标准答案：</span>
            <span class="answer-content correct-answer">{{ question.correctAnswer }}</span>
          </div>
        </div>
        <div class="question-answer" v-else-if="question.type === 'judgment'">
          <div class="answer-item">
            <span class="answer-label">你的答案：</span>
            <span class="answer-content">{{ question.studentAnswer === 'true' ? '正确' : '错误' }}</span>
          </div>
          <div class="answer-item">
            <span class="answer-label">标准答案：</span>
            <span class="answer-content correct-answer">{{ question.correctAnswer === 'true' ? '正确' : '错误' }}</span>
          </div>
        </div>
        <div class="question-analysis" v-if="question.analysis">
          <h4>题目解析</h4>
          <p>{{ question.analysis }}</p>
        </div>
        <div class="question-actions" v-if="!question.isCorrect">
          <el-button size="small" @click="addToErrorBook(question.id)">
            加入错题本
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 主观题报告区 -->
    <el-card shadow="never" class="mt-4" v-if="report.subjectiveQuestions.length > 0">
      <template #header>
        <span>主观题批改结果</span>
      </template>
      <div v-for="(question, index) in report.subjectiveQuestions" :key="question.id" class="question-item">
        <div class="question-header">
          <span class="question-number">{{ index + 1 + report.objectiveQuestions.length }}.</span>
          <span class="question-type">{{ getQuestionTypeText(question.type) }}</span>
          <span class="question-score">({{ question.score }}分)</span>
          <span class="question-status score">
            {{ question.score }}分
          </span>
        </div>
        <div class="question-content">
          {{ question.content }}
        </div>
        <div class="student-answer">
          <h4>你的答案</h4>
          <div class="answer-content rich-text" v-html="question.studentAnswer"></div>
        </div>
        <div class="ai-feedback">
          <h4>AI 批改反馈</h4>
          <div class="feedback-tags" v-if="question.errorPoints.length > 0">
            <span class="tag-label">核心错误点：</span>
            <el-tag v-for="(point, idx) in question.errorPoints" :key="idx" type="danger" size="small" class="error-tag">
              {{ point }}
            </el-tag>
          </div>
          <div class="feedback-suggestions" v-if="question.suggestions.length > 0">
            <span class="suggestion-label">修正建议：</span>
            <ul class="suggestion-list">
              <li v-for="(suggestion, idx) in question.suggestions" :key="idx">
                {{ suggestion }}
                <el-button size="small" type="text" @click="copySuggestion(suggestion)">
                  复制
                </el-button>
              </li>
            </ul>
          </div>
        </div>
        <div class="teacher-comments" v-if="question.teacherComments">
          <h4>教师评语</h4>
          <p>{{ question.teacherComments }}</p>
        </div>
      </div>
    </el-card>

    <!-- 操作按钮 -->
    <div class="report-actions">
      <el-button @click="router.push('/student/assignments')">
        返回作业列表
      </el-button>
      <el-button type="primary" @click="router.push('/student/assignments/history')">
        查看历史记录
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const route = useRoute();
const router = useRouter();
const reportId = computed(() => route.params.id as string);

const report = ref({
  assignmentTitle: '第一章 计算机基础作业',
  courseName: '计算机导论',
  totalScore: 100,
  score: 85,
  accuracy: 85,
  gradingTime: '2026-03-10 15:30:00',
  gradingMethod: '客观题智能批改，主观题大模型批改',
  objectiveQuestions: [
    {
      id: 1,
      type: 'single',
      content: '计算机的基本组成部分不包括以下哪项？',
      score: 10,
      isCorrect: true,
      studentAnswer: 'D',
      correctAnswer: 'D',
      analysis: '计算机的基本组成部分包括CPU、内存和硬盘，打印机属于外部设备。'
    },
    {
      id: 2,
      type: 'multiple',
      content: '以下哪些是计算机的输入设备？',
      score: 15,
      isCorrect: false,
      studentAnswer: 'A,B,D',
      correctAnswer: 'A,B',
      analysis: '键盘和鼠标是输入设备，显示器和打印机是输出设备。'
    },
    {
      id: 3,
      type: 'judgment',
      content: '计算机病毒是一种程序。',
      score: 5,
      isCorrect: true,
      studentAnswer: 'true',
      correctAnswer: 'true',
      analysis: '计算机病毒是一种能够自我复制的程序代码。'
    }
  ],
  subjectiveQuestions: [
    {
      id: 4,
      type: 'essay',
      content: '请简述计算机的发展历程。',
      score: 55,
      studentAnswer: '<p>计算机的发展历程可以分为四个阶段：</p><p>1. 电子管计算机时代</p><p>2. 晶体管计算机时代</p><p>3. 集成电路计算机时代</p><p>4. 大规模集成电路计算机时代</p>',
      errorPoints: ['内容过于简略', '缺少具体时间节点'],
      suggestions: ['补充每个阶段的具体时间范围', '增加每个阶段的代表机型', '描述每个阶段的技术特点'],
      teacherComments: '回答基本正确，但内容不够详细，需要补充更多具体信息。'
    }
  ]
});

const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '简答题'
  };
  return typeMap[type] || type;
};

const getReportDetail = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get(`/student/reports/${reportId.value}`);
    // return response.data;
    
    // 模拟数据
    return {
      assignmentTitle: '第一章 计算机基础作业',
      courseName: '计算机导论',
      totalScore: 100,
      score: 85,
      accuracy: 85,
      gradingTime: '2026-03-10 15:30:00',
      gradingMethod: '客观题智能批改，主观题大模型批改',
      objectiveQuestions: [
        {
          id: 1,
          type: 'single',
          content: '计算机的基本组成部分不包括以下哪项？',
          score: 10,
          isCorrect: true,
          studentAnswer: 'D',
          correctAnswer: 'D',
          analysis: '计算机的基本组成部分包括CPU、内存和硬盘，打印机属于外部设备。'
        },
        {
          id: 2,
          type: 'multiple',
          content: '以下哪些是计算机的输入设备？',
          score: 15,
          isCorrect: false,
          studentAnswer: 'A,B,D',
          correctAnswer: 'A,B',
          analysis: '键盘和鼠标是输入设备，显示器和打印机是输出设备。'
        },
        {
          id: 3,
          type: 'judgment',
          content: '计算机病毒是一种程序。',
          score: 5,
          isCorrect: true,
          studentAnswer: 'true',
          correctAnswer: 'true',
          analysis: '计算机病毒是一种能够自我复制的程序代码。'
        }
      ],
      subjectiveQuestions: [
        {
          id: 4,
          type: 'essay',
          content: '请简述计算机的发展历程。',
          score: 55,
          studentAnswer: '<p>计算机的发展历程可以分为四个阶段：</p><p>1. 电子管计算机时代</p><p>2. 晶体管计算机时代</p><p>3. 集成电路计算机时代</p><p>4. 大规模集成电路计算机时代</p>',
          errorPoints: ['内容过于简略', '缺少具体时间节点'],
          suggestions: ['补充每个阶段的具体时间范围', '增加每个阶段的代表机型', '描述每个阶段的技术特点'],
          teacherComments: '回答基本正确，但内容不够详细，需要补充更多具体信息。'
        }
      ]
    };
  } catch (error) {
    ElMessage.error('获取批改报告失败');
    return null;
  }
};

const addToErrorBook = async (questionId: number) => {
  try {
    // 实际项目中调用接口
    // const response = await request.post(`/student/error-book/add`, { questionId });
    // return response.data;
    
    // 模拟成功
    ElMessage.success('已加入错题本');
    return { success: true };
  } catch (error) {
    ElMessage.error('加入错题本失败');
    throw error;
  }
};

const addAllToErrorBook = async () => {
  try {
    // 收集所有错题
    const wrongQuestions = report.value.objectiveQuestions.filter(q => !q.isCorrect);
    
    if (wrongQuestions.length === 0) {
      ElMessage.info('没有错题需要加入错题本');
      return;
    }
    
    // 实际项目中调用接口
    // const response = await request.post(`/student/error-book/add-batch`, { questionIds: wrongQuestions.map(q => q.id) });
    // return response.data;
    
    // 模拟成功
    ElMessage.success(`已成功加入 ${wrongQuestions.length} 道错题到错题本`);
    return { success: true };
  } catch (error) {
    ElMessage.error('批量加入错题本失败');
    throw error;
  }
};

const copySuggestion = (suggestion: string) => {
  navigator.clipboard.writeText(suggestion).then(() => {
    ElMessage.success('建议已复制到剪贴板');
  });
};

const { execute: fetchReportDetail } = useRequest(getReportDetail);

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchReportDetail().then((data: any) => {
    if (data) {
      report.value = data;
    }
  });
});
</script>

<style scoped>
.student-report {
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

.report-header {
  margin-top: 16px;
}

.score-card {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.score-item {
  text-align: center;
}

.score-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.score-value {
  display: block;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.score-value.score {
  color: #409eff;
  font-size: 32px;
}

.report-meta {
  color: #606266;
  font-size: 14px;
}

.question-item {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.question-number {
  font-weight: 500;
  margin-right: 8px;
}

.question-type {
  background-color: #ecf5ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 8px;
}

.question-score {
  color: #f56c6c;
  font-size: 12px;
  margin-right: 8px;
}

.question-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.question-status.correct {
  background-color: #f0f9eb;
  color: #67c23a;
}

.question-status.incorrect {
  background-color: #fef0f0;
  color: #f56c6c;
}

.question-content {
  margin-bottom: 16px;
  line-height: 1.5;
}

.question-answer {
  margin-left: 24px;
  margin-bottom: 16px;
}

.answer-item {
  margin-bottom: 8px;
}

.answer-label {
  font-weight: 500;
  margin-right: 8px;
}

.answer-content {
  color: #606266;
}

.answer-content.correct-answer {
  color: #67c23a;
  font-weight: 500;
}

.question-analysis {
  margin-left: 24px;
  margin-bottom: 16px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.question-analysis h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
}

.question-actions {
  margin-left: 24px;
}

.student-answer {
  margin-left: 24px;
  margin-bottom: 16px;
}

.student-answer h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
}

.answer-content.rich-text {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.ai-feedback {
  margin-left: 24px;
  margin-bottom: 16px;
}

.ai-feedback h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
}

.feedback-tags {
  margin-bottom: 12px;
}

.tag-label {
  font-weight: 500;
  margin-right: 8px;
}

.error-tag {
  margin-right: 8px;
}

.feedback-suggestions {
  margin-top: 12px;
}

.suggestion-label {
  font-weight: 500;
  display: block;
  margin-bottom: 8px;
}

.suggestion-list {
  margin: 0;
  padding-left: 20px;
}

.suggestion-list li {
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.teacher-comments {
  margin-left: 24px;
  padding: 12px;
  background-color: #f0f9eb;
  border-radius: 4px;
}

.teacher-comments h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
}

.report-actions {
  margin-top: 32px;
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>