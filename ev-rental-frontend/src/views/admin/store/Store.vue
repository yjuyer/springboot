<template>
  <div class="store-manage">
    <div class="page-header">
      <h2>门店管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 添加门店
      </el-button>
    </div>

    <div class="ev-card">
      <el-table :data="stores" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="storeName" label="门店名称" min-width="160" />
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="managerName" label="负责人" width="100" />
        <el-table-column prop="totalParking" label="车位" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '营业' : '关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="loadData"
        />
      </div>
    </div>

    <!-- 弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑门店' : '添加门店'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="门店名称" prop="storeName">
          <el-input v-model="form.storeName" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="form.province" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" />
        </el-form-item>
        <el-form-item label="区县">
          <el-input v-model="form.district" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.managerName" />
        </el-form-item>
        <el-form-item label="车位数">
          <el-input-number v-model="form.totalParking" :min="1" :max="200" />
        </el-form-item>
        <el-form-item label="营业时间">
          <el-input v-model="form.businessHours" placeholder="如 08:00-22:00" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">营业</el-radio>
            <el-radio :value="0">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '@/api/vehicle'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const stores = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const form = reactive({
  id: null, storeName: '', province: '', city: '', district: '',
  address: '', phone: '', managerName: '', totalParking: 30,
  businessHours: '08:00-22:00', status: 1, description: ''
})

const rules = {
  storeName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.getStoreList({ pageNum: pageNum.value, pageSize: pageSize.value })
    stores.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function openDialog(row) {
  isEdit.value = !!row
  if (row) {
    Object.keys(form).forEach(k => { form[k] = row[k] })
  } else {
    Object.assign(form, {
      id: null, storeName: '', province: '', city: '', district: '',
      address: '', phone: '', managerName: '', totalParking: 30,
      businessHours: '08:00-22:00', status: 1, description: ''
    })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await adminApi.updateStore(form)
    } else {
      await adminApi.addStore(form)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}

async function handleDelete(id) {
  await adminApi.deleteStore(id)
  ElMessage.success('已删除')
  loadData()
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 24px; }

.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
