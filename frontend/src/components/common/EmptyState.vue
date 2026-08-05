<script setup lang="ts">
defineProps<{
  title?: string
  text?: string
}>()
</script>

<template>
  <div class="empty-state">
    <div class="empty-state__mark">+</div>
    <h3 v-if="title">{{ title }}</h3>
    <p v-if="text">{{ text }}</p>
    <slot />
  </div>
</template>

<style scoped lang="scss">
.empty-state {
  width: 100%;
  min-height: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  text-align: center;
  color: $color-text-muted;

  &__mark {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    border: 1px solid rgba(22, 163, 74, 0.22);
    background: $color-leaf-50;
    color: $color-leaf-600;
    display: grid;
    place-items: center;
    font-size: 1.5rem;
    line-height: 1;
  }

  h3 {
    color: $color-leaf-900;
    font-size: 1rem;
  }
}
</style>
