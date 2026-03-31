<template>
  <div class="admin-assignment-edit">
    <el-card shadow="never" class="page-header">
      <template #header>
        <div class="card-header">
          <div class="card-header__title">
            <div class="card-header__icon">
              <el-icon><Notebook /></el-icon>
            </div>
            <span>{{ isEdit ? '编辑作业' : '发布作业' }}</span>
          </div>
        </div>
      </template>
      <el-form :model="assignment" label-width="100px" :rules="assignmentRules" ref="assignmentFormRef">
        <el-form-item label="作业标题" prop="title">
          <el-input v-model="assignment.title" placeholder="请输入作业标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="assignment.courseId" placeholder="选择课程" style="width: 100%" @change="handleCourseChange">
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="总分值">
          <el-input v-model="totalScoreDisplay" disabled style="width: 120px" />
          <span class="score-hint">（自动累加题目分值）</span>
        </el-form-item>
        <el-form-item label="补充说明" prop="description">
          <el-input
            v-model="assignment.description"
            type="textarea"
            rows="4"
            placeholder="请输入补充说明（可选）"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline">
          <el-date-picker
            v-model="assignment.deadline"
            type="datetime"
            placeholder="选择截止时间"
            style="width: 100%"
            :disabled-date="disabledDate"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="mt-4">
      <template #header>
        <div class="card-header">
          <span class="section-title">题目配置</span>
          <el-button type="success" @click="openQuestionTypeDialog">
            <el-icon><Plus /></el-icon> 添加题目
          </el-button>
        </div>
      </template>
      
      <div v-if="questions.length === 0" class="empty-questions">
        <el-empty description="暂无题目">
          <el-button type="success" @click="openQuestionTypeDialog">
            <el-icon><Plus /></el-icon> 添加题目
          </el-button>
        </el-empty>
      </div>
      
      <div v-else class="questions-container">
        <div v-for="(question, index) in questions" :key="question.tempId" class="question-card">
          <div class="question-header">
            <div class="question-title">
              <span class="question-number">第{{ index + 1 }}题</span>
              <el-tag size="small" :type="getQuestionTypeTag(question.type)">
                {{ getQuestionTypeText(question.type) }}
              </el-tag>
            </div>
            <div class="question-actions">
              <el-button type="danger" size="small" @click="removeQuestion(index)">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </div>
          </div>
          
          <el-form :model="question" label-width="90px" class="question-form">
            <el-form-item label="题目内容" required>
              <div class="rich-text-editor-wrapper">
                <div :ref="(el) => setEditorRef(el, question.tempId)" class="quill-editor"></div>
                <div class="word-count" v-if="question.type === 'essay'">
                  字数：{{ getWordCount(question.tempId) }} / {{ question.minWords || '∞' }} - {{ question.maxWords || '∞' }}
                </div>
              </div>
            </el-form-item>
            
            <el-form-item label="选项" v-if="question.type === 'single' || question.type === 'multiple'">
              <div class="options-container">
                <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-item">
                  <el-radio v-if="question.type === 'single'" v-model="question.correctAnswer" :label="String.fromCharCode(65 + optIndex)" />
                  <el-checkbox v-else v-model="question.correctAnswers" :label="String.fromCharCode(65 + optIndex)" />
                  <span class="option-label">{{ String.fromCharCode(65 + optIndex) }}.</span>
                  <el-input
                    v-model="option.value"
                    placeholder="请输入选项内容"
                    class="option-input"
                  />
                  <el-button
                    type="danger"
                    size="small"
                    circle
                    @click="removeOption(question, optIndex)"
                    :disabled="question.options.length <= 3"
                  >
                    <el-icon><Close /></el-icon>
                  </el-button>
                </div>
                <el-button
                  type="primary"
                  size="small"
                  @click="addOption(question)"
                  :disabled="question.options.length >= 9"
                >
                  <el-icon><Plus /></el-icon> 添加选项
                </el-button>
                <div class="option-hint">
                  当前{{ question.options.length }}个选项（最少3个，最多9个）
                </div>
              </div>
            </el-form-item>
            
            <el-form-item label="标准答案" required v-if="question.type === 'judgment'">
              <el-radio-group v-model="question.correctAnswer">
                <el-radio label="true">正确</el-radio>
                <el-radio label="false">错误</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="分值" required>
              <el-input-number
                v-model="question.score"
                :min="1"
                :max="100"
                :precision="0"
                controls-position="right"
                style="width: 120px"
              />
              <span class="score-unit">分</span>
            </el-form-item>
            
            <template v-if="question.type === 'essay'">
              <el-form-item label="字数限制">
                <div class="word-limit">
                  <el-input-number
                    v-model="question.minWords"
                    :min="0"
                    :max="10000"
                    :precision="0"
                    controls-position="right"
                    placeholder="最少"
                    style="width: 120px"
                  />
                  <span class="word-limit-separator">至</span>
                  <el-input-number
                    v-model="question.maxWords"
                    :min="0"
                    :max="10000"
                    :precision="0"
                    controls-position="right"
                    placeholder="最多"
                    style="width: 120px"
                  />
                  <span class="word-limit-hint">字（0表示无限制）</span>
                </div>
              </el-form-item>
              <el-form-item label="参考答案">
                <div class="rich-text-editor-wrapper">
                  <div :ref="(el) => setAnswerEditorRef(el, question.tempId)" class="quill-editor"></div>
                </div>
                <div class="answer-hint">
                  <el-icon><InfoFilled /></el-icon>
                  AI大模型将基于参考答案抓取关键词及逻辑关系进行打分，并生成"错误点标注+通俗修正建议"的评语
                </div>
              </el-form-item>
            </template>
          </el-form>
        </div>
      </div>
      
      <div class="add-question-btn" v-if="questions.length > 0">
        <el-button type="success" @click="openQuestionTypeDialog">
          <el-icon><Plus /></el-icon> 添加题目
        </el-button>
      </div>
      
      <div class="total-score">
        <span>作业总分：{{ totalScore }}分</span>
        <span class="question-count">共{{ questions.length }}道题目</span>
      </div>
    </el-card>

    <div class="action-buttons mt-4">
      <el-button @click="previewAssignment" :disabled="!canPreview">
        <el-icon><View /></el-icon> 预览模式
      </el-button>
      <el-button type="primary" @click="publishNow" :disabled="!canPublish">
        <el-icon><Promotion /></el-icon> 现在发布
      </el-button>
    </div>

    <el-dialog
      v-model="questionTypeDialogVisible"
      title="选择题型"
      width="500px"
      :close-on-click-modal="false"
      append-to-body
      destroy-on-close
    >
      <div class="question-type-list">
        <div
          v-for="type in questionTypes"
          :key="type.value"
          class="question-type-item"
          @click="addQuestionByType(type.value)"
        >
          <div class="type-icon-wrapper" :style="{ backgroundColor: type.color }">
            <el-icon class="type-icon"><component :is="type.icon" /></el-icon>
          </div>
          <div class="type-info">
            <span class="type-name">{{ type.label }}</span>
            <span class="type-desc">{{ type.description }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="questionTypeDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="previewDialogVisible"
      :title="'预览: ' + (assignment.title || '作业预览')"
      width="800px"
      top="5vh"
      append-to-body
      destroy-on-close
    >
      <div class="preview-content" v-if="previewData">
        <div class="preview-header">
          <h2>{{ previewData.title }}</h2>
          <div class="preview-meta">
            <span>课程：{{ previewData.courseName }}</span>
            <span>总分：{{ previewData.totalScore }}分</span>
            <span>截止时间：{{ formatDateTime(previewData.deadline) }}</span>
          </div>
        </div>
        <div class="preview-description" v-if="previewData.description">
          <h4>作业说明</h4>
          <p>{{ previewData.description }}</p>
        </div>
        <div class="preview-questions">
          <div v-for="(q, idx) in previewData.questions" :key="idx" class="preview-question">
            <div class="q-title">
              <span class="q-number">{{ Number(idx) + 1 }}.</span>
              <span class="q-type">[{{ getQuestionTypeText(q.type) }}]</span>
              <span class="q-score">({{ q.score }}分)</span>
              <div class="q-content" v-html="q.content"></div>
            </div>
            <div class="question-options" v-if="q.type === 'single' || q.type === 'multiple'">
              <div v-for="(opt, optIdx) in q.options" :key="optIdx" class="option">
                {{ String.fromCharCode(65 + (typeof optIdx === 'number' ? optIdx : parseInt(optIdx))) }}. {{ opt.value }}
              </div>
              <div class="question-answer">
                正确答案：{{ q.type === 'single' ? q.correctAnswer : q.correctAnswers.join(', ') }}
              </div>
            </div>
            <div class="question-answer" v-if="q.type === 'judgment'">
              答案：{{ q.correctAnswer === 'true' ? '正确' : '错误' }}
            </div>
            <div class="question-essay" v-if="q.type === 'essay'">
              <div v-if="q.minWords || q.maxWords">字数要求：{{ q.minWords || 0 }} - {{ q.maxWords || '无限制' }}字</div>
              <div v-if="q.referenceAnswer" class="reference-answer">
                <h5>参考答案：</h5>
                <div v-html="q.referenceAnswer"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  Plus, Delete, Close, View, Promotion, 
  InfoFilled, Check, Edit, List, Notebook
} from '@element-plus/icons-vue';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';
import request from '@/utils/request';

