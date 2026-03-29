<template>
  <div class="container bg">
    <div class="content">
      <div id="large-header" class="large-header">
        <canvas id="demo-canvas"></canvas>
        <div class="logo_box">
          <div class="logo-image">
            <img
              src="@/assets/images/Scoututor.png"
              alt="Scoututor"
              class="logo-img"
            />
          </div>
          <form @submit.prevent="handleRegister">
            <!-- 账号输入 -->
            <div class="input_outer">
              <input
                v-model="username"
                class="text-input"
                type="text"
                placeholder="请输入账号"
                autocomplete="username"
              />
            </div>

            <!-- 真实姓名输入 -->
            <div class="input_outer">
              <input
                v-model="realName"
                class="text-input"
                type="text"
                placeholder="请输入姓名"
                autocomplete="name"
              />
            </div>

            <!-- 角色选择 -->
            <div class="input_outer">
              <div class="custom-select-container">
                <div class="custom-select" @click="toggleDropdown">
                  <span class="select-value">{{
                    roleDisplay || '请选择角色'
                  }}</span>
                  <span class="select-arrow">{{
                    isDropdownOpen ? '▼' : '▲'
                  }}</span>
                </div>
                <div v-if="isDropdownOpen" class="dropdown-menu">
                  <div
                    v-for="item in roleOptions"
                    :key="item.value"
                    class="dropdown-item"
                    @click="selectRole(item)"
                  >
                    {{ item.label }}
                  </div>
                </div>
              </div>
            </div>

            <!-- 邮箱输入 -->
            <div class="input_outer">
              <input
                v-model="email"
                class="text-input"
                type="email"
                placeholder="请输入邮箱"
                autocomplete="email"
              />
            </div>

            <!-- 电话输入 -->
            <div class="input_outer">
              <input
                v-model="phone"
                class="text-input"
                type="tel"
                placeholder="请输入电话"
                autocomplete="tel"
              />
            </div>

            <!-- 密码输入 -->
            <div class="input_outer">
              <input
                v-model="password"
                class="text-input"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                autocomplete="password"
              />
              <span
                class="password-toggle"
                @click="showPassword = !showPassword"
              >
                <el-icon v-if="showPassword"><Hide /></el-icon>
                <el-icon v-else><View /></el-icon>
              </span>
            </div>

            <!-- 确认密码输入 -->
            <div class="input_outer">
              <input
                v-model="confirmPassword"
                class="text-input"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="请确认密码"
                autocomplete="new-password"
              />
              <span
                class="password-toggle"
                @click="showConfirmPassword = !showConfirmPassword"
              >
                <el-icon v-if="showConfirmPassword"><Hide /></el-icon>
                <el-icon v-else><View /></el-icon>
              </span>
            </div>

            <!-- 注册按钮 -->
            <div class="button-container">
              <button
                type="button"
                class="register-button"
                @click="handleRegister"
                :disabled="loading"
              >
                {{ loading ? '注册中...' : '注册' }}
              </button>
            </div>

            <!-- 登录链接 -->
            <div class="login-link">
              已有账号？
              <a href="/login" class="link">点击登录</a>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { View, Hide } from '@element-plus/icons-vue';
import request from '@/utils/request';

// 声明全局变量
declare const TweenLite: any;
declare const Circ: any;

// 表单数据
const username = ref('');
const realName = ref('');
const role = ref('');
const roleDisplay = ref('');
const email = ref('');
const phone = ref('');
const password = ref('');
const confirmPassword = ref('');
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const loading = ref(false);
const isDropdownOpen = ref(false);
const router = useRouter();

// 角色选项
const roleOptions = [
  { label: '学生', value: 'STUDENT' },
  { label: '教师', value: 'TEACHER' },
];

// 切换下拉框
const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value;
};

// 选择角色
const selectRole = (item: { label: string; value: string }) => {
  role.value = item.value;
  roleDisplay.value = item.label;
  isDropdownOpen.value = false;
};

// 点击外部关闭下拉框
const handleClickOutside = (e: MouseEvent) => {
  const target = e.target as HTMLElement;
  if (!target.closest('.custom-select-container')) {
    isDropdownOpen.value = false;
  }
};

