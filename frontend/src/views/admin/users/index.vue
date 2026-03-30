<template>
  <div class="admin-users">
    <!-- 用户列表模块 -->
    <el-card shadow="never" class="users-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Files /></el-icon>
            </div>
            <span>用户管理</span>
          </div>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户账号、姓名"
              clearable
              @keyup.enter="handleSearch"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="searchRole" placeholder="按角色筛选" class="filter-item" clearable @change="handleSearch">
              <el-option label="全部" value="" />
              <el-option label="学生" value="STUDENT" />
              <el-option label="教师" value="TEACHER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
            <el-select v-model="searchStatus" placeholder="按状态筛选" class="filter-item" clearable @change="handleSearch">
              <el-option label="全部" value="" />
              <el-option label="正常" value="active" />
              <el-option label="禁用" value="inactive" />
            </el-select>
            <el-button type="primary" @click="handleAddUser">
              <el-icon><Plus /></el-icon> 添加用户
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="users" 
        class="modern-table"
        :cell-style="{ textAlign: 'center' }" 
        :header-cell-style="{ textAlign: 'center' }"
        @selection-change="handleSelectionChange"
        @sort-change="handleTableSortChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="头像" width="80">
          <template #default="scope">
            <div class="user-avatar">
              <img v-if="scope.row.avatar" :src="scope.row.avatar" alt="头像" @error="handleAvatarError(scope.row)" />
              <span v-else class="avatar-placeholder">{{ (scope.row.realName || scope.row.username).charAt(0) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="账号" min-width="150" sortable="custom" />
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleText(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
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
      
      <div class="pagination-container">
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
        <el-form-item label="账号" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入账号" />
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
import { Plus, Files, Search, Delete } from '@element-plus/icons-vue';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const users = ref([]);
const allUsersData = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchKeyword = ref('');
const searchRole = ref('');
const searchStatus = ref('');
const dialogVisible = ref(false);
const dialogType = ref('add');
const selectedUserIds = ref<number[]>([]);
const currentSortOrder = ref<'asc' | 'desc'>('asc');
const userForm = ref({
  id: 0,
  username: '',
  realName: '',
  password: '',
  role: '',
  email: '',
  phone: '',
  status: 1
});

const getUsernameSortPriority = (username: string): number => {
  if (username.startsWith('admin')) return 1;
  if (username.startsWith('t')) return 2;
  if (username.startsWith('s')) return 3;
  return 4;
};

const sortByUsername = (a: any, b: any): number => {
  const priorityA = getUsernameSortPriority(a.username);
  const priorityB = getUsernameSortPriority(b.username);
  
  if (priorityA !== priorityB) {
    return priorityA - priorityB;
  }
  
  return a.username.localeCompare(b.username);
};

const applySortAndPaginate = (userList: any[], order: 'asc' | 'desc') => {
  const sorted = [...userList].sort((a, b) => {
    const result = sortByUsername(a, b);
    return order === 'asc' ? result : -result;
  });
  
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  
  return sorted.slice(startIndex, endIndex);
};

const getUsers = async () => {
  try {
    console.log('开始获取用户列表...');
    console.log('当前页码:', currentPage.value);
    console.log('每页大小:', pageSize.value);
    // 构建查询参数
    const params: Record<string, any> = {
      page: 1, // 总是查询第一页，获取所有数据
      size: 100 // 设置一个足够大的值，确保获取所有数据
    };
    
    console.log('查询参数:', params);
    
    // 只有当搜索条件不为空时才传递参数
    if (searchKeyword.value && searchKeyword.value.trim() !== '') {
      params.username = searchKeyword.value;
      console.log('搜索关键词:', searchKeyword.value);
    }
    if (searchRole.value && searchRole.value.trim() !== '') {
      params.role = searchRole.value;
    }
    if (searchStatus.value && searchStatus.value.trim() !== '') {
      params.status = searchStatus.value === 'active' ? 1 : 0;
    }
    
    // 调用后端API
    console.log('开始调用后端API...');
    console.log('完整请求参数:', params);
    
    const response = await request.get('/admin/users', {
      params
    });
    
    console.log('API响应:', response);
    console.log('记录数:', response?.data?.records?.length || 0);
    console.log('总数:', response?.data?.total || 0);
    
    // 获取所有数据
    const allUsers = response?.data?.records || [];
    allUsersData.value = allUsers;
    total.value = response?.data?.total || 0;
    
    // 应用全列表排序后分页
    const pageUsers = applySortAndPaginate(allUsers, currentSortOrder.value);
    
    console.log('前端分页后记录数:', pageUsers.length);
    
    return {
      list: pageUsers,
      total: total.value
    };
  } catch (error) {
    console.error('获取用户列表失败:', error);
    console.error('错误详情:', error.message);
    ElMessage.error('获取用户列表失败');
    return {
      list: [],
      total: 0
    };
  }
};

const { execute: fetchUsers } = useRequest(getUsers);

const handleTableSortChange = ({ prop, order }: { prop: string; order: string | null }) => {
  if (prop === 'username') {
    if (order === 'ascending') {
      currentSortOrder.value = 'asc';
    } else if (order === 'descending') {
      currentSortOrder.value = 'desc';
    } else {
      currentSortOrder.value = 'asc';
    }
    users.value = applySortAndPaginate(allUsersData.value, currentSortOrder.value);
  }
};

const handleSearch = () => {
  console.log('搜索关键词:', searchKeyword.value);
  currentPage.value = 1;
  currentSortOrder.value = 'asc';
  fetchUsers().then((data: any) => {
    console.log('搜索结果:', data);
    users.value = data.list;
    total.value = data.total;
  });
};

const handleAddUser = () => {
  dialogType.value = 'add';
  userForm.value = {
    id: 0,
    username: '',
    realName: '',
    password: '',
    role: '',
    email: '',
    phone: '',
    status: 1
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
    status: user.status || 1
  };
  dialogVisible.value = true;
};

const handleDeleteUser = (userId: number) => {
  // 检查是否有批量选择的用户
  if (selectedUserIds.value.length > 0) {
    // 批量删除
    ElMessageBox.confirm(`确定要删除选中的 ${selectedUserIds.value.length} 个用户吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      // 调用后端API进行批量删除
      request.delete('/admin/users/batch', {
        data: { ids: selectedUserIds.value }
      }).then(() => {
        ElMessage.success('批量删除成功');
        fetchUsers().then((data: any) => {
          users.value = data.list;
          total.value = data.total;
          // 重置选择
          selectedUserIds.value = [];
        });
      });
    });
  } else {
    // 单个删除
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
  }
};

const handleSaveUser = async () => {
  try {
    // 验证表单
    if (!userForm.value.username) {
      ElMessage.warning('请输入账号');
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
  console.log('handleSizeChange被调用，新的分页大小:', size);
  pageSize.value = size;
  currentPage.value = 1;
  users.value = applySortAndPaginate(allUsersData.value, currentSortOrder.value);
};

const handleCurrentChange = (current: number) => {
  currentPage.value = current;
  users.value = applySortAndPaginate(allUsersData.value, currentSortOrder.value);
  selectedUserIds.value = [];
};

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedUserIds.value = selection.map(item => item.id);
  console.log('选中的用户ID:', selectedUserIds.value);
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

// 处理头像加载失败
const handleAvatarError = (user: any) => {
  user.avatar = null;
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
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: none;
  background: transparent;
}

:deep(.el-card__body) {
  padding: 0 20px 20px;
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
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
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
  width: 250px;
}

.filter-item {
  width: 150px;
}

/* 现代化表格样式 */
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

/* 用户头像 */
.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar .avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  color: #fff;
  font-size: 16px;
  font-weight: bold;
}

/* 分页 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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
  
  .pagination-container {
    justify-content: center;
  }
}
</style>