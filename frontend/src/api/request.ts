import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

const request: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

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
      const { status, data } = error.response
      const message = data?.message || `Request failed with status ${status}`
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
