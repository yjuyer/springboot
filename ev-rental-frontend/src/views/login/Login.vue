<template>
  <div class="login-page">
    <div class="login-card">
      <!-- Logo -->
      <div class="login-header">
        <div class="login-logo">
          <svg viewBox="0 0 32 32" width="40" height="40" fill="none">
            <rect width="32" height="32" rx="8" fill="#38b48b"/>
            <path d="M8 20c0-4 2-8 8-8s8 4 8 8" stroke="#fff" stroke-width="2.5" stroke-linecap="round" fill="none"/>
            <circle cx="12" cy="21" r="2.5" fill="#fff"/>
            <circle cx="20" cy="21" r="2.5" fill="#fff"/>
            <path d="M10 17l3-4h6l3 4" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
          </svg>
        </div>
        <h1>e租出行</h1>
        <p>新能源汽车租赁平台</p>
      </div>

      <!-- 登录表单 -->
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large"
                    prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <div style="margin-bottom:8px">
          <router-link to="/forgot" style="color:#999;font-size:13px">忘记密码？</router-link>
        </div>
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    // 根据角色跳转
    if (userStore.role === 'USER') {
      router.push('/home')
    } else {
      router.push('/admin/dashboard')
    }
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f4f3;
}

.login-card {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.login-header h1 {
  font-size: 22px;
  color: #38b48b;
  margin: 0 0 4px;
  font-weight: 700;
}

.login-header p {
  color: #999;
  font-size: 13px;
}

.login-footer {
  text-align: center;
  color: #999;
  font-size: 14px;
}

.login-footer a {
  color: #38b48b;
  text-decoration: none;
  font-weight: 500;
}
</style>
