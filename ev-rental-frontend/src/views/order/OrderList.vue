<template>
  <div class="order-list-page">
    <h2 class="page-title">我的订单</h2>

    <el-tabs v-model="activeTab" @tab-change="loadOrders" class="ev-card">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="0" />
      <el-tab-pane label="已支付" name="1" />
      <el-tab-pane label="待取车" name="2" />
      <el-tab-pane label="租赁中" name="3" />
      <el-tab-pane label="待还车" name="4" />
      <el-tab-pane label="已完成" name="5" />
      <el-tab-pane label="已取消" name="6" />
      <el-tab-pane label="退款中" name="7" />
      <el-tab-pane label="已退款" name="8" />
    </el-tabs>

    <div v-loading="loading">
      <div v-for="order in orders" :key="order.id" class="order-card ev-card">
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <el-tag :type="statusType(order.orderStatus)" size="small">
            {{ statusText(order.orderStatus) }}
          </el-tag>
        </div>

        <div class="order-body" @click="goDetail(order.id)">
          <div class="order-img">
            <img :src="getVehicleImage(order)" :alt="order.vehicleModel" />
          </div>
          <div class="order-info">
            <h3>{{ order.vehicleModel }}</h3>
            <p>
              <el-icon><Calendar /></el-icon>
              {{ formatTime(order.pickupTime) }} ~ {{ formatTime(order.returnTime) }}
            </p>
            <p>
              <el-icon><Shop /></el-icon>
              取车：{{ order.pickupStoreName }} ｜ 还车：{{ order.returnStoreName }}
            </p>
            <p>
              <el-icon><Timer /></el-icon>
              租赁 {{ order.rentalDays }} 天
            </p>
          </div>
          <div class="order-price">
            <div class="price">{{ order.totalAmount }}</div>
            <span class="unit">租金</span>
          </div>
        </div>

        <div class="order-footer">
          <div class="footer-left">
            <span class="time">创建时间：{{ formatTime(order.createTime) }}</span>
            <div v-if="order.orderStatus === 0" class="countdown-wrap">
              <el-icon class="countdown-icon"><Timer /></el-icon>
              <span class="countdown-text">
                支付剩余：
                <span :class="['countdown', getCountdownClass(order)]">
                  {{ getCountdown(order) }}
                </span>
              </span>
            </div>
          </div>
          <div class="actions">
            <el-button v-if="order.orderStatus === 0"
                       type="primary" size="small" @click="handlePay(order)">
              立即支付
            </el-button>
            <el-button v-if="order.orderStatus === 0 || order.orderStatus === 2"
                       type="danger" size="small" @click="handleCancel(order)">
              取消订单
            </el-button>
            <el-button v-if="order.orderStatus === 2"
                       type="warning" size="small" @click="handlePickup(order)"
                       :disabled="!canPickup(order)"
                       :title="canPickup(order) ? '点击确认取车' : '请在预约取车时间前1小时内操作'">
              去取车
            </el-button>
            <span v-if="order.orderStatus === 2 && !canPickup(order)" class="pickup-hint">
              请在预约取车时间前1小时内操作
            </span>
            <el-button v-if="order.orderStatus === 3"
                       type="success" size="small" @click="handleReturnRequest(order)">
              申请还车
            </el-button>
            <el-button size="small" @click="goDetail(order.id)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
    </div>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadOrders" />
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
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi, payApi } from '@/api/vehicle'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Wallet, PictureFilled, Loading, CircleCheckFilled, Timer, Calendar, Shop } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const activeTab = ref('')
const countdownTimer = ref(null)
const currentTime = ref(new Date())

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

const ORDER_TIMEOUT_SECONDS = 60

onMounted(() => {
  loadOrders()
  startCountdown()
})
onUnmounted(() => {
  clearPolling()
  stopCountdown()
})

async function loadOrders() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (activeTab.value) params.status = Number(activeTab.value)
    console.log('[OrderList] 请求参数:', params)
    const res = await orderApi.myOrders(params)
    console.log('[OrderList] API返回:', res)
    // 拦截器已返回 R 对象：{ code: 200, data: { records: [...], total: X } }
    const page = res.data || {}
    console.log('[OrderList] 分页数据:', page)
    orders.value = page.records || []
    total.value = page.total || 0
    console.log('[OrderList] 订单数:', orders.value.length, '总数:', total.value)
  } catch (e) {
    console.error('[OrderList] 加载订单失败', e)
  } finally {
    loading.value = false
  }
}

