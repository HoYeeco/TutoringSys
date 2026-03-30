<template>
  <div class="submit-page">
    <div class="submit-container">
      <div class="main-content">
        <div class="questions-wrapper">
          <div
            v-for="(question, index) in questions"
            :key="question.id"
            :id="`question-${question.id}`"
            class="question-card"
          >
            <div class="question-header">
              <div class="question-info">
                <span class="question-number">第{{ index + 1 }}题</span>
                <el-tag size="small" :type="getQuestionTypeTag(question.type)">
                  {{ getQuestionTypeText(question.type) }}
                </el-tag>
                <span class="question-score">({{ question.score }}分)</span>
              </div>
            </div>

            <div class="question-content" v-html="question.content"></div>

            <div class="question-answer">
              <template v-if="question.type === 'single'">
                <div class="answer-label">请选择正确答案：</div>
                <el-radio-group
                  v-model="answers[question.id]"
                  @change="handleAnswerChange(question.id)"
                >
                  <div class="options-list">
                    <div
                      v-for="(option, idx) in parseOptions(question.options)"
                      :key="idx"
                      class="option-item"
                    >
                      <el-radio :label="String.fromCharCode(65 + idx)">
                        <span class="option-label"
                          >{{ String.fromCharCode(65 + idx) }}.</span
                        >
                        <span class="option-text">{{ option }}</span>
                      </el-radio>
                    </div>
                  </div>
                </el-radio-group>
              </template>

              <template v-else-if="question.type === 'multiple'">
                <div class="answer-label">请选择所有正确答案：</div>
                <div class="select-all">
                  <el-checkbox
                    :model-value="isAllSelected(question.id, question.options)"
                    @change="
                      handleSelectAll(question.id, question.options, $event)
                    "
                  >
                    全选
                  </el-checkbox>
                </div>
                <el-checkbox-group
                  v-model="answers[question.id]"
                  @change="handleAnswerChange(question.id)"
                >
                  <div class="options-list">
                    <div
                      v-for="(option, idx) in parseOptions(question.options)"
                      :key="idx"
                      class="option-item"
                    >
                      <el-checkbox :label="String.fromCharCode(65 + idx)">
                        <span class="option-label"
                          >{{ String.fromCharCode(65 + idx) }}.</span
                        >
                        <span class="option-text">{{ option }}</span>
                      </el-checkbox>
                    </div>
                  </div>
                </el-checkbox-group>
              </template>

              <template v-else-if="question.type === 'judgment'">
                <div class="answer-label">请判断正误：</div>
                <div class="judgment-switch">
                  <span
                    class="judgment-label"
                    :class="{ active: answers[question.id] === 'true' }"
                    >正确</span
                  >
                  <el-switch
                    v-model="answers[question.id]"
                    active-value="true"
                    inactive-value="false"
                    active-color="#67c23a"
                    inactive-color="#f56c6c"
                    @change="handleAnswerChange(question.id)"
                  />
                  <span
                    class="judgment-label"
                    :class="{ active: answers[question.id] === 'false' }"
                    >错误</span
                  >
                </div>
              </template>

              <template v-else-if="question.type === 'essay'">
                <div class="answer-label">请输入答案：</div>
                <div class="essay-editor-wrapper">
                  <div
                    class="editor-toolbar"
                    v-if="!question.minWords && !question.maxWords"
                  >
                    <el-button-group>
                      <el-button
                        size="small"
                        @click="insertFormula(question.id)"
                      >
                        <el-icon><Edit /></el-icon> 插入公式
                      </el-button>
                    </el-button-group>
                  </div>
                  <div
                    :ref="(el) => setEditorRef(el, question.id)"
                    class="quill-editor"
                  ></div>
                  <div class="editor-footer">
                    <div class="word-count">
                      字数：{{ getWordCount(question.id) }}
                      <template v-if="question.minWords || question.maxWords">
                        / {{ question.minWords || 0 }} -
                        {{ question.maxWords || '∞' }}
                      </template>
                    </div>
                    <div class="auto-save-hint" v-if="lastAutoSave">
                      <el-icon><Clock /></el-icon>
                      自动保存于 {{ lastAutoSave }}
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>

        <div class="bottom-action-bar">
          <div class="action-left">
            <el-tag type="info">共 {{ questions.length }} 题</el-tag>
            <el-tag
              :type="answeredCount === questions.length ? 'success' : 'warning'"
            >
              已答 {{ answeredCount }} 题
            </el-tag>
          </div>
          <div class="action-right">
            <el-button @click="handleSaveDraft" :loading="savingDraft">
              <el-icon><Document /></el-icon> 保存草稿
            </el-button>
            <el-button
              type="primary"
              @click="handleSubmit"
              :loading="submitting"
              :disabled="isOverdue"
            >
              <el-icon><Promotion /></el-icon> 提交作业
            </el-button>
          </div>
        </div>
      </div>

      <div class="side-panel">
        <div class="panel-header">
          <h3 class="assignment-title">{{ assignment.title }}</h3>
          <el-tag type="info" size="small">{{ assignment.courseName }}</el-tag>
        </div>

        <div class="countdown-section" :class="{ overdue: isOverdue }">
          <div class="countdown-label">
            <el-icon><Clock /></el-icon>
            <span>{{ isOverdue ? '已逾期' : '剩余时间' }}</span>
          </div>
          <div class="countdown-value" v-if="!isOverdue">
            <span class="time-block">{{ countdown.days }}</span>
            <span class="time-unit">天</span>
            <span class="time-block">{{ countdown.hours }}</span>
            <span class="time-unit">时</span>
            <span class="time-block">{{ countdown.minutes }}</span>
            <span class="time-unit">分</span>
            <span class="time-block">{{ countdown.seconds }}</span>
            <span class="time-unit">秒</span>
          </div>
          <div class="countdown-value overdue-text" v-else>
            作业已逾期，无法提交
          </div>
        </div>

        <div class="question-index-section">
          <h4 class="section-title">题目索引</h4>
          <div class="index-grid">
            <div
              v-for="(question, index) in questions"
              :key="question.id"
              class="index-item"
              :class="{
                answered: isAnswered(question.id),
                current: currentQuestionId === question.id,
              }"
              @click="scrollToQuestion(question.id)"
            >
              <span class="index-number">{{ index + 1 }}</span>
              <el-icon v-if="isAnswered(question.id)" class="check-icon"
                ><Check
              /></el-icon>
            </div>
          </div>
        </div>

        <div class="panel-footer">
          <el-button
            @click="handleSaveDraft"
            :loading="savingDraft"
            style="flex: 1"
          >
            <el-icon><Document /></el-icon> 保存草稿
          </el-button>
          <el-button
            type="primary"
            @click="handleSubmit"
            :loading="submitting"
            :disabled="isOverdue"
            style="flex: 1"
          >
            <el-icon><Promotion /></el-icon> 提交
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="submitDialogVisible"
      title="确认提交"
      width="500px"
      :close-on-click-modal="false"
      append-to-body
    >
      <div class="submit-confirm">
        <div class="confirm-icon">
          <el-icon :size="48" color="#e6a23c"><WarningFilled /></el-icon>
        </div>
        <p class="confirm-title">确定要提交作业吗？</p>
        <p class="confirm-desc">提交后将无法修改答案</p>
        <div class="answer-summary">
          <el-tag type="success">已答 {{ answeredCount }} 题</el-tag>
          <el-tag type="danger" v-if="unansweredCount > 0"
            >未答 {{ unansweredCount }} 题</el-tag
          >
        </div>
        <div class="unanswered-list" v-if="unansweredQuestions.length > 0">
          <p class="unanswered-title">未作答题目：</p>
          <div class="unanswered-numbers">
            <el-tag
              v-for="q in unansweredQuestions"
              :key="q.id"
              type="danger"
              size="small"
              class="unanswered-tag"
            >
              第{{ q.index }}题
            </el-tag>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSubmit" :loading="submitting">
          确认提交
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="formulaDialogVisible" title="插入公式" width="500px" append-to-body>
      <div class="formula-input">
        <el-input
          v-model="formulaInput"
          type="textarea"
          :rows="4"
          placeholder="请输入LaTeX公式，例如：\frac{a}{b}、\sqrt{x}、x^2"
        />
        <div class="formula-preview" v-if="formulaInput">
          <span class="preview-label">预览：</span>
          <div class="preview-content" v-html="renderedFormula"></div>
        </div>
      </div>
      <template #footer>
        <el-button @click="formulaDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmInsertFormula">插入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Clock,
  Check,
  Document,
  Promotion,
  Edit,
  WarningFilled,
} from '@element-plus/icons-vue';
import Quill from 'quill';
import 'quill/dist/quill.snow.css';
import request from '@/utils/request';
import katex from 'katex';
import 'katex/dist/katex.min.css';

