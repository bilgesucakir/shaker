<script setup>
import { ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const error = ref('')
const busy = ref(false)

async function submit() {
  error.value = ''
  busy.value = true
  try {
    await auth.login(username.value, password.value)
    router.push(route.query.redirect || { name: 'account' })
  } catch (e) {
    error.value = e.response?.data?.message || 'Login failed.'
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <h1>Log in</h1>
  <form class="card" @submit.prevent="submit">
    <label>Username</label>
    <input v-model="username" autocomplete="username" required />
    <label>Password</label>
    <input v-model="password" type="password" autocomplete="current-password" required />
    <p v-if="error" class="error">{{ error }}</p>
    <button :disabled="busy">{{ busy ? '...' : 'Log in' }}</button>
  </form>
  <p class="muted">No account? <RouterLink to="/signup">Sign up</RouterLink>.</p>
</template>
