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
                  <div class="options-list single-options">
                    <div
                      v-for="(option, idx) in parseOptions(question.options)"
                      :key="idx"
                      class="option-item single-option-item"
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
                  <div class="options-list multiple-options">
                    <div
                      v-for="(option, idx) in parseOptions(question.options)"
                      :key="idx"
                      class="option-item multiple-option-item"
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
                <el-radio-group
                  v-model="answers[question.id]"
                  @change="handleAnswerChange(question.id)"
                  class="judgment-options"
                >
                  <el-radio label="true" class="judgment-option">
                    <span class="judgment-label" :class="{ active: answers[question.id] === 'true' }">
                      正确
                    </span>
                  </el-radio>
                  <el-radio label="false" class="judgment-option">
                    <span class="judgment-label" :class="{ active: answers[question.id] === 'false' }">
                      错误
                    </span>
                  </el-radio>
                </el-radio-group>
              </template>

              <template v-else-if="question.type === 'essay'">
                <div class="answer-label">请输入答案：</div>
                <div class="essay-editor-wrapper">
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

    <el-dialog v-model="formulaDialogVisible" title="插入数学公式" width="700px" append-to-body>
      <div class="formula-editor">
        <div class="formula-input-section">
          <el-input
            v-model="formulaInput"
            type="textarea"
            :rows="4"
            placeholder="请输入LaTeX公式，例如：\frac{a}{b}、\sqrt{x}、x^2"
            @input="updateFormulaPreview"
          />
          <div class="formula-preview" v-if="formulaInput">
            <span class="preview-label">预览：</span>
            <div class="preview-content" v-html="renderedFormula"></div>
          </div>
        </div>
        
        <div class="formula-templates">
          <h4>常用公式模板</h4>
          <div class="template-grid">
            <el-button
              v-for="(template, index) in commonFormulas"
              :key="index"
              size="small"
              @click="insertTemplate(template.formula)"
              class="template-btn"
            >
              {{ template.name }}
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="formulaDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmInsertFormula" :disabled="!formulaInput">插入</el-button>
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
import { registerKatex, commonFormulas } from '@/utils/quill-katex';

registerKatex();

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

const countdownTimer = ref<number | null>(null);
const autoSaveTimer = ref<number | null>(null);

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
      toolbar: {
        container: [
          ['bold', 'italic', 'underline', 'strike'],
          [{ list: 'ordered' }, { list: 'bullet' }],
          ['katex']
        ],
        handlers: {
          katex: () => insertFormula(questionId)
        }
      }
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

const insertTemplate = (formula: string) => {
  formulaInput.value = formula;
};

