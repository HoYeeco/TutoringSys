<template>
  <div class="teacher-assignment-edit">
    <el-card shadow="never" class="config-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><Document /></el-icon>
          </div>
          <span class="card-title__text">作业信息</span>
        </div>
      </template>

      <el-form :model="assignment" label-width="120px" class="config-form">
        <el-form-item label="作业标题" required>
          <el-input v-model="assignment.title" placeholder="请输入作业标题" />
        </el-form-item>
        <el-form-item label="所属课程" required>
          <el-select v-model="assignment.courseId" placeholder="选择课程">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="总分值">
          <el-input v-model="totalScore" disabled style="width: 120px" />
          <span class="score-hint">（自动累加题目分值）</span>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input
            v-model="assignment.description"
            type="textarea"
            :rows="3"
            placeholder="请输入补充说明"
          />
        </el-form-item>
        <el-form-item label="截止时间" required>
          <el-date-picker
            v-model="assignment.deadline"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="questions-section">
      <template #header>
        <div class="card-title">
          <div class="card-title__icon">
            <el-icon><List /></el-icon>
          </div>
          <span class="card-title__text">题目配置</span>
        </div>
        <div class="header-actions">
          <el-button type="success" @click="openQuestionTypeDialog"
            >添加题目</el-button
          >
        </div>
      </template>

      <div v-if="questions.length === 0" class="empty-questions">
        <el-empty description="暂无题目，请添加题目" :image-size="160" />
        <el-button type="success" @click="openQuestionTypeDialog">
          <el-icon><Plus /></el-icon> 添加题目
        </el-button>
      </div>

      <div
        v-else
        v-for="(question, index) in questions"
        :key="question.id"
        class="question-card"
      >
        <div class="question-header">
          <span class="question-number">{{ index + 1 }}.</span>
          <el-select
            v-model="question.type"
            placeholder="选择题型"
            style="width: 120px"
            @change="handleQuestionTypeChange(question)"
          >
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judgment" />
            <el-option label="主观题" value="essay" />
          </el-select>
          <el-button type="danger" size="small" @click="removeQuestion(index)">
            删除
          </el-button>
        </div>

        <el-form :model="question" label-width="80px" class="mt-2">
          <el-form-item label="题目内容" required>
            <div class="rich-text-editor">
              <div ref="editorRefs[question.id]" class="quill-editor"></div>
            </div>
          </el-form-item>

          <el-form-item
            label="选项"
            v-if="['single', 'multiple'].includes(question.type)"
          >
            <div
              v-for="(option, optIndex) in question.options"
              :key="optIndex"
              class="option-item"
            >
              <span class="option-label"
                >{{ String.fromCharCode(65 + Number(optIndex)) }}.</span
              >
              <el-input
                v-model="option.value"
                placeholder="请输入选项内容"
                style="width: 80%"
              />
              <el-button
                type="danger"
                size="small"
                @click="removeOption(question, Number(optIndex))"
                :disabled="question.options.length <= 3"
              >
                删除
              </el-button>
            </div>
            <el-button
              type="primary"
              size="small"
              @click="addOption(question)"
              :disabled="question.options.length >= 9"
            >
              添加选项
            </el-button>
            <div class="option-hint">
              选项数量：{{ question.options.length }}/9（最少3个，最多9个）
            </div>
          </el-form-item>

          <el-form-item
            label="标准答案"
            required
            v-if="['single', 'multiple', 'judgment'].includes(question.type)"
          >
            <el-input
              v-model="question.correctAnswer"
              placeholder="请输入答案"
            />
          </el-form-item>

          <el-form-item label="分值" required>
            <el-input-number
              v-model="question.score"
              :min="1"
              :max="100"
              style="width: 100px"
            />
          </el-form-item>

          <!-- 主观题特有设置 -->
          <template v-if="question.type === 'essay'">
            <el-form-item label="字数限制">
              <div class="word-limit">
                <el-input-number
                  v-model="question.minWords"
                  :min="0"
                  style="width: 100px"
                  placeholder="最小"
                />
                <span class="word-limit-separator">-</span>
                <el-input-number
                  v-model="question.maxWords"
                  :min="0"
                  style="width: 100px"
                  placeholder="最大"
                />
                <span class="word-limit-hint">（0表示无限制）</span>
              </div>
            </el-form-item>
            <el-form-item label="参考答案">
              <div class="rich-text-editor">
                <div
                  ref="answerEditorRefs[question.id]"
                  class="quill-editor"
                ></div>
              </div>
              <div class="answer-hint">
                AI大模型将基于参考答案抓取关键词及逻辑关系以打分并生成评语
              </div>
            </el-form-item>
          </template>
        </el-form>
      </div>

      <div class="total-score mt-4">
        <span>总分：{{ totalScore }}分</span>
      </div>
    </el-card>

    <div class="action-buttons mt-4">
      <el-button @click="previewAssignment">预览模式</el-button>
      <el-button type="primary" @click="publishAssignment">现在发布</el-button>
    </div>

    <!-- 题型选择弹窗 -->
    <el-dialog
      v-model="questionTypeDialogVisible"
      title="选择题型"
      width="400px"
    >
      <div class="question-type-list">
        <div
          v-for="type in questionTypes"
          :key="type.value"
          class="question-type-item"
          @click="addQuestionByType(type.value)"
        >
          <el-icon class="type-icon">{{ type.icon }}</el-icon>
          <span class="type-name">{{ type.label }}</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="questionTypeDialogVisible = false">取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Document,
  Plus,
  Check,
  Delete,
  Edit,
  List,
} from '@element-plus/icons-vue';
import request from '@/utils/request';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';

