<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const guidelines = ref([])
const error = ref('')

onMounted(async () => {
  try {
    const { data } = await api.get('/guidelines')
    guidelines.value = data
  } catch (e) {
    error.value = 'Could not load guidelines.'
  }
})
</script>

<template>
  <h1>Guidelines</h1>
  <p class="muted">Open to everyone, no account needed.</p>

  <p v-if="error" class="error">{{ error }}</p>

  <div v-for="g in guidelines" :key="g.id" class="card">
    <span class="pill">{{ g.category }}</span>
    <h2 style="font-size: 1.05rem; margin: 8px 0 6px">{{ g.title }}</h2>
    <p style="margin: 0">{{ g.body }}</p>
  </div>
</template>
