import { describe, it, expect, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useAppStore } from '../app';

describe('app store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should initialize with default values', () => {
    const appStore = useAppStore();
    expect(appStore.sidebarCollapsed).toBe(false);
  });

  it('should toggle sidebar', () => {
    const appStore = useAppStore();
    expect(appStore.sidebarCollapsed).toBe(false);

    appStore.toggleSidebar();
    expect(appStore.sidebarCollapsed).toBe(true);

    appStore.toggleSidebar();
    expect(appStore.sidebarCollapsed).toBe(false);
  });
});
