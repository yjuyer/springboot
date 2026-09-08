<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="vehicle" class="detail-content">
      <div class="page-header">
        <h2 class="page-title">{{ vehicle.model }}</h2>
        <span class="back-link" @click="$router.back()">← 返回</span>
      </div>

      <div class="detail-main">
        <!-- 特斯拉风格图片展示区 -->
        <div class="gallery-container">
          <!-- 主图展示区 -->
          <div class="main-image-wrapper">
            <transition name="fade" mode="out-in">
              <el-image
                :key="activeImageIndex"
                :src="activeImage"
                fit="cover"
                class="main-image"
                :preview-src-list="allImages"
                :initial-index="activeImageIndex"
                preview-teleported
              >
                <template #placeholder>
                  <div class="img-placeholder">
                    <div class="loading-spinner"></div>
                    <span>加载中...</span>
                  </div>
                </template>
                <template #error>
                  <div class="img-placeholder">
                    <el-icon :size="64" color="#ccc"><PictureFilled /></el-icon>
                    <span>暂无图片</span>
                  </div>
                </template>
              </el-image>
            </transition>

            <!-- 毛玻璃悬浮信息 -->
            <div class="image-overlay">
              <div class="overlay-content">
                <div class="overlay-title">{{ vehicle.model }}</div>
                <div class="overlay-subtitle">{{ vehicleTypeText(vehicle.vehicleType) }} · {{ vehicle.seatCount }}座</div>
              </div>
            </div>

            <!-- 状态标签 -->
            <el-tag :type="statusType(vehicle.status)" class="status-badge" size="large" effect="dark">
              {{ statusText(vehicle.status) }}
            </el-tag>

            <!-- 图片指示器 -->
            <div class="image-indicators">
              <div
                v-for="(img, idx) in allImages"
                :key="idx"
                class="indicator"
                :class="{ active: idx === activeImageIndex }"
                @click="setActiveImage(idx)"
              ></div>
            </div>
          </div>

          <!-- 缩略图区域 -->
          <div class="thumbnail-section">
            <div
              v-for="(img, idx) in thumbnailImages"
              :key="idx"
              class="thumbnail-card"
              :class="{ active: idx === activeThumbnailIndex }"
              @click="setActiveThumbnail(idx)"
            >
              <el-image
                :src="img.url"
                fit="cover"
                class="thumbnail-image"
              >
                <template #error>
                  <div class="thumb-placeholder">
                    <el-icon :size="24" color="#ccc"><PictureFilled /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="thumbnail-label">{{ img.label }}</div>
            </div>
          </div>
        </div>

        <div class="detail-info">
          <h1>{{ vehicle.model }}</h1>
          <p class="sub-info">
            {{ vehicleTypeText(vehicle.vehicleType) }} · {{ vehicle.seatCount }}座 · {{ vehicle.color }}
          </p>

          <div class="price-box">
            <span class="price-big">¥{{ vehicle.dailyPrice }}</span>
            <span class="price-unit">/天</span>
            <span class="deposit">押金 ¥{{ vehicle.deposit }}</span>
          </div>

          <!-- 车辆参数介绍 -->
          <div class="intro-section">
            <div class="intro-title">车辆参数</div>
            <div class="intro-grid">
              <div class="intro-item">
                <span class="intro-label">品&emsp;&emsp;牌</span>
                <span class="intro-value">{{ vehicle.brandName || getBrandShort(vehicle.brandId) || '-' }}</span>
              </div>
              <div class="intro-item">
                <span class="intro-label">续航里程</span>
                <span class="intro-value">{{ vehicle.batteryRange || '-' }} km</span>
              </div>
              <div class="intro-item">
                <span class="intro-label">电机类型</span>
                <span class="intro-value">{{ vehicle.motorType || '永磁同步电机' }}</span>
              </div>
              <div class="intro-item">
                <span class="intro-label">电池容量</span>
                <span class="intro-value">{{ vehicle.batteryCapacity || '-' }} kWh</span>
              </div>
              <div class="intro-item">
                <span class="intro-label">座&emsp;&emsp;位</span>
                <span class="intro-value">{{ vehicle.seatCount || 5 }} 座</span>
              </div>
              <div class="intro-item">
                <span class="intro-label">快充时间</span>
                <span class="intro-value">{{ vehicle.fastCharge || '-' }} h</span>
              </div>
              <div class="intro-item">
                <span class="intro-label">最高时速</span>
                <span class="intro-value">{{ vehicle.topSpeed || '-' }} km/h</span>
              </div>
              <div class="intro-item">
                <span class="intro-label">当前电量</span>
                <span class="intro-value">{{ vehicle.batteryLevel || 0 }}%</span>
              </div>
            </div>
          </div>

          <div class="battery-display">
            <span class="battery-label">电量</span>
            <el-progress
              :percentage="vehicle.batteryLevel || 0"
              :stroke-width="12"
              :color="batteryColor(vehicle.batteryLevel)"
              :show-text="false"
              style="flex:1"
            />
            <span class="battery-val">{{ vehicle.batteryLevel || 0 }}%</span>
          </div>

          <div v-if="vehicle.storeName" class="store-info" @click="goStoreMap">
            <el-icon color="#38b48b"><Shop /></el-icon>
            <div class="store-text">
              <span class="store-name">所在门店：{{ vehicle.storeName }}</span>
              <span v-if="vehicle.storeAddress" class="store-address">{{ vehicle.storeAddress }}</span>
            </div>
            <el-icon class="map-arrow" color="#38b48b"><Location /></el-icon>
          </div>

          <div class="detail-actions">
            <el-button v-if="vehicle.status === 0 && credit >= 80" type="primary" size="large" @click="goBook" style="width:200px">
              立即预约
            </el-button>
            <el-button v-else-if="vehicle.status === 0 && credit < 80" size="large" disabled style="width:200px">
              信誉分不足
            </el-button>
            <el-button v-else size="large" disabled style="width:200px">
              暂不可租
            </el-button>
            <el-button size="large" :type="isFav ? 'danger' : 'default'" @click="handleToggleFav">
              <el-icon style="margin-right:4px"><Star /></el-icon>
              {{ isFav ? '已收藏' : '收藏' }}
            </el-button>
            <el-button size="large" @click="$router.back()" plain>返回</el-button>
          </div>
        </div>
      </div>

      <div v-if="vehicle.description" class="detail-desc">
        <h3>车辆简介</h3>
        <p>{{ vehicle.description }}</p>
      </div>

      <div class="detail-desc review-section">
        <div class="review-header">
          <h3>车辆评价</h3>
          <div class="review-summary">
            <el-rate :model-value="reviewSummary.avgRating || 0" disabled allow-half />
            <span>{{ reviewSummary.avgRating || 0 }} 分 · {{ reviewSummary.count || 0 }} 条评价</span>
          </div>
        </div>
        <el-empty v-if="reviews.length === 0" description="暂无评价" :image-size="80" />
        <div v-else class="review-list">
          <div v-for="item in reviews" :key="item.id" class="review-item">
            <div class="review-item-head">
              <strong>{{ item.anonymous === 1 ? '匿名用户' : item.username }}</strong>
              <el-rate :model-value="item.rating" disabled />
            </div>
            <div class="review-tags" v-if="item.tags">
              <el-tag v-for="tag in item.tags.split(',')" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
            </div>
            <p>{{ item.content || '用户未填写文字评价' }}</p>
            <div class="review-time">{{ formatTime(item.createTime) }}</div>
            <div v-if="item.reply" class="review-reply">商家回复：{{ item.reply }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { vehicleApi, userApi, favoriteApi, reviewApi } from '@/api/vehicle'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { PictureFilled, Star } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const vehicle = ref(null)
const credit = ref(100)
const activeImageIndex = ref(0)
const activeThumbnailIndex = ref(0)
const isFav = ref(false)
const reviewSummary = ref({ avgRating: 0, count: 0 })
const reviews = ref([])

// 所有图片列表
const allImages = computed(() => {
  if (!vehicle.value) return []
  const images = vehicle.value.images || []
  const result = images
    .sort((a, b) => (a.sortNum || 0) - (b.sortNum || 0))
    .map(img => resolveImageUrl(img.imageUrl))
    .filter(Boolean)
  // 如果没有多图，使用封面图
  if (result.length === 0 && (vehicle.value.image || vehicle.value.mainImageUrl)) {
    result.push(getCoverImage(vehicle.value))
  }
  return result
})

// 当前显示的图片
const activeImage = computed(() => {
  return allImages.value[activeImageIndex.value] || getCoverImage(vehicle.value)
})

// 缩略图列表（内饰图、后排图）
const thumbnailImages = computed(() => {
  const images = allImages.value
  if (images.length <= 1) {
    // 只有一张图时，显示相同的图作为缩略图
    return [
      { url: images[0] || getCoverImage(vehicle.value), label: '外观' },
      { url: images[0] || getCoverImage(vehicle.value), label: '内饰' }
    ]
  }
  // 多张图时，第一张是外观，后面的作为内饰和后排
  return [
    { url: images[0], label: '外观' },
    { url: images[1] || images[0], label: '内饰' },
    { url: images[2] || images[1] || images[0], label: '后排' }
  ].filter(img => img.url)
})

onMounted(async () => {
  try {
    const res = await vehicleApi.detail(route.params.id)
    vehicle.value = res.data
    const [summaryRes, reviewRes] = await Promise.all([
      reviewApi.getVehicleSummary(route.params.id),
      reviewApi.getVehicleReviews(route.params.id, { pageSize: 5 })
    ])
    reviewSummary.value = summaryRes.data || { avgRating: 0, count: 0 }
    reviews.value = reviewRes.data?.records || []
  } catch (e) {
    console.error('加载车辆详情失败', e)
  } finally {
    loading.value = false
  }
  if (userStore.token) {
    try {
      const res = await userApi.getProfile()
      credit.value = res.data.credit || res.data.creditScore || 100
    } catch {}
    // 检查收藏状态
    try {
      const favRes = await favoriteApi.checkFavorite(route.params.id)
      isFav.value = favRes.data?.favorited || false
    } catch {}
  }
})

/** 切换收藏状态 */
async function handleToggleFav() {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await favoriteApi.toggleFavorite(route.params.id)
    isFav.value = res.data?.favorited || false
    ElMessage.success(res.data?.message || (isFav.value ? '已收藏' : '已取消收藏'))
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function setActiveImage(index) {
  activeImageIndex.value = index
}

function setActiveThumbnail(index) {
  activeThumbnailIndex.value = index
  activeImageIndex.value = index
}

function goStoreMap() {
  if (vehicle.value && vehicle.value.storeLongitude && vehicle.value.storeLatitude) {
    window.open(`https://uri.amap.com/marker?position=${vehicle.value.storeLongitude},${vehicle.value.storeLatitude}&name=${vehicle.value.storeName}`, '_blank')
  } else {
    ElMessage.info('暂无门店位置信息')
  }
}

function resolveImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/api/image/')) return url
  if (url.startsWith('/upload/')) return url
  return url
}

function getCoverImage(v) {
  if (!v) return ''
  const img = v.image || v.mainImageUrl
  if (img) {
    if (img.startsWith('http')) return img
    if (img.startsWith('/api/image/')) return img
    if (img.startsWith('/upload/')) return img
    return img
  }
  return '/images/byd-hanev.png'
}

function getBrandShort(brandId) {
  const map = { 1: '比亚迪', 2: '特斯拉', 3: '蔚来', 4: '小鹏', 5: '理想', 6: '问界' }
  return map[brandId] || ''
}

function goBook() {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (credit.value < 80) {
    ElMessage.error('信誉积分不足80分，暂时无法租车')
    return
  }
  router.push(`/order/create/${vehicle.value.id}`)
}

function statusText(s) { return ['可租','已预约','租赁中','维修中','充电中','调度中'][s] || '未知' }
function statusType(s) { return s === 0 ? 'success' : s === 2 ? 'warning' : 'info' }
function vehicleTypeText(t) { return t || '' }
function batteryColor(val) {
  if (val >= 60) return '#38b48b'
  if (val >= 30) return '#fdcb6e'
  return '#e17055'
}
function formatTime(t) {
  return t ? String(t).replace('T', ' ').substring(0, 16) : '-'
}
</script>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 48px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 20px 0;
}
.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}
.back-link {
  font-size: 14px;
  color: #38b48b;
  cursor: pointer;
  transition: all 0.3s ease;
}
.back-link:hover {
  color: #2d9a76;
  transform: translateX(-4px);
}