const route = useRoute();
const router = useRouter();
const assignmentId = computed(() => {
  const id = route.params.id as string;
  // 如果是 "create" 或 undefined，返回 null
  if (!id || id === 'create' || id === 'undefined') {
    return null;
  }
  return id;
});
const isEdit = computed(() => !!assignmentId.value);

const assignment = ref({
  id: '',
  title: '',
  courseId: '',
  description: '',
  deadline: '',
});

const courses = ref([
  { id: 1, name: '计算机导论' },
  { id: 2, name: '数据结构' },
  { id: 3, name: '算法设计与分析' },
]);

const questions = ref([]);

const questionTypes = [
  { label: '单选题', value: 'single', icon: Check },
  { label: '多选题', value: 'multiple', icon: Check },
  { label: '判断题', value: 'judgment', icon: Check },
  { label: '主观题', value: 'essay', icon: Edit },
];

const questionTypeDialogVisible = ref(false);

const editorRefs = ref({});
const answerEditorRefs = ref({});
const editors = ref({});
const answerEditors = ref({});

const totalScore = computed(() => {
  return questions.value.reduce(
    (sum, question) => sum + (question.score || 0),
    0,
  );
});

const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '主观题',
  };
  return typeMap[type] || type;
};

const openQuestionTypeDialog = () => {
  questionTypeDialogVisible.value = true;
};

const addQuestionByType = (type: string) => {
  const newQuestion = {
    id: Date.now(),
    type: type,
    content: '',
    options:
      type === 'single' || type === 'multiple'
        ? [{ value: '' }, { value: '' }, { value: '' }]
        : [],
    correctAnswer: '',
    score: 10,
    minWords: 0,
    maxWords: 0,
    referenceAnswer: '',
  };
  questions.value.push(newQuestion);
  questionTypeDialogVisible.value = false;

  // 初始化编辑器
  nextTick(() => {
    initQuillEditors();
  });
};

const handleQuestionTypeChange = (question: any) => {
  if (question.type === 'single' || question.type === 'multiple') {
    if (!question.options || question.options.length < 3) {
      question.options = [{ value: '' }, { value: '' }, { value: '' }];
    }
  } else {
    question.options = [];
  }
};

const removeQuestion = (index: number) => {
  ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    questions.value.splice(index, 1);
  });
};

const addOption = (question: any) => {
  if (question.options.length < 9) {
    question.options.push({ value: '' });
  }
};

const removeOption = (question: any, index: number) => {
  if (question.options.length > 3) {
    question.options.splice(index, 1);
  }
};

const openQuestionBankDialog = () => {
  // 题库功能已移除
};

