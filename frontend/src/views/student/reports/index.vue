<template>
  <div class="student-report">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>批改报告</span>
          <el-tag type="success">{{ report.assignmentTitle }}</el-tag>
        </div>
      </template>
      <div class="report-info">
        <p>总分：{{ report.totalScore }}</p>
        <p>得分：{{ report.studentScore }}</p>
        <p>正确率：{{ (report.studentScore / report.totalScore * 100).toFixed(1) }}%</p>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <el-collapse v-model="activeNames">
        <el-collapse-item v-for="type in questionTypes" :key="type.value" :title="type.label" :name="type.value">
          <div v-for="question in getQuestionsByType(type.value)" :key="question.questionId" class="question-item">
            <div class="question-header">
              <span class="question-score">得分：{{ question.score }}分</span>
              <el-tag
                v-if="question.errorType"
                :type="getErrorTypeColor(question.errorType)"
                size="small"
              >
                {{ question.errorType }}
              </el-tag>
            </div>
            <div class="question-content">
              <h4>题目：</h4>
              <p>{{ question.questionContent }}</p>
            </div>
            <div class="question-answer">
              <h4>你的答案：</h4>
              <p>{{ question.studentAnswer }}</p>
            </div>
            <div class="question-correct-answer">
              <h4>标准答案：</h4>
              <p>{{ question.correctAnswer }}</p>
            </div>
            <div class="question-feedback">
              <h4>反馈：</h4>
              <p>{{ question.feedback }}</p>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const route = useRoute();
const submissionId = computed(() => route.params.id as string);

const report = ref({
  assignmentTitle: '第一章 计算机基础作业',
  totalScore: 100,
  studentScore: 85,
  questions: [
    {
      questionId: 1,
      questionContent: '计算机的基本组成部分不包括以下哪项？',
      studentAnswer: 'D',
      correctAnswer: 'D',
      score: 10,
      errorType: '',
      feedback: '回答正确！',
      type: 'single'
    },
    {
      questionId: 2,
      questionContent: '以下哪些是计算机的输入设备？',
      studentAnswer: ['A', 'B', 'C'],
      correctAnswer: ['A', 'B'],
      score: 7,
      errorType: '概念错误',
      feedback: '显示器是输出设备，不是输入设备。',
      type: 'multiple'
    },
    {
      questionId: 3,
      questionContent: '计算机病毒是一种程序。',
      studentAnswer: 'true',
      correctAnswer: 'true',
      score: 5,
      errorType: '',
      feedback: '回答正确！',
      type: 'judgment'
    },
    {
      questionId: 4,
      questionContent: '请简述计算机的发展历程。',
      studentAnswer: '计算机的发展经历了四个阶段：电子管计算机、晶体管计算机、集成电路计算机和大规模集成电路计算机。',
      correctAnswer: '计算机的发展经历了四个阶段：1. 电子管计算机（1946-1958）；2. 晶体管计算机（1958-1964）；3. 集成电路计算机（1964-1971）；4. 大规模集成电路计算机（1971至今）。',
      score: 20,
      errorType: '逻辑错误',
      feedback: '回答基本正确，但缺少具体的时间范围。',
      type: 'essay'
    }
  ]
});

const activeNames = ref(['single', 'multiple', 'judgment', 'essay']);

const questionTypes = [
  { label: '单选题', value: 'single' },
  { label: '多选题', value: 'multiple' },
  { label: '判断题', value: 'judgment' },
  { label: '简答题', value: 'essay' }
];

const getQuestionsByType = (type: string) => {
  return report.value.questions.filter(q => q.type === type);
};

const getErrorTypeColor = (errorType: string) => {
  const colorMap: Record<string, string> = {
    '概念错误': 'danger',
    '逻辑错误': 'warning',
    '格式错误': 'primary'
  };
  return colorMap[errorType] || 'info';
};

const getReport = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get(`/student/assignments/report/${submissionId.value}`);
    // return response.data;
    
    // 模拟数据
    return {
      assignmentTitle: '第一章 计算机基础作业',
      totalScore: 100,
      studentScore: 85,
      questions: [
        {
          questionId: 1,
          questionContent: '计算机的基本组成部分不包括以下哪项？',
          studentAnswer: 'D',
          correctAnswer: 'D',
          score: 10,
          errorType: '',
          feedback: '回答正确！',
          type: 'single'
        },
        {
          questionId: 2,
          questionContent: '以下哪些是计算机的输入设备？',
          studentAnswer: ['A', 'B', 'C'],
          correctAnswer: ['A', 'B'],
          score: 7,
          errorType: '概念错误',
          feedback: '显示器是输出设备，不是输入设备。',
          type: 'multiple'
        },
        {
          questionId: 3,
          questionContent: '计算机病毒是一种程序。',
          studentAnswer: 'true',
          correctAnswer: 'true',
          score: 5,
          errorType: '',
          feedback: '回答正确！',
          type: 'judgment'
        },
        {
          questionId: 4,
          questionContent: '请简述计算机的发展历程。',
          studentAnswer: '计算机的发展经历了四个阶段：电子管计算机、晶体管计算机、集成电路计算机和大规模集成电路计算机。',
          correctAnswer: '计算机的发展经历了四个阶段：1. 电子管计算机（1946-1958）；2. 晶体管计算机（1958-1964）；3. 集成电路计算机（1964-1971）；4. 大规模集成电路计算机（1971至今）。',
          score: 20,
          errorType: '逻辑错误',
          feedback: '回答基本正确，但缺少具体的时间范围。',
          type: 'essay'
        }
      ]
    };
  } catch (error) {
    ElMessage.error('获取批改报告失败');
    return null;
  }
};

const { execute: fetchReport } = useRequest(getReport);

onMounted(() => {
  fetchReport().then((data: any) => {
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

.report-info {
  margin-top: 10px;
  display: flex;
  gap: 20px;
  color: #606266;
}

.question-item {
  margin-bottom: 24px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-score {
  font-weight: 500;
  color: #67c23a;
}

.question-content,
.question-answer,
.question-correct-answer,
.question-feedback {
  margin-bottom: 16px;
}

.question-content h4,
.question-answer h4,
.question-correct-answer h4,
.question-feedback h4 {
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.question-content p,
.question-answer p,
.question-correct-answer p,
.question-feedback p {
  line-height: 1.5;
}

.question-answer {
  background-color: #f9f9f9;
  padding: 12px;
  border-radius: 4px;
}

.question-correct-answer {
  background-color: #f0f9eb;
  padding: 12px;
  border-radius: 4px;
}

.question-feedback {
  background-color: #ecf5ff;
  padding: 12px;
  border-radius: 4px;
}
</style>