# QuestionReportCard 组件

## 组件简介
QuestionReportCard 组件用于展示题目批改报告，包括得分、答题情况和详细分析。

## Props

| 属性 | 类型 | 默认值 | 描述 |
| --- | --- | --- | --- |
| report | Object | - | 批改报告信息对象 |
| question | Object | - | 题目信息对象 |

## 用法示例

```vue
<template>
  <QuestionReportCard
    :report="report"
    :question="question"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import QuestionReportCard from '@/components/QuestionReportCard.vue';

const question = ref({
  id: 1,
  title: '下列哪个是Vue 3的新特性？',
  type: 'multiple',
  options: [
    { id: 'A', text: 'Composition API' },
    { id: 'B', text: 'Options API' },
    { id: 'C', text: 'Virtual DOM' },
    { id: 'D', text: 'Reactivity' }
  ],
  correctAnswer: ['A']
});

const report = ref({
  score: 100,
  userAnswer: ['A'],
  isCorrect: true,
  feedback: '回答正确！Composition API是Vue 3的重要新特性。',
  timeSpent: 15 // 秒
});
</script>
```
