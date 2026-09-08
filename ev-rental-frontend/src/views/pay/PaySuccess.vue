<template>
  <div class="pay-success-page">
    <div class="success-card">
      <div class="success-icon">
        <el-icon :size="80" color="#38b48b"><CircleCheckFilled /></el-icon>
      </div>
      <h1 class="success-title">支付成功</h1>
      <p class="success-desc">您的订单已支付成功，请等待取车</p>

      <div class="order-info" v-if="orderInfo">
        <div class="info-item">
          <span class="label">订单编号</span>
          <span class="value">{{ orderInfo.orderNo }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付金额</span>
          <span class="value price">¥{{ orderInfo.paidAmount }}</span>
        </div>
      </div>

      <div class="actions">
        <el-button type="primary" size="large" @click="goOrderDetail">
          查看订单详情
        </el-button>
        <el-button size="large" @click="goHome">
          返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { orderApi } from '@/api/vehicle'

const router = useRouter()
const route = useRoute()
const orderInfo = ref(null)

onMounted(async () => {
  // 从URL参数获取订单号
  const orderNo = route.query.out_trade_no || route.query.orderNo
  if (orderNo) {
    try {
      const res = await orderApi.detail(orderNo)
      orderInfo.value = res.data
    } catch (e) {
      console.error('获取订单信息失败', e)
    }
  }
})

function goOrderDetail() {
  if (orderInfo.value) {
    router.push(`/order/detail/${orderInfo.value.id}`)
  } else {
    router.push('/order')
  }
}

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.pay-success-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.success-card {
  background: #fff;
  border-radius: 16px;
  padding: 60px 50px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  max-width: 500px;
  width: 100%;
}

.success-icon {
  margin-bottom: 24px;
}

.success-title {
  font-size: 28px;
  color: #333;
  margin: 0 0 12px;
  font-weight: 600;
}

.success-desc {
  font-size: 16px;
  color: #666;
  margin: 0 0 32px;
}

.order-info {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 32px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.info-item .label {
  color: #999;
}

.info-item .value {
  font-weight: 500;
  color: #333;
}

.info-item .value.price {
  color: #ff6b6b;
  font-size: 18px;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.actions .el-button {
  min-width: 140px;
}
</style>
