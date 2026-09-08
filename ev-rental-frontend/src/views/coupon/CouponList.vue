<!--
  优惠券中心页面

  功能说明：
  1. 可领取优惠券标签页 - 显示所有可领取的优惠券，用户可点击领取
  2. 我的优惠券标签页 - 显示用户已领取的优惠券（未使用/已使用/已过期）
  3. 领取后自动从可领取列表中移除，刷新我的优惠券列表

  优惠券类型：
  - 满减券（类型1）：满足最低消费后减免固定金额
  - 折扣券（类型2）：按比例打折（如8折）
  - 立减券（类型3）：直接减免固定金额

  优惠券状态：
  - 0: 未使用（可使用）
  - 1: 已使用
  - 2: 已过期

  数据来源：
  - couponApi.getAvailableCoupons() 获取可领取优惠券
  - couponApi.getMyCoupons() 获取我的优惠券
  - couponApi.claimCoupon(id) 领取优惠券
-->
<template>
  <div class="coupon-page">
    <div class="page-header">
      <h2 class="page-title">优惠券中心</h2>
      <p class="page-desc">领取优惠券，享受更多租车优惠</p>
    </div>

    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" class="coupon-tabs">
      <!-- 可领取优惠券 -->
      <el-tab-pane label="可领取优惠券" name="available">
        <div class="coupon-list" v-loading="loadingAvailable">
          <div v-for="coupon in availableCoupons" :key="coupon.id" class="coupon-card">
            <div class="coupon-left" :class="getCouponTypeClass(coupon.couponType)">
              <div class="coupon-value">
                <span v-if="coupon.couponType === 1" class="value">¥{{ coupon.discountValue }}</span>
                <span v-else-if="coupon.couponType === 2" class="value">{{ coupon.discountValue * 10 }}折</span>
                <span v-else class="value">¥{{ coupon.discountValue }}</span>
              </div>
              <div class="coupon-condition">
                {{ coupon.minAmount > 0 ? `满${coupon.minAmount}元可用` : '无门槛' }}
              </div>
            </div>
            <div class="coupon-right">
              <div class="coupon-info">
                <h3 class="coupon-name">{{ coupon.couponName }}</h3>
                <p class="coupon-desc">{{ coupon.description }}</p>
                <p class="coupon-time">
                  有效期：{{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}
                </p>
              </div>
              <el-button
                type="primary"
                size="small"
                :loading="claimingId === coupon.id"
                @click="handleClaim(coupon.id)"
              >
                立即领取
              </el-button>
            </div>
          </div>
          <el-empty v-if="!loadingAvailable && availableCoupons.length === 0" description="暂无可领取的优惠券" />
        </div>
      </el-tab-pane>

      <!-- 我的优惠券 -->
      <el-tab-pane label="我的优惠券" name="my">
        <div class="coupon-list" v-loading="loadingMy">
          <div
            v-for="coupon in myCoupons"
            :key="coupon.id"
            class="coupon-card"
            :class="{ 'coupon-used': coupon.status === 1, 'coupon-expired': coupon.status === 2 }"
          >
            <div class="coupon-left" :class="getCouponTypeClass(coupon.couponType)">
              <div class="coupon-value">
                <span v-if="coupon.couponType === 1" class="value">¥{{ coupon.discountValue }}</span>
                <span v-else-if="coupon.couponType === 2" class="value">{{ coupon.discountValue * 10 }}折</span>
                <span v-else class="value">¥{{ coupon.discountValue }}</span>
              </div>
              <div class="coupon-condition">
                {{ coupon.minAmount > 0 ? `满${coupon.minAmount}元可用` : '无门槛' }}
              </div>
            </div>
            <div class="coupon-right">
              <div class="coupon-info">
                <h3 class="coupon-name">{{ coupon.couponName }}</h3>
                <p class="coupon-desc">{{ coupon.description }}</p>
                <p class="coupon-time">
                  有效期：{{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}
                </p>
              </div>
              <div class="coupon-status">
                <el-tag v-if="coupon.status === 0" type="success" size="large">可使用</el-tag>
                <el-tag v-else-if="coupon.status === 1" type="info" size="large">已使用</el-tag>
                <el-tag v-else type="danger" size="large">已过期</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-if="!loadingMy && myCoupons.length === 0" description="暂无优惠券" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { couponApi } from '@/api/vehicle'
import { ElMessage } from 'element-plus'

const activeTab = ref('available')
const loadingAvailable = ref(false)
const loadingMy = ref(false)
const claimingId = ref(null)
const availableCoupons = ref([])
const myCoupons = ref([])

onMounted(() => {
  loadAvailableCoupons()
  loadMyCoupons()
})

// 加载可领取优惠券
async function loadAvailableCoupons() {
  loadingAvailable.value = true
  try {
    const res = await couponApi.getAvailableCoupons()
    availableCoupons.value = res.data || []
  } catch (e) {
    console.error('加载优惠券失败', e)
  } finally {
    loadingAvailable.value = false
  }
}

// 加载我的优惠券
async function loadMyCoupons() {
  loadingMy.value = true
  try {
    const res = await couponApi.getMyCoupons()
    myCoupons.value = res.data || []
  } catch (e) {
    console.error('加载我的优惠券失败', e)
  } finally {
    loadingMy.value = false
  }
}

// 领取优惠券
async function handleClaim(couponId) {
  claimingId.value = couponId
  try {
    await couponApi.claimCoupon(couponId)
    ElMessage.success('领取成功')
    // 从可领取列表中移除该优惠券
    availableCoupons.value = availableCoupons.value.filter(c => c.id !== couponId)
    // 刷新我的优惠券列表
    loadMyCoupons()
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    claimingId.value = null
  }
}

// 获取优惠券类型样式类
function getCouponTypeClass(type) {
  return {
    1: 'coupon-type-manjian',
    2: 'coupon-type-zhekou',
    3: 'coupon-type-lijian'
  }[type] || 'coupon-type-manjian'
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr.substring(0, 10)
}
</script>

<style scoped>
.coupon-page {
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

.coupon-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.coupon-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
}

.coupon-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
}

.coupon-used,
.coupon-expired {
  opacity: 0.6;
}

.coupon-left {
  width: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #fff;
}

.coupon-type-manjian {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
}

.coupon-type-zhekou {
  background: linear-gradient(135deg, #38b48b, #4cd9a0);
}

.coupon-type-lijian {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
}

.coupon-value .value {
  font-size: 32px;
  font-weight: 700;
}

.coupon-condition {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.9;
}

.coupon-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #fff;
}

.coupon-info {
  flex: 1;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.coupon-desc {
  font-size: 13px;
  color: #666;
  margin: 0 0 8px;
}

.coupon-time {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.coupon-status {
  flex-shrink: 0;
}

/* 响应式 */
@media (max-width: 640px) {
  .coupon-card {
    flex-direction: column;
  }

  .coupon-left {
    width: 100%;
    padding: 16px;
  }

  .coupon-right {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
