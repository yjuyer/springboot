<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/home')">
          <span class="logo-icon">
            <svg viewBox="0 0 32 32" width="28" height="28" fill="none">
              <rect width="32" height="32" rx="8" fill="#38b48b"/>
              <path d="M8 20c0-4 2-8 8-8s8 4 8 8" stroke="#fff" stroke-width="2.5" stroke-linecap="round" fill="none"/>
              <circle cx="12" cy="21" r="2.5" fill="#fff"/>
              <circle cx="20" cy="21" r="2.5" fill="#fff"/>
              <path d="M10 17l3-4h6l3 4" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
            </svg>
          </span>
          <span class="logo-text">e租出行</span>
        </div>

        <nav class="nav-menu">
          <router-link to="/home" class="nav-item">首页</router-link>
          <router-link to="/vehicle" class="nav-item">我要租车</router-link>
          <router-link to="/stores" class="nav-item">门店查询</router-link>
          <router-link to="/charging-map" class="nav-item">充电地图</router-link>
          <span class="nav-item" @click="goProfile">个人中心</span>
        </nav>

        <div class="user-area">
          <template v-if="userStore.token">
            <!-- 消息通知铃铛 -->
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-bell">
              <el-icon :size="22" class="bell-icon" @click="router.push('/message')"><Bell /></el-icon>
            </el-badge>
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userStore.userInfo.avatar || undefined">
                  {{ userStore.userInfo.username?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="message">消息中心</el-dropdown-item>
                  <el-dropdown-item command="order">我的订单</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.role !== 'USER'" command="admin">后台管理</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button class="btn-login" @click="router.push('/login')">登录</el-button>
            <el-button class="btn-register" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <div class="footer-inner">
        <p>e租出行 &copy; 2024 | 新能源汽车租赁一站式服务平台</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import { notificationApi } from '@/api/vehicle'
import { ref, onMounted, onUnmounted, watch } from 'vue'

const router = useRouter()
const userStore = useUserStore()

// 未读消息数量
const unreadCount = ref(0)
let pollTimer = null

/** 获取未读消息数量 */
async function fetchUnreadCount() {
  if (!userStore.token) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await notificationApi.getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (e) {
    // 静默失败
  }
}

/** 开始轮询 */
function startPolling() {
  stopPolling()
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000) // 每30秒轮询一次
}

/** 停止轮询 */
function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 监听登录状态变化
watch(() => userStore.token, (newToken) => {
  if (newToken) {
    startPolling()
  } else {
    stopPolling()
    unreadCount.value = 0
  }
})

onMounted(() => {
  if (userStore.token) {
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})

function goProfile() {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/profile')
}

function handleCommand(cmd) {
  switch (cmd) {
    case 'message': router.push('/message'); break
    case 'order': router.push('/order'); break
    case 'admin': router.push('/admin/dashboard'); break
    case 'logout':
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
      break
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f7f8fa;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  margin-right: 32px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #38b48b;
  letter-spacing: 1px;
  white-space: nowrap;
}

.nav-menu {
  display: flex;
  gap: 28px;
  flex: 1;
}

.nav-item {
  text-decoration: none;
  color: #333;
  font-size: 15px;
  font-weight: 500;
  padding: 8px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.nav-item:hover,
.nav-item.router-link-active {
  color: #38b48b;
  border-bottom-color: #38b48b;
}

.nav-item:last-child {
  cursor: pointer;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notification-bell {
  cursor: pointer;
  margin-right: 4px;
}

.bell-icon {
  color: #666;
  transition: color 0.2s;
  vertical-align: middle;
}

.bell-icon:hover {
  color: #38b48b;
}

.btn-login {
  background: #38b48b;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 500;
}
.btn-login:hover {
  background: #2d9a76;
  color: #fff;
}

.btn-register {
  border: 1px solid #38b48b;
  color: #38b48b;
  border-radius: 6px;
  font-weight: 500;
  background: #fff;
}
.btn-register:hover {
  background: #38b48b;
  color: #fff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
}

.username {
  font-size: 14px;
}

.main-content {
  flex: 1;
  width: 100%;
}

.footer {
  background: #2d3436;
  color: #b2bec3;
  text-align: center;
  padding: 20px;
  font-size: 13px;
}
</style>
