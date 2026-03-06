<template>
  <div class="student-assignments">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>作业列表</span>
        </div>
      </template>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="待提交" name="pending">
          <el-empty v-if="pendingAssignments.length === 0" description="暂无待提交作业" />
          <RecycleScroller
            v-else
            class="assignment-list"
            :items="pendingAssignments"
            :item-size="120"
            key-field="id"
          >
            <template #default="{ item }">
              <div class="assignment-card" @click="handleAssignmentClick(item, 'submit')">
                <div class="assignment-header">
                  <span class="assignment-title">{{ item.title }}</span>
                  <el-tag type="warning">待提交</el-tag>
                </div>
                <div class="assignment-content">
                  <p class="course-name">课程：{{ item.courseName }}</p>
                  <p class="deadline">截止时间：{{ formatDate(item.deadline) }}</p>
                </div>
              </div>
            </template>
          </RecycleScroller>
        </el-tab-pane>
        <el-tab-pane label="已批改" name="graded">
          <el-empty v-if="gradedAssignments.length === 0" description="暂无已批改作业" />
          <RecycleScroller
            v-else
            class="assignment-list"
            :items="gradedAssignments"
            :item-size="120"
            key-field="id"
          >
            <template #default="{ item }">
              <div class="assignment-card" @click="handleAssignmentClick(item, 'report')">
                <div class="assignment-header">
                  <span class="assignment-title">{{ item.title }}</span>
                  <el-tag type="success">已批改</el-tag>
                </div>
                <div class="assignment-content">
                  <p class="course-name">课程：{{ item.courseName }}</p>
                  <p class="score">得分：{{ item.score }} / {{ item.totalScore }}</p>
                </div>
              </div>
            </template>
          </RecycleScroller>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const router = useRouter();
const activeTab = ref('pending');
const pendingAssignments = ref([]);
const gradedAssignments = ref([]);

// 模拟数据
const mockAssignments = [
  {
    id: 1,
    title: '第一章 计算机基础作业',
    courseName: '计算机导论',
    deadline: '2026-03-10 23:59:59',
    status: 'pending',
    totalScore: 100
  },
  {
    id: 2,
    title: '第二章 数据结构作业',
    courseName: '数据结构',
    deadline: '2026-03-15 23:59:59',
    status: 'pending',
    totalScore: 100
  },
  {
    id: 3,
    title: '第三章 算法作业',
    courseName: '算法设计与分析',
    deadline: '2026-03-01 23:59:59',
    status: 'graded',
    score: 85,
    totalScore: 100
  }
];

const getAssignments = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get('/student/assignments/list', { params: { status: activeTab.value } });
    // return response.data;
    
    // 模拟数据
    return mockAssignments;
  } catch (error) {
    ElMessage.error('获取作业列表失败');
    return [];
  }
};

const { execute: fetchAssignments } = useRequest(getAssignments);

const handleAssignmentClick = (assignment: any, type: string) => {
  if (type === 'submit') {
    router.push(`/student/assignments/${assignment.id}/submit`);
  } else {
    router.push(`/student/reports/${assignment.id}`);
  }
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchAssignments().then((data: any) => {
    pendingAssignments.value = data.filter((item: any) => item.status === 'pending');
    gradedAssignments.value = data.filter((item: any) => item.status === 'graded');
  });
});
</script>

<style scoped>
.student-assignments {
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

.assignment-card {
  margin-bottom: 16px;
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

.course-name {
  margin-bottom: 5px;
  color: #606266;
}

.deadline {
  color: #f56c6c;
}

.score {
  color: #67c23a;
  font-weight: 500;
}

.assignment-list {
  height: 500px;
  overflow: hidden;
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
</style>