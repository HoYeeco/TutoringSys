<template>
  <div class="student-submit">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>{{ assignment.title }}</span>
          <el-tag type="info">{{ assignment.courseName }}</el-tag>
        </div>
      </template>
      <div class="assignment-info">
        <p>截止时间：{{ formatDate(assignment.deadline) }}</p>
        <p>总分：{{ assignment.totalScore }}</p>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <div v-for="(question, index) in questions" :key="question.id" class="question-item">
        <div class="question-header">
          <span class="question-number">{{ index + 1 }}.</span>
          <span class="question-type">{{ getQuestionTypeText(question.type) }}</span>
          <span class="question-score">({{ question.score }}分)</span>
        </div>
        <div class="question-content">
          {{ question.content }}
        </div>
        <div class="question-answer" v-if="question.type === 'single'">
          <el-radio-group v-model="answers[question.id]">
            <el-radio
              v-for="(option, idx) in question.options.split('|')"
              :key="idx"
              :label="String.fromCharCode(65 + idx)"
            >
              {{ String.fromCharCode(65 + idx) }}. {{ option }}
            </el-radio>
          </el-radio-group>
        </div>
        <div class="question-answer" v-else-if="question.type === 'multiple'">
          <el-checkbox-group v-model="answers[question.id]">
            <el-checkbox
              v-for="(option, idx) in question.options.split('|')"
              :key="idx"
              :label="String.fromCharCode(65 + idx)"
            >
              {{ String.fromCharCode(65 + idx) }}. {{ option }}
            </el-checkbox>
          </el-checkbox-group>
        </div>
        <div class="question-answer" v-else-if="question.type === 'judgment'">
          <el-radio-group v-model="answers[question.id]">
            <el-radio label="true">正确</el-radio>
            <el-radio label="false">错误</el-radio>
          </el-radio-group>
        </div>
        <div class="question-answer" v-else-if="question.type === 'essay'">
          <el-input
            v-model="answers[question.id]"
            type="textarea"
            :rows="6"
            placeholder="请输入答案"
          />
        </div>
      </div>

      <div class="submit-section">
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          提交作业
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const route = useRoute();
const router = useRouter();
const assignmentId = computed(() => route.params.id as string);

const assignment = ref({
  id: 1,
  title: '第一章 计算机基础作业',
  courseName: '计算机导论',
  deadline: '2026-03-10 23:59:59',
  totalScore: 100
});

const questions = ref([
  {
    id: 1,
    type: 'single',
    content: '计算机的基本组成部分不包括以下哪项？',
    options: 'CPU|内存|硬盘|打印机',
    score: 10
  },
  {
    id: 2,
    type: 'multiple',
    content: '以下哪些是计算机的输入设备？',
    options: '键盘|鼠标|显示器|打印机',
    score: 15
  },
  {
    id: 3,
    type: 'judgment',
    content: '计算机病毒是一种程序。',
    score: 5
  },
  {
    id: 4,
    type: 'essay',
    content: '请简述计算机的发展历程。',
    score: 20
  }
]);

const answers = ref({});
const loading = ref(false);

const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '简答题'
  };
  return typeMap[type] || type;
};

const getAssignmentDetail = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get(`/student/assignments/${assignmentId.value}`);
    // return response.data;
    
    // 模拟数据
    return {
      assignment: {
        id: 1,
        title: '第一章 计算机基础作业',
        courseName: '计算机导论',
        deadline: '2026-03-10 23:59:59',
        totalScore: 100
      },
      questions: [
        {
          id: 1,
          type: 'single',
          content: '计算机的基本组成部分不包括以下哪项？',
          options: 'CPU|内存|硬盘|打印机',
          score: 10
        },
        {
          id: 2,
          type: 'multiple',
          content: '以下哪些是计算机的输入设备？',
          options: '键盘|鼠标|显示器|打印机',
          score: 15
        },
        {
          id: 3,
          type: 'judgment',
          content: '计算机病毒是一种程序。',
          score: 5
        },
        {
          id: 4,
          type: 'essay',
          content: '请简述计算机的发展历程。',
          score: 20
        }
      ]
    };
  } catch (error) {
    ElMessage.error('获取作业详情失败');
    return null;
  }
};

const submitAssignment = async (data: any) => {
  try {
    // 实际项目中调用接口
    // const response = await request.post(`/student/assignments/submit/${assignmentId.value}`, data);
    // return response.data;
    
    // 模拟成功
    return { success: true };
  } catch (error) {
    ElMessage.error('提交作业失败');
    throw error;
  }
};

const { execute: fetchAssignmentDetail } = useRequest(getAssignmentDetail);
const { execute: submit } = useRequest(submitAssignment);

const handleSubmit = () => {
  // 检查是否所有题目都已回答
  const allAnswered = questions.value.every(question => {
    return answers.value[question.id] !== undefined && answers.value[question.id] !== '';
  });

  if (!allAnswered) {
    ElMessage.warning('请完成所有题目后再提交');
    return;
  }

  ElMessageBox.confirm('确定要提交作业吗？提交后将无法修改。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    loading.value = true;
    
    const submissionData = {
      assignmentId: assignmentId.value,
      answers: questions.value.map(question => ({
        questionId: question.id,
        answerContent: answers.value[question.id]
      }))
    };

    submit(submissionData).then(() => {
      ElMessage.success('作业提交成功');
      router.push('/student/assignments');
    }).finally(() => {
      loading.value = false;
    });
  });
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchAssignmentDetail().then((data: any) => {
    if (data) {
      assignment.value = data.assignment;
      questions.value = data.questions;
      // 初始化答案对象
      questions.value.forEach(question => {
        answers.value[question.id] = '';
      });
    }
  });
});
</script>

<style scoped>
.student-submit {
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

.assignment-info {
  margin-top: 10px;
  color: #606266;
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
}

.question-content {
  margin-bottom: 16px;
  line-height: 1.5;
}

.question-answer {
  margin-left: 24px;
}

.submit-section {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}
</style>