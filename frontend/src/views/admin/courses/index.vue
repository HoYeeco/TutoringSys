<template>
  <div class="admin-courses">
    <!-- 课程列表模块 -->
    <el-card shadow="never" class="courses-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><School /></el-icon>
            </div>
            <span>课程管理</span>
          </div>
          <div class="header-actions">
            <el-input
              v-model="filterForm.name"
              placeholder="搜索课程名称"
              clearable
              @keyup.enter="handleSearch"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="filterForm.teacherId" placeholder="选择教师" class="filter-item" clearable>
              <el-option
                v-for="teacher in teachers"
                :key="teacher.id"
                :label="teacher.realName"
                :value="teacher.id"
              />
            </el-select>
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon> 新增课程
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="courses.data"
        class="modern-table"
        :cell-style="{ textAlign: 'center' }"
        :header-cell-style="{ textAlign: 'center' }"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="name" label="课程名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="负责教师" min-width="150">
          <template #default="scope">
            <div class="teacher-info">
              <el-avatar :size="24" :src="getTeacherAvatar(scope.row.teacherId)">
                {{ scope.row.teacherName?.charAt(0) || '?' }}
              </el-avatar>
              <span>{{ scope.row.teacherName || getTeacherName(scope.row.teacherId) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="课程描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="studentCount" label="学生人数" width="120" sortable />
        <el-table-column prop="assignmentCount" label="作业数量" width="120" sortable />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button size="small" type="success" @click="handlePublishAssignment(scope.row)">
              发布作业
            </el-button>
            <el-button size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="courses.page"
          v-model:page-size="courses.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="courses.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑课程对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'create' ? '新增课程' : '编辑课程'"
      width="700px"
      append-to-body
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入课程描述" />
        </el-form-item>
        <el-form-item label="负责教师" prop="teacherId">
          <el-select v-model="form.teacherId" placeholder="选择教师">
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
        </el-form-item>
        <el-form-item label="选课学生">
          <el-select
            v-model="form.studentIds"
            multiple
            filterable
            placeholder="选择学生"
            style="width: 100%"
          >
            <el-option
              v-for="student in students"
              :key="student.id"
              :label="`${student.username} - ${student.realName}`"
              :value="student.id"
            >
              <div class="student-option">
                <el-avatar :size="24" :src="student.avatar || ''">{{ student.realName.charAt(0) }}</el-avatar>
                <span>{{ student.realName }} ({{ student.username }})</span>
              </div>
            </el-option>
          </el-select>
          <div class="form-hint">若不选则学生人数为0</div>
        </el-form-item>
        <el-form-item label="作业数量">
          <el-input v-model="form.assignmentCount" disabled placeholder="作业数量" />
          <div class="form-hint">默认为0，只能通过教师端或管理员端发布作业添加</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, School, Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

const router = useRouter();

// 筛选表单
const filterForm = reactive({
  name: '',
  teacherId: undefined,
  description: ''
});

// 排序参数
const sortField = ref('');
const sortOrder = ref('');

// 课程列表数据
const courses = reactive({
  data: [],
  page: 1,
  pageSize: 10,
  total: 0
});

// 教师列表
const teachers = ref([]);

// 学生列表
const students = ref([]);

// 对话框状态
const dialogVisible = ref(false);
const dialogType = ref('create');
const formRef = ref();

// 表单数据
const form = reactive({
  id: undefined,
  name: '',
  description: '',
  teacherId: undefined,
  studentIds: [],
  assignmentCount: 0
});

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入课程描述', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请选择负责教师', trigger: 'change' }]
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
    ElMessage.error('获取教师列表失败');
  }
};

// 获取学生列表
const getStudents = async () => {
  try {
    const response = await request.get('/admin/users', {
      params: {
        role: 'STUDENT',
        page: 1,
        size: 100
      }
    });
    students.value = response.data.records;
  } catch (error) {
    ElMessage.error('获取学生列表失败');
  }
};

