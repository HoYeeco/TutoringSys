import katex from 'katex';
import Quill from 'quill';

const BlockEmbed = Quill.import('blots/block/embed');
const Inline = Quill.import('blots/inline');

class KatexBlot extends BlockEmbed {
  static create(value) {
    const node = super.create();
    node.setAttribute('data-formula', value);
    const formulaContainer = document.createElement('div');
    katex.render(value, formulaContainer, {
      throwOnError: false,
      displayMode: true
    });
    node.appendChild(formulaContainer);
    return node;
  }

  static value(node) {
    return node.getAttribute('data-formula');
  }
}

KatexBlot.blotName = 'katex';
KatexBlot.tagName = 'div';
KatexBlot.className = 'ql-katex';

class KatexInlineBlot extends Inline {
  static create(value) {
    const node = super.create();
    node.setAttribute('data-formula', value);
    katex.render(value, node, {
      throwOnError: false,
      displayMode: false
    });
    return node;
  }

  static value(node) {
    return node.getAttribute('data-formula');
  }
}

KatexInlineBlot.blotName = 'katex-inline';
KatexInlineBlot.tagName = 'span';
KatexInlineBlot.className = 'ql-katex-inline';

export const commonFormulas = [
  { name: '分数', formula: '\\frac{a}{b}' },
  { name: '平方根', formula: '\\sqrt{x}' },
  { name: 'n次方根', formula: '\\sqrt[n]{x}' },
  { name: '上标', formula: 'x^2' },
  { name: '下标', formula: 'x_1' },
  { name: '求和', formula: '\\sum_{i=1}^{n} x_i' },
  { name: '积分', formula: '\\int_{a}^{b} f(x)dx' },
  { name: '极限', formula: '\\lim_{x \\to \\infty} f(x)' },
  { name: '希腊字母', formula: '\\alpha, \\beta, \\gamma, \\delta' },
  { name: '矩阵', formula: '\\begin{pmatrix} a & b \\\\ c & d \\end{pmatrix}' },
  { name: '三角函数', formula: '\\sin(x), \\cos(x), \\tan(x)' },
  { name: '对数', formula: '\\log_a(b)' },
  { name: '指数', formula: 'e^{x}' },
  { name: '绝对值', formula: '|x|' },
  { name: '不等式', formula: 'a \\leq b \\leq c' },
  { name: '向量', formula: '\\vec{AB}' },
  { name: '偏导数', formula: '\\frac{\\partial f}{\\partial x}' },
  { name: '梯度', formula: '\\nabla f' },
  { name: '点积', formula: '\\vec{a} \\cdot \\vec{b}' },
  { name: '叉积', formula: '\\vec{a} \\times \\vec{b}' }
];

class KatexModule {
  constructor(quill, options) {
    this.quill = quill;
    this.options = options || {};
    if (this.options.toolbar) {
      this.addToolbarButtons();
    }
  }

  addToolbarButtons() {
    const toolbar = this.quill.getModule('toolbar');
    if (toolbar) {
      toolbar.addHandler('katex', this.katexHandler.bind(this));
      toolbar.addHandler('katex-inline', this.katexInlineHandler.bind(this));
    }
  }

  katexHandler() {
    this.showFormulaInput(true);
  }

  katexInlineHandler() {
    this.showFormulaInput(false);
  }

  showFormulaInput(displayMode) {
    const value = prompt('Enter your LaTeX formula:');
    if (value) {
      const range = this.quill.getSelection(true);
      if (displayMode) {
        this.quill.insertEmbed(range.index, 'katex', value);
        this.quill.setSelection(range.index + 1);
      } else {
        this.quill.insertEmbed(range.index, 'katex-inline', value);
        this.quill.setSelection(range.index + 1);
      }
    }
  }
}

export function registerKatex() {
  Quill.register({
    'formats/katex': KatexBlot,
    'formats/katex-inline': KatexInlineBlot,
    'modules/katex': KatexModule
  }, true);
}

export { KatexModule, KatexBlot, KatexInlineBlot };
