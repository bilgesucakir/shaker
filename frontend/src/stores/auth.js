import { defineStore } from 'pinia'
import api from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    ready: false // becomes true once the initial /me check has completed
  }),
  getters: {
    isAuthenticated: (state) => state.user !== null,
    hasRole: (state) => (role) => state.user?.roles?.includes(role) ?? false
  },
  actions: {
    async fetchMe() {
      try {
        const { data } = await api.get('/auth/me')
        this.user = data
      } catch (err) {
        if (err.response && err.response.status === 401) {
          this.user = null
        } else {
          throw err
        }
      } finally {
        this.ready = true
      }
    },
    async login(username, password) {
      const { data } = await api.post('/auth/login', { username, password })
      this.user = data
    },
    async signup(payload) {
      const { data } = await api.post('/auth/signup', payload)
      this.user = data
    },
    async logout() {
      await api.post('/auth/logout')
      this.user = null
    },
    async updateAccount(payload) {
      const { data } = await api.put('/account', payload)
      this.user = data
    }
  }
})
