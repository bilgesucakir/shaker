import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'

import HomeView from './views/HomeView.vue'
import GuidelinesView from './views/GuidelinesView.vue'
import LoginView from './views/LoginView.vue'
import SignupView from './views/SignupView.vue'
import AccountView from './views/AccountView.vue'
import DiaryView from './views/DiaryView.vue'
import AdminUsersView from './views/admin/AdminUsersView.vue'
import AdminIngredientsView from './views/admin/AdminIngredientsView.vue'
import AdminGuidelinesView from './views/admin/AdminGuidelinesView.vue'
import AdminRecipesView from './views/admin/AdminRecipesView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/guidelines', name: 'guidelines', component: GuidelinesView },
  { path: '/login', name: 'login', component: LoginView, meta: { guestOnly: true } },
  { path: '/signup', name: 'signup', component: SignupView, meta: { guestOnly: true } },
  { path: '/account', name: 'account', component: AccountView, meta: { requiresAuth: true } },
  { path: '/diary', name: 'diary', component: DiaryView, meta: { requiresAuth: true } },
  { path: '/admin/users', name: 'admin-users', component: AdminUsersView, meta: { requiresAdmin: true } },
  { path: '/admin/ingredients', name: 'admin-ingredients', component: AdminIngredientsView, meta: { requiresAdmin: true } },
  { path: '/admin/guidelines', name: 'admin-guidelines', component: AdminGuidelinesView, meta: { requiresAdmin: true } },
  { path: '/admin/recipes', name: 'admin-recipes', component: AdminRecipesView, meta: { requiresAdmin: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.ready) {
    await auth.fetchSession()
  }
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !auth.hasRole('ADMIN')) {
    return auth.isAuthenticated ? { name: 'home' } : { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'account' }
  }
  return true
})

export default router
