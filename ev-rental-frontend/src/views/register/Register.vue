<template>
  <div class="register-page">
    <div class="register-card">
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
        <h1>用户注册</h1>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" size="large" prefix-icon="Message" />
        </el-form-item>
        <!-- 邮箱验证码 -->
        <el-form-item prop="emailCode">
          <div style="display:flex;width:100%;gap:12px">
            <el-input v-model="form.emailCode" placeholder="请输入邮箱验证码" size="large" prefix-icon="Key" maxlength="6"
                      style="flex:1" />
            <el-button size="large" :disabled="countdown > 0 || !form.email" :loading="sendingCode"
                       @click="sendEmailCode" style="width:130px">
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（至少6位）" size="large"
                    prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" size="large"
                    prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)
let timer = null

const form = reactive({
  username: '', phone: '', email: '', emailCode: '', password: '', confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  emailCode: [{ required: true, message: '请输入邮箱验证码', trigger: 'blur' },
              { len: 6, message: '验证码为6位', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' },
             { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' },
                    { validator: validateConfirm, trigger: 'blur' }]
}

// 发送邮箱验证码
async function sendEmailCode() {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  sendingCode.value = true
  try {
    const res = await request.post('/auth/sendEmailCode', { email: form.email })
    sentCode = res.data // 演示模式
    ElMessage.success('验证码已发送到邮箱（演示模式：' + res.data + '）')
    startCountdown()
  } catch (e) {} finally {
    sendingCode.value = false
  }
}

let sentCode = ''

function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {} finally {
    loading.value = false
  }
}

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f4f3;
}

.register-card {
  width: 460px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.login-header { text-align: center; margin-bottom: 24px; }
.login-logo { display: flex; justify-content: center; margin-bottom: 12px; }
.login-header h1 { font-size: 22px; color: #38b48b; margin: 0; font-weight: 700; }
.login-footer { text-align: center; color: #999; font-size: 14px; }
.login-footer a { color: #38b48b; text-decoration: none; font-weight: 500; }
</style>
