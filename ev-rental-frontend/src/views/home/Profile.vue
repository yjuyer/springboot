<template>
  <div class="user-center">

    <!-- 左侧菜单 -->
    <div class="sidebar">
      <div class="logo">
        <h2>e租出行</h2>
      </div>

      <el-menu
        :default-active="activeMenu"
        class="menu"
        @select="onMenuSelect"
      >
        <el-menu-item index="profile">
          <el-icon><User /></el-icon>
          <span>个人中心</span>
        </el-menu-item>

        <el-menu-item index="coupon">
          <el-icon><Ticket /></el-icon>
          <span>优惠券</span>
        </el-menu-item>

        <el-menu-item index="member">
          <el-icon><Medal /></el-icon>
          <span>会员中心</span>
        </el-menu-item>

        <el-menu-item index="wallet">
          <el-icon><Wallet /></el-icon>
          <span>我的钱包</span>
        </el-menu-item>

        <el-menu-item index="credit">
          <el-icon><Star /></el-icon>
          <span>信誉积分</span>
        </el-menu-item>

        <el-menu-item index="favorite">
          <el-icon><Collection /></el-icon>
          <span>我的收藏</span>
        </el-menu-item>

        <el-menu-item index="order">
          <el-icon><Tickets /></el-icon>
          <span>我的订单</span>
        </el-menu-item>

        <el-menu-item index="message">
          <el-icon><Bell /></el-icon>
          <span>消息中心</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧内容 -->
    <div class="main-content">

      <!-- 顶部信息 -->
      <div class="top-card">

        <div class="user-info">

          <div class="avatar-box">
            <el-upload
              class="avatar-uploader"
              action="/api/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="onAvatarSuccess"
            >
              <img
                v-if="userInfo.avatar"
                class="avatar"
                :src="userInfo.avatar"
              />
              <img
                v-else
                class="avatar"
                src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"
              />
            </el-upload>
          </div>

          <div class="info-text">
            <div class="name-row">
              <h1>{{ userInfo.username || '未登录' }}</h1>

              <el-tag type="primary">
                {{ roleText }}
              </el-tag>

              <el-tag type="warning">
                {{ memberLevel }}
              </el-tag>
            </div>

            <p class="desc">
              绿色出行，优选新能源，开启美好旅程！
            </p>

            <div class="bottom-info">
              <span>📞 {{ maskPhone(userInfo.phone) }}</span>
              <span>📧 {{ userInfo.email || '未绑定' }}</span>
              <span>📅 注册时间：{{ formatDate(userInfo.createTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 信用积分 -->
        <div class="credit-card">

          <h3>信用积分</h3>

          <el-progress
            type="dashboard"
            :percentage="userInfo.credit || 100"
            :color="creditColor"
          />

          <div class="credit-score">
            {{ creditLevel }}
          </div>

          <p class="credit-desc">
            保持良好信用，享受更多租车优惠
          </p>

        </div>

      </div>

      <!-- 数据统计 -->
      <div class="stat-grid">

        <div class="stat-card">
          <h2>{{ stats.totalOrders }}</h2>
          <p>总订单</p>
        </div>

        <div class="stat-card">
          <h2>{{ stats.rentingOrders }}</h2>
          <p>租赁中</p>
        </div>

        <div class="stat-card">
          <h2>{{ stats.completedOrders }}</h2>
          <p>已完成</p>
        </div>

        <div class="stat-card">
          <h2>￥{{ stats.totalSpent }}</h2>
          <p>累计消费</p>
        </div>

      </div>

      <!-- 认证区域 -->
      <div class="auth-grid">

        <!-- 实名认证 -->
        <div class="auth-card">

          <div class="card-header">
            <h3>实名认证</h3>

            <el-tag :type="idCardTagType">
              {{ idCardText }}
            </el-tag>
          </div>

          <div class="auth-content">

            <div class="auth-icon">
              🪪
            </div>

            <div class="auth-info">
              <p>真实姓名：{{ userInfo.realName || '未填写' }}</p>
              <p>身份证号：{{ maskIdCard(userInfo.idCard) }}</p>
              <p>认证状态：{{ idCardText }}</p>
            </div>

            <!-- 已认证：显示查看详情（不可更改） -->
            <el-button
              v-if="userInfo.idCardVerified === 1"
              class="btn-white-blue"
              @click="showIdCardDialog = true"
            >
              查看详情
            </el-button>

            <!-- 未认证：去认证 -->
            <el-button
              v-else
              class="btn-white-blue"
              @click="$router.push('/verify')"
            >
              去认证
            </el-button>

          </div>

        </div>

        <!-- 驾驶证 -->
        <div class="auth-card">

          <div class="card-header">
            <h3>驾驶证认证</h3>

            <el-tag :type="licenseTagType">
              {{ licenseText }}
            </el-tag>
          </div>

          <div class="auth-content">

            <div class="auth-icon">
              🚗
            </div>

            <div class="auth-info">
              <p>驾驶证号：{{ userInfo.driverLicense ? '已上传' : '未上传' }}</p>
              <p>准驾车型：C1</p>
              <p>认证状态：{{ licenseText }}</p>
            </div>

            <!-- 未上传：显示"去上传" -->
            <el-button
              v-if="userInfo.licenseVerified === 0 || !userInfo.driverLicense"
              class="btn-white-blue"
              @click="$router.push('/license')"
            >
              去上传
            </el-button>

            <!-- 待审核：显示"待审核"（禁用状态） -->
            <el-button
              v-else-if="userInfo.licenseVerified === 1"
              class="btn-white-blue"
              disabled
            >
              待审核
            </el-button>

            <!-- 已通过：显示"去更改" -->
            <el-button
              v-else-if="userInfo.licenseVerified === 2"
              class="btn-white-blue"
              @click="handleChangeLicense"
            >
              去更改
            </el-button>

            <!-- 已拒绝：显示"重新上传" -->
            <el-button
              v-else-if="userInfo.licenseVerified === 3"
              class="btn-white-blue"
              @click="$router.push('/license')"
            >
              重新上传
            </el-button>

          </div>

        </div>

      </div>

      <!-- 下方 -->
      <div class="bottom-grid">

        <!-- 个人信息 -->
        <div class="info-card">

          <div class="card-header">
            <h3>个人信息</h3>

            <el-button class="btn-white-blue" size="small" @click="openEdit">
              编辑
            </el-button>
          </div>

          <div class="info-list">

            <div class="info-item">
              <span>手机号</span>
              <span>{{ maskPhone(userInfo.phone) }}</span>
            </div>

            <div class="info-item">
              <span>邮箱</span>
              <span>{{ userInfo.email || '未绑定' }}</span>
            </div>

            <div class="info-item">
              <span>所在城市</span>
              <span>{{ userInfo.city || '未填写' }}</span>
            </div>

            <div class="info-item">
              <span>常用地址</span>
              <span>{{ userInfo.address || '未填写' }}</span>
            </div>

          </div>

        </div>

        <!-- 最近订单 -->
        <div class="order-card">

          <div class="card-header">
            <h3>最近订单</h3>
          </div>

          <div v-if="recentOrders.length === 0" style="text-align:center;padding:30px 0;color:#ccc;">
            暂无订单
          </div>

          <div v-for="order in recentOrders" :key="order.id" class="order-item">

            <img
              class="car-img"
              :src="order.mainImageUrl || '/images/default-car.png'"
              @error="e => e.target.style.display='none'"
            />

            <div class="order-info">
              <h4>{{ order.vehicleModel || '未知车型' }}</h4>
              <p>{{ formatDate(order.pickupTime) }} 至 {{ formatDate(order.returnTime) }}</p>
            </div>

            <div class="order-status">
              <span :class="orderStatusClass(order.orderStatus)">{{ orderStatusText(order.orderStatus) }}</span>
              <h4>￥{{ order.totalAmount || 0 }}</h4>
            </div>

          </div>

        </div>

      </div>

    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editVisible" title="编辑个人信息" width="480px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="所在城市">
          <el-input v-model="editForm.city" placeholder="例如：北京市" />
        </el-form-item>
        <el-form-item label="常用地址">
          <el-input v-model="editForm.address" placeholder="请输入常用地址" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 身份证详情对话框 -->
    <el-dialog v-model="showIdCardDialog" title="实名认证详情" width="680px">
      <div class="idcard-detail">
        <div class="idcard-info">
          <p><strong>真实姓名：</strong>{{ userInfo.realName }}</p>
          <p><strong>身份证号：</strong>{{ userInfo.idCard }}</p>
          <p><strong>认证状态：</strong><el-tag type="success">已认证</el-tag></p>
        </div>
        <div class="idcard-images">
          <div class="idcard-image-item">
            <h4>身份证正面（人像面）</h4>
            <img
              v-if="userInfo.idCardFront"
              :src="userInfo.idCardFront"
              class="idcard-img"
              @click="previewIdCard(userInfo.idCardFront)"
            />
            <div v-else class="no-image">
              <el-icon :size="40"><Picture /></el-icon>
              <span>暂无图片</span>
            </div>
          </div>
          <div class="idcard-image-item">
            <h4>身份证背面（国徽面）</h4>
            <img
              v-if="userInfo.idCardBack"
              :src="userInfo.idCardBack"
              class="idcard-img"
              @click="previewIdCard(userInfo.idCardBack)"
            />
            <div v-else class="no-image">
              <el-icon :size="40"><Picture /></el-icon>
              <span>暂无图片</span>
            </div>
          </div>
        </div>
        <div v-if="!userInfo.idCardFront && !userInfo.idCardBack" class="tip-text">
          <el-icon><InfoFilled /></el-icon>
          身份证图片未上传，如需更新请重新进行实名认证
        </div>
      </div>
      <template #footer>
        <el-button @click="showIdCardDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="showPreviewDialog" title="图片预览" width="700px">
      <img :src="previewImageUrl" class="preview-img" />
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi, orderApi } from '@/api/vehicle'
import { ElMessage } from 'element-plus'
import {
  User,
  Tickets,
  Bell,
  Ticket,
  Wallet,
  Star,
  Collection,
  Picture,
  InfoFilled,
  Medal
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const userInfo = ref({})
const recentOrders = ref([])
const stats = ref({ totalOrders: 0, rentingOrders: 0, completedOrders: 0, totalSpent: 0 })
const activeMenu = ref('profile')
const editVisible = ref(false)
const saving = ref(false)
const showIdCardDialog = ref(false)
const showPreviewDialog = ref(false)
const previewImageUrl = ref('')
const editForm = reactive({ phone: '', email: '', city: '', address: '' })

const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + (localStorage.getItem('token') || '')
}))

