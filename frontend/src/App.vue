<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const router = useRouter()

async function logout() {
  await auth.logout()
  router.push({ name: 'home' })
}
</script>

<template>
  <nav class="topbar">
    <RouterLink class="brand" to="/">SHAKER</RouterLink>
    <RouterLink to="/guidelines">Guidelines</RouterLink>
    <RouterLink v-if="auth.isAuthenticated" to="/diary">Diary</RouterLink>
    <span class="spacer" />
    <template v-if="auth.ready && auth.isAuthenticated">
      <RouterLink to="/account">{{ auth.user.displayName }}</RouterLink>
      <button class="ghost" style="margin: 0" @click="logout">Log out</button>
    </template>
    <template v-else-if="auth.ready">
      <RouterLink to="/login">Log in</RouterLink>
      <RouterLink to="/signup">Sign up</RouterLink>
    </template>
  </nav>

  <div class="container">
    <RouterView />
  </div>
</template>
