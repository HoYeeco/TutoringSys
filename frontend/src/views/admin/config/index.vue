<template>
  <div class="admin-config">
    <el-card shadow="never" class="config-card">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Setting /></el-icon>
            </div>
            <span>配置管理</span>
          </div>
          <div class="card-header__actions">
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              新增配置
            </el-button>
            <el-button type="danger" @click="clearAllCache">
              <el-icon><Delete /></el-icon>
              清除缓存
            </el-button>
          </div>
        </div>
      </template>

      <div class="filter-form">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="配置类型">
            <el-select v-model="filterForm.configType" placeholder="全部类型" clearable @change="handleFilter">
              <el-option label="基础配置" value="basic" />
              <el-option label="大模型配置" value="llm" />
              <el-option label="缓存配置" value="cache" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="filterForm.keyword" placeholder="搜索配置键或描述" clearable @keyup.enter="handleFilter" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleFilter">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        :data="configs"
        style="width: 100%"
        v-loading="loading"
        class="modern-table"
        :cell-style="{ textAlign: 'center' }"
        :header-cell-style="{ textAlign: 'center' }"
      >
        <el-table-column prop="configKey" label="配置键" min-width="180" />
        <el-table-column prop="configValue" label="配置值" min-width="200">
          <template #default="scope">
            <el-tooltip :content="scope.row.configValue" placement="top" :disabled="!isOverflow(scope.row.configValue, 20)">
              <span class="ellipsis-text">{{ scope.row.configValue || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="configType" label="配置类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.configType)">
              {{ getTypeLabel(scope.row.configType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="scope">
            <el-tooltip :content="scope.row.description" placement="top" :disabled="!isOverflow(scope.row.description, 25)">
              <span class="ellipsis-text">{{ scope.row.description || '-' }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteConfig(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑配置' : '新增配置'"
      width="500px"
      append-to-body
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="formData.configKey" placeholder="请输入配置键" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="formData.configValue" type="textarea" :rows="3" placeholder="请输入配置值" />
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="formData.configType" placeholder="请选择配置类型" style="width: 100%">
            <el-option label="基础配置" value="basic" />
            <el-option label="大模型配置" value="llm" />
            <el-option label="缓存配置" value="cache" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入配置描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus';
import { Setting, Plus, Delete } from '@element-plus/icons-vue';
import request from '@/utils/request';

interface Config {
  id: number;
  configKey: string;
  configValue: string;
  configType: string;
  description: string;
  createTime: string;
  updateTime: string;
}

const loading = ref(false);
const configs = ref<Config[]>([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref<FormInstance>();

const filterForm = reactive({
  configType: '',
  keyword: ''
});

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

const formData = reactive({
  id: 0,
  configKey: '',
  configValue: '',
  configType: '',
  description: ''
});

const formRules: FormRules = {
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
};

const getTypeTagType = (type: string): string => {
  const typeMap: Record<string, string> = {
    basic: '',
    llm: 'success',
    cache: 'warning',
    other: 'info'
  };
  return typeMap[type] || 'info';
};

const getTypeLabel = (type: string): string => {
  const typeMap: Record<string, string> = {
    basic: '基础配置',
    llm: '大模型配置',
    cache: '缓存配置',
    other: '其他'
  };
  return typeMap[type] || type;
};

const isOverflow = (text: string | null, maxChars: number): boolean => {
  if (!text) return false;
  return text.length > maxChars;
};

const getConfigs = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size
    };
    if (filterForm.configType) {
      params.configType = filterForm.configType;
    }
    if (filterForm.keyword) {
      params.keyword = filterForm.keyword;
    }
    const response = await request.get('/admin/config', { params });
    configs.value = response.data?.records || [];
    pagination.total = response.data?.total || 0;
  } catch (error) {
    console.error('获取配置列表失败:', error);
  } finally {
    loading.value = false;
  }
};

const handleFilter = () => {
  pagination.page = 1;
  getConfigs();
};

const resetFilter = () => {
  filterForm.configType = '';
  filterForm.keyword = '';
  pagination.page = 1;
  getConfigs();
};

const handleSizeChange = (size: number) => {
  pagination.size = size;
  pagination.page = 1;
  getConfigs();
};

const handleCurrentChange = (page: number) => {
  pagination.page = page;
  getConfigs();
};

const resetForm = () => {
  formData.id = 0;
  formData.configKey = '';
  formData.configValue = '';
  formData.configType = '';
  formData.description = '';
};

const showCreateDialog = () => {
  isEdit.value = false;
  resetForm();
  dialogVisible.value = true;
};

const showEditDialog = (config: Config) => {
  isEdit.value = true;
  formData.id = config.id;
  formData.configKey = config.configKey;
  formData.configValue = config.configValue;
  formData.configType = config.configType;
  formData.description = config.description;
  dialogVisible.value = true;
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await request.put(`/admin/config/${formData.id}`, {
            configValue: formData.configValue,
            configType: formData.configType,
            description: formData.description
          });
          ElMessage.success('配置更新成功');
        } else {
          await request.post('/admin/config', {
            configKey: formData.configKey,
            configValue: formData.configValue,
            configType: formData.configType,
            description: formData.description
          });
          ElMessage.success('配置创建成功');
        }
        dialogVisible.value = false;
        getConfigs();
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新配置失败' : '创建配置失败');
      }
    }
  });
};

const deleteConfig = async (config: Config) => {
  try {
    await ElMessageBox.confirm(`确定要删除配置「${config.configKey}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await request.delete(`/admin/config/${config.id}`);
    ElMessage.success('配置删除成功');
    getConfigs();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除配置失败');
    }
  }
};

const clearAllCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清除所有配置缓存吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const response = await request.post('/admin/config/clear-cache');
    ElMessage.success(response.data || '缓存已清除');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清除缓存失败');
    }
  }
};

onMounted(() => {
  getConfigs();
});
</script>

<style scoped>
.admin-config {
  padding: 24px;
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(4px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
}

.config-card {
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
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  color: #fff;
}

.card-header__icon .el-icon {
  font-size: 24px;
}

.card-header__actions {
  display: flex;
  gap: 12px;
}

.filter-form {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 12px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 16px;
}

.filter-form :deep(.el-select) {
  width: 150px;
}

.filter-form :deep(.el-input) {
  width: 200px;
}

.modern-table {
  width: 100%;
}

.modern-table :deep(.el-table__header th) {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  color: #606266;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 8px;
}

.modern-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.modern-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.ellipsis-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .admin-config {
    padding: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .card-header__actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
