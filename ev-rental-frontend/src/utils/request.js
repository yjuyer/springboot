import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/**
 * Axios HTTP请求封装
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>自动携带JWT Token - 从localStorage读取token并添加到请求头</li>
 *   <li>统一错误处理 - 后端返回非200状态码时显示错误提示</li>
 *   <li>401自动跳转登录 - token过期或无效时清除本地存储并跳转登录页</li>
 *   <li>基础URL代理 - /api前缀的请求通过Vite代理转发到后端</li>
 * </ul>
 *
 * @example
 * // GET请求
 * const res = await request.get('/api/user/profile')
 *
 * // POST请求
 * const res = await request.post('/api/auth/login', { username, password })
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器 - 附加Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器 - 统一处理
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(error.response?.data?.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