const route = useRoute();
const router = useRouter();
const assignmentId = computed(() => route.params.id as string);

const assignment = ref({
  id: 0,
  title: '',
  courseName: '',
  deadline: '',
  totalScore: 0,
});

const questions = ref<any[]>([]);
const answers = ref<Record<number, any>>({});
const answeredSet = ref<Set<number>>(new Set());

const loading = ref(true);
const savingDraft = ref(false);
const submitting = ref(false);
const isOverdue = ref(false);
const lastAutoSave = ref('');

const countdown = ref({ days: 0, hours: 0, minutes: 0, seconds: 0 });
const currentQuestionId = ref<number | null>(null);

const editorRefs = ref<Record<number, HTMLElement | null>>({});
const editors = ref<Record<number, Quill | null>>({});
const wordCounts = ref<Record<number, number>>({});

const submitDialogVisible = ref(false);
const formulaDialogVisible = ref(false);
const formulaInput = ref('');
const currentFormulaQuestionId = ref<number | null>(null);

let countdownTimer: number | null = null;
let autoSaveTimer: number | null = null;
let essayAutoSaveTimer: number | null = null;

const answeredCount = computed(() => answeredSet.value.size);
const unansweredCount = computed(
  () => questions.value.length - answeredCount.value,
);

const unansweredQuestions = computed(() => {
  return questions.value
    .map((q, index) => ({ ...q, index: index + 1 }))
    .filter((q) => !answeredSet.value.has(q.id));
});

