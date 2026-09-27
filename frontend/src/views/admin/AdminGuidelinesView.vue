<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import AdminNav from './AdminNav.vue'
import { GUIDELINE_CONTENT_TYPE, GUIDELINE_AUTHOR_TYPE, titleCase, tagsToText, textToTags } from '../../constants/enums'

const guidelines = ref([])
const editingId = ref(null)
const error = ref('')
const busy = ref(false)

const emptyForm = () => ({
  slug: '',
  title: '',
  category: '',
  body: '',
  sortOrder: 0,
  contentType: 'ARTICLE',
  authorType: 'EDITORIAL',
  authorUsername: '',
  videoUrl: '',
  relatedRecipeId: '',
  relatedSpiritTagsText: ''
})
const form = ref(emptyForm())

async function load() {
  const { data } = await api.get('/guidelines')
  guidelines.value = data
}

function edit(g) {
  editingId.value = g.id
  form.value = {
    slug: g.slug,
    title: g.title,
    category: g.category || '',
    body: g.body,
    sortOrder: g.sortOrder,
    contentType: g.contentType,
    authorType: g.authorType,
    authorUsername: g.authorUsername || '',
    videoUrl: g.videoUrl || '',
    relatedRecipeId: g.relatedRecipeId || '',
    relatedSpiritTagsText: tagsToText(g.relatedSpiritTags)
  }
}

function cancelEdit() {
  editingId.value = null
  form.value = emptyForm()
}

async function submit() {
  error.value = ''
  busy.value = true
  const payload = {
    slug: form.value.slug,
    title: form.value.title,
    category: form.value.category || null,
    body: form.value.body,
    sortOrder: Number(form.value.sortOrder) || 0,
    contentType: form.value.contentType,
    authorType: form.value.authorType,
    authorUsername: form.value.authorUsername || null,
    videoUrl: form.value.videoUrl || null,
    relatedRecipeId: form.value.relatedRecipeId || null,
    relatedSpiritTags: textToTags(form.value.relatedSpiritTagsText)
  }
  try {
    if (editingId.value) {
      await api.put(`/guidelines/${editingId.value}`, payload)
    } else {
      await api.post('/guidelines', payload)
    }
    cancelEdit()
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Could not save guideline.'
  } finally {
    busy.value = false
  }
}

async function remove(id) {
  await api.delete(`/guidelines/${id}`)
  await load()
}

async function moderate(id, status) {
  await api.put(`/guidelines/${id}/moderation-status`, { status })
  await load()
}

onMounted(load)
</script>

<template>
  <h1>Admin</h1>
  <AdminNav />

  <h2 style="font-size: 1.1rem">Guidelines</h2>
  <p v-if="error" class="error">{{ error }}</p>

  <form class="card" @submit.prevent="submit">
    <strong>{{ editingId ? 'Edit guideline' : 'Add guideline' }}</strong>
    <div class="row-form">
      <div>
        <label>Slug</label>
        <input v-model="form.slug" required :disabled="!!editingId" />
      </div>
      <div>
        <label>Title</label>
        <input v-model="form.title" required />
      </div>
      <div>
        <label>Category</label>
        <input v-model="form.category" placeholder="e.g. Technique" />
      </div>
      <div>
        <label>Sort order</label>
        <input v-model="form.sortOrder" type="number" />
      </div>
      <div>
        <label>Content type</label>
        <select v-model="form.contentType">
          <option v-for="c in GUIDELINE_CONTENT_TYPE" :key="c" :value="c">{{ titleCase(c) }}</option>
        </select>
      </div>
      <div>
        <label>Author type</label>
        <select v-model="form.authorType">
          <option v-for="a in GUIDELINE_AUTHOR_TYPE" :key="a" :value="a">{{ titleCase(a) }}</option>
        </select>
      </div>
      <div>
        <label>Author username <span class="muted">(optional)</span></label>
        <input v-model="form.authorUsername" />
      </div>
      <div>
        <label>Video URL <span class="muted">(if video)</span></label>
        <input v-model="form.videoUrl" />
      </div>
    </div>
    <label>Body</label>
    <textarea v-model="form.body" required />
    <label>Related spirit tags <span class="muted">(comma separated, e.g. gin, rum)</span></label>
    <input v-model="form.relatedSpiritTagsText" />

    <button :disabled="busy">{{ editingId ? 'Save changes' : 'Add guideline' }}</button>
    <button v-if="editingId" type="button" class="ghost" @click="cancelEdit">Cancel</button>
  </form>

  <div class="card" style="overflow-x: auto">
    <table class="admin-table">
      <thead>
        <tr>
          <th>Slug</th>
          <th>Title</th>
          <th>Type</th>
          <th>Status</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="g in guidelines" :key="g.id">
          <td>{{ g.slug }}</td>
          <td>{{ g.title }}</td>
          <td>{{ titleCase(g.contentType) }}</td>
          <td>
            <span class="pill">{{ g.moderationStatus }}</span>
          </td>
          <td>
            <template v-if="g.moderationStatus === 'PENDING'">
              <button class="ghost" style="margin: 0" @click="moderate(g.id, 'APPROVED')">Approve</button>
              <button class="ghost" style="margin: 0 0 0 6px" @click="moderate(g.id, 'REJECTED')">Reject</button>
            </template>
            <button class="ghost" style="margin: 0 0 0 6px" @click="edit(g)">Edit</button>
            <button class="ghost" style="margin: 0 0 0 6px" @click="remove(g.id)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
