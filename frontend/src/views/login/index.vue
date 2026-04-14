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
          <form @submit.prevent="handleLogin">
            <div class="input_outer">
              <span class="user"></span>
              <input
                v-model="username"
                class="text"
                style="color: #ffffff !important"
                type="text"
                placeholder="请输入账号"
                autocomplete="username"
              />
            </div>
            <div class="input_outer">
              <span class="user"></span>
              <input
                v-model="password"
                class="text"
                style="color: #ffffff !important"
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
            <div class="mb2">
              <a
                class="act-but submit"
                href="javascript:;"
                style="color: #ffffff"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登录' }}
              </a>
            </div>
            <div class="register-link">
              没有账号？
              <a href="/register" style="color: #0096e6; text-decoration: none"
                >点击注册</a
              >
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
import { useUserStore } from '@/stores/user';
import { ElMessage } from 'element-plus';
import { View, Hide } from '@element-plus/icons-vue';
import request from '@/utils/request';

// 声明全局变量
declare const TweenLite: any;
declare const Circ: any;

const username = ref('');
const password = ref('');
const loading = ref(false);
const showPassword = ref(false);
const router = useRouter();
const userStore = useUserStore();

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
    await loadScript(new URL('./js/TweenLite.min.js', basePath).href);
    await loadScript(new URL('./js/EasePack.min.js', basePath).href);
    await loadScript(new URL('./js/rAF.js', basePath).href);
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

// 登录处理函数
const handleLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入账号或密码');
    return;
  }

  loading.value = true;
  try {
    const params = new URLSearchParams();
    params.append('username', username.value);
    params.append('password', password.value);

    const response = await request.post('/auth/login', params, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });

    if (response.code === 200) {
      userStore.setToken(response.data.token);
      userStore.setUserInfo(response.data);
      ElMessage.success('登录成功');
      router.push('/dashboard');
    } else {
      ElMessage.error('账号或密码错误');
    }
  } catch (error) {
    console.error('登录错误:', error);
    ElMessage.error('账号或密码错误');
  } finally {
    loading.value = false;
  }
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
});
</script>

<style scoped>
/* 直接复制 component.css 和 demo.css 的样式 */
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
  /* 使用相对路径引用图片 */
  background-image: url('./img/bg.jpg');
}

.logo_box {
  width: 400px;
  height: 500px;
  padding: 35px;
  color: #eee;
  position: absolute;
  left: 50%;
  top: 50%;
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

.input_outer {
  height: 50px;
  padding: 0 20px;
  margin-bottom: 30px;
  border-radius: 50px;
  position: relative;
  border: rgba(255, 255, 255, 0.2) 2px solid !important;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user {
  width: 25px;
  height: 25px;
  background-position: -125px 0;
  position: absolute;
  left: 20px;
  flex-shrink: 0;
}

.text {
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
  background: none;
  color: #ffffff;
  padding: 0 40px;
  box-sizing: border-box;
  text-align: center;
}

.text::placeholder {
  color: rgba(255, 255, 255, 0.6);
  vertical-align: middle;
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

.mb2 {
  margin-bottom: 20px;
}

.mb2 a {
  text-decoration: none;
  outline: none;
}

.submit {
  padding: 15px;
  margin-top: 20px;
  display: block;
}

.act-but {
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

.act-but:hover {
  background: rgba(1, 137, 248, 0.652);
}

.act-but:disabled {
  background: rgba(204, 204, 204, 0.8);
  cursor: not-allowed;
}

.password-toggle {
  cursor: pointer;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  position: absolute;
  right: 20px;
  flex-shrink: 0;
}

.password-toggle:hover {
  color: rgba(255, 255, 255, 1);
}

.register-link {
  text-align: center;
  margin-top: 25px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
}

.register-link a:hover {
  text-decoration: underline;
}

/* 基础样式 */
*,
*:after,
*:before {
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
}

.clearfix:before,
.clearfix:after {
  content: '';
  display: table;
}

.clearfix:after {
  clear: both;
}

.container {
  min-height: 100vh;
  width: 100%;
}

.content {
  width: 100%;
  height: 100vh;
}
</style>
