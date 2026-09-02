<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()

const form = ref({
  displayName: auth.user?.displayName ?? '',
  bio: auth.user?.bio ?? ''
})
const status = ref('')
const error = ref('')
const busy = ref(false)

async function save() {
  status.value = ''
  error.value = ''
  busy.value = true
  try {
    await auth.updateAccount({ displayName: form.value.displayName, bio: form.value.bio })
    status.value = 'Saved.'
  } catch (e) {
    error.value = e.response?.data?.message || 'Could not save.'
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <h1>Account</h1>

  <div class="card">
    <div class="muted">@{{ auth.user.username }} &middot; {{ auth.user.email }}</div>
    <div style="margin-top: 8px">
      <span v-for="r in auth.user.roles" :key="r" class="pill">{{ r }}</span>
    </div>
    <div class="muted" style="margin-top: 8px">
      Joined {{ new Date(auth.user.createdAt).toLocaleDateString() }}
    </div>
  </div>

  <form class="card" @submit.prevent="save">
    <label>Display name</label>
    <input v-model="form.displayName" maxlength="60" />
    <label>Bio</label>
    <textarea v-model="form.bio" maxlength="500" />
    <p v-if="status" class="muted">{{ status }}</p>
    <p v-if="error" class="error">{{ error }}</p>
    <button :disabled="busy">{{ busy ? '...' : 'Save changes' }}</button>
  </form>
</template>
