<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()

const form = ref({ username: '', email: '', password: '', displayName: '' })
const error = ref('')
const fieldErrors = ref({})
const busy = ref(false)

async function submit() {
  error.value = ''
  fieldErrors.value = {}
  busy.value = true
  try {
    await auth.signup(form.value)
    router.push({ name: 'account' })
  } catch (e) {
    const data = e.response?.data
    if (data?.fields) fieldErrors.value = data.fields
    error.value = data?.message || 'Sign up failed.'
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <h1>Sign up</h1>
  <form class="card" @submit.prevent="submit">
    <label>Username</label>
    <input v-model="form.username" autocomplete="username" required />
    <small v-if="fieldErrors.username" class="error">{{ fieldErrors.username }}</small>

    <label>Email</label>
    <input v-model="form.email" type="email" autocomplete="email" required />
    <small v-if="fieldErrors.email" class="error">{{ fieldErrors.email }}</small>

    <label>Display name <span class="muted">(optional)</span></label>
    <input v-model="form.displayName" autocomplete="nickname" />

    <label>Password <span class="muted">(min 8 characters)</span></label>
    <input v-model="form.password" type="password" autocomplete="new-password" required />
    <small v-if="fieldErrors.password" class="error">{{ fieldErrors.password }}</small>

    <p v-if="error" class="error">{{ error }}</p>
    <button :disabled="busy">{{ busy ? '...' : 'Create account' }}</button>
  </form>
  <p class="muted">Already registered? <RouterLink to="/login">Log in</RouterLink>.</p>
</template>
