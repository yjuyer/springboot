<template>
  <div class="point-shop">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>积分商城</span>
          <el-tag type="warning" size="large">当前积分：{{ currentPoints }}</el-tag>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="8" v-for="item in shopItems" :key="item.id">
          <el-card class="shop-card" shadow="hover">
            <div class="shop-icon">{{ item.icon }}</div>
            <h3>{{ item.name }}</h3>
            <p class="desc">{{ item.desc }}</p>
            <el-button type="primary" :disabled="currentPoints < item.cost" @click="exchange(item)">
              {{ item.cost }} 积分兑换
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pointApi } from '@/api/vehicle'

const currentPoints = ref(0)

const shopItems = [
  { id: 1, icon: '🎟️', name: '50元优惠券', desc: '满200元可用', cost: 1000, type: 'coupon', couponId: 1 },
  { id: 2, icon: '🎫', name: '30元立减券', desc: '满100元可用', cost: 500, type: 'coupon', couponId: 3 },
  { id: 3, icon: '❌', name: '免费取消次数', desc: '增加1次免费取消', cost: 500, type: 'freeCancel' }
]

onMounted(async () => {
  try {
    const res = await pointApi.balance()
    currentPoints.value = res.data || 0
  } catch (e) {
    console.error('获取积分失败', e)
  }
})

const exchange = async (item) => {
  try {
    await ElMessageBox.confirm(`确认使用 ${item.cost} 积分兑换"${item.name}"？`, '确认兑换')
    if (item.type === 'coupon') {
      await pointApi.exchangeCoupon({ pointCost: item.cost, couponId: item.couponId })
    } else if (item.type === 'freeCancel') {
      await pointApi.exchangeFreeCancel({ pointCost: item.cost })
    }
    ElMessage.success('兑换成功！')
    currentPoints.value -= item.cost
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '兑换失败')
  }
}
</script>

<style scoped>
.point-shop { max-width: 1000px; margin: 20px auto; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
.shop-card { text-align: center; padding: 20px; }
.shop-icon { font-size: 48px; margin-bottom: 10px; }
.shop-card h3 { margin: 8px 0; }
.desc { color: #999; font-size: 13px; margin-bottom: 12px; }
</style>
