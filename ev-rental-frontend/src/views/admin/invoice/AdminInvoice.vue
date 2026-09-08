<template>
  <div class="admin-invoice">
    <el-card>
      <template #header><span>发票管理</span></template>

      <el-tabs v-model="tab" @tab-change="fetchInvoices">
        <el-tab-pane label="待审核" name="0" />
        <el-tab-pane label="已开具" name="1" />
        <el-tab-pane label="已发送" name="2" />
        <el-tab-pane label="已驳回" name="3" />
      </el-tabs>

      <el-table :data="invoices" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="invoiceTitle" label="发票抬头" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ row.invoiceType === 1 ? '普通' : '专用' }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="100" />
        <el-table-column prop="applyTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button size="small" type="success" @click="openAudit(row)">开具</el-button>
              <el-button size="small" type="danger" @click="rejectOpen(row)">驳回</el-button>
            </template>
            <el-button v-if="row.status === 1" size="small" type="primary" @click="sendInvoice(row)">标记已发送</el-button>
            <el-button v-if="row.invoiceFileUrl" size="small" @click="window.open(row.invoiceFileUrl)">下载</el-button>
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

    <!-- 开具发票对话框 -->
    <el-dialog v-model="auditVisible" title="开具发票" width="450px">
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="发票号码" required>
          <el-input v-model="auditForm.invoiceNo" placeholder="请输入发票号码" />
        </el-form-item>
        <el-form-item label="发票文件" required>
          <el-input v-model="auditForm.invoiceFileUrl" placeholder="请输入发票文件URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="doAudit(true)">确认开具</el-button>
      </template>
    </el-dialog>

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectVisible" title="驳回发票申请" width="450px">
      <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="doAudit(false)">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { invoiceApi } from '@/api/vehicle'

const invoices = ref([])
const loading = ref(false)
const tab = ref('0')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const auditVisible = ref(false)
const rejectVisible = ref(false)
const currentRow = ref(null)
const rejectReason = ref('')

const auditForm = reactive({ invoiceNo: '', invoiceFileUrl: '' })

const fetchInvoices = async () => {
  loading.value = true
  try {
    const res = await invoiceApi.adminList({ page: page.value, size: size.value, status: Number(tab.value) })
    invoices.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载发票列表失败')
  } finally {
    loading.value = false
  }
}

const openAudit = (row) => {
  currentRow.value = row
  auditForm.invoiceNo = ''
  auditForm.invoiceFileUrl = ''
  auditVisible.value = true
}

const rejectOpen = (row) => {
  currentRow.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

const doAudit = async (approved) => {
  try {
    await invoiceApi.audit({
      invoiceId: currentRow.value.id,
      invoiceNo: auditForm.invoiceNo,
      invoiceFileUrl: auditForm.invoiceFileUrl,
      approved,
      rejectReason: approved ? null : rejectReason.value
    })
    ElMessage.success(approved ? '发票已开具' : '已驳回')
    auditVisible.value = false
    rejectVisible.value = false
    fetchInvoices()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

const sendInvoice = async (row) => {
  try {
    await ElMessageBox.confirm('确认发送该发票？', '确认操作')
    await invoiceApi.send(row.id)
    ElMessage.success('已标记为已发送')
    fetchInvoices()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(fetchInvoices)
</script>

<style scoped>
.admin-invoice { margin: 20px; }
</style>
