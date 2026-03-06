<template>
  <div class="teacher-courses">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>我的课程</span>
          <el-button type="primary" @click="handleCreateAssignment">
            <el-icon><Plus /></el-icon> 发布作业
          </el-button>
        </div>
      </template>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <el-empty v-if="courses.length === 0" description="暂无课程" />
      <el-table v-else :data="courses" style="width: 100%">
        <el-table-column prop="name" label="课程名称" width="200" />
        <el-table-column prop="code" label="课程代码" width="120" />
        <el-table-column prop="studentCount" label="学生人数" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleCourseDetail(scope.row.id)">
              查看详情
            </el-button>
            <el-button size="small" type="primary" @click="handleCreateAssignment(scope.row.id)">
              发布作业
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const router = useRouter();
const courses = ref([]);

// 模拟数据
const mockCourses = [
  {
    id: 1,
    name: '计算机导论',
    code: 'CS101',
    studentCount: 45,
    createdAt: '2026-02-01 08:00:00'
  },
  {
    id: 2,
    name: '数据结构',
    code: 'CS201',
    studentCount: 38,
    createdAt: '2026-02-01 08:00:00'
  },
  {
    id: 3,
    name: '算法设计与分析',
    code: 'CS301',
    studentCount: 32,
    createdAt: '2026-02-01 08:00:00'
  }
];

const getCourses = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get('/teacher/courses');
    // return response.data;
    
    // 模拟数据
    return mockCourses;
  } catch (error) {
    ElMessage.error('获取课程列表失败');
    return [];
  }
};

const { execute: fetchCourses } = useRequest(getCourses);

const handleCourseDetail = (courseId: number) => {
  router.push(`/teacher/courses/${courseId}`);
};

const handleCreateAssignment = (courseId?: number) => {
  if (courseId) {
    router.push(`/teacher/assignments/create?courseId=${courseId}`);
  } else {
    // 选择课程后创建作业
    ElMessage.info('请先选择课程');
  }
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchCourses().then((data: any) => {
    courses.value = data;
  });
});
</script>

<style scoped>
.teacher-courses {
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
</style>