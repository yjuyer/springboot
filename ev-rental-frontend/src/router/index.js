import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

/**
 * 路由配置文件
 *
 * <p>路由结构：</p>
 * <ul>
 *   <li>公开页面 - 登录、注册、忘记密码（无需认证）</li>
 *   <li>前台用户页面 - 使用MainLayout布局，包含导航栏</li>
 *   <li>后台管理页面 - 使用AdminLayout布局，需要ADMIN或OPERATOR角色</li>
 * </ul>
 *
 * <p>路由守卫：</p>
 * <ul>
 *   <li>登录检查 - 需要认证的页面未登录时跳转到登录页</li>
 *   <li>权限检查 - 后台管理页面需要特定角色才能访问</li>
 *   <li>页面标题 - 根据路由meta.title动态设置浏览器标题</li>
 * </ul>
 */
const routes = [
  // ========== 公开页面 ==========
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forgot',
    name: 'ForgotPassword',
    component: () => import('@/views/forgot/ForgotPassword.vue'),
    meta: { title: '忘记密码' }
  },
  {
    path: '/pay/success',
    name: 'PaySuccess',
    component: () => import('@/views/pay/PaySuccess.vue'),
    meta: { title: '支付成功' }
  },

  // ========== 前台用户页面（带导航栏） ==========
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'vehicle',
        name: 'VehicleList',
        component: () => import('@/views/vehicle/VehicleList.vue'),
        meta: { title: '我要租车' }
      },
      {
        path: 'stores',
        name: 'StoreList',
        component: () => import('@/views/stores/StoreList.vue'),
        meta: { title: '门店查询' }
      },
      {
        path: 'charging-map',
        name: 'ChargingMap',
        component: () => import('@/views/charging/ChargingMap.vue'),
        meta: { title: '充电地图' }
      },
      {
        path: 'vehicle/:id',
        name: 'VehicleDetail',
        component: () => import('@/views/vehicle/VehicleDetail.vue'),
        meta: { title: '车辆详情' }
      },
      {
        path: 'order/create/:vehicleId',
        name: 'CreateOrder',
        component: () => import('@/views/order/CreateOrder.vue'),
        meta: { title: '预约租车', requireAuth: true }
      },
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '我的订单', requireAuth: true }
      },
      {
        path: 'order/detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '订单详情', requireAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/home/Profile.vue'),
        meta: { title: '个人中心', requireAuth: true }
      },
      {
        path: 'verify',
        name: 'Verify',
        component: () => import('@/views/home/Verify.vue'),
        meta: { title: '实名认证', requireAuth: true }
      },
      {
        path: 'license',
        name: 'LicenseUpload',
        component: () => import('@/views/home/LicenseUpload.vue'),
        meta: { title: '驾驶证上传', requireAuth: true }
      },
      {
        path: 'message',
        name: 'MessageCenter',
        component: () => import('@/views/message/MessageCenter.vue'),
        meta: { title: '消息中心', requireAuth: true }
      },
      {
        path: 'favorite',
        name: 'FavoriteList',
        component: () => import('@/views/favorite/FavoriteList.vue'),
        meta: { title: '我的收藏', requireAuth: true }
      },
      {
        path: 'credit',
        name: 'Credit',
        component: () => import('@/views/credit/Credit.vue'),
        meta: { title: '信誉积分', requireAuth: true }
      },
      {
        path: 'coupon',
        name: 'CouponList',
        component: () => import('@/views/coupon/CouponList.vue'),
        meta: { title: '优惠券中心', requireAuth: true }
      },
      {
        path: 'member',
        name: 'MemberLevel',
        component: () => import('@/views/home/MemberLevel.vue'),
        meta: { title: '会员中心', requireAuth: true }
      },
      {
        path: 'invoice/apply',
        name: 'InvoiceApply',
        component: () => import('@/views/invoice/InvoiceApply.vue'),
        meta: { title: '申请发票', requireAuth: true }
      },
      {
        path: 'invoices',
        name: 'InvoiceList',
        component: () => import('@/views/invoice/InvoiceList.vue'),
        meta: { title: '我的发票', requireAuth: true }
      },
      {
        path: 'points/shop',
        name: 'PointShop',
        component: () => import('@/views/points/PointShop.vue'),
        meta: { title: '积分商城', requireAuth: true }
      },
      {
        path: 'points/history',
        name: 'PointHistory',
        component: () => import('@/views/points/PointHistory.vue'),
        meta: { title: '积分明细', requireAuth: true }
      }
    ]
  },

  // ========== 后台管理页面 ==========
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requireAuth: true, roles: ['ADMIN', 'OPERATOR'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard/Dashboard.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'vehicle',
        name: 'AdminVehicle',
        component: () => import('@/views/admin/vehicle/Vehicle.vue'),
        meta: { title: '车辆管理' }
      },
      {
        path: 'store',
        name: 'AdminStore',
        component: () => import('@/views/admin/store/Store.vue'),
        meta: { title: '门店管理' }
      },
      {
        path: 'deposit',
        name: 'AdminDeposit',
        component: () => import('@/views/admin/deposit/Deposit.vue'),
        meta: { title: '押金管理' }
      },
      {
        path: 'order',
        name: 'AdminOrder',
        component: () => import('@/views/admin/order/AdminOrder.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'review',
        name: 'AdminReview',
        component: () => import('@/views/admin/review/Review.vue'),
        meta: { title: '评价管理' }
      },
      {
        path: 'operation',
        name: 'AdminOperation',
        component: () => import('@/views/admin/operation/Operation.vue'),
        meta: { title: '运维管理' }
      },
      {
        path: 'verify',
        name: 'AdminVerify',
        component: () => import('@/views/admin/verify/Verify.vue'),
        meta: { title: '认证审核' }
      },
      {
        path: 'invoice',
        name: 'AdminInvoice',
        component: () => import('@/views/admin/invoice/AdminInvoice.vue'),
        meta: { title: '发票管理' }
      },
      {
        path: 'promotion',
        name: 'AdminPromotion',
        component: () => import('@/views/admin/promotion/Promotion.vue'),
        meta: { title: '促销活动' }
      },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 登录和权限检查
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 新能源汽车租赁` : '新能源汽车租赁'

  const userStore = useUserStore()

  // 需要登录
  if (to.meta.requireAuth && !userStore.token) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }

  // 角色权限
  if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
    ElMessage.error('没有访问权限')
    next(from.path || '/')
    return
  }

  next()
})

export default router
