<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import AdminNav from './AdminNav.vue'
import { INGREDIENT_CATEGORY, OPACITY, titleCase, tagsToText, textToTags } from '../../constants/enums'

const ingredients = ref([])
const editingId = ref(null)
const error = ref('')
const busy = ref(false)

const emptyForm = () => ({
  name: '',
  aliasesText: '',
  category: 'SPIRIT',
  subCategory: '',
  abvPercent: 0,
  colorHex: '#FFFFFF',
  opacity: 'CLEAR',
  relativeDensity: null,
  allergenTagsText: '',
  caloriesPerOz: null
})
const form = ref(emptyForm())

async function load() {
  const { data } = await api.get('/ingredients')
  ingredients.value = data
}

function edit(ingredient) {
  editingId.value = ingredient.id
  form.value = {
    name: ingredient.name,
    aliasesText: tagsToText(ingredient.aliases),
    category: ingredient.category,
    subCategory: ingredient.subCategory || '',
    abvPercent: ingredient.abvPercent,
    colorHex: ingredient.colorHex,
    opacity: ingredient.opacity,
    relativeDensity: ingredient.relativeDensity,
    allergenTagsText: tagsToText(ingredient.allergenTags),
    caloriesPerOz: ingredient.caloriesPerOz
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
    name: form.value.name,
    aliases: textToTags(form.value.aliasesText),
    category: form.value.category,
    subCategory: form.value.subCategory || null,
    abvPercent: Number(form.value.abvPercent) || 0,
    colorHex: form.value.colorHex,
    opacity: form.value.opacity,
    relativeDensity: form.value.relativeDensity !== null && form.value.relativeDensity !== ''
      ? Number(form.value.relativeDensity) : null,
    allergenTags: textToTags(form.value.allergenTagsText),
    caloriesPerOz: form.value.caloriesPerOz !== null && form.value.caloriesPerOz !== ''
      ? Number(form.value.caloriesPerOz) : null
  }
  try {
    if (editingId.value) {
      await api.put(`/ingredients/${editingId.value}`, payload)
    } else {
      await api.post('/ingredients', payload)
    }
    cancelEdit()
    await load()
  } catch (e) {
    error.value = e.response?.data?.message || 'Could not save ingredient.'
  } finally {
    busy.value = false
  }
}

async function remove(id) {
  await api.delete(`/ingredients/${id}`)
  await load()
}

onMounted(load)
</script>

<template>
  <h1>Admin</h1>
  <AdminNav />

  <h2 style="font-size: 1.1rem">Ingredients</h2>
  <p v-if="error" class="error">{{ error }}</p>

  <form class="card" @submit.prevent="submit">
    <strong>{{ editingId ? 'Edit ingredient' : 'Add ingredient' }}</strong>
    <div class="row-form">
      <div>
        <label>Name</label>
        <input v-model="form.name" required />
      </div>
      <div>
        <label>Category</label>
        <select v-model="form.category">
          <option v-for="c in INGREDIENT_CATEGORY" :key="c" :value="c">{{ titleCase(c) }}</option>
        </select>
      </div>
      <div>
        <label>Sub-category</label>
        <input v-model="form.subCategory" placeholder="e.g. gin, sweet_vermouth" />
      </div>
      <div>
        <label>ABV %</label>
        <input v-model="form.abvPercent" type="number" step="0.1" min="0" max="100" />
      </div>
      <div>
        <label>Color</label>
        <input v-model="form.colorHex" type="color" style="padding: 2px; height: 38px" />
      </div>
      <div>
        <label>Opacity</label>
        <select v-model="form.opacity">
          <option v-for="o in OPACITY" :key="o" :value="o">{{ titleCase(o) }}</option>
        </select>
      </div>
      <div>
        <label>Relative density <span class="muted">(optional)</span></label>
        <input v-model="form.relativeDensity" type="number" step="0.01" />
      </div>
      <div>
        <label>Calories / oz <span class="muted">(optional)</span></label>
        <input v-model="form.caloriesPerOz" type="number" step="0.1" />
      </div>
    </div>
    <label>Aliases <span class="muted">(comma separated)</span></label>
    <input v-model="form.aliasesText" />
    <label>Allergen tags <span class="muted">(comma separated, e.g. dairy, egg)</span></label>
    <input v-model="form.allergenTagsText" />

    <button :disabled="busy">{{ editingId ? 'Save changes' : 'Add ingredient' }}</button>
    <button v-if="editingId" type="button" class="ghost" @click="cancelEdit">Cancel</button>
  </form>

  <div class="card" style="overflow-x: auto">
    <table class="admin-table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Category</th>
          <th>ABV %</th>
          <th>Color</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="i in ingredients" :key="i.id">
          <td>{{ i.name }}</td>
          <td>{{ titleCase(i.category) }}<span v-if="i.subCategory" class="muted"> / {{ i.subCategory }}</span></td>
          <td>{{ i.abvPercent }}</td>
          <td><span class="color-swatch" :style="{ background: i.colorHex }" />{{ i.colorHex }}</td>
          <td>
            <button class="ghost" style="margin: 0" @click="edit(i)">Edit</button>
            <button class="ghost" style="margin: 0 0 0 6px" @click="remove(i.id)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
