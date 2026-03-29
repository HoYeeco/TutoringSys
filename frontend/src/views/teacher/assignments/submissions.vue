<template>
  <div class="teacher-submissions">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>作业提交列表</span>
          <el-tag type="info">{{ assignmentTitle }}</el-tag>
        </div>
      </template>
      <div class="assignment-info">
        <p>课程：{{ courseName }}</p>
        <p>截止时间：{{ formatDate(deadline) }}</p>
        <p>提交人数：{{ totalSubmissions }} / {{ totalStudents }}</p>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>学生提交</span>
        </div>
      </template>
      <el-table :data="submissions" style="width: 100%">
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="submitTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'graded' ? 'success' : 'info'">
              {{ scope.row.status === 'graded' ? '已批改' : '待批改' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewSubmission(scope.row.id)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination mt-4">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalSubmissions"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 提交详情弹窗 -->
    <el-dialog
      v-model="submissionDialogVisible"
      :title="`${currentSubmission.studentName}的提交`"
      width="800px"
    >
      <div
        v-for="(question, index) in currentSubmission.questions"
        :key="question.questionId"
        class="question-item"
      >
        <div class="question-header">
          <span class="question-number">{{ index + 1 }}.</span>
          <span class="question-type">{{
            getQuestionTypeText(question.type)
          }}</span>
          <span class="question-score">
            得分：
            <el-input-number
              v-model="question.score"
              :min="0"
              :max="question.maxScore"
              style="width: 80px"
            />
            / {{ question.maxScore }}
          </span>
        </div>
        <div class="question-content">
          <h4>题目：</h4>
          <p>{{ question.questionContent }}</p>
        </div>
        <div class="question-answer">
          <h4>学生答案：</h4>
          <p>{{ question.studentAnswer }}</p>
        </div>
        <div class="question-correct-answer">
          <h4>标准答案：</h4>
          <p>{{ question.correctAnswer }}</p>
        </div>
        <div class="question-feedback">
          <h4>反馈：</h4>
          <el-input
            v-model="question.feedback"
            type="textarea"
            rows="3"
            placeholder="请输入反馈"
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="submissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReview">提交复核</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const route = useRoute();
const assignmentId = computed(() => route.params.id as string);

const assignmentTitle = ref('第一章 计算机基础作业');
const courseName = ref('计算机导论');
const deadline = ref('2026-03-10 23:59:59');
const totalSubmissions = ref(3);
const totalStudents = ref(3);

const submissions = ref([
  {
    id: 1,
    studentId: '2026001',
    studentName: '张三',
    submitTime: '2026-03-10 10:00:00',
    score: 85,
    status: 'graded',
  },
  {
    id: 2,
    studentId: '2026002',
    studentName: '李四',
    submitTime: '2026-03-10 11:00:00',
    score: 90,
    status: 'graded',
  },
  {
    id: 3,
    studentId: '2026003',
    studentName: '王五',
    submitTime: '2026-03-10 12:00:00',
    score: 75,
    status: 'graded',
  },
]);

const currentPage = ref(1);
const pageSize = ref(10);

const submissionDialogVisible = ref(false);
const currentSubmission = ref({
  id: '',
  studentId: '',
  studentName: '',
  questions: [],
});

const getSubmissions = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get(`/teacher/assignments/${assignmentId.value}/submissions`);
    // return response.data;

    // 模拟数据
    return {
      assignment: {
        title: '第一章 计算机基础作业',
        courseName: '计算机导论',
        deadline: '2026-03-10 23:59:59',
        totalStudents: 3,
      },
      submissions: [
        {
          id: 1,
          studentId: '2026001',
          studentName: '张三',
          submitTime: '2026-03-10 10:00:00',
          score: 85,
          status: 'graded',
        },
        {
          id: 2,
          studentId: '2026002',
          studentName: '李四',
          submitTime: '2026-03-10 11:00:00',
          score: 90,
          status: 'graded',
        },
        {
          id: 3,
          studentId: '2026003',
          studentName: '王五',
          submitTime: '2026-03-10 12:00:00',
          score: 75,
          status: 'graded',
        },
      ],
    };
  } catch (error) {
    ElMessage.error('获取提交列表失败');
    return null;
  }
};

