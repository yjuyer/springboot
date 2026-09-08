<template>
  <div class="verify-page">
    <h2 class="page-title">实名认证</h2>

    <!-- 步骤条 -->
    <el-steps :active="currentStep" finish-status="success" align-center class="steps">
      <el-step title="上传身份证" />
      <el-step title="信息确认" />
      <el-step title="活体认证" />
      <el-step title="认证完成" />
    </el-steps>

    <!-- 步骤1：上传身份证 -->
    <div v-if="currentStep === 0" class="step-card">
      <h3>请上传身份证照片</h3>
      <p class="step-desc">请确保照片清晰、完整，支持 JPG / PNG 格式</p>

      <div class="idcard-upload-row">
        <!-- 正面 -->
        <div class="idcard-upload-item">
          <div class="idcard-label">身份证正面（人像面）</div>
          <div
            class="idcard-box"
            :class="{ 'has-image': idCardFront }"
            @click="triggerUpload('front')"
          >
            <img v-if="idCardFront" :src="idCardFront" class="idcard-preview" />
            <div v-else class="idcard-placeholder">
              <el-icon :size="40"><CreditCard /></el-icon>
              <span>点击上传正面</span>
            </div>
          </div>
        </div>

        <!-- 背面 -->
        <div class="idcard-upload-item">
          <div class="idcard-label">身份证背面（国徽面）</div>
          <div
            class="idcard-box"
            :class="{ 'has-image': idCardBack }"
            @click="triggerUpload('back')"
          >
            <img v-if="idCardBack" :src="idCardBack" class="idcard-preview" />
            <div v-else class="idcard-placeholder">
              <el-icon :size="40"><CreditCard /></el-icon>
              <span>点击上传背面</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 隐藏的上传input -->
      <input ref="frontInput" type="file" accept="image/jpeg,image/png" style="display:none"
             @change="(e) => handleIdCardUpload(e, 'front')" />
      <input ref="backInput" type="file" accept="image/jpeg,image/png" style="display:none"
             @change="(e) => handleIdCardUpload(e, 'back')" />

      <el-button type="primary" size="large" :disabled="!idCardFront"
                 style="width:200px;margin-top:24px" @click="goStep(1)">
        下一步
      </el-button>
    </div>

    <!-- 步骤2：信息确认 -->
    <div v-if="currentStep === 1" class="step-card">
      <h3>请确认身份信息</h3>
      <p class="step-desc">OCR自动识别结果，请核对无误后提交</p>

      <el-form :model="verifyForm" label-width="90px" class="verify-form">
        <el-form-item label="姓名">
          <el-input v-model="verifyForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="verifyForm.idCard" placeholder="请输入身份证号" maxlength="18" />
        </el-form-item>
        <el-form-item label="住址">
          <el-input v-model="verifyForm.address" disabled />
        </el-form-item>
        <el-form-item label="出生日期">
          <el-input v-model="verifyForm.birth" disabled />
        </el-form-item>
      </el-form>

      <div class="step-actions">
        <el-button @click="goStep(0)">上一步</el-button>
        <el-button type="primary" @click="goStep(2)">下一步：活体认证</el-button>
      </div>
    </div>

    <!-- 步骤3：活体认证 -->
    <div v-if="currentStep === 2" class="step-card">
      <h3>活体认证</h3>
      <p class="step-desc">请根据提示完成动作，确保为本人操作</p>

      <!-- 动作指令 -->
      <div class="action-guide" v-if="livenessActions.length > 0">
        <div class="action-title">请依次完成以下动作：</div>
        <div class="action-list">
          <span v-for="(action, i) in livenessActions" :key="i" class="action-tag">
            {{ i + 1 }}. {{ action }}
          </span>
        </div>
      </div>

      <!-- 摄像头区域 -->
      <div class="camera-area">
        <video ref="videoRef" class="camera-video" autoplay playsinline></video>
        <canvas ref="canvasRef" style="display:none"></canvas>

        <div class="camera-overlay" v-if="!cameraStarted">
          <el-icon :size="48" color="#fff"><Camera /></el-icon>
          <span>点击下方按钮开启摄像头</span>
        </div>
      </div>

      <div class="step-actions">
        <el-button @click="goStep(1)">上一步</el-button>
        <el-button v-if="!cameraStarted" type="primary" @click="startCamera">
          开启摄像头
        </el-button>
        <el-button v-if="cameraStarted" type="primary" :loading="verifying"
                   @click="captureAndVerify">
          {{ verifying ? '认证中...' : '拍照认证' }}
        </el-button>
      </div>
    </div>

    <!-- 步骤4：认证完成 -->
    <div v-if="currentStep === 3" class="step-card result-card">
      <el-icon :size="72" color="#38b48b"><CircleCheckFilled /></el-icon>
      <h2 class="result-title">实名认证成功</h2>
      <div class="result-info">
        <p>姓名：{{ verifyForm.realName }}</p>
        <p>身份证号：{{ maskIdCard(verifyForm.idCard) }}</p>
        <p v-if="verifyResult.livenessScore">活体检测分数：{{ verifyResult.livenessScore }}</p>
        <p v-if="verifyResult.similarity">人脸相似度：{{ (verifyResult.similarity * 100).toFixed(1) }}%</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/profile')">
        返回个人中心
      </el-button>
    </div>
  </div>
</template>

<script setup>
/**
 * 实名认证页面
 *
 * 流程：上传身份证 → OCR识别 → 信息确认 → 活体认证 → 完成
 */
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

// ==================== 步骤控制 ====================
const currentStep = ref(0)
function goStep(step) {
  currentStep.value = step
  if (step === 2 && !cameraStarted.value) {
    // 进入活体认证时不需要自动开启
  }
}