const confirmSelectQuestions = () => {
  // 题库功能已移除
};

const getAssignmentDetail = async (id: string) => {
  try {
    const response = await request.get(`/teacher/assignments/${id}`);
    const data = response.data;

    if (!data) {
      ElMessage.error('作业数据为空');
      return;
    }

    assignment.value = {
      id: data.id,
      title: data.title || '',
      courseId: data.courseId,
      description: data.description || '',
      deadline: data.deadline || '',
    };

    questions.value = data.questions || [];

    await nextTick();
    initEditors();

    ElMessage.success('作业数据加载成功');
  } catch (error: any) {
    console.error('加载作业数据失败:', error);
    ElMessage.error('加载作业数据失败：' + (error?.message || '未知错误'));
  }
};

const initEditors = () => {
  questions.value.forEach((question) => {
    if (question.id && editorRefs.value[question.id]) {
      const editor = new Quill(editorRefs.value[question.id], {
        theme: 'snow',
        placeholder: '请输入题目内容...',
        modules: {
          toolbar: [
            ['bold', 'italic', 'underline'],
            [{ list: 'ordered' }, { list: 'bullet' }],
            ['link', 'clean'],
          ],
        },
      });

      if (question.content) {
        editor.root.innerHTML = question.content;
      }

      editors.value[question.id] = editor;
    }

    // 主观题需要初始化答案编辑器
    if (
      question.type === 'essay' &&
      question.id &&
      answerEditorRefs.value[question.id]
    ) {
      const answerEditor = new Quill(answerEditorRefs.value[question.id], {
        theme: 'snow',
        placeholder: '请输入参考答案...',
        modules: {
          toolbar: [
            ['bold', 'italic', 'underline'],
            [{ list: 'ordered' }, { list: 'bullet' }],
            ['link', 'clean'],
          ],
        },
      });

      if (question.referenceAnswer) {
        answerEditor.root.innerHTML = question.referenceAnswer;
      }

      answerEditors.value[question.id] = answerEditor;
    }
  });
};

const publishAssignment = async () => {
  try {
    // 验证表单
    if (!assignment.value.title) {
      ElMessage.warning('请输入作业标题');
      return;
    }
    if (!assignment.value.courseId) {
      ElMessage.warning('请选择所属课程');
      return;
    }
    if (!assignment.value.deadline) {
      ElMessage.warning('请设置截止时间');
      return;
    }
    if (questions.value.length === 0) {
      ElMessage.warning('请添加至少一道题目');
      return;
    }

    // 收集编辑器内容
    questions.value.forEach((question) => {
      if (editors.value[question.id]) {
        question.content = editors.value[question.id].root.innerHTML;
      }
      if (question.type === 'essay' && answerEditors.value[question.id]) {
        question.referenceAnswer =
          answerEditors.value[question.id].root.innerHTML;
      }
    });

    ElMessageBox.confirm(
      '确定要发布该作业吗？发布后将通知所有选课学生。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    ).then(async () => {
      const data = {
        ...assignment.value,
        questions: questions.value,
        totalScore: totalScore.value,
        status: 'published', // 设置为已发布状态
      };

      // 如果是编辑模式，使用 PUT 更新；否则使用 POST 创建
      if (assignmentId.value) {
        await request.put(`/teacher/assignments/${assignmentId.value}`, data);
      } else {
        await request.post('/teacher/assignments', data);
      }

      ElMessage.success('作业发布成功');
      router.push('/teacher/courses');
    });
  } catch (error) {
    ElMessage.error('发布作业失败');
  }
};

const previewAssignment = () => {
  // 预览模式，实际项目中应该打开预览页面
  ElMessage.info('预览模式功能开发中');
};