const route = useRoute();
const router = useRouter();
const assignmentId = computed(() => route.params.id as string);
const isEdit = computed(() => !!assignmentId.value);

interface Question {
  tempId: number;
  type: string;
  content: string;
  options: { value: string }[];
  correctAnswer: string;
  correctAnswers: string[];
  score: number;
  minWords: number;
  maxWords: number;
  referenceAnswer: string;
}

const assignment = ref({
  id: null as number | null,
  title: '',
  courseId: null as number | null,
  description: '',
  deadline: '',
  courseName: ''
});

const assignmentRules = {
  title: [{ required: true, message: '请输入作业标题', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择所属课程', trigger: 'change' }],
  deadline: [{ required: true, message: '请设置截止时间', trigger: 'change' }]
};

const assignmentFormRef = ref();
const courses = ref<{id: number; name: string}[]>([]);
const questions = ref<Question[]>([]);

const questionTypes = [
  { label: '单选题', value: 'single', icon: Check, color: '#409eff', description: '从多个选项中选择一个正确答案' },
  { label: '多选题', value: 'multiple', icon: List, color: '#67c23a', description: '从多个选项中选择一个或多个正确答案' },
  { label: '判断题', value: 'judgment', icon: Check, color: '#e6a23c', description: '判断陈述内容的对错' },
  { label: '主观题', value: 'essay', icon: Edit, color: '#f56c6c', description: '需要详细文字作答的题目' }
];

const questionTypeDialogVisible = ref(false);
const previewDialogVisible = ref(false);

const editorRefs = ref<Record<number, any>>({});
const answerEditorRefs = ref<Record<number, any>>({});
const editors = ref<Record<number, Quill>>({});
const answerEditors = ref<Record<number, Quill>>({});

const previewData = ref<any>(null);

const totalScore = computed(() => {
  return questions.value.reduce((sum, q) => sum + (q.score || 0), 0);
});

const totalScoreDisplay = computed(() => `${totalScore.value}分`);

const canPreview = computed(() => assignment.value.title && questions.value.length > 0);
const canSave = computed(() => assignment.value.title && assignment.value.courseId && assignment.value.deadline);
const canPublish = computed(() => {
  if (!canSave.value || questions.value.length === 0) return false;
  return questions.value.every(q => q.content && q.score > 0);
});

const getQuestionTypeText = (type: string) => {
  const map: Record<string, string> = {
    single: '单选题', multiple: '多选题', judgment: '判断题', essay: '主观题'
  };
  return map[type] || type;
};

const getQuestionTypeTag = (type: string) => {
  const map: Record<string, string> = {
    single: '', multiple: 'success', judgment: 'warning', essay: 'danger'
  };
  return map[type] || '';
};

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7;
};

const setEditorRef = (el: any, tempId: number) => {
  if (el) editorRefs.value[tempId] = el;
};

const setAnswerEditorRef = (el: any, tempId: number) => {
  if (el) answerEditorRefs.value[tempId] = el;
};

const getWordCount = (tempId: number) => {
  const editor = editors.value[tempId];
  if (!editor) return 0;
  const text = editor.getText();
  return text.trim().length;
};

const getCourses = async () => {
  try {
    const response = await request.get('/admin/courses', { params: { page: 1, size: 100 } });
    courses.value = response.data?.records || [];
  } catch (error) {
    console.error('获取课程列表失败:', error);
  }
};

const handleCourseChange = (courseId: number) => {
  const course = courses.value.find(c => c.id === courseId);
  if (course) {
    assignment.value.courseName = course.name;
  }
};

const openQuestionTypeDialog = () => {
  questionTypeDialogVisible.value = true;
};

const addQuestionByType = (type: string) => {
  const tempId = Date.now();
  const newQuestion: Question = {
    tempId,
    type,
    content: '',
    options: (type === 'single' || type === 'multiple') 
      ? [{ value: '' }, { value: '' }, { value: '' }] 
      : [],
    correctAnswer: '',
    correctAnswers: [],
    score: 10,
    minWords: 0,
    maxWords: 0,
    referenceAnswer: ''
  };
  questions.value.push(newQuestion);
  questionTypeDialogVisible.value = false;
  
  nextTick(() => {
    initQuillEditor(tempId, type);
  });
};

const initQuillEditor = (tempId: number, type: string) => {
  if (!editorRefs.value[tempId]) return;
  
  const toolbarOptions = [
    ['bold', 'italic', 'underline', 'strike'],
    [{ 'list': 'ordered' }, { 'list': 'bullet' }],
    ['clean']
  ];
  
  const editor = new Quill(editorRefs.value[tempId], {
    theme: 'snow',
    modules: {
      toolbar: {
        container: toolbarOptions,
        handlers: {
          image: () => handleImageUpload(tempId)
        }
      }
    },
    placeholder: '请输入题目内容...'
  });
  
  editors.value[tempId] = editor;
  
  editor.on('text-change', () => {
    const q = questions.value.find(q => q.tempId === tempId);
    if (q) q.content = editor.root.innerHTML;
  });
  
  if (type === 'essay') {
    initAnswerEditor(tempId);
  }
};

const initAnswerEditor = (tempId: number) => {
  if (!answerEditorRefs.value[tempId]) return;
  
  const editor = new Quill(answerEditorRefs.value[tempId], {
    theme: 'snow',
    modules: {
      toolbar: [
        ['bold', 'italic', 'underline', 'strike'],
        [{ 'list': 'ordered' }, { 'list': 'bullet' }],
        ['clean']
      ]
    },
    placeholder: '请输入参考答案（可选）...'
  });
  
  answerEditors.value[tempId] = editor;
  
  editor.on('text-change', () => {
    const q = questions.value.find(q => q.tempId === tempId);
    if (q) q.referenceAnswer = editor.root.innerHTML;
  });
};

const handleImageUpload = (tempId: number) => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.onchange = async () => {
    const file = input.files?.[0];
    if (file) {
      try {
        const formData = new FormData();
        formData.append('file', file);
        const response = await request.post('/upload/image', formData);
        const imageUrl = response.data?.url;
        if (imageUrl && editors.value[tempId]) {
          const range = editors.value[tempId].getSelection();
          editors.value[tempId].insertEmbed(range?.index || 0, 'image', imageUrl);
        }
      } catch (error) {
        ElMessage.error('图片上传失败');
      }
    }
  };
  input.click();
};

