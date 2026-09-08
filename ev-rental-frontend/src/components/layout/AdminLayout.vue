<template>
  <el-container class="admin-layout">
    <!-- 左侧菜单 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="aside-logo">
        <el-icon :size="28" color="#38b48b"><Lightning /></el-icon>
        <span v-show="!isCollapse" class="logo-text">后台管理</span>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        router
        background-color="#1e272e"
        text-color="#b2bec3"
        active-text-color="#38b48b"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>数据统计</template>
        </el-menu-item>
        <el-menu-item index="/admin/vehicle">
          <el-icon><Van /></el-icon>
          <template #title>车辆管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/store">
          <el-icon><Shop /></el-icon>
          <template #title>门店管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/order">
          <el-icon><Document /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/review">
          <el-icon><Star /></el-icon>
          <template #title>评价管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/operation">
          <el-icon><Tools /></el-icon>
          <template #title>运维管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/deposit">
          <el-icon><Wallet /></el-icon>
          <template #title>押金管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/verify">
          <el-icon><Stamp /></el-icon>
          <template #title>认证审核</template>
        </el-menu-item>
        <el-menu-item index="/admin/invoice">
          <el-icon><Tickets /></el-icon>
          <template #title>发票管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/promotion">
          <el-icon><Discount /></el-icon>
          <template #title>促销活动</template>
        </el-menu-item>
        <el-menu-item index="/">
          <el-icon><Back /></el-icon>
          <template #title>返回前台</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧内容 -->
    <el-container>
      <el-header class="admin-header">
        <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="20">
          <Fold v-if="!isCollapse" />
          <Expand v-else />
        </el-icon>

        <el-breadcrumb separator="/">
          <el-breadcrumb-item>后台管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
        </el-breadcrumb>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="admin-user">
              <el-avatar :size="32">{{ userStore.userInfo.username?.charAt(0) }}</el-avatar>
              <span>{{ userStore.userInfo.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

function handleCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出')
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background: #1e272e;
  transition: width 0.3s;
  overflow: hidden;
}

.aside-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid #2d3436;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.admin-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 20px;
  height: 56px;
}

.collapse-btn {
  cursor: pointer;
  color: #666;
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
  font-size: 14px;
}

.admin-main {
  background: #f0f2f5;
  min-height: 0;
}
</style>
