<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import { useAuthStore } from '../../stores/auth'
import AdminNav from './AdminNav.vue'

const auth = useAuthStore()
const users = ref([])
const error = ref('')
const busyUsername = ref('')

async function load() {
  const { data } = await api.get('/users')
  users.value = data
}

async function grantAdmin(username) {
  error.value = ''
  busyUsername.value = username
  try {
    await api.post(`/users/${username}/roles/ADMIN`)
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Could not update role.'
  } finally {
    busyUsername.value = ''
  }
}

async function revokeAdmin(username) {
  error.value = ''
  busyUsername.value = username
  try {
    await api.delete(`/users/${username}/roles/ADMIN`)
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Could not update role.'
  } finally {
    busyUsername.value = ''
  }
}

onMounted(load)
</script>

<template>
  <h1>Admin</h1>
  <AdminNav />

  <h2 style="font-size: 1.1rem">Users</h2>
  <p v-if="error" class="error">{{ error }}</p>

  <div class="card" style="overflow-x: auto">
    <table class="admin-table">
      <thead>
        <tr>
          <th>Username</th>
          <th>Email</th>
          <th>Roles</th>
          <th>Joined</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.username">
          <td>{{ u.username }}</td>
          <td>{{ u.email }}</td>
          <td>
            <span v-for="r in u.roles" :key="r" class="pill">{{ r }}</span>
          </td>
          <td>{{ new Date(u.createdAt).toLocaleDateString() }}</td>
          <td>
            <button
              v-if="!u.roles.includes('ADMIN')"
              class="ghost"
              style="margin: 0"
              :disabled="busyUsername === u.username"
              @click="grantAdmin(u.username)"
            >
              Make admin
            </button>
            <button
              v-else-if="u.username !== auth.user?.username"
              class="ghost"
              style="margin: 0"
              :disabled="busyUsername === u.username"
              @click="revokeAdmin(u.username)"
            >
              Revoke admin
            </button>
            <span v-else class="muted">(you)</span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
