<template>
  <div class="invoice-list">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>我的发票</span>
          <el-button type="primary" @click="$router.push('/invoice/apply')">申请发票</el-button>
        </div>
      </template>

      <el-table :data="invoices" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="invoiceTitle" label="发票抬头" />
        <el-table-column label="发票类型" width="140">
          <template #default="{ row }">
            {{ row.invoiceType === 1 ? '普通发票' : '专用发票' }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button text type="primary" @click="downloadInvoice(row)">下载</el-button>
            <el-button v-if="row.status === 3" text type="warning" @click="showRejectReason(row)">查看原因</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @change="fetchInvoices"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { invoiceApi } from '@/api/vehicle'

const invoices = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const statusType = (s) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }[s])
const statusText = (s) => ({ 0: '待审核', 1: '已开具', 2: '已发送', 3: '已驳回' }[s])

const fetchInvoices = async () => {
  loading.value = true
  try {
    const res = await invoiceApi.myInvoices({ page: page.value, size: size.value })
    invoices.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载发票列表失败')
  } finally {
    loading.value = false
  }
}

const downloadInvoice = (row) => {
  if (row.invoiceFileUrl) {
    window.open(row.invoiceFileUrl)
  } else {
    ElMessage.warning('发票文件尚未生成')
  }
}

const showRejectReason = (row) => {
  ElMessageBox.alert(row.rejectReason || '无', '驳回原因')
}

onMounted(fetchInvoices)
</script>

<style scoped>
.invoice-list { max-width: 1000px; margin: 20px auto; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
</style>
