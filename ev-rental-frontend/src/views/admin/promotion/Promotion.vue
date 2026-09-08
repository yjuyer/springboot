<template>
  <div class="admin-promotion">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>促销活动管理</span>
          <el-button type="primary" @click="openCreate">新建活动</el-button>
        </div>
      </template>

      <el-table :data="activities" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="activityName" label="活动名称" width="180" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ typeText(row.activityType) }}</template>
        </el-table-column>
        <el-table-column label="规则" width="180">
          <template #default="{ row }">
            <el-tag size="small">{{ rulePreview(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="220">
          <template #default="{ row }">{{ row.startTime }} ~ {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column label="叠加" width="80">
          <template #default="{ row }">
            <el-tag :type="row.stackable ? 'success' : 'info'" size="small">{{ row.stackable ? '可' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="toggleActivity(row)" />
          </template>
        </el-table-column>
        <el-table-column label="已参与" width="80">
          <template #default="{ row }">{{ row.usedCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除？" @confirm="deleteActivity(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @change="fetchActivities"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '新建活动'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="活动名称" prop="activityName">
          <el-input v-model="form.activityName" placeholder="如：暑期租车满减活动" />
        </el-form-item>
        <el-form-item label="活动类型" prop="activityType">
          <el-select v-model="form.activityType">
            <el-option :value="1" label="满减" />
            <el-option :value="2" label="折扣" />
            <el-option :value="3" label="立减" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则配置">
          <template v-if="form.activityType === 1 || form.activityType === 3">
            <el-input-number v-model="ruleMinAmount" :min="0" placeholder="最低消费" style="width: 160px" /> 元起 &nbsp;
            <el-input-number v-model="ruleDiscountAmount" :min="1" placeholder="优惠金额" style="width: 160px" /> 元
          </template>
          <template v-else>
            <el-input-number v-model="ruleMinAmount" :min="0" placeholder="最低消费" style="width: 160px" /> 元起 &nbsp;
            <el-input-number v-model="ruleDiscountRate" :min="0.1" :max="0.99" :step="0.01" :precision="2" placeholder="折扣率" style="width: 160px" />
            <span style="margin-left: 8px; color: #999">（0.85=85折）</span>
          </template>
        </el-form-item>
        <el-form-item label="活动时间" required>
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="每人限参与次数">
          <el-input-number v-model="form.userLimit" :min="0" /> <span style="margin-left: 8px; color: #999">0=不限</span>
        </el-form-item>
        <el-form-item label="是否可叠加优惠券">
          <el-switch v-model="form.stackable" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item v-if="form.stackable" label="叠加上限(元)">
          <el-input-number v-model="form.maxDiscount" :min="0" :precision="2" /> <span style="margin-left: 8px; color: #999">0=不限制</span>
        </el-form-item>
        <el-form-item label="活动说明">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="活动说明（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { promotionApi } from '@/api/vehicle'

const activities = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()

const timeRange = ref([])
const ruleMinAmount = ref(0)
const ruleDiscountAmount = ref(50)
const ruleDiscountRate = ref(0.85)

const form = reactive({
  id: null,
  activityName: '',
  activityType: 1,
  ruleConfig: '',
  userLimit: 0,
  stackable: 0,
  maxDiscount: 0,
  description: '',
  status: 1
})

const rules = {
  activityName: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择活动类型', trigger: 'change' }]
}

const typeText = (t) => ({ 1: '满减', 2: '折扣', 3: '立减' }[t])

const rulePreview = (row) => {
  try {
    const r = JSON.parse(row.ruleConfig)
    if (row.activityType === 2) return `${r.minAmount}元起 ${(r.discountRate * 100).toFixed(0)}折`
    return `${r.minAmount}元减${r.discountAmount}元`
  } catch { return '-' }
}

const buildRuleConfig = () => {
  if (form.activityType === 2) {
    form.ruleConfig = JSON.stringify({ minAmount: ruleMinAmount.value, discountRate: ruleDiscountRate.value })
  } else {
    form.ruleConfig = JSON.stringify({ minAmount: ruleMinAmount.value, discountAmount: ruleDiscountAmount.value })
  }
}

const fetchActivities = async () => {
  loading.value = true
  try {
    const res = await promotionApi.list({ page: page.value, size: size.value })
    activities.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  isEdit.value = false
  Object.assign(form, { id: null, activityName: '', activityType: 1, ruleConfig: '', userLimit: 0, stackable: 0, maxDiscount: 0, description: '', status: 1 })
  ruleMinAmount.value = 0
  ruleDiscountAmount.value = 50
  ruleDiscountRate.value = 0.85
  timeRange.value = []
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  try {
    const r = JSON.parse(row.ruleConfig)
    ruleMinAmount.value = r.minAmount || 0
    if (row.activityType === 2) {
      ruleDiscountRate.value = r.discountRate || 0.85
    } else {
      ruleDiscountAmount.value = r.discountAmount || 50
    }
  } catch {
    ruleMinAmount.value = 0
  }
  timeRange.value = [row.startTime, row.endTime]
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (!timeRange.value || timeRange.value.length !== 2) {
    ElMessage.warning('请选择活动时间')
    return
  }

  buildRuleConfig()
  form.startTime = timeRange.value[0]
  form.endTime = timeRange.value[1]

  submitting.value = true
  try {
    if (isEdit.value) {
      await promotionApi.update(form)
    } else {
      await promotionApi.create(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchActivities()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const toggleActivity = async (row) => {
  try {
    await promotionApi.toggle(row.id)
    ElMessage.success(row.status === 1 ? '已禁用' : '已启用')
    fetchActivities()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const deleteActivity = async (id) => {
  try {
    await promotionApi.delete(id)
    ElMessage.success('删除成功')
    fetchActivities()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(fetchActivities)
</script>

<style scoped>
.admin-promotion { margin: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
</style>
