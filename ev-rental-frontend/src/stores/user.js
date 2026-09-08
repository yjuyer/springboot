import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

/**
 * 用户状态管理 Store
 *
 * <p>使用Pinia进行状态管理，配合pinia-plugin-persistedstate插件实现持久化</p>
 *
 * <p>状态说明：</p>
 * <ul>
 *   <li>token - JWT认证令牌，用于接口认证</li>
 *   <li>userInfo - 用户基本信息（userId、username、avatar）</li>
 *   <li>role - 用户角色（ADMIN/OPERATOR/USER）</li>
 * </ul>
 *
 * <p>方法说明：</p>
 * <ul>
 *   <li>login(username, password) - 用户登录，获取token和用户信息</li>
 *   <li>register(data) - 用户注册</li>
 *   <li>fetchUserInfo() - 获取当前用户详细信息</li>
 *   <li>logout() - 退出登录，清除所有本地存储</li>
 * </ul>
 */
export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref({})
  const role = ref('')

  /** 登录 */
  async function login(username, password) {
    const res = await request.post('/auth/login', { username, password })
    token.value = res.data.token
    // 同时存到localStorage，让Axios拦截器能读取
    localStorage.setItem('token', res.data.token)
    userInfo.value = {
      userId: res.data.userId,
      username: res.data.username,
      avatar: res.data.avatar
    }
    role.value = res.data.role
    return res.data
  }

  /** 注册 */
  async function register(data) {
    await request.post('/auth/register', data)
  }

  /** 获取用户信息 */
  async function fetchUserInfo() {
    const res = await request.get('/auth/info')
    userInfo.value = res.data
    return res.data
  }

  /** 退出登录 */
  function logout() {
    token.value = ''
    userInfo.value = {}
    role.value = ''
    localStorage.clear()
  }

  return { token, userInfo, role, login, register, fetchUserInfo, logout }
}, {
  persist: true
})
