<template>
  <div class="point-history">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>积分明细</span>
          <el-tag type="warning" size="large">{{ currentPoints }} 积分</el-tag>
        </div>
      </template>

      <el-table :data="records" stripe v-loading="loading">
        <el-table-column prop="points" label="积分变动" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.points > 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.points > 0 ? '+' + row.points : row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.type)">
              {{ typeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="说明" />
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @change="fetchHistory"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pointApi } from '@/api/vehicle'

const records = ref([])
const currentPoints = ref(0)
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const typeTag = (t) => ({ 1: '', 2: 'warning', 3: 'success', 4: 'info' }[t])
const typeText = (t) => ({ 1: '订单完成', 2: '积分兑换', 3: '活动赠送', 4: '管理员调整' }[t])

const fetchHistory = async () => {
  loading.value = true
  try {
    const [histRes, balRes] = await Promise.all([
      pointApi.history({ page: page.value, size: size.value }),
      pointApi.balance()
    ])
    records.value = histRes.data?.records || []
    total.value = histRes.data?.total || 0
    currentPoints.value = balRes.data || 0
  } catch (e) {
    console.error('获取积分历史失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(fetchHistory)
</script>

<style scoped>
.point-history { max-width: 900px; margin: 20px auto; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
</style>
