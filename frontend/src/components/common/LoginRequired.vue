<script setup lang="ts">
defineProps<{
  text?: string
  actionText?: string
}>()

const emit = defineEmits<{
  action: []
}>()
</script>

<template>
  <div class="login-required">
    <p>{{ text || '登录后可使用此功能' }}</p>
    <button @click="emit('action')">{{ actionText || '登录' }}</button>
  </div>
</template>

<style scoped lang="scss">
.login-required {
  min-height: 220px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 1rem;
  color: $color-text-muted;

  button {
    padding: 0.7rem 1.6rem;
    border: none;
    border-radius: 0.7rem;
    background: $color-leaf-600;
    color: white;
    font-weight: 700;
    cursor: pointer;

    &:hover {
      background: $color-leaf-700;
    }
  }
}
</style>
