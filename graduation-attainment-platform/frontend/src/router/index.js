import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import { ROUTE_NAMES, DEFAULT_HOME_PATH } from '@/utils/constants'

/**
 * 路由表
 * - 登录页：wzj 已完成
 * - Layout / 首页 / 404：由前端2 (zml) 在下方标记区域补充
 */
const routes = [
  {
    path: '/login',
    name: ROUTE_NAMES.LOGIN,
    component: () => import('@/views/login/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },

  // ========== 前端2 (zml) 扩展区开始 ==========
  // 示例（请 zml 取消注释并替换为实际组件）：
  // {
  //   path: '/',
  //   component: () => import('@/layouts/MainLayout.vue'),
  //   redirect: DEFAULT_HOME_PATH,
  //   children: [
  //     {
  //       path: 'home',
  //       name: ROUTE_NAMES.HOME,
  //       component: () => import('@/views/home/HomeView.vue'),
  //       meta: { title: '首页' },
  //     },
  //   ],
  // },
  // {
  //   path: '/:pathMatch(.*)*',
  //   name: ROUTE_NAMES.NOT_FOUND,
  //   component: () => import('@/views/error/NotFoundView.vue'),
  //   meta: { public: true, title: '404' },
  // },
  // ========== 前端2 (zml) 扩展区结束 ==========

  {
    path: '/',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 高校教务管理系统`
    : '高校教务管理系统'

  const isPublic = Boolean(to.meta.public)
  const hasToken = Boolean(getToken())

  if (!isPublic && !hasToken) {
    next({ name: ROUTE_NAMES.LOGIN, query: { redirect: to.fullPath } })
    return
  }

  if (to.name === ROUTE_NAMES.LOGIN && hasToken) {
    next(DEFAULT_HOME_PATH)
    return
  }

  next()
})

export default router
export { ROUTE_NAMES, DEFAULT_HOME_PATH }