const removeQuestion = (index: number) => {
  ElMessageBox.confirm('确定要删除该题目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const q = questions.value[index];
    if (q && editors.value[q.tempId]) {
      delete editors.value[q.tempId];
    }
    questions.value.splice(index, 1);
  });
};

const addOption = (question: Question) => {
  if (question.options.length < 9) {
    question.options.push({ value: '' });
  }
};

const removeOption = (question: Question, index: number) => {
  if (question.options.length > 3) {
    question.options.splice(index, 1);
  }
};

const previewAssignment = async () => {
  questions.value.forEach(q => {
    if (editors.value[q.tempId]) {
      q.content = editors.value[q.tempId].root.innerHTML;
    }
  });
  
  previewData.value = {
    title: assignment.value.title,
    courseName: assignment.value.courseName,
    description: assignment.value.description,
    deadline: assignment.value.deadline,
    totalScore: totalScore.value,
    questions: questions.value.map(q => ({
      ...q,
      options: q.options
    }))
  };
  previewDialogVisible.value = true;
};

const publishNow = async () => {
  try {
    await assignmentFormRef.value?.validate();
    
    if (questions.value.length === 0) {
      ElMessage.warning('请添加至少一道题目');
      return;
    }
    
    questions.value.forEach(q => {
      if (editors.value[q.tempId]) {
        q.content = editors.value[q.tempId].root.innerHTML;
      }
      if (q.type === 'essay' && answerEditors.value[q.tempId]) {
        q.referenceAnswer = answerEditors.value[q.tempId].root.innerHTML;
      }
    });
    
    for (let i = 0; i < questions.value.length; i++) {
      const question = questions.value[i];
      const questionNum = i + 1;
      
      const contentText = question.content?.replace(/<[^>]+>/g, '').trim() || '';
      if (!contentText) {
        ElMessage.warning(`第${questionNum}题题目内容为空，请填写完整`);
        return;
      }
      
      if (question.type === 'single' || question.type === 'multiple') {
        const emptyOptions = question.options?.filter(
          (opt: any) => !opt.value || opt.value.trim() === ''
        );
        if (emptyOptions?.length > 0) {
          ElMessage.warning(`第${questionNum}题存在空白选项，请填写完整`);
          return;
        }
        if (question.type === 'single' && !question.correctAnswer) {
          ElMessage.warning(`第${questionNum}题未设置正确答案`);
          return;
        }
        if (question.type === 'multiple' && (!question.correctAnswers || question.correctAnswers.length === 0)) {
          ElMessage.warning(`第${questionNum}题未设置正确答案`);
          return;
        }
      }
      
      if (question.type === 'judgment') {
        if (!question.correctAnswer) {
          ElMessage.warning(`第${questionNum}题未设置正确答案`);
          return;
        }
      }
      
      if (question.type === 'essay') {
        const answerText = question.referenceAnswer?.replace(/<[^>]+>/g, '').trim() || '';
        if (!answerText) {
          ElMessage.warning(`第${questionNum}题参考答案为空，请填写完整`);
          return;
        }
      }
    }
    
    const formattedQuestions = questions.value.map((q, index) => {
      let answer = q.correctAnswer || '';
      if (q.type === 'multiple' && q.correctAnswers && q.correctAnswers.length > 0) {
        answer = q.correctAnswers.join(',');
      }
      
      return {
        type: q.type,
        content: q.content,
        options: q.options ? q.options.map(opt => opt.value) : [],
        answer: answer,
        referenceAnswer: q.referenceAnswer || '',
        score: q.score,
        minWords: q.minWords || 0,
        maxWords: q.maxWords || 0,
        sortOrder: index + 1
      };
    });
    
    const data = {
      title: assignment.value.title,
      description: assignment.value.description,
      courseId: assignment.value.courseId,
      deadline: assignment.value.deadline,
      totalScore: totalScore.value,
      questions: formattedQuestions
    };
    
    if (isEdit.value) {
      await request.put('/admin/assignments', {
        id: assignmentId.value,
        ...data
      });
      ElMessage.success('作业更新成功！');
    } else {
      await request.post('/admin/assignments', data);
      ElMessage.success('作业发布成功！');
    }
    
    router.push('/admin/assignments');
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(isEdit.value ? '更新失败: ' + (error.message || '未知错误') : '发布失败: ' + (error.message || '未知错误'));
    }
  }
};

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', month: '2-digit', day: '2-digit', 
    hour: '2-digit', minute: '2-digit', second: '2-digit' 
  });
};

