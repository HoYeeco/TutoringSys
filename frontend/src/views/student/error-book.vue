<template>
  <div class="student-error-book">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>错题本</span>
          <div class="header-actions">
            <el-button size="small" @click="exportErrorBook('pdf')">
              导出PDF
            </el-button>
            <el-button size="small" type="primary" @click="exportErrorBook('excel')">
              导出Excel
            </el-button>
          </div>
        </div>
      </template>
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索错题题干、答案或课程"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select v-model="courseId" placeholder="选择课程" clearable>
              <el-option
                v-for="course in courses"
                :key="course.id"
                :label="course.name"
                :value="course.id"
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="sortBy" placeholder="排序方式">
              <el-option label="按错题时间（最新）" value="errorTime_desc" />
              <el-option label="按错题时间（最早）" value="errorTime_asc" />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="handleSearch">
              搜索
            </el-button>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <div class="error-list">
        <el-collapse v-model="activeNames">
          <el-collapse-item
            v-for="course in groupedErrors"
            :key="course.id"
            :name="course.id.toString()"
          >
            <template #title>
              <div class="course-title">
                <span>{{ course.name }}</span>
                <el-tag size="small">{{ course.questions.length }}道错题</el-tag>
              </div>
            </template>
            <div class="course-errors">
              <div
                v-for="question in course.questions"
                :key="question.id"
                class="error-item"
              >
                <div class="error-header">
                  <span class="question-type">{{ getQuestionTypeText(question.type) }}</span>
                  <span class="assignment-info">
                    来自：{{ question.assignmentTitle }}
                  </span>
                  <el-button size="small" type="danger" @click="removeError(question.id)">
                    移除
                  </el-button>
                </div>
                <div class="error-content">
                  <p class="question-text">{{ question.content }}</p>
                  <div class="answer-info" v-if="question.type === 'single' || question.type === 'multiple'">
                    <p class="answer-item">
                      <span class="label">你的答案：</span>
                      <span class="student-answer">{{ question.studentAnswer }}</span>
                    </p>
                    <p class="answer-item">
                      <span class="label">正确答案：</span>
                      <span class="correct-answer">{{ question.correctAnswer }}</span>
                    </p>
                  </div>
                  <div class="answer-info" v-else-if="question.type === 'judgment'">
                    <p class="answer-item">
                      <span class="label">你的答案：</span>
                      <span class="student-answer">{{ question.studentAnswer === 'true' ? '正确' : '错误' }}</span>
                    </p>
                    <p class="answer-item">
                      <span class="label">正确答案：</span>
                      <span class="correct-answer">{{ question.correctAnswer === 'true' ? '正确' : '错误' }}</span>
                    </p>
                  </div>
                  <div class="answer-info" v-else-if="question.type === 'essay'">
                    <p class="answer-item">
                      <span class="label">你的答案：</span>
                      <div class="student-answer rich-text" v-html="question.studentAnswer"></div>
                    </p>
                    <p class="answer-item">
                      <span class="label">正确答案：</span>
                      <div class="correct-answer rich-text" v-html="question.correctAnswer"></div>
                    </p>
                  </div>
                  <div class="analysis" v-if="question.analysis">
                    <p class="analysis-label">题目解析：</p>
                    <p class="analysis-content">{{ question.analysis }}</p>
                  </div>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>

      <div class="empty-state" v-if="Object.keys(groupedErrors).length === 0">
        <el-empty description="暂无错题" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import { useRequest } from '@/composables/useRequest';
import request from '@/utils/request';

const searchKeyword = ref('');
const courseId = ref('');
const sortBy = ref('errorTime_desc');
const activeNames = ref<string[]>([]);

const courses = ref([
  { id: 1, name: '计算机导论' },
  { id: 2, name: '高等数学' },
  { id: 3, name: '数据结构' }
]);

