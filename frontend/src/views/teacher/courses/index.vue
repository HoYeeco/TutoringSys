<template>
  <div class="teacher-courses">
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
              v-model="keyword"
              placeholder="搜索课程名称"
              clearable
              @keyup.enter="handleSearch"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
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
      >
        <el-table-column prop="name" label="课程名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="description" label="课程描述" min-width="250" show-overflow-tooltip />
        <el-table-column prop="studentCount" label="学生人数" width="120" />
        <el-table-column prop="assignmentCount" label="作业数量" width="120" />
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
          justify="center"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

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
          <div class="form-hint">默认为0，只能通过发布作业添加</div>
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
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, School, Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

const router = useRouter();

const keyword = ref('');

const courses = reactive({
  data: [],
  page: 1,
  pageSize: 10,
  total: 0
});

const students = ref([]);

const dialogVisible = ref(false);
const dialogType = ref<'create' | 'edit'>('create');
const formRef = ref();

const form = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  studentIds: [] as number[],
  assignmentCount: 0
});

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入课程描述', trigger: 'blur' }]
};

const getStudents = async () => {
  try {
    const response = await request.get('/teacher/courses/students');
    students.value = response.data || [];
  } catch (error) {
    console.error('获取学生列表失败:', error);
    students.value = [];
  }
};

const getCourses = async () => {
  try {
    const params: any = {
      page: courses.page,
      size: courses.pageSize
    };
    
    if (keyword.value) {
      params.keyword = keyword.value;
    }
    
    const response = await request.get('/teacher/courses/list', { params });
    courses.data = response.data?.records || [];
    courses.total = response.data?.total || 0;
  } catch (error) {
    console.error('获取课程列表失败:', error);
    ElMessage.error('获取课程列表失败');
    courses.data = [];
    courses.total = 0;
  }
};

const handleSearch = () => {
  courses.page = 1;
  getCourses();
};

const handleSizeChange = (size: number) => {
  courses.pageSize = size;
  getCourses();
};

const handleCurrentChange = (current: number) => {
  courses.page = current;
  getCourses();
};

const handleCreate = async () => {
  dialogType.value = 'create';
  form.id = undefined;
  form.name = '';
  form.description = '';
  form.studentIds = [];
  form.assignmentCount = 0;
  await getStudents();
  dialogVisible.value = true;
};

const handleEdit = async (row: any) => {
  dialogType.value = 'edit';
  form.id = row.id;
  form.name = row.name;
  form.description = row.description;
  form.assignmentCount = row.assignmentCount || 0;
  form.studentIds = [];
  
  await getStudents();
  
  try {
    const response = await request.get(`/teacher/courses/${row.id}`);
    const detail = response.data;
    if (detail && detail.students) {
      form.studentIds = detail.students.map((s: any) => s.id);
    }
  } catch (error) {
    console.error('获取课程详情失败:', error);
  }
  
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    const submitData = {
      name: form.name,
      description: form.description,
      studentIds: form.studentIds
    };
    
    if (dialogType.value === 'create') {
      await request.post('/teacher/courses', submitData);
      ElMessage.success('课程创建成功');
    } else {
      await request.put('/teacher/courses', {
        id: form.id,
        ...submitData
      });
      ElMessage.success('课程更新成功');
    }
    
    dialogVisible.value = false;
    getCourses();
  } catch (error) {
    console.error('提交失败:', error);
  }
};

const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定要删除这门课程吗？删除后所有学生将被移出课程。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/teacher/courses/${id}`);
      ElMessage.success('课程删除成功');
      getCourses();
    } catch (error) {
      ElMessage.error('课程删除失败');
    }
  }).catch(() => {});
};

const handlePublishAssignment = (row: any) => {
  router.push({
    path: '/teacher/assignments/create',
    query: { courseId: row.id }
  });
};

onMounted(() => {
  getCourses();
});
</script>

<style scoped>
.teacher-courses {
  padding: 24px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(4px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
}

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
}

.search-input {
  width: 200px;
}

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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

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

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    flex-direction: column;
  }
  
  .search-input {
    width: 100%;
  }
  
  .teacher-courses {
    padding: 16px;
  }
}
</style>
