<template>
  <div class="stores-page">
    <div class="page-header">
      <h2 class="page-title">门店</h2>
      <p class="page-desc">全国5城覆盖，随时为您提供便捷的取还车服务</p>
    </div>

    <div class="store-grid">
      <div v-for="store in stores" :key="store.id" class="store-card" @click="goMap(store)">
        <div class="store-icon">
          <el-icon :size="28" color="#38b48b"><Shop /></el-icon>
        </div>
        <div class="store-info">
          <h3>{{ store.storeName }}</h3>
          <p class="store-address">
            <el-icon :size="14"><Location /></el-icon>
            {{ store.province }} {{ store.city }} {{ store.address }}
          </p>
          <div class="store-meta">
            <span v-if="store.phone">
              <el-icon :size="14"><Phone /></el-icon>
              {{ store.phone }}
            </span>
            <span v-if="store.businessHours">
              <el-icon :size="14"><Clock /></el-icon>
              {{ store.businessHours }}
            </span>
          </div>
          <el-tag v-if="store.status === 1" type="success" size="small">营业中</el-tag>
          <el-tag v-else type="info" size="small">暂停营业</el-tag>
        </div>
        <el-button size="small" class="btn-view" @click.stop="goStoreVehicles(store.id)">
          查看车辆
        </el-button>
      </div>
    </div>

    <div v-if="stores.length === 0 && !loading" class="empty">
      <el-icon :size="48" color="#ccc"><Shop /></el-icon>
      <p>暂无门店数据</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { vehicleApi } from '@/api/vehicle'
import { Shop, Location, Phone, Clock } from '@element-plus/icons-vue'

const router = useRouter()

const stores = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await vehicleApi.getStores()
    stores.value = res.data || []
  } catch (e) {
    console.error('加载门店失败', e)
  } finally {
    loading.value = false
  }
})

function goMap(store) {
  if (store.longitude && store.latitude) {
    window.open(`https://uri.amap.com/marker?position=${store.longitude},${store.latitude}&name=${store.storeName}`, '_blank')
  }
}

// 跳转到该门店的车辆列表
function goStoreVehicles(storeId) {
  router.push({ path: '/vehicle', query: { storeId } })
}
</script>

<style scoped>
.stores-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 48px;
}

.page-header { margin-bottom: 24px; }
.page-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px;
  color: #1a1a2e;
}
.page-desc {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.store-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.store-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: all 0.2s;
}

.store-card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.store-icon {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  background: #e8f8f5;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.store-info { flex: 1; }

.store-info h3 {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.store-address {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 13px;
  margin: 0 0 6px;
}

.store-meta {
  display: flex;
  gap: 16px;
  color: #999;
  font-size: 13px;
  margin-bottom: 6px;
}

.store-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-view {
  background: #38b48b;
  color: #fff;
  border: none;
  border-radius: 6px;
}

.btn-view:hover {
  background: #2d9a76;
  color: #fff;
}

.empty {
  text-align: center;
  padding: 60px 0;
  color: #ccc;
}
</style>
