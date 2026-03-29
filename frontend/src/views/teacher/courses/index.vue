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
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入课程描述" 
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="已选课学生">
          <div class="student-section">
            <div class="student-header">
              <span class="student-count">已选择 {{ selectedStudents.length }} 名学生</span>
              <div class="student-actions">
                <el-button 
                  type="danger" 
                  size="small" 
                  :disabled="studentsToRemove.length === 0"
                  @click="handleBatchRemoveStudents"
                >
                  <el-icon><Delete /></el-icon>
                  批量移除 ({{ studentsToRemove.length }})
                </el-button>
                <el-button type="primary" size="small" @click="openAddStudentDialog">
                  <el-icon><Plus /></el-icon>
                  添加学生
                </el-button>
              </div>
            </div>
            
            <div class="student-search" v-if="selectedStudents.length > 0">
              <el-input
                v-model="studentSearchKeyword"
                placeholder="搜索学生姓名或账号"
                clearable
                size="small"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            
            <div class="student-list" v-if="filteredSelectedStudents.length > 0">
              <el-checkbox-group v-model="studentsToRemove">
                <div 
                  v-for="student in filteredSelectedStudents" 
                  :key="student.id" 
                  class="student-item"
                >
                  <el-checkbox :value="student.id">
                    <div class="student-info">
                      <el-avatar :size="32" class="student-avatar">
                        {{ student.realName?.charAt(0) || '?' }}
                      </el-avatar>
                      <div class="student-detail">
                        <span class="student-name">{{ student.realName }}</span>
                        <span class="student-username">({{ student.username }})</span>
                      </div>
                    </div>
                  </el-checkbox>
                </div>
              </el-checkbox-group>
            </div>
            <el-empty v-else description="暂无选课学生" :image-size="80" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ dialogType === 'create' ? '创建' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="addStudentDialogVisible"
      title="添加学生"
      width="600px"
      append-to-body
      destroy-on-close
    >
      <div class="add-student-section">
        <el-input
          v-model="addStudentSearchKeyword"
          placeholder="搜索学生姓名或账号"
          clearable
          class="mb-4"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <div class="available-student-list" v-if="filteredAvailableStudents.length > 0">
          <el-checkbox-group v-model="studentsToAdd">
            <div 
              v-for="student in filteredAvailableStudents" 
              :key="student.id" 
              class="student-item"
            >
              <el-checkbox :value="student.id">
                <div class="student-info">
                  <el-avatar :size="32" class="student-avatar">
                    {{ student.realName?.charAt(0) || '?' }}
                  </el-avatar>
                  <div class="student-detail">
                    <span class="student-name">{{ student.realName }}</span>
                    <span class="student-username">({{ student.username }})</span>
                  </div>
                </div>
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </div>
        <el-empty v-else description="没有可添加的学生" :image-size="80" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addStudentDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="confirmAddStudents" 
            :disabled="studentsToAdd.length === 0"
          >
            添加 ({{ studentsToAdd.length }})
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, School, Search, Delete } from '@element-plus/icons-vue';
import request from '@/utils/request';

interface Student {
  id: number;
  username: string;
  realName: string;
  avatar?: string;
}

const router = useRouter();

const keyword = ref('');

const courses = reactive({
  data: [],
  page: 1,
  pageSize: 10,
  total: 0
});

const allStudents = ref<Student[]>([]);
const dialogVisible = ref(false);
const dialogType = ref<'create' | 'edit'>('create');
const formRef = ref();
const submitting = ref(false);

const form = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  studentIds: [] as number[]
});

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入课程描述', trigger: 'blur' }]
};

const selectedStudents = ref<Student[]>([]);
const studentsToRemove = ref<number[]>([]);
const studentSearchKeyword = ref('');

const addStudentDialogVisible = ref(false);
const studentsToAdd = ref<number[]>([]);
const addStudentSearchKeyword = ref('');

const filteredSelectedStudents = computed(() => {
  if (!studentSearchKeyword.value) {
    return selectedStudents.value;
  }
  const keyword = studentSearchKeyword.value.toLowerCase();
  return selectedStudents.value.filter(student => 
    student.realName.toLowerCase().includes(keyword) ||
    student.username.toLowerCase().includes(keyword)
  );
});

