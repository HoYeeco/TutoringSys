<template>
  <div class="student-courses">
    <el-card shadow="never" class="courses-section">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><School /></el-icon>
            </div>
            <span>我的课程</span>
          </div>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索课程名称或教师"
              clearable
              @keyup.enter="handleSearch"
              class="search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <el-empty v-if="filteredCourses.length === 0" description="暂无已选课程" />
      <div v-else class="courses-grid">
        <div
          v-for="course in filteredCourses"
          :key="course.id"
          class="course-card"
          @click="handleCourseDetail(course.id)"
        >
          <div class="course-card__header">
            <div class="course-meta">
              <h3 class="course-name">{{ course.name }}</h3>
              <div class="teacher-info">
                <el-avatar :size="28" :src="course.teacherAvatar">
                  {{ course.teacherName?.charAt(0) || '?' }}
                </el-avatar>
                <span class="teacher-name">{{ course.teacherName }}</span>
              </div>
            </div>
          </div>
          <div class="course-card__body">
            <p class="course-description">{{ course.description || '暂无课程描述' }}</p>
          </div>
          <div class="course-card__footer">
            <div class="course-stats">
              <div class="stat-item">
                <el-icon><Document /></el-icon>
                <span>{{ course.assignmentCount || 0 }} 份作业</span>
              </div>
            </div>
            <el-button type="primary" size="small" @click.stop="handleCourseDetail(course.id)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, School, Document } from '@element-plus/icons-vue';
import request from '@/utils/request';

const router = useRouter();
const courses = ref([]);
const searchKeyword = ref('');

const filteredCourses = computed(() => {
  if (!searchKeyword.value) {
    return courses.value;
  }
  const keyword = searchKeyword.value.toLowerCase();
  return courses.value.filter(course => {
    return (
      course.name.toLowerCase().includes(keyword) ||
      course.teacherName.toLowerCase().includes(keyword)
    );
  });
});

const getCourses = async () => {
  try {
    const response = await request.get('/student/courses');
    courses.value = response.data || [];
  } catch (error) {
    ElMessage.error('获取课程列表失败');
    courses.value = [];
  }
};

const handleSearch = () => {};

const handleCourseDetail = (courseId: number) => {
  router.push(`/student/courses/${courseId}`);
};

onMounted(() => {
  getCourses();
});
</script>

<style scoped>
.student-courses {
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
}

.search-input {
  width: 280px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.course-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  min-height: 200px;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: transparent;
}

.course-card__header {
  margin-bottom: 16px;
}

.course-meta {
  width: 100%;
}

.course-name {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.teacher-name {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.course-card__body {
  flex: 1;
  margin-bottom: 16px;
}

.course-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.course-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.stat-item .el-icon {
  font-size: 14px;
  color: #c0c4cc;
}

:deep(.el-empty) {
  padding: 60px 0;
}

@media (max-width: 768px) {
  .student-courses {
    padding: 16px;
    margin: 12px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .courses-grid {
    grid-template-columns: 1fr;
  }

  .course-card__footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .course-card__footer .el-button {
    width: 100%;
  }
}
</style>