const updateFormulaPreview = () => {
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

    const formulaHtml = `<div class="ql-katex" data-formula="${formulaInput.value}"><div>${katex.renderToString(formulaInput.value, { throwOnError: false, displayMode: true })}</div></div>`;
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
      
      // 显示题目数量提示
      if (questions.value.length === 0) {
        ElMessage.warning('该作业暂无题目');
      }

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

      // 延迟检查草稿，确保编辑器已初始化
      setTimeout(() => {
        checkDraft();
      }, 500);

      startAutoSave();
    } else {
      questions.value = [];
      answers.value = {};
    }
  } catch (error) {
    console.error('获取作业详情失败:', error);
    ElMessage.error('获取作业详情失败');
    questions.value = [];
    answers.value = {};
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

const restoreDraft = async (draftAnswers: any[]) => {
  for (const item of draftAnswers) {
    const question = questions.value.find((q) => q.id === item.questionId);
    if (!question) continue;

    if (question.type === 'multiple') {
      try {
        answers.value[item.questionId] = JSON.parse(item.answerContent);
      } catch {
        answers.value[item.questionId] = item.answerContent
          ? [item.answerContent]
          : [];
      }
    } else if (question.type === 'essay') {
      // 等待编辑器初始化完成
      let retries = 0;
      while (!editors.value[item.questionId] && retries < 10) {
        await new Promise(resolve => setTimeout(resolve, 100));
        retries++;
      }
      
      const editor = editors.value[item.questionId];
      if (editor && item.answerContent) {
        editor.root.innerHTML = item.answerContent;
        wordCounts.value[item.questionId] = editor.getText().trim().length;
      }
    } else {
      answers.value[item.questionId] = item.answerContent || '';
    }

    handleAnswerChange(item.questionId);
  }
};

const collectAnswers = () => {
  // 先收集主观题答案
  questions.value.forEach((q) => {
    if (q.type === 'essay') {
      const editor = editors.value[q.id];
      if (editor) {
        // 获取编辑器内容，去除HTML标签后检查是否为空
        const htmlContent = editor.root.innerHTML;
        const textContent = editor.getText().trim();
        // 如果文本内容为空，则存储空字符串
        answers.value[q.id] = textContent.length > 0 ? htmlContent : '';
      }
    }
  });

  // 返回答案数据
  const answersData = questions.value.map((q) => {
    let answerContent = answers.value[q.id];
    
    // 处理多选题答案
    if (q.type === 'multiple' && Array.isArray(answerContent)) {
      answerContent = JSON.stringify(answerContent);
    }
    
    // 确保答案不为null或undefined
    if (answerContent === null || answerContent === undefined) {
      answerContent = '';
    }
    
    return {
      questionId: q.id,
      answer: String(answerContent),
    };
  });

  return answersData;
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

  let answersData: any[] = [];

  try {
    submitting.value = true;
    
    // 提示用户当前题目数量
    if (questions.value.length === 0) {
      ElMessage.error('没有可提交的题目，请刷新页面重试');
      return;
    }
    
    // 强制重新收集所有答案（特别是主观题）
    questions.value.forEach((q) => {
      if (q.type === 'essay') {
        const editor = editors.value[q.id];
        if (editor) {
          const html = editor.root.innerHTML;
          const text = editor.getText().trim();
          answers.value[q.id] = text.length > 0 ? html : '';
        }
      }
    });
    
    answersData = questions.value.map((q) => {
      let answerContent = answers.value[q.id];
      if (q.type === 'multiple' && Array.isArray(answerContent)) {
        answerContent = JSON.stringify(answerContent);
      }
      if (answerContent === null || answerContent === undefined) {
        answerContent = '';
      }
      return {
        questionId: q.id,
        answer: String(answerContent),
      };
    });
    
    // 检查题目数量和答案数量是否匹配
    if (answersData.length !== questions.value.length) {
      ElMessage.error('题目数据加载异常，请刷新页面重试');
      return;
    }
    
    // 检查是否有未作答的题目
    const unanswered = answersData.filter(a => !a.answer || a.answer === '');
    if (unanswered.length > 0) {
      // 找到未作答的题目序号
      const unansweredQuestions = unanswered.map(a => {
        const q = questions.value.find(q => q.id === a.questionId);
        const index = questions.value.findIndex(q => q.id === a.questionId);
        return { index: index + 1, type: q?.type };
      });
      const questionNumbers = unansweredQuestions.map(q => `第${q.index}题`).join('、');
      ElMessage.warning(`${questionNumbers} 未作答，请完成后再提交`);
      return;
    }

    await request.post(`/student/assignments/${assignmentId.value}/submit`, {
      answers: answersData,
    });

    ElMessage.success('作业提交成功');
    submitDialogVisible.value = false;
    router.push('/student/assignments');
  } catch (error: any) {
    if (error.response?.data) {
      ElMessage.error(error.response.data.message || '提交作业失败');
    } else {
      ElMessage.error(error.message || '提交作业失败');
    }
  } finally {
    submitting.value = false;
  }
};

const startAutoSave = () => {
  // 只启动一个定时器，每30秒自动保存一次
  autoSaveTimer.value = window.setInterval(() => {
    handleSaveDraft();
  }, 30000);
};

onMounted(() => {
  fetchAssignmentData();

  countdownTimer.value = window.setInterval(calculateCountdown, 1000);
});

onUnmounted(() => {
  if (countdownTimer.value) clearInterval(countdownTimer.value);
  if (autoSaveTimer.value) clearInterval(autoSaveTimer.value);
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

/* 单选题样式 - 水平排列，根据文本长度自适应宽度，不重合 */
.single-options {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
}

.single-option-item {
  padding: 12px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fafafa 100%);
  border-radius: 10px;
  transition: all 0.3s;
  border: 1px solid transparent;
  display: flex;
  align-items: flex-start;
  flex: 0 0 auto;        /* 不伸缩，根据内容自适应宽度 */
  min-width: 80px;       /* 最小宽度 */
  max-width: 100%;       /* 最大宽度不超过容器 */
  word-break: break-word;
}

.single-option-item:hover {
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f7ff 100%);
  border-color: #409eff;
}

/* 多选题样式 - 垂直排列，每项一行，水平居中 */
.multiple-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.multiple-option-item {
  padding: 12px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fafafa 100%);
  border-radius: 10px;
  transition: all 0.3s;
  border: 1px solid transparent;
  display: flex;
  align-items: flex-start;
  width: 100%;
  max-width: 500px;
  word-break: break-word;
}

.multiple-option-item:hover {
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f7ff 100%);
  border-color: #409eff;
}

.single-option-item:deep(.el-radio__label),
.multiple-option-item:deep(.el-checkbox__label) {
  margin: 0;
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

.judgment-options {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fafafa 100%);
  border-radius: 10px;
}

.judgment-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fafafa 100%);
  border-radius: 10px;
  border: 1px solid transparent;
  transition: all 0.3s;
  cursor: pointer;
}

.judgment-option:hover {
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f7ff 100%);
  border-color: #409eff;
}

.judgment-option:deep(.el-radio__label) {
  margin: 0;
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

:deep(.ql-toolbar .ql-katex) {
  position: relative;
}

:deep(.ql-toolbar .ql-katex::before) {
  content: 'fx';
  font-family: 'Times New Roman', serif;
  font-style: italic;
  font-weight: bold;
  font-size: 14px;
  display: block;
}

:deep(.ql-toolbar .ql-katex svg) {
  display: none;
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

.formula-editor {
  padding: 12px 0;
}

.formula-input-section {
  margin-bottom: 20px;
}

.formula-preview {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.preview-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #606266;
}

.preview-content {
  padding: 12px;
  background: white;
  border-radius: 4px;
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.formula-templates h4 {
  margin-bottom: 12px;
  font-size: 14px;
  color: #303133;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 8px;
}

.template-btn {
  margin: 0;
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
