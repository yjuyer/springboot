<template>
  <div class="credit-page">
    <h2 class="page-title">信誉积分</h2>

    <!-- 积分概览 -->
    <div class="credit-overview">
      <div class="credit-left">
        <el-progress
          type="dashboard"
          :percentage="credit"
          :width="160"
          :color="creditColor"
          :stroke-width="12"
        >
          <template #default>
            <div class="credit-inner">
              <span class="credit-num">{{ credit }}</span>
              <span class="credit-label">信用分</span>
            </div>
          </template>
        </el-progress>
      </div>
      <div class="credit-right">
        <h3>{{ creditLevel }}</h3>
        <p class="credit-desc">{{ creditDesc }}</p>
        <div class="credit-rule">
          <el-icon :size="16" color="#38b48b"><InfoFilled /></el-icon>
          <span>积分 ≥ 80 可正常租车，低于 80 将被限制用车</span>
        </div>
      </div>
    </div>

    <!-- 积分规则 -->
    <div class="rule-card">
      <h3>积分规则</h3>
      <div class="rule-list">
        <div class="rule-item" v-for="rule in rules" :key="rule.code">
          <div class="rule-left">
            <el-tag :type="rule.type === '加分' ? 'success' : 'danger'" size="small">{{ rule.type }}</el-tag>
            <span class="rule-name">{{ rule.name }}</span>
          </div>
          <span class="rule-amount" :class="rule.type === '加分' ? 'plus' : 'minus'">
            {{ rule.type === '加分' ? '+' : '-' }}{{ rule.amount }}分
          </span>
        </div>
      </div>
    </div>

    <!-- 积分变动记录 -->
    <div class="log-card">
      <h3>变动记录</h3>
      <div v-if="logs.length === 0" class="empty">
        <p>暂无变动记录</p>
      </div>
      <div v-else class="log-list">
        <div class="log-item" v-for="log in logs" :key="log.id">
          <div class="log-left">
            <span class="log-reason">{{ log.reason }}</span>
            <span class="log-time">{{ log.createTime }}</span>
          </div>
          <span class="log-amount" :class="log.changeType === '加分' ? 'plus' : 'minus'">
            {{ log.changeType === '加分' ? '+' : '-' }}{{ log.changeAmount }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { userApi, creditApi } from '@/api/vehicle'
import { InfoFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const credit = ref(100)
const logs = ref([])
const rules = ref([])

onMounted(async () => {
  try {
    const [res, logRes, ruleRes] = await Promise.all([
      userApi.getProfile(),
      creditApi.logs({ pageSize: 20 }),
      creditApi.rules()
    ])
    credit.value = res.data.credit || res.data.creditScore || 100
    logs.value = logRes.data?.records || []
    rules.value = (ruleRes.data || []).map(r => ({
      code: r.ruleCode,
      name: r.ruleName,
      type: r.changeType,
      amount: r.changeAmount
    }))
  } catch {
    credit.value = userStore.userInfo?.credit || 100
  }
})

const creditLevel = computed(() => {
  const c = credit.value
  if (c >= 90) return '信用极好'
  if (c >= 80) return '信用良好'
  if (c >= 60) return '信用一般'
  return '信用较差'
})

const creditDesc = computed(() => {
  const c = credit.value
  if (c >= 90) return '您的信用表现优异，可享受优先租车等特权'
  if (c >= 80) return '您的信用良好，可以正常租车'
  if (c >= 60) return '您的信用分偏低，部分功能受限'
  return '您的信用分过低，无法使用租车服务'
})

const creditColor = (val) => {
  if (val >= 90) return '#00b578'
  if (val >= 80) return '#38b48b'
  if (val >= 60) return '#e6a23c'
  return '#f56c6c'
}


</script>

<style scoped>
.credit-page { max-width: 800px; margin: 0 auto; }
.page-title { font-size: 24px; margin-bottom: 20px; color: #1a1a2e; }

.credit-overview {
  display: flex;
  align-items: center;
  gap: 40px;
  background: #fff;
  border-radius: 20px;
  padding: 36px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  margin-bottom: 20px;
}
.credit-left { flex-shrink: 0; }
.credit-inner { text-align: center; }
.credit-num { font-size: 36px; font-weight: 700; color: #1a1a2e; display: block; }
.credit-label { font-size: 12px; color: #999; }

.credit-right h3 { margin: 0 0 8px; font-size: 22px; color: #1a1a2e; }
.credit-desc { color: #666; margin: 0 0 16px; font-size: 14px; line-height: 1.6; }
.credit-rule {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #f0f7ff;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
  color: #38b48b;
}

.rule-card, .log-card {
  background: #fff;
  border-radius: 18px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  margin-bottom: 20px;
}
.rule-card h3, .log-card h3 { margin: 0 0 16px; font-size: 17px; color: #1a1a2e; }

.rule-list { display: flex; flex-direction: column; }
.rule-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}
.rule-item:last-child { border-bottom: none; }
.rule-left { display: flex; align-items: center; gap: 10px; }
.rule-name { font-size: 14px; color: #333; }
.rule-amount { font-weight: 600; font-size: 15px; }
.rule-amount.plus { color: #00b578; }
.rule-amount.minus { color: #f56c6c; }

.log-list { display: flex; flex-direction: column; }
.log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f5f5f5;
}
.log-item:last-child { border-bottom: none; }
.log-left { display: flex; flex-direction: column; gap: 4px; }
.log-reason { font-size: 14px; color: #333; }
.log-time { font-size: 12px; color: #bbb; }
.log-amount { font-weight: 600; font-size: 16px; }
.log-amount.plus { color: #00b578; }
.log-amount.minus { color: #f56c6c; }

.empty { text-align: center; padding: 30px 0; color: #ccc; }
</style>
