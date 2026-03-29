import { ref } from 'vue';
import type { Ref } from 'vue';

export function useRequest<T>(apiFunction: (...args: any[]) => Promise<T>) {
  const data = ref<T | null>(null) as Ref<T | null>;
  const loading = ref(false);
  const error = ref<Error | null>(null);

  const execute = async (...args: any[]) => {
    loading.value = true;
    error.value = null;
    try {
      const result = await apiFunction(...args);
      data.value = result;
      return result;
    } catch (err) {
      error.value = err as Error;
      throw err;
    } finally {
      loading.value = false;
    }
  };

  return { data, loading, error, execute };
}
