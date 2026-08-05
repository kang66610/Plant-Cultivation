<script setup lang="ts">
defineProps<{
  loading?: boolean
  error?: string
  empty?: boolean
  emptyText?: string
  loadingText?: string
}>()
</script>

<template>
  <div v-if="loading" class="api-state">
    <div class="api-state__spinner" />
    <p>{{ loadingText || '加载中...' }}</p>
  </div>
  <div v-else-if="error" class="api-state api-state--error">
    <p>{{ error }}</p>
  </div>
  <div v-else-if="empty" class="api-state">
    <p>{{ emptyText || '暂无数据' }}</p>
  </div>
  <slot v-else />
</template>

<style scoped lang="scss">
.api-state {
  width: 100%;
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  color: $color-text-muted;
  text-align: center;

  &--error {
    color: #dc2626;
  }

  &__spinner {
    width: 34px;
    height: 34px;
    border: 3px solid rgba(22, 163, 74, 0.16);
    border-top-color: $color-leaf-600;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