const getAssignmentDetail = async () => {
  if (!isEdit.value) return;
  
  try {
    const response = await request.get(`/admin/assignments/${assignmentId.value}`);
    const data = response.data;
    
    if (data) {
      assignment.value = {
        id: data.id,
        title: data.title,
        courseId: data.courseId,
        description: data.description,
        deadline: data.deadline,
        courseName: data.courseName
      };
      
      if (data.questions && data.questions.length > 0) {
        questions.value = data.questions.map((q: any, index: number) => ({
          tempId: Date.now() + index,
          type: q.type,
          content: q.content,
          options: q.options ? q.options.map((opt: string) => ({ value: opt })) : [],
          correctAnswer: q.answer,
          correctAnswers: q.type === 'multiple' ? q.answer.split(',') : [],
          score: q.score,
          minWords: q.minWords || 0,
          maxWords: q.maxWords || 0,
          referenceAnswer: q.referenceAnswer || ''
        }));
      }
    }
  } catch (error) {
    console.error('获取作业详情失败:', error);
    ElMessage.error('获取作业详情失败');
  }
};

onMounted(async () => {
  await getCourses();
  await getAssignmentDetail();
  await nextTick();
  
  if (questions.value.length > 0) {
    questions.value.forEach((q: Question) => {
      nextTick(() => {
        initQuillEditor(q.tempId, q.type);
        if (q.content) {
          nextTick(() => {
            const editor = editors.value[q.tempId];
            if (editor) {
              editor.root.innerHTML = q.content;
            }
          });
        }
        if (q.type === 'essay' && q.referenceAnswer) {
          initAnswerEditor(q.tempId, q.referenceAnswer);
        }
      });
    });
  }
});
</script>

