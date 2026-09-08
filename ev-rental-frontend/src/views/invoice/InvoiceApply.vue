<template>
  <div class="invoice-apply">
    <el-card class="apply-card">
      <template #header>
        <span>申请电子发票</span>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="订单编号" prop="orderId">
          <el-select v-model="form.orderId" placeholder="请选择已完成订单" filterable>
            <el-option
              v-for="order in completedOrders"
              :key="order.id"
              :label="`${order.orderNo} - ¥${order.totalAmount}`"
              :value="order.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="发票类型" prop="invoiceType">
          <el-radio-group v-model="form.invoiceType">
            <el-radio :value="1">增值税普通发票</el-radio>
            <el-radio :value="2">增值税专用发票</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="发票抬头" prop="invoiceTitle">
          <el-input v-model="form.invoiceTitle" placeholder="请输入发票抬头" />
        </el-form-item>

        <el-form-item v-if="form.invoiceType === 2" label="税号" prop="taxNumber">
          <el-input v-model="form.taxNumber" placeholder="请输入统一社会信用代码" />
        </el-form-item>

        <el-form-item label="接收邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入接收发票的邮箱" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交申请</el-button>
          <el-button @click="$router.push('/invoices')">查看我的发票</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { invoiceApi, orderApi } from '@/api/vehicle'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const completedOrders = ref([])

const form = reactive({
  orderId: null,
  invoiceType: 1,
  invoiceTitle: '',
  taxNumber: '',
  email: ''
})

const rules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  invoiceTitle: [{ required: true, message: '请输入发票抬头', trigger: 'blur' }],
  taxNumber: [{ required: true, message: '企业发票必须填写税号', trigger: 'blur', when: (f) => f.invoiceType === 2 }],
  email: [
    { required: true, message: '请输入接收邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

onMounted(async () => {
  try {
    const res = await orderApi.my({ params: { status: 5 } })
    completedOrders.value = res.data?.records || []
  } catch (e) {
    console.error('获取已完成订单失败', e)
  }
})

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await invoiceApi.apply({
      orderId: form.orderId,
      invoiceType: form.invoiceType,
      invoiceTitle: form.invoiceTitle,
      taxNumber: form.taxNumber,
      email: form.email
    })
    ElMessage.success('发票申请已提交')
    router.push('/invoices')
  } catch (e) {
    ElMessage.error(e.message || '申请失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.invoice-apply {
  max-width: 700px;
  margin: 20px auto;
}
.apply-card {
  border-radius: 8px;
}
</style>
