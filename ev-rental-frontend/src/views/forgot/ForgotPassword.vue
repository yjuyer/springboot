<template>
  <div class="forgot-page">
    <div class="forgot-card">
      <!-- 头部 -->
      <div class="card-header">
        <el-icon :size="40" color="#38b48b"><Key /></el-icon>
        <h1>找回密码</h1>
        <p>通过手机号重置您的密码</p>
      </div>

      <!-- 步骤条 -->
      <el-steps :active="step" finish-status="success" align-center style="margin-bottom:24px">
        <el-step title="验证手机" />
        <el-step title="重置密码" />
        <el-step title="完成" />
      </el-steps>

      <!-- 步骤1：输入手机号 -->
      <div v-if="step === 0">
        <el-form ref="phoneFormRef" :model="form" :rules="phoneRules">
          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="请输入注册手机号" size="large"
                      prefix-icon="Iphone" maxlength="11" />
          </el-form-item>
        </el-form>

        <el-button type="primary" size="large" style="width:100%" :loading="sending"
                   @click="handleSendCode">
          {{ sending ? '发送中...' : '获取验证码' }}
        </el-button>

        <!-- 验证码输入 -->
        <div v-if="codeSent" style="margin-top:20px">
          <p class="code-hint">验证码已发送至 {{ maskPhone(form.phone) }}</p>
          <p class="code-hint" style="color:#38b48b">演示模式：验证码为 {{ sentCode }}</p>
          <el-form ref="codeFormRef" :model="form" :rules="codeRules">
            <el-form-item prop="code">
              <el-input v-model="form.code" placeholder="请输入6位验证码" size="large"
                        prefix-icon="Key" maxlength="6" />
            </el-form-item>
          </el-form>
          <el-button type="primary" size="large" style="width:100%" :loading="verifying"
                     @click="handleVerifyCode">
            {{ verifying ? '验证中...' : '下一步' }}
          </el-button>
          <el-button link style="width:100%;margin-top:8px" :disabled="countdown > 0"
                     @click="handleSendCode">
            {{ countdown > 0 ? `${countdown}s 后重发` : '重新发送' }}
          </el-button>
        </div>
      </div>

      <!-- 步骤2：重置密码 -->
      <div v-if="step === 1">
        <el-form ref="resetFormRef" :model="form" :rules="resetRules">
          <el-form-item prop="newPassword">
            <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码（至少6位）"
                      size="large" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="请确认新密码"
                      size="large" prefix-icon="Lock" show-password />
          </el-form-item>
        </el-form>
        <el-button type="primary" size="large" style="width:100%" :loading="resetting"
                   @click="handleReset">
          {{ resetting ? '重置中...' : '确认重置' }}
        </el-button>
      </div>

      <!-- 步骤3：完成 -->
      <div v-if="step === 2" style="text-align:center;padding:20px 0">
        <el-icon :size="64" color="#38b48b"><CircleCheckFilled /></el-icon>
        <h2 style="color:#38b48b;margin:16px 0 8px">密码重置成功</h2>
        <p style="color:#999;margin-bottom:24px">请使用新密码登录</p>
        <el-button type="primary" size="large" @click="$router.push('/login')">
          去登录
        </el-button>
      </div>

      <!-- 返回登录 -->
      <div class="card-footer" v-if="step < 2">
        想起密码了？<router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 忘记密码页面
 *
 * 流程：输入手机号 → 发送验证码 → 输入验证码 → 重置密码 → 完成
 */
import { ref, reactive, onUnmounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const step = ref(0)
const sending = ref(false)
const codeSent = ref(false)
const sentCode = ref('')
const verifying = ref(false)
const resetting = ref(false)
const countdown = ref(0)
let timer = null

const phoneFormRef = ref(null)
const codeFormRef = ref(null)
const resetFormRef = ref(null)

const form = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const codeRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const resetRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' }
  ]
}

// 发送验证码
async function handleSendCode() {
  await phoneFormRef.value.validate()
  sending.value = true
  try {
    const res = await request.post('/auth/sendCode', { phone: form.phone })
    sentCode.value = res.data // 演示模式返回验证码
    codeSent.value = true
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    sending.value = false
  }
}

// 倒计时
function startCountdown() {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

// 验证验证码
async function handleVerifyCode() {
  await codeFormRef.value.validate()
  verifying.value = true
  try {
    await request.post('/auth/verifyCode', { phone: form.phone, code: form.code })
    step.value = 1
  } catch (e) {} finally {
    verifying.value = false
  }
}

// 重置密码
async function handleReset() {
  await resetFormRef.value.validate()
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  resetting.value = true
  try {
    await request.post('/auth/resetPassword', {
      phone: form.phone,
      code: form.code,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })
    step.value = 2
  } catch (e) {} finally {
    resetting.value = false
  }
}

function maskPhone(phone) {
  if (!phone) return ''
  return phone.substring(0, 3) + '****' + phone.substring(7)
}

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.forgot-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f4f3;
}

.forgot-card {
  width: 440px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.card-header {
  text-align: center;
  margin-bottom: 24px;
}
.card-header h1 { font-size: 22px; color: #38b48b; margin: 12px 0 4px; font-weight: 700; }
.card-header p { color: #999; font-size: 13px; }

.code-hint {
  text-align: center;
  color: #999;
  font-size: 13px;
  margin-bottom: 12px;
}

.card-footer {
  text-align: center;
  color: #999;
  font-size: 14px;
  margin-top: 20px;
}
.card-footer a { color: #38b48b; text-decoration: none; font-weight: 500; }
</style>
