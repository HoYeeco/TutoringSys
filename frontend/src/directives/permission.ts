import { useUserStore } from '@/stores/user';

export const permission = {
  mounted(el: HTMLElement, binding: any) {
    const userStore = useUserStore();
    const roles = binding.value as string[];
    if (!roles.includes(userStore.role)) {
      el.parentNode?.removeChild(el);
    }
  },
};
