<template>
  <div class="rich-editor">
    <QuillEditor
      ref="editorRef"
      v-model:content="modelValue"
      :options="editorOptions"
      contentType="html"
      @ready="onReady"
      @change="onChange"
    />
    <div class="word-count" v-if="showWordCount">
      字数：{{ wordCount }}{{ maxWords ? ` / ${maxWords}` : '' }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { QuillEditor } from '@vueup/vue-quill';
import '@vueup/vue-quill/dist/vue-quill.snow.css';

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  placeholder: {
    type: String,
    default: '请输入内容...',
  },
  maxWords: {
    type: Number,
    default: null,
  },
  minWords: {
    type: Number,
    default: null,
  },
  showWordCount: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['update:modelValue', 'wordCountChange']);

const editorRef = ref(null);
const content = ref(props.modelValue);

const editorOptions = {
  placeholder: props.placeholder,
  theme: 'snow',
  modules: {
    toolbar: [
      ['bold', 'italic', 'underline', 'strike'],
      ['blockquote', 'code-block'],
      [{ header: 1 }, { header: 2 }],
      [{ list: 'ordered' }, { list: 'bullet' }],
      [{ script: 'sub' }, { script: 'super' }],
      ['link', 'image', 'video'],
      ['clean'],
    ],
  },
};

const wordCount = computed(() => {
  if (!content.value) return 0;
  const text = content.value.replace(/<[^>]+>/g, '');
  return text.length;
});

watch(content, (newVal) => {
  emit('update:modelValue', newVal);
  emit('wordCountChange', wordCount.value);
});

watch(
  () => props.modelValue,
  (newVal) => {
    content.value = newVal;
  },
);

const onReady = (quill) => {
  console.log('Quill editor ready');
};

const onChange = ({ quill, html, text }) => {
  content.value = html;
};

const getHTML = () => {
  return content.value;
};

const getText = () => {
  if (!content.value) return '';
  return content.value.replace(/<[^>]+>/g, '');
};

const validateWordCount = () => {
  const count = wordCount.value;
  if (props.minWords && count < props.minWords) {
    return { valid: false, message: `字数不足，最少需要 ${props.minWords} 字` };
  }
  if (props.maxWords && count > props.maxWords) {
    return { valid: false, message: `字数超出，最多允许 ${props.maxWords} 字` };
  }
  return { valid: true, message: '' };
};

defineExpose({
  getHTML,
  getText,
  validateWordCount,
});
</script>

<style scoped>
.rich-editor {
  width: 100%;
}

.rich-editor :deep(.ql-container) {
  min-height: 200px;
  font-size: 14px;
}

.rich-editor :deep(.ql-editor) {
  min-height: 200px;
}

.rich-editor :deep(.ql-editor.ql-blank::before) {
  font-style: normal;
  color: #a8abb2;
}

.word-count {
  text-align: right;
  padding: 8px 0;
  font-size: 12px;
  color: #909399;
}
</style>
