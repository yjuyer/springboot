<template>
  <div class="admin-order-manage">
    <div class="page-header">
      <h2>订单管理</h2>
    </div>

    <div class="ev-card search-bar">
      <el-select v-model="query.status" placeholder="订单状态" @change="loadData" style="width:150px" clearable>
        <el-option label="全部" :value="null" />
        <el-option label="待支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="待取车" :value="2" />
        <el-option label="租赁中" :value="3" />
        <el-option label="待还车" :value="4" />
        <el-option label="已完成" :value="5" />
        <el-option label="已取消" :value="6" />
        <el-option label="退款中" :value="7" />
        <el-option label="已退款" :value="8" />
      </el-select>
      <el-input v-model="query.orderNo" placeholder="订单编号" clearable @change="loadData"
                style="width:220px" prefix-icon="Search" />
      <el-input v-model="query.username" placeholder="用户名/手机号" clearable @change="loadData"
                style="width:200px" prefix-icon="User" />
    </div>

    <div class="ev-card">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单编号" min-width="160" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="vehicleModel" label="车辆" min-width="130" />
        <el-table-column label="订单金额" width="120">
          <template #default="{ row }">
            <span style="color:#ff6b6b;font-weight:600">
              ¥{{ formatPrice(row.totalAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="押金" width="100">
          <template #default="{ row }">¥{{ formatPrice(row.depositAmount) }}</template>
        </el-table-column>
        <el-table-column label="租赁时间" min-width="220">
          <template #default="{ row }">
            {{ formatTime(row.pickupTime) }} ~ {{ formatTime(row.returnTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.orderStatus)" size="small">
              {{ statusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.orderStatus === 2"
                       type="primary" size="small" @click="openPickup(row)">
              确认取车
            </el-button>
            <el-button v-if="row.orderStatus === 4"
                       type="success" size="small" @click="openReturnConfirm(row)">
              确认还车
            </el-button>
            <el-button v-if="row.orderStatus === 5"
                       type="warning" size="small" @click="handleInitiateRefund(row)">
              发起退款
            </el-button>
            <el-button v-if="row.orderStatus === 7"
                       type="danger" size="small" @click="handleCompleteRefund(row)">
              完成退款
            </el-button>
            <span v-if="!hasAction(row.orderStatus)" style="color:#bbb;font-size:12px">--</span>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px;text-align:right">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10,20,50]"
          layout="total,sizes,prev,pager,next"
          @change="loadData" />
      </div>
    </div>

    <!-- 确认取车弹窗 -->
    <el-dialog v-model="pickupVisible" title="确认取车" width="460px" @closed="pickupForm = {}">
      <el-descriptions :column="2" border style="margin-bottom:16px">
        <el-descriptions-item label="订单编号">{{ selectedOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ selectedOrder?.username }}</el-descriptions-item>
        <el-descriptions-item label="车辆">{{ selectedOrder?.vehicleModel }}</el-descriptions-item>
        <el-descriptions-item label="取车门店">{{ selectedOrder?.pickupStoreName }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="100px">
        <el-form-item label="当前电量(%)" required>
          <el-input-number v-model="pickupForm.battery" :min="0" :max="100" style="width:100%" />
        </el-form-item>
        <el-form-item label="当前里程(km)" required>
          <el-input-number v-model="pickupForm.mileage" :min="0" :precision="1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pickupVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doPickup">
          确认取车
        </el-button>
      </template>
    </el-dialog>

    <!-- 确认还车弹窗 -->
    <el-dialog v-model="returnVisible" title="确认还车" width="460px" @closed="returnForm = {}">
      <el-descriptions :column="2" border style="margin-bottom:16px">
        <el-descriptions-item label="订单编号">{{ selectedOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ selectedOrder?.username }}</el-descriptions-item>
        <el-descriptions-item label="车辆">{{ selectedOrder?.vehicleModel }}</el-descriptions-item>
        <el-descriptions-item label="取车电量">{{ selectedOrder?.pickupBattery ?? '-' }}%</el-descriptions-item>
        <el-descriptions-item label="取车里程">{{ selectedOrder?.pickupMileage ?? '-' }}km</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="100px">
        <el-form-item label="还车电量(%)" required>
          <el-input-number v-model="returnForm.battery" :min="0" :max="100" style="width:100%" />
        </el-form-item>
        <el-form-item label="还车里程(km)" required>
          <el-input-number v-model="returnForm.mileage" :min="0" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="还车后状态" required>
          <el-select v-model="returnForm.vehicleStatus" style="width:100%">
            <el-option label="空闲" :value="0" />
            <el-option label="维修中" :value="3" />
            <el-option label="充电中" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doReturnConfirm">
          确认还车
        </el-button>
      </template>
    </el-dialog>

    <!-- 完成退款弹窗 -->
    <el-dialog v-model="refundCompleteVisible" title="确认退款完成" width="420px">
      <el-descriptions :column="1" border style="margin-bottom:16px">
        <el-descriptions-item label="订单编号">{{ selectedOrder?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ selectedOrder?.username }}</el-descriptions-item>
        <el-descriptions-item label="押金金额">
          <span style="color:#38b48b;font-weight:bold">
            ¥{{ formatPrice(selectedOrder?.depositAmount) }}
          </span>
        </el-descriptions-item>
      </el-descriptions>
      <p style="color:#999;text-align:center">
        确认押金已实际退还到用户账户？
      </p>
      <template #footer>
        <el-button @click="refundCompleteVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="doCompleteRefund">
          确认已退款
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { adminApi } from '@/api/vehicle'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const records = ref([])
const total = ref(0)
const submitting = ref(false)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null,
  orderNo: '',
  username: ''
})

const STATUS_MAP = {
  0: '待支付', 1: '已支付', 2: '待取车', 3: '租赁中',
  4: '待还车', 5: '已完成', 6: '已取消', 7: '退款中', 8: '已退款'
}

const STATUS_TYPE_MAP = {
  0: 'warning', 1: 'primary', 2: 'info', 3: 'success',
  4: 'info', 5: 'success', 6: 'info', 7: 'warning', 8: 'success'
}

function statusText(s) { return STATUS_MAP[s] || '未知' }
function statusType(s) { return STATUS_TYPE_MAP[s] || 'info' }

function hasAction(s) { return [2, 4, 5, 7].includes(s) }

function formatPrice(val) {
  if (val === null || val === undefined) return '0'
  return Number(val).toFixed(0)
}

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function loadData() {
  loading.value = true
  try {
    const params = { ...query }
    if (params.status === null) delete params.status
    if (!params.orderNo) delete params.orderNo
    if (!params.username) delete params.username
    const res = await adminApi.getOrderList(params)
    const body = res.data?.code ? res.data : { data: res.data }
    records.value = body.data?.records || body.data || []
    total.value = body.data?.total || 0
  } finally {
    loading.value = false
  }
}

// ========== 确认取车 ==========
const pickupVisible = ref(false)
const selectedOrder = ref(null)
const pickupForm = reactive({ battery: null, mileage: null })

function openPickup(row) {
  selectedOrder.value = row
  pickupForm.battery = null
  pickupForm.mileage = null
  pickupVisible.value = true
}

async function doPickup() {
  if (pickupForm.battery == null || pickupForm.mileage == null) {
    ElMessage.warning('请填写电量和里程')
    return
  }
  submitting.value = true
  try {
    await adminApi.pickupOrder({
      orderId: selectedOrder.value.id,
      battery: pickupForm.battery,
      mileage: pickupForm.mileage
    })
    ElMessage.success('取车确认成功')
    pickupVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '取车确认失败')
  } finally {
    submitting.value = false
  }
}

// ========== 确认还车 ==========
const returnVisible = ref(false)
const returnForm = reactive({ battery: null, mileage: null, vehicleStatus: 0 })

function openReturnConfirm(row) {
  selectedOrder.value = row
  returnForm.battery = null
  returnForm.mileage = null
  returnForm.vehicleStatus = 0
  returnVisible.value = true
}

async function doReturnConfirm() {
  if (returnForm.battery == null || returnForm.mileage == null) {
    ElMessage.warning('请填写电量和里程')
    return
  }
  submitting.value = true
  try {
    await adminApi.confirmReturn(selectedOrder.value.id, {
      battery: returnForm.battery,
      mileage: returnForm.mileage
    })
    await adminApi.updateVehicle({ id: selectedOrder.value.vehicleId, vehicleStatus: returnForm.vehicleStatus })
    ElMessage.success('还车确认成功')
    returnVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '还车确认失败')
  } finally {
    submitting.value = false
  }
}

// ========== 发起退款 ==========
async function handleInitiateRefund(row) {
  try {
    await ElMessageBox.confirm(
      `确认发起押金退款？订单号：${row.orderNo}`,
      '发起退款',
      { type: 'info' }
    )
  } catch { return }

  submitting.value = true
  try {
    await adminApi.initiateRefund(row.id)
    ElMessage.success('退款流程已发起')
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '发起退款失败')
  } finally {
    submitting.value = false
  }
}

// ========== 完成退款 ==========
const refundCompleteVisible = ref(false)

function handleCompleteRefund(row) {
  selectedOrder.value = row
  refundCompleteVisible.value = true
}

async function doCompleteRefund() {
  submitting.value = true
  try {
    await adminApi.completeRefund(selectedOrder.value.id, {})
    ElMessage.success('退款完成')
    refundCompleteVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '退款完成失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadData())
</script>

<style scoped>
.admin-order-manage { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; color: #2d3436; }

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}
</style>