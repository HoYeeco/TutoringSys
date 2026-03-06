<template>
  <div class="teacher-assignment-edit">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑作业' : '创建作业' }}</span>
        </div>
      </template>
      <el-form :model="assignment" label-width="100px">
        <el-form-item label="作业标题">
          <el-input v-model="assignment.title" placeholder="请输入作业标题" />
        </el-form-item>
        <el-form-item label="所属课程">
          <el-select v-model="assignment.courseId" placeholder="选择课程">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="assignment.deadline"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>题目管理</span>
          <div>
            <el-button type="primary" @click="openQuestionBankDialog">从题库选择</el-button>
            <el-button type="success" @click="addQuestion">添加题目</el-button>
          </div>
        </div>
      </template>
      
      <div v-for="(question, index) in questions" :key="question.id" class="question-card">
        <div class="question-header">
          <span class="question-number">{{ index + 1 }}.</span>
          <el-select v-model="question.type" placeholder="选择题型" style="width: 120px">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judgment" />
            <el-option label="简答题" value="essay" />
          </el-select>
          <el-button
            type="danger"
            size="small"
            @click="removeQuestion(index)"
          >
            删除
          </el-button>
        </div>
        
        <el-form :model="question" label-width="80px" class="mt-2">
          <el-form-item label="题目内容">
            <el-input
              v-model="question.content"
              type="textarea"
              rows="3"
              placeholder="请输入题目内容"
            />
          </el-form-item>
          
          <el-form-item label="选项" v-if="['single', 'multiple'].includes(question.type)">
            <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-item">
              <el-input
                v-model="option.value"
                placeholder="请输入选项内容"
                style="width: 80%"
              />
              <el-button
                type="danger"
                size="small"
                @click="removeOption(question, optIndex)"
              >
                删除
              </el-button>
            </div>
            <el-button
              type="primary"
              size="small"
              @click="addOption(question)"
            >
              添加选项
            </el-button>
          </el-form-item>
          
          <el-form-item label="答案">
            <el-input
              v-model="question.correctAnswer"
              placeholder="请输入答案"
            />
          </el-form-item>
          
          <el-form-item label="分值">
            <el-input-number
              v-model="question.score"
              :min="1"
              :max="100"
              style="width: 100px"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <div class="total-score mt-4">
        <span>总分：{{ totalScore }}分</span>
      </div>
    </el-card>

    <div class="action-buttons mt-4">
      <el-button @click="saveDraft">保存草稿</el-button>
      <el-button type="primary" @click="publishAssignment">发布作业</el-button>
    </div>

    <!-- 题库选择弹窗 -->
    <el-dialog
      v-model="questionBankDialogVisible"
      title="从题库选择题目"
      width="800px"
    >
      <el-table :data="questionBank" style="width: 100%">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="题目ID" width="80" />
        <el-table-column prop="type" label="题型" width="100">
          <template #default="scope">
            {{ getQuestionTypeText(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" />
        <el-table-column prop="score" label="分值" width="80" />
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="questionBankDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSelectQuestions">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const route = useRoute();
const router = useRouter();
const assignmentId = computed(() => route.params.id as string);
const isEdit = computed(() => !!assignmentId.value);

const assignment = ref({
  id: '',
  title: '',
  courseId: '',
  deadline: ''
});

const courses = ref([
  { id: 1, name: '计算机导论' },
  { id: 2, name: '数据结构' },
  { id: 3, name: '算法设计与分析' }
]);

const questions = ref([
  {
    id: 1,
    type: 'single',
    content: '',
    options: [{ value: '' }, { value: '' }, { value: '' }, { value: '' }],
    correctAnswer: '',
    score: 10
  }
]);

const questionBank = ref([
  {
    id: 1,
    type: 'single',
    content: '计算机的基本组成部分不包括以下哪项？',
    options: ['CPU', '内存', '硬盘', '打印机'],
    correctAnswer: 'D',
    score: 10
  },
  {
    id: 2,
    type: 'multiple',
    content: '以下哪些是计算机的输入设备？',
    options: ['键盘', '鼠标', '显示器', '打印机'],
    correctAnswer: 'A,B',
    score: 15
  },
  {
    id: 3,
    type: 'judgment',
    content: '计算机病毒是一种程序。',
    correctAnswer: 'true',
    score: 5
  }
]);

const questionBankDialogVisible = ref(false);

const totalScore = computed(() => {
  return questions.value.reduce((sum, question) => sum + (question.score || 0), 0);
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

const addQuestion = () => {
  questions.value.push({
    id: Date.now(),
    type: 'single',
    content: '',
    options: [{ value: '' }, { value: '' }, { value: '' }, { value: '' }],
    correctAnswer: '',
    score: 10
  });
};

const removeQuestion = (index: number) => {
  ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    questions.value.splice(index, 1);
  });
};

const addOption = (question: any) => {
  question.options.push({ value: '' });
};

const removeOption = (question: any, index: number) => {
  if (question.options.length > 2) {
    question.options.splice(index, 1);
  } else {
    ElMessage.warning('至少需要两个选项');
  }
};

const openQuestionBankDialog = () => {
  // 实际项目中调用接口获取题库
  questionBankDialogVisible.value = true;
};

const confirmSelectQuestions = () => {
  // 实际项目中处理选中的题目
  questionBankDialogVisible.value = false;
  ElMessage.success('题目添加成功');
};

const saveDraft = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.post('/teacher/assignments/draft', {
    //   ...assignment.value,
    //   questions: questions.value
    // });
    
    // 模拟成功
    ElMessage.success('草稿保存成功');
  } catch (error) {
    ElMessage.error('保存草稿失败');
  }
};

const publishAssignment = async () => {
  try {
    // 验证表单
    if (!assignment.value.title) {
      ElMessage.warning('请输入作业标题');
      return;
    }
    if (!assignment.value.courseId) {
      ElMessage.warning('请选择所属课程');
      return;
    }
    if (!assignment.value.deadline) {
      ElMessage.warning('请设置截止时间');
      return;
    }
    if (questions.value.length === 0) {
      ElMessage.warning('请添加至少一道题目');
      return;
    }
    
    // 实际项目中调用接口
    // const response = await request.post('/teacher/assignments/publish', {
    //   ...assignment.value,
    //   questions: questions.value
    // });
    
    // 模拟成功
    ElMessage.success('作业发布成功');
    router.push('/teacher/courses');
  } catch (error) {
    ElMessage.error('发布作业失败');
  }
};

const getAssignmentDetail = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get(`/teacher/assignments/${assignmentId.value}`);
    // return response.data;
    
    // 模拟数据
    return {
      assignment: {
        id: 1,
        title: '第一章 计算机基础作业',
        courseId: 1,
        deadline: '2026-03-10 23:59:59'
      },
      questions: [
        {
          id: 1,
          type: 'single',
          content: '计算机的基本组成部分不包括以下哪项？',
          options: [{ value: 'CPU' }, { value: '内存' }, { value: '硬盘' }, { value: '打印机' }],
          correctAnswer: 'D',
          score: 10
        },
        {
          id: 2,
          type: 'multiple',
          content: '以下哪些是计算机的输入设备？',
          options: [{ value: '键盘' }, { value: '鼠标' }, { value: '显示器' }, { value: '打印机' }],
          correctAnswer: 'A,B',
          score: 15
        }
      ]
    };
  } catch (error) {
    ElMessage.error('获取作业详情失败');
    return null;
  }
};

const { execute: fetchAssignmentDetail } = useRequest(getAssignmentDetail);

onMounted(() => {
  if (isEdit.value) {
    fetchAssignmentDetail().then(data => {
      if (data) {
        assignment.value = data.assignment;
        questions.value = data.questions;
      }
    });
  }
});
</script>

<style scoped>
.teacher-assignment-edit {
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

.question-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.question-number {
  font-weight: 500;
  margin-right: 12px;
  width: 30px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.option-item .el-button {
  margin-left: 12px;
}

.total-score {
  font-size: 16px;
  font-weight: 500;
  color: #409eff;
  text-align: right;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>