const renderedFormula = computed(() => {
  if (!formulaInput.value) return '';
  try {
    return katex.renderToString(formulaInput.value, { throwOnError: false });
  } catch (e) {
    return '<span style="color: red;">公式语法错误</span>';
  }
});

const getQuestionTypeText = (type: string) => {
  const map: Record<string, string> = {
    single: '单选题',
    multiple: '多选题',
    judgment: '判断题',
    essay: '主观题',
  };
  return map[type] || type;
};

const getQuestionTypeTag = (type: string) => {
  const map: Record<string, string> = {
    single: '',
    multiple: 'success',
    judgment: 'warning',
    essay: 'danger',
  };
  return map[type] || '';
};

const parseOptions = (options: string) => {
  if (!options) return [];
  if (typeof options === 'string') {
    try {
      const parsed = JSON.parse(options);
      return Array.isArray(parsed) ? parsed : options.split('|');
    } catch {
      return options.split('|');
    }
  }
  return Array.isArray(options) ? options : [];
};

const setEditorRef = (el: any, id: number) => {
  if (el) editorRefs.value[id] = el;
};

const isAnswered = (questionId: number) => {
  return answeredSet.value.has(questionId);
};

const handleAnswerChange = (questionId: number) => {
  const answer = answers.value[questionId];
  if (answer !== undefined && answer !== null && answer !== '') {
    if (Array.isArray(answer)) {
      if (answer.length > 0) {
        answeredSet.value.add(questionId);
      } else {
        answeredSet.value.delete(questionId);
      }
    } else {
      answeredSet.value.add(questionId);
    }
  } else {
    answeredSet.value.delete(questionId);
  }
};

const isAllSelected = (questionId: number, options: string) => {
  const opts = parseOptions(options);
  const selected = answers.value[questionId] || [];
  return selected.length === opts.length;
};

