import request from '@/api/request'
import type { ApiResponse, Diary, PageResult } from '@/types'

export interface DiaryPayload {
  title: string
  content?: string
  plantSlug?: string | null
  plantName?: string
  images?: string
  weather?: string
  mood?: string
  heightCm?: number | null
  leafCount?: number | null
  growthStage?: string
}

export function listMyDiaries(params = { page: 1, size: 200 }) {
  return request.get<unknown, ApiResponse<PageResult<Diary>>>('/diaries/my', { params })
}

export function getDiary(id: number) {
  return request.get<unknown, ApiResponse<Diary>>(`/diaries/${id}`)
}

export function createDiary(payload: DiaryPayload) {
  return request.post<unknown, ApiResponse<Diary>>('/diaries', payload)
}

export function updateDiary(id: number, payload: DiaryPayload) {
  return request.put<unknown, ApiResponse<Diary>>(`/diaries/${id}`, payload)
}

export function deleteDiary(id: number) {
  return request.delete<unknown, ApiResponse<void>>(`/diaries/${id}`)
}
