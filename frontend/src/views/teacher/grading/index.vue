<template>
  <div class="teacher-grading">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>批改复核</span>
        </div>
      </template>
    </el-card>

    <el-card shadow="never" class="filter-card">
      <div class="filter-container">
        <el-select v-model="filterForm.courseId" placeholder="按课程筛选" class="filter-item">
          <el-option
            v-for="course in courses"
            :key="course.courseId"
            :label="course.courseName"
            :value="course.courseId"
          />
        </el-select>
        <el-select v-model="filterForm.aiStatus" placeholder="按智能批改状态筛选" class="filter-item">
          <el-option label="全部" value="" />
          <el-option label="已完成" value="completed" />
          <el-option label="异常" value="error" />
        </el-select>
        <el-input
          v-model="filterForm.search"
          placeholder="按作业名、课程、学生姓名搜索"
          class="filter-item"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #suffix>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>待复核列表</span>
          <el-button type="primary" @click="handleBatchReview">批量复核</el-button>
        </div>
      </template>
      <el-table
        :data="submissions"
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="assignmentName" label="作业名称" min-width="180" />
        <el-table-column prop="courseName" label="所属课程" min-width="150" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="submitTime" label="提交时间" width="180" sortable />
        <el-table-column prop="aiStatus" label="智能批改状态" width="120" sortable>
          <template #default="scope">
            <el-tag
              :type="scope.row.aiStatus === 'completed' ? 'success' : 'danger'"
            >
              {{ scope.row.aiStatus === 'completed' ? '已完成' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="viewReviewDetail(scope.row.submissionId)"
            >
              复核
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const router = useRouter();

const filterForm = ref({
  courseId: '',
  aiStatus: '',
  search: ''
});

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

const courses = ref([
  { courseId: 1, courseName: '计算机导论' },
  { courseId: 2, courseName: '数据结构' },
  { courseId: 3, courseName: '算法设计与分析' }
]);

const submissions = ref([
  {
    submissionId: 1,
    assignmentName: '数据结构作业1',
    courseName: '数据结构',
    studentName: '张三',
    submitTime: '2024-01-15 14:30:00',
    aiStatus: 'completed'
  },
  {
    submissionId: 2,
    assignmentName: '算法作业2',
    courseName: '算法设计与分析',
    studentName: '李四',
    submitTime: '2024-01-15 15:45:00',
    aiStatus: 'completed'
  },
  {
    submissionId: 3,
    assignmentName: '计算机导论作业3',
    courseName: '计算机导论',
    studentName: '王五',
    submitTime: '2024-01-15 16:20:00',
    aiStatus: 'error'
  },
  {
    submissionId: 4,
    assignmentName: '数据结构作业2',
    courseName: '数据结构',
    studentName: '赵六',
    submitTime: '2024-01-15 17:10:00',
    aiStatus: 'completed'
  },
  {
    submissionId: 5,
    assignmentName: '算法作业1',
    courseName: '算法设计与分析',
    studentName: '钱七',
    submitTime: '2024-01-15 18:00:00',
    aiStatus: 'completed'
  }
]);

total.value = submissions.value.length;

const getSubmissions = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get('/teacher/grading/submissions', {
    //   params: {
    //     page: currentPage.value,
    //     pageSize: pageSize.value,
    //     courseId: filterForm.value.courseId,
    //     aiStatus: filterForm.value.aiStatus,
    //     search: filterForm.value.search
    //   }
    // });
    // return response.data;
    
    // 模拟数据
    return {
      list: submissions.value,
      total: submissions.value.length
    };
  } catch (error) {
    ElMessage.error('获取待复核列表失败');
    return null;
  }
};

const { execute: fetchSubmissions } = useRequest(getSubmissions);

const handleSearch = () => {
  currentPage.value = 1;
  fetchSubmissions();
};

const handleSortChange = (sort: any) => {
  // 处理排序逻辑
  console.log('排序', sort);
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchSubmissions();
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  fetchSubmissions();
};

const viewReviewDetail = (submissionId: number) => {
  router.push(`/teacher/grading/detail/${submissionId}`);
};

const handleBatchReview = () => {
  router.push('/teacher/grading/batch');
};

// 初始加载数据
fetchSubmissions();
</script>

<style scoped>
.teacher-grading {
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

.filter-card {
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-item {
  min-width: 200px;
  flex: 1;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .filter-container {
    flex-direction: column;
  }
  
  .filter-item {
    width: 100%;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .pagination-container {
    justify-content: center;
  }
}
</style>