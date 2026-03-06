import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    state: () => ({
        token: null as string | null,
        userInfo: null as any,
        role: 'student' as string | null,
    }),
    actions: {
        setToken(token: string) {
            this.token = token;
        },
        setUserInfo(info: any) {
            this.userInfo = info;
            this.role = info?.role || 'student';
        },
        logout() {
            this.token = null;
            this.userInfo = null;
            this.role = 'student';
        },
    },
    persist: true,
});