<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const entries = ref([])
const form = ref({ drinkName: '', rating: 5, notes: '' })
const error = ref('')
const busy = ref(false)

async function load() {
  const { data } = await api.get('/diary')
  entries.value = data
}

async function add() {
  error.value = ''
  busy.value = true
  try {
    await api.post('/diary', {
      drinkName: form.value.drinkName,
      rating: Number(form.value.rating),
      notes: form.value.notes
    })
    form.value = { drinkName: '', rating: 5, notes: '' }
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Could not add entry.'
  } finally {
    busy.value = false
  }
}

async function remove(id) {
  await api.delete(`/diary/${id}`)
  await load()
}

onMounted(load)
</script>

<template>
  <h1>Diary</h1>

  <form class="card" @submit.prevent="add">
    <label>Drink</label>
    <input v-model="form.drinkName" required />
    <label>Rating (1-5)</label>
    <input v-model="form.rating" type="number" min="1" max="5" />
    <label>Notes</label>
    <textarea v-model="form.notes" />
    <p v-if="error" class="error">{{ error }}</p>
    <button :disabled="busy">Log it</button>
  </form>

  <p v-if="!entries.length" class="muted">Nothing logged yet.</p>

  <div v-for="e in entries" :key="e.id" class="card">
    <strong>{{ e.drinkName }}</strong>
    <span class="muted"> &middot; {{ '★'.repeat(e.rating || 0) }}</span>
    <span class="muted"> &middot; {{ e.loggedOn }}</span>
    <p v-if="e.notes" style="margin: 8px 0 0">{{ e.notes }}</p>
    <button class="ghost" style="margin-top: 10px" @click="remove(e.id)">Delete</button>
  </div>
</template>