const errorList = ref([
  {
    id: 1,
    type: 'multiple',
    content: '以下哪些是计算机的输入设备？',
    studentAnswer: 'A,B,D',
    correctAnswer: 'A,B',
    analysis: '键盘和鼠标是输入设备，显示器和打印机是输出设备。',
    courseId: 1,
    courseName: '计算机导论',
    assignmentTitle: '第一章 计算机基础作业',
    errorTime: '2026-03-10 15:30:00'
  },
  {
    id: 2,
    type: 'single',
    content: '下列哪个不是操作系统？',
    studentAnswer: 'C',
    correctAnswer: 'D',
    analysis: 'Windows、Linux和macOS都是操作系统，而Office是办公软件。',
    courseId: 1,
    courseName: '计算机导论',
    assignmentTitle: '第二章 操作系统作业',
    errorTime: '2026-03-05 16:45:00'
  },
  {
    id: 3,
    type: 'judgment',
    content: '函数在某点可导，则在该点一定连续。',
    studentAnswer: 'false',
    correctAnswer: 'true',
    analysis: '可导必连续，连续不一定可导。',
    courseId: 2,
    courseName: '高等数学',
    assignmentTitle: '第一章 函数作业',
    errorTime: '2026-03-01 11:20:00'
  }
]);

const groupedErrors = computed(() => {
  const grouped: any = {};
  errorList.value.forEach(question => {
    if (!grouped[question.courseId]) {
      grouped[question.courseId] = {
        id: question.courseId,
        name: question.courseName,
        questions: []
      };
    }
    grouped[question.courseId].questions.push(question);
  });
  return grouped;
});

const getErrorBook = async () => {
  try {
    // 实际项目中调用接口
    // const response = await request.get('/student/error-book', {
    //   params: {
    //     keyword: searchKeyword.value,
    //     courseId: courseId.value,
    //     sortBy: sortBy.value
    //   }
    // });
    // return response.data;
    
    // 模拟数据
    return errorList.value;
  } catch (error) {
    ElMessage.error('获取错题本失败');
    return null;
  }
};

const removeErrorFromBook = async (questionId: number) => {
  try {
    // 实际项目中调用接口
    // const response = await request.delete(`/student/error-book/remove/${questionId}`);
    // return response.data;
    
    // 模拟成功
    return { success: true };
  } catch (error) {
    ElMessage.error('移除错题失败');
    throw error;
  }
};

const exportErrorBookData = async (format: string) => {
  try {
    // 实际项目中调用接口
    // const response = await request.get(`/student/error-book/export`, {
    //   params: { format },
    //   responseType: 'blob'
    // });
    // return response.data;
    
    // 模拟成功
    ElMessage.success(`错题本已导出为${format.toUpperCase()}格式`);
    return { success: true };
  } catch (error) {
    ElMessage.error('导出错题本失败');
    throw error;
  }
};

const { execute: fetchErrorBook } = useRequest(getErrorBook);
const { execute: removeErrorApi } = useRequest(removeErrorFromBook);
const { execute: exportErrorBook } = useRequest(exportErrorBookData);

const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '简答题'
  };
  return typeMap[type] || type;
};

const handleSearch = () => {
  fetchErrorBook();
};

const removeError = (questionId: number) => {
  ElMessageBox.confirm('确定要移除这道错题吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    removeErrorApi(questionId).then(() => {
      ElMessage.success('错题已移除');
      // 重新获取错题列表
      fetchErrorBook();
    });
  });
};

onMounted(() => {
  fetchErrorBook().then((data: any) => {
    if (data) {
      errorList.value = data;
      // 默认展开第一个课程
      if (Object.keys(groupedErrors.value).length > 0) {
        activeNames.value = [Object.keys(groupedErrors.value)[0]];
      }
    }
  });
});
</script>

<style scoped>
.student-error-book {
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

.header-actions {
  display: flex;
  gap: 8px;
}

.filter-section {
  margin-top: 16px;
}

.error-list {
  margin-top: 20px;
}

.course-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.course-errors {
  margin-top: 16px;
}

.error-item {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
}

.error-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.question-type {
  background-color: #ecf5ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.assignment-info {
  color: #606266;
  font-size: 14px;
}

.error-content {
  margin-top: 12px;
}

.question-text {
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.answer-info {
  margin-bottom: 16px;
}

.answer-item {
  margin-bottom: 8px;
}

.label {
  font-weight: 500;
  margin-right: 8px;
  color: #606266;
}

.student-answer {
  color: #f56c6c;
}

.correct-answer {
  color: #67c23a;
}

.student-answer.rich-text,
.correct-answer.rich-text {
  padding: 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-top: 4px;
}

.analysis {
  margin-top: 12px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.analysis-label {
  font-weight: 500;
  margin-bottom: 4px;
}

.analysis-content {
  margin: 0;
  line-height: 1.5;
}

.empty-state {
  margin: 60px 0;
  text-align: center;
}
</style>