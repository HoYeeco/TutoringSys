<template>
  <div class="teacher-submissions">

    <!-- 页面头部卡片 -->
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="title-content">
              <span class="main-title">{{ assignmentTitle }}</span>
            </div>
          </div>
              <!-- 返回按钮 -->
          <div class="back-nav">
            <el-button type="primary" @click="goBack" class="back-btn" plain>
            <el-icon><ArrowLeft /></el-icon> 返回列表
          </el-button>
          </div>
        </div>
      </template>
      
      <!-- 统计卡片区域 -->
      <div class="stats-container">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :lg="6">
            <div class="stat-card course-card">
              <div class="stat-icon">
                <el-icon><Reading /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">所属课程</div>
                <div class="stat-value" :title="courseName">{{ courseName || '-' }}</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="6">
            <div class="stat-card deadline-card">
              <div class="stat-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">截止时间</div>
                <div class="stat-value">{{ formatDate(deadline) }}</div>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="6">
            <div class="stat-card total-card">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">应交人数</div>
                <div class="stat-value">{{ totalStudents }} <span class="unit">人</span></div>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :lg="6">
            <div class="stat-card submitted-card">
              <div class="stat-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">已提交人数</div>
                <div class="stat-value">{{ totalSubmissions }} <span class="unit">人</span></div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 进度条区域 -->
      <div class="progress-section">
        <div class="progress-header">
          <span class="progress-title">提交进度</span>
          <span class="progress-stats">
            <span class="submitted-num">{{ totalSubmissions }}</span>
            <span class="divider">/</span>
            <span class="total-num">{{ totalStudents }}</span>
          </span>
        </div>
        <el-progress
          :percentage="completionRate"
          :stroke-width="12"
          :status="completionRate === 100 ? 'success' : ''"
          class="completion-progress"
        />
      </div>
    </el-card>

    <!-- 学生提交列表卡片 -->
    <el-card shadow="never" class="submissions-card">
      <template #header>
        <div class="card-header">
          <div class="section-title">
            <el-icon><List /></el-icon>
            <span>学生提交列表</span>
            <el-badge :value="totalSubmissions" class="count-badge" type="primary" />
          </div>
          <div class="header-filters">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索学生姓名"
              clearable
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty 
        v-if="!loading && submissions.length === 0" 
        description="暂无学生提交记录"
        :image-size="120"
      >
        <template #image>
          <el-icon :size="60" color="#dcdfe6"><DocumentDelete /></el-icon>
        </template>
      </el-empty>

      <!-- 数据表格 -->
      <el-table
        v-else
        :data="filteredSubmissions"
        style="width: 100%"
        v-loading="loading"
        class="modern-table"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column label="学生信息" min-width="160">
          <template #default="scope">
            <div class="student-info-cell">
              <el-avatar 
                :size="40" 
                :src="scope.row.avatar || ''"
                class="student-avatar"
              >
                {{ scope.row.studentName?.charAt(0) || '?' }}
              </el-avatar>
              <div class="student-details">
                <div class="student-name">{{ scope.row.studentName }}</div>
                <div class="student-username">@{{ scope.row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" min-width="160">
          <template #default="scope">
            <div class="time-cell">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDate(scope.row.submitTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="150" align="center">
          <template #default="scope">
            <div class="score-cell">
              <span :class="['score-value', getScoreClass(scope.row.score)]">
                {{ scope.row.score !== null ? scope.row.score : '-' }}
              </span>
              <span v-if="scope.row.score !== null" class="score-unit">分</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="批改状态" width="150" align="center">
          <template #default="scope">
            <el-tag 
              :type="getStatusTagType(scope.row.status)" 
              size="small"
              effect="light"
              class="status-tag"
            >
              <el-icon v-if="scope.row.status === 'graded'"><CircleCheck /></el-icon>
              <el-icon v-else-if="scope.row.status === 'ai_pending'"><Clock /></el-icon>
              <el-icon v-else><Warning /></el-icon>
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewSubmission(scope.row)"
              class="action-btn"
            >
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="rejectSubmission(scope.row)"
              class="action-btn"
              plain
            >
              <el-icon><RefreshLeft /></el-icon>
              打回重做
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" v-if="submissions.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalSubmissions"
          justify="center"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <!-- 批改弹窗 -->
    <el-dialog
      v-model="submissionDialogVisible"
      :title="`${currentSubmission.studentName}的提交详情`"
      width="900px"
      append-to-body
      class="submission-dialog"
      destroy-on-close
    >
      <div class="submission-header">
        <div class="student-card">
          <el-avatar :size="56" :src="currentSubmission.avatar || ''" class="dialog-avatar">
            {{ currentSubmission.studentName?.charAt(0) || '?' }}
          </el-avatar>
          <div class="student-meta-info">
            <div class="student-name">{{ currentSubmission.studentName }}</div>
            <div class="student-meta">
              <el-icon><Clock /></el-icon>
              提交于 {{ formatDate(currentSubmission.submitTime) }}
            </div>
          </div>
        </div>
        <div class="score-display">
          <div class="score-label">总分</div>
          <div class="score-value-large" :class="getScoreClass(currentSubmission.totalScore)">
            {{ currentSubmission.totalScore ?? '-' }}
          </div>
        </div>
      </div>

      <el-divider class="header-divider" />

      <div class="questions-container">
        <div
          v-for="(question, index) in currentSubmission.questions"
          :key="question.questionId"
          class="question-item"
        >
          <div class="question-header">
            <div class="question-info">
              <span class="question-number">{{ index + 1 }}</span>
              <el-tag size="small" :type="getQuestionTypeTag(question.type)" effect="light">
                {{ getQuestionTypeText(question.type) }}
              </el-tag>
              <el-tag 
                v-if="question.graderType" 
                size="small" 
                :type="getGraderTagType(question.graderType)" 
                effect="plain"
                class="grader-tag"
              >
                {{ getGraderText(question.graderType) }}
              </el-tag>
            </div>
            <!-- 客观题直接显示系统分数，不可修改 -->
            <div v-if="isObjectiveQuestion(question.type)" class="question-score-display">
              <span class="score-label-text">得分：</span>
              <span class="score-value" :class="getScoreClass(question.score)">{{ question.score ?? '-' }}</span>
              <span class="max-score"> / {{ question.maxScore }} 分</span>
              <el-tag size="small" type="success" effect="plain">系统自动批改</el-tag>
            </div>
            <!-- 主观题显示分数来源 -->
            <div v-else class="question-score-display">
              <span class="score-label-text">得分：</span>
              <span class="score-value" :class="getScoreClass(getSubjectiveQuestionScore(question))">
                {{ getSubjectiveQuestionScore(question) ?? '-' }}
              </span>
              <span class="max-score"> / {{ question.maxScore }} 分</span>
              <el-tag 
                v-if="question.reviewStatus === 2" 
                size="small" 
                type="success" 
                effect="plain"
              >
                已复核
              </el-tag>
              <el-tag 
                v-else-if="question.aiScore !== null && question.aiScore !== undefined" 
                size="small" 
                type="warning" 
                effect="plain"
              >
                AI评分: {{ question.aiScore }}分
              </el-tag>
              <el-tag v-else size="small" type="info" effect="plain">待评分</el-tag>
            </div>
          </div>
          
          <div class="question-content-box">
            <div class="content-label">
              <el-icon><QuestionFilled /></el-icon>
              题目内容
            </div>
            <div class="content-text" v-html="question.questionContent"></div>
          </div>
          
          <div class="answer-box student-answer" :class="getAnswerBoxClass(question)">
            <div class="answer-label">
              <el-icon><EditPen /></el-icon>
              学生答案
            </div>
            <div class="answer-text" v-html="formatStudentAnswer(question)"></div>
          </div>

          <div v-if="question.aiFeedback && !isObjectiveQuestion(question.type)" class="answer-box ai-feedback-box" style="background: #f8f9fc; border: 1px solid #e8ecf4;">
            <div class="answer-label" style="color: #606266;">
              <el-icon><ChatDotRound /></el-icon>
              AI评语
            </div>
            <div class="answer-text ai-feedback-text" style="background: transparent; border: none; color: #303133;">{{ question.aiFeedback }}</div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="danger" @click="deleteSubmission" :loading="deleting" size="large">
            <el-icon><Delete /></el-icon>
            删除提交
          </el-button>
          <el-button @click="submissionDialogVisible = false" size="large">
            <el-icon><Close /></el-icon>
            关闭
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onActivated } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Document, Reading, Clock, User, CircleCheck, 
  View, ArrowLeft, List, Search, 
  DocumentDelete, Warning, Close, QuestionFilled, EditPen, ChatDotRound, Delete, RefreshLeft
} from '@element-plus/icons-vue';
import request from '@/utils/request';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const assignmentId = computed(() => route.params.id as string);

const isAdmin = computed(() => {
  const role = (userStore.userInfo?.role || userStore.role || '').toUpperCase();
  return role === 'ADMIN';
});

const assignmentTitle = ref('');
const courseName = ref('');
const deadline = ref('');
const totalSubmissions = ref(0);
const totalStudents = ref(0);
const loading = ref(false);
const deleting = ref(false);

const submissions = ref<any[]>([]);
const searchKeyword = ref('');

const currentPage = ref(1);
const pageSize = ref(10);

const submissionDialogVisible = ref(false);
const currentSubmission = ref<any>({
  id: '',
  studentId: '',
  studentName: '',
  submitTime: '',
  avatar: '',
  totalScore: null,
  questions: [],
});

const completionRate = computed(() => {
  if (totalStudents.value === 0) return 0;
  return Math.round((totalSubmissions.value / totalStudents.value) * 100);
});

// 过滤后的提交列表
const filteredSubmissions = computed(() => {
  if (!searchKeyword.value) return submissions.value;
  const keyword = searchKeyword.value.toLowerCase();
  return submissions.value.filter(item => 
    item.studentName?.toLowerCase().includes(keyword) ||
    item.username?.toLowerCase().includes(keyword)
  );
});

// 返回上一页
const goBack = () => {
  router.back();
};

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1;
};

