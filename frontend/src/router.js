import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'

import HomeView from './views/HomeView.vue'
import GuidelinesView from './views/GuidelinesView.vue'
import LoginView from './views/LoginView.vue'
import SignupView from './views/SignupView.vue'
import AccountView from './views/AccountView.vue'
import DiaryView from './views/DiaryView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/guidelines', name: 'guidelines', component: GuidelinesView },
  { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
  { path: '/signup', name: 'signup', component: SignupView, meta: { guestOnly: true } },
  { path: '/account', name: 'account', component: AccountView, meta: { requiresAuth: true } },
  { path: '/diary', name: 'diary', component: DiaryView, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.ready) {
    await auth.fetchMe()
  }
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'account' }
  }
  return true
})

export default router
