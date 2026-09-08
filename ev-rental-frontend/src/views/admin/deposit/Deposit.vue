<template>
  <div class="deposit-manage">
    <div class="page-header">
      <h2>押金管理</h2>
    </div>

    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background:#e8f5e9">
          <el-icon :size="28" color="#38b48b"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">￥{{ formatAmount(stats.frozenAmount) }}</span>
          <span class="stat-label">冻结押金</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#e3f2fd">
          <el-icon :size="28" color="#38b48b"><RefreshRight /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">￥{{ formatAmount(stats.refundedAmount) }}</span>
          <span class="stat-label">已退押金</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#fff3e0">
          <el-icon :size="28" color="#e17055"><List /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.frozenCount }}</span>
          <span class="stat-label">冻结笔数</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background:#fce4ec">
          <el-icon :size="28" color="#d63031"><Finished /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.refundedCount }}</span>
          <span class="stat-label">已退笔数</span>
        </div>
      </div>
    </div>

    <div class="ev-card search-bar">
      <el-select v-model="query.status" placeholder="押金状态" @change="loadData" style="width:140px" clearable>
        <el-option label="全部" :value="null" />
        <el-option label="待支付" :value="0" />
        <el-option label="押金冻结" :value="1" />
        <el-option label="押金已退" :value="2" />
      </el-select>
      <el-input v-model="query.orderNo" placeholder="订单编号" clearable @change="loadData"
                style="width:220px" prefix-icon="Search" />
    </div>

    <div class="ev-card">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderNo" label="订单编号" min-width="160" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="vehicleInfo" label="车辆" min-width="140" />
        <el-table-column label="押金金额" width="120">
          <template #default="{ row }">￥{{ formatAmount(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="freezeTime" label="冻结时间" width="170" />
        <el-table-column label="退款时间" width="170">
          <template #default="{ row }">{{ row.refundTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="warning" size="small"
                       @click="openRefund(row)">退款</el-button>
            <span v-else style="color:#999;font-size:12px">-</span>
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
          @change="loadData"
        />
      </div>
    </div>

    <el-dialog v-model="refundVisible" title="押金退款" width="460px" @closed="refundForm = {}">
      <el-descriptions :column="2" border style="margin-bottom:16px">
        <el-descriptions-item label="订单编号">{{ selectedDeposit?.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ selectedDeposit?.username }}</el-descriptions-item>
        <el-descriptions-item label="车辆">{{ selectedDeposit?.vehicleInfo }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">
          <span style="color:#38b48b;font-weight:bold">
            ￥{{ formatAmount(selectedDeposit?.amount) }}
          </span>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px">
        <el-form-item label="退款方式">
          <el-radio-group v-model="refundForm.refundType">
            <el-radio :value="1">原路退回</el-radio>
            <el-radio :value="2">线下转账</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundForm.reason" type="textarea" :rows="2"
                    placeholder="可选填退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" :loading="refunding" @click="doRefund">
          确认退款
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { depositApi } from '@/api/vehicle'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const records = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null,
  orderNo: ''
})

const stats = reactive({
  frozenAmount: 0,
  refundedAmount: 0,
  frozenCount: 0,
  refundedCount: 0
})

function formatAmount(val) {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}

function statusText(s) {
  const map = { 0: '待支付', 1: '押金冻结', 2: '押金已退' }
  return map[s] || '未知'
}

function statusTag(s) {
  const map = { 0: 'info', 1: 'warning', 2: 'success' }
  return map[s] || 'info'
}

async function loadStats() {
  try {
    const res = await depositApi.stats()
    if (res.data?.code === 200 || !res.data?.code) {
      Object.assign(stats, res.data?.data || res.data || {})
    }
  } catch { /* ignore */ }
}

async function loadData() {
  loading.value = true
  try {
    const res = await depositApi.list(query)
    const body = res.data?.code ? res.data : { data: res.data }
    records.value = body.data?.records || body.data || []
    total.value = body.data?.total || 0
  } finally {
    loading.value = false
  }
}

const refundVisible = ref(false)
const refunding = ref(false)
const selectedDeposit = ref(null)
const refundForm = reactive({ refundType: 1, reason: '' })

function openRefund(row) {
  selectedDeposit.value = row
  refundForm.refundType = 1
  refundForm.reason = ''
  refundVisible.value = true
}

async function doRefund() {
  try {
    await ElMessageBox.confirm(
      `确认退还 ￥${formatAmount(selectedDeposit.value.amount)} 押金？`,
      '确认退款',
      { type: 'warning' }
    )
  } catch { return; }

  refunding.value = true
  try {
    await depositApi.refund({
      orderId: selectedDeposit.value.orderId,
      refundType: refundForm.refundType,
      reason: refundForm.reason
    })
    ElMessage.success('退款成功')
    refundVisible.value = false
    loadData()
    loadStats()
  } catch (e) {
    const msg = e.response?.data?.message || e.response?.data?.msg || '退款失败'
    ElMessage.error(msg)
  } finally {
    refunding.value = false
  }
}

onMounted(() => {
  loadStats()
  loadData()
})
</script>

<style scoped>
.deposit-manage { padding: 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; color: #2d3436; }

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 22px; font-weight: 700; color: #2d3436; }
.stat-label { font-size: 13px; color: #888; margin-top: 2px; }

.search-bar {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
}
</style>