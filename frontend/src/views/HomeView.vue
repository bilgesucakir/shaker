<script setup>
import { RouterLink } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
</script>

<template>
  <h1>Shaker</h1>
  <p class="muted">A cocktail diary - log what you drink, keep recipes, learn the craft.</p>

  <div class="card">
    <p v-if="auth.isAuthenticated">
      Welcome back, <strong>{{ auth.user.displayName }}</strong>.
      Head to your <RouterLink to="/diary">diary</RouterLink> or
      <RouterLink to="/account">account</RouterLink>.
    </p>
    <p v-else>
      <RouterLink to="/signup">Create an account</RouterLink> to start your diary, or
      browse the <RouterLink to="/guidelines">guidelines</RouterLink> - those are open to everyone.
    </p>
  </div>

  <div class="card">
    <h2 style="margin-top: 0; font-size: 1.05rem">Coming later</h2>
    <p class="muted" style="margin: 0">Recipes, events, following other people, ratings and reviews.</p>
  </div>
</template>