const handleSelectAll = (
  questionId: number,
  options: string,
  checked: boolean,
) => {
  const opts = parseOptions(options);
  if (checked) {
    answers.value[questionId] = opts.map((_, idx) =>
      String.fromCharCode(65 + idx),
    );
  } else {
    answers.value[questionId] = [];
  }
  handleAnswerChange(questionId);
};

const getWordCount = (questionId: number) => {
  return wordCounts.value[questionId] || 0;
};

const scrollToQuestion = (questionId: number) => {
  const element = document.getElementById(`question-${questionId}`);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    currentQuestionId.value = questionId;
  }
};

const initQuillEditor = (questionId: number) => {
  const el = editorRefs.value[questionId];
  if (!el) return;

  const editor = new Quill(el, {
    theme: 'snow',
    modules: {
      toolbar: [
        ['bold', 'italic', 'underline', 'strike'],
        [{ list: 'ordered' }, { list: 'bullet' }],
        ['clean'],
      ],
    },
    placeholder: '请输入答案...',
  });

  editors.value[questionId] = editor;

  editor.on('text-change', () => {
    const text = editor.getText().trim();
    wordCounts.value[questionId] = text.length;

    if (text.length > 0) {
      answeredSet.value.add(questionId);
    } else {
      answeredSet.value.delete(questionId);
    }
  });
};

const insertFormula = (questionId: number) => {
  currentFormulaQuestionId.value = questionId;
  formulaInput.value = '';
  formulaDialogVisible.value = true;
};

const confirmInsertFormula = () => {
  if (!formulaInput.value || !currentFormulaQuestionId.value) {
    ElMessage.warning('请输入公式');
    return;
  }

  const editor = editors.value[currentFormulaQuestionId.value];
  if (editor) {
    const selection = editor.getSelection();
    const index = selection ? selection.index : editor.getLength();

    const formulaHtml = `<span class="katex-formula">$${formulaInput.value}$</span>`;
    editor.clipboard.dangerouslyPasteHTML(index, formulaHtml);
  }

  formulaDialogVisible.value = false;
};

const calculateCountdown = () => {
  if (!assignment.value.deadline) return;

  const now = new Date().getTime();
  const deadline = new Date(assignment.value.deadline).getTime();
  const diff = deadline - now;

  if (diff <= 0) {
    isOverdue.value = true;
    countdown.value = { days: 0, hours: 0, minutes: 0, seconds: 0 };
    return;
  }

  isOverdue.value = false;
  countdown.value = {
    days: Math.floor(diff / (1000 * 60 * 60 * 24)),
    hours: Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
    minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)),
    seconds: Math.floor((diff % (1000 * 60)) / 1000),
  };
};

const fetchAssignmentData = async () => {
  try {
    loading.value = true;
    const response = await request.get(
      `/student/assignments/${assignmentId.value}/detail`,
    );

    if (response.data) {
      assignment.value = {
        id: response.data.id,
        title: response.data.title,
        courseName: response.data.courseName,
        deadline: response.data.deadline,
        totalScore: response.data.totalScore,
      };

      questions.value = response.data.questions || [];

      questions.value.forEach((q) => {
        if (q.type === 'multiple') {
          answers.value[q.id] = [];
        } else if (q.type === 'essay') {
          answers.value[q.id] = '';
        } else {
          answers.value[q.id] = '';
        }
      });

      calculateCountdown();

      await nextTick();
      questions.value.forEach((q) => {
        if (q.type === 'essay') {
          initQuillEditor(q.id);
        }
      });

      startAutoSave();
      checkDraft();
    }
  } catch (error) {
    console.error('获取作业详情失败:', error);
    ElMessage.error('获取作业详情失败');
  } finally {
    loading.value = false;
  }
};