// 倒计时相关函数
function startCountdown() {
  countdownTimer.value = setInterval(() => {
    currentTime.value = new Date()
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
  // 处理 "2026-06-11T08:07:32" 或 "2026-06-11 08:07:32" 格式
  const str = timeStr.replace('T', ' ').substring(0, 19)
  return new Date(str.replace(/-/g, '/'))
}

function getCountdown(order) {
  if (!order || order.orderStatus !== 0 || !order.createTime) {
    return '00:60'
  }

  const createTime = parseTime(order.createTime)
  if (!createTime) return '00:60'

  const deadline = createTime.getTime() + ORDER_TIMEOUT_SECONDS * 1000
  const remaining = deadline - currentTime.value.getTime()

  if (remaining <= 0) {
    return '已超时'
  }

  const seconds = Math.ceil(remaining / 1000)
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60

  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

function getCountdownClass(order) {
  if (!order || order.orderStatus !== 0 || !order.createTime) {
    return ''
  }

  const createTime = parseTime(order.createTime)
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

function goDetail(id) {
  router.push(`/order/detail/${id}`)
}

async function handlePay(order) {
  paid.value = false
  qrCodeUrl.value = ''
  pollingCount.value = 0
  payVisible.value = true
  qrLoading.value = true

  try {
    const res = await payApi.create({ orderId: order.id })
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
          loadOrders()
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
  loadOrders()
}

function onPaySuccess() {
  closePayDialog()
}

async function handleCancel(order) {
  // 待支付订单直接取消
  if (order.orderStatus === 0) {
    await ElMessageBox.confirm(
      '确定取消该订单吗？待支付订单取消不产生费用。',
      '取消订单确认',
      { type: 'warning', confirmButtonText: '确定取消', cancelButtonText: '再想想' }
    )
  } else {
    // 已支付/待取车订单计算手续费
    const pickupTime = new Date(order.pickupTime)
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
    const res = await orderApi.cancel(order.id, '用户主动取消')
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

    activeTab.value = '6'
    pageNum.value = 1
    loadOrders()
  } catch (e) {
    ElMessage.error('取消订单失败')
  }
}

async function handleReturnRequest(order) {
  await ElMessageBox.confirm('确认申请还车吗？', '提示', { type: 'info' })
  try {
    await orderApi.requestReturn(order.id)
    ElMessage.success('还车申请已提交，请等待确认')
    loadOrders()
  } catch (e) {
    ElMessage.error('申请还车失败')
  }
}

async function handlePickup(order) {
  await ElMessageBox.confirm(
    '请确认已到门店取车，取车后将开始计费',
    '去取车确认',
    { type: 'info', confirmButtonText: '确认取车', cancelButtonText: '取消' }
  )
  try {
    await orderApi.pickup(order.id)
    ElMessage.success('取车成功，开始计费')
    loadOrders()
  } catch (e) {
    ElMessage.error('取车操作失败')
  }
}

function formatPrice(val) {
  if (val === null || val === undefined) return '0'
  return Number(val).toFixed(0)
}

const STATUS_MAP = {
  0: '待支付',
  1: '已支付',
  2: '待取车',
  3: '租赁中',
  4: '待还车',
  5: '已完成',
  6: '已取消',
  7: '退款中',
  8: '已退款'
}

const STATUS_TYPE_MAP = {
  0: 'warning',
  1: 'primary',
  2: 'info',
  3: 'success',
  4: 'info',
  5: 'success',
  6: 'info',
  7: 'warning',
  8: 'success'
}

function statusText(s) {
  return STATUS_MAP[s] || '未知'
}

function statusType(s) {
  return STATUS_TYPE_MAP[s] || 'info'
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

function getVehicleImage(o) {
  if (!o) return ''
  return o.mainImageUrl || ''
}

function canPickup(order) {
  if (!order || order.orderStatus !== 2 || !order.pickupTime) {
    return false
  }
  const now = new Date()
  const pickupTime = new Date(order.pickupTime)
  const oneHourBefore = new Date(pickupTime.getTime() - 60 * 60 * 1000)
  return now >= oneHourBefore
}
</script>

<style scoped>
.page-title { font-size: 24px; margin-bottom: 20px; }

.order-card {
  margin-bottom: 16px;
  cursor: pointer;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  color: #999;
  font-size: 13px;
}

.order-body {
  display: flex;
  gap: 16px;
}

.order-img {
  width: 160px;
  height: 110px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
}

.order-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.order-info {
  flex: 1;
  min-width: 0;
}

.order-info h3 {
  font-size: 16px;
  margin-bottom: 8px;
}

.order-info p {
  color: #999;
  font-size: 13px;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.order-price {
  text-align: right;
  flex-shrink: 0;
}

.order-price .price {
  font-size: 22px;
  color: #ff6b6b;
  font-weight: 700;
}

.order-price .price::before {
  content: '¥';
  font-size: 13px;
}

.order-price .unit {
  color: #999;
  font-size: 12px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.order-footer .time {
  color: #bbb;
  font-size: 12px;
}

.footer-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.countdown-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.countdown-icon {
  color: #e6a23c;
}

.countdown-text {
  font-size: 13px;
  color: #666;
}

.countdown {
  font-weight: 700;
  font-family: monospace;
  font-size: 15px;
}

.countdown.normal {
  color: #409eff;
}

.countdown.warning {
  color: #e6a23c;
}

.countdown.danger {
  color: #f56c6c;
  animation: blink 1s infinite;
}

.countdown.expired {
  color: #909399;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pickup-hint {
  color: #e6a23c;
  font-size: 12px;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* ===== Payment Dialog ===== */
.pay-dialog {
  padding: 0 10px;
}

.pay-info-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pay-info-item {
  display: flex;
  justify-content: space-between;
}

.pay-label {
  color: #999;
}

.pay-value {
  font-weight: 500;
}

.pay-value.mono {
  font-family: monospace;
  font-size: 13px;
}

.pay-amounts {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pay-amount-row {
  display: flex;
  justify-content: space-between;
}

.pay-amount-row .price {
  color: #ff6b6b;
  font-weight: 600;
}

.pay-amount-row.total {
  font-size: 17px;
}

.pay-amount-row.total .total-price {
  color: #ff6b6b;
  font-weight: 700;
  font-size: 20px;
}

.pay-qrcode-area {
  text-align: center;
}

.qr-title-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
}

.qr-desc {
  font-size: 14px;
  color: #555;
}

.qr-img-wrapper {
  width: 180px;
  height: 180px;
  margin: 0 auto;
  border: 1px solid #eee;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.qr-img {
  width: 170px;
  height: 170px;
  object-fit: contain;
}

.qr-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #ccc;
  font-size: 13px;
}

.qr-status {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  color: #38b48b;
}

.qr-status.success {
  color: #38b48b;
}
</style>