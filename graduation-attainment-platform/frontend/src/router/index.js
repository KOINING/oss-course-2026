import { createRouter, createWebHistory } from 'vue-router'
import pinia from '@/stores'
import { useUserStore } from '@/stores/user'
import { getProtectedRoutes } from '@/config/navigation'
import { getToken } from '@/utils/auth'
import { APP_TITLE, DEFAULT_HOME_PATH, ROUTE_NAMES } from '@/utils/constants'

function resolveProtectedComponent(item) {
  if (item.componentKey === 'account-role-management') {
    return () => import('@/views/admin/AccountRoleManagementView.vue')
  }
  if (item.routeName === ROUTE_NAMES.REQUIREMENTS) {
    return () => import('@/views/requirements/RequirementsView.vue')
  }

  if (item.routeName === ROUTE_NAMES.SUPPORT_MATRIX) {
    return () => import('@/views/requirements/SupportMatrixView.vue')
  }

  if (item.routeName === ROUTE_NAMES.BASIC_DATA) {
    return () => import('@/views/basic-data/BasicDataView.vue')
  }

  if (item.routeName === ROUTE_NAMES.TEACHING_CLASS) {
    return () => import('@/views/basic/TeachingClassView.vue')
  }

  if (item.routeName === ROUTE_NAMES.STUDENT_LIST) {
    return () => import('@/views/basic/StudentListView.vue')
  }

  return item.routeName === ROUTE_NAMES.HOME
    ? () => import('@/views/home/HomeView.vue')
    : () => import('@/views/system/ModulePlaceholderView.vue')
}

const protectedChildren = getProtectedRoutes().map((item) => ({
  path: item.path.replace(/^\//, ''),
  name: item.routeName,
  component: resolveProtectedComponent(item),
  meta: {
    title: item.label,
    summary: item.summary,
    entities: item.entities,
    moduleTitle: item.moduleTitle,
    sectionLabel: item.sectionLabel,
  },
}))

const routes = [
  {
    path: '/login',
    name: ROUTE_NAMES.LOGIN,
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: DEFAULT_HOME_PATH,
    children: protectedChildren,
  },
  {
    path: '/:pathMatch(.*)*',
    name: ROUTE_NAMES.NOT_FOUND,
    component: () => import('@/views/system/NotFoundView.vue'),
    meta: { public: true, title: '404' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(async (to) => {
  document.title = to.meta.title ? `${to.meta.title} - ${APP_TITLE}` : APP_TITLE

  const isPublic = Boolean(to.meta.public)
  const hasToken = Boolean(getToken())
  const userStore = useUserStore(pinia)

  if (!isPublic && !hasToken) {
    return { name: ROUTE_NAMES.LOGIN, query: { redirect: to.fullPath } }
  }

  if (to.name === ROUTE_NAMES.LOGIN && hasToken) {
    return DEFAULT_HOME_PATH
  }

  if (!isPublic && hasToken && !userStore.profileLoaded) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      if (!getToken()) {
        return { name: ROUTE_NAMES.LOGIN, query: { redirect: to.fullPath } }
      }
    }
  }

  return true
})

export default router
export { ROUTE_NAMES, DEFAULT_HOME_PATH }
