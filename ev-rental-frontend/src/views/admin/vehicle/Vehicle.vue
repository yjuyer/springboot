<template>
  <div class="vehicle-manage">
    <div class="page-header">
      <h2>车辆管理</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 添加车辆
      </el-button>
    </div>

    <div class="ev-card search-bar">
      <el-input v-model="query.keyword" placeholder="搜索车型/车牌号" clearable @change="loadData"
                style="width:200px" prefix-icon="Search" />
      <el-select v-model="query.brandId" placeholder="品牌" @change="loadData" style="width:140px">
        <el-option label="全部" :value="null" />
        <el-option label="比亚迪" :value="1" />
        <el-option label="特斯拉" :value="2" />
        <el-option label="蔚来" :value="3" />
        <el-option label="小鹏" :value="4" />
        <el-option label="理想" :value="5" />
      </el-select>
      <el-select v-model="query.vehicleStatus" placeholder="状态" @change="loadData" style="width:120px">
        <el-option label="全部" :value="null" />
        <el-option label="可租" :value="0" />
        <el-option label="已预约" :value="1" />
        <el-option label="租赁中" :value="2" />
        <el-option label="维修中" :value="3" />
        <el-option label="充电中" :value="4" />
      </el-select>
    </div>

    <div class="ev-card">
      <el-table :data="vehicles" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="车辆" min-width="180">
          <template #default="{ row }">
            <div class="vehicle-cell">
              <el-image :src="getCoverImage(row)" class="cell-img" fit="cover" lazy>
                <template #error><div class="cell-img-placeholder"><el-icon><PictureFilled /></el-icon></div></template>
              </el-image>
              <div>
                <div class="name">{{ row.model }}</div>
                <div class="plate">{{ row.licensePlate }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">{{ typeText(row.vehicleType) }}</template>
        </el-table-column>
        <el-table-column label="日租" width="100">
          <template #default="{ row }"><span class="price">{{ row.dailyPrice }}</span></template>
        </el-table-column>
        <el-table-column label="续航" width="90">
          <template #default="{ row }">{{ row.rangeKm }}km</template>
        </el-table-column>
        <el-table-column label="电量" width="140">
          <template #default="{ row }">
            <el-progress :percentage="getBatteryPercent(row)" :stroke-width="8"
                         :color="batteryColor(getBatteryPercent(row))" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.vehicleStatus)" size="small">{{ statusText(row.vehicleStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openImageDialog(row)">图片</el-button>
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该车辆吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="loadData"
        />
      </div>
    </div>

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑车辆' : '添加车辆'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="品牌ID" prop="brandId">
          <el-input v-model="form.brandId" placeholder="如：1" />
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="form.model" placeholder="如：汉EV" />
        </el-form-item>
        <el-form-item label="车牌号" prop="licensePlate">
          <el-input v-model="form.licensePlate" placeholder="如：京A·NE0001" />
        </el-form-item>
        <el-form-item label="车辆类型">
          <el-select v-model="form.vehicleType" style="width:100%">
            <el-option label="轿车" :value="1" /><el-option label="SUV" :value="2" />
            <el-option label="MPV" :value="3" /><el-option label="跑车" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="颜色">
          <el-input v-model="form.color" />
        </el-form-item>
        <el-form-item label="座位数">
          <el-input-number v-model="form.seatCount" :min="2" :max="9" />
        </el-form-item>
        <el-form-item label="电池容量(kWh)">
          <el-input-number v-model="form.batteryCapacity" :min="10" :max="200" :precision="1" />
        </el-form-item>
        <el-form-item label="续航(km)">
          <el-input-number v-model="form.rangeKm" :min="100" :max="2000" />
        </el-form-item>
        <el-form-item label="快充时间(h)">
          <el-input-number v-model="form.fastCharge" :min="0.1" :max="10" :precision="1" />
        </el-form-item>
        <el-form-item label="日租金(元)" prop="dailyPrice">
          <el-input-number v-model="form.dailyPrice" :min="50" :max="2000" />
        </el-form-item>
        <el-form-item label="押金(元)">
          <el-input-number v-model="form.deposit" :min="1000" :max="20000" />
        </el-form-item>
        <el-form-item label="所属门店">
          <el-select v-model="form.storeId" placeholder="请选择门店" style="width:100%">
            <el-option v-for="s in stores" :key="s.id" :label="s.storeName || s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="主图上传">
          <div class="cover-upload">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onCoverUploadSuccess"
              :before-upload="beforeUpload"
              accept=".jpg,.jpeg,.png"
            >
              <div v-if="form.image" class="cover-preview">
                <el-image :src="resolveUrl(form.image)" fit="cover" style="width:200px;height:120px;border-radius:8px" />
                <div class="cover-mask"><el-icon><Edit /></el-icon> 更换主图</div>
              </div>
              <div v-else class="upload-trigger">
                <el-icon :size="32"><Plus /></el-icon>
                <span>上传主图</span>
              </div>
            </el-upload>
          </div>
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

    <!-- 图片管理弹窗 -->
    <el-dialog v-model="imageDialogVisible" title="图片管理" width="760px" destroy-on-close>
      <div class="image-manage">
        <div class="image-upload-row">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :file-list="uploadFileList"
            :on-success="onImageUploadSuccess"
            :before-upload="beforeUpload"
            :on-remove="onImageRemove"
            accept=".jpg,.jpeg,.png"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <el-alert title="拖拽图片可调整排序，点击图片可预览" type="info" :closable="false" show-icon style="margin-top:12px" />
        </div>

        <div class="image-sort-list" v-if="currentImages.length > 0">
          <div
            v-for="(img, idx) in currentImages"
            :key="img.id || idx"
            class="image-sort-item"
            draggable="true"
            @dragstart="onDragStart(idx)"
            @dragover.prevent="onDragOver(idx)"
            @drop="onDrop(idx)"
          >
            <div class="sort-handle">
              <el-icon><Rank /></el-icon>
              <span class="sort-num">{{ idx + 1 }}</span>
            </div>
            <el-image
              :src="resolveUrl(img.imageUrl)"
              fit="cover"
              class="sort-img"
              :preview-src-list="currentImages.map(i => resolveUrl(i.imageUrl))"
              :initial-index="idx"
              preview-teleported
            />
            <el-button type="danger" link size="small" @click="removeImage(idx)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
        <el-empty v-else description="暂无图片，请上传" :image-size="80" />
      </div>
      <template #footer>
        <el-button @click="imageDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingImages" @click="saveImages">保存图片</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { adminApi, uploadApi } from '@/api/vehicle'
