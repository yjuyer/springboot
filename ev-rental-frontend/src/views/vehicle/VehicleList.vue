<!--
  车辆列表页面

  功能说明：
  1. 左右布局 - 左侧筛选栏 + 右侧车辆列表
  2. 品牌筛选 - 支持按品牌筛选（比亚迪、特斯拉、蔚来、小鹏、理想、问界）
  3. 价格筛选 - 支持按价格区间筛选（100以下/100-200/200-300/300以上）
  4. 门店筛选 - 支持从门店页面跳转过来，只显示该门店的车辆
  5. 分页功能 - 支持切换每页显示数量（6/12/18）

  URL参数：
  - brand - 品牌ID，从首页品牌分类跳转
  - storeId - 门店ID，从门店查询页面跳转

  响应式设计：
  - 桌面端：左侧筛选栏 + 右侧三列车辆卡片
  - 平板端：筛选栏水平排列 + 两列车辆卡片
  - 手机端：筛选栏水平排列 + 单列车辆卡片

  数据来源：
  - vehicleApi.list(query) 获取车辆列表（支持分页和筛选）
  - vehicleApi.getStores() 获取门店列表（用于显示门店名称）
-->
<template>
  <div class="vehicle-list-page">
    <div class="page-header">
      <h2 class="page-title">全部车辆</h2>
      <p class="page-desc">浏览我们精选的新能源汽车，选择最适合您的座驾</p>
      <div v-if="query.storeId" class="store-filter-tag">
        <el-tag type="success" closable @close="clearStoreFilter">
          当前门店：{{ storeName || '门店ID-' + query.storeId }}
        </el-tag>
      </div>
    </div>

    <!-- 左右布局 -->
    <div class="content-wrap">
      <!-- 左侧筛选栏 -->
      <aside class="sidebar">
        <div class="side-section">
          <div class="side-title">车辆品牌</div>
          <div
            class="side-option"
            :class="{ active: query.brandId === null }"
            @click="selectBrand(null)"
          >全部</div>
          <div
            v-for="b in brandOptions"
            :key="b.value"
            class="side-option"
            :class="{ active: query.brandId === b.value }"
            @click="selectBrand(b.value)"
          >{{ b.label }}</div>
        </div>
        <div class="side-section">
          <div class="side-title">价格区间</div>
          <div
            class="side-option"
            :class="{ active: !priceRange }"
            @click="selectPrice('')"
          >不限</div>
          <div
            v-for="p in priceOptions"
            :key="p.value"
            class="side-option"
            :class="{ active: priceRange === p.value }"
            @click="selectPrice(p.value)"
          >{{ p.label }}</div>
        </div>
      </aside>

      <!-- 右侧车辆列表 -->
      <main class="main-content">
        <div v-loading="loading" class="vehicle-grid" element-loading-text="加载中...">
          <div
            v-for="v in vehicles"
            :key="v.id"
            class="vehicle-card"
            @click="$router.push(`/vehicle/${v.id}`)"
          >
            <div class="card-img">
              <el-image
                :src="getCoverImage(v)"
                :alt="v.model"
                fit="cover"
                lazy
                class="card-cover"
              >
                <template #placeholder>
                  <div class="img-placeholder">
                    <el-icon :size="36" color="#ccc"><PictureFilled /></el-icon>
                  </div>
                </template>
                <template #error>
                  <div class="img-placeholder">
                    <el-icon :size="36" color="#ccc"><PictureFilled /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="brand-badge">{{ getBrandShort(v.brandId) }}</div>
            </div>
            <div class="card-body">
              <h3 class="card-title">{{ v.model }}</h3>
              <div class="card-specs">
                <span>{{ v.rangeKm }}km</span>
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

        <el-empty v-if="!loading && vehicles.length === 0" description="暂无符合条件的车辆" />

        <div class="pagination" v-if="total > 0">
          <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            :page-sizes="[6, 12, 18]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadVehicles"
            @current-change="loadVehicles"
          />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { vehicleApi } from '@/api/vehicle'