// 注册处理函数
const handleRegister = async () => {
  if (
    !username.value ||
    !realName.value ||
    !role.value ||
    !email.value ||
    !phone.value ||
    !password.value ||
    !confirmPassword.value
  ) {
    ElMessage.warning('请填写所有必填字段');
    return;
  }

  if (password.value !== confirmPassword.value) {
    ElMessage.warning('两次输入的密码不一致');
    return;
  }

  loading.value = true;
  try {
    const response = await request.post('/auth/register', {
      username: username.value,
      realName: realName.value,
      role: role.value,
      email: email.value,
      phone: phone.value,
      password: password.value,
    });

    if (response.code === 200) {
      ElMessage.success('注册成功，请等待管理员审核，审核通过后即可登录');
      router.push('/login');
    } else {
      ElMessage.error(response.message || '注册失败，请稍后重试');
    }
  } catch (error) {
    console.error('注册错误:', error);
    ElMessage.error('注册失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 动画相关变量
let width: number,
  height: number,
  largeHeader: HTMLElement | null,
  canvas: HTMLCanvasElement | null,
  ctx: CanvasRenderingContext2D | null,
  points: any[],
  target: { x: number; y: number },
  animateHeader = true;
let animationFrame: number;

// 加载脚本
const loadScript = (src: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = src;
    script.onload = () => {
      console.log(`Script loaded: ${src}`);
      resolve();
    };
    script.onerror = () => {
      console.error(`Failed to load script: ${src}`);
      reject(new Error(`Failed to load script: ${src}`));
    };
    document.head.appendChild(script);
  });
};

// 加载所有必要的脚本（使用相对路径）
const loadScripts = async () => {
  try {
    // 使用相对于当前文件的路径
    const basePath = import.meta.url.replace(/index\.vue.*$/, '');
    await loadScript(new URL('../login/js/TweenLite.min.js', basePath).href);
    await loadScript(new URL('../login/js/EasePack.min.js', basePath).href);
    await loadScript(new URL('../login/js/rAF.js', basePath).href);
    console.log('All scripts loaded successfully');
    return true;
  } catch (error) {
    console.error('Failed to load scripts:', error);
    return false;
  }
};

// 初始化头部动画
const initHeader = () => {
  width = window.innerWidth;
  height = window.innerHeight;
  target = { x: width / 2, y: height / 2 };

  largeHeader = document.getElementById('large-header');
  if (largeHeader) {
    largeHeader.style.height = height + 'px';
  }

  canvas = document.getElementById('demo-canvas') as HTMLCanvasElement;
  if (!canvas) return;

  canvas.width = width;
  canvas.height = height;
  ctx = canvas.getContext('2d');

  // 创建点
  points = [];
  for (let x = 0; x < width; x = x + width / 20) {
    for (let y = 0; y < height; y = y + height / 20) {
      const px = x + (Math.random() * width) / 20;
      const py = y + (Math.random() * height) / 20;
      const p = { x: px, originX: px, y: py, originY: py };
      points.push(p);
    }
  }

  // 为每个点找到最近的5个点
  for (let i = 0; i < points.length; i++) {
    const closest = [];
    const p1 = points[i];
    for (let j = 0; j < points.length; j++) {
      const p2 = points[j];
      if (!(p1 === p2)) {
        let placed = false;
        for (let k = 0; k < 5; k++) {
          if (!placed) {
            if (closest[k] === undefined) {
              closest[k] = p2;
              placed = true;
            }
          }
        }

        for (let k = 0; k < 5; k++) {
          if (!placed) {
            if (getDistance(p1, p2) < getDistance(p1, closest[k])) {
              closest[k] = p2;
              placed = true;
            }
          }
        }
      }
    }
    p1.closest = closest;
  }

  // 为每个点分配一个圆
  for (const i in points) {
    const c = new Circle(
      points[i],
      2 + Math.random() * 2,
      'rgba(255,255,255,0.3)',
    );
    points[i].circle = c;
  }
};

// 事件处理
const addListeners = () => {
  if (!('ontouchstart' in window)) {
    window.addEventListener('mousemove', mouseMove);
  }
  window.addEventListener('scroll', scrollCheck);
  window.addEventListener('resize', resize);
  document.addEventListener('click', handleClickOutside);
};

const mouseMove = (e: MouseEvent) => {
  let posx = 0,
    posy = 0;
  if (e.pageX || e.pageY) {
    posx = e.pageX;
    posy = e.pageY;
  } else if (e.clientX || e.clientY) {
    posx =
      e.clientX +
      document.body.scrollLeft +
      document.documentElement.scrollLeft;
    posy =
      e.clientY + document.body.scrollTop + document.documentElement.scrollTop;
  }
  target.x = posx;
  target.y = posy;
};

const scrollCheck = () => {
  if (document.body.scrollTop > height) {
    animateHeader = false;
  } else {
    animateHeader = true;
  }
};

const resize = () => {
  width = window.innerWidth;
  height = window.innerHeight;
  if (largeHeader) {
    largeHeader.style.height = height + 'px';
  }
  if (canvas) {
    canvas.width = width;
    canvas.height = height;
  }
  initHeader();
};

// 动画
const initAnimation = () => {
  animate();
  for (const i in points) {
    shiftPoint(points[i]);
  }
};

const animate = () => {
  if (animateHeader && ctx) {
    ctx.clearRect(0, 0, width, height);
    for (const i in points) {
      // 检测范围内的点
      if (Math.abs(getDistance(target, points[i])) < 4000) {
        points[i].active = 0.3;
        points[i].circle.active = 0.6;
      } else if (Math.abs(getDistance(target, points[i])) < 20000) {
        points[i].active = 0.1;
        points[i].circle.active = 0.3;
      } else if (Math.abs(getDistance(target, points[i])) < 40000) {
        points[i].active = 0.02;
        points[i].circle.active = 0.1;
      } else {
        points[i].active = 0;
        points[i].circle.active = 0;
      }

      drawLines(points[i]);
      points[i].circle.draw();
    }
  }
  animationFrame = requestAnimationFrame(animate);
};

const shiftPoint = (p: any) => {
  // 检查 TweenLite 是否可用
  if (typeof TweenLite !== 'undefined' && typeof Circ !== 'undefined') {
    TweenLite.to(p, 1 + 1 * Math.random(), {
      x: p.originX - 50 + Math.random() * 100,
      y: p.originY - 50 + Math.random() * 100,
      ease: Circ.easeInOut,
      onComplete: () => {
        shiftPoint(p);
      },
    });
  } else {
    // 如果 TweenLite 不可用，使用简单的 setTimeout 作为降级方案
    setTimeout(
      () => {
        p.x = p.originX - 50 + Math.random() * 100;
        p.y = p.originY - 50 + Math.random() * 100;
        shiftPoint(p);
      },
      1000 + Math.random() * 1000,
    );
  }
};

// Canvas 操作
const drawLines = (p: any) => {
  if (!p.active || !ctx) return;
  for (const i in p.closest) {
    ctx.beginPath();
    ctx.moveTo(p.x, p.y);
    ctx.lineTo(p.closest[i].x, p.closest[i].y);
    ctx.strokeStyle = 'rgba(156,217,249,' + p.active + ')';
    ctx.stroke();
  }
};

// Circle类
class Circle {
  pos: any;
  radius: number;
  color: string;
  active: number;

  constructor(pos: any, rad: number, color: string) {
    this.pos = pos || null;
    this.radius = rad || null;
    this.color = color || null;
    this.active = 0;
  }

  draw() {
    if (!this.active || !ctx) return;
    ctx.beginPath();
    ctx.arc(this.pos.x, this.pos.y, this.radius, 0, 2 * Math.PI, false);
    ctx.fillStyle = 'rgba(156,217,249,' + this.active + ')';
    ctx.fill();
  }
}

// 工具函数
const getDistance = (p1: any, p2: any) => {
  return Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2);
};

