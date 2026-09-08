<template>
  <div class="dashboard">
    <h2 class="page-title">数据统计大屏</h2>

    <div class="stat-cards">
      <div class="stat-card"><el-icon :size="34" color="#38b48b"><User /></el-icon><div><span>{{ stats.totalUsers || 0 }}</span><em>总用户数</em></div></div>
      <div class="stat-card"><el-icon :size="34" color="#38b48b"><Van /></el-icon><div><span>{{ stats.totalVehicles || 0 }}</span><em>总车辆数</em></div></div>
      <div class="stat-card"><el-icon :size="34" color="#fdcb6e"><Document /></el-icon><div><span>{{ stats.activeOrders || 0 }}</span><em>进行中订单</em></div></div>
      <div class="stat-card"><el-icon :size="34" color="#e17055"><Money /></el-icon><div><span class="price">{{ stats.totalRevenue || 0 }}</span><em>总营收</em></div></div>
      <div class="stat-card"><el-icon :size="34" color="#0984e3"><Calendar /></el-icon><div><span>{{ stats.todayOrders || 0 }}</span><em>今日订单</em></div></div>
      <div class="stat-card"><el-icon :size="34" color="#e17055"><TrendCharts /></el-icon><div><span class="price">{{ stats.monthRevenue || 0 }}</span><em>本月收入</em></div></div>
      <div class="stat-card"><el-icon :size="34" color="#6c5ce7"><Wallet /></el-icon><div><span>{{ stats.pendingRefundOrders || 0 }}</span><em>待退押金订单</em></div></div>
      <div class="stat-card danger"><el-icon :size="34" color="#d63031"><Warning /></el-icon><div><span>{{ stats.lowBatteryVehicles || 0 }}</span><em>低电量车辆</em></div></div>
    </div>

    <div class="chart-grid">
      <div class="ev-card chart-card"><h3>车辆状态分布</h3><div ref="vehicleStatusRef" class="chart-container"></div></div>
      <div class="ev-card chart-card"><h3>订单状态分布</h3><div ref="orderStatusRef" class="chart-container"></div></div>
      <div class="ev-card chart-card wide"><h3>近6月订单与营收趋势</h3><div ref="trendRef" class="chart-container"></div></div>
      <div class="ev-card chart-card"><h3>热门车型排行</h3><div ref="vehicleRankRef" class="chart-container"></div></div>
      <div class="ev-card chart-card"><h3>门店订单排行</h3><div ref="storeRankRef" class="chart-container"></div></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { adminApi } from '@/api/vehicle'

const stats = ref({})
const vehicleStatusRef = ref(null)
const orderStatusRef = ref(null)
const trendRef = ref(null)
const vehicleRankRef = ref(null)
const storeRankRef = ref(null)
const charts = []

onMounted(async () => {
  const [statsRes, vehicleStatusRes, trendRes, orderStatusRes, vehicleRankRes, storeRankRes] = await Promise.all([
    adminApi.getStats(), adminApi.getVehicleStatus(), adminApi.getOrderTrend(),
    adminApi.getOrderStatus(), adminApi.getPopularVehicles(), adminApi.getStoreRank()
  ])
  stats.value = statsRes.data || {}
  renderPie(vehicleStatusRef.value, vehicleStatusRes.data || [], ['#38b48b','#0984e3','#fdcb6e','#e17055','#6c5ce7','#b2bec3'])
  renderPie(orderStatusRef.value, orderStatusRes.data || [], ['#f39c12','#409eff','#909399','#67c23a','#8e44ad','#2ecc71','#95a5a6','#e6a23c','#00b894'])
  renderTrend(trendRes.data || {})
  renderBar(vehicleRankRef.value, vehicleRankRes.data || [], '订单数')
  renderBar(storeRankRef.value, storeRankRes.data || [], '订单数')
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  charts.forEach(c => c.dispose())
  window.removeEventListener('resize', resizeCharts)
})
function resizeCharts() { charts.forEach(c => c.resize()) }

function createChart(el) {
  const chart = echarts.init(el)
  charts.push(chart)
  return chart
}
function renderPie(el, data, colors) {
  const chart = createChart(el)
  chart.setOption({
    tooltip: { trigger: 'item' },
    color: colors,
    series: [{ type: 'pie', radius: ['42%', '70%'], itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 }, label: { formatter: '{b}\n{c}' }, data }]
  })
}
function renderTrend(data) {
  const chart = createChart(trendRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '营收'] },
    grid: { left: 50, right: 50, bottom: 35, top: 45 },
    xAxis: { type: 'category', data: data.months || [] },
    yAxis: [{ type: 'value', name: '订单数' }, { type: 'value', name: '营收' }],
    series: [
      { name: '订单数', type: 'bar', data: data.orderCounts || [], itemStyle: { color: '#38b48b', borderRadius: [4,4,0,0] } },
      { name: '营收', type: 'line', yAxisIndex: 1, smooth: true, data: data.revenues || [], itemStyle: { color: '#e17055' } }
    ]
  })
}
function renderBar(el, data, name) {
  const chart = createChart(el)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 80, right: 20, bottom: 30, top: 20 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: data.map(i => i.name), inverse: true },
    series: [{ name, type: 'bar', data: data.map(i => i.value), itemStyle: { color: '#38b48b', borderRadius: [0,4,4,0] } }]
  })
}
</script>

<style scoped>
.page-title { font-size:24px; margin-bottom:20px; }
.stat-cards { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; margin-bottom:20px; }
.stat-card { background:#fff; border-radius:12px; padding:20px; display:flex; align-items:center; gap:14px; box-shadow:0 2px 12px rgba(0,0,0,.06); }
.stat-card span { display:block; font-size:25px; font-weight:700; color:#2d3436; }
.stat-card .price::before { content:'¥'; font-size:14px; }
.stat-card em { font-style:normal; color:#999; font-size:13px; }
.stat-card.danger span { color:#d63031; }
.chart-grid { display:grid; grid-template-columns:1fr 1fr; gap:20px; }
.chart-card h3 { font-size:16px; margin-bottom:14px; color:#333; }
.chart-card.wide { grid-column:1 / -1; }
.chart-container { height:320px; }
</style>
