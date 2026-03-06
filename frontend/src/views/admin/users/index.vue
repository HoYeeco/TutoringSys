<template>
  <div class="admin-users">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAddUser">
            <el-icon><Plus /></el-icon> 添加用户
          </el-button>
        </div>
      </template>
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="请输入用户名或姓名"
          style="width: 300px"
          prefix-icon="el-icon-search"
        />
        <el-select v-model="searchRole" placeholder="选择角色" style="width: 120px; margin-left: 12px">
          <el-option label="全部" value="" />
          <el-option label="学生" value="STUDENT" />
          <el-option label="教师" value="TEACHER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
        <el-select v-model="searchStatus" placeholder="选择状态" style="width: 120px; margin-left: 12px">
          <el-option label="全部" value="" />
          <el-option label="正常" value="active" />
          <el-option label="禁用" value="inactive" />
        </el-select>
        <el-button type="primary" @click="handleSearch" style="margin-left: 12px">
          搜索
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <el-empty v-if="users.length === 0" description="暂无用户" />
      <el-table v-else :data="users" style="width: 100%">
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="handleEditUser(scope.row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDeleteUser(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
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

    <!-- 添加/编辑用户弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      width="500px"
    >
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" v-if="dialogType === 'add'" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="选择角色">
            <el-option label="学生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" placeholder="选择状态">
            <el-option label="正常" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveUser">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const users = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchKeyword = ref('');
const searchRole = ref('');
const searchStatus = ref('');
const dialogVisible = ref(false);
const dialogType = ref('add');
const userForm = ref({
  id: '',
  username: '',
  realName: '',
  password: '',
  role: '',
  email: '',
  phone: '',
  status: 'active'
});

// 模拟数据
const mockUsers = [
  {
    id: 1,
    username: 'admin',
    realName: '管理员',
    role: 'ADMIN',
    email: 'admin@example.com',
    phone: '13800138001',
    status: 'active',
    createdAt: '2026-02-01 08:00:00'
  },
  {
    id: 2,
    username: 'teacher',
    realName: '教师',
    role: 'TEACHER',
    email: 'teacher@example.com',
    phone: '13800138002',
    status: 'active',
    createdAt: '2026-02-01 08:00:00'
  },
  {
    id: 3,
    username: 'student',
    realName: '学生',
    role: 'STUDENT',
    email: 'student@example.com',
    phone: '13800138003',
    status: 'active',
    createdAt: '2026-02-01 08:00:00'
  }
];

const getUsers = async () => {
  try {
    // 调用后端API
    const response = await request.get('/admin/users', {
      params: {
        page: currentPage.value,
        pageSize: pageSize.value,
        username: searchKeyword.value,
        role: searchRole.value,
        status: searchStatus.value === 'active' ? 1 : searchStatus.value === 'inactive' ? 0 : undefined
      }
    });
    console.log('API响应:', response);
    // 适配后端返回的字段名
    return {
      list: response.records || [],
      total: response.total || 0
    };
  } catch (error) {
    console.error('获取用户列表失败:', error);
    ElMessage.error('获取用户列表失败');
    return { list: [], total: 0 };
  }
};

const { execute: fetchUsers } = useRequest(getUsers);

const handleSearch = () => {
  currentPage.value = 1;
  fetchUsers().then((data: any) => {
    users.value = data.list;
    total.value = data.total;
  });
};

const handleAddUser = () => {
  dialogType.value = 'add';
  userForm.value = {
    id: '',
    username: '',
    realName: '',
    password: '',
    role: ''
  };
  dialogVisible.value = true;
};

const handleEditUser = (user: any) => {
  dialogType.value = 'edit';
  userForm.value = {
    id: user.id,
    username: user.username,
    realName: user.realName,
    password: '',
    role: user.role,
    email: user.email || '',
    phone: user.phone || '',
    status: user.status?.toString() || '1'
  };
  dialogVisible.value = true;
};

const handleDeleteUser = (userId: number) => {
  ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 调用后端API
    request.delete(`/admin/users/${userId}`).then(() => {
      ElMessage.success('删除成功');
      fetchUsers().then((data: any) => {
        users.value = data.list;
        total.value = data.total;
      });
    });
  });
};

const handleSaveUser = async () => {
  try {
    // 验证表单
    if (!userForm.value.username) {
      ElMessage.warning('请输入用户名');
      return;
    }
    if (!userForm.value.realName) {
      ElMessage.warning('请输入姓名');
      return;
    }
    if (dialogType.value === 'add' && !userForm.value.password) {
      ElMessage.warning('请输入密码');
      return;
    }
    if (!userForm.value.role) {
      ElMessage.warning('请选择角色');
      return;
    }
    
    // 调用后端API
    if (dialogType.value === 'add') {
      await request.post('/admin/users', userForm.value);
    } else {
      await request.put(`/admin/users/${userForm.value.id}`, userForm.value);
    }
    
    ElMessage.success(dialogType.value === 'add' ? '添加成功' : '编辑成功');
    dialogVisible.value = false;
    fetchUsers().then((data: any) => {
      users.value = data.list;
      total.value = data.total;
    });
  } catch (error) {
    ElMessage.error(dialogType.value === 'add' ? '添加失败' : '编辑失败');
  }
};

const handleSizeChange = (size: number) => {
  pageSize.value = size;
  fetchUsers().then((data: any) => {
    users.value = data.list;
    total.value = data.total;
  });
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  fetchUsers().then((data: any) => {
    users.value = data.list;
    total.value = data.total;
  });
};

const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    STUDENT: '学生',
    TEACHER: '教师',
    ADMIN: '管理员'
  };
  return roleMap[role] || role;
};

const getRoleType = (role: string) => {
  const typeMap: Record<string, string> = {
    STUDENT: 'info',
    TEACHER: 'primary',
    ADMIN: 'warning'
  };
  return typeMap[role] || 'default';
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  fetchUsers().then((data: any) => {
    users.value = data.list;
    total.value = data.total;
  });
});
</script>

<style scoped>
.admin-users {
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

.search-section {
  display: flex;
  align-items: center;
  margin-top: 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}
</style>