import { ElMessage } from 'element-plus'
import { PictureFilled, Plus, Edit, Rank, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const vehicles = ref([])
const total = ref(0)
const stores = ref([])
const query = reactive({ keyword: '', brandId: null, vehicleStatus: null, pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const form = reactive({
  id: null, brandId: null, model: '', licensePlate: '', vehicleType: 1,
  color: '', seatCount: 5, batteryCapacity: null, rangeKm: null,
  fastCharge: null, dailyPrice: null, deposit: null, storeId: null,
  image: '', description: ''
})

const rules = {
  brandId: [{ required: true, message: '请输入品牌ID', trigger: 'blur' }],
  model: [{ required: true, message: '请输入型号', trigger: 'blur' }],
  licensePlate: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  dailyPrice: [{ required: true, message: '请输入日租金', trigger: 'change' }]
}

const imageDialogVisible = ref(false)
const currentVehicleId = ref(null)
const currentImages = ref([])
const savingImages = ref(false)
const dragIdx = ref(-1)
const uploadFileList = ref([])

const uploadUrl = '/api/image/upload'
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token') || ''
  return { Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}` }
})

onMounted(async () => {
  loadData()
  try {
    const res = await adminApi.getVehicleList({ pageSize: 100 })
    stores.value = res.data.records || []
  } catch (e) {}
})

async function loadData() {
  loading.value = true
  try {
    const res = await adminApi.getVehicleList(query)
    vehicles.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

function getCoverImage(v) {
  const img = v.image || v.mainImageUrl
  if (!img) return ''
  if (img.startsWith('http')) return img
  return img
}

function resolveUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url
}

function getBatteryPercent(row) {
  if (row.currentBattery) return parseFloat(row.currentBattery) || 0
  return 0
}

function openDialog(row) {
  isEdit.value = !!row
  if (row) {
    Object.keys(form).forEach(k => { form[k] = row[k] !== undefined ? row[k] : form[k] })
  } else {
    resetForm()
  }
  dialogVisible.value = true
}

function resetForm() {
  form.id = null
  form.brandId = null
  form.model = ''
  form.licensePlate = ''
  form.vehicleType = 1
  form.color = ''
  form.seatCount = 5
  form.batteryCapacity = null
  form.rangeKm = null
  form.fastCharge = null
  form.dailyPrice = null
  form.deposit = null
  form.storeId = null
  form.image = ''
  form.description = ''
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = { ...form }
    data.vehicleType = Number(data.vehicleType)
    if (isEdit.value) {
      await adminApi.updateVehicle(data)
    } else {
      await adminApi.addVehicle(data)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally { submitting.value = false }
}

async function handleDelete(id) {
  await adminApi.deleteVehicle(id)
  ElMessage.success('已删除')
  loadData()
}

function onCoverUploadSuccess(res) {
  if (res.code === 200) {
    form.image = res.data
    ElMessage.success('主图上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error('仅支持 jpg、png、jpeg 格式'); return false }
  if (!isLt5M) { ElMessage.error('图片大小不能超过5MB'); return false }
  return true
}

async function openImageDialog(row) {
  currentVehicleId.value = row.id
  uploadFileList.value = []
  try {
    const res = await adminApi.getVehicleImages(row.id)
    currentImages.value = (res.data || []).map(img => ({
      id: img.id,
      imageUrl: img.imageUrl,
      sortNum: img.sortNum
    }))
  } catch (e) {
    currentImages.value = []
  }
  imageDialogVisible.value = true
}

function onImageUploadSuccess(res, file) {
  if (res.code === 200) {
    currentImages.value.push({
      id: null,
      imageUrl: res.data,
      sortNum: currentImages.value.length + 1
    })
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function onImageRemove(file, fileList) {
  const url = file.response?.data || file.url
  currentImages.value = currentImages.value.filter(img => img.imageUrl !== url)
}

function removeImage(idx) {
  currentImages.value.splice(idx, 1)
  currentImages.value.forEach((img, i) => { img.sortNum = i + 1 })
}

function onDragStart(idx) { dragIdx.value = idx }
function onDragOver(idx) {}
function onDrop(idx) {
  if (dragIdx.value === idx || dragIdx.value < 0) return
  const item = currentImages.value.splice(dragIdx.value, 1)[0]
  currentImages.value.splice(idx, 0, item)
  currentImages.value.forEach((img, i) => { img.sortNum = i + 1 })
  dragIdx.value = -1
}

async function saveImages() {
  savingImages.value = true
  try {
    await adminApi.saveVehicleImages(currentVehicleId.value, currentImages.value.map(img => ({
      imageUrl: img.imageUrl,
      sortNum: img.sortNum
    })))
    ElMessage.success('图片保存成功')
    imageDialogVisible.value = false
  } catch (e) {
    ElMessage.error('保存失败')
  } finally { savingImages.value = false }
}

function statusText(s) { return ['可租','已预约','租赁中','维修中','充电中','调度中'][s] || '' }
function statusType(s) { return s === 0 ? 'success' : s === 2 ? 'warning' : 'info' }
function typeText(t) { return ['','轿车','SUV','MPV','跑车','皮卡'][t] || '' }
function batteryColor(v) { return v >= 60 ? '#38b48b' : v >= 30 ? '#fdcb6e' : '#e17055' }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { font-size: 24px; }

.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }

.vehicle-cell { display: flex; align-items: center; gap: 10px; }
.cell-img { width: 60px; height: 40px; border-radius: 6px; object-fit: cover; background: #f5f5f5; }
.cell-img-placeholder { width: 60px; height: 40px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 6px; }
.vehicle-cell .name { font-weight: 500; }
.vehicle-cell .plate { color: #999; font-size: 12px; }
.price { color: #ff6b6b; font-weight: 600; }
.price::before { content: '¥'; font-size: 12px; }

.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }

.cover-upload { display: flex; align-items: center; }
.cover-preview { position: relative; cursor: pointer; border-radius: 8px; overflow: hidden; }
.cover-mask {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.5);
  color: #fff;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.3s; font-size: 13px; gap: 6px;
}
.cover-preview:hover .cover-mask { opacity: 1; }
.upload-trigger {
  width: 200px; height: 120px;
  border: 2px dashed #ddd; border-radius: 8px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #999; cursor: pointer; transition: border-color 0.3s;
}
.upload-trigger:hover { border-color: #38b48b; color: #38b48b; }

.image-manage { min-height: 200px; }
.image-sort-list { display: flex; flex-wrap: wrap; gap: 16px; margin-top: 20px; }
.image-sort-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px; background: #f8f9fa; border-radius: 10px;
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: grab;
}
.image-sort-item:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.sort-handle { display: flex; flex-direction: column; align-items: center; gap: 4px; color: #888; }
.sort-num { font-size: 12px; font-weight: 600; }
.sort-img { width: 120px; height: 80px; border-radius: 8px; cursor: pointer; }
</style>