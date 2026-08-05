export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
  hasMore?: boolean
  nextCursorId?: number | null
  nextCursorCreatedAt?: string | null
}

export interface User {
  id: number
  username: string
  account: string
  avatarUrl?: string
  bio?: string
}

export interface Plant {
  id: number
  commonName: string
  scientificName: string
  slug: string
  shortDescription?: string
  description?: string
  imageUrl?: string
  difficulty: string
  lightLevel: string
  waterFrequency: string
  isIndoor?: boolean
  isPetSafe?: boolean
  waterIntervalDaysMin?: number
  waterIntervalDaysMax?: number
}

export interface Category {
  id: number
  name: string
  slug: string
  description?: string
}

export interface PlantCollection {
  id: number
  plantId: number
  plantName?: string
  plantImage?: string
  plantSlug?: string
  nickname?: string
  location?: string
  notes?: string
  waterIntervalDays?: number
  lastWateredAt?: string
  nextWaterAt?: string
  createdAt?: string
}

export interface Post {
  id: number
  userAccount: string
  content: string
  images: string | null
  plantSlug: string | null
  categorySlug: string | null
  plantName: string | null
  likeCount: number
  commentCount: number
  createdAt: string
  username: string
  avatarUrl: string | null
  liked: boolean
}

export interface PostComment {
  id: number
  postId: number
  userAccount: string
  content: string
  createdAt: string
  username: string
  avatarUrl: string | null
}

export interface Diary {
  id: number
  userAccount: string
  plantSlug?: string
  plantName?: string
  title: string
  content?: string
  images?: string
  weather?: string
  mood?: string
  heightCm: number | null
  leafCount: number | null
  growthStage?: string
  createdAt: string
  username?: string
  avatarUrl?: string
}
