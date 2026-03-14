<template>
  <div class="admin-assignments">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>作业管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索作业名称、所属课程、负责教师或补充说明"
              clearable
              @keyup.enter="handleSearch"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              <template #suffix>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
            <el-select v-model="filterForm.courseId" placeholder="按课程筛选" class="filter-item">
              <el-option label="全部" value="" />
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
            <el-select v-model="filterForm.teacherId" placeholder="按负责教师筛选" class="filter-item">
              <el-option label="全部" value="" />
              <el-option
                v-for="teacher in teachers"
                :key="teacher.id"
                :label="teacher.realName"
                :value="teacher.id"
              >
                <div class="teacher-option">
                  <el-avatar :size="24" :src="teacher.avatar || ''">{{ teacher.realName.charAt(0) }}</el-avatar>
                  <span>{{ teacher.realName }}</span>
                </div>
              </el-option>
            </el-select>
            <el-select v-model="filterForm.status" placeholder="按状态筛选" class="filter-item">
              <el-option label="全部" value="" />
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="published" />
            </el-select>
            <el-button type="primary" @click="createAssignment">
              <el-icon><Plus /></el-icon> 发布作业
            </el-button>
          </div>
        </div>
      </template>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>作业列表</span>
          <div class="sort-actions">
            <span>排序：</span>
            <el-select v-model="sortBy" @change="handleSort">
              <el-option label="截止时间" value="deadline" />
              <el-option label="完成情况" value="completion" />
            </el-select>
            <el-select v-model="sortOrder" @change="handleSort">
              <el-option label="正序" value="asc" />
              <el-option label="倒序" value="desc" />
            </el-select>
          </div>
        </div>
      </template>
      <el-table
        :data="filteredAssignments"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="assignmentName" label="作业名称" min-width="200" />
        <el-table-column prop="courseName" label="所属课程" width="150" />
        <el-table-column prop="teacherName" label="负责教师" width="150">
          <template #default="scope">
            <div class="teacher-info">
              <el-avatar :size="20" :src="scope.row.teacherAvatar || ''">{{ scope.row.teacherName.charAt(0) }}</el-avatar>
              <span>{{ scope.row.teacherName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalScore" label="总分值" width="100" />
        <el-table-column prop="description" label="补充说明" min-width="200">
          <template #default="scope">
            <span v-if="scope.row.description.length > 50">{{ scope.row.description.substring(0, 50) }}...</span>
            <span v-else>{{ scope.row.description }}</span>
            <el-button v-if="scope.row.description.length > 50" size="small" @click="viewFullDescription(scope.row)">
              查看完整
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column prop="completion" label="完成情况" width="150">
          <template #default="scope">
            <div class="completion-info">
              <span>{{ scope.row.submittedCount }}/{{ scope.row.totalStudents }}</span>
              <el-progress
                :percentage="scope.row.completionRate"
                :stroke-width="8"
                :show-text="false"
                class="completion-progress"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === 'published' ? 'success' : 'info'"
            >
              {{ scope.row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="editAssignment(scope.row.assignmentId)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteAssignment(scope.row)">
              删除
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

    <!-- 完整说明对话框 -->
    <el-dialog
      v-model="descriptionDialogVisible"
      title="补充说明"
      width="600px"
    >
      <p>{{ currentAssignment?.description }}</p>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

interface Course {
  id: number;
  name: string;
}

interface Teacher {
  id: number;
  realName: string;
  avatar: string;
}

interface Assignment {
  assignmentId: number;
  assignmentName: string;
  courseId: number;
  courseName: string;
  teacherId: number;
  teacherName: string;
  teacherAvatar: string;
  totalScore: number;
  description: string;
  deadline: string;
  submittedCount: number;
  totalStudents: number;
  completionRate: number;
  status: string;
}

interface FilterForm {
  courseId: string;
  teacherId: string;
  status: string;
}

const router = useRouter();

const courses = ref<Course[]>([]);
const teachers = ref<Teacher[]>([]);
const assignments = ref<Assignment[]>([]);
const searchKeyword = ref('');
const filterForm = ref<FilterForm>({
  courseId: '',
  teacherId: '',
  status: ''
});
const sortBy = ref('deadline');
const sortOrder = ref('desc');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const selectedAssignments = ref<number[]>([]);
const descriptionDialogVisible = ref(false);
const currentAssignment = ref<Assignment | null>(null);

// 过滤后的作业列表
const filteredAssignments = computed(() => {
  let result = [...assignments.value];
  
  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(assignment => {
      return (
        assignment.assignmentName.toLowerCase().includes(keyword) ||
        assignment.courseName.toLowerCase().includes(keyword) ||
        assignment.teacherName.toLowerCase().includes(keyword) ||
        assignment.description.toLowerCase().includes(keyword)
      );
    });
  }
  
  // 课程筛选
  if (filterForm.value.courseId) {
    result = result.filter(assignment => assignment.courseId === parseInt(filterForm.value.courseId));
  }
  
  // 教师筛选
  if (filterForm.value.teacherId) {
    result = result.filter(assignment => assignment.teacherId === parseInt(filterForm.value.teacherId));
  }
  
  // 状态筛选
  if (filterForm.value.status) {
    result = result.filter(assignment => assignment.status === filterForm.value.status);
  }
  
  // 排序
  result.sort((a, b) => {
    if (sortBy.value === 'deadline') {
      const dateA = new Date(a.deadline).getTime();
      const dateB = new Date(b.deadline).getTime();
      return sortOrder.value === 'asc' ? dateA - dateB : dateB - dateA;
    } else if (sortBy.value === 'completion') {
      return sortOrder.value === 'asc' ? a.completionRate - b.completionRate : b.completionRate - a.completionRate;
    }
    return 0;
  });
  
  return result;
});

// 获取课程列表
const getCourses = async () => {
  try {
    const response = await request.get('/admin/courses', {
      params: {
        page: 1,
        size: 100
      }
    });
    courses.value = response.data.records;
  } catch (error) {
    // 模拟数据
    courses.value = [
      { id: 1, name: '计算机导论' },
      { id: 2, name: '数据结构' },
      { id: 3, name: '算法设计与分析' }
    ];
  }
};

// 获取教师列表
const getTeachers = async () => {
  try {
    const response = await request.get('/admin/users', {
      params: {
        role: 'TEACHER',
        page: 1,
        size: 100
      }
    });
    teachers.value = response.data.records;
  } catch (error) {
    // 模拟数据
    teachers.value = [
      { id: 1, realName: '张老师', avatar: '' },
      { id: 2, realName: '李老师', avatar: '' },
      { id: 3, realName: '王老师', avatar: '' }
    ];
  }
};

// 获取作业列表
const getAssignments = async () => {
  try {
    const response = await request.get('/admin/assignments', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value
      }
    });
    assignments.value = response.data.records;
    total.value = response.data.total;
  } catch (error) {
    // 模拟数据
    assignments.value = [
      {
        assignmentId: 1,
        assignmentName: '数据结构作业1',
        courseId: 2,
        courseName: '数据结构',
        teacherId: 1,
        teacherName: '张老师',
        teacherAvatar: '',
        totalScore: 100,
        description: '本次作业主要考察栈和队列的基本概念和应用，包括栈的实现、队列的实现以及它们在实际问题中的应用。',
        deadline: '2024-01-20 23:59:59',
        submittedCount: 25,
        totalStudents: 30,
        completionRate: 83,
        status: 'published'
      },
      {
        assignmentId: 2,
        assignmentName: '算法作业1',
        courseId: 3,
        courseName: '算法设计与分析',
        teacherId: 2,
        teacherName: '李老师',
        teacherAvatar: '',
        totalScore: 120,
        description: '本次作业主要考察排序算法的实现和分析，包括冒泡排序、选择排序、插入排序、归并排序和快速排序。',
        deadline: '2024-01-25 23:59:59',
        submittedCount: 18,
        totalStudents: 25,
        completionRate: 72,
        status: 'published'
      },
      {
        assignmentId: 3,
        assignmentName: '计算机导论作业1',
        courseId: 1,
        courseName: '计算机导论',
        teacherId: 3,
        teacherName: '王老师',
        teacherAvatar: '',
        totalScore: 80,
        description: '本次作业主要考察计算机的基本组成、工作原理以及计算机网络的基本概念。',
        deadline: '2024-01-15 23:59:59',
        submittedCount: 28,
        totalStudents: 35,
        completionRate: 80,
        status: 'published'
      },
      {
        assignmentId: 4,
        assignmentName: '数据结构作业2',
        courseId: 2,
        courseName: '数据结构',
        teacherId: 1,
        teacherName: '张老师',
        teacherAvatar: '',
        totalScore: 100,
        description: '本次作业主要考察树和图的基本概念和应用，包括二叉树的遍历、图的表示和遍历算法。',
        deadline: '2024-02-01 23:59:59',
        submittedCount: 0,
        totalStudents: 30,
        completionRate: 0,
        status: 'draft'
      }
    ];
    total.value = assignments.value.length;
  }
};

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
};