<style scoped>
.admin-assignment-edit {
  padding: 24px;
  padding-bottom: 100px;
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

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.mt-4 {
  margin-top: 20px;
}

.score-hint {
  margin-left: 12px;
  font-size: 14px;
  color: #909399;
}

.empty-questions {
  text-align: center;
  padding: 60px 0;
}

.questions-container {
  max-height: none;
}

.question-card {
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.question-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.question-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-number {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.question-form {
  margin-top: 16px;
}

.rich-text-editor-wrapper {
  width: 100%;
}

.quill-editor {
  min-height: 120px;
  background: white;
  border-radius: 8px;
}

.quill-editor:deep(.ql-editor) {
  min-height: 100px;
}

.word-count {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-label {
  font-weight: 500;
  min-width: 24px;
}

.option-input {
  flex: 1;
}

.option-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.score-unit {
  margin-left: 8px;
  color: #606266;
}

.word-limit {
  display: flex;
  align-items: center;
  gap: 8px;
}

.word-limit-separator {
  color: #606266;
}

.word-limit-hint {
  margin-left: 8px;
  font-size: 14px;
  color: #909399;
}

.answer-hint {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fdf6ec;
  border-radius: 8px;
  font-size: 12px;
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 6px;
}

.add-question-btn {
  text-align: center;
  padding: 20px;
}

.total-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgb(15, 38, 70) 0%, rgb(58, 97, 156) 50%, rgb(11, 17, 27) 100%);
  border-radius: 12px;
  margin-top: 20px;
  font-size: 16px;
  font-weight: 500;
  color: #fff;
}

.question-count {
  font-size: 14px;
  opacity: 0.9;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  background: white;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.1);
  z-index: 99;
}

.question-type-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.question-type-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.question-type-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.type-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-icon-wrapper .type-icon {
  font-size: 24px;
  color: white;
}

.type-info {
  flex: 1;
}

.type-name {
  display: block;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.type-desc {
  font-size: 12px;
  color: #909399;
}

.preview-content {
  padding: 20px;
}

.preview-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #ebeef5;
}

.preview-header h2 {
  margin-bottom: 12px;
  color: #303133;
}

.preview-meta {
  display: flex;
  justify-content: center;
  gap: 24px;
  font-size: 14px;
  color: #606266;
}

.preview-description {
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.preview-description h4 {
  margin-bottom: 8px;
  color: #303133;
}

.preview-question {
  margin-bottom: 24px;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.q-title {
  margin-bottom: 12px;
}

.q-number {
  font-weight: 600;
  margin-right: 8px;
}

.q-type {
  color: #909399;
  margin-right: 8px;
}

.q-score {
  color: #f56c6c;
}

.q-content {
  margin-top: 8px;
  padding: 8px;
  background: #fafafa;
  border-radius: 4px;
}

.question-options {
  margin-top: 12px;
  padding-left: 20px;
}

.question-options .option {
  padding: 6px 0;
}

.question-answer {
  margin-top: 12px;
  padding: 8px;
  background: #ecf5ff;
  border-radius: 4px;
}

.reference-answer {
  margin-top: 12px;
  padding: 12px;
  background: #f0f9ff;
  border-left: 3px solid #409eff;
  border-radius: 4px;
}

.reference-answer h5 {
  margin: 0 0 8px 0;
  color: #409eff;
  font-size: 14px;
}

.reference-answer > div {
  color: #303133;
  line-height: 1.6;
}

.question-essay {
  margin-top: 12px;
  font-size: 14px;
  color: #606266;
}

@media (max-width: 768px) {
  .admin-assignment-edit {
    padding: 16px;
  }
  
  .question-type-list {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    flex-wrap: wrap;
  }
}
</style>
