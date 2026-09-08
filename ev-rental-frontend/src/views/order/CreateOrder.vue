<!--
  创建订单页面

  功能说明：
  1. 车辆信息展示 - 显示车辆图片、名称、续航、座位、日租金、押金
  2. 预约表单 - 选择取车/还车门店、取车/还车时间、备注
  3. 优惠券选择 - 选择可用优惠券，自动计算折扣后金额
  4. 费用预估 - 实时计算租赁天数、优惠金额、押金、合计金额

  业务规则：
  - 信誉积分低于80分无法租车
  - 取车门店固定为车辆所在门店
  - 一个订单只能使用一张优惠券
  - 还车时间必须晚于取车时间

  数据来源：
  - vehicleApi.detail() 获取车辆详情
  - vehicleApi.getStores() 获取门店列表
  - couponApi.getAvailableForOrder() 获取可用优惠券

  提交后：
  - 调用 orderApi.create() 创建订单
  - 跳转到订单列表页面
-->
<template>
  <div class="create-order-page">
    <h2 class="page-title">预约租车</h2>

    <div class="order-content" v-loading="loading">
      <!-- 车辆信息 -->
      <div class="ev-card vehicle-info" v-if="vehicle">
        <div class="vehicle-img">
          <img :src="getVehicleImage(vehicle)" :alt="vehicle.model" />
        </div>
        <div class="vehicle-detail">
          <h3>{{ vehicle.model }}</h3>
          <p>续航 {{ vehicle.rangeKm }}km · {{ vehicle.seatCount }}座</p>
          <div class="price-row">
            <span class="price">{{ vehicle.dailyPrice }}</span>
            <span>/天</span>
            <span class="deposit">押金 ¥{{ vehicle.deposit }}</span>
          </div>
        </div>
      </div>

      <!-- 预约表单 -->
      <div class="ev-card">
        <h3 class="card-title">填写预约信息</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="取车门店" prop="pickupStoreId">
            <el-select v-model="form.pickupStoreId" placeholder="车辆所在门店" style="width:100%" disabled>
              <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
            </el-select>
            <div class="form-tip">取车门店固定为车辆所在门店</div>
          </el-form-item>
          <el-form-item label="还车门店" prop="returnStoreId">
            <el-select v-model="form.returnStoreId" placeholder="请选择还车门店" style="width:100%">
              <el-option v-for="s in stores" :key="s.id" :label="s.storeName" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="取车时间" prop="pickupTime">
            <el-date-picker v-model="form.pickupTime" type="datetime" placeholder="选择取车时间"
                            value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
          </el-form-item>
          <el-form-item label="还车时间" prop="returnTime">
            <el-date-picker v-model="form.returnTime" type="datetime" placeholder="选择还车时间"
                            value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
          </el-form-item>
          <el-form-item label="优惠券">
            <el-select v-model="form.userCouponId" placeholder="选择优惠券（可选）" style="width:100%" clearable>
              <el-option
                v-for="c in availableCoupons"
                :key="c.id"
                :label="`${c.couponName} - ${getCouponLabel(c)}`"
                :value="c.id"
              />
            </el-select>
            <div class="form-tip" v-if="form.userCouponId">优惠后金额：¥{{ discountedAmount }}</div>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="可选" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 费用预估 -->
      <div class="ev-card fee-box">
        <h3 class="card-title">费用预估</h3>
        <div class="fee-row">
          <span>租赁天数</span>
          <span>{{ rentalDays }} 天</span>
        </div>
        <div class="fee-row">
          <span>日租金 × 天数</span>
          <span>¥{{ totalAmount }}</span>
        </div>
        <div class="fee-row" v-if="form.userCouponId">
          <span>优惠券抵扣</span>
          <span class="discount">-¥{{ (parseFloat(totalAmount) - parseFloat(discountedAmount)).toFixed(2) }}</span>
        </div>
        <div class="fee-row">
          <span>小计</span>
          <span class="price">¥{{ discountedAmount }}</span>
        </div>
        <div class="fee-row">
          <span>押金（还车退还）</span>
          <span>¥{{ vehicle?.deposit || 0 }}</span>
        </div>
        <el-divider />
        <div class="fee-row total">
          <span>合计</span>
          <span class="price">{{ totalWithDeposit }}</span>
        </div>
        <el-button type="primary" size="large" :loading="submitting" style="width:100%;margin-top:16px"
                   @click="handleSubmit">
          提交订单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { vehicleApi, orderApi, userApi, couponApi } from '@/api/vehicle'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(true)
const submitting = ref(false)
const vehicle = ref(null)
const stores = ref([])
const availableCoupons = ref([])

const form = reactive({
  vehicleId: Number(route.params.vehicleId),
  pickupStoreId: null,
  returnStoreId: null,
  pickupTime: '',
  returnTime: '',
  userCouponId: null,
  remark: ''
})

