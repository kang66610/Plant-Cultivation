<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { RouterView } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useAuthStore } from '@/stores/auth'
import { useBackendAutoReload } from '@/composables/useBackendAutoReload'

const auth = useAuthStore()

// 开发模式：后端重启完成后自动刷新页面（改 Java 代码 → 重启后端 → 页面自动刷新）
useBackendAutoReload()

function handleAuthExpired() {
  // 令牌已被服务端判定失效（过期 / 版本变化），只需清理本地状态，无需再请求登出接口
  auth.clearSession()
}

onMounted(() => {
  auth.init()
  // request.ts 在 401 时派发的事件：同步清理本地会话（提示已在拦截器里展示）
  window.addEventListener('auth:expired', handleAuthExpired)
})

onUnmounted(() => {
  window.removeEventListener('auth:expired', handleAuthExpired)
})
</script>

<template>
  <div class="app">
    <AppHeader />
    <main class="app__main">
      <RouterView v-slot="{ Component, route }">
        <Transition :name="(route.meta.transition as string) || 'fade'" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>
    <AppFooter />
  </div>
</template>

<style scoped lang="scss">
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  &__main {
    flex: 1;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.3s ease;
}
.slide-left-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
</style>