// 生命周期钩子
onMounted(async () => {
  // 先加载脚本
  await loadScripts();

  // 确保DOM元素存在后初始化动画
  if (document.getElementById('demo-canvas')) {
    // 延迟一点确保脚本完全初始化
    setTimeout(() => {
      initHeader();
      initAnimation();
      addListeners();
    }, 100);
  }
});

onBeforeUnmount(() => {
  // 清理动画和事件监听
  if (animationFrame) {
    cancelAnimationFrame(animationFrame);
  }
  window.removeEventListener('mousemove', mouseMove);
  window.removeEventListener('scroll', scrollCheck);
  window.removeEventListener('resize', resize);
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
/* 基础样式 */
*,
*:after,
*:before {
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
}

.container {
  min-height: 100vh;
  width: 100%;
}

.content {
  width: 100%;
  height: 100vh;
}

.large-header {
  position: relative;
  width: 100%;
  background: #333;
  overflow: hidden;
  background-size: cover;
  background-position: center center;
  z-index: 1;
  min-height: 100vh;
}

.bg .large-header {
  background-image: url('../login/img/bg.jpg');
}

.logo_box {
  width: 400px;
  height: 550px;
  padding: 35px;
  color: #eee;
  position: absolute;
  left: 50%;
  top: 33%;
  margin-left: -200px;
  margin-top: -250px;
}

.logo-image {
  text-align: center;
  padding: 0 0 40px 0;
}

.logo-img {
  height: 60px;
  width: auto;
  object-fit: contain;
  filter: invert(1);
}

/* 输入框容器 */
.input_outer {
  height: 50px;
  padding: 0 20px;
  margin-bottom: 15px;
  border-radius: 50px;
  position: relative;
  border: rgba(255, 255, 255, 0.2) 2px solid;
  display: flex;
  align-items: center;
  background: transparent;
}

/* 文本输入框 */
.text-input {
  flex: 1;
  height: 100%;
  outline: none;
  font:
    20px 'Microsoft YaHei',
    Helvetica,
    Tahoma,
    Arial,
    'Microsoft jhengHei';
  border: none;
  background: transparent;
  color: #ffffff;
  padding: 0 20px;
  box-sizing: border-box;
  text-align: center;
  direction: ltr;
}

.text-input:-webkit-autofill {
  text-align: center;
}

.text-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
  text-align: center;
}

/* 隐藏浏览器默认的密码切换图标 */
input[type='password']::-ms-reveal,
input[type='password']::-ms-clear {
  display: none;
}

input[type='password']::-webkit-credentials-auto-fill-button {
  display: none !important;
  visibility: hidden;
  pointer-events: none;
  position: absolute;
  right: 0;
}

/* 隐藏Chrome的密码管理器图标 */
input[type='password']::-webkit-password-toggle-button {
  display: none;
}

/* 密码切换按钮 */
.password-toggle {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

.password-toggle:hover {
  color: rgba(255, 255, 255, 1);
}

/* 密码图标样式 */
.password-icon {
  display: inline-block;
  width: 20px;
  height: 20px;
  position: relative;
}

.password-icon::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 20px;
  height: 12px;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 6px;
  transform: translateY(-50%);
}

.password-icon::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 5px;
  width: 10px;
  height: 10px;
  background-color: #333;
  border-radius: 50%;
  transform: translateY(-50%);
}

