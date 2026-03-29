<template>
  <div class="student-courses">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>我的课程</span>
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索课程名称或教师"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
              </template>
            </el-input>
          </div>
        </div>
      </template>
    </el-card>

    <div class="courses-grid">
      <el-empty v-if="filteredCourses.length === 0" description="暂无已选课程" />
      <el-card
        v-else
        v-for="course in filteredCourses"
        :key="course.id"
        class="course-card"
        @click="handleCourseDetail(course.id)"
      >
        <div class="course-header">
          <h3 class="course-name">{{ course.name }}</h3>
          <span class="course-teacher">{{ course.teacherName }}</span>
        </div>
        <div class="course-description">{{ course.description }}</div>
        <div class="course-footer">
          <div class="course-info">
            <span class="course-assignments">作业数量: {{ course.assignmentCount || 0 }}</span>
            <span class="course-join-time">加入时间: {{ formatDate(course.joinTime) }}</span>
          </div>
          <el-button type="primary" size="small" @click.stop="handleCourseDetail(course.id)">
            查看详情
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import request from '@/utils/request';

const router = useRouter();
const courses = ref([]);
const searchKeyword = ref('');

// 过滤后的课程列表
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

// 获取学生课程列表
const getCourses = async () => {
  try {
    const response = await request.get('/student/courses');
    courses.value = response.data || [];
  } catch (error) {
    ElMessage.error('获取课程列表失败');
    courses.value = [];
  }
};

// 处理搜索
const handleSearch = () => {
  // 搜索逻辑已在computed中实现
};

const handleCourseDetail = (courseId: number) => {
  router.push(`/student/courses/${courseId}`);
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    year: 'numeric'
  });
};

onMounted(() => {
  getCourses();
});
</script>

<style scoped>
.student-courses {
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

.search-box {
  flex: 1;
  min-width: 300px;
  max-width: 500px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.course-card {
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.course-header {
  margin-bottom: 15px;
}

.course-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 5px;
}

.course-teacher {
  font-size: 14px;
  color: #999;
  background-color: var(--color-background-light);
  padding: 2px 8px;
  border-radius: 10px;
}

.course-description {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 15px;
  border-top: 1px solid var(--color-border);
}

.course-info {
  font-size: 12px;
  color: #999;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.course-assignments,
.course-join-time {
  display: block;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-box {
    width: 100%;
    min-width: unset;
  }
  
  .courses-grid {
    grid-template-columns: 1fr;
  }
  
  .course-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .course-info {
    flex-direction: row;
    gap: 15px;
    width: 100%;
    justify-content: space-between;
  }
  
  .course-card .el-button {
    width: 100%;
  }
}
</style>