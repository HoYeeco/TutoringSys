<template>
  <el-card class="question-card">
    <template #header>
      <div class="question-header">
        <span class="question-title">{{ question.title }}</span>
        <span class="question-score">{{ question.score }}分</span>
      </div>
    </template>
    <div class="question-content">
      <p v-if="question.type === 'single'">单选题</p>
      <p v-else-if="question.type === 'multiple'">多选题</p>
      <p v-else-if="question.type === 'judgment'">判断题</p>
      <p v-else>主观题</p>
      <div v-if="question.type === 'single' || question.type === 'multiple' || question.type === 'judgment'" class="options">
        <div 
          v-for="(option, index) in question.options" 
          :key="index"
          class="option-item"
          :class="{ active: selectedOptions.includes(index.toString()) }"
          @click="handleOptionClick(index.toString())"
        >
          <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
          <span class="option-content">{{ option }}</span>
        </div>
      </div>
      <el-input
        v-else
        v-model="subjectiveAnswer"
        type="textarea"
        :rows="4"
        placeholder="请输入答案"
        @input="handleSubjectiveInput"
      />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
  question: {
    id: number;
    title: string;
    type: 'single' | 'multiple' | 'judgment' | 'subjective';
    options?: string[];
    score: number;
  };
}>();

const emit = defineEmits<{
  (e: 'answer', questionId: number, answer: string | string[]): void;
}>();

const selectedOptions = ref<string[]>([]);
const subjectiveAnswer = ref('');

const handleOptionClick = (optionIndex: string) => {
  if (props.question.type === 'single') {
    selectedOptions.value = [optionIndex];
  } else if (props.question.type === 'multiple' || props.question.type === 'judgment') {
    const index = selectedOptions.value.indexOf(optionIndex);
    if (index > -1) {
      selectedOptions.value.splice(index, 1);
    } else {
      selectedOptions.value.push(optionIndex);
    }
  }
  emit('answer', props.question.id, selectedOptions.value);
};

const handleSubjectiveInput = () => {
  emit('answer', props.question.id, subjectiveAnswer.value);
};
</script>

<style scoped>
.question-card {
  margin-bottom: 16px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-title {
  font-size: 16px;
  font-weight: 500;
}

.question-score {
  color: #f56c6c;
  font-weight: 500;
}

.question-content {
  margin-top: 16px;
}

.options {
  margin-top: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-item:hover {
  border-color: #409eff;
}

.option-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.option-label {
  margin-right: 8px;
  font-weight: 500;
}
</style>