// 根据分数获取样式类
const getScoreClass = (score: number | null) => {
  if (score === null) return 'score-null';
  return 'score';
};

// 根据题目得分获取学生答案框样式类
const getAnswerBoxClass = (question: any) => {
  const score = question.score || 0;
  const maxScore = question.maxScore || 1;
  if (score >= maxScore) return 'answer-full-score';
  if (score === 0) return 'answer-zero-score';
  return 'answer-partial-score';
};

// 格式化学生答案
const formatStudentAnswer = (question: any) => {
  const answer = question.studentAnswer;
  if (!answer) return '<span class="no-answer">未作答</span>';
  
  const type = question.type;
  const score = question.score || 0;
  const maxScore = question.maxScore || 1;
  
  // 选择题：只显示字母
  if (type === 'single' || type === 'multiple') {
    const letters = answer.match(/[A-Z]/g);
    if (letters) return letters.join('');
    return answer;
  }
  
  // 判断题：转换为文字
  if (type === 'judgment') {
    const lowerAnswer = answer.toLowerCase();
    if (lowerAnswer === 'true') {
      return '正确';
    } else if (lowerAnswer === 'false') {
      return '错误';
    }
    return answer;
  }
  
  return answer;
};

const mapReviewStatus = (reviewStatus: number | null): string => {
  if (reviewStatus === 0) return 'graded';
  if (reviewStatus === 1) return 'ai_pending';
  if (reviewStatus === 2) return 'graded';
  return 'pending';
};

const getStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    graded: '已批改',
    ai_pending: '待复核',
    pending: '待批改'
  };
  return statusMap[status] || status;
};

const getStatusTagType = (status: string): string => {
  const typeMap: Record<string, string> = {
    graded: 'success',
    ai_pending: 'warning',
    pending: 'danger'
  };
  return typeMap[status] || 'info';
};

const getSubmissions = async () => {
  if (!assignmentId.value || assignmentId.value === 'undefined') {
    console.warn('assignmentId is undefined, skipping getSubmissions');
    return;
  }
  
  loading.value = true;
  try {
    const baseUrl = isAdmin.value ? '/admin/assignments' : '/teacher/assignments';
    
    const assignmentResponse = await request.get(`${baseUrl}/${assignmentId.value}`);
    if (assignmentResponse.data) {
      assignmentTitle.value = assignmentResponse.data.title || '';
      courseName.value = assignmentResponse.data.courseName || '';
      deadline.value = assignmentResponse.data.deadline || '';
      totalStudents.value = assignmentResponse.data.totalStudents ?? 0;
    }

    const response = await request.get(`${baseUrl}/${assignmentId.value}/submissions`, {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    });
    
    if (response.data) {
      totalSubmissions.value = response.data.total || 0;
      submissions.value = (response.data.records || []).map((record: any) => ({
        id: record.id,
        studentId: record.studentId,
        studentName: record.studentName,
        username: record.studentUsername,
        avatar: record.studentAvatar,
        submitTime: record.submitTime,
        score: record.finalTotalScore,
        aiScore: record.aiTotalScore,
        status: mapReviewStatus(record.reviewStatus),
        reviewStatus: record.reviewStatus
      }));
    }
  } catch (error) {
    console.error('获取提交列表失败:', error);
    ElMessage.error('获取提交列表失败');
  } finally {
    loading.value = false;
  }
};

