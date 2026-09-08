<template>
  <div class="license-page">
    <h2 class="page-title">驾驶证上传</h2>

    <!-- 上传卡片 -->
    <div class="upload-card">
      <div class="upload-area" @click="triggerUpload">
        <img v-if="previewUrl" :src="previewUrl" class="license-preview" />
        <div v-else class="upload-placeholder">
          <el-icon :size="48"><Upload /></el-icon>
          <p>点击上传驾驶证照片</p>
          <span>支持 JPG / PNG，小于 2MB</span>
        </div>
        <!-- Hover遮罩 -->
        <div v-if="previewUrl" class="upload-overlay">
          <el-icon :size="28"><RefreshRight /></el-icon>
          <span>重新上传</span>
        </div>
      </div>

      <input ref="fileInput" type="file" accept="image/jpeg,image/png" style="display:none"
             @change="handleUpload" />

      <!-- 上传状态 -->
      <div class="status-bar">
        <el-tag :type="statusType" size="large">
          {{ statusText }}
        </el-tag>
      </div>

      <!-- 操作按钮 -->
      <div class="actions">
        <el-button type="primary" size="large" :loading="uploading" :disabled="!selectedFile"
                   @click="doUpload" style="width:200px">
          {{ uploading ? '上传中...' : '提交审核' }}
        </el-button>
      </div>
    </div>

    <!-- 上传须知 -->
    <div class="notice-card">
      <h3>上传须知</h3>
      <ul>
        <li>请上传驾驶证正页照片（含姓名、证号、准驾车型等信息）</li>
        <li>照片需清晰完整，无遮挡、无反光</li>
        <li>支持 JPG / PNG 格式，文件小于 2MB</li>
        <li>审核通常在 1-3 个工作日内完成</li>
        <li>驾驶证认证通过后可享受更多租车优惠</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
/**
 * 驾驶证上传页面
 *
 * 功能：
 * 1. 点击上传驾驶证照片
 * 2. 本地预览
 * 3. 校验格式和大小
 * 4. 提交到后端审核
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api/vehicle'

const router = useRouter()
const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const uploading = ref(false)
const licenseStatus = ref(0)

// 页面加载时获取当前状态
onMounted(async () => {
  try {
    const res = await userApi.getProfile()
    licenseStatus.value = res.data.licenseStatus || 0
    if (res.data.licenseImg) {
      previewUrl.value = res.data.licenseImg
    }
  } catch (e) {}
})

// 状态文案
const statusText = computed(() => {
  return ['未上传', '待审核', '已通过', '已拒绝'][licenseStatus.value] || '未上传'
})
const statusType = computed(() => {
  return ['', 'warning', 'success', 'danger'][licenseStatus.value] || 'info'
})

// 触发文件选择
function triggerUpload() {
  fileInput.value?.click()
}

// 文件选择后
function handleUpload(e) {
  const file = e.target.files[0]
  if (!file) return

  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    ElMessage.error('仅支持 JPG / PNG 格式')
    e.target.value = ''
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 2MB')
    e.target.value = ''
    return
  }

  selectedFile.value = file
  previewUrl.value = URL.createObjectURL(file)
  licenseStatus.value = 0
  e.target.value = ''
}

// 上传
async function doUpload() {
  if (!selectedFile.value) return

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    await userApi.uploadLicense(formData)

    ElMessage.success('驾驶证上传成功，请等待审核')
    licenseStatus.value = 1 // 待审核
    selectedFile.value = null
  } catch (err) {
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.license-page { max-width: 600px; margin: 0 auto; }
.page-title { font-size: 24px; margin-bottom: 24px; }

/* ========== 上传卡片 ========== */
.upload-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  text-align: center;
}

.upload-area {
  position: relative;
  width: 100%;
  height: 240px;
  border: 2px dashed #dfe6e9;
  border-radius: 12px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.upload-area:hover { border-color: #38b48b; }

.license-preview {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.upload-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #b2bec3;
}
.upload-placeholder p { font-size: 16px; color: #666; }
.upload-placeholder span { font-size: 13px; }

.upload-overlay {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0,0,0,0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}
.upload-area:hover .upload-overlay { opacity: 1; }

.status-bar { margin-bottom: 16px; }
.actions { margin-top: 8px; }

/* ========== 须知卡片 ========== */
.notice-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-top: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.notice-card h3 { font-size: 16px; margin-bottom: 12px; color: #2d3436; }
.notice-card ul { padding-left: 20px; }
.notice-card li { color: #666; font-size: 13px; line-height: 2; }
</style>
