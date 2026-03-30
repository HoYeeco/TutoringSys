import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/stores/user';

const routes = [
  {
    path: '/',
    redirect: (to) => {
      const userStore = useUserStore();
      if (userStore.token) {
        return '/dashboard';
      } else {
        return '/login';
      }
    },
    meta: { requiresAuth: false },
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    redirect: (to) => {
      const userStore = useUserStore();
      const role = (
        userStore.userInfo?.role ||
        userStore.role ||
        ''
      ).toUpperCase();
      switch (role) {
        case 'STUDENT':
          return '/dashboard/student';
        case 'TEACHER':
          return '/dashboard/teacher';
        case 'ADMIN':
          return '/dashboard/admin';
        default:
          return '/login';
      }
    },
    meta: { requiresAuth: true },
  },
  {
    path: '/dashboard/student',
    name: 'StudentDashboard',
    component: () => import('@/views/dashboard/student.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/dashboard/teacher',
    name: 'TeacherDashboard',
    component: () => import('@/views/dashboard/teacher.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/dashboard/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/dashboard/admin.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/index.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/messages/index.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/student/assignments',
    name: 'StudentAssignments',
    component: () => import('@/views/student/assignments/index.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/assignments/:id/submit',
    name: 'StudentSubmit',
    component: () => import('@/views/student/assignments/submit.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/assignments/:id/detail',
    name: 'StudentAssignmentDetail',
    component: () => import('@/views/student/assignments/detail.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/courses',
    name: 'StudentCourses',
    component: () => import('@/views/student/courses/index.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/courses/:id',
    name: 'StudentCourseDetail',
    component: () => import('@/views/student/courses/detail.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/grading',
    name: 'StudentGrading',
    component: () => import('@/views/student/grading/index.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/student/error-book',
    name: 'StudentErrorBook',
    component: () => import('@/views/student/error-book/index.vue'),
    meta: { requiresAuth: true, role: 'STUDENT' },
  },
  {
    path: '/teacher/courses',
    name: 'TeacherCourses',
    component: () => import('@/views/teacher/courses/index.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/courses/:id',
    name: 'TeacherCourseDetail',
    component: () => import('@/views/teacher/courses/detail.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/assignments',
    name: 'TeacherAssignments',
    component: () => import('@/views/teacher/assignments/index.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/assignments/create',
    name: 'TeacherCreateAssignment',
    component: () => import('@/views/teacher/assignments/edit.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/assignments/:id/edit',
    name: 'TeacherEditAssignment',
    component: () => import('@/views/teacher/assignments/edit.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/assignments/:id/submissions',
    name: 'TeacherSubmissions',
    component: () => import('@/views/teacher/assignments/submissions.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/grading',
    name: 'TeacherGrading',
    component: () => import('@/views/teacher/grading/index.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/grading/:id',
    name: 'TeacherGradingDetail',
    component: () => import('@/views/teacher/grading/detail.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/grading/batch',
    name: 'TeacherGradingBatch',
    component: () => import('@/views/teacher/grading/batch.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/teacher/analysis',
    name: 'TeacherAnalysis',
    component: () => import('@/views/teacher/analysis/index.vue'),
    meta: { requiresAuth: true, role: 'TEACHER' },
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/users/index.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/admin/courses',
    name: 'AdminCourses',
    component: () => import('@/views/admin/courses/index.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/admin/assignments',
    name: 'AdminAssignments',
    component: () => import('@/views/admin/assignments/index.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/admin/assignments/create',
    name: 'AdminCreateAssignment',
    component: () => import('@/views/admin/assignments/edit.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/admin/assignments/:id/edit',
    name: 'AdminEditAssignment',
    component: () => import('@/views/admin/assignments/edit.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/admin/monitoring',
    name: 'AdminMonitoring',
    component: () => import('@/views/admin/monitoring/index.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  {
    path: '/admin/config',
    name: 'AdminConfig',
    component: () => import('@/views/admin/config/index.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
  },
  { path: '/403', component: () => import('@/views/403.vue') },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/404.vue') },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  if (to.meta.requiresAuth && !userStore.token) {
    next('/login');
    return;
  }

  if (to.meta.role) {
    const userRole = (
      userStore.userInfo?.role ||
      userStore.role ||
      ''
    ).toUpperCase();
    const requiredRole = (to.meta.role as string).toUpperCase();

    if (userRole !== requiredRole) {
      next('/403');
      return;
    }
  }

  next();
});

export default router;
