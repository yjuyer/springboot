<template>
  <div class="review-admin">
    <div class="page-header">
      <h2>评价管理</h2>
      <el-select v-model="query.status" placeholder="审核状态" clearable style="width:140px" @change="loadData">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已驳回" :value="2" />
      </el-select>
    </div>

    <div class="ev-card">
      <el-table :data="reviews" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="vehicleModel" label="车辆" min-width="120" />
        <el-table-column label="用户" width="120">
          <template #default="{ row }">{{ row.anonymous === 1 ? '匿名用户' : row.username }}</template>
        </el-table-column>
        <el-table-column label="评分" width="150">
          <template #default="{ row }"><el-rate :model-value="row.rating" disabled /></template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="tags" label="标签" width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link size="small" @click="openAudit(row, 1)">通过</el-button>
            <el-button type="warning" link size="small" @click="openAudit(row, 2)">驳回</el-button>
            <el-popconfirm title="确定删除该评价吗？" @confirm="handleDelete(row.id)">
              <template #reference><el-button type="danger" link size="small">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @change="loadData" />
      </div>
    </div>

    <el-dialog v-model="auditVisible" title="评价审核" width="460px">
      <el-form label-width="80px">
        <el-form-item label="审核结果"><el-tag :type="auditForm.status === 1 ? 'success' : 'danger'">{{ auditForm.status === 1 ? '通过' : '驳回' }}</el-tag></el-form-item>
        <el-form-item label="回复"><el-input v-model="auditForm.reply" type="textarea" :rows="4" placeholder="可填写管理员回复" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { adminApi } from '@/api/vehicle'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const reviews = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: null })
const auditVisible = ref(false)
const auditForm = reactive({ id: null, status: 1, reply: '' })

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.getReviewList(query)
    reviews.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}
function openAudit(row, status) {
  auditForm.id = row.id
  auditForm.status = status
  auditForm.reply = row.reply || ''
  auditVisible.value = true
}
async function handleAudit() {
  await adminApi.auditReview(auditForm.id, { status: auditForm.status, reply: auditForm.reply })
  ElMessage.success('审核成功')
  auditVisible.value = false
  loadData()
}
async function handleDelete(id) {
  await adminApi.deleteReview(id)
  ElMessage.success('已删除')
  loadData()
}
function statusText(s) { return ['待审核', '已通过', '已驳回'][s] || '未知' }
function statusType(s) { return s === 1 ? 'success' : s === 2 ? 'danger' : 'warning' }
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; }
.page-header h2 { font-size:24px; }
.pagination { display:flex; justify-content:flex-end; margin-top:16px; }
</style>
