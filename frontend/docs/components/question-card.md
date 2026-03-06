# QuestionCard 组件

## 组件简介
QuestionCard 组件用于展示题目信息，支持不同类型题目的显示和交互。

## Props

| 属性 | 类型 | 默认值 | 描述 |
| --- | --- | --- | --- |
| question | Object | - | 题目信息对象 |
| showAnswer | Boolean | false | 是否显示答案 |
| disabled | Boolean | false | 是否禁用交互 |

## Events

| 事件 | 描述 | 参数 |
| --- | --- | --- |
| select | 选择答案时触发 | 选择的答案 |
| submit | 提交答案时触发 | 完整的答题信息 |

## 用法示例

### 基本用法

```vue
<template>
  <QuestionCard
    :question="question"
    :show-answer="false"
    :disabled="false"
    @select="handleSelect"
    @submit="handleSubmit"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import QuestionCard from '@/components/QuestionCard.vue';

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

const handleSelect = (answer: string[]) => {
  console.log('Selected answer:', answer);
};

const handleSubmit = (submission: any) => {
  console.log('Submitted:', submission);
};
</script>
```

### 显示答案

```vue
<template>
  <QuestionCard
    :question="question"
    :show-answer="true"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import QuestionCard from '@/components/QuestionCard.vue';

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
</script>
```