const getSubmissionDetail = async (submissionId: string) => {
  try {
    // 实际项目中调用接口
    // const response = await request.get(`/teacher/submissions/${submissionId}`);
    // return response.data;

    // 模拟数据
    return {
      id: submissionId,
      studentId: '2026001',
      studentName: '张三',
      questions: [
        {
          questionId: 1,
          questionContent: '计算机的基本组成部分不包括以下哪项？',
          studentAnswer: 'D',
          correctAnswer: 'D',
          score: 10,
          maxScore: 10,
          feedback: '回答正确！',
          type: 'single',
        },
        {
          questionId: 2,
          questionContent: '以下哪些是计算机的输入设备？',
          studentAnswer: ['A', 'B', 'C'],
          correctAnswer: ['A', 'B'],
          score: 7,
          maxScore: 15,
          feedback: '显示器是输出设备，不是输入设备。',
          type: 'multiple',
        },
        {
          questionId: 3,
          questionContent: '计算机病毒是一种程序。',
          studentAnswer: 'true',
          correctAnswer: 'true',
          score: 5,
          maxScore: 5,
          feedback: '回答正确！',
          type: 'judgment',
        },
        {
          questionId: 4,
          questionContent: '请简述计算机的发展历程。',
          studentAnswer:
            '计算机的发展经历了四个阶段：电子管计算机、晶体管计算机、集成电路计算机和大规模集成电路计算机。',
          correctAnswer:
            '计算机的发展经历了四个阶段：1. 电子管计算机（1946-1958）；2. 晶体管计算机（1958-1964）；3. 集成电路计算机（1964-1971）；4. 大规模集成电路计算机（1971至今）。',
          score: 20,
          maxScore: 20,
          feedback: '回答基本正确，但缺少具体的时间范围。',
          type: 'essay',
        },
      ],
    };
  } catch (error) {
    ElMessage.error('获取提交详情失败');
    return null;
  }
};

const reviewSubmission = async (data: any) => {
  try {
    // 实际项目中调用接口
    // const response = await request.put(`/teacher/submissions/${data.id}/review`, data);
    // return response.data;

    // 模拟成功
    return { success: true };
  } catch (error) {
    ElMessage.error('提交复核失败');
    throw error;
  }
};

const { execute: fetchSubmissions } = useRequest(getSubmissions);
const { execute: fetchSubmissionDetail } = useRequest(getSubmissionDetail);
const { execute: submitReview } = useRequest(reviewSubmission);

const viewSubmission = (submissionId: string) => {
  fetchSubmissionDetail(submissionId).then((data) => {
    if (data) {
      currentSubmission.value = data;
      submissionDialogVisible.value = true;
    }
  });
};

const confirmReview = () => {
  submitReview(currentSubmission.value).then(() => {
    ElMessage.success('复核提交成功');
    submissionDialogVisible.value = false;
    // 重新获取提交列表
    fetchSubmissions().then((data) => {
      if (data) {
        submissions.value = data.submissions;
        totalSubmissions.value = data.submissions.length;
        totalStudents.value = data.assignment.totalStudents;
      }
    });
  });
};

const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '简答题',
  };
  return typeMap[type] || type;
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  // 实际项目中重新获取数据
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  // 实际项目中重新获取数据
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchSubmissions().then((data) => {
    if (data) {
      assignmentTitle.value = data.assignment.title;
      courseName.value = data.assignment.courseName;
      deadline.value = data.assignment.deadline;
      submissions.value = data.submissions;
      totalSubmissions.value = data.submissions.length;
      totalStudents.value = data.assignment.totalStudents;
    }
  });
});
</script>

<style scoped>
.teacher-submissions {
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

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.question-item {
  margin-bottom: 24px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
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
  margin-right: 12px;
}

.question-score {
  margin-left: auto;
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
.question-correct-answer p {
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
</style>
