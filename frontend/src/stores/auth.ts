import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import * as authApi from '@/api/authApi'
import type { User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const isLoggedIn = computed(() => !!token.value)

  function setToken(value: string | null) {
    token.value = value
    if (value) {
      localStorage.setItem('auth_token', value)
    } else {
      localStorage.removeItem('auth_token')
    }
  }

  /** 仅清理本地会话（令牌已过期 / 服务端已拒绝时使用，不再请求服务端）。 */
  function clearSession() {
    user.value = null
    setToken(null)
  }

  function init() {
    const saved = localStorage.getItem('auth_token')
    if (saved) {
      token.value = saved
      fetchUser()
    }
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      const res = await authApi.getMe()
      if (res.code === 200) {
        user.value = res.data
      } else {
        clearSession()
      }
    } catch {
      clearSession()
    }
  }

  async function login(account: string, password: string): Promise<string | null> {
    try {
      const res = await authApi.login({ account, password })
      if (res.code === 200) {
        setToken(res.data.token)
        user.value = res.data.user
        return null
      }
      return res.message
    } catch (e: any) {
      return e.response?.data?.message || '登录失败'
    }
  }

  async function register(username: string, account: string, password: string): Promise<string | null> {
    try {
      const res = await authApi.register({ username, account, password })
      if (res.code === 200) {
        return null
      }
      return res.message
    } catch (e: any) {
      return e.response?.data?.message || '注册失败'
    }
  }

  /**
   * 登出：立即清理本地状态，再通知服务端让令牌版本自增。
   * 服务端失败（网络异常 / 令牌已过期）不影响本地登出结果，也不会阻塞界面。
   */
  async function logout(): Promise<void> {
    const current = token.value
    clearSession()
    if (!current) return
    try {
      await authApi.logout(current)
    } catch {
      // 令牌已失效或网络不可用：本地已登出，无需额外处理
    }
  }

  async function updateProfile(username: string, bio: string, avatarUrl?: string): Promise<string | null> {
    try {
      const body: any = { username, bio }
      if (avatarUrl !== undefined) body.avatarUrl = avatarUrl
      const res = await authApi.updateProfile(body)
      if (res.code === 200) {
        user.value = res.data
        return null
      }
      return res.message
    } catch (e: any) {
      return e.response?.data?.message || '更新失败'
    }
  }

  async function changePassword(oldPassword: string, newPassword: string): Promise<string | null> {
    try {
      const res = await authApi.changePassword({ oldPassword, newPassword })
      if (res.code === 200) {
        // 服务端轮换了令牌版本：保存新 token，当前设备保持登录，其他设备立即失效
        setToken(res.data.token)
        return null
      }
      return res.message
    } catch (e: any) {
      return e.response?.data?.message || '修改失败'
    }
  }

  return {
    user,
    token,
    isLoggedIn,
    init,
    login,
    register,
    logout,
    clearSession,
    updateProfile,
    changePassword,
    fetchUser,
  }
})
