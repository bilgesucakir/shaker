<script setup>
import { ref, onMounted } from 'vue'
import api from '../../api'
import AdminNav from './AdminNav.vue'
import {
  RECIPE_CATEGORY, GLASS, ICE_STYLE, PREPARATION_METHOD, TASTE_NOTE, DIFFICULTY,
  INGREDIENT_ROLE, MEASUREMENT_UNIT, GARNISH_TYPE, titleCase
} from '../../constants/enums'

const recipes = ref([])
const ingredientCatalog = ref([])
const error = ref('')
const busy = ref(false)

function emptyIngredientLine(sequence) {
  return { ingredientRef: '', role: 'BASE_SPIRIT', amount: 1, unit: 'OZ', preparationNote: '', optional: false, sequence }
}
function emptyGarnish() {
  return { description: '', type: 'CITRUS_TWIST' }
}
function emptyForm() {
  return {
    name: '',
    category: 'UNFORGETTABLES',
    glass: 'ROCKS',
    ice: 'ROCKS_CUBED',
    method: 'STIRRED',
    servings: 1,
    difficulty: 'EASY',
    description: '',
    tasteProfile: [],
    tagsText: '',
    instructionsText: '',
    ingredients: [emptyIngredientLine(1)],
    garnishes: []
  }
}
const form = ref(emptyForm())

async function loadRecipes() {
  const { data } = await api.get('/recipes', { params: { all: true } })
  recipes.value = data
}

async function loadIngredients() {
  const { data } = await api.get('/ingredients')
  ingredientCatalog.value = data
}

function addIngredientLine() {
  form.value.ingredients.push(emptyIngredientLine(form.value.ingredients.length + 1))
}
function removeIngredientLine(index) {
  form.value.ingredients.splice(index, 1)
}
function addGarnish() {
  form.value.garnishes.push(emptyGarnish())
}
function removeGarnish(index) {
  form.value.garnishes.splice(index, 1)
}

async function submitClassic() {
  error.value = ''
  busy.value = true
  const payload = {
    name: form.value.name,
    category: form.value.category,
    baseSpiritTags: [],
    glass: form.value.glass,
    ice: form.value.ice,
    method: form.value.method,
    servings: Number(form.value.servings) || 1,
    difficulty: form.value.difficulty,
    description: form.value.description || null,
    tasteProfile: form.value.tasteProfile,
    tags: form.value.tagsText.split(',').map((t) => t.trim()).filter(Boolean),
    instructions: form.value.instructionsText.split('\n').map((l) => l.trim()).filter(Boolean),
    photos: [],
    visibility: 'PUBLIC',
    calorieEstimate: null,
    ingredients: form.value.ingredients.map((l) => ({
      ingredientRef: l.ingredientRef,
      role: l.role,
      amount: Number(l.amount),
      unit: l.unit,
      preparationNote: l.preparationNote || null,
      optional: l.optional,
      sequence: l.sequence
    })),
    garnishes: form.value.garnishes
  }
  try {
    await api.post('/recipes', payload, { params: { asClassic: true } })
    form.value = emptyForm()
    await loadRecipes()
  } catch (e) {
    error.value = e.response?.data?.message || 'Could not create recipe.'
  } finally {
    busy.value = false
  }
}

async function setVisibility(id, visibility) {
  await api.put(`/recipes/${id}/visibility`, { visibility })
  await loadRecipes()
}

onMounted(() => {
  loadRecipes()
  loadIngredients()
})
</script>