const getSubmissionDetail = async (submissionId: number) => {
  try {
    const baseUrl = isAdmin.value ? '/admin/assignments' : '/teacher/assignments';
    const response = await request.get(`${baseUrl}/submissions/${submissionId}`);
    if (response.data) {
      const data = response.data;
      return {
        id: data.id,
        studentId: data.studentId,
        studentName: data.studentName,
        avatar: data.studentAvatar,
        submitTime: data.submitTime,
        totalScore: data.finalTotalScore,
        aiTotalScore: data.aiTotalScore,
        reviewStatus: data.reviewStatus,
        questions: (data.answers || []).map((answer: any, index: number) => ({
          questionId: answer.questionId,
          type: answer.questionType,
          questionContent: answer.questionContent,
          maxScore: answer.questionScore,
          studentAnswer: answer.answerContent || answer.answer,
          correctAnswer: '',
          referenceAnswer: '',
          score: answer.finalScore ?? answer.score ?? null,
          aiScore: answer.aiScore ?? null,
          aiFeedback: answer.aiFeedback || '',
          feedback: answer.teacherFeedback || '',
          graderType: answer.graderType || 'PENDING',
          reviewStatus: answer.reviewStatus
        }))
      };
    }
    return null;
  } catch (error) {
    ElMessage.error('获取提交详情失败');
    return null;
  }
};

const viewSubmission = async (row: any) => {
  const data = await getSubmissionDetail(row.id);
  if (data) {
    currentSubmission.value = data;
    submissionDialogVisible.value = true;
  }
};

const deleteSubmission = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生「${currentSubmission.value.studentName}」的提交记录吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    deleting.value = true;
    const baseUrl = isAdmin.value ? '/admin/assignments' : '/teacher/assignments';
    await request.delete(`${baseUrl}/submissions/${currentSubmission.value.id}`);
    ElMessage.success('提交记录已删除');
    submissionDialogVisible.value = false;
    getSubmissions();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'));
    }
  } finally {
    deleting.value = false;
  }
};

const rejecting = ref(false);

