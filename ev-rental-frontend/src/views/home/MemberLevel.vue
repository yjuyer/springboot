<!--
  会员等级页面

  功能说明：
  1. 会员信息展示 - 显示当前会员等级、累计消费、成长值
  2. 等级进度条 - 显示距离下一等级还需消费多少
  3. 等级权益对比 - 展示各等级的权益差异
  4. 会员等级说明 - 解释等级规则

  会员等级（基于累计消费金额）：
  - 白银会员：0-999元
  - 黄金会员：1000-2999元
  - 白金会员：3000-5999元
  - 钻石会员：6000-9999元
  - 黑金会员：10000元以上

  权益说明：
  - 优惠券折扣：不同等级享受不同折扣
  - 免费取消：高等级可享受免费取消订单
  - 优先取车：高等级可优先取车
  - 专属客服：高等级享受专属客服服务
  - 生日特权：生日当天享受特别优惠
-->
<template>
  <div class="member-page">
    <h2 class="page-title">会员中心</h2>

    <!-- 会员卡片 -->
    <div class="member-card" :style="{ background: currentLevel.gradient, boxShadow: currentLevel.shadow }">
      <div class="card-decoration">
        <span class="deco-circle deco-1" :style="{ background: currentLevel.accent }"></span>
        <span class="deco-circle deco-2" :style="{ background: currentLevel.accent }"></span>
        <span class="deco-circle deco-3" :style="{ background: currentLevel.accent }"></span>
      </div>
      <div class="card-header">
        <div class="member-info">
          <div class="member-icon-wrap" :style="{ borderColor: currentLevel.accent }">
            <span class="member-icon">{{ currentLevel.icon }}</span>
          </div>
          <div class="member-details">
            <h3 class="member-level">{{ currentLevel.name }}</h3>
            <p class="member-desc">{{ currentLevel.desc }}</p>
          </div>
        </div>
        <div class="level-badge" :style="{ background: currentLevel.accent, color: currentLevel.color }">
          Lv.{{ levels.findIndex(l => l.name === currentLevel.name) + 1 }}
        </div>
      </div>
      <div class="member-stats">
        <div class="stat-item">
          <span class="stat-value">¥{{ totalSpent }}</span>
          <span class="stat-label">累计消费</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ memberPoints }}</span>
          <span class="stat-label">会员积分</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ orderCount }}</span>
          <span class="stat-label">完成订单</span>
        </div>
        <div class="stat-item" v-if="nextLevel">
          <span class="stat-value">¥{{ amountToNext }}</span>
          <span class="stat-label">距{{ nextLevel.name }}</span>
        </div>
      </div>
    </div>

    <!-- 等级进度 -->
    <div class="progress-section ev-card">
      <h3 class="section-title">等级进度</h3>
      <div class="progress-info">
        <span>当前等级：{{ currentLevel.name }}</span>
        <span v-if="nextLevel">距离{{ nextLevel.name }}还需消费 <strong>¥{{ amountToNext }}</strong></span>
        <span v-else class="max-level">已达最高等级 🎉</span>
      </div>
      <el-progress
        :percentage="progressPercent"
        :color="currentLevel.color"
        :stroke-width="12"
        class="level-progress"
      />
      <div class="level-range">
        <span>¥{{ currentLevel.minAmount }}</span>
        <span v-if="nextLevel">¥{{ nextLevel.minAmount }}</span>
        <span v-else>无上限</span>
      </div>
    </div>

    <!-- 等级体系 -->
    <div class="levels-section ev-card">
      <h3 class="section-title">会员等级体系</h3>
      <div class="levels-grid">
        <div
          v-for="(level, index) in levels"
          :key="level.name"
          class="level-item"
          :class="{ active: level.name === currentLevel.name }"
          :style="{
            borderColor: level.name === currentLevel.name ? level.color : '#eee',
            background: level.name === currentLevel.name ? level.accent : '#fff'
          }"
        >
          <div class="level-icon" :style="{ textShadow: level.name === currentLevel.name ? `0 2px 8px ${level.color}` : 'none' }">
            {{ level.icon }}
          </div>
          <h4 class="level-name" :style="{ color: level.name === currentLevel.name ? level.color : '#1a1a2e' }">
            {{ level.name }}
          </h4>
          <p class="level-range-text">累计消费 ≥ ¥{{ level.minAmount }}</p>
          <div
            v-if="level.name === currentLevel.name"
            class="current-badge"
            :style="{ background: level.color }"
          >当前</div>
        </div>
      </div>
    </div>

    <!-- 权益对比 -->
    <div class="benefits-section ev-card">
      <h3 class="section-title">会员权益对比</h3>
      <el-table :data="benefitsData" border stripe class="benefits-table">
        <el-table-column prop="benefit" label="权益项目" width="150" fixed />
        <el-table-column label="白银" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'benefit-yes': row.silver, 'benefit-no': !row.silver }">
              {{ row.silver ? '✓' : '✗' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="黄金" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'benefit-yes': row.gold, 'benefit-no': !row.gold }">
              {{ row.gold ? '✓' : '✗' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="白金" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'benefit-yes': row.platinum, 'benefit-no': !row.platinum }">
              {{ row.platinum ? '✓' : '✗' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="钻石" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'benefit-yes': row.diamond, 'benefit-no': !row.diamond }">
              {{ row.diamond ? '✓' : '✗' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="黑金" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'benefit-yes': row.blackGold, 'benefit-no': !row.blackGold }">
              {{ row.blackGold ? '✓' : '✗' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 权益详情 -->
    <div class="benefit-detail ev-card">
      <h3 class="section-title">权益详情</h3>
      <div class="benefit-list">
        <div v-for="benefit in currentBenefits" :key="benefit.name" class="benefit-item">
          <div class="benefit-icon">{{ benefit.icon }}</div>
          <div class="benefit-info">
            <h4>{{ benefit.name }}</h4>
            <p>{{ benefit.desc }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 等级规则 -->
    <div class="rules-section ev-card">
      <h3 class="section-title">等级规则</h3>
      <ul class="rules-list">
        <li>会员等级根据累计消费金额自动升级，无需手动申请</li>
        <li>消费金额包括租车费用，不包含押金</li>
        <li>等级只升不降，永久有效</li>
        <li>每月1日系统自动结算并更新等级</li>
        <li>如有疑问请联系客服：400-888-8888</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { userApi, orderApi, memberApi } from '@/api/vehicle'

// 会员等级定义
const levels = [
  {
    name: '白银会员',
    icon: '🥈',
    minAmount: 0,
    color: '#9CA3AF',
    gradient: 'linear-gradient(135deg, #9CA3AF 0%, #6B7280 50%, #4B5563 100%)',
    shadow: '0 8px 32px rgba(156,163,175,0.4)',
    accent: '#E5E7EB',
    desc: '基础会员，享受基本权益'
  },
  {
    name: '黄金会员',
    icon: '🥇',
    minAmount: 1000,
    color: '#F59E0B',
    gradient: 'linear-gradient(135deg, #FBBF24 0%, #F59E0B 40%, #D97706 100%)',
    shadow: '0 8px 32px rgba(245,158,11,0.45)',
    accent: '#FEF3C7',
    desc: '尊享会员，享受更多优惠'
  },
  {
    name: '白金会员',
    icon: '💎',
    minAmount: 3000,
    color: '#8B5CF6',
    gradient: 'linear-gradient(135deg, #A78BFA 0%, #8B5CF6 40%, #7C3AED 100%)',
    shadow: '0 8px 32px rgba(139,92,246,0.45)',
    accent: '#EDE9FE',
    desc: '高级会员，专属特权'
  },
  {
    name: '钻石会员',
    icon: '💠',
    minAmount: 6000,
    color: '#06B6D4',
    gradient: 'linear-gradient(135deg, #22D3EE 0%, #06B6D4 40%, #0891B2 100%)',
    shadow: '0 8px 32px rgba(6,182,212,0.45)',
    accent: '#CFFAFE',
    desc: '尊贵会员，极致体验'
  },
  {
    name: '黑金会员',
    icon: '👑',
    minAmount: 10000,
    color: '#D4AF37',
    gradient: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%)',
    shadow: '0 8px 32px rgba(212,175,55,0.5)',
    accent: '#FEF9C3',
    desc: '顶级会员，至尊享受'
  }
]

// 权益数据
const benefitsData = [
  { benefit: '专属优惠券', silver: true, gold: true, platinum: true, diamond: true, blackGold: true },
  { benefit: '生日特权', silver: false, gold: true, platinum: true, diamond: true, blackGold: true },
  { benefit: '免费取消(次/月)', silver: false, gold: '1次', platinum: '2次', diamond: '3次', blackGold: '5次' },
  { benefit: '优先取车', silver: false, gold: false, platinum: true, diamond: true, blackGold: true },
  { benefit: '专属客服', silver: false, gold: false, platinum: false, diamond: true, blackGold: true },
  { benefit: '免费升级车型', silver: false, gold: false, platinum: false, diamond: true, blackGold: true },
  { benefit: '机场接送', silver: false, gold: false, platinum: false, diamond: false, blackGold: true },
  { benefit: '专属活动邀请', silver: false, gold: false, platinum: false, diamond: false, blackGold: true }
]

const totalSpent = ref(0)
const memberPoints = ref(0)
const orderCount = ref(0)
const memberInfo = ref(null)

onMounted(async () => {
  await loadMemberInfo()
})

// 加载会员信息
async function loadMemberInfo() {
  try {
    const res = await memberApi.getMemberInfo()
    memberInfo.value = res.data
    totalSpent.value = Number(res.data.totalSpent || 0)
    memberPoints.value = Number(res.data.memberPoints || 0)
  } catch (e) {
    console.error('加载会员信息失败', e)
    // 降级：从订单计算
    await loadFromOrders()
  }
}

// 降级方案：从订单计算
async function loadFromOrders() {
  try {
    const res = await orderApi.myOrders({ pageNum: 1, pageSize: 1000 })
    const records = res.data.records || []
    orderCount.value = records.filter(o => o.orderStatus === 5).length
    totalSpent.value = records
      .filter(o => o.orderStatus === 5)
      .reduce((sum, o) => sum + Number(o.totalAmount || 0), 0)
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

// 当前等级
const currentLevel = computed(() => {
  // 优先使用会员接口返回的等级
  if (memberInfo.value && memberInfo.value.level !== undefined) {
    return levels[memberInfo.value.level] || levels[0]
  }
  for (let i = levels.length - 1; i >= 0; i--) {
    if (totalSpent.value >= levels[i].minAmount) {
      return levels[i]
    }
  }
  return levels[0]
})

// 下一等级
const nextLevel = computed(() => {
  const currentIndex = levels.findIndex(l => l.name === currentLevel.value.name)
  return currentIndex < levels.length - 1 ? levels[currentIndex + 1] : null
})

// 距离下一等级还需消费
const amountToNext = computed(() => {
  if (!nextLevel.value) return 0
  return Math.max(0, nextLevel.value.minAmount - totalSpent.value)
})

// 进度百分比
const progressPercent = computed(() => {
  if (!nextLevel.value) return 100
  const currentMin = currentLevel.value.minAmount
  const nextMin = nextLevel.value.minAmount
  const range = nextMin - currentMin
  const progress = totalSpent.value - currentMin
  return Math.min(100, Math.round((progress / range) * 100))
})

// 当前等级权益
const currentBenefits = computed(() => {
  const levelIndex = levels.findIndex(l => l.name === currentLevel.value.name)
  const allBenefits = [
    { name: '专属优惠券', icon: '🎫', desc: '每月发放专属优惠券，享受更多折扣', minLevel: 0 },
    { name: '生日特权', icon: '🎂', desc: '生日当天享受8折优惠', minLevel: 1 },
    { name: '免费取消', icon: '❌', desc: '每月可免费取消订单，无需支付违约金', minLevel: 1 },
    { name: '优先取车', icon: '🚗', desc: '取车时无需排队，优先处理', minLevel: 2 },
    { name: '专属客服', icon: '📞', desc: '享受7×24小时专属客服服务', minLevel: 3 },
    { name: '免费升级车型', icon: '⬆️', desc: '有机会免费升级到更高档次车型', minLevel: 3 },
    { name: '机场接送', icon: '✈️', desc: '享受免费机场接送服务', minLevel: 4 },
    { name: '专属活动邀请', icon: '🎉', desc: '受邀参加平台举办的专属活动', minLevel: 4 }
  ]
  return allBenefits.filter(b => levelIndex >= b.minLevel)
})
</script>

<style scoped>
.member-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 16px 48px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 24px;
}

/* 会员卡片 */
.member-card {
  border-radius: 20px;
  padding: 32px;
  color: #fff;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.member-card:hover {
  transform: translateY(-4px);
}

/* 装饰圆形 */
.card-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.12;
}

.deco-1 {
  width: 200px;
  height: 200px;
  top: -60px;
  right: -40px;
}

.deco-2 {
  width: 120px;
  height: 120px;
  bottom: -30px;
  right: 100px;
}

.deco-3 {
  width: 80px;
  height: 80px;
  top: 20px;
  right: 200px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
  position: relative;
  z-index: 1;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.member-icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.15);
  backdrop-filter: blur(4px);
}

.member-icon {
  font-size: 36px;
}

.level-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 1px;
}

.member-level {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.member-desc {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.member-stats {
  display: flex;
  gap: 40px;
  position: relative;
  z-index: 1;
  padding-top: 20px;
  border-top: 1px solid rgba(255,255,255,0.2);
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
}

/* 通用卡片样式 */
.ev-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 20px;
}

/* 进度条 */
.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.progress-info strong {
  color: #ff6b35;
  font-size: 16px;
}

.max-level {
  color: #38b48b;
  font-weight: 600;
}

.level-progress {
  margin-bottom: 8px;
}

.level-range {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

/* 等级体系 */
.levels-grid {
  display: flex;
  gap: 16px;
}

.level-item {
  flex: 1;
  text-align: center;
  padding: 20px 12px;
  border-radius: 12px;
  border: 2px solid #eee;
  position: relative;
  transition: all 0.3s;
}

.level-item.active {
  transform: scale(1.05);
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
}

.level-icon {
  font-size: 32px;
  margin-bottom: 8px;
  transition: text-shadow 0.3s;
}

.level-name {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px;
  transition: color 0.3s;
}

.level-range-text {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.current-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  color: #fff;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

/* 权益对比表格 */
.benefits-table {
  width: 100%;
}

.benefit-yes {
  color: #38b48b;
  font-weight: 700;
  font-size: 18px;
}

.benefit-no {
  color: #ccc;
  font-size: 18px;
}

/* 权益详情 */
.benefit-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.benefit-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.benefit-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.benefit-info h4 {
  margin: 0 0 4px;
  font-size: 15px;
  color: #1a1a2e;
}

.benefit-info p {
  margin: 0;
  font-size: 13px;
  color: #666;
}

/* 规则说明 */
.rules-list {
  padding-left: 20px;
  margin: 0;
}

.rules-list li {
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.rules-list li:last-child {
  margin-bottom: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .levels-grid {
    flex-wrap: wrap;
  }

  .level-item {
    flex: 0 0 calc(33.33% - 11px);
  }

  .benefit-list {
    grid-template-columns: 1fr;
  }

  .member-stats {
    gap: 24px;
  }
}

@media (max-width: 480px) {
  .level-item {
    flex: 0 0 calc(50% - 8px);
  }
}
</style>
