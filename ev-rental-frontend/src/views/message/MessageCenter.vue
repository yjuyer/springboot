<template>
  <div class="msg-page">
    <h2 class="page-title">消息中心</h2>

    <!-- 顶部 Tab -->
    <div class="msg-tabs">
      <div class="msg-tab" :class="{ active: activeTab === 'notification' }" @click="switchTab('notification')">
        系统消息
        <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="tab-badge" />
      </div>
      <div class="msg-tab" :class="{ active: activeTab === 'notice' }" @click="switchTab('notice')">
        平台公告
      </div>
    </div>

    <!-- 系统消息 -->
    <template v-if="activeTab === 'notification'">
      <!-- 操作栏 -->
      <div class="msg-actions" v-if="notifications.length > 0">
        <div class="filter-group">
          <el-radio-group v-model="readFilter" size="small" @change="loadNotifications">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="unread">未读</el-radio-button>
            <el-radio-button label="read">已读</el-radio-button>
          </el-radio-group>
        </div>
        <el-button type="primary" link size="small" @click="markAllAsRead" :disabled="unreadCount === 0">
          全部已读
        </el-button>
      </div>

      <div class="msg-list">
        <div v-for="msg in notifications" :key="msg.id" class="msg-item" :class="{ unread: msg.isRead === 0 }"
             @click="handleNotificationClick(msg)">
          <div class="msg-dot" v-if="msg.isRead === 0"></div>
          <div class="msg-icon">{{ getTypeIcon(msg.type) }}</div>
          <div class="msg-body">
            <div class="msg-header">
              <span class="msg-title">{{ msg.title }}</span>
              <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
            </div>
            <p class="msg-content">{{ msg.content }}</p>
          </div>
          <el-button class="msg-delete" type="danger" :icon="Delete" circle size="small"
                     @click.stop="deleteNotification(msg.id)" />
        </div>

        <div v-if="notifications.length === 0" class="empty">
          <el-icon :size="48" color="#ccc"><Bell /></el-icon>
          <p>{{ readFilter === 'unread' ? '暂无未读消息' : '暂无消息' }}</p>
        </div>

        <!-- 分页 -->
        <div class="msg-pagination" v-if="notificationTotal > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="notificationTotal"
            layout="prev, pager, next"
            @current-change="loadNotifications"
          />
        </div>
      </div>
    </template>

    <!-- 平台公告 -->
    <template v-if="activeTab === 'notice'">
      <div class="msg-list">
        <div v-for="notice in notices" :key="notice.id" class="msg-item notice-item"
             @click="openNoticeDetail(notice)">
          <div class="msg-icon">📢</div>
          <div class="msg-body">
            <div class="msg-header">
              <span class="msg-title">
                <el-tag v-if="notice.isTop" type="danger" size="small" effect="dark" class="top-tag">置顶</el-tag>
                {{ notice.title }}
              </span>
              <span class="msg-time">{{ formatTime(notice.publishTime || notice.createTime) }}</span>
            </div>
            <p class="msg-content" v-html="notice.content"></p>
          </div>
        </div>

        <div v-if="notices.length === 0" class="empty">
          <el-icon :size="48" color="#ccc"><Promotion /></el-icon>
          <p>暂无公告</p>
        </div>
      </div>
    </template>

    <!-- 公告详情弹窗 -->
    <el-dialog v-model="noticeDialogVisible" :title="currentNotice?.title" width="600px" top="15vh">
      <div class="notice-detail" v-html="currentNotice?.content"></div>
      <div class="notice-meta">
        <span>发布时间：{{ formatTime(currentNotice?.publishTime || currentNotice?.createTime) }}</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { noticeApi, notificationApi } from '@/api/vehicle'