// ==================== 身份证上传 ====================
const frontInput = ref(null)
const backInput = ref(null)
const idCardFront = ref('')
const idCardBack = ref('')

function triggerUpload(side) {
  if (side === 'front') frontInput.value?.click()
  else backInput.value?.click()
}

async function handleIdCardUpload(e, side) {
  const file = e.target.files[0]
  if (!file) return

  // 校验
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    ElMessage.error('仅支持 JPG / PNG')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('文件不能超过 2MB')
    return
  }

  // 本地预览
  const localUrl = URL.createObjectURL(file)
  if (side === 'front') idCardFront.value = localUrl
  else idCardBack.value = localUrl

  // 上传并OCR识别
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('side', side)

    const res = await request.post('/user/uploadIdCard', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (side === 'front') {
      verifyForm.realName = res.data.name || ''
      verifyForm.idCard = res.data.idCard || ''
      verifyForm.address = res.data.address || ''
      verifyForm.birth = res.data.birth || ''
      ElMessage.success('身份证识别成功，请核对信息')
    } else {
      ElMessage.success('身份证背面上传成功')
    }
  } catch (err) {
    ElMessage.error('上传失败，请重试')
  }

  e.target.value = ''
}

// ==================== 信息确认 ====================
const verifyForm = reactive({
  realName: '',
  idCard: '',
  address: '',
  birth: ''
})

// ==================== 活体认证 ====================
const videoRef = ref(null)
const canvasRef = ref(null)
const cameraStarted = ref(false)
const verifying = ref(false)
const livenessActions = ref(['眨眼', '张嘴', '点头'])
const verifyResult = ref({})

// 获取活体动作指令
async function fetchActions() {
  try {
    const res = await request.post('/user/livenessActions')
    livenessActions.value = res.data
  } catch (e) {}
}

fetchActions()

// 开启摄像头
async function startCamera() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: { facingMode: 'user', width: 640, height: 480 }
    })
    videoRef.value.srcObject = stream
    cameraStarted.value = true
  } catch (err) {
    ElMessage.error('无法访问摄像头，请检查权限')
  }
}

// 拍照并认证
async function captureAndVerify() {
  verifying.value = true
  try {
    // 拍照
    const canvas = canvasRef.value
    const video = videoRef.value
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    canvas.getContext('2d').drawImage(video, 0, 0)
    const liveImage = canvas.toDataURL('image/jpeg', 0.8)

    // 调用完整认证接口
    const res = await request.post('/user/fullVerify', {
      realName: verifyForm.realName,
      idCard: verifyForm.idCard,
      idCardImage: idCardFront.value,
      liveImage: liveImage
    })

    verifyResult.value = res.data
    ElMessage.success('实名认证成功！')
    stopCamera()
    goStep(3)

  } catch (err) {
    ElMessage.error(err.response?.data?.message || '认证失败，请重试')
  } finally {
    verifying.value = false
  }
}

// 停止摄像头
function stopCamera() {
  const video = videoRef.value
  if (video?.srcObject) {
    video.srcObject.getTracks().forEach(track => track.stop())
    video.srcObject = null
  }
  cameraStarted.value = false
}

// 身份证号脱敏
function maskIdCard(idCard) {
  if (!idCard) return ''
  return idCard.substring(0, 4) + '**********' + idCard.substring(14)
}

onUnmounted(() => stopCamera())
</script>

<style scoped>
.verify-page { max-width: 700px; margin: 0 auto; }
.page-title { font-size: 24px; margin-bottom: 24px; }
.steps { margin-bottom: 32px; }

.step-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  text-align: center;
}
.step-card h3 { font-size: 20px; margin-bottom: 8px; }
.step-desc { color: #999; margin-bottom: 24px; }

/* ========== 身份证上传 ========== */
.idcard-upload-row {
  display: flex;
  gap: 24px;
  justify-content: center;
}
.idcard-upload-item { flex: 1; max-width: 280px; }
.idcard-label { font-size: 14px; color: #666; margin-bottom: 10px; }
.idcard-box {
  height: 170px;
  border: 2px dashed #dfe6e9;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  overflow: hidden;
}
.idcard-box:hover { border-color: #38b48b; }
.idcard-box.has-image { border-style: solid; border-color: #38b48b; }
.idcard-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #b2bec3;
}
.idcard-preview { width: 100%; height: 100%; object-fit: cover; }

/* ========== 信息确认表单 ========== */
.verify-form { max-width: 400px; margin: 0 auto; text-align: left; }

/* ========== 动作指引 ========== */
.action-guide { margin-bottom: 20px; }
.action-title { font-size: 15px; margin-bottom: 10px; }
.action-tag {
  display: inline-block;
  padding: 6px 16px;
  margin: 4px;
  background: #e8f8f5;
  color: #38b48b;
  border-radius: 20px;
  font-size: 14px;
}

/* ========== 摄像头 ========== */
.camera-area {
  position: relative;
  width: 100%;
  max-width: 480px;
  height: 320px;
  margin: 0 auto;
  border-radius: 12px;
  overflow: hidden;
  background: #2d3436;
}
.camera-video { width: 100%; height: 100%; object-fit: cover; }
.camera-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: rgba(255,255,255,0.6);
}

/* ========== 认证结果 ========== */
.result-card { padding: 48px 32px; }
.result-title { color: #38b48b; margin: 16px 0 24px; }
.result-info { margin-bottom: 24px; }
.result-info p { color: #666; margin: 8px 0; font-size: 15px; }

.step-actions { margin-top: 24px; display: flex; gap: 12px; justify-content: center; }
</style>
