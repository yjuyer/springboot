<!--
  充电地图页面

  功能说明：
  1. 地图展示 - 使用高德地图API展示地图
  2. 充电站搜索 - 按城市搜索充电站（默认北京）
  3. 位置定位 - 点击按钮定位当前位置
  4. 充电站列表 - 右侧面板展示搜索到的充电站列表
  5. 标记点击 - 点击地图标记或列表项可查看详情

  技术实现：
  - 高德地图JavaScript API
  - POI搜索插件（AMap.PlaceSearch）
  - 定位插件（AMap.Geolocation）
  - 信息窗口（AMap.InfoWindow）

  响应式设计：
  - 桌面端：左侧地图 + 右侧充电站列表
  - 手机端：上方地图 + 下方充电站列表

  注意：需要在public/index.html中引入高德地图API的script标签
-->
<template>
  <div class="charging-page">
    <div class="page-header">
      <h2 class="page-title">充电地图</h2>
      <div class="search-bar">
        <el-input
          v-model="searchCity"
          placeholder="输入城市搜索"
          style="width: 200px"
          @keyup.enter="searchChargingStations"
        />
        <el-button type="primary" @click="searchChargingStations">
          <el-icon><Search /></el-icon>
          搜索充电站
        </el-button>
        <el-button @click="locateMe">
          <el-icon><Location /></el-icon>
          定位当前位置
        </el-button>
      </div>
    </div>

    <div class="map-content">
      <!-- 地图容器 -->
      <div id="amap-container" class="map-container"></div>

      <!-- 充电站列表 -->
      <div class="station-panel">
        <div class="panel-header">
          <h3>周边充电站</h3>
          <el-tag type="success">{{ stations.length }}个</el-tag>
        </div>
        <div class="station-list" v-loading="loading">
          <div
            v-for="(station, idx) in stations"
            :key="idx"
            class="station-item"
            :class="{ active: activeStation === idx }"
            @click="focusStation(idx)"
          >
            <div class="station-icon">
              <el-icon :size="24" color="#38b48b"><Lightning /></el-icon>
            </div>
            <div class="station-info">
              <h4>{{ station.name }}</h4>
              <p class="station-address">{{ station.address }}</p>
              <div class="station-meta">
                <el-tag v-if="station.type" type="success" size="small">{{ station.type }}</el-tag>
                <span v-if="station.tel" class="station-tel">📞 {{ station.tel }}</span>
              </div>
            </div>
            <div class="station-distance" v-if="station.distance">
              {{ station.distance }}m
            </div>
          </div>
          <el-empty v-if="!loading && stations.length === 0" description="暂无充电站数据" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Search, Location, Lightning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const searchCity = ref('北京')
const loading = ref(false)
const stations = ref([])
const activeStation = ref(-1)

let map = null
let markers = []
let infoWindow = null

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
  }
})

// 初始化地图
function initMap() {
  try {
    // 创建地图实例
    map = new AMap.Map('amap-container', {
      zoom: 12,
      center: [116.397428, 39.90923], // 默认北京
      mapStyle: 'amap://styles/whitesmoke',
      viewMode: '2D'
    })

    // 创建信息窗口
    infoWindow = new AMap.InfoWindow({
      offset: new AMap.Pixel(0, -30),
      autoMove: true
    })

    // 默认搜索北京的充电站
    searchChargingStations()
  } catch (error) {
    console.error('地图初始化失败:', error)
    ElMessage.error('地图加载失败，请检查网络连接')
  }
}

// 搜索充电站
function searchChargingStations() {
  if (!map) return

  loading.value = true
  stations.value = []
  clearMarkers()

  // 使用高德地图 POI 搜索
  AMap.plugin('AMap.PlaceSearch', () => {
    const placeSearch = new AMap.PlaceSearch({
      pageSize: 20,
      pageIndex: 1,
      city: searchCity.value,
      map: map,
      autoFitView: true
    })

    // 搜索充电站
    placeSearch.search('充电站', (status, result) => {
      loading.value = false

      if (status === 'complete' && result.poiList) {
        const pois = result.poiList.pois
        stations.value = pois.map((poi, idx) => {
          // 添加标记
          addMarker(poi, idx)

          return {
            name: poi.name,
            address: poi.address || '暂无详细地址',
            type: getStationType(poi),
            tel: poi.tel || '',
            distance: poi.distance ? Math.round(poi.distance) : '',
            location: poi.location,
            poi: poi
          }
        })

        ElMessage.success(`找到 ${stations.value.length} 个充电站`)
      } else {
        ElMessage.warning('未找到充电站数据')
      }
    })
  })
}