// 获取课程列表
const getCourses = async () => {
  try {
    const params: any = {
      page: courses.page,
      size: courses.pageSize
    };
    
    if (filterForm.name) {
      params.keyword = filterForm.name;
    }
    if (filterForm.teacherId) {
      params.teacherId = filterForm.teacherId;
    }
    
    const response = await request.get('/admin/courses', { params });
    courses.data = response.data.records;
    courses.total = response.data.total;
  } catch (error) {
    console.error('获取课程列表失败:', error);
    ElMessage.error('获取课程列表失败');
    courses.data = [];
    courses.total = 0;
  }
};

const getTeacherAvatar = (teacherId: number) => {
  const teacher = teachers.value.find((t: any) => t.id === teacherId);
  return teacher?.avatar || '';
};

const getTeacherName = (teacherId: number) => {
  const teacher = teachers.value.find((t: any) => t.id === teacherId);
  return teacher?.realName || '未知教师';
};

// 搜索
const handleSearch = () => {
  courses.page = 1;
  getCourses();
};

// 重置筛选
const resetFilter = () => {
  filterForm.name = '';
  filterForm.teacherId = undefined;
  filterForm.description = '';
  sortField.value = '';
  sortOrder.value = '';
  courses.page = 1;
  getCourses();
};

// 排序处理
const handleSortChange = (sort: any) => {
  sortField.value = sort.prop;
  sortOrder.value = sort.order === 'ascending' ? 'asc' : 'desc';
  getCourses();
};

// 分页处理
const handleSizeChange = (size: number) => {
  courses.pageSize = size;
  getCourses();
};

const handleCurrentChange = (current: number) => {
  courses.page = current;
  getCourses();
};

// 打开创建对话框
const handleCreate = () => {
  dialogType.value = 'create';
  form.id = undefined;
  form.name = '';
  form.description = '';
  form.teacherId = undefined;
  form.studentIds = [];
  form.assignmentCount = 0;
  getStudents();
  dialogVisible.value = true;
};

// 打开编辑对话框
const handleEdit = (row: any) => {
  dialogType.value = 'edit';
  form.id = row.id;
  form.name = row.name;
  form.description = row.description;
  form.teacherId = row.teacherId;
  form.studentIds = []; // 实际项目中应该从后端获取
  form.assignmentCount = row.assignmentCount;
  getStudents();
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    if (dialogType.value === 'create') {
      await request.post('/admin/courses', form);
      ElMessage.success('课程创建成功');
    } else {
      await request.put(`/admin/courses/${form.id}`, form);
      ElMessage.success('课程更新成功');
    }
    
    dialogVisible.value = false;
    getCourses();
  } catch (error) {
    // 验证失败或请求失败
  }
};

// 删除课程
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除这门课程吗？删除后所有学生将被移出课程，但已发布过的作业不会被删除，全部变成只读模式。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/admin/courses/${id}`);
      ElMessage.success('课程删除成功');
      getCourses();
    } catch (error) {
      ElMessage.error('课程删除失败');
    }
  }).catch(() => {
    // 取消删除
  });
};

// 发布作业
const handlePublishAssignment = (row: any) => {
  router.push({
    path: '/admin/assignments/create',
    query: { courseId: row.id }
  });
};

// 初始化
onMounted(() => {
  getTeachers();
  getCourses();
});
</script>

<style scoped>
.admin-courses {
  padding: 24px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(4px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
}

/* 通用卡片样式 */
:deep(.el-card) {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: none;
  background: transparent;
}

:deep(.el-card__body) {
  padding: 20px;
}

/* 卡片头部 */
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
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.card-header__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.card-header__icon .el-icon {
  font-size: 24px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.filter-item {
  width: 150px;
}

/* 表格样式 */
.modern-table {
  --el-table-border-radius: 12px;
}

.modern-table :deep(.el-table__header th) {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  color: #606266;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 8px;
}

.modern-table :deep(.el-table__row) {
  height: 60px;
}

.modern-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.modern-table :deep(.el-table__cell) {
  padding: 8px;
  font-size: 13px;
}

.teacher-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.teacher-info .el-avatar {
  flex-shrink: 0;
}

/* 分页 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 对话框 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.teacher-option,
.student-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 响应式设计 */
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
  
  .admin-courses {
    padding: 16px;
  }
}
</style>