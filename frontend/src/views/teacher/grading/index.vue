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
        <el-select v-model="filterForm.courseId" placeholder="按课程筛选" class="filter-item" clearable>
          <el-option
            v-for="course in courses"
            :key="course.courseId"
            :label="course.courseName"
            :value="course.courseId"
          />
        </el-select>
        <el-input
          v-model="filterForm.search"
          placeholder="按作业名、课程、学生姓名搜索"
          class="filter-item"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>待复核列表</span>
          <el-button type="primary" @click="handleBatchAdopt" :disabled="selectedSubmissions.length === 0">
            批量采用AI评分
          </el-button>
        </div>
      </template>
      <el-table
        :data="submissions"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="assignmentTitle" label="作业名称" min-width="180" />
        <el-table-column prop="courseName" label="所属课程" min-width="150" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column prop="aiGradingStatus" label="智能批改状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.aiGradingStatus === '已完成' ? 'success' : 'danger'">
              {{ scope.row.aiGradingStatus }}
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

const router = useRouter();

const filterForm = ref({
  courseId: '',
  search: ''
});

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const selectedSubmissions = ref<any[]>([]);

const courses = ref<any[]>([]);
const submissions = ref<any[]>([]);

const getCourses = async () => {
  try {
    const response = await request.get('/teacher/courses');
    courses.value = (response.data || []).map((c: any) => ({
      courseId: c.id,
      courseName: c.name
    }));
  } catch (error) {
    console.error('获取课程列表失败:', error);
  }
};

const getSubmissions = async () => {
  loading.value = true;
  try {
    const response = await request.get('/teacher/review/pending', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        courseId: filterForm.value.courseId || undefined,
        keyword: filterForm.value.search || undefined
      }
    });
    submissions.value = response.data?.records || [];
    total.value = response.data?.total || 0;
  } catch (error) {
    ElMessage.error('获取待复核列表失败');
    submissions.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  getSubmissions();
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  getSubmissions();
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getSubmissions();
};

const handleSelectionChange = (selection: any[]) => {
  selectedSubmissions.value = selection;
};

const viewReviewDetail = (submissionId: string) => {
  router.push(`/teacher/grading/detail/${submissionId}`);
};

const handleBatchAdopt = async () => {
  if (selectedSubmissions.value.length === 0) {
    ElMessage.warning('请选择要批量复核的提交');
    return;
  }
  
  try {
    const submissionIds = selectedSubmissions.value.map(s => s.submissionId);
    await request.post('/teacher/review/batch-adopt', submissionIds);
    ElMessage.success('批量采用AI评分成功');
    getSubmissions();
  } catch (error) {
    ElMessage.error('批量采用AI评分失败');
  }
};

onMounted(() => {
  getCourses();
  getSubmissions();
});
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
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.mt-4 {
  margin-top: 16px;
}
</style>
