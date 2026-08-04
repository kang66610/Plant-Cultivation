// Types barrel export

/** 用户收藏的植物（含浇水提醒字段，对应后端 UserPlantCollection 实体） */
export interface PlantCollection {
  id: number
  plantId: number
  plantName?: string
  plantImage?: string
  plantSlug?: string
  waterIntervalDays?: number
  lastWateredAt?: string
  nextWaterAt?: string
  createdAt?: string
}
