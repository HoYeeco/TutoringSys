<template>
  <div class="student-course-detail">
    <el-card shadow="never" class="course-header-card">
      <template #header>
        <div class="card-title">
          <div class="card-title__left">
            <div class="card-title__icon">
              <el-icon><Reading /></el-icon>
            </div>
            <span class="card-title__text">{{ course.name || '课程详情' }}</span>
          </div>
          <el-button class="back-btn" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
        </div>
      </template>
      <div class="course-info">
        <div class="info-row">
          <span class="info-label">课程描述：</span>
          <span class="info-value">{{ course.description || '暂无描述' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">授课教师：</span>
          <span class="info-value">
            <el-avatar :size="24" :src="course.teacherAvatar">
              {{ course.teacherName?.charAt(0) || '?' }}
            </el-avatar>
            <span class="teacher-name-text">{{ course.teacherName || '未知' }}</span>
          </span>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="assignments-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__left">
            <div class="card-title__icon">
              <el-icon><Document /></el-icon>
            </div>
            <span class="card-title__text">课程作业</span>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="modern-tabs">
        <el-tab-pane name="pending">
          <template #label>
            <span class="tab-label">
              <el-icon><Clock /></el-icon>
              待提交
              <el-badge v-if="pendingAssignments.length > 0" :value="pendingAssignments.length" type="warning" />
            </span>
          </template>
          <el-empty v-if="pendingAssignments.length === 0" description="暂无待提交作业" />
          <div v-else class="assignment-list">
            <div
              v-for="assignment in pendingAssignments"
              :key="assignment.id"
              class="assignment-card pending"
              @click="handleAssignmentSubmit(assignment.id)"
            >
              <div class="assignment-card__header">
                <div class="assignment-icon pending">
                  <el-icon><Edit /></el-icon>
                </div>
                <div class="assignment-info">
                  <h4 class="assignment-title">{{ assignment.title }}</h4>
                  <div class="assignment-meta">
                    <span class="meta-item">
                      <el-icon><Timer /></el-icon>
                      截止：{{ formatDate(assignment.deadline) }}
                    </span>
                    <span class="meta-item">
                      <el-icon><TrendCharts /></el-icon>
                      总分：{{ assignment.totalScore }}
                    </span>
                  </div>
                </div>
                <el-tag type="warning" effect="light" class="status-tag">待提交</el-tag>
              </div>
              <div class="assignment-card__action">
                <el-button type="primary" size="small" @click.stop="handleAssignmentSubmit(assignment.id)">
                  去提交
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="graded">
          <template #label>
            <span class="tab-label">
              <el-icon><CircleCheck /></el-icon>
              已批改
              <el-badge v-if="gradedAssignments.length > 0" :value="gradedAssignments.length" type="success" />
            </span>
          </template>
          <el-empty v-if="gradedAssignments.length === 0" description="暂无已批改作业" />
          <div v-else class="assignment-list">
            <div
              v-for="assignment in gradedAssignments"
              :key="assignment.id"
              class="assignment-card graded"
              @click="handleAssignmentReport(assignment.id)"
            >
              <div class="assignment-card__header">
                <div class="assignment-icon graded">
                  <el-icon><Select /></el-icon>
                </div>
                <div class="assignment-info">
                  <h4 class="assignment-title">{{ assignment.title }}</h4>
                  <div class="assignment-meta">
                    <span class="meta-item score">
                      <el-icon><TrendCharts /></el-icon>
                      得分：{{ assignment.score }} / {{ assignment.totalScore }}
                    </span>
                    <span class="meta-item">
                      <el-icon><Calendar /></el-icon>
                      批改：{{ formatDate(assignment.gradeTime) }}
                    </span>
                  </div>
                </div>
                <el-tag type="success" effect="light" class="status-tag">已批改</el-tag>
              </div>
              <div class="assignment-card__action">
                <el-button type="success" size="small" @click.stop="handleAssignmentReport(assignment.id)">
                  查看报告
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { 
  Reading, Calendar, Document, Clock, Edit, Timer, 
  TrendCharts, CircleCheck, Select, ArrowLeft 
} from '@element-plus/icons-vue';
import request from '@/utils/request';

const route = useRoute();
const router = useRouter();
const courseId = computed(() => route.params.id as string);

const course = ref({
  id: 0,
  name: '',
  description: '',
  teacherName: '',
  teacherAvatar: '',
  joinTime: '',
  assignments: [],
});

const activeTab = ref('pending');
const pendingAssignments = ref([]);
const gradedAssignments = ref([]);

const getCourseDetail = async () => {
  try {
    const response = await request.get(`/student/courses/${courseId.value}`);
    course.value = response.data;
    
    const assignments = response.data.assignments || [];
    pendingAssignments.value = assignments.filter(
      (item: any) => item.status === 'pending',
    );
    gradedAssignments.value = assignments.filter(
      (item: any) => item.status === 'graded',
    );
  } catch (error) {
    ElMessage.error('获取课程详情失败');
  }
};

const handleAssignmentSubmit = (assignmentId: number) => {
  router.push(`/student/assignments/${assignmentId}/submit`);
};

const handleAssignmentReport = (assignmentId: number) => {
  router.push(`/student/assignments/report/${assignmentId}`);
};

const goBack = () => {
  router.push('/student/courses');
};

const formatDate = (date: string) => {
  if (!date) return '未知';
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  getCourseDetail();
});
</script>

<style scoped>
.student-course-detail {
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
  overflow: hidden;
}

:deep(.el-card__body) {
  padding: 0 20px 20px;
  overflow: hidden;
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

.back-btn {
  padding: 6px 12px;
  font-size: 13px;
  border-radius: 8px;
}

.back-btn .el-icon {
  margin-right: 4px;
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

.card-title__text {
  color: #303133;
}

.course-header-card {
  margin-bottom: 0;
}

.course-info {
  padding: 10px 0;
}

.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  font-size: 14px;
  line-height: 1.6;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  font-weight: 600;
  color: #606266;
  min-width: 100px;
  flex-shrink: 0;
}

.info-value {
  color: #303133;
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.teacher-name-text {
  font-size: 14px;
  color: #303133;
}

.assignments-section {
  margin-bottom: 0;
}

.modern-tabs :deep(.el-tabs__header) {
  margin: 0 0 20px 0;
}

.modern-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.modern-tabs :deep(.el-tabs__item) {
  padding: 0 20px;
  height: 44px;
  line-height: 44px;
  font-size: 14px;
  color: #606266;
}

.modern-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}

.modern-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: 2px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-label .el-icon {
  font-size: 16px;
}

.tab-label .el-badge {
  margin-left: 4px;
}

.assignment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.assignment-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.assignment-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  border-color: transparent;
}

.assignment-card.pending:hover {
  border-left-color: #e6a23c;
}

.assignment-card.graded:hover {
  border-left-color: #67c23a;
}

.assignment-card__header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.assignment-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  flex-shrink: 0;
}

.assignment-icon .el-icon {
  font-size: 22px;
}

.assignment-icon.pending {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
  color: #e6a23c;
}

.assignment-icon.graded {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  color: #67c23a;
}

.assignment-info {
  flex: 1;
  min-width: 0;
}

.assignment-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.assignment-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.meta-item .el-icon {
  font-size: 14px;
}

.meta-item.score {
  color: #67c23a;
  font-weight: 500;
}

.status-tag {
  flex-shrink: 0;
}

.assignment-card__action {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-empty) {
  padding: 40px 0;
}

@media (max-width: 768px) {
  .student-course-detail {
    padding: 16px;
    margin: 12px;
  }

  .card-title {
    font-size: 14px;
  }

  .info-row {
    flex-direction: column;
  }

  .info-label {
    margin-bottom: 4px;
  }

  .assignment-card__header {
    flex-direction: column;
  }

  .assignment-meta {
    flex-direction: column;
    gap: 8px;
  }

  .status-tag {
    align-self: flex-start;
  }
}
</style>
