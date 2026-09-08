<template>
  <div class="fav-page">
    <h2 class="page-title">我的收藏</h2>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="favorites.length === 0" class="empty">
      <el-icon :size="48" color="#ccc"><Star /></el-icon>
      <p>还没有收藏任何车辆</p>
      <el-button type="primary" @click="$router.push('/vehicle')">去租车</el-button>
    </div>

    <div v-else class="fav-grid">
      <div v-for="v in favorites" :key="v.id" class="fav-card" @click="$router.push(`/vehicle/${v.vehicleId}`)">
        <div class="card-img">
          <el-image :src="v.image" fit="cover" class="card-cover">
            <template #error>
              <div class="img-placeholder"><el-icon :size="32" color="#ccc"><PictureFilled /></el-icon></div>
            </template>
          </el-image>
          <el-tag :type="v.vehicleStatus === 0 ? 'success' : 'info'" effect="dark" size="small" class="card-status">
            {{ v.vehicleStatus === 0 ? '可租' : '暂不可租' }}
          </el-tag>
          <div class="fav-remove" @click.stop="removeFav(v.id)">
            <el-icon :size="16"><Star /></el-icon>
          </div>
        </div>
        <div class="card-body">
          <h3>{{ v.model }}</h3>
          <div class="card-meta">
            <span>{{ v.rangeKm }}km 续航</span>
            <span class="price">¥{{ v.dailyPrice }}/天</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Star, PictureFilled } from '@element-plus/icons-vue'
import { favoriteApi } from '@/api/vehicle'
import { ElMessage } from 'element-plus'

const router = useRouter()
const favorites = ref([])
const loading = ref(true)

onMounted(() => {
  loadFavorites()
})

async function loadFavorites() {
  loading.value = true
  try {
    const res = await favoriteApi.getMyFavorites()
    favorites.value = res.data || []
  } catch (e) {
    favorites.value = []
  } finally {
    loading.value = false
  }
}

async function removeFav(id) {
  try {
    await favoriteApi.deleteFavorite(id)
    favorites.value = favorites.value.filter(v => v.id !== id)
    ElMessage.success('已取消收藏')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.fav-page { max-width: 1000px; margin: 0 auto; padding: 20px; }
.page-title { font-size: 24px; margin-bottom: 20px; color: #1a1a2e; }

.loading {
  padding: 40px;
  background: #fff;
  border-radius: 18px;
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.fav-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: transform 0.25s, box-shadow 0.25s;
}
.fav-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.1);
}

.card-img {
  position: relative;
  height: 180px;
  background: #f0f0f0;
}
.card-cover { width: 100%; height: 100%; }
.img-placeholder {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  background: #f5f5f5;
}
.card-status {
  position: absolute;
  top: 10px;
  left: 10px;
}
.fav-remove {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(255,255,255,0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f56c6c;
  cursor: pointer;
  transition: transform 0.2s;
}
.fav-remove:hover { transform: scale(1.15); }

.card-body { padding: 16px; }
.card-body h3 { margin: 0 0 10px; font-size: 16px; color: #1a1a2e; }
.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #888;
}
.price { color: #ff6b6b; font-weight: 600; font-size: 16px; }

.empty {
  text-align: center;
  padding: 80px 0;
  color: #ccc;
}
.empty p { margin: 12px 0 20px; }

@media (max-width: 768px) {
  .fav-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 480px) {
  .fav-grid { grid-template-columns: 1fr; }
}
</style>
