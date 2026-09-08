<template>
  <div class="order-detail-page">
    <div class="back-row">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
    </div>

    <h2 class="page-title">订单详情</h2>

    <div v-loading="loading">
      <!-- 订单状态流程 -->
      <div class="ev-card status-flow-card">
        <h3 class="card-title">订单状态</h3>
        <el-steps :active="currentStep" align-center finish-status="success">
          <el-step title="待支付" description="生成订单" />
          <el-step title="已支付" description="支付押金" />
          <el-step title="待取车" description="押金冻结" />
          <el-step title="租赁中" description="确认取车" />
          <el-step title="待还车" description="申请还车" />
          <el-step title="已完成" description="确认还车" />
          <el-step title="已退款" description="退还押金" />
        </el-steps>
      </div>

      <!-- 订单基本信息 -->
      <div class="ev-card" v-if="order">
        <div class="card-header-row">
          <h3 class="card-title">订单信息</h3>
          <el-tag :type="statusType(order.orderStatus)" size="default">
            {{ statusText(order.orderStatus) }}
          </el-tag>
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="租赁车辆">
            <span class="link-text" @click="goVehicleDetail(order.vehicleId)">
              {{ order.vehicleModel }}
              <el-icon><Link /></el-icon>
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="取车门店">
            <span class="link-text" @click="goStoreDetail(order.pickupStoreId)">
              {{ order.pickupStoreName }}
              <el-icon><Link /></el-icon>
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="还车门店">
            <span class="link-text" @click="goStoreDetail(order.returnStoreId)">
              {{ order.returnStoreName }}
              <el-icon><Link /></el-icon>
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="预约取车">{{ formatTime(order.pickupTime) }}</el-descriptions-item>
          <el-descriptions-item label="预约还车">{{ formatTime(order.returnTime) }}</el-descriptions-item>
          <el-descriptions-item label="租赁天数">{{ order.rentalDays }} 天</el-descriptions-item>
          <el-descriptions-item label="日租金">¥{{ formatPrice(order.dailyPrice) }}</el-descriptions-item>
          <el-descriptions-item label="租金总额">
            <span style="color:#ff6b6b;font-weight:bold">
              ¥{{ formatPrice(order.totalAmount) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="押金金额">¥{{ formatPrice(order.depositAmount) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">¥{{ formatPrice(order.paidAmount) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ order.payType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatTime(order.payTime) }}</el-descriptions-item>
          <el-descriptions-item label="实际还车">{{ formatTime(order.actualReturnTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(order.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="2" border style="margin-top:16px" v-if="hasVehicleInfo">
          <el-descriptions-item label="取车电量">{{ order.pickupBattery ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="还车电量">{{ order.returnBattery ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="取车里程">{{ order.pickupMileage ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="还车里程">{{ order.returnMileage ?? '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="1" border style="margin-top:16px" v-if="order.remark">
          <el-descriptions-item label="备注">{{ order.remark }}</el-descriptions-item>
        </el-descriptions>

        <el-descriptions :column="1" border style="margin-top:16px" v-if="order.cancelReason">
          <el-descriptions-item label="取消原因">
            <span style="color:#e17055">{{ order.cancelReason }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 租赁车辆信息 -->
      <div class="ev-card" v-if="order && order.vehicleModel">
        <h3 class="card-title">租赁车辆</h3>
        <div class="vehicle-info-card">
          <div class="vehicle-img">
            <img :src="order.mainImageUrl || '/images/default-car.png'" :alt="order.vehicleModel" />
          </div>
          <div class="vehicle-detail">
            <h4>{{ order.vehicleModel }}</h4>
            <div class="vehicle-specs">
              <span v-if="order.color">颜色：{{ order.color }}</span>
              <span v-if="order.seatCount">座位：{{ order.seatCount }}座</span>
              <span v-if="order.rangeKm">续航：{{ order.rangeKm }}km</span>
            </div>
            <el-button type="primary" link @click="goVehicleDetail(order.vehicleId)">
              查看车辆详情 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 门店信息 -->
      <div class="ev-card" v-if="order && order.pickupStoreName">
        <h3 class="card-title">门店信息</h3>
        <div class="store-info-grid">
          <div class="store-card">
            <h4>取车门店</h4>
            <p class="store-name">{{ order.pickupStoreName }}</p>
            <p class="store-address" v-if="order.pickupStoreAddress">
              <el-icon><Location /></el-icon> {{ order.pickupStoreAddress }}
            </p>
            <p class="store-phone" v-if="order.pickupStorePhone">
              <el-icon><Phone /></el-icon> {{ order.pickupStorePhone }}
            </p>
            <el-button type="primary" link @click="goStoreDetail(order.pickupStoreId)">
              查看门店详情 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="store-card">
            <h4>还车门店</h4>
            <p class="store-name">{{ order.returnStoreName }}</p>
            <p class="store-address" v-if="order.returnStoreAddress">
              <el-icon><Location /></el-icon> {{ order.returnStoreAddress }}
            </p>
            <p class="store-phone" v-if="order.returnStorePhone">
              <el-icon><Phone /></el-icon> {{ order.returnStorePhone }}
            </p>
            <el-button type="primary" link @click="goStoreDetail(order.returnStoreId)">
              查看门店详情 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 倒计时提醒 -->
      <div class="ev-card countdown-card" v-if="order && order.orderStatus === 0">
        <div class="countdown-content">
          <el-icon class="countdown-icon-large" :class="getCountdownClass()"><Timer /></el-icon>
          <div class="countdown-info">
            <h4 class="countdown-title">支付剩余时间</h4>
            <div class="countdown-timer" :class="getCountdownClass()">
              {{ countdown }}
            </div>
            <p class="countdown-hint">超时未支付订单将自动取消</p>
          </div>
        </div>
      </div>

      <!-- 操作区 -->
      <div class="ev-card actions-card" v-if="order">
        <el-button v-if="order.orderStatus === 0"
                   type="primary" size="large" @click="handlePay">
          立即支付
        </el-button>
        <el-button v-if="order.orderStatus === 0 || order.orderStatus === 2"
                   type="danger" size="large" @click="handleCancel">
          取消订单
        </el-button>
        <el-button v-if="order.orderStatus === 2"
                   type="warning" size="large" @click="handlePickup"
                   :disabled="!canPickup"
                   :title="canPickup ? '点击确认取车' : '请在预约取车时间前1小时内操作'">
          去取车
        </el-button>
        <span v-if="order.orderStatus === 2 && !canPickup" class="pickup-hint">
          请在预约取车时间前1小时内操作
        </span>
        <el-button v-if="order.orderStatus === 3"
                   type="success" size="large" @click="handleReturnRequest">
          申请还车
        </el-button>
        <span v-if="isFinalStatus" class="final-hint">
          该订单已完成全部流程
        </span>
      </div>
    </div>

    <!-- 支付弹窗 -->
    <el-dialog v-model="payVisible" title="订单支付" width="480px" :close-on-click-modal="false"
               @closed="closePayDialog">
      <div class="pay-dialog">
        <div class="pay-info-section">
          <div class="pay-info-item">
            <span class="pay-label">订单编号</span>
            <span class="pay-value mono">{{ payData.orderNo || '' }}</span>
          </div>
          <div class="pay-info-item">
            <span class="pay-label">租赁车辆</span>
            <span class="pay-value">{{ payData.vehicleName || '' }}</span>
          </div>
          <div class="pay-info-item">
            <span class="pay-label">租赁天数</span>
            <span class="pay-value">{{ payData.rentalDays || 0 }} 天</span>
          </div>
        </div>
        <el-divider />
        <div class="pay-amounts">
          <div class="pay-amount-row">
            <span>日租金</span>
            <span>¥{{ formatPrice(payData.dailyPrice) }} × {{ payData.rentalDays || 0 }}天</span>
          </div>
          <div class="pay-amount-row">
            <span>租金小计</span>
            <span class="price">¥{{ formatPrice(payData.rentAmount) }}</span>
          </div>
          <div class="pay-amount-row">
            <span>押金（还车退还）</span>
            <span>¥{{ formatPrice(payData.depositAmount) }}</span>
          </div>
          <div class="pay-amount-row total">
            <span>应付总额</span>
            <span class="total-price">¥{{ formatPrice(payData.totalAmount) }}</span>
          </div>
        </div>
        <el-divider />
        <div class="pay-qrcode-area" v-loading="qrLoading">
          <div class="qr-title-row">
            <el-icon size="20" color="#38b48b"><Wallet /></el-icon>
            <span class="qr-desc">请使用支付宝扫一扫完成支付</span>
          </div>
          <div class="qr-img-wrapper">
            <img v-if="qrCodeUrl" :src="qrCodeUrl" class="qr-img" alt="支付二维码" />
            <div v-else class="qr-placeholder">
              <el-icon :size="48" color="#ccc"><PictureFilled /></el-icon>
              <span>二维码加载中...</span>
            </div>
          </div>
          <div class="qr-status" v-if="qrCodeUrl && !paid">
            <el-icon class="is-loading" color="#38b48b"><Loading /></el-icon>
            <span>{{ pollingCount > 0 ? '等待支付中...' : '请扫码支付' }}</span>
          </div>
          <div class="qr-status success" v-if="paid">
            <el-icon color="#38b48b"><CircleCheckFilled /></el-icon>
            <span>支付成功</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="closePayDialog">取消</el-button>
        <el-button v-if="paid" type="success" @click="onPaySuccess">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi, payApi, reviewApi } from '@/api/vehicle'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Wallet, PictureFilled, Loading, CircleCheckFilled, Link, Location, Phone, Timer } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const order = ref(null)
const reviewVisible = ref(false)
const reviewStatus = ref({ reviewed: false, canReview: false })
const reviewTagList = ref([])
const reviewAnonymous = ref(false)
const reviewForm = reactive({ rating: 5, content: '' })
const countdown = ref('00:60')
const countdownTimer = ref(null)
const currentTime = ref(new Date())

const ORDER_TIMEOUT_SECONDS = 60

const payVisible = ref(false)
const paying = ref(false)
const qrLoading = ref(false)
const qrCodeUrl = ref('')
const paid = ref(false)
const pollingCount = ref(0)
const payData = reactive({
  orderId: null,
  orderNo: '',
  vehicleName: '',
  rentalDays: 0,
  dailyPrice: 0,
  rentAmount: 0,
  depositAmount: 0,
  totalAmount: 0
})
let currentOrderNo = null
let pollingTimer = null

const STATUS_MAP = {
  0: '待支付', 1: '已支付', 2: '待取车', 3: '租赁中',
  4: '待还车', 5: '已完成', 6: '已取消', 7: '退款中', 8: '已退款'
}

const STATUS_TYPE_MAP = {
  0: 'warning', 1: 'primary', 2: 'info', 3: 'success',
  4: 'info', 5: 'success', 6: 'info', 7: 'warning', 8: 'success'
}

const STEP_MAP = {
  0: 0, 1: 1, 2: 2, 3: 3, 4: 4, 5: 5, 6: -1, 7: 5, 8: 6
}

const currentStep = computed(() => {
  if (!order.value) return 0
  return STEP_MAP[order.value.orderStatus] ?? 0
})

const isFinalStatus = computed(() => {
  return order.value && [6, 8].includes(order.value.orderStatus)
})

const hasVehicleInfo = computed(() => {
  return order.value && (
    order.value.pickupBattery != null ||
    order.value.returnBattery != null ||
    order.value.pickupMileage != null ||
    order.value.returnMileage != null
  )
})

const canPickup = computed(() => {
  if (!order.value || order.value.orderStatus !== 2 || !order.value.pickupTime) {
    return false
  }
  const now = new Date()
  const pickupTime = new Date(order.value.pickupTime)
  const oneHourBefore = new Date(pickupTime.getTime() - 60 * 60 * 1000)
  return now >= oneHourBefore
})

onMounted(() => {
  loadOrder()
  startCountdown()
})
onUnmounted(() => {
  clearPolling()
  stopCountdown()
})

async function loadOrder() {
  loading.value = true
  try {
    const res = await orderApi.detail(route.params.id)
    order.value = res.data?.code ? res.data.data : res.data
    if (order.value?.id && order.value.orderStatus >= 5 && order.value.orderStatus !== 6) {
      const reviewRes = await reviewApi.getOrderReviewStatus(order.value.id)
      reviewStatus.value = reviewRes.data || { reviewed: false, canReview: false }
    }
    updateCountdown()
  } finally {
    loading.value = false
  }
}

// 倒计时相关函数
function startCountdown() {
  countdownTimer.value = setInterval(() => {
    currentTime.value = new Date()
    updateCountdown()
  }, 1000)
}

function stopCountdown() {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

// 解析时间字符串为Date对象
function parseTime(timeStr) {
  if (!timeStr) return null
  const str = timeStr.replace('T', ' ').substring(0, 19)
  return new Date(str.replace(/-/g, '/'))
}

function updateCountdown() {
  if (!order.value || order.value.orderStatus !== 0 || !order.value.createTime) {
    countdown.value = '00:60'
    return
  }

  const createTime = parseTime(order.value.createTime)
  if (!createTime) {
    countdown.value = '00:60'
    return
  }

  const deadline = createTime.getTime() + ORDER_TIMEOUT_SECONDS * 1000
  const remaining = deadline - currentTime.value.getTime()

  if (remaining <= 0) {
    countdown.value = '已超时'
    return
  }

  const seconds = Math.ceil(remaining / 1000)
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60

  countdown.value = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

function getCountdownClass() {
  if (!order.value || order.value.orderStatus !== 0 || !order.value.createTime) {
    return ''
  }

  const createTime = parseTime(order.value.createTime)
  if (!createTime) return ''

  const deadline = createTime.getTime() + ORDER_TIMEOUT_SECONDS * 1000
  const remaining = deadline - currentTime.value.getTime()

  if (remaining <= 0) {
    return 'expired'
  }

  const seconds = Math.ceil(remaining / 1000)
  if (seconds <= 20) {
    return 'danger'
  } else if (seconds <= 40) {
    return 'warning'
  }
  return 'normal'
}

function statusText(s) { return STATUS_MAP[s] || '未知' }
function statusType(s) { return STATUS_TYPE_MAP[s] || 'info' }

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

function formatPrice(val) {
  if (val === null || val === undefined) return '0'
  return Number(val).toFixed(0)
}

async function handlePay() {
  paid.value = false
  qrCodeUrl.value = ''
  pollingCount.value = 0
  payVisible.value = true
  qrLoading.value = true

  try {
    const res = await payApi.create({ orderId: order.value.id })
    const data = res.data
    Object.assign(payData, {
      orderId: data.orderId,
      orderNo: data.orderNo,
      vehicleName: data.vehicleName,
      rentalDays: data.rentalDays,
      dailyPrice: data.dailyPrice,
      rentAmount: data.rentAmount,
      depositAmount: data.depositAmount,
      totalAmount: data.totalAmount
    })
    currentOrderNo = data.orderNo
    qrCodeUrl.value = data.qrCodeUrl
    startPolling()
  } catch (e) {
    ElMessage.error('创建支付失败')
    payVisible.value = false
  } finally {
    qrLoading.value = false
  }
}

let isPaidProcessing = false

function startPolling() {
  clearPolling()
  isPaidProcessing = false
  pollingTimer = setInterval(async () => {
    if (!currentOrderNo || isPaidProcessing) return
    pollingCount.value++
    try {
      const res = await payApi.status(currentOrderNo)
      if (res.data.paid && !isPaidProcessing) {
        isPaidProcessing = true
        paid.value = true
        clearPolling()
        ElMessage.success('支付成功')
        // 延迟关闭弹窗并刷新
        setTimeout(() => {
          payVisible.value = false
          loadOrder()
        }, 1000)
      }
    } catch (e) {}
  }, 2000)
}

function clearPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

async function handleConfirmPay() {
  if (paid.value) return
  paying.value = true
  try {
    await payApi.confirm({ orderId: payData.orderId, payType: 1 })
    paid.value = true
    clearPolling()
    ElMessage.success('支付成功')
  } catch (e) {
    ElMessage.error('支付失败')
  } finally {
    paying.value = false
  }
}

function closePayDialog() {
  clearPolling()
  payVisible.value = false
  isPaidProcessing = false
  loadOrder()
}

function onPaySuccess() {
  closePayDialog()
}

async function handleCancel() {
  // 待支付订单直接取消
  if (order.value.orderStatus === 0) {
    await ElMessageBox.confirm(
      '确定取消该订单吗？待支付订单取消不产生费用。',
      '取消订单确认',
      { type: 'warning', confirmButtonText: '确定取消', cancelButtonText: '再想想' }
    )
  } else {
    // 已支付/待取车订单计算手续费
    const pickupTime = new Date(order.value.pickupTime)
    const now = new Date()
    const hoursUntilPickup = (pickupTime - now) / (1000 * 60 * 60)

    let feeWarning = ''
    if (hoursUntilPickup < 24) {
      feeWarning = '\n\n⚠️ 取车前24小时内取消将扣除20%手续费'
    } else if (hoursUntilPickup < 48) {
      feeWarning = '\n\n⚠️ 取车前24-48小时取消将扣除10%手续费'
    } else {
      feeWarning = '\n\n✅ 取车前48小时以上取消，免费'
    }

    await ElMessageBox.confirm(
      `确定取消该订单吗？${feeWarning}`,
      '取消订单确认',
      { type: 'warning', confirmButtonText: '确定取消', cancelButtonText: '再想想' }
    )
  }

  try {
    const res = await orderApi.cancel(order.value.id, '用户主动取消')
    const data = res.data

    // 显示取消结果
    ElMessageBox.alert(
      `<div style="line-height: 2">
        <p><strong>订单编号：</strong>${data.orderNo}</p>
        <p><strong>手续费：</strong><span style="color:#ff6b6b">¥${data.cancelFee}</span></p>
        <p><strong>退款金额：</strong><span style="color:#38b48b;font-size:18px">¥${data.refundAmount}</span></p>
        <p style="color:#999;margin-top:10px">${data.feeDesc}</p>
      </div>`,
      '订单取消成功',
      { dangerouslyUseHTMLString: true, confirmButtonText: '知道了' }
    )

    loadOrder()
  } catch (e) {
    ElMessage.error('取消订单失败')
  }
}

async function handleReturnRequest() {
  await ElMessageBox.confirm('确认申请还车吗？', '提示', { type: 'info' })
  try {
    await orderApi.requestReturn(order.value.id)
    ElMessage.success('还车申请已提交，请等待确认')
    loadOrder()
  } catch (e) {
    ElMessage.error('申请还车失败')
  }
}

async function handlePickup() {
  await ElMessageBox.confirm(
    '请确认已到门店取车，取车后将开始计费',
    '去取车确认',
    { type: 'info', confirmButtonText: '确认取车', cancelButtonText: '取消' }
  )
  try {
    await orderApi.pickup(order.value.id)
    ElMessage.success('取车成功，开始计费')
    loadOrder()
  } catch (e) {
    ElMessage.error('取车操作失败')
  }
}

async function submitReview() {
  if (!reviewForm.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  await reviewApi.submit({
    orderId: order.value.id,
    rating: reviewForm.rating,
    tags: reviewTagList.value.join(','),
    content: reviewForm.content,
    anonymous: reviewAnonymous.value ? 1 : 0
  })
  ElMessage.success('评价已提交，等待后台审核')
  reviewVisible.value = false
  reviewStatus.value.reviewed = true
}

function goVehicleDetail(vehicleId) {
  if (vehicleId) {
    router.push(`/vehicle/${vehicleId}`)
  }
}

function goStoreDetail(storeId) {
  if (storeId) {
    router.push(`/stores?storeId=${storeId}`)
  }
}
</script>

<style scoped>
.page-title { font-size: 24px; margin-bottom: 20px; }

.back-row {
  margin-bottom: 12px;
}

.card-title { font-size: 17px; margin-bottom: 16px; }
.card-header-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-header-row .card-title {
  margin-bottom: 0;
}

.status-flow-card {
  margin-bottom: 20px;
}

.actions-card {
  margin-top: 20px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.final-hint {
  color: #999;
  font-size: 14px;
}

/* ===== Countdown Card ===== */
.countdown-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  border: 1px solid #ffeaa7;
}

.countdown-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.countdown-icon-large {
  font-size: 48px;
  color: #e6a23c;
}

.countdown-icon-large.normal {
  color: #409eff;
}

.countdown-icon-large.warning {
  color: #e6a23c;
}

.countdown-icon-large.danger {
  color: #f56c6c;
  animation: pulse 1s infinite;
}

.countdown-icon-large.expired {
  color: #909399;
}

.countdown-info {
  flex: 1;
}

.countdown-title {
  font-size: 16px;
  color: #666;
  margin: 0 0 8px 0;
}

.countdown-timer {
  font-size: 36px;
  font-weight: 700;
  font-family: monospace;
  letter-spacing: 2px;
}

.countdown-timer.normal {
  color: #409eff;
}

.countdown-timer.warning {
  color: #e6a23c;
}

.countdown-timer.danger {
  color: #f56c6c;
  animation: blink 1s infinite;
}

.countdown-timer.expired {
  color: #909399;
  font-size: 24px;
}

.countdown-hint {
  font-size: 13px;
  color: #999;
  margin: 8px 0 0 0;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.pickup-hint {
  color: #e6a23c;
  font-size: 13px;
}

/* ===== Payment Dialog ===== */
.pay-dialog { padding: 0 10px; }
.pay-info-section { display: flex; flex-direction: column; gap: 10px; }
.pay-info-item { display: flex; justify-content: space-between; }
.pay-label { color: #999; }
.pay-value { font-weight: 500; }
.pay-value.mono { font-family: monospace; font-size: 13px; }
.pay-amounts { display: flex; flex-direction: column; gap: 10px; }
.pay-amount-row { display: flex; justify-content: space-between; }
.pay-amount-row .price { color: #ff6b6b; font-weight: 600; }
.pay-amount-row.total { font-size: 17px; }
.pay-amount-row.total .total-price { color: #ff6b6b; font-weight: 700; font-size: 20px; }
.pay-qrcode-area { text-align: center; }
.qr-title-row { display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 16px; }
.qr-desc { font-size: 14px; color: #555; }
.qr-img-wrapper { width: 180px; height: 180px; margin: 0 auto; border: 1px solid #eee; border-radius: 10px; display: flex; align-items: center; justify-content: center; background: #fafafa; }
.qr-img { width: 170px; height: 170px; object-fit: contain; }
.qr-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #ccc; font-size: 13px; }
.qr-status { margin-top: 12px; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 14px; color: #38b48b; }
.qr-status.success { color: #38b48b; }

/* ===== Link Text ===== */
.link-text {
  color: #409eff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.link-text:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* ===== Vehicle Info Card ===== */
.vehicle-info-card {
  display: flex;
  gap: 20px;
  align-items: center;
}

.vehicle-img {
  width: 180px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
}

.vehicle-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.vehicle-detail h4 {
  font-size: 18px;
  margin: 0 0 12px;
}

.vehicle-specs {
  display: flex;
  gap: 16px;
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
}

/* ===== Store Info Grid ===== */
.store-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.store-card {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.store-card h4 {
  font-size: 15px;
  margin: 0 0 12px;
  color: #333;
}

.store-name {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 8px;
}

.store-address,
.store-phone {
  font-size: 14px;
  color: #666;
  margin: 0 0 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>