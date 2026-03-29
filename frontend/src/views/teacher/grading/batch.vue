<template>
  <div class="teacher-grading-batch">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>批量复核</span>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>
    </el-card>

    <el-card shadow="never" class="filter-card">
      <div class="filter
        <el-select
          v-model="filterForm.courseId"
          placeholder="选择课程"
          class="filter-item"
        >
          <el-option
            v-for="course in courses"
            :key="course.courseId"
            :label="course.courseName"
            :value="course.courseId"
          />
        </el-select>
        <el-select
          v-model="filterForm.assignmentId"
          placeholder="选择作业"
          class="filter-item"
        >
          <el-option
            v-for="assignment in assignments"
            :key="assignment.assignmentId"
            :label="assignment.assignmentName"
            :value="assignment.assignmentId"
          />
        </el-select>
        <el-select
          v-model="filterForm.questionId"
          placeholder="选择题目"
          class="filter-item"
        >
          <el-option
            v-for="question in questions"
            :key="question.questionId"
            :label="`${question.questionType === 'single' ? '单选题' : question.questionType === 'multiple' ? '多选题' : question.questionType === 'truefalse' ? '判断题' : '主观题'} - ${question.content.substring(0, 20)}...`"
            :value="question.questionId"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>学生答案列表</span>
          <div class="header-actions">
            <el-checkbox v-model="selectAll" @change="handleSelectAll"
              >全选</el-checkbox
            >
            <el-button type="primary" @click="batchAdopt">批量采用</el-button>
            <el-button @click="batchModify">批量修改</el-button>
            <el-button @click="batchAddComment">批量添加评语</el-button>
          </div>
        </div>
      </template>
      <div class="batch-review-container">
        <div class="question-info">
          <h3>题目：{{ currentQuestion?.content }}</h3>
          <p
            v-if="
              ['single', 'multiple', 'truefalse'].includes(
                currentQuestion?.questionType || '',
              )
            "
          >
            正确答案：{{ currentQuestion?.correctAnswer.join(', ') }}
          </p>
          <p>分值：{{ currentQuestion?.score }}分</p>
        </div>

        <div class="students-answers">
          <div
            v-for="student in studentsAnswers"
            :key="student.studentId"
            class="student-answer-card"
          >
            <div class="student-header">
              <div class="student-info">
                <el-checkbox
                  v-model="student.selected"
                  @change="handleStudentSelect"
                ></el-checkbox>
                <span class="student-name">{{ student.studentName }}</span>
              </div>
              <div class="student-score">
                <span class="score-label">AI评分：</span>
                <span class="score-value"
                  >{{ student.aiScore }}/{{ currentQuestion?.score }}</span
                >
              </div>
            </div>

            <!-- 学生答案 -->
            <div class="answer-content">
              <div
                v-if="
                  ['single', 'multiple', 'truefalse'].includes(
                    currentQuestion?.questionType || '',
                  )
                "
                class="objective-answer"
              >
                <span>学生答案：{{ student.answer }}</span>
              </div>
              <div
                v-else
                class="subjective-answer"
                v-html="student.answer"
              ></div>
            </div>

            <!-- AI反馈 -->
            <div
              v-if="currentQuestion?.questionType === 'subjective'"
              class="ai-feedback"
            >
              <div class="feedback-label">AI反馈：</div>
              <div class="feedback-content" v-html="student.aiFeedback"></div>
            </div>

            <!-- 教师操作 -->
            <div class="teacher-actions">
              <el-button
                size="small"
                type="primary"
                @click="adoptScore(student.studentId)"
                >采用</el-button
              >
              <el-button size="small" @click="modifyScore(student.studentId)"
                >修改</el-button
              >
              <el-button size="small" @click="addComment(student.studentId)"
                >添加评语</el-button
              >
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRequest } from '@/composables/useRequest';

interface Course {
  courseId: number;
  courseName: string;
}

interface Assignment {
  assignmentId: number;
  assignmentName: string;
}

interface Question {
  questionId: number;
  questionType: string;
  content: string;
  correctAnswer?: string[];
  score: number;
}

interface StudentAnswer {
  studentId: number;
  studentName: string;
  answer: string;
  aiScore: number;
  aiFeedback: string;
  selected: boolean;
}

interface FilterForm {
  courseId: string;
  assignmentId: string;
  questionId: string;
}

const router = useRouter();

const filterForm = ref<FilterForm>({
  courseId: '',
  assignmentId: '',
  questionId: '',
});

const selectAll = ref(false);

const courses = ref<Course[]>([
  { courseId: 1, courseName: '数据结构' },
  { courseId: 2, courseName: '算法设计与分析' },
  { courseId: 3, courseName: '计算机导论' },
]);

const assignments = ref<Assignment[]>([
  { assignmentId: 1, assignmentName: '数据结构作业1' },
  { assignmentId: 2, assignmentName: '数据结构作业2' },
]);

const questions = ref<Question[]>([
  {
    questionId: 1,
    questionType: 'single',
    content: '数据结构中，栈的特点是？',
    correctAnswer: ['B'],
    score: 5,
  },
  {
    questionId: 2,
    questionType: 'multiple',
    content: '以下哪些是线性数据结构？',
    correctAnswer: ['A', 'B'],
    score: 8,
  },
  {
    questionId: 3,
    questionType: 'subjective',
    content: '请简述冒泡排序的基本原理。',
    score: 12,
  },
]);

const currentQuestion = computed(() => {
  return (
    questions.value.find(
      (q) => q.questionId === parseInt(filterForm.value.questionId),
    ) || null
  );
});