onMounted(async () => {
  await loadProfile()
  await loadOrders()
})

async function loadProfile() {
  try {
    const res = await userApi.getProfile()
    userInfo.value = res.data
  } catch (e) {
    userInfo.value = userStore.userInfo || {}
  }
}

async function loadOrders() {
  try {
    const res = await orderApi.myOrders({ pageNum: 1, pageSize: 10 })
    const records = res.data.records || []
    recentOrders.value = records.slice(0, 2)

    stats.value.totalOrders = res.data.total || records.length
    stats.value.rentingOrders = records.filter(o => o.orderStatus === 3).length
    stats.value.completedOrders = records.filter(o => o.orderStatus === 5).length
    stats.value.totalSpent = records
      .filter(o => o.orderStatus === 5)
      .reduce((sum, o) => sum + Number(o.totalAmount || 0), 0)
  } catch (e) {
    // 静默处理
  }
}

async function onAvatarSuccess(res) {
  if (res.code === 200) {
    const avatarUrl = res.data
    // 更新本地状态
    userInfo.value.avatar = avatarUrl
    userStore.userInfo.avatar = avatarUrl
    // 保存到数据库
    try {
      await userApi.updateProfile({ avatar: avatarUrl })
      ElMessage.success('头像更新成功')
    } catch (e) {
      ElMessage.error('头像保存失败')
    }
  }
}

