import request from '@/api/request'
import type { ApiResponse, User } from '@/types'

export interface LoginPayload {
  account: string
  password: string
}

export interface RegisterPayload extends LoginPayload {
  username: string
}

export interface LoginResult {
  token: string
  user: User
}

export function login(payload: LoginPayload) {
  return request.post<unknown, ApiResponse<LoginResult>>('/auth/login', payload)
}

export function register(payload: RegisterPayload) {
  return request.post<unknown, ApiResponse<User>>('/auth/register', payload)
}

export function getMe() {
  return request.get<unknown, ApiResponse<User>>('/auth/me')
}

export function updateProfile(payload: { username: string; bio: string; avatarUrl?: string }) {
  return request.put<unknown, ApiResponse<User>>('/auth/profile', payload)
}

/**
 * 修改密码：服务端会轮换令牌版本（旧 token 全部失效）并返回新 token。
 */
export function changePassword(payload: { oldPassword: string; newPassword: string }) {
  return request.put<unknown, ApiResponse<{ token: string }>>('/auth/password', payload)
}

/**
 * 服务端登出：令当前账号的令牌版本自增，使已签发的 JWT 立即失效。
 * 显式传入 token，因为本地状态在发起请求前就已清理。
 */
export function logout(token: string) {
  return request.post<unknown, ApiResponse<void>>(
    '/auth/logout',
    undefined,
    { headers: { Authorization: `Bearer ${token}` } },
  )
}
