<template>
  <div class="avatar-upload-card">
    <h3 class="card-title">个人头像</h3>

    <!-- 头像区域 -->
    <div class="avatar-wrapper" @click="triggerUpload">
      <div class="avatar-circle">
        <img :src="previewUrl || defaultAvatar" class="avatar-img" />
        <div class="avatar-overlay">
          <el-icon :size="24"><Camera /></el-icon>
          <span>{{ previewUrl ? '更换头像' : '上传头像' }}</span>
        </div>
      </div>
    </div>

    <p class="avatar-hint">支持 JPG / PNG，文件小于 2MB</p>

    <!-- 隐藏的上传控件 -->
    <input ref="fileInput" type="file" accept="image/jpeg,image/png"
           style="display:none" @change="onFileSelected" />
  </div>
</template>

<script setup>
/**
 * 用户头像上传组件
 * 点击头像 → 选文件 → 校验 → 直接上传 → 立即预览
 */
import { ref, watch } from 'vue'
import { Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue', 'success'])

const fileInput = ref(null)
const previewUrl = ref(props.modelValue)
const uploading = ref(false)

// 默认头像（SVG）
const defaultAvatar = ref(
  'data:image/svg+xml,' + encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120"><circle cx="60" cy="60" r="60" fill="#dfe6e9"/><circle cx="60" cy="45" r="20" fill="#b2bec3"/><ellipse cx="60" cy="100" rx="35" ry="28" fill="#b2bec3"/></svg>`
  )
)

// 监听props变化
watch(() => props.modelValue, (val) => { previewUrl.value = val })

// 触发文件选择
function triggerUpload() {
  fileInput.value?.click()
}

// 文件选择后 → 校验 → 上传
async function onFileSelected(e) {
  const file = e.target.files[0]
  if (!file) return

  // 校验格式
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    ElMessage.error('仅支持 JPG / PNG 格式')
    e.target.value = ''
    return
  }

  // 校验大小
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 2MB')
    e.target.value = ''
    return
  }

  // 立即本地预览
  previewUrl.value = URL.createObjectURL(file)

  // 上传
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)

    const res = await request.post('/file/upload', formData)
    const avatarUrl = res.data

    // 更新头像到用户信息
    await request.put('/user/profile', { avatar: avatarUrl })

    previewUrl.value = avatarUrl
    emit('update:modelValue', avatarUrl)
    emit('success', avatarUrl)
    ElMessage.success('头像上传成功')
  } catch (err) {
    ElMessage.error('上传失败，请重试')
    previewUrl.value = props.modelValue
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}
</script>

<style scoped>
.avatar-upload-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #2d3436;
  margin-bottom: 24px;
}

.avatar-wrapper { display: inline-block; cursor: pointer; }

.avatar-circle {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid #38b48b;
  box-shadow: 0 4px 15px rgba(0,184,148,0.25);
  transition: all 0.3s;
}
.avatar-circle:hover {
  box-shadow: 0 6px 20px rgba(0,184,148,0.4);
  transform: scale(1.03);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-overlay {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0,0,0,0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-circle:hover .avatar-overlay { opacity: 1; }
.avatar-overlay span { font-size: 12px; margin-top: 6px; }

.avatar-hint { margin-top: 14px; color: #b2bec3; font-size: 13px; }
</style>