const checkDraft = async () => {
  try {
    const response = await request.get(
      `/student/assignments/${assignmentId.value}/draft`,
    );
    if (response.data && response.data.answers) {
      const hasContent = response.data.answers.some(
        (a: any) => a.answerContent && a.answerContent.length > 0,
      );

      if (hasContent) {
        ElMessageBox.confirm('你有未保存的草稿，是否恢复？', '提示', {
          confirmButtonText: '恢复',
          cancelButtonText: '放弃',
          type: 'info',
        })
          .then(() => {
            restoreDraft(response.data.answers);
            ElMessage.success('草稿已恢复');
          })
          .catch(() => {});
      }
    }
  } catch (error) {
    console.log('无草稿数据');
  }
};

const restoreDraft = (draftAnswers: any[]) => {
  draftAnswers.forEach((item) => {
    const question = questions.value.find((q) => q.id === item.questionId);
    if (!question) return;

    if (question.type === 'multiple') {
      try {
        answers.value[item.questionId] = JSON.parse(item.answerContent);
      } catch {
        answers.value[item.questionId] = item.answerContent
          ? [item.answerContent]
          : [];
      }
    } else if (question.type === 'essay') {
      const editor = editors.value[item.questionId];
      if (editor && item.answerContent) {
        editor.root.innerHTML = item.answerContent;
        wordCounts.value[item.questionId] = editor.getText().trim().length;
      }
    } else {
      answers.value[item.questionId] = item.answerContent || '';
    }

    handleAnswerChange(item.questionId);
  });
};

const collectAnswers = () => {
  questions.value.forEach((q) => {
    if (q.type === 'essay') {
      const editor = editors.value[q.id];
      if (editor) {
        answers.value[q.id] = editor.root.innerHTML;
      }
    }
  });

  return questions.value.map((q) => ({
    questionId: q.id,
    answerContent: Array.isArray(answers.value[q.id])
      ? JSON.stringify(answers.value[q.id])
      : String(answers.value[q.id] || ''),
  }));
};

const handleSaveDraft = async () => {
  try {
    savingDraft.value = true;
    const answersData = collectAnswers();

    await request.post(`/student/assignments/${assignmentId.value}/save-draft`, {
      answers: answersData,
    });

    const now = new Date();
    lastAutoSave.value = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;

    ElMessage.success('草稿保存成功');
  } catch (error) {
    console.error('保存草稿失败:', error);
    ElMessage.error('保存草稿失败');
  } finally {
    savingDraft.value = false;
  }
};

const handleSubmit = () => {
  collectAnswers();

  if (unansweredCount.value > 0) {
    ElMessageBox.confirm(
      `还有 ${unansweredCount.value} 道题目未作答，确定要提交吗？`,
      '提示',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '继续作答',
        type: 'warning',
      },
    )
      .then(() => {
        submitDialogVisible.value = true;
      })
      .catch(() => {});
  } else {
    submitDialogVisible.value = true;
  }
};

const confirmSubmit = async () => {
  if (isOverdue.value) {
    ElMessage.error('作业已逾期，无法提交');
    return;
  }

  try {
    submitting.value = true;
    const answersData = collectAnswers();

    await request.post(`/student/assignments/${assignmentId.value}/submit`, {
      answers: answersData,
    });

    ElMessage.success('作业提交成功');
    submitDialogVisible.value = false;
    router.push('/student/assignments');
  } catch (error: any) {
    console.error('提交作业失败:', error);
    ElMessage.error(error.message || '提交作业失败');
  } finally {
    submitting.value = false;
  }
};

const startAutoSave = () => {
  autoSaveTimer.value = window.setInterval(() => {
    handleSaveDraft();
  }, 30000);

  essayAutoSaveTimer.value = window.setInterval(() => {
    handleSaveDraft();
  }, 60000);
};

onMounted(() => {
  fetchAssignmentData();

  countdownTimer.value = window.setInterval(calculateCountdown, 1000);
});

onUnmounted(() => {
  if (countdownTimer.value) clearInterval(countdownTimer.value);
  if (autoSaveTimer.value) clearInterval(autoSaveTimer.value);
  if (essayAutoSaveTimer.value) clearInterval(essayAutoSaveTimer.value);
});
</script>

<style scoped>
.submit-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.submit-container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.questions-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  padding-bottom: 80px;
}

.question-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
}

.question-header {
  margin-bottom: 16px;
}