const studentsAnswers = ref<StudentAnswer[]>([
  {
    studentId: 1,
    studentName: '张三',
    answer: 'B',
    aiScore: 5,
    aiFeedback: '',
    selected: false,
  },
  {
    studentId: 2,
    studentName: '李四',
    answer: 'A',
    aiScore: 0,
    aiFeedback: '',
    selected: false,
  },
  {
    studentId: 3,
    studentName: '王五',
    answer: 'B',
    aiScore: 5,
    aiFeedback: '',
    selected: false,
  },
  {
    studentId: 4,
    studentName: '赵六',
    answer: 'B',
    aiScore: 5,
    aiFeedback: '',
    selected: false,
  },
]);

const getBatchReviewData = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get('/teacher/grading/batch', {
    //   params: filterForm.value
    // });
    // return response.data;

    // 模拟数据
    return {
      studentsAnswers: studentsAnswers.value,
    };
  } catch (error) {
    ElMessage.error('获取批量复核数据失败');
    return null;
  }
};

const { execute: fetchBatchReviewData } = useRequest(getBatchReviewData);

const handleSearch = () => {
  fetchBatchReviewData();
};

const handleSelectAll = (value: boolean) => {
  studentsAnswers.value.forEach((student) => {
    student.selected = value;
  });
};

const handleStudentSelect = () => {
  selectAll.value = studentsAnswers.value.every((student) => student.selected);
};

const batchAdopt = () => {
  const selectedStudents = studentsAnswers.value.filter(
    (student) => student.selected,
  );
  if (selectedStudents.length === 0) {
    ElMessage.warning('请选择学生');
    return;
  }
  ElMessage.success(`已批量采用 ${selectedStudents.length} 个学生的评分`);
};

const batchModify = () => {
  const selectedStudents = studentsAnswers.value.filter(
    (student) => student.selected,
  );
  if (selectedStudents.length === 0) {
    ElMessage.warning('请选择学生');
    return;
  }
  // 这里可以打开一个对话框让教师输入统一的评分
  ElMessageBox.prompt('请输入统一评分', '批量修改评分', {
    inputValidator: (value) => {
      const score = parseInt(value);
      if (
        isNaN(score) ||
        score < 0 ||
        score > (currentQuestion.value?.score || 100)
      ) {
        return '请输入有效的评分';
      }
      return true;
    },
  }).then(() => {
    ElMessage.success(`已批量修改 ${selectedStudents.length} 个学生的评分`);
  });
};

const batchAddComment = () => {
  const selectedStudents = studentsAnswers.value.filter(
    (student) => student.selected,
  );
  if (selectedStudents.length === 0) {
    ElMessage.warning('请选择学生');
    return;
  }
  // 这里可以打开一个对话框让教师输入统一的评语
  ElMessageBox.prompt('请输入统一评语', '批量添加评语', {
    inputType: 'textarea',
    inputPlaceholder: '请输入评语内容',
  }).then(() => {
    ElMessage.success(`已批量添加 ${selectedStudents.length} 个学生的评语`);
  });
};

const adoptScore = (_studentId: number) => {
  ElMessage.success('已采用AI评分');
};

const modifyScore = (_studentId: number) => {
  // 这里可以打开一个对话框让教师输入修改后的评分
  ElMessageBox.prompt('请输入修改后的评分', '修改评分', {
    inputValidator: (value) => {
      const score = parseInt(value);
      if (
        isNaN(score) ||
        score < 0 ||
        score > (currentQuestion.value?.score || 100)
      ) {
        return '请输入有效的评分';
      }
      return true;
    },
  }).then(() => {
    ElMessage.success('评分修改成功');
  });
};

const addComment = (_studentId: number) => {
  // 这里可以打开一个对话框让教师输入评语
  ElMessageBox.prompt('请输入评语', '添加评语', {
    inputType: 'textarea',
    inputPlaceholder: '请输入评语内容',
  }).then(() => {
    ElMessage.success('评语添加成功');
  });
};

const goBack = () => {
  router.push('/teacher/grading');
};

// 初始加载数据
fetchBatchReviewData();
</script>

<style scoped>
.teacher-grading-batch {
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

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-item {
  min-width: 200px;
  flex: 1;
}

.batch-review-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-info {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.question-info h3 {
  margin-bottom: 10px;
  color: #303133;
}

.question-info p {
  margin: 5px 0;
  color: #606266;
}

.students-answers {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.student-answer-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
}

.student-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.student-name {
  font-weight: bold;
  color: #303133;
}

.student-score {
  display: flex;
  align-items: center;
  gap: 5px;
}

.score-label {
  color: #606266;
}

.score-value {
  font-weight: bold;
  color: #409eff;
}

.answer-content {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.objective-answer {
  font-size: 14px;
  line-height: 1.5;
}

.subjective-answer {
  line-height: 1.6;
}

.ai-feedback {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #ecf5ff;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

.feedback-label {
  font-weight: bold;
  margin-bottom: 5px;
  color: #409eff;
}

.feedback-content {
  font-size: 14px;
  line-height: 1.5;
  color: #606266;
}

.teacher-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .filter-container {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-item {
    width: 100%;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .students-answers {
    grid-template-columns: 1fr;
  }

  .student-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .teacher-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
n: column;
    align-items: stretch;
  }
  
  .filter-item {
    width: 100%;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .students-answers {
    grid-template-columns: 1fr;
  }
  
  .student-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .teacher-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>