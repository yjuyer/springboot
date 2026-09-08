import request from '@/utils/request'

/**
 * API接口定义文件
 *
 * <p>集中管理所有后端API接口，便于维护和复用</p>
 *
 * <p>接口模块：</p>
 * <ul>
 *   <li>vehicleApi - 车辆相关接口（列表、详情、热门、门店）</li>
 *   <li>uploadApi - 文件上传接口</li>
 *   <li>orderApi - 订单相关接口（创建、支付、取消、列表、详情）</li>
 *   <li>payApi - 支付相关接口（创建支付、二维码、确认支付）</li>
 *   <li>noticeApi - 公告相关接口</li>
 *   <li>userApi - 用户相关接口（资料、实名认证、驾驶证）</li>
 *   <li>adminApi - 管理员接口（统计、车辆/门店/订单管理）</li>
 *   <li>depositApi - 押金管理接口</li>
 *   <li>couponApi - 优惠券相关接口</li>
 * </ul>
 */

/** 车辆相关接口 */
export const vehicleApi = {
  list(params) {
    return request.get('/vehicle/list', { params })
  },
  hot() {
    return request.get('/vehicle/hot')
  },
  detail(id) {
    return request.get(`/vehicle/detail/${id}`)
  },
  getStores() {
    return request.get('/store/list')
  }
}