.question-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.question-number {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.question-score {
  font-size: 14px;
  color: #f56c6c;
}

.question-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #fafafa 0%, #f5f7fa 100%);
  border-radius: 12px;
  border-left: 4px solid #409eff;
}

.question-answer {
  padding-left: 8px;
}

.answer-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
  font-weight: 500;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  padding: 14px 18px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fafafa 100%);
  border-radius: 10px;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.option-item:hover {
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f7ff 100%);
  border-color: #409eff;
}

.option-label {
  font-weight: 600;
  margin-right: 8px;
  color: #409eff;
}

.option-text {
  color: #303133;
}

.select-all {
  margin-bottom: 12px;
  padding: 10px 18px;
  background: #f5f7fa;
  border-radius: 10px;
}

.judgment-switch {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fafafa 100%);
  border-radius: 10px;
}

.judgment-label {
  font-size: 14px;
  color: #909399;
  transition: all 0.3s;
}

.judgment-label.active {
  color: #303133;
  font-weight: 600;
}

.essay-editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 12px;
  overflow: hidden;
}

.editor-toolbar {
  padding: 10px 14px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fafafa 100%);
  border-bottom: 1px solid #dcdfe6;
}

.quill-editor {
  min-height: 200px;
  background: white;
}

.quill-editor :deep(.ql-editor) {
  min-height: 180px;
  font-size: 14px;
  line-height: 1.8;
}

.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fafafa 100%);
  border-top: 1px solid #dcdfe6;
}

.word-count {
  font-size: 12px;
  color: #909399;
}

.auto-save-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #67c23a;
}

.bottom-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 300px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-top: 1px solid #ebeef5;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.08);
  z-index: 100;
}

.action-left {
  display: flex;
  gap: 12px;
}

.action-right {
  display: flex;
  gap: 12px;
}

.side-panel {
  width: 300px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-left: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 12px rgba(0, 0, 0, 0.08);
}

.panel-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.assignment-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.countdown-section {
  padding: 16px 20px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9eb 100%);
  border-bottom: 1px solid #ebeef5;
}

.countdown-section.overdue {
  background: linear-gradient(135deg, #fef0f0 0%, #fdf6ec 100%);
}

.countdown-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.countdown-value {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.time-block {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  background: white;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.time-unit {
  font-size: 12px;
  color: #606266;
}

.overdue-text {
  color: #f56c6c;
  font-weight: 500;
  justify-content: flex-start;
}

.question-index-section {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.index-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.index-item {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: 2px solid #dcdfe6;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.index-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.index-item.answered {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
}

.index-item.current {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.2);
}

.index-number {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.index-item.answered .index-number {
  color: #67c23a;
}

.check-icon {
  position: absolute;
  top: -4px;
  right: -4px;
  font-size: 12px;
  color: #67c23a;
  background: white;
  border-radius: 50%;
}

.panel-footer {
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 12px;
}

.submit-confirm {
  text-align: center;
  padding: 20px 0;
}

.confirm-icon {
  margin-bottom: 16px;
}

.confirm-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.confirm-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
}

.answer-summary {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
}

.unanswered-list {
  background: #fef0f0;
  padding: 12px 16px;
  border-radius: 10px;
}

.unanswered-title {
  font-size: 14px;
  color: #f56c6c;
  margin-bottom: 8px;
}

.unanswered-numbers {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.unanswered-tag {
  cursor: pointer;
}

.formula-input {
  padding: 12px 0;
}

.formula-preview {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 10px;
}

.preview-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
  display: block;
}

.preview-content {
  font-size: 16px;
}

.katex-formula {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'KaTeX_Main', serif;
}

@media (max-width: 1200px) {
  .side-panel {
    width: 260px;
  }

  .bottom-action-bar {
    right: 260px;
  }
}

@media (max-width: 992px) {
  .submit-container {
    flex-direction: column;
  }

  .side-panel {
    width: 100%;
    order: -1;
    max-height: 200px;
  }

  .question-index-section {
    display: none;
  }

  .bottom-action-bar {
    right: 0;
  }
}
</style>