import { PictureFilled } from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(false)
const vehicles = ref([])
const total = ref(0)
const priceRange = ref('')
const storeName = ref('')

const brandOptions = [
  { label: '比亚迪', value: 1 },
  { label: '特斯拉', value: 2 },
  { label: '蔚来', value: 3 },
  { label: '小鹏', value: 4 },
  { label: '理想', value: 5 },
  { label: '问界', value: 6 },
]

const priceOptions = [
  { label: '100元以下', value: '0-100' },
  { label: '100~200元', value: '100-200' },
  { label: '200~300元', value: '200-300' },
  { label: '300元以上', value: '300-99999' },
]

const query = reactive({
  brandId: route.query.brand ? parseInt(route.query.brand) : null,
  storeId: route.query.storeId ? parseInt(route.query.storeId) : null,
  vehicleType: null,
  minRange: '',
  minPrice: '',
  maxPrice: '',
  vehicleStatus: 0,
  pageNum: 1,
  pageSize: 6
})

onMounted(() => {
  // 如果有storeId参数，加载门店名称
  if (query.storeId) {
    loadStoreName()
  }
  loadVehicles()
})

// 加载门店名称
async function loadStoreName() {
  try {
    const res = await vehicleApi.getStores()
    const stores = res.data || []
    const store = stores.find(s => s.id === query.storeId)
    if (store) {
      storeName.value = store.storeName
    }
  } catch (e) {
    console.error('加载门店信息失败', e)
  }
}

// 清除门店筛选
function clearStoreFilter() {
  query.storeId = null
  storeName.value = ''
  query.pageNum = 1
  loadVehicles()
}

async function loadVehicles() {
  loading.value = true
  try {
    const res = await vehicleApi.list(query)
    vehicles.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function selectBrand(val) {
  query.brandId = val
  query.pageNum = 1
  loadVehicles()
}

function selectPrice(val) {
  priceRange.value = val
  if (val) {
    const [min, max] = val.split('-')
    query.minPrice = min
    query.maxPrice = max
  } else {
    query.minPrice = ''
    query.maxPrice = ''
  }
  query.pageNum = 1
  loadVehicles()
}

function getCoverImage(v) {
  // 从数据库获取图片路径
  const img = v.image || v.mainImageUrl
  if (img) {
    if (img.startsWith('http')) return img
    // /api/image/show/{id} 格式直接返回
    if (img.startsWith('/api/image/')) return img
    // /upload/ 格式（旧数据兼容）
    if (img.startsWith('/upload/')) return img
    return img
  }
  return '/images/byd-hanev.png'
}

function getBrandShort(brandId) {
  const map = { 1: 'BYD', 2: 'TSL', 3: 'NIO', 4: 'XPEV', 5: 'LIX', 6: 'AITO' }
  return map[brandId] || ''
}
</script>

<style scoped>
.vehicle-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 48px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px;
}

.page-desc {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.store-filter-tag {
  margin-top: 12px;
}

/* ========== 左右布局 ========== */
.content-wrap {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* ========== 左侧筛选栏 ========== */
.sidebar {
  width: 180px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  overflow: hidden;
}

.side-section {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.side-section:last-child {
  border-bottom: none;
}

.side-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a1a2e;
  padding: 0 18px 10px;
}

.side-option {
  padding: 9px 18px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.side-option:hover {
  background: #f0faf6;
  color: #38b48b;
}

.side-option.active {
  background: #f0faf6;
  color: #38b48b;
  font-weight: 600;
}

/* ========== 右侧内容 ========== */
.main-content {
  flex: 1;
  min-width: 0;
}

/* ========== 车辆网格 ========== */
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

.img-placeholder {
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

/* ========== 分页 ========== */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* ========== 响应式 ========== */
@media (max-width: 992px) {
  .content-wrap { flex-direction: column; }
  .sidebar { width: 100%; display: flex; gap: 0; }
  .side-section { flex: 1; border-bottom: none; border-right: 1px solid #f0f0f0; padding: 12px 0; }
  .side-section:last-child { border-right: none; }
  .vehicle-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .vehicle-grid { grid-template-columns: 1fr; }
}
</style>