<template>
  <h1>Admin</h1>
  <AdminNav />

  <h2 style="font-size: 1.1rem">Add a classic recipe</h2>
  <p class="muted">Always saved as a public, owner-less classic (e.g. Gin Tonic, Aperol Spritz).</p>
  <p v-if="error" class="error">{{ error }}</p>

  <form class="card" @submit.prevent="submitClassic">
    <div class="row-form">
      <div>
        <label>Name</label>
        <input v-model="form.name" required />
      </div>
      <div>
        <label>Category</label>
        <select v-model="form.category">
          <option v-for="c in RECIPE_CATEGORY" :key="c" :value="c">{{ titleCase(c) }}</option>
        </select>
      </div>
      <div>
        <label>Glass</label>
        <select v-model="form.glass">
          <option v-for="g in GLASS" :key="g" :value="g">{{ titleCase(g) }}</option>
        </select>
      </div>
      <div>
        <label>Ice</label>
        <select v-model="form.ice">
          <option v-for="i in ICE_STYLE" :key="i" :value="i">{{ titleCase(i) }}</option>
        </select>
      </div>
      <div>
        <label>Method</label>
        <select v-model="form.method">
          <option v-for="m in PREPARATION_METHOD" :key="m" :value="m">{{ titleCase(m) }}</option>
        </select>
      </div>
      <div>
        <label>Difficulty</label>
        <select v-model="form.difficulty">
          <option v-for="d in DIFFICULTY" :key="d" :value="d">{{ titleCase(d) }}</option>
        </select>
      </div>
      <div>
        <label>Servings</label>
        <input v-model="form.servings" type="number" min="1" />
      </div>
    </div>

    <label>Description</label>
    <textarea v-model="form.description" />

    <label>Taste profile</label>
    <div>
      <label v-for="t in TASTE_NOTE" :key="t" style="display: inline-block; width: auto; margin: 4px 12px 4px 0">
        <input v-model="form.tasteProfile" type="checkbox" :value="t" style="width: auto" /> {{ titleCase(t) }}
      </label>
    </div>

    <label>Tags <span class="muted">(comma separated)</span></label>
    <input v-model="form.tagsText" />

    <label>Instructions <span class="muted">(one step per line)</span></label>
    <textarea v-model="form.instructionsText" />

    <label>Ingredients</label>
    <div v-for="(line, idx) in form.ingredients" :key="idx" class="inline-row">
      <select v-model="line.ingredientRef" required>
        <option value="" disabled>Choose ingredient...</option>
        <option v-for="ing in ingredientCatalog" :key="ing.id" :value="ing.id">{{ ing.name }}</option>
      </select>
      <select v-model="line.role">
        <option v-for="r in INGREDIENT_ROLE" :key="r" :value="r">{{ titleCase(r) }}</option>
      </select>
      <input v-model="line.amount" type="number" step="0.1" min="0" placeholder="Amount" />
      <select v-model="line.unit">
        <option v-for="u in MEASUREMENT_UNIT" :key="u" :value="u">{{ u }}</option>
      </select>
      <input v-model="line.preparationNote" placeholder="Prep note (optional)" />
      <label style="width: auto"><input v-model="line.optional" type="checkbox" style="width: auto" /> optional</label>
      <button type="button" class="ghost" @click="removeIngredientLine(idx)">Remove</button>
    </div>
    <button type="button" class="ghost" @click="addIngredientLine">+ Add ingredient</button>

    <label style="margin-top: 20px">Garnishes</label>
    <div v-for="(g, idx) in form.garnishes" :key="idx" class="inline-row">
      <input v-model="g.description" placeholder="e.g. Orange twist" />
      <select v-model="g.type">
        <option v-for="t in GARNISH_TYPE" :key="t" :value="t">{{ titleCase(t) }}</option>
      </select>
      <button type="button" class="ghost" @click="removeGarnish(idx)">Remove</button>
    </div>
    <button type="button" class="ghost" @click="addGarnish">+ Add garnish</button>

    <div>
      <button :disabled="busy">Create classic recipe</button>
    </div>
  </form>

  <h2 style="font-size: 1.1rem">All recipes</h2>
  <div class="card" style="overflow-x: auto">
    <table class="admin-table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Owner</th>
          <th>Origin</th>
          <th>Visibility</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in recipes" :key="r.id">
          <td>{{ r.name }}</td>
          <td>{{ r.createdBy || 'app' }}</td>
          <td>{{ titleCase(r.origin) }}</td>
          <td><span class="pill">{{ r.visibility }}</span></td>
          <td>
            <button
              v-if="r.visibility === 'PUBLIC'"
              class="ghost"
              style="margin: 0"
              @click="setVisibility(r.id, 'PRIVATE')"
            >
              Unpublish
            </button>
            <button v-else class="ghost" style="margin: 0" @click="setVisibility(r.id, 'PUBLIC')">Publish</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
