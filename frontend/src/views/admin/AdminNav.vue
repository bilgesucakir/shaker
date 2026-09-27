<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <nav class="admin-tabs">
    <RouterLink to="/admin/users">Users</RouterLink>
    <RouterLink to="/admin/ingredients">Ingredients</RouterLink>
    <RouterLink to="/admin/guidelines">Guidelines</RouterLink>
    <RouterLink to="/admin/recipes">Recipes</RouterLink>
  </nav>
</template>