function onMenuSelect(index) {
  const map = {
    profile: '/profile',
    order: '/order',
    coupon: '/coupon',
    member: '/member',
    wallet: '/profile',
    credit: '/credit',
    favorite: '/favorite',
    message: '/message'
  }
  if (map[index] && map[index] !== '/profile') router.push(map[index])
}

function openEdit() {
  editForm.phone = userInfo.value.phone || ''
  editForm.email = userInfo.value.email || ''
  editForm.city = userInfo.value.city || ''
  editForm.address = userInfo.value.address || ''
  editVisible.value = true
}

async function saveEdit() {
  saving.value = true
  try {
    await userApi.updateProfile({
      phone: editForm.phone,
      email: editForm.email,
      city: editForm.city,
      address: editForm.address
    })
    userInfo.value.phone = editForm.phone
    userInfo.value.email = editForm.email
    userInfo.value.city = editForm.city
    userInfo.value.address = editForm.address
    editVisible.value = false
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 预览身份证图片
function previewIdCard(url) {
  previewImageUrl.value = url
  showPreviewDialog.value = true
}

// 更改驾驶证（检查半年限制）
async function handleChangeLicense() {
  try {
    // 检查是否可以更改
    const res = await userApi.checkLicenseChange()
    if (res.data.canChange) {
      router.push('/license')
    } else {
      ElMessage.warning(res.data.message || '每半年只能更改一次驾驶证')
    }
  } catch (e) {
    ElMessage.error('检查更改权限失败')
  }
}

const roleText = computed(() => {
  return { ADMIN: '管理员', OPERATOR: '运营人员', USER: '普通用户' }[userStore.role] || '普通用户'
})
const memberLevel = computed(() => {
  // 会员等级基于累计消费金额
  const spent = stats.value.totalSpent || 0
  if (spent >= 10000) return '👑 黑金会员'
  if (spent >= 6000) return '💠 钻石会员'
  if (spent >= 3000) return '💎 白金会员'
  if (spent >= 1000) return '🥇 黄金会员'
  return '🥈 白银会员'
})
const creditLevel = computed(() => {
  const c = userInfo.value.credit || 100
  if (c >= 90) return '信用极好'
  if (c >= 70) return '信用良好'
  if (c >= 50) return '信用一般'
  return '信用较差'
})
const creditColor = (val) => val >= 80 ? '#38b48b' : val >= 60 ? '#e6a23c' : '#f56c6c'

// 实名认证状态
const idCardText = computed(() => {
  const status = userInfo.value.idCardVerified
  if (status === 1) return '已审核'
  if (userInfo.value.idCard) return '已上传'
  return '未认证'
})
const idCardTagType = computed(() => {
  const status = userInfo.value.idCardVerified
  if (status === 1) return 'success'
  if (userInfo.value.idCard) return 'warning'
  return 'info'
})

// 驾驶证状态
const licenseText = computed(() => {
  const status = userInfo.value.licenseVerified
  return ['未上传', '待审核', '已通过', '已拒绝'][status] || '未上传'
})
const licenseTagType = computed(() => {
  const s = userInfo.value.licenseVerified
  return s === 2 ? 'success' : s === 1 ? 'warning' : 'info'
})

function maskPhone(phone) {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}
function maskIdCard(idCard) {
  if (!idCard) return '未填写'
  return idCard.replace(/(\d{3})\d{11}(\d{4})/, '$1***********$2')
}
function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr.substring(0, 10)
}
function orderStatusText(s) {
  return ['待支付', '已支付', '待取车', '租赁中', '待还车', '已完成', '已取消', '退款中', '已退款'][s] || '未知'
}
function orderStatusClass(s) {
  return s === 3 ? 'renting' : s === 5 ? 'finish' : 'other'
}
</script>

<style scoped>

.btn-white-blue{
  background:#fff;
  color:#38b48b;
  border:1px solid #d9d9d9;
}
.btn-white-blue:hover{
  color:#2d9a76;
  border-color:#2d9a76;
}

.user-center{
  display:flex;
  background:#f5f7fa;
  min-height:100vh;
}

/* 左侧 */

.sidebar{
  width:240px;
  background:#fff;
  box-shadow:0 2px 12px rgba(0,0,0,.05);
  padding:20px;
}

.logo{
  margin-bottom:30px;
  color:#38b48b;
}

.menu{
  border:none;
}

/* 主体 */

.main-content{
  flex:1;
  padding:25px;
}

/* 顶部 */

.top-card{
  display:flex;
  gap:20px;
}

.user-info{
  flex:1;
  background:linear-gradient(135deg,#e3f8ee,#f7fbff);
  border-radius:20px;
  padding:30px;
  display:flex;
  align-items:center;
}

.avatar{
  width:120px;
  height:120px;
  border-radius:50%;
  object-fit:cover;
}

.info-text{
  margin-left:30px;
}

.name-row{
  display:flex;
  align-items:center;
  gap:10px;
}

.name-row h1{
  margin:0;
}

.desc{
  color:#666;
  margin:15px 0;
}

.bottom-info{
  display:flex;
  gap:30px;
  color:#666;
  font-size:14px;
}

/* 信用卡 */

.credit-card{
  width:300px;
  background:#fff;
  border-radius:20px;
  padding:20px;
  text-align:center;
  flex-shrink:0;
}

.credit-card h3{
  margin:0 0 10px;
}

.credit-score{
  font-size:24px;
  font-weight:bold;
  color:#38b48b;
}

.credit-desc{
  color:#999;
  margin-top:8px;
  font-size:13px;
}

/* 统计 */

.stat-grid{
  margin-top:20px;
  display:grid;
  grid-template-columns:repeat(4,1fr);
  gap:20px;
}

.stat-card{
  background:#fff;
  border-radius:18px;
  padding:25px;
  text-align:center;
  transition:.3s;
}

.stat-card:hover{
  transform:translateY(-5px);
}

.stat-card h2{
  margin:0 0 8px;
  font-size:28px;
  color:#38b48b;
}

.stat-card p{
  margin:0;
  color:#999;
}

/* 认证 */

.auth-grid{
  margin-top:20px;
  display:grid;
  grid-template-columns:1fr 1fr;
  gap:20px;
}

.auth-card{
  background:#fff;
  border-radius:18px;
  padding:25px;
}

.card-header{
  display:flex;
  justify-content:space-between;
  align-items:center;
  margin-bottom:20px;
}

.card-header h3{
  margin:0;
}

.auth-content{
  display:flex;
  align-items:center;
  justify-content:space-between;
}

.auth-icon{
  font-size:50px;
}

.auth-info{
  flex:1;
  margin-left:20px;
}

.auth-info p{
  margin:6px 0;
  color:#666;
  font-size:14px;
}

/* 底部 */

.bottom-grid{
  margin-top:20px;
  display:grid;
  grid-template-columns:1fr 1fr;
  gap:20px;
}

.info-card,
.order-card{
  background:#fff;
  border-radius:18px;
  padding:25px;
}

.info-item{
  display:flex;
  justify-content:space-between;
  padding:15px 0;
  border-bottom:1px solid #f0f0f0;
}

.order-item{
  display:flex;
  align-items:center;
  margin-bottom:20px;
}

.car-img{
  width:90px;
  height:60px;
  border-radius:10px;
  object-fit:cover;
  background:#f0f0f0;
}

.order-info{
  flex:1;
  margin-left:15px;
}

.order-info h4{
  margin:0 0 6px;
}

.order-info p{
  margin:0;
  color:#999;
  font-size:13px;
}

.order-status{
  text-align:right;
}

.order-status h4{
  margin:6px 0 0;
}

.renting{
  color:#38b48b;
}

.finish{
  color:#38b48b;
}

.other{
  color:#999;
}

/* 身份证详情对话框 */
.idcard-detail {
  padding: 10px 0;
}

.idcard-info {
  margin-bottom: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.idcard-info p {
  margin: 8px 0;
  font-size: 15px;
}

.idcard-images {
  display: flex;
  gap: 20px;
}

.idcard-image-item {
  flex: 1;
}

.idcard-image-item h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #666;
}

.idcard-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
  cursor: pointer;
  transition: transform 0.3s;
}

.idcard-img:hover {
  transform: scale(1.02);
}

.no-image {
  width: 100%;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #f5f5f5;
  border-radius: 8px;
  color: #ccc;
}

.tip-text {
  margin-top: 16px;
  padding: 12px;
  background: #fef0f0;
  border-radius: 8px;
  color: #f56c6c;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-img {
  width: 100%;
  max-height: 500px;
  object-fit: contain;
}

</style>