const filteredAvailableStudents = computed(() => {
  const selectedIds = selectedStudents.value.map(s => s.id);
  let available = allStudents.value.filter(s => !selectedIds.includes(s.id));
  
  if (addStudentSearchKeyword.value) {
    const keyword = addStudentSearchKeyword.value.toLowerCase();
    available = available.filter(student => 
      student.realName.toLowerCase().includes(keyword) ||
      student.username.toLowerCase().includes(keyword)
    );
  }
  
  return available;
});

const getAllStudents = async () => {
  try {
    const response = await request.get('/teacher/courses/students');
    allStudents.value = response.data || [];
  } catch (error) {
    console.error('获取学生列表失败:', error);
    allStudents.value = [];
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
  selectedStudents.value = [];
  studentsToRemove.value = [];
  studentSearchKeyword.value = '';
  
  await getAllStudents();
  dialogVisible.value = true;
};

const handleEdit = async (row: any) => {
  dialogType.value = 'edit';
  form.id = row.id;
  form.name = row.name;
  form.description = row.description;
  form.studentIds = [];
  selectedStudents.value = [];
  studentsToRemove.value = [];
  studentSearchKeyword.value = '';
  
  await getAllStudents();
  
  try {
    const response = await request.get(`/teacher/courses/${row.id}`);
    const detail = response.data;
    if (detail && detail.students) {
      selectedStudents.value = detail.students.map((s: any) => ({
        id: s.id,
        username: s.username,
        realName: s.realName
      }));
      form.studentIds = detail.students.map((s: any) => s.id);
    }
  } catch (error) {
    console.error('获取课程详情失败:', error);
  }
  
  dialogVisible.value = true;
};

const openAddStudentDialog = () => {
  studentsToAdd.value = [];
  addStudentSearchKeyword.value = '';
  addStudentDialogVisible.value = true;
};

const confirmAddStudents = () => {
  if (studentsToAdd.value.length === 0) {
    ElMessage.warning('请选择要添加的学生');
    return;
  }
  
  const newStudents = allStudents.value.filter(s => studentsToAdd.value.includes(s.id));
  selectedStudents.value = [...selectedStudents.value, ...newStudents];
  form.studentIds = selectedStudents.value.map(s => s.id);
  
  addStudentDialogVisible.value = false;
  studentsToAdd.value = [];
  addStudentSearchKeyword.value = '';
};

const handleBatchRemoveStudents = () => {
  if (studentsToRemove.value.length === 0) {
    ElMessage.warning('请选择要移除的学生');
    return;
  }
  
  ElMessageBox.confirm(
    `确定要移除选中的 ${studentsToRemove.value.length} 名学生吗？移除后学生将收到通知。`,
    '确认移除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    selectedStudents.value = selectedStudents.value.filter(
      s => !studentsToRemove.value.includes(s.id)
    );
    form.studentIds = selectedStudents.value.map(s => s.id);
    studentsToRemove.value = [];
    ElMessage.success('已移除选中学生');
  }).catch(() => {});
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    submitting.value = true;
    
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
  } finally {
    submitting.value = false;
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

watch(dialogVisible, (val) => {
  if (!val) {
    studentsToRemove.value = [];
    studentSearchKeyword.value = '';
  }
});

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
  justify-content: flex-end;
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

.student-section {
  width: 100%;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.student-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.student-count {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.student-actions {
  display: flex;
  gap: 8px;
}

.student-search {
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.student-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 8px 16px;
}

.student-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.student-item:last-child {
  border-bottom: none;
}

.student-item :deep(.el-checkbox__label) {
  display: flex;
  align-items: center;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 14px;
  flex-shrink: 0;
}

.student-detail {
  display: flex;
  align-items: center;
  gap: 4px;
}

.student-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.student-username {
  font-size: 13px;
  color: #909399;
}

.add-student-section {
  min-height: 200px;
}

.available-student-list {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 8px 16px;
}

.mb-4 {
  margin-bottom: 16px;
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
  
  .student-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .student-actions {
    width: 100%;
  }
  
  .student-actions .el-button {
    flex: 1;
  }
}
</style>
