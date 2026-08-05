import request from '@/api/request'
import type { ApiResponse } from '@/types'

export function uploadImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<unknown, ApiResponse<string>>('/upload/image', formData)
}
