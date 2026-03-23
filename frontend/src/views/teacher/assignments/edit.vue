<template>
  <div class="teacher-assignment-edit">
    <!-- 右侧草稿箱图标 -->
    <div class="draft-box-icon" @click="openDraftBox">
      <el-icon class="icon"><Document /></el-icon>
      <el-badge v-if="drafts.length > 0" :value="drafts.length" />
    </div>

    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑作业' : '创建作业' }}</span>
        </div>
      </template>
      <el-form :model="assignment" label-width="100px">
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
          <el-input v-model="totalScore" disabled style="width: 100px" />
          <span class="score-hint">（自动累加题目分值）</span>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input
            v-model="assignment.description"
            type="textarea"
            rows="3"
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

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span>题目配置</span>
          <div>
            <el-button type="primary" @click="openQuestionBankDialog">从题库选择</el-button>
            <el-button type="success" @click="openQuestionTypeDialog">添加题目</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="questions.length === 0" class="empty-questions">
        <el-button type="success" @click="openQuestionTypeDialog">
          <el-icon><Plus /></el-icon> 添加题目
        </el-button>
      </div>
      
      <div v-else v-for="(question, index) in questions" :key="question.id" class="question-card">
        <div class="question-header">
          <span class="question-number">{{ index + 1 }}.</span>
          <el-select v-model="question.type" placeholder="选择题型" style="width: 120px" @change="handleQuestionTypeChange(question)">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judgment" />
            <el-option label="主观题" value="essay" />
          </el-select>
          <el-button
            type="danger"
            size="small"
            @click="removeQuestion(index)"
          >
            删除
          </el-button>
        </div>
        
        <el-form :model="question" label-width="80px" class="mt-2">
          <el-form-item label="题目内容" required>
            <div class="rich-text-editor">
              <div ref="editorRefs[question.id]" class="quill-editor"></div>
            </div>
          </el-form-item>
          
          <el-form-item label="选项" v-if="['single', 'multiple'].includes(question.type)">
            <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-item">
              <span class="option-label">{{ String.fromCharCode(65 + optIndex) }}.</span>
              <el-input
                v-model="option.value"
                placeholder="请输入选项内容"
                style="width: 80%"
              />
              <el-button
                type="danger"
                size="small"
                @click="removeOption(question, optIndex)"
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
          
          <el-form-item label="标准答案" required v-if="['single', 'multiple', 'judgment'].includes(question.type)">
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
                <div ref="answerEditorRefs[question.id]" class="quill-editor"></div>
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
      <el-button @click="saveDraft">预设发布</el-button>
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

    <!-- 题库选择弹窗 -->
    <el-dialog
      v-model="questionBankDialogVisible"
      title="从题库选择题目"
      width="800px"
    >
      <el-table :data="questionBank" style="width: 100%">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="题目ID" width="80" />
        <el-table-column prop="type" label="题型" width="100">
          <template #default="scope">
            {{ getQuestionTypeText(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" />
        <el-table-column prop="score" label="分值" width="80" />
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="questionBankDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmSelectQuestions">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 草稿箱弹窗 -->
    <el-dialog
      v-model="draftBoxDialogVisible"
      title="草稿箱"
      width="600px"
    >
      <el-empty v-if="drafts.length === 0" description="暂无草稿" />
      <div v-else class="draft-list">
        <el-card
          v-for="draft in drafts"
          :key="draft.id"
          class="draft-item"
          @click="loadDraft(draft.id)"
        >
          <div class="draft-header">
            <span class="draft-title">{{ draft.title }}</span>
            <span class="draft-course">{{ draft.courseName }}</span>
          </div>
          <div class="draft-info">
            <span class="draft-time">保存时间：{{ formatDate(draft.saveTime) }}</span>
            <span class="draft-questions">{{ draft.questionCount }}道题目</span>
          </div>
        </el-card>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="draftBoxDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Document, Plus, Check, Delete, Edit } from '@element-plus/icons-vue';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';

const route = useRoute();
const router = useRouter();
const assignmentId = computed(() => route.params.id as string);
const isEdit = computed(() => !!assignmentId.value);

const assignment = ref({
  id: '',
  title: '',
  courseId: '',
  description: '',
  deadline: ''
});

const courses = ref([
  { id: 1, name: '计算机导论' },
  { id: 2, name: '数据结构' },
  { id: 3, name: '算法设计与分析' }
]);

const questions = ref([]);
const questionBank = ref([
  {
    id: 1,
    type: 'single',
    content: '计算机的基本组成部分不包括以下哪项？',
    options: ['CPU', '内存', '硬盘', '打印机'],
    correctAnswer: 'D',
    score: 10
  },
  {
    id: 2,
    type: 'multiple',
    content: '以下哪些是计算机的输入设备？',
    options: ['键盘', '鼠标', '显示器', '打印机'],
    correctAnswer: 'A,B',
    score: 15
  },
  {
    id: 3,
    type: 'judgment',
    content: '计算机病毒是一种程序。',
    correctAnswer: 'true',
    score: 5
  }
]);

const drafts = ref([
  {
    id: 1,
    title: '第一章 计算机基础作业',
    courseName: '计算机导论',
    saveTime: new Date(Date.now() - 3600000).toISOString(),
    questionCount: 3
  },
  {
    id: 2,
    title: '第二章 数据结构作业',
    courseName: '数据结构',
    saveTime: new Date(Date.now() - 7200000).toISOString(),
    questionCount: 2
  }
]);

const questionTypes = [
  { label: '单选题', value: 'single', icon: Check },
  { label: '多选题', value: 'multiple', icon: Check },
  { label: '判断题', value: 'judgment', icon: Check },
  { label: '主观题', value: 'essay', icon: Edit }
];

const questionTypeDialogVisible = ref(false);
const questionBankDialogVisible = ref(false);
const draftBoxDialogVisible = ref(false);

const editorRefs = ref({});
const answerEditorRefs = ref({});
const editors = ref({});
const answerEditors = ref({});
const autoSaveTimer = ref(null);

const totalScore = computed(() => {
  return questions.value.reduce((sum, question) => sum + (question.score || 0), 0);
});

const getQuestionTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '主观题'
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
    options: type === 'single' || type === 'multiple' ? [{ value: '' }, { value: '' }, { value: '' }] : [],
    correctAnswer: '',
    score: 10,
    minWords: 0,
    maxWords: 0,
    referenceAnswer: ''
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
    type: 'warning'
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
  questionBankDialogVisible.value = true;
};

const confirmSelectQuestions = () => {
  questionBankDialogVisible.value = false;
  ElMessage.success('题目添加成功');
};

const openDraftBox = () => {
  draftBoxDialogVisible.value = true;
};

const loadDraft = (draftId: number) => {
  ElMessageBox.confirm('你将查看该草稿，当前页面未保存内容可能丢失，是否继续？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 模拟加载草稿
    ElMessage.success('草稿加载成功');
    draftBoxDialogVisible.value = false;
  });
};

const saveDraft = async () => {
  try {
    // 收集编辑器内容
    questions.value.forEach(question => {
      if (editors.value[question.id]) {
        question.content = editors.value[question.id].root.innerHTML;
      }
      if (question.type === 'essay' && answerEditors.value[question.id]) {
        question.referenceAnswer = answerEditors.value[question.id].root.innerHTML;
      }
    });
    
    // 实际项目中调用接口
    ElMessage.success('草稿保存成功');
  } catch (error) {
    ElMessage.error('保存草稿失败');
  }
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
    questions.value.forEach(question => {
      if (editors.value[question.id]) {
        question.content = editors.value[question.id].root.innerHTML;
      }
      if (question.type === 'essay' && answerEditors.value[question.id]) {
        question.referenceAnswer = answerEditors.value[question.id].root.innerHTML;
      }
    });
    
    ElMessageBox.confirm('确定要发布作业吗？发布后将通知所有学生。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      // 实际项目中调用接口
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
  questions.value.forEach(question => {
    // 题目内容编辑器
    if (!editors.value[question.id] && editorRefs.value[question.id]) {
      const editor = new Quill(editorRefs.value[question.id], {
        theme: 'snow',
        modules: {
          toolbar: [
            ['bold', 'italic', 'underline', 'strike'],
            ['blockquote', 'code-block'],
            [{ 'header': 1 }, { 'header': 2 }],
            [{ 'list': 'ordered' }, { 'list': 'bullet' }],
            [{ 'color': [] }, { 'background': [] }],
            ['image'],
            ['clean']
          ],
          syntax: true
        },
        placeholder: '请输入题目内容...'
      });
      editors.value[question.id] = editor;
    }
    
    // 参考答案编辑器（仅主观题）
    if (question.type === 'essay' && !answerEditors.value[question.id] && answerEditorRefs.value[question.id]) {
      const editor = new Quill(answerEditorRefs.value[question.id], {
        theme: 'snow',
        modules: {
          toolbar: [
            ['bold', 'italic', 'underline', 'strike'],
            ['blockquote', 'code-block'],
            [{ 'header': 1 }, { 'header': 2 }],
            [{ 'list': 'ordered' }, { 'list': 'bullet' }],
            [{ 'color': [] }, { 'background': [] }],
            ['image'],
            ['clean']
          ],
          syntax: true
        },
        placeholder: '请输入参考答案...'
      });
      answerEditors.value[question.id] = editor;
    }
  });
};

const startAutoSave = () => {
  autoSaveTimer.value = window.setInterval(() => {
    saveDraft();
  }, 60000); // 每分钟自动保存
};

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN');
};

onMounted(() => {
  startAutoSave();
  nextTick(() => {
    initQuillEditors();
  });
});

onUnmounted(() => {
  if (autoSaveTimer.value) {
    clearInterval(autoSaveTimer.value);
  }
});
</script>

<style scoped>
.teacher-assignment-edit {
  padding: 20px;
  position: relative;
}

.draft-box-icon {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 50px;
  height: 50px;
  background-color: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.draft-box-icon .icon {
  font-size: 24px;
  color: white;
}

.page-header {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-questions {
  text-align: center;
  padding: 40px 0;
}

.question-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 16px;
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.question-number {
  font-weight: 500;
  margin-right: 12px;
  width: 30px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.option-label {
  width: 20px;
  margin-right: 8px;
  font-weight: 500;
}

.option-item .el-button {
  margin-left: 12px;
}

.option-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.score-hint {
  margin-left: 12px;
  font-size: 14px;
  color: #909399;
}

.word-limit {
  display: flex;
  align-items: center;
  gap: 10px;
}

.word-limit-separator {
  font-size: 16px;
  color: #909399;
}

.word-limit-hint {
  margin-left: 12px;
  font-size: 14px;
  color: #909399;
}

.answer-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.rich-text-editor {
  margin-bottom: 12px;
}

.quill-editor {
  min-height: 100px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.total-score {
  font-size: 16px;
  font-weight: 500;
  color: #409eff;
  text-align: right;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.question-type-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.question-type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.question-type-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.type-icon {
  font-size: 32px;
  color: #409eff;
  margin-bottom: 10px;
}

.type-name {
  font-size: 16px;
  font-weight: 500;
}

.draft-list {
  max-height: 400px;
  overflow-y: auto;
}

.draft-item {
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.draft-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.draft-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.draft-title {
  font-weight: 500;
  font-size: 16px;
}

.draft-course {
  font-size: 14px;
  color: #909399;
}

.draft-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
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