const rejectSubmission = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要将学生「${row.studentName}」的作业打回重做吗？学生将可以重新提交作业，原提交数据将被保留。`,
      '打回重做确认',
      {
        confirmButtonText: '确定打回',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    
    rejecting.value = true;
    const baseUrl = isAdmin.value ? '/admin/assignments' : '/teacher/assignments';
    await request.post(`${baseUrl}/submissions/${row.id}/reject`);
    ElMessage.success('作业已打回，学生可重新提交');
    getSubmissions();
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('打回失败: ' + (error.response?.data?.message || error.message || '未知错误'));
    }
  } finally {
    rejecting.value = false;
  }
};

const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '简答题'
  };
  return typeMap[type] || type;
};

const getQuestionTypeTag = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '',
    multiple: '',
    judgment: '',
    essay: ''
  };
  return typeMap[type] || '';
};

const getGraderTagType = (graderType: string): string => {
  const typeMap: Record<string, string> = {
    AUTO: 'success',
    AI: 'primary',
    TEACHER: 'warning',
    PENDING: 'info'
  };
  return typeMap[graderType] || 'info';
};

const getGraderText = (graderType: string): string => {
  const textMap: Record<string, string> = {
    AUTO: '自动批改',
    AI: 'AI预评分',
    TEACHER: '人工批改',
    PENDING: '待批改'
  };
  return textMap[graderType] || graderType;
};

// 判断是否为客观题
const isObjectiveQuestion = (type: string): boolean => {
  return ['single', 'multiple', 'judgment'].includes(type);
};

// 获取主观题分数（已复核显示最终分数，未复核显示AI分数）
const getSubjectiveQuestionScore = (question: any): number | null => {
  if (question.reviewStatus === 2) {
    // 已复核，显示最终分数
    return question.finalScore ?? question.score ?? question.aiScore ?? null;
  } else {
    // 未复核，显示AI分数
    return question.aiScore ?? question.score ?? null;
  }
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  getSubmissions();
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getSubmissions();
};

const formatDate = (date: string | null) => {
  if (!date) return '-';
  try {
    const d = new Date(date);
    return d.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch {
    return date;
  }
};

onMounted(() => {
  getSubmissions();
});

onActivated(() => {
  getSubmissions();
});
</script>

<style scoped>
/* ========== 基础布局 ========== */
.teacher-submissions {
  padding: 24px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(4px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
}

.back-btn:hover {
  transform: translateX(-4px);
}

/* ========== 卡片通用样式 ========== */
:deep(.el-card) {
  border: none;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

:deep(.el-card__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: transparent;
}

:deep(.el-card__body) {
  padding: 24px;
}

/* ========== 头部样式 ========== */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.card-header__title {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-header__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.card-header__icon .el-icon {
  font-size: 26px;
}

.title-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.main-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: -0.5px;
}

.sub-title {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* ========== 统计卡片区域 ========== */
.stats-container {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  margin-bottom: 16px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-card .stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 26px;
  flex-shrink: 0;
}

.course-card .stat-icon {
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
}

.deadline-card .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.35);
}

.total-card .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.35);
}

.submitted-card .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  box-shadow: 0 4px 12px rgba(67, 233, 123, 0.35);
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
  font-weight: 500;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-value .unit {
  font-size: 13px;
  font-weight: 500;
  color: #909399;
  margin-left: 2px;
}

/* ========== 进度条区域 ========== */
.progress-section {
  background: linear-gradient(135deg, #f8faff 0%, #f0f5ff 100%);
  border-radius: 14px;
  padding: 20px 24px;
  border: 1px solid #e8eeff;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.progress-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.progress-stats {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.submitted-num {
  font-weight: 700;
  color: #409eff;
  font-size: 18px;
}

.divider {
  color: #c0c4cc;
}

.total-num {
  font-weight: 600;
  color: #606266;
}

.completion-progress :deep(.el-progress-bar__outer) {
  border-radius: 6px;
  background-color: #e4e7ed;
}

.completion-progress :deep(.el-progress-bar__inner) {
  border-radius: 6px;
  transition: all 0.4s ease;
}

/* ========== 提交列表卡片 ========== */
.submissions-card {
  margin-top: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
}

.section-title .el-icon {
  font-size: 20px;
  color: #667eea;
}

.count-badge :deep(.el-badge__content) {
  font-size: 12px;
  height: 20px;
  padding: 0 8px;
  border-radius: 10px;
}

.header-filters {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 220px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* ========== 表格样式 ========== */
.modern-table {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table__header th) {
  background: linear-gradient(135deg, #f8f9fc 0%, #f0f2f7 100%) !important;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
  padding: 14px 12px;
  text-align: center;
}

:deep(.el-table__row) {
  height: 76px;
  transition: background-color 0.2s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f8faff !important;
}

:deep(.el-table__cell) {
  padding: 12px;
  font-size: 14px;
}

/* 学生信息单元格 */
.student-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  width: fit-content;
  margin: 0 auto;
}

.student-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.student-details {
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: left;
  min-width: 0;
  line-height: 1.4;
}

.student-details .student-name {
  font-weight: 600;
  color: #1a1a2e;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.student-details .student-username {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  white-space: nowrap;
}

/* 时间单元格 */
.time-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.time-cell .el-icon {
  color: #909399;
}

/* 分数单元格 */
.score-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.score-value {
  font-weight: 700;
  font-size: 18px;
}

.score-unit {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.score-null {
  color: #ff0000;
}

.score {
  color: #17a900;
}rgb(30, 255, 0)rgb(23, 200, 0)

/* 状态标签 */
:deep(.status-tag) {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 6px !important;
  font-weight: 500;
  padding: 5px 12px !important;
  border-radius: 4px;
  min-width: 100px;
  line-height: 1;
  height: auto !important;
}

:deep(.status-tag .el-icon) {
  font-size: 14px;
  flex-shrink: 0;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
}

:deep(.status-tag span) {
  display: inline-flex !important;
  align-items: center !important;
  line-height: 1;
}

/* 操作按钮 */
.action-btn {
  border-radius: 8px;
  font-weight: 500;
}

/* 评分来源标签 */
.grader-tag {
  margin-left: 4px;
  font-size: 11px;
}

/* AI预评分提示 */
.ai-score-hint {
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
}

.ai-score-badge {
  font-size: 12px;
  color: #e6a23c;
  font-weight: 600;
  background: #fdf6ec;
  padding: 2px 8px;
  border-radius: 10px;
  border: 1px solid #f5dab1;
}

/* AI评语区域 */
.ai-feedback-box .answer-text {
  background: #fff8e6;
  border: 1px solid #ffe58f;
  color: #ad6800;
}

.ai-feedback-text {
  white-space: pre-wrap;
  line-height: 1.7;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* ========== 批改弹窗 ========== */
.submission-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
}

.submission-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  background: linear-gradient(135deg, #f8faff 0%, #f0f5ff 100%);
  border-bottom: 1px solid #e8eeff;
  margin-right: 0;
}

.submission-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
}

.submission-dialog :deep(.el-dialog__body) {
  padding: 0;
  max-height: 70vh;
  overflow-y: auto;
}

.submission-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.submission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #fff 0%, #f8faff 100%);
}

.student-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dialog-avatar {
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.student-meta-info .student-name {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.student-meta-info .student-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.score-display {
  text-align: center;
  padding: 16px 28px;
  border-radius: 16px;
  color: #fff;
  box-shadow: 0 2px 10px #e1e1e1;
}

.score-display .score-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
  font-weight: 500;
  color: #000;
}

.score-value-large {
  font-size: 32px;
  font-weight: 700;
}

.header-divider {
  margin: 0;
}

/* 题目区域 */
.questions-container {
  padding: 20px 24px;
}

.question-item {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease;
}

.question-item:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.question-item:last-child {
  margin-bottom: 0;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #e8e8e8;
}

.question-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-number {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  box-shadow: 0 4px 10px rgba(102, 126, 234, 0.3);
}

.question-score-input {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #606266;
}

.score-label-text {
  font-weight: 500;
}

.score-input {
  width: 110px;
}

.score-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.max-score {
  color: #909399;
  font-weight: 500;
}

/* 内容区域 */
.question-content-box,
.answer-box,
.feedback-box {
  margin-bottom: 16px;
}

.answer-box:last-of-type {
  margin-bottom: 20px;
}

.content-label,
.answer-label,
.feedback-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 10px;
}

.content-label .el-icon,
.answer-label .el-icon,
.feedback-label .el-icon {
  font-size: 16px;
}

.content-text,
.answer-text {
  padding: 16px;
  border-radius: 12px;
  line-height: 1.8;
  font-size: 14px;
}

.content-text {
  background: #f8f9fc;
  border: 1px solid #e8ecf4;
  color: #303133;
}

.answer-box.student-answer .answer-text {
  background: #fff8f0;
  border: 1px solid #ffe4cc;
  color: #5c3d00;
}

.answer-box.student-answer.answer-full-score .answer-text {
  background: #f0f9eb;
  border: 1px solid #c2e7b0;
  color: #2e5c1d;
}

.answer-box.student-answer.answer-zero-score .answer-text {
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  color: #c41e3a;
}

.answer-box.student-answer.answer-partial-score .answer-text {
  background: #fff8f0;
  border: 1px solid #ffe4cc;
  color: #5c3d00;
}

.answer-box.correct-answer .answer-text {
  background: #f0f9eb;
  border: 1px solid #c2e7b0;
  color: #2e5c1d;
}

.answer-text :deep(.no-answer) {
  color: #c0c4cc;
  font-style: italic;
}

/* 反馈输入框 */
.feedback-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 14px;
  resize: none;
  border-color: #e0e0e0;
  transition: all 0.3s ease;
}

.feedback-input :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 弹窗底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  border-radius: 10px;
  padding: 10px 24px;
  font-weight: 500;
}

/* ========== 响应式适配 ========== */
@media (max-width: 992px) {
  .stat-card {
    margin-bottom: 12px;
  }
}

@media (max-width: 768px) {
  .teacher-submissions {
    padding: 16px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-filters {
    width: 100%;
  }
  
  .search-input {
    width: 100%;
  }
  
  .question-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .submission-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .student-card {
    flex-direction: column;
  }
  
  .questions-container {
    padding: 16px;
  }
  
  .question-item {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 18px;
  }
  
  .sub-title {
    font-size: 12px;
  }
  
  .stat-value {
    font-size: 15px;
  }
}
</style>