.detail-main {
  display: flex;
  gap: 32px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* ========== 特斯拉风格图片展示区 ========== */
.gallery-container {
  width: 480px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-image-wrapper {
  position: relative;
  width: 100%;
  height: 320px;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 12px 40px rgba(0,0,0,0.15);
}

.main-image {
  width: 100%;
  height: 100%;
  transition: transform 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.main-image:hover {
  transform: scale(1.02);
}

/* 毛玻璃悬浮效果 */
.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24px;
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.overlay-content {
  color: white;
}

.overlay-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.overlay-subtitle {
  font-size: 16px;
  opacity: 0.9;
  letter-spacing: 1px;
}

.status-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 10;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.image-indicators {
  position: absolute;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 10;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator.active {
  background: #38b48b;
  transform: scale(1.2);
  box-shadow: 0 0 10px rgba(56, 180, 139, 0.5);
}

/* 缩略图区域 */
.thumbnail-section {
  display: flex;
  gap: 12px;
}

.thumbnail-card {
  flex: 1;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  position: relative;
}

.thumbnail-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
}

.thumbnail-card.active {
  border: 3px solid #38b48b;
  box-shadow: 0 0 20px rgba(56, 180, 139, 0.3);
}

.thumbnail-image {
  width: 100%;
  height: 100%;
}

.thumbnail-label {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 6px;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  color: white;
  font-size: 12px;
  text-align: center;
  font-weight: 500;
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  gap: 12px;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #38b48b;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 图片切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: scale(0.95);
}

.fade-leave-to {
  opacity: 0;
  transform: scale(1.05);
}

/* ========== 车辆信息区域 ========== */
.detail-info {
  flex: 1;
  min-width: 0;
  padding-left: 20px;
}

.detail-info h1 {
  font-size: 32px;
  margin-bottom: 12px;
  color: #1a1a2e;
  font-weight: 700;
  background: linear-gradient(135deg, #1a1a2e 0%, #38b48b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sub-info {
  color: #666;
  margin-bottom: 24px;
  font-size: 16px;
  letter-spacing: 0.5px;
}

.price-box {
  background: linear-gradient(135deg, #fff5f5 0%, #fff8f8 100%);
  border: 2px solid #ffe0e0;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  display: flex;
  align-items: baseline;
  gap: 8px;
  box-shadow: 0 4px 16px rgba(255, 107, 107, 0.1);
}

.price-big {
  font-size: 42px;
  color: #ff6b6b;
  font-weight: 800;
  text-shadow: 0 2px 4px rgba(255, 107, 107, 0.2);
}

.price-unit {
  color: #999;
  font-size: 16px;
  font-weight: 500;
}

.deposit {
  margin-left: auto;
  color: #666;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.8);
  padding: 8px 16px;
  border-radius: 10px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

/* ========== 车辆参数介绍 ========== */
.intro-section {
  margin-bottom: 24px;
}

.intro-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #38b48b;
}

.intro-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  background: rgba(248, 249, 250, 0.8);
  border-radius: 12px;
  overflow: hidden;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.intro-item {
  display: flex;
  justify-content: space-between;
  padding: 14px 20px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  transition: all 0.3s ease;
}

.intro-item:hover {
  background: rgba(56, 180, 139, 0.05);
}

.intro-item:nth-child(odd) {
  border-right: 1px solid rgba(0,0,0,0.05);
}

.intro-item:nth-last-child(-n+2) {
  border-bottom: none;
}

.intro-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.intro-value {
  font-size: 14px;
  font-weight: 700;
  color: #1a1a2e;
}

.battery-display {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
}

.battery-label {
  font-size: 15px;
  color: #555;
  white-space: nowrap;
  font-weight: 600;
}

.battery-val {
  font-size: 18px;
  font-weight: 800;
  color: #1a1a2e;
  white-space: nowrap;
}

.store-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
  color: #555;
  font-size: 15px;
  padding: 12px 16px;
  background: rgba(56, 180, 139, 0.05);
  border-radius: 10px;
  border-left: 4px solid #38b48b;
  cursor: pointer;
  transition: all 0.3s ease;
}

.store-info:hover {
  background: rgba(56, 180, 139, 0.1);
  transform: translateX(4px);
}

.store-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.store-name {
  font-weight: 600;
  color: #1a1a2e;
}

.store-address {
  font-size: 13px;
  color: #888;
}

.map-arrow {
  transition: transform 0.3s ease;
}

.store-info:hover .map-arrow {
  transform: translateX(4px);
}

.detail-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.detail-actions .el-button--primary {
  background: linear-gradient(135deg, #38b48b 0%, #2d9a76 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 4px 16px rgba(56, 180, 139, 0.3);
  transition: all 0.3s ease;
}

.detail-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(56, 180, 139, 0.4);
}

.detail-desc {
  margin-top: 32px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.detail-desc h3 {
  font-size: 22px;
  margin-bottom: 16px;
  color: #1a1a2e;
  font-weight: 700;
}

.detail-desc p {
  color: #555;
  line-height: 1.8;
  font-size: 16px;
}

/* ========== 响应式设计 ========== */
@media (max-width: 992px) {
  .detail-main {
    flex-direction: column;
  }
  .gallery-container {
    width: 100%;
  }
  .main-image-wrapper {
    height: 280px;
  }
  .detail-info {
    padding-left: 0;
    padding-top: 20px;
  }
}

@media (max-width: 640px) {
  .main-image-wrapper {
    height: 220px;
  }
  .thumbnail-card {
    height: 60px;
  }
  .overlay-title {
    font-size: 22px;
  }
  .price-big {
    font-size: 32px;
  }
  .detail-actions {
    flex-direction: column;
  }
  .detail-actions .el-button {
    width: 100%;
  }
}
.review-section { margin-top: 20px; }
.review-header { display:flex; justify-content:space-between; align-items:center; gap:16px; }
.review-summary { display:flex; align-items:center; gap:8px; color:#666; font-size:14px; }
.review-list { display:flex; flex-direction:column; gap:14px; }
.review-item { padding:16px; background:#fafafa; border-radius:12px; border:1px solid #f0f0f0; }
.review-item-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.review-tags { display:flex; gap:6px; flex-wrap:wrap; margin-bottom:8px; }
.review-item p { margin:8px 0; color:#333; line-height:1.6; }
.review-time { color:#aaa; font-size:12px; }
.review-reply { margin-top:10px; padding:10px 12px; border-radius:8px; background:#f0f7ff; color:#409eff; font-size:13px; }
</style>
