import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  // 注意：不在这里设置全局 Content-Type。
  // JSON 对象 axios 会自动设 application/json；FormData 由浏览器自动生成 multipart/boundary。
  // 手动设置会导致 multipart 上传失败（无 boundary 或错误 Content-Type）。
})

// 轻量提示（避免为单个提示引入 element-plus 全量样式）
function showToast(message: string) {
  const existing = document.querySelector('.api-toast')
  if (existing) existing.remove()
  const el = document.createElement('div')
  el.className = 'api-toast'
  el.textContent = message
  document.body.appendChild(el)
  setTimeout(() => el.remove(), 3000)
}

// 请求拦截器：自动附加 JWT token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器：自动解包 data
request.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data
  },
  (error) => {
    if (error.response) {
      const { status, data, config } = error.response
      const message = data?.message || `Request failed with status ${status}`
      // 401 会话过期统一处理（排除登录/注册接口——它们的 401 是"密码错误"业务响应）
      const url: string = config?.url || ''
      const isAuthEndpoint = url.includes('/auth/login') || url.includes('/auth/register')
      if (status === 401 && !isAuthEndpoint) {
        localStorage.removeItem('auth_token')
        showToast('登录已过期，请重新登录')
        // 通知应用层同步登出状态（App.vue 监听后调用 auth.logout()）
        window.dispatchEvent(new CustomEvent('auth:expired'))
      } else if (status === 429) {
        showToast(message)
      }
      console.error(`[API Error] ${status}: ${message}`)
    } else if (error.request) {
      console.error('[API Error] No response received from server')
    } else {
      console.error('[API Error]', error.message)
    }
    return Promise.reject(error)
  },
)

export default request
