import request from '@/api/request'
import type { ApiResponse, PageResult, Post, PostComment } from '@/types'

export interface PostListParams {
  page?: number
  size?: number
  categorySlug?: string
  keyword?: string
  cursorCreatedAt?: string
  cursorId?: number
}

export function listPosts(params: PostListParams) {
  return request.get<unknown, ApiResponse<PageResult<Post>>>('/posts', { params })
}

export function createPost(payload: {
  content: string
  images?: string | null
  plantSlug?: string | null
  categorySlug?: string | null
}) {
  return request.post<unknown, ApiResponse<Post>>('/posts', payload)
}

export function deletePost(postId: number) {
  return request.delete<unknown, ApiResponse<void>>(`/posts/${postId}`)
}

export function togglePostLike(postId: number) {
  return request.post<unknown, ApiResponse<{ liked: boolean }>>(`/posts/${postId}/like`)
}

export function listComments(postId: number, params = { page: 1, size: 50 }) {
  return request.get<unknown, ApiResponse<PageResult<PostComment>>>(`/posts/${postId}/comments`, { params })
}

export function createComment(postId: number, content: string) {
  return request.post<unknown, ApiResponse<PostComment>>(`/posts/${postId}/comments`, { content })
}
