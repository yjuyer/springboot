<!--
  首页组件

  功能说明：
  1. Hero Banner - 展示平台名称和标语，带有背景图片和遮罩效果
  2. 服务亮点栏 - 展示平台核心服务（海量新能源、24小时客服、全国门店、充电无忧）
  3. 精选好车 - 展示热门车辆列表，支持骨架屏加载、图片懒加载、hover动画效果

  数据来源：
  - 调用 vehicleApi.hot() 获取热门车辆数据

  跳转功能：
  - 点击车辆卡片跳转到车辆详情页
  - 点击"查看更多"跳转到车辆列表页
-->
<template>
  <div class="home">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">e租出行</h1>
        <h2 class="hero-subtitle">新能源汽车租赁平台</h2>
        <p class="hero-desc">绿色出行，智享未来 —— 专注新能源汽车租赁的一站式服务平台，为您打造环保、经济、智能的出行体验</p>
      </div>
    </div>

    <!-- 服务亮点栏 -->
    <div class="filter-bar">
      <div class="filter-inner">
        <div class="filter-item" v-for="f in filters" :key="f.key">
          <span class="filter-icon">{{ f.icon }}</span>
          <span class="filter-label">{{ f.label }}</span>
        </div>
      </div>
    </div>

    <!-- 精选好车 -->
    <section class="section">
      <div class="section-header">
        <h2 class="section-title">精选好车</h2>
        <span class="more-link" @click="$router.push('/vehicle')">查看更多 &gt;</span>
      </div>

      <!-- 骨架屏 -->
      <div v-if="loading" class="vehicle-grid">
        <div v-for="i in 6" :key="'skeleton-' + i" class="vehicle-card skeleton-card">
          <div class="skeleton-img"></div>
          <div class="card-body">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="text" style="width:70%;height:20px" />
                <el-skeleton-item variant="text" style="width:50%;height:14px;margin-top:8px" />
                <el-skeleton-item variant="text" style="width:40%;height:22px;margin-top:10px" />
              </template>
            </el-skeleton>
          </div>
        </div>
      </div>

      <!-- 车辆列表 -->
      <div v-else class="vehicle-grid">
        <div
          v-for="v in vehicles"
          :key="v.id"
          class="vehicle-card"
          @click="goDetail(v.id)"
        >
          <div class="card-img">
            <el-image
              :src="getCoverImage(v)"
              :alt="v.vehicleName"
              fit="cover"
              lazy
              class="card-cover"
            >
              <template #placeholder>
                <div class="image-placeholder">
                  <el-icon :size="36" color="#ccc"><PictureFilled /></el-icon>
                </div>
              </template>
              <template #error>
                <div class="image-placeholder">
                  <el-icon :size="36" color="#ccc"><PictureFilled /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="brand-badge">{{ getShortBrand(v.vehicleName) }}</div>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ v.vehicleName }}</h3>
            <div class="card-specs">
              <span>{{ v.batteryRange }}km</span>
              <span class="spec-sep">|</span>
              <span>{{ v.seatCount || 5 }}座</span>
            </div>
            <div class="card-price">
              <span class="price-num">¥{{ v.dailyPrice }}</span>
              <span class="price-unit">起/天</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { vehicleApi } from '@/api/vehicle'
import { PictureFilled } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const vehicles = ref([])

const filters = [
  { key: 'ev', icon: '⚡', label: '海量新能源' },
  { key: 'service', icon: '📞', label: '24小时客服' },
  { key: 'store', icon: '📍', label: '全国门店' },
  { key: 'charging', icon: '🔋', label: '充电无忧' },
]

onMounted(async () => {
  loading.value = true
  try {
    const res = await vehicleApi.hot()
    vehicles.value = res.data || []
  } catch (e) {
    console.error('加载热门车型失败', e)
  } finally {
    loading.value = false
  }
})

function goDetail(id) {
  router.push(`/vehicle/${id}`)
}

function getCoverImage(v) {
  if (v.coverImage) {
    if (v.coverImage.startsWith('http')) return v.coverImage
    if (v.coverImage.startsWith('/upload')) return v.coverImage
    return v.coverImage
  }
  return ''
}