const initQuillEditors = () => {
  questions.value.forEach((question) => {
    if (!editors.value[question.id] && editorRefs.value[question.id]) {
      const editor = new Quill(editorRefs.value[question.id], {
        theme: 'snow',
        modules: {
          toolbar: [
            ['bold', 'italic', 'underline', 'strike'],
            ['blockquote', 'code-block'],
            [{ header: 1 }, { header: 2 }],
            [{ list: 'ordered' }, { list: 'bullet' }],
            [{ color: [] }, { background: [] }],
            ['image'],
            ['clean'],
          ],
          syntax: true,
        },
        placeholder: '请输入题目内容...',
      });
      editors.value[question.id] = editor;
    }

    if (
      question.type === 'essay' &&
      !answerEditors.value[question.id] &&
      answerEditorRefs.value[question.id]
    ) {
      const editor = new Quill(answerEditorRefs.value[question.id], {
        theme: 'snow',
        modules: {
          toolbar: [
            ['bold', 'italic', 'underline', 'strike'],
            ['blockquote', 'code-block'],
            [{ header: 1 }, { header: 2 }],
            [{ list: 'ordered' }, { list: 'bullet' }],
            [{ color: [] }, { background: [] }],
            ['image'],
            ['clean'],
          ],
          syntax: true,
        },
        placeholder: '请输入参考答案...',
      });
      answerEditors.value[question.id] = editor;
    }
  });
};

onMounted(() => {
  if (assignmentId.value && assignmentId.value !== 'create') {
    getAssignmentDetail(assignmentId.value);
  }

  nextTick(() => {
    initQuillEditors();
  });
});
</script>

<style scoped>
.teacher-assignment-edit {
  padding: 24px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  margin: 16px;
  min-height: calc(100vh - 84px);
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 通用卡片样式 */
:deep(.el-card) {
  border: none;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: none;
  background: transparent;
  overflow: hidden;
}

:deep(.el-card__body) {
  padding: 0 20px 20px;
  overflow: hidden;
}

/* 配置卡片 */
.config-section {
  margin-bottom: 0;
}

.questions-section {
  margin-bottom: 0;
}

.questions-section :deep(.el-card__header) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 卡片标题 */
.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-title__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.card-title__icon .el-icon {
  font-size: 18px;
}

.card-title__text {
  color: #303133;
}

/* 表单样式 */
.config-form {
  padding: 10px 0;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 8px;
  padding: 10px 24px;
  font-size: 14px;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.el-button--primary:hover) {
  opacity: 0.9;
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
  border: none;
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  border: none;
}

/* 空题目区域 */
.empty-questions {
  text-align: center;
  padding: 60px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  background: linear-gradient(135deg, #fafafa 0%, #f5f7fa 100%);
  border-radius: 12px;
  margin: 20px 0;
}

.empty-questions :deep(.el-empty__description) {
  color: #909399;
  font-size: 16px;
}

/* 题目卡片 */
.question-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  background: #fafafa;
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.question-number {
  font-weight: 600;
  margin-right: 16px;
  font-size: 16px;
  color: #303133;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.option-label {
  width: 28px;
  margin-right: 12px;
  font-weight: 600;
  color: #303133;
}

.option-item .el-button {
  margin-left: 12px;
}

.option-hint {
  font-size: 13px;
  color: #909399;
  margin-top: 12px;
}

.score-hint {
  margin-left: 16px;
  font-size: 14px;
  color: #909399;
}

.word-limit {
  display: flex;
  align-items: center;
  gap: 12px;
}

.word-limit-separator {
  font-size: 16px;
  color: #909399;
}

.word-limit-hint {
  margin-left: 16px;
  font-size: 14px;
  color: #909399;
}

.answer-hint {
  font-size: 13px;
  color: #909399;
  margin-top: 12px;
}

.rich-text-editor {
  margin-bottom: 16px;
}

.quill-editor {
  min-height: 120px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
}

.total-score {
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
  text-align: right;
  padding: 20px 0;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
}

/* 题型选择弹窗 */
.question-type-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.question-type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.question-type-item:hover {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
  transform: translateY(-2px);
}

.type-icon {
  font-size: 36px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 12px;
}

.type-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .teacher-assignment-edit {
    padding: 16px;
  }

  .welcome-title {
    font-size: 36px;
  }

  .welcome-subtitle {
    font-size: 18px;
  }

  .questions-section :deep(.el-card__header) {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    display: flex;
    gap: 8px;
  }

  .action-buttons {
    flex-direction: column;
    align-items: stretch;
  }

  .question-type-list {
    grid-template-columns: 1fr;
  }
}
</style>
