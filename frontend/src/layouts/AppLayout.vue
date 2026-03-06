     <template>
       <el-container class="layout-container">
         <el-aside :width="collapsed ? '64px' : '200px'" :class="{ 'mobile-hidden': isMobile }">
           <el-menu :collapse="collapsed" :default-active="$route.path" router>
             <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
               <el-icon><component :is="item.icon" /></el-icon>
               <span>{{ item.title }}</span>
             </el-menu-item>
           </el-menu>
         </el-aside>
         <el-container>
           <el-header>
             <div class="header-left">
               <el-icon @click="toggleSidebar" v-if="!isMobile"><Fold /></el-icon>
               <el-icon @click="toggleMobileMenu" v-else><Menu /></el-icon>
             </div>
             <div class="header-right">
               <el-dropdown @command="handleCommand">
                 <span class="user-info">
                   {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
                   <el-icon><ArrowDown /></el-icon>
                 </span>
                 <template #dropdown>
                   <el-dropdown-menu>
                     <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                     <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                   </el-dropdown-menu>
                 </template>
               </el-dropdown>
               <el-switch v-model="isDark" @change="toggleTheme" inline-prompt :active-icon="Moon" :inactive-icon="Sunny" />
             </div>
           </el-header>
           <el-main>
             <slot></slot>
           </el-main>
         </el-container>
       </el-container>
       <!-- 移动端菜单 -->
       <el-drawer
         v-model="mobileMenuVisible"
         direction="ltr"
         size="200px"
         title="菜单"
       >
         <el-menu :default-active="$route.path" router @select="handleMenuSelect">
           <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
             <el-icon><component :is="item.icon" /></el-icon>
             <span>{{ item.title }}</span>
           </el-menu-item>
         </el-menu>
       </el-drawer>
     </template>

     <script setup lang="ts">
     import { computed, ref, onMounted, onUnmounted } from 'vue';
     import { useRouter } from 'vue-router';
     import { useUserStore } from '@/stores/user';
     import { useAppStore } from '@/stores/app';
     import { Fold, ArrowDown, Moon, Sunny, Menu } from '@element-plus/icons-vue';

     const props = defineProps<{ menuItems: any[] }>();

     const router = useRouter();
     const userStore = useUserStore();
     const appStore = useAppStore();

     const collapsed = computed(() => appStore.sidebarCollapsed);
     const toggleSidebar = () => appStore.toggleSidebar();

     // 响应式布局
     const isMobile = ref(false);
     const mobileMenuVisible = ref(false);

     const checkMobile = () => {
       isMobile.value = window.innerWidth < 768;
     };

     const toggleMobileMenu = () => {
       mobileMenuVisible.value = !mobileMenuVisible.value;
     };

     const handleMenuSelect = () => {
       mobileMenuVisible.value = false;
     };

     // 主题切换
     const isDark = computed({
       get: () => appStore.theme === 'dark',
       set: (value) => appStore.setTheme(value ? 'dark' : 'light')
     });

     const toggleTheme = () => {
       appStore.toggleTheme();
     };

     const handleCommand = (cmd: string) => {
       if (cmd === 'profile') router.push('/profile');
       else if (cmd === 'logout') {
         // 调用登出接口后清理
         userStore.logout();
         router.push('/login');
       }
     };

     onMounted(() => {
       checkMobile();
       window.addEventListener('resize', checkMobile);
     });

     onUnmounted(() => {
       window.removeEventListener('resize', checkMobile);
     });
     </script>

     <style scoped>
     .layout-container {
       height: 100vh;
       width: 100%;
     }
     .el-header {
       display: flex;
       align-items: center;
       justify-content: space-between;
       border-bottom: 1px solid var(--color-border);
       background-color: var(--color-background);
       width: 100%;
       box-sizing: border-box;
     }
     .header-right {
       display: flex;
       align-items: center;
       gap: 16px;
     }
     .user-info {
       cursor: pointer;
       color: var(--color-text);
     }
     .mobile-hidden {
       display: none !important;
     }
     
     /* 响应式设计 */
     @media (min-width: 768px) {
       .mobile-hidden {
         display: block !important;
       }
     }
     
     @media (max-width: 767px) {
       .el-main {
         padding: 10px;
       }
       .el-header {
         padding: 0 10px;
       }
     }
     </style>