function getShortBrand(name) {
  if (!name) return ''
  const parts = name.split(/\s+/)
  return parts[0] || name.substring(0, 3)
}
</script>

<style scoped>
.home {
  max-width: 1800px;
  margin: 0 auto;
  padding: 0 48px 48px;
}

/* ========== Hero Banner ========== */
.hero-banner {
  position: relative;
  height: 380px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 0;
  background-image: url('https://images.unsplash.com/photo-1593941707882-a5bba14938c7?w=1400&h=500&fit=crop');
  background-size: cover;
  background-position: center;
}

.hero-banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.35);
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 3;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #fff;
  text-align: center;
  padding: 0 20px;
}

.hero-title {
  font-size: 52px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: 4px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.hero-subtitle {
  font-size: 26px;
  font-weight: 500;
  margin: 0 0 20px;
  text-shadow: 0 1px 4px rgba(0,0,0,0.2);
}

.hero-desc {
  font-size: 15px;
  opacity: 0.9;
  margin: 0;
  max-width: 600px;
  line-height: 1.8;
  text-shadow: 0 1px 2px rgba(0,0,0,0.15);
}

/* ========== Filter Bar ========== */
.filter-bar {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  padding: 20px 0;
  margin: -28px 0 36px;
  position: relative;
  z-index: 10;
}

.filter-inner {
  display: flex;
  justify-content: space-evenly;
  padding: 0 24px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  color: #555;
  padding: 14px 40px;
  border-radius: 24px;
  transition: all 0.2s;
  background: #fff;
  user-select: none;
  border: 1px solid #e0e0e0;
  white-space: nowrap;
}

.filter-item + .filter-item {
  margin-left: 0;
}

.filter-item:hover {
  border-color: #38b48b;
  color: #38b48b;
  background: #f0faf6;
}

.filter-icon {
  font-size: 18px;
}

.filter-label {
  font-weight: 500;
}

/* ========== Section ========== */
.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

.more-link {
  font-size: 14px;
  color: #38b48b;
  cursor: pointer;
  transition: color 0.2s;
}

.more-link:hover {
  color: #2d9a76;
}

/* ========== Vehicle Grid ========== */
.vehicle-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.vehicle-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}

.vehicle-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}

.card-img {
  position: relative;
  height: 190px;
  background: #f0f2f5;
  overflow: hidden;
}

.card-cover {
  width: 100%;
  height: 100%;
  transition: transform 0.4s ease;
}

.vehicle-card:hover .card-cover {
  transform: scale(1.05);
}

.brand-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(0,0,0,0.55);
  color: #fff;
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
}

.card-body {
  padding: 14px 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-specs {
  font-size: 13px;
  color: #888;
  margin-bottom: 10px;
}

.spec-sep {
  margin: 0 8px;
  color: #ddd;
}

.card-price {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.price-num {
  font-size: 22px;
  font-weight: 700;
  color: #ff6b35;
}

.price-unit {
  font-size: 13px;
  color: #999;
}

/* ========== Skeleton ========== */
.skeleton-card {
  pointer-events: none;
}

.skeleton-img {
  height: 190px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ========== Responsive ========== */
@media (max-width: 992px) {
  .vehicle-grid { grid-template-columns: repeat(2, 1fr); }
  .filter-bar { margin: -24px 16px 32px; }
  .filter-inner { gap: 12px; flex-wrap: wrap; }
  .filter-item { padding: 6px 14px; font-size: 13px; }
  .hero-title { font-size: 38px; }
  .hero-subtitle { font-size: 20px; }
}

@media (max-width: 640px) {
  .vehicle-grid { grid-template-columns: 1fr; }
  .filter-bar { margin: -20px 8px 24px; padding: 12px 16px; }
  .filter-inner { gap: 12px; flex-wrap: wrap; }
  .filter-item { font-size: 13px; padding: 4px 10px; }
  .hero-banner { height: 280px; }
  .hero-title { font-size: 30px; }
  .hero-subtitle { font-size: 18px; }
}
</style>
