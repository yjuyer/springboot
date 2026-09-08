<!--
  后台管理 - 认证审核页面

  功能说明：
  1. 实名认证审核标签页
     - 查看提交实名认证的用户列表
     - 支持按审核状态筛选（待审核/已通过/已拒绝）
     - 通过/拒绝用户的实名认证
     - 删除用户的实名认证信息（用户需重新认证）
     - 身份证号脱敏显示

  2. 驾驶证审核标签页
     - 查看提交驾驶证的用户列表
     - 支持按审核状态筛选
     - 查看驾驶证图片
     - 通过/拒绝用户的驾驶证
     - 删除用户的驾驶证信息（用户需重新上传）

  审核状态说明：
  - 实名认证：0-待审核, 1-已通过, 2-已拒绝
  - 驾驶证：0-未上传, 1-待审核, 2-已通过, 3-已拒绝

  删除功能：
  - 删除实名认证：清除姓名、身份证号、图片，状态重置为0
  - 删除驾驶证：清除驾驶证图片，状态重置为0
-->
<template>
  <div class="verify-page">
    <h2 class="page-title">认证审核</h2>

    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 实名认证审核 -->
      <el-tab-pane label="实名认证审核" name="idcard">
        <div class="filter-bar">
          <el-select v-model="idCardFilter" placeholder="审核状态" clearable @change="loadIdCardList">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </div>

        <el-table :data="idCardList" v-loading="loadingIdCard" border stripe>
          <el-table-column prop="id" label="用户ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="真实姓名" width="120" />
          <el-table-column prop="idCard" label="身份证号" width="180">
            <template #default="{ row }">
              {{ maskIdCard(row.idCard) }}
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column prop="idCardVerified" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.idCardVerified === 1" type="success">已通过</el-tag>
              <el-tag v-else-if="row.idCardVerified === 2" type="danger">已拒绝</el-tag>
              <el-tag v-else type="warning">待审核</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="170" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.idCardVerified !== 1 && row.idCardVerified !== 2"
                type="success"
                size="small"
                @click="handleApproveIdCard(row.id, 1)"
              >
                通过
              </el-button>
              <el-button
                v-if="row.idCardVerified !== 1 && row.idCardVerified !== 2"
                type="danger"
                size="small"
                @click="handleApproveIdCard(row.id, 2)"
              >
                拒绝
              </el-button>
              <el-button
                type="warning"
                size="small"
                @click="handleDeleteIdCard(row.id)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="idCardPage"
            v-model:page-size="idCardPageSize"
            :total="idCardTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadIdCardList"
            @current-change="loadIdCardList"
          />
        </div>
      </el-tab-pane>

      <!-- 驾驶证审核 -->
      <el-tab-pane label="驾驶证审核" name="license">
        <div class="filter-bar">
          <el-select v-model="licenseFilter" placeholder="审核状态" clearable @change="loadLicenseList">
            <el-option label="待审核" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已拒绝" :value="3" />
          </el-select>
        </div>

        <el-table :data="licenseList" v-loading="loadingLicense" border stripe>
          <el-table-column prop="id" label="用户ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="真实姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="驾驶证" width="120">
            <template #default="{ row }">
              <el-button
                v-if="row.driverLicense"
                type="primary"
                link
                @click="previewImage(row.driverLicense)"
              >
                查看图片
              </el-button>
              <span v-else class="no-file">未上传</span>
            </template>
          </el-table-column>
          <el-table-column prop="licenseVerified" label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.licenseVerified === 0" type="info">未上传</el-tag>
              <el-tag v-else-if="row.licenseVerified === 1" type="warning">待审核</el-tag>
              <el-tag v-else-if="row.licenseVerified === 2" type="success">已通过</el-tag>
              <el-tag v-else-if="row.licenseVerified === 3" type="danger">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="170" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.licenseVerified === 1"
                type="success"
                size="small"
                @click="handleApproveLicense(row.id, 2)"
              >
                通过
              </el-button>
              <el-button
                v-if="row.licenseVerified === 1"
                type="danger"
                size="small"
                @click="handleApproveLicense(row.id, 3)"
              >
                拒绝
              </el-button>
              <el-button
                v-if="row.licenseVerified !== 0"
                type="warning"
                size="small"
                @click="handleDeleteLicense(row.id)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="licensePage"
            v-model:page-size="licensePageSize"
            :total="licenseTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="loadLicenseList"
            @current-change="loadLicenseList"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="600px">
      <img :src="previewUrl" class="preview-image" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('idcard')