const rules = {
  pickupStoreId: [{ required: true, message: '请选择取车门店', trigger: 'change' }],
  returnStoreId: [{ required: true, message: '请选择还车门店', trigger: 'change' }],
  pickupTime: [{ required: true, message: '请选择取车时间', trigger: 'change' }],
  returnTime: [{ required: true, message: '请选择还车时间', trigger: 'change' }]
}

onMounted(async () => {
  try {
    // 信誉积分检查
    const pRes = await userApi.getProfile()
    const credit = pRes.data.credit || pRes.data.creditScore || 100
    if (credit < 80) {
      ElMessage.error('信誉积分不足80分，暂时无法租车')
      router.back()
      return
    }

    const [vRes, sRes, cRes] = await Promise.all([
      vehicleApi.detail(route.params.vehicleId),
      vehicleApi.getStores(),
      couponApi.getAvailableForOrder()
    ])
    vehicle.value = vRes.data
    stores.value = sRes.data
    availableCoupons.value = cRes.data || []

    // 取车门店固定为车辆所在门店
    if (vehicle.value && vehicle.value.storeId) {
      form.pickupStoreId = vehicle.value.storeId
    }
  } finally {
    loading.value = false
  }
})

// 计算租赁天数
const rentalDays = computed(() => {
  if (!form.pickupTime || !form.returnTime) return 0
  const diff = new Date(form.returnTime) - new Date(form.pickupTime)
  const days = Math.ceil(diff / (1000 * 60 * 60 * 24))
  return days > 0 ? days : 1
})

// 计算总价
const totalAmount = computed(() => {
  if (!vehicle.value) return 0
  return (vehicle.value.dailyPrice * rentalDays.value).toFixed(2)
})

const totalWithDeposit = computed(() => {
  if (!vehicle.value) return 0
  const discount = form.userCouponId ? (parseFloat(totalAmount.value) - parseFloat(discountedAmount.value)) : 0
  return (parseFloat(discountedAmount.value) + vehicle.value.deposit).toFixed(2)
})

// 计算优惠后金额
const discountedAmount = computed(() => {
  if (!form.userCouponId || !vehicle.value) return totalAmount.value
  const coupon = availableCoupons.value.find(c => c.id === form.userCouponId)
  if (!coupon) return totalAmount.value

  const amount = parseFloat(totalAmount.value)
  switch (coupon.couponType) {
    case 1: // 满减券
      return Math.max(0, amount - parseFloat(coupon.discountValue)).toFixed(2)
    case 2: // 折扣券
      return (amount * parseFloat(coupon.discountValue)).toFixed(2)
    case 3: // 立减券
      return Math.max(0, amount - parseFloat(coupon.discountValue)).toFixed(2)
    default:
      return amount.toFixed(2)
  }
})

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await orderApi.create(form)
    ElMessage.success('订单创建成功，请尽快支付')
    router.push('/order')
  } catch (e) {} finally {
    submitting.value = false
  }
}

function getVehicleImage(v) {
  if (!v) return ''
  return v.coverImage || v.mainImageUrl || v.image || ''
}

// 获取优惠券显示文本
function getCouponLabel(coupon) {
  switch (coupon.couponType) {
    case 1: return `减¥${coupon.discountValue}`
    case 2: return `${coupon.discountValue * 10}折`
    case 3: return `立减¥${coupon.discountValue}`
    default: return ''
  }
}
</script>

<style scoped>
.page-title { font-size: 24px; margin-bottom: 20px; }

.order-content {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 20px;
}

.vehicle-info {
  display: flex;
  gap: 20px;
  grid-column: 1 / -1;
}

.vehicle-img {
  width: 240px;
  height: 160px;
  border-radius: 10px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
}
.vehicle-img img { width: 100%; height: 100%; object-fit: cover; }

.vehicle-detail h3 { font-size: 20px; margin-bottom: 8px; }
.vehicle-detail p { color: #999; margin-bottom: 12px; }
.price-row { display: flex; align-items: baseline; gap: 6px; }
.price-row .price { font-size: 24px; color: #ff6b6b; font-weight: 700; }
.price-row .price::before { content: '¥'; font-size: 14px; }
.price-row .deposit { margin-left: 16px; color: #999; font-size: 13px; }

.card-title { font-size: 17px; margin-bottom: 16px; }

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.fee-box .fee-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  color: #666;
}
.fee-box .fee-row .price { color: #ff6b6b; font-weight: 600; }
.fee-box .fee-row .price::before { content: ''; }
.fee-box .fee-row .discount { color: #38b48b; font-weight: 600; }
.fee-box .fee-row.total { font-size: 18px; font-weight: 700; color: #333; }
.fee-box .fee-row.total .price { font-size: 22px; }
</style>
