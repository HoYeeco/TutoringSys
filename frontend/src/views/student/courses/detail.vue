<template>
  <div class="student-course-detail">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>{{ course.name }}</span>
        </div>
      </template>
      <div class="course-info">
        <p>课程描述：{{ course.description }}</p>
        <p>授课教师：{{ course.teacherName }}</p>
        <p>加入时间：{{ formatDate(course.joinTime) }}</p>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>课程作业</span>
        </div>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待提交" name="pending">
          <el-empty v-if="pendingAssignments.length === 0" description="暂无待提交作业" />
          <div v-else class="assignment-list">
            <div 
              v-for="assignment in pendingAssignments" 
              :key="assignment.id" 
              class="assignment-card"
              @click="handleAssignmentSubmit(assignment.id)"
            >
              <div class="assignment-header">
                <span class="assignment-title">{{ assignment.title }}</span>
                <el-tag type="warning">待提交</el-tag>
              </div>
              <div class="assignment-content">
                <p class="deadline">截止时间：{{ formatDate(assignment.deadline) }}</p>
                <p class="total-score">总分：{{ assignment.totalScore }}</p>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="已批改" name="graded">
          <el-empty v-if="gradedAssignments.length === 0" description="暂无已批改作业" />
          <div v-else class="assignment-list">
            <div 
              v-for="assignment in gradedAssignments" 
              :key="assignment.id" 
              class="assignment-card"
              @click="handleAssignmentReport(assignment.id)"
            >
              <div class="assignment-header">
                <span class="assignment-title">{{ assignment.title }}</span>
                <el-tag type="success">已批改</el-tag>
              </div>
              <div class="assignment-content">
                <p class="score">得分：{{ assignment.score }} / {{ assignment.totalScore }}</p>
                <p class="grade-time">批改时间：{{ formatDate(assignment.gradeTime) }}</p>
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
import request from '@/utils/request';

const route = useRoute();
const router = useRouter();
const courseId = computed(() => route.params.id as string);

const course = ref({
  id: 0,
  name: '',
  description: '',
  teacherName: '',
  joinTime: ''
});

const activeTab = ref('pending');
const pendingAssignments = ref([]);
const gradedAssignments = ref([]);

// 获取课程详情
const getCourseDetail = async () => {
  try {
    const response = await request.get(`/student/courses/${courseId.value}`);
    course.value = response.data;
  } catch (error) {
    ElMessage.error('获取课程详情失败');
  }
};

// 获取课程作业列表
const getCourseAssignments = async () => {
  try {
    const response = await request.get(`/student/courses/${courseId.value}/assignments`);
    const assignments = response.data;
    pendingAssignments.value = assignments.filter((item: any) => item.status === 'pending');
    gradedAssignments.value = assignments.filter((item: any) => item.status === 'graded');
  } catch (error) {
    ElMessage.error('获取作业列表失败');
  }
};

const handleAssignmentSubmit = (assignmentId: number) => {
  router.push(`/student/assignments/${assignmentId}/submit`);
};

const handleAssignmentReport = (assignmentId: number) => {
  router.push(`/student/assignments/report/${assignmentId}`);
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  getCourseDetail();
  getCourseAssignments();
});
</script>

<style scoped>
.student-course-detail {
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

.course-info {
  margin-top: 10px;
  color: #606266;
}

.assignment-list {
  margin-top: 10px;
}

.assignment-card {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.assignment-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.assignment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assignment-title {
  font-size: 16px;
  font-weight: 500;
}

.assignment-content {
  margin-top: 10px;
}

.deadline {
  color: #f56c6c;
  margin-bottom: 5px;
}

.total-score {
  color: #606266;
}

.score {
  color: #67c23a;
  font-weight: 500;
  margin-bottom: 5px;
}

.grade-time {
  color: #606266;
}
</style>