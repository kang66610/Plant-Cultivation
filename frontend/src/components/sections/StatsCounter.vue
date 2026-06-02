<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const stats = computed(() => [
  { target: 51, suffix: '+', label: t('stats.species') },
  { target: 100, suffix: '+', label: t('stats.tips') },
  { target: 28, suffix: '+', label: t('stats.airPurifying') },
])

const counts = ref<number[]>(stats.value.map(() => 0))
const visible = ref(false)
const sectionRef = ref<HTMLElement>()

function animateCount(index: number, target: number) {
  const duration = 2000
  const start = performance.now()

  function update(now: number) {
    const elapsed = now - start
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3) // easeOutCubic
    counts.value[index] = Math.floor(eased * target)

    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }

  requestAnimationFrame(update)
}

onMounted(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting && !visible.value) {
        visible.value = true
        stats.value.forEach((stat, i) => {
          setTimeout(() => animateCount(i, stat.target), i * 200)
        })
      }
    },
    { threshold: 0.3 }
  )

  if (sectionRef.value) {
    observer.observe(sectionRef.value)
  }
})

function formatNumber(n: number): string {
  return n.toLocaleString()
}
</script>

<template>
  <section ref="sectionRef" class="stats" :class="{ 'stats--visible': visible }">
    <div class="stats__pattern" />
    <div class="stats__grid">
      <div v-for="(stat, i) in stats" :key="i" class="stats__item" :style="{ transitionDelay: `${i * 0.15}s` }">
        <div class="stats__icon-wrap">
          <svg v-if="i === 0" class="stats__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c1.5 0 3-.3 4.3-.9C10 18 6 14 6 12c0-4.4 3.6-8 8-8 .7 0 1.4.1 2 .2C15 3.5 13.5 2 12 2z"/><path d="M18 4c-2 2-3 5-3 8s1 6 3 8"/></svg>
          <svg v-else-if="i === 1" class="stats__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>
          <svg v-else class="stats__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 22c-4.97 0-9-2.69-9-6v-2c0-3.31 4.03-6 9-6s9 2.69 9 6v2c0 3.31-4.03 6-9 6z"/><path d="M12 8V2m0 0L9 5m3-3l3 3"/><path d="M7.5 12.5c2-1.5 3.5-2 4.5-2s2.5.5 4.5 2"/><path d="M5 15c2.5-2 4.5-2.5 7-2.5s4.5.5 7 2.5"/></svg>
        </div>
        <span class="stats__number">
          {{ formatNumber(counts[i]) }}{{ stat.suffix }}
        </span>
        <span class="stats__label">{{ stat.label }}</span>
      </div>
    </div>
    <div class="stats__divider" />
  </section>
</template>

<style scoped lang="scss">
.stats {
  padding: 4rem 1.5rem;
  background: $color-leaf-900;
  position: relative;
  overflow: hidden;

  &__pattern {
    position: absolute;
    inset: 0;
    background-image:
      radial-gradient(circle at 20% 50%, rgba(74, 222, 128, 0.06) 0%, transparent 50%),
      radial-gradient(circle at 80% 50%, rgba(74, 222, 128, 0.04) 0%, transparent 50%);
    pointer-events: none;
  }

  &__grid {
    position: relative;
    display: flex;
    justify-content: center;
    gap: 4rem;
    max-width: 900px;
    margin: 0 auto;
    flex-wrap: wrap;
  }

  &__item {
    text-align: center;
    min-width: 160px;
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.6s $ease-out-expo;
  }

  &--visible &__item {
    opacity: 1;
    transform: translateY(0);
  }

  &__icon-wrap {
    width: 48px;
    height: 48px;
    margin: 0 auto 1rem;
    border-radius: 50%;
    background: rgba(74, 222, 128, 0.1);
    border: 1px solid rgba(74, 222, 128, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__icon {
    width: 24px;
    height: 24px;
    color: $color-leaf-400;
  }

  &__number {
    display: block;
    font-family: $font-display;
    font-size: clamp(2rem, 4vw, 3rem);
    font-weight: 700;
    color: $color-leaf-400;
    line-height: 1;
    margin-bottom: 0.5rem;
  }

  &__label {
    font-size: 0.85rem;
    color: rgba(240, 253, 244, 0.6);
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }

  &__divider {
    position: relative;
    width: 60px;
    height: 2px;
    margin: 2.5rem auto 0;
    background: linear-gradient(90deg, transparent, $color-leaf-500, transparent);
    opacity: 0.4;
  }
}
</style>
