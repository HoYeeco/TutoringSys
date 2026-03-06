<template>
  <div class="teacher-course-detail">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>{{ course.name }}</span>
          <el-button type="primary" @click="openAddStudentDialog">添加学生</el-button>
        </div>
      </template>
      <div class="course-info">
        <p>课程代码：{{ course.code }}</p>
        <p>创建时间：{{ formatDate(course.createdAt) }}</p>
        <p>学生人数：{{ students.length }}</p>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>学生列表</span>
        </div>
      </template>
      <RecycleScroller
        class="student-list"
        :items="students"
        :item-size="70"
        key-field="id"
      >
        <template #default="{ item }">
          <div class="student-item">
            <div class="student-id">{{ item.id }}</div>
            <div class="student-name">{{ item.name }}</div>
            <div class="student-email">{{ item.email }}</div>
            <div class="student-joined">{{ formatDate(item.joinedAt) }}</div>
            <div class="student-action">
              <el-button
                type="danger"
                size="small"
                @click="removeStudent(item.id)"
              >
                移除
              </el-button>
            </div>
          </div>
        </template>
      </RecycleScroller>
      <div class="pagination mt-4">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加学生弹窗 -->
    <el-dialog
      v-model="addStudentDialogVisible"
      title="添加学生"
      width="500px"
    >
      <el-select
        v-model="selectedStudents"
        multiple
        placeholder="选择学生"
        style="width: 100%"
      >
        <el-option
          v-for="student in availableStudents"
          :key="student.id"
          :label="`${student.id} - ${student.name}`"
          :value="student.id"
        />
      </el-select>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addStudentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAddStudents">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const route = useRoute();
const courseId = computed(() => route.params.id as string);

const course = ref({
  id: 1,
  name: '计算机导论',
  code: 'CS101',
  createdAt: '2026-01-01 00:00:00'
});

const students = ref([
  {
    id: '2026001',
    name: '张三',
    email: 'zhangsan@example.com',
    joinedAt: '2026-02-01 10:00:00'
  },
  {
    id: '2026002',
    name: '李四',
    email: 'lisi@example.com',
    joinedAt: '2026-02-01 10:00:00'
  },
  {
    id: '2026003',
    name: '王五',
    email: 'wangwu@example.com',
    joinedAt: '2026-02-01 10:00:00'
  }
]);

const availableStudents = ref([
  {
    id: '2026004',
    name: '赵六',
    email: 'zhaoliu@example.com'
  },
  {
    id: '2026005',
    name: '钱七',
    email: 'qianqi@example.com'
  }
]);

const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(3);

const addStudentDialogVisible = ref(false);
const selectedStudents = ref<string[]>([]);

const getCourseDetail = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get(`/teacher/courses/${courseId.value}`);
    // return response.data;
    
    // 模拟数据
    return {
      course: {
        id: 1,
        name: '计算机导论',
        code: 'CS101',
        createdAt: '2026-01-01 00:00:00'
      },
      students: [
        {
          id: '2026001',
          name: '张三',
          email: 'zhangsan@example.com',
          joinedAt: '2026-02-01 10:00:00'
        },
        {
          id: '2026002',
          name: '李四',
          email: 'lisi@example.com',
          joinedAt: '2026-02-01 10:00:00'
        },
        {
          id: '2026003',
          name: '王五',
          email: 'wangwu@example.com',
          joinedAt: '2026-02-01 10:00:00'
        }
      ]
    };
  } catch (error) {
    ElMessage.error('获取课程详情失败');
    return null;
  }
};

const getAvailableStudents = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get('/teacher/courses/available-students');
    // return response.data;
    
    // 模拟数据
    return [
      {
        id: '2026004',
        name: '赵六',
        email: 'zhaoliu@example.com'
      },
      {
        id: '2026005',
        name: '钱七',
        email: 'qianqi@example.com'
      }
    ];
  } catch (error) {
    ElMessage.error('获取可选学生失败');
    return [];
  }
};

const addStudents = async (studentIds: string[]) => {
  try {
    // 实际项目中调用接口
    // const response = await request.post(`/teacher/courses/${courseId.value}/students`, { studentIds });
    // return response.data;
    
    // 模拟成功
    return { success: true };
  } catch (error) {
    ElMessage.error('添加学生失败');
    throw error;
  }
};

const removeStudent = async (studentId: string) => {
  try {
    ElMessageBox.confirm('确定要移除该学生吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      // 实际项目中调用接口
      // await request.delete(`/teacher/courses/${courseId.value}/students/${studentId}`);
      
      // 模拟成功
      students.value = students.value.filter(s => s.id !== studentId);
      total.value--;
      ElMessage.success('移除学生成功');
    });
  } catch (error) {
    ElMessage.error('移除学生失败');
  }
};

const { execute: fetchCourseDetail } = useRequest(getCourseDetail);
const { execute: fetchAvailableStudents } = useRequest(getAvailableStudents);
const { execute: addStudentsToCourse } = useRequest(addStudents);

const openAddStudentDialog = () => {
  fetchAvailableStudents().then(data => {
    if (data) {
      availableStudents.value = data;
      addStudentDialogVisible.value = true;
    }
  });
};

const confirmAddStudents = () => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请选择学生');
    return;
  }

  addStudentsToCourse(selectedStudents.value).then(() => {
    ElMessage.success('添加学生成功');
    addStudentDialogVisible.value = false;
    selectedStudents.value = [];
    // 重新获取学生列表
    fetchCourseDetail().then(data => {
      if (data) {
        students.value = data.students;
        total.value = data.students.length;
      }
    });
  });
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  // 实际项目中重新获取数据
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  // 实际项目中重新获取数据
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchCourseDetail().then(data => {
    if (data) {
      course.value = data.course;
      students.value = data.students;
      total.value = data.students.length;
    }
  });
});
</script>

<style scoped>
.teacher-course-detail {
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

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.student-list {
  height: 500px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.student-item {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 70px;
  border-bottom: 1px solid #e4e7ed;
}

.student-item:last-child {
  border-bottom: none;
}

.student-id {
  width: 120px;
}

.student-name {
  width: 120px;
}

.student-email {
  flex: 1;
}

.student-joined {
  width: 180px;
}

.student-action {
  width: 120px;
  text-align: right;
}
</style>