import { Bell, Promotion, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// Tab 状态
const activeTab = ref('notification')

// 通知相关
const notifications = ref([])
const notificationTotal = ref(0)
const unreadCount = ref(0)
const readFilter = ref('all')
const currentPage = ref(1)
const pageSize = 10

// 公告相关
const notices = ref([])
const noticeDialogVisible = ref(false)
const currentNotice = ref(null)

/** 切换 Tab */
function switchTab(tab) {
  activeTab.value = tab
  if (tab === 'notification') {
    loadNotifications()
  } else {
    loadNotices()
  }
}

/** 加载通知列表 */
async function loadNotifications() {
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize
    }
    if (readFilter.value === 'unread') params.isRead = 0
    if (readFilter.value === 'read') params.isRead = 1

    const res = await notificationApi.getMy(params)
    notifications.value = res.data?.records || []
    notificationTotal.value = res.data?.total || 0
  } catch (e) {
    notifications.value = []
  }
  fetchUnreadCount()
}

/** 加载公告列表 */
async function loadNotices() {
  try {
    const res = await noticeApi.list()
    notices.value = res.data || []
  } catch (e) {
    notices.value = []
  }
}

/** 获取未读数量 */
async function fetchUnreadCount() {
  try {
    const res = await notificationApi.getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (e) {
    // 静默
  }
}

/** 点击通知 */
async function handleNotificationClick(msg) {
  if (msg.isRead === 0) {
    try {
      await notificationApi.markAsRead(msg.id)
      msg.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {
      // 静默
    }
  }
  // 订单类通知可跳转
  if (msg.type === 1 && msg.relatedId) {
    router.push(`/order/detail/${msg.relatedId}`)
  }
}

/** 标记全部已读 */
async function markAllAsRead() {
  try {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => { n.isRead = 1 })
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

/** 删除通知 */
async function deleteNotification(id) {
  try {
    await ElMessageBox.confirm('确定删除该通知？', '提示', { type: 'warning' })
    await notificationApi.deleteNotification(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
    ElMessage.success('已删除')
    fetchUnreadCount()
  } catch (e) {
    // 取消
  }
}

/** 打开公告详情 */
function openNoticeDetail(notice) {
  currentNotice.value = notice
  noticeDialogVisible.value = true
}

/** 获取类型图标 */
function getTypeIcon(type) {
  const icons = { 1: '🛒', 2: '🪪', 3: '📢', 4: '🎫' }
  return icons[type] || '📋'
}

/** 格式化时间 */
function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.msg-page { max-width: 800px; margin: 0 auto; padding: 20px; }
.page-title { font-size: 24px; margin-bottom: 20px; color: #1a1a2e; }

.msg-tabs {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}
.msg-tab {
  font-size: 15px;
  color: #999;
  cursor: pointer;
  padding-bottom: 8px;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}
.msg-tab.active {
  color: #38b48b;
  border-bottom-color: #38b48b;
}

.tab-badge {
  margin-left: 2px;
}

.msg-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.msg-list {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
}

.msg-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 18px 24px;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.15s;
  cursor: pointer;
  position: relative;
}
.msg-item:last-child { border-bottom: none; }
.msg-item:hover { background: #fafafa; }
.msg-item.unread { background: #f0faf6; }

.msg-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #38b48b;
  margin-top: 8px;
  flex-shrink: 0;
}

.msg-icon {
  font-size: 24px;
  flex-shrink: 0;
  margin-top: 2px;
}

.msg-body { flex: 1; min-width: 0; }

.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.msg-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 6px;
}
.msg-time {
  font-size: 12px;
  color: #bbb;
  flex-shrink: 0;
}
.msg-content {
  font-size: 13px;
  color: #666;
  margin: 0;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.msg-delete {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0;
  transition: opacity 0.2s;
}
.msg-item:hover .msg-delete {
  opacity: 1;
}

.top-tag {
  margin-right: 4px;
}

.empty {
  text-align: center;
  padding: 60px 0;
  color: #ccc;
}
.empty p {
  margin-top: 12px;
  font-size: 14px;
}

.msg-pagination {
  padding: 16px;
  display: flex;
  justify-content: center;
}

.notice-detail {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
}

.notice-meta {
  margin-top: 16px;
  font-size: 12px;
  color: #999;
  text-align: right;
}
</style>
