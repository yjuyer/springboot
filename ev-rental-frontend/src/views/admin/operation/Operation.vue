<template>
  <div class="operation-page">
    <div class="page-header"><h2>调度 / 维修 / 充电管理</h2></div>

    <div class="stat-cards">
      <div class="stat-card"><span>{{ stats.dispatching || 0 }}</span><em>调度中</em></div>
      <div class="stat-card"><span>{{ stats.repairing || 0 }}</span><em>维修中</em></div>
      <div class="stat-card"><span>{{ stats.charging || 0 }}</span><em>充电中</em></div>
      <div class="stat-card danger"><span>{{ stats.lowBattery || 0 }}</span><em>低电量</em></div>
    </div>

    <div class="ev-card">
      <el-tabs v-model="activeTab" @tab-change="loadCurrent">
        <el-tab-pane label="车辆调度" name="dispatch">
          <div class="toolbar"><el-button type="primary" @click="openDispatch">新增调度</el-button></div>
          <el-table :data="dispatchList" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="vehicleModel" label="车辆" />
            <el-table-column prop="fromStoreName" label="原门店" />
            <el-table-column prop="toStoreName" label="目标门店" />
            <el-table-column prop="reason" label="原因" show-overflow-tooltip />
            <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag>{{ dispatchStatus(row.status) }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="120"><template #default="{ row }"><el-button v-if="row.status === 1" link type="success" @click="completeDispatch(row.id)">完成</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="维修保养" name="repair">
          <div class="toolbar"><el-button type="primary" @click="openRepair">新增维修</el-button></div>
          <el-table :data="repairList" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="vehicleModel" label="车辆" />
            <el-table-column prop="repairType" label="类型" width="120" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="cost" label="费用" width="100" />
            <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag>{{ repairStatus(row.status) }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="120"><template #default="{ row }"><el-button v-if="row.status !== 2" link type="success" @click="completeRepair(row)">完成</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="充电管理" name="charging">
          <div class="toolbar"><el-button type="primary" @click="openCharging">开始充电</el-button></div>
          <el-table :data="chargingList" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="vehicleModel" label="车辆" />
            <el-table-column prop="stationName" label="充电站" />
            <el-table-column label="电量" width="150"><template #default="{ row }">{{ row.startBattery }}% → {{ row.endBattery ?? '-' }}%</template></el-table-column>
            <el-table-column prop="cost" label="费用" width="100" />
            <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag>{{ row.status === 1 ? '已完成' : '充电中' }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="120"><template #default="{ row }"><el-button v-if="row.status === 0" link type="success" @click="completeCharging(row)">完成</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="dispatchVisible" title="新增车辆调度" width="460px">
      <el-form label-width="90px">
        <el-form-item label="车辆ID"><el-input-number v-model="dispatchForm.vehicleId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="目标门店ID"><el-input-number v-model="dispatchForm.toStoreId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="dispatchForm.reason" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dispatchVisible=false">取消</el-button><el-button type="primary" @click="submitDispatch">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="repairVisible" title="新增维修记录" width="460px">
      <el-form label-width="90px">
        <el-form-item label="车辆ID"><el-input-number v-model="repairForm.vehicleId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="维修类型"><el-input v-model="repairForm.repairType" placeholder="保养/故障维修/事故维修" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="repairForm.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="repairVisible=false">取消</el-button><el-button type="primary" @click="submitRepair">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="chargingVisible" title="开始充电" width="460px">
      <el-form label-width="90px">
        <el-form-item label="车辆ID"><el-input-number v-model="chargingForm.vehicleId" :min="1" style="width:100%" /></el-form-item>
        <el-form-item label="充电站"><el-input v-model="chargingForm.stationName" /></el-form-item>
        <el-form-item label="起始电量"><el-input-number v-model="chargingForm.startBattery" :min="0" :max="100" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="chargingVisible=false">取消</el-button><el-button type="primary" @click="submitCharging">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { adminApi } from '@/api/vehicle'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('dispatch')
const loading = ref(false)
const stats = ref({})
const dispatchList = ref([])
const repairList = ref([])
const chargingList = ref([])
const dispatchVisible = ref(false)
const repairVisible = ref(false)
const chargingVisible = ref(false)
const dispatchForm = reactive({ vehicleId: null, toStoreId: null, reason: '' })
const repairForm = reactive({ vehicleId: null, repairType: '', description: '' })
const chargingForm = reactive({ vehicleId: null, stationName: '', startBattery: null })

onMounted(async () => { await loadStats(); await loadCurrent() })
async function loadStats() { stats.value = (await adminApi.getOperationStats()).data || {} }
async function loadCurrent() {
  loading.value = true
  try {
    if (activeTab.value === 'dispatch') dispatchList.value = (await adminApi.getDispatchList({ pageSize: 50 })).data.records || []
    if (activeTab.value === 'repair') repairList.value = (await adminApi.getRepairList({ pageSize: 50 })).data.records || []
    if (activeTab.value === 'charging') chargingList.value = (await adminApi.getChargingList({ pageSize: 50 })).data.records || []
  } finally { loading.value = false }
}
function openDispatch() { Object.assign(dispatchForm, { vehicleId: null, toStoreId: null, reason: '' }); dispatchVisible.value = true }
function openRepair() { Object.assign(repairForm, { vehicleId: null, repairType: '', description: '' }); repairVisible.value = true }
function openCharging() { Object.assign(chargingForm, { vehicleId: null, stationName: '', startBattery: null }); chargingVisible.value = true }
async function submitDispatch() { await adminApi.createDispatch(dispatchForm); ElMessage.success('调度已创建'); dispatchVisible.value = false; await loadStats(); loadCurrent() }
async function completeDispatch(id) { await adminApi.completeDispatch(id); ElMessage.success('调度已完成'); await loadStats(); loadCurrent() }
async function submitRepair() { await adminApi.createRepair(repairForm); ElMessage.success('维修已创建'); repairVisible.value = false; await loadStats(); loadCurrent() }
async function completeRepair(row) {
  const { value } = await ElMessageBox.prompt('请输入维修费用', '完成维修', { inputValue: row.cost || 0 })
  await adminApi.completeRepair(row.id, { cost: Number(value || 0), remark: '维修完成' })
  ElMessage.success('维修已完成'); await loadStats(); loadCurrent()
}
async function submitCharging() { await adminApi.startCharging(chargingForm); ElMessage.success('充电已开始'); chargingVisible.value = false; await loadStats(); loadCurrent() }
async function completeCharging(row) {
  const { value } = await ElMessageBox.prompt('请输入结束电量', '完成充电', { inputValue: 100 })
  await adminApi.completeCharging(row.id, { endBattery: Number(value || 100), cost: row.cost || 0 })
  ElMessage.success('充电已完成'); await loadStats(); loadCurrent()
}
function dispatchStatus(s) { return ['待调度', '调度中', '已完成', '已取消'][s] || '未知' }
function repairStatus(s) { return ['待维修', '维修中', '已完成'][s] || '未知' }
</script>

<style scoped>
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; }
.page-header h2 { font-size:24px; }
.stat-cards { display:grid; grid-template-columns:repeat(4,1fr); gap:16px; margin-bottom:18px; }
.stat-card { background:#fff; border-radius:12px; padding:18px; box-shadow:0 2px 10px rgba(0,0,0,.05); }
.stat-card span { display:block; font-size:28px; font-weight:700; color:#38b48b; }
.stat-card em { font-style:normal; color:#888; font-size:13px; }
.stat-card.danger span { color:#e17055; }
.toolbar { margin-bottom:12px; }
</style>
