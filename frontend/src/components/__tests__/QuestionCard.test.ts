import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import QuestionCard from '../QuestionCard.vue';

describe('QuestionCard component', () => {
  it('should render single choice question', () => {
    const question = {
      id: 1,
      title: '测试单选题',
      type: 'single' as const,
      options: ['选项A', '选项B', '选项C', '选项D'],
      score: 10
    };

    const wrapper = mount(QuestionCard, {
      props: { question }
    });

    expect(wrapper.find('.question-title').text()).toBe('测试单选题');
    expect(wrapper.find('.question-score').text()).toBe('10分');
    expect(wrapper.find('.options').exists()).toBe(true);
    expect(wrapper.findAll('.option-item')).toHaveLength(4);
  });

  it('should render multiple choice question', () => {
    const question = {
      id: 2,
      title: '测试多选题',
      type: 'multiple' as const,
      options: ['选项A', '选项B', '选项C'],
      score: 15
    };

    const wrapper = mount(QuestionCard, {
      props: { question }
    });

    expect(wrapper.find('.question-title').text()).toBe('测试多选题');
    expect(wrapper.find('.question-score').text()).toBe('15分');
    expect(wrapper.find('.options').exists()).toBe(true);
    expect(wrapper.findAll('.option-item')).toHaveLength(3);
  });

  it('should render subjective question', () => {
    const question = {
      id: 3,
      title: '测试主观题',
      type: 'subjective' as const,
      score: 20
    };

    const wrapper = mount(QuestionCard, {
      props: { question }
    });

    expect(wrapper.find('.question-title').text()).toBe('测试主观题');
    expect(wrapper.find('.question-score').text()).toBe('20分');
    expect(wrapper.find('textarea').exists()).toBe(true);
  });

  it('should emit answer event when option is clicked', async () => {
    const question = {
      id: 1,
      title: '测试单选题',
      type: 'single' as const,
      options: ['选项A', '选项B'],
      score: 10
    };

    const wrapper = mount(QuestionCard, {
      props: { question }
    });

    await wrapper.findAll('.option-item')[0].trigger('click');

    expect(wrapper.emitted('answer')).toHaveLength(1);
    expect(wrapper.emitted('answer')?.[0]).toEqual([1, ['0']]);
  });

  it('should emit answer event when subjective answer is input', async () => {
    const question = {
      id: 3,
      title: '测试主观题',
      type: 'subjective' as const,
      score: 20
    };

    const wrapper = mount(QuestionCard, {
      props: { question }
    });

    const textarea = wrapper.find('textarea');
    await textarea.setValue('测试答案');

    expect(wrapper.emitted('answer')).toHaveLength(1);
    expect(wrapper.emitted('answer')?.[0]).toEqual([3, '测试答案']);
  });
});