// 处理排序
const handleSort = () => {
  // 排序逻辑已在computed中实现
};

// 处理分页
const handleSizeChange = (size: number) => {
  pageSize.value = size;
  currentPage.value = 1;
  getAssignments();
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  getAssignments();
};

// 处理选择变化
const handleSelectionChange = (val: Assignment[]) => {
  selectedAssignments.value = val.map(item => item.assignmentId);
};

// 创建作业
const createAssignment = () => {
  router.push('/admin/assignments/create');
};

// 编辑作业
const editAssignment = (assignmentId: number) => {
  router.push(`/admin/assignments/${assignmentId}/edit`);
};

// 删除作业
const deleteAssignment = (assignment: Assignment) => {
  ElMessageBox.confirm(
    `确定要删除作业「${assignment.assignmentName}」吗？${assignment.status === 'published' ? '删除后将通知所有选课学生和对应教师。' : ''}`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    try {
      // 模拟删除，实际项目中应该调用后端API
      assignments.value = assignments.value.filter(a => a.assignmentId !== assignment.assignmentId);
      total.value = assignments.value.length;
      ElMessage.success('作业删除成功');
      // 实际项目中应该发送通知给学生和教师
    } catch (error) {
      ElMessage.error('删除作业失败');
    }
  });
};

// 查看完整说明
const viewFullDescription = (assignment: Assignment) => {
  currentAssignment.value = assignment;
  descriptionDialogVisible.value = true;
};

// 初始化
onMounted(() => {
  getCourses();
  getTeachers();
  getAssignments();
});
</script>

<style scoped>
.admin-assignments {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  min-width: 300px;
}

.filter-item {
  min-width: 150px;
}

.sort-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.completion-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.completion-progress {
  flex: 1;
}

.teacher-option,
.teacher-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 1200px) {
  .header-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input,
  .filter-item {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .sort-actions {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
  }
  
  .pagination-container {
    justify-content: center;
  }
}
</style>