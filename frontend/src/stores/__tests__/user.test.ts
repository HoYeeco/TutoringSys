import { describe, it, expect, beforeEach } from 'vitest';
import { createPinia, setActivePinia } from 'pinia';
import { useUserStore } from '../user';

describe('user store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('should initialize with default values', () => {
    const userStore = useUserStore();
    expect(userStore.userInfo).toBe(null);
    expect(userStore.token).toBe(null);
    expect(userStore.role).toBe(null);
  });

  it('should set user info', () => {
    const userStore = useUserStore();
    const userInfo = {
      id: 1,
      username: 'testuser',
      realName: 'Test User',
      role: 'student'
    };
    const token = 'test-token';

    userStore.setUserInfo(userInfo);
    userStore.setToken(token);

    expect(userStore.userInfo).toEqual(userInfo);
    expect(userStore.token).toBe(token);
    expect(userStore.role).toBe('student');
  });

  it('should logout and clear user info', () => {
    const userStore = useUserStore();
    const userInfo = {
      id: 1,
      username: 'testuser',
      realName: 'Test User',
      role: 'student'
    };
    const token = 'test-token';

    userStore.setUserInfo(userInfo);
    userStore.setToken(token);
    userStore.logout();

    expect(userStore.userInfo).toBe(null);
    expect(userStore.token).toBe(null);
    expect(userStore.role).toBe(null);
  });
});