/* 显示密码时的图标样式 */
.password-icon.show::before {
  background-color: rgba(255, 255, 255, 0.7);
}

.password-icon.show::after {
  width: 12px;
  height: 2px;
  background-color: #333;
  border-radius: 1px;
  transform: translateY(-50%) rotate(45deg);
  left: 4px;
}

/* 自定义选择框 */
.custom-select-container {
  flex: 1;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
}

.custom-select {
  flex: 1;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  cursor: pointer;
}

.select-value {
  font:
    20px 'Microsoft YaHei',
    Helvetica,
    Tahoma,
    Arial;
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
  flex: 1;
}

.select-arrow {
  position: absolute;
  right: 2px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

/* 下拉菜单 */
.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 5px;
  background-color: rgba(0, 0, 0, 0.392);
  border: 1px solid rgba(0, 0, 0, 0.504);
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.dropdown-item {
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.7);
  font:
    18px 'Microsoft YaHei',
    Helvetica,
    Tahoma,
    Arial;
  cursor: pointer;
  transition: background-color 0.3s;
  text-align: center;
}

.dropdown-item:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.dropdown-item:first-child {
  border-radius: 8px 8px 0 0;
}

.dropdown-item:last-child {
  border-radius: 0 0 8px 8px;
}

/* 按钮容器 */
.button-container {
  margin-bottom: 20px;
}

/* 注册按钮 */
.register-button {
  line-height: 24px;
  text-align: center;
  font-size: 22px;
  border-radius: 50px;
  background: rgba(0, 74, 114, 0.603);
  cursor: pointer;
  color: #ffffff;
  border: none;
  width: 100%;
  padding: 18px;
  margin-top: 25px;
}

.register-button:hover {
  background: rgba(1, 137, 248, 0.652);
}

.register-button:disabled {
  background: rgba(204, 204, 204, 0.8);
  cursor: not-allowed;
}

/* 登录链接 */
.login-link {
  text-align: center;
  margin-top: 25px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
}

.link {
  color: #0096e6;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}
</style>
