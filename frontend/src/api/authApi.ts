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

export function changePassword(payload: { oldPassword: string; newPassword: string }) {
  return request.put<unknown, ApiResponse<void>>('/auth/password', payload)
}
