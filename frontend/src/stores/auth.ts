import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import request from '@/api/request'

interface User {
  id: number
  username: string
  account: string
  avatarUrl: string
  bio: string
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const isLoggedIn = computed(() => !!token.value)

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
      const res: any = await request.get('/auth/me', {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      if (res.code === 200) {
        user.value = res.data
      } else {
        logout()
      }
    } catch {
      logout()
    }
  }

  async function login(account: string, password: string): Promise<string | null> {
    try {
      const res: any = await request.post('/auth/login', { account, password })
      if (res.code === 200) {
        token.value = res.data.token
        user.value = res.data.user
        localStorage.setItem('auth_token', token.value!)
        return null
      }
      return res.message
    } catch (e: any) {
      return e.response?.data?.message || '登录失败'
    }
  }

  async function register(username: string, account: string, password: string): Promise<string | null> {
    try {
      const res: any = await request.post('/auth/register', { username, account, password })
      if (res.code === 200) {
        return null
      }
      return res.message
    } catch (e: any) {
      return e.response?.data?.message || '注册失败'
    }
  }

  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('auth_token')
  }

  async function updateProfile(username: string, bio: string, avatarUrl?: string): Promise<string | null> {
    try {
      const body: any = { username, bio }
      if (avatarUrl !== undefined) body.avatarUrl = avatarUrl
      const res: any = await request.put('/auth/profile', body, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
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
      const res: any = await request.put('/auth/password', { oldPassword, newPassword }, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      if (res.code === 200) return null
      return res.message
    } catch (e: any) {
      return e.response?.data?.message || '修改失败'
    }
  }

  return { user, token, isLoggedIn, init, login, register, logout, updateProfile, changePassword, fetchUser }
})
