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
        meta: { requiresAuth: false }
    },
    {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/register/index.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/student/assignments',
        name: 'StudentAssignments',
        component: () => import('@/views/student/assignments/index.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
    },
    {
        path: '/student/assignments/:id/submit',
        name: 'StudentSubmit',
        component: () => import('@/views/student/assignments/submit.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
    },
    {
        path: '/student/reports/:id',
        name: 'StudentReports',
        component: () => import('@/views/student/reports/index.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
    },
    {
        path: '/student/history',
        name: 'StudentHistory',
        component: () => import('@/views/student/history/index.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' }
    },
    {
        path: '/teacher/courses',
        name: 'TeacherCourses',
        component: () => import('@/views/teacher/courses/index.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
    },
    {
        path: '/teacher/courses/:id',
        name: 'TeacherCourseDetail',
        component: () => import('@/views/teacher/courses/detail.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
    },
    {
        path: '/teacher/assignments/create',
        name: 'TeacherCreateAssignment',
        component: () => import('@/views/teacher/assignments/edit.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
    },
    {
        path: '/teacher/assignments/:id/edit',
        name: 'TeacherEditAssignment',
        component: () => import('@/views/teacher/assignments/edit.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
    },
    {
        path: '/teacher/assignments/:id/submissions',
        name: 'TeacherSubmissions',
        component: () => import('@/views/teacher/assignments/submissions.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
    },
    {
        path: '/teacher/analysis',
        name: 'TeacherAnalysis',
        component: () => import('@/views/teacher/analysis/index.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' }
    },
    {
        path: '/admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users/index.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
        path: '/admin/courses',
        name: 'AdminCourses',
        component: () => import('@/views/admin/courses/index.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
        path: '/admin/questions',
        name: 'AdminQuestions',
        component: () => import('@/views/admin/questions/index.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
        path: '/admin/stats',
        name: 'AdminStats',
        component: () => import('@/views/admin/stats/index.vue'),
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    { path: '/403', component: () => import('@/views/403.vue') },
    { path: '/:pathMatch(.*)*', component: () => import('@/views/404.vue') }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

router.beforeEach((to, from, next) => {
    const userStore = useUserStore();
    if (to.meta.requiresAuth && !userStore.token) {
        next('/login');
    } else if (to.meta.role && userStore.role !== to.meta.role) {
        next('/403');
    } else {
        next();
    }
});

export default router;