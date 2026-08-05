import request from '@/api/request'
import type { ApiResponse, PageResult, PlantCollection } from '@/types'

export function listCollections(params = { page: 1, size: 100 }) {
  return request.get<unknown, ApiResponse<PageResult<PlantCollection>>>('/collections', { params })
}

export function updateCollection(
  plantId: number,
  payload: Pick<PlantCollection, 'nickname' | 'location' | 'notes' | 'waterIntervalDays'>,
) {
  return request.put<unknown, ApiResponse<PlantCollection>>(`/collections/${plantId}`, payload)
}

export function markWatered(plantId: number) {
  return request.post<unknown, ApiResponse<void>>(`/collections/${plantId}/water`)
}

export function removeCollection(plantId: number) {
  return request.delete<unknown, ApiResponse<void>>(`/collections/${plantId}`)
}