// 实名认证相关
const loadingIdCard = ref(false)
const idCardList = ref([])
const idCardPage = ref(1)
const idCardPageSize = ref(10)
const idCardTotal = ref(0)
const idCardFilter = ref(null)

// 驾驶证相关
const loadingLicense = ref(false)
const licenseList = ref([])
const licensePage = ref(1)
const licensePageSize = ref(10)
const licenseTotal = ref(0)
const licenseFilter = ref(null)

// 图片预览
const previewVisible = ref(false)
const previewUrl = ref('')

onMounted(() => {
  loadIdCardList()
})

// 切换标签页
function handleTabChange(tab) {
  if (tab === 'idcard') {
    loadIdCardList()
  } else {
    loadLicenseList()
  }
}

// 加载实名认证列表
async function loadIdCardList() {
  loadingIdCard.value = true
  try {
    const res = await request.get('/admin/user/verify/list', {
      params: {
        pageNum: idCardPage.value,
        pageSize: idCardPageSize.value,
        idCardVerified: idCardFilter.value
      }
    })
    idCardList.value = res.data.records || []
    idCardTotal.value = res.data.total || 0
  } catch (e) {
    console.error('加载实名认证列表失败', e)
  } finally {
    loadingIdCard.value = false
  }
}

// 加载驾驶证列表
async function loadLicenseList() {
  loadingLicense.value = true
  try {
    const res = await request.get('/admin/user/license/list', {
      params: {
        pageNum: licensePage.value,
        pageSize: licensePageSize.value,
        licenseVerified: licenseFilter.value
      }
    })
    licenseList.value = res.data.records || []
    licenseTotal.value = res.data.total || 0
  } catch (e) {
    console.error('加载驾驶证列表失败', e)
  } finally {
    loadingLicense.value = false
  }
}

// 审核实名认证
async function handleApproveIdCard(userId, status) {
  const action = status === 1 ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户的实名认证吗？`, '确认操作', {
      type: 'warning'
    })
    await request.put('/admin/user/verify/approve', { userId, status })
    ElMessage.success(`实名认证已${action}`)
    loadIdCardList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 审核驾驶证
async function handleApproveLicense(userId, status) {
  const action = status === 2 ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${action}该用户的驾驶证吗？`, '确认操作', {
      type: 'warning'
    })
    await request.put('/admin/user/license/approve', { userId, status })
    ElMessage.success(`驾驶证已${action}`)
    loadLicenseList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除用户实名认证
async function handleDeleteIdCard(userId) {
  try {
    await ElMessageBox.confirm('确定要删除该用户的实名认证信息吗？删除后用户需要重新认证。', '确认删除', {
      type: 'warning'
    })
    await request.delete(`/admin/user/verify/delete/${userId}`)
    ElMessage.success('实名认证已删除，用户需重新认证')
    loadIdCardList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除用户驾驶证认证
async function handleDeleteLicense(userId) {
  try {
    await ElMessageBox.confirm('确定要删除该用户的驾驶证信息吗？删除后用户需要重新上传。', '确认删除', {
      type: 'warning'
    })
    await request.delete(`/admin/user/license/delete/${userId}`)
    ElMessage.success('驾驶证已删除，用户需重新上传')
    loadLicenseList()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 预览图片
function previewImage(url) {
  previewUrl.value = url
  previewVisible.value = true
}

// 脱敏身份证号
function maskIdCard(idCard) {
  if (!idCard) return '-'
  return idCard.replace(/(\d{3})\d{11}(\d{4})/, '$1***********$2')
}
</script>

<style scoped>
.verify-page {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 20px;
}

.filter-bar {
  margin-bottom: 16px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.reviewed {
  color: #999;
  font-size: 13px;
}

.no-file {
  color: #ccc;
  font-size: 13px;
}

.preview-image {
  width: 100%;
  max-height: 500px;
  object-fit: contain;
}
</style>