export const uploadApi = {
  upload(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/image/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export const orderApi = {
  create(data) {
    return request.post('/order/create', data)
  },
  pay(data) {
    return request.post('/order/pay', data)
  },
  cancel(id, reason) {
    return request.post(`/order/cancel/${id}`, { reason })
  },
  myOrders(params) {
    return request.get('/order/my', { params })
  },
  detail(id) {
    return request.get(`/order/detail/${id}`)
  },
  requestReturn(id) {
    return request.post(`/order/return-request/${id}`)
  },
  pickup(id) {
    return request.post(`/order/pickup/${id}`)
  }
}

export const payApi = {
  create(data) {
    return request.post('/pay/create', data)
  },
  qrcode(orderNo) {
    return request.get(`/pay/qrcode/${orderNo}`)
  },
  orderDetail(orderNo) {
    return request.get(`/pay/order/${orderNo}`)
  },
  status(orderNo) {
    return request.get('/pay/status', { params: { orderNo } })
  },
  confirm(data) {
    return request.post('/pay/confirm', data)
  }
}

export const noticeApi = {
  list() { return request.get('/notice/list') },
  detail(id) { return request.get(`/notice/detail/${id}`) }
}

/** 用户通知接口 */
export const notificationApi = {
  /** 获取我的通知列表（分页） */
  getMy(params) { return request.get('/notification/my', { params }) },
  /** 获取未读通知数量 */
  getUnreadCount() { return request.get('/notification/unread-count') },
  /** 标记单条通知为已读 */
  markAsRead(id) { return request.put(`/notification/read/${id}`) },
  /** 标记所有通知为已读 */
  markAllAsRead() { return request.put('/notification/read-all') },
  /** 删除通知 */
  deleteNotification(id) { return request.delete(`/notification/${id}`) }
}

export const userApi = {
  getProfile() { return request.get('/user/profile') },
  updateProfile(data) { return request.put('/user/profile', data) },
  realNameVerify(data) { return request.post('/user/realNameVerify', data) },
  uploadLicense(formData) {
    return request.post('/user/uploadLicense', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  checkLicenseChange() { return request.get('/user/license/canChange') }
}

export const adminApi = {
  getStats() { return request.get('/admin/dashboard/stats') },
  getVehicleStatus() { return request.get('/admin/dashboard/vehicleStatus') },
  getOrderTrend() { return request.get('/admin/dashboard/orderTrend') },
  getOrderStatus() { return request.get('/admin/dashboard/orderStatus') },
  getPopularVehicles() { return request.get('/admin/dashboard/popularVehicles') },
  getStoreRank() { return request.get('/admin/dashboard/storeRank') },

  getVehicleList(params) { return request.get('/admin/vehicle/list', { params }) },
  addVehicle(data) { return request.post('/admin/vehicle/add', data) },
  updateVehicle(data) { return request.put('/admin/vehicle/update', data) },
  deleteVehicle(id) { return request.delete(`/admin/vehicle/delete/${id}`) },

  getVehicleImages(vehicleId) { return request.get(`/admin/vehicle/images/${vehicleId}`) },
  saveVehicleImages(vehicleId, images) {
    return request.post(`/admin/vehicle/images/save?vehicleId=${vehicleId}`, images)
  },
  deleteVehicleImage(imageId) { return request.delete(`/admin/vehicle/images/delete/${imageId}`) },

  getStoreList(params) { return request.get('/admin/store/list', { params }) },
  addStore(data) { return request.post('/admin/store/add', data) },
  updateStore(data) { return request.put('/admin/store/update', data) },
  deleteStore(id) { return request.delete(`/admin/store/delete/${id}`) },

  getOrderList(params) { return request.get('/admin/order/list', { params }) },
  getOrderDetail(id) { return request.get(`/admin/order/detail/${id}`) },
  pickupOrder(data) { return request.post('/admin/order/pickup', data) },
  confirmReturn(orderId, data) { return request.post(`/admin/order/return/confirm/${orderId}`, data) },
  initiateRefund(orderId) { return request.post(`/admin/order/refund/initiate/${orderId}`) },
  completeRefund(orderId, data) { return request.post(`/admin/order/refund/complete/${orderId}`, data) },

  getReviewList(params) { return request.get('/admin/review/list', { params }) },
  auditReview(id, data) { return request.put(`/admin/review/audit/${id}`, data) },
  deleteReview(id) { return request.delete(`/admin/review/${id}`) },

  getOperationStats() { return request.get('/admin/operation/stats') },
  getDispatchList(params) { return request.get('/admin/operation/dispatch/list', { params }) },
  createDispatch(data) { return request.post('/admin/operation/dispatch/create', data) },
  completeDispatch(id) { return request.post(`/admin/operation/dispatch/complete/${id}`) },
  getRepairList(params) { return request.get('/admin/operation/repair/list', { params }) },
  createRepair(data) { return request.post('/admin/operation/repair/create', data) },
  completeRepair(id, data) { return request.post(`/admin/operation/repair/complete/${id}`, data) },
  getChargingList(params) { return request.get('/admin/operation/charging/list', { params }) },
  startCharging(data) { return request.post('/admin/operation/charging/start', data) },
  completeCharging(id, data) { return request.post(`/admin/operation/charging/complete/${id}`, data) }
}

export const depositApi = {
  stats() { return request.get('/admin/deposit/stats') },
  list(params) { return request.get('/admin/deposit/list', { params }) },
  detail(orderNo) { return request.get(`/admin/deposit/detail/${orderNo}`) },
  refund(data) { return request.post('/admin/deposit/refund', data) },
  refunds(params) { return request.get('/admin/deposit/refunds', { params }) }
}

export const couponApi = {
  getAvailableCoupons() { return request.get('/coupon/list') },
  claimCoupon(id) { return request.post(`/coupon/claim/${id}`) },
  getMyCoupons() { return request.get('/coupon/my') },
  getAvailableForOrder() { return request.get('/coupon/available') },
  calculateDiscount(userCouponId, orderAmount) {
    return request.get('/coupon/calculate', { params: { userCouponId, orderAmount } })
  }
}

export const reviewApi = {
  getVehicleReviews(vehicleId, params) { return request.get(`/review/vehicle/${vehicleId}`, { params }) },
  getVehicleSummary(vehicleId) { return request.get(`/review/vehicle/${vehicleId}/summary`) },
  getOrderReviewStatus(orderId) { return request.get(`/review/order/${orderId}/status`) },
  submit(data) { return request.post('/review/submit', data) }
}

export const creditApi = {
  logs(params) { return request.get('/credit/logs', { params }) },
  rules() { return request.get('/credit/rules') }
}

export const memberApi = {
  getMemberInfo() { return request.get('/member/info') },
  getBenefits(level) { return request.get('/member/benefits', { params: { level } }) },
  canFreeCancel() { return request.get('/member/can-free-cancel') }
}

/** 用户收藏接口 */
export const favoriteApi = {
  /** 获取我的收藏列表 */
  getMyFavorites() { return request.get('/favorite/list') },
  /** 切换收藏状态 */
  toggleFavorite(vehicleId) { return request.post(`/favorite/toggle/${vehicleId}`) },
  /** 检查是否已收藏 */
  checkFavorite(vehicleId) { return request.get(`/favorite/check/${vehicleId}`) },
  /** 删除收藏 */
  deleteFavorite(id) { return request.delete(`/favorite/${id}`) }
}

/** 发票接口 */
export const invoiceApi = {
  /** 申请发票 */
  apply(data) { return request.post('/invoice/apply', data) },
  /** 我的发票列表 */
  myInvoices(params) { return request.get('/invoice/my', { params }) },
  /** 发票详情 */
  detail(id) { return request.get(`/invoice/detail/${id}`) },
  /** 管理员发票列表 */
  adminList(params) { return request.get('/admin/invoice/list', { params }) },
  /** 管理员审核开票 */
  audit(data) { return request.post('/admin/invoice/audit', data) },
  /** 管理员标记已发送 */
  send(id) { return request.post(`/admin/invoice/send/${id}`) }
}

/** 促销活动接口 */
export const promotionApi = {
  /** 活动列表 */
  list(params) { return request.get('/admin/promotion/list', { params }) },
  /** 创建活动 */
  create(data) { return request.post('/admin/promotion/create', data) },
  /** 更新活动 */
  update(data) { return request.put('/admin/promotion/update', data) },
  /** 删除活动 */
  delete(id) { return request.delete(`/admin/promotion/delete/${id}`) },
  /** 切换活动状态 */
  toggle(id) { return request.post(`/admin/promotion/toggle/${id}`) },
  /** 获取有效活动（用户端） */
  active() { return request.get('/admin/promotion/active') }
}

/** 积分接口 */
export const pointApi = {
  /** 积分历史 */
  history(params) { return request.get('/points/history', { params }) },
  /** 当前积分余额 */
  balance() { return request.get('/points/balance') },
  /** 兑换优惠券 */
  exchangeCoupon(data) { return request.post('/points/exchange/coupon', data) },
  /** 兑换免费取消 */
  exchangeFreeCancel(data) { return request.post('/points/exchange/free-cancel', data) },
  /** 管理员手动调整积分 */
  adjust(data) { return request.post('/admin/points/adjust', data) },
  /** 管理员查看用户积分历史 */
  userHistory(userId, params) { return request.get(`/admin/points/history/${userId}`, { params }) }
}
