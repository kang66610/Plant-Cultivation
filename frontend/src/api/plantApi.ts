import request from '@/api/request'
import type { ApiResponse, Category, PageResult, Plant } from '@/types'

export interface PlantListParams {
  page?: number
  size?: number
  search?: string
  category?: string
  light?: string
  water?: string
  difficulty?: string
  indoor?: boolean
  petSafe?: boolean
}

export function listPlants(params: PlantListParams) {
  return request.get<unknown, ApiResponse<PageResult<Plant>>>('/plants', { params })
}

export function listCategories() {
  return request.get<unknown, ApiResponse<Category[]>>('/categories')
}

export function listFeaturedPlants(limit = 6) {
  return request.get<unknown, ApiResponse<Plant[]>>('/plants/featured', { params: { limit } })
}

export function getPlant(slug: string) {
  return request.get<unknown, ApiResponse<Plant>>(`/plants/${slug}`)
}