// 添加标记
function addMarker(poi, index) {
  if (!poi.location) return

  const marker = new AMap.Marker({
    position: [poi.location.lng, poi.location.lat],
    title: poi.name,
    animation: 'AMAP_ANIMATION_DROP',
    offset: new AMap.Pixel(-15, -15),
    label: {
      content: '⚡',
      offset: new AMap.Pixel(0, -20)
    }
  })

  // 点击标记显示信息
  marker.on('click', () => {
    activeStation.value = index
    showInfoWindow(poi, marker)
  })

  marker.setMap(map)
  markers.push(marker)
}

// 显示信息窗口
function showInfoWindow(poi, marker) {
  const content = `
    <div style="padding: 10px; min-width: 200px;">
      <h4 style="margin: 0 0 8px; color: #1a1a2e;">${poi.name}</h4>
      <p style="margin: 0 0 4px; color: #666; font-size: 13px;">📍 ${poi.address || '暂无地址'}</p>
      ${poi.tel ? `<p style="margin: 0; color: #38b48b; font-size: 13px;">📞 ${poi.tel}</p>` : ''}
    </div>
  `
  infoWindow.setContent(content)
  infoWindow.open(map, marker.getPosition())
}

// 清除标记
function clearMarkers() {
  markers.forEach(marker => marker.setMap(null))
  markers = []
}

// 聚焦到某个充电站
function focusStation(index) {
  activeStation.value = index
  const station = stations.value[index]
  if (station && station.location) {
    map.setCenter([station.location.lng, station.location.lat])
    map.setZoom(15)
    showInfoWindow(station.poi, markers[index])
  }
}

// 定位当前位置
function locateMe() {
  if (!map) return

  AMap.plugin('AMap.Geolocation', () => {
    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      buttonPosition: 'RB',
      buttonOffset: new AMap.Pixel(10, 20),
      zoomToAccuracy: true
    })

    geolocation.getCurrentPosition((status, result) => {
      if (status === 'complete' && result.position) {
        const { lng, lat } = result.position
        map.setCenter([lng, lat])
        map.setZoom(13)
        searchCity.value = result.addressComponent?.city || '北京'
        searchChargingStations()
        ElMessage.success('定位成功')
      } else {
        ElMessage.warning('定位失败，使用默认位置')
      }
    })
  })
}

// 获取充电站类型
function getStationType(poi) {
  const name = poi.name || ''
  if (name.includes('超级') || name.includes('特斯拉')) return '超级快充'
  if (name.includes('快充')) return '快充'
  return '快充+慢充'
}
</script>

<style scoped>
.charging-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 48px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  color: #1a1a2e;
  margin: 0;
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.map-content {
  display: flex;
  gap: 20px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  min-height: 600px;
}

.map-container {
  flex: 1;
  min-height: 600px;
  border-radius: 16px 0 0 16px;
}

.station-panel {
  width: 360px;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-bottom: 1px solid #eee;
}

.panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1a1a2e;
}

.station-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.station-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.station-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.station-item.active {
  border-color: #38b48b;
  background: #e8f8f5;
}

.station-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #e8f8f5 0%, #d0f0e8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.station-info {
  flex: 1;
  min-width: 0;
}

.station-info h4 {
  margin: 0 0 6px;
  font-size: 14px;
  color: #1a1a2e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.station-address {
  margin: 0 0 8px;
  font-size: 12px;
  color: #888;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.station-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.station-tel {
  font-size: 12px;
  color: #666;
}

.station-distance {
  font-size: 12px;
  color: #38b48b;
  font-weight: 600;
  white-space: nowrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .map-content {
    flex-direction: column;
  }

  .station-panel {
    width: 100%;
    max-height: 300px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
