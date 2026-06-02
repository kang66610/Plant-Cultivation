<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const tips = computed(() => [
  { icon: '💧', title: t('careTip.waterTitle'), text: t('careTip.waterText') },
  { icon: '☀️', title: t('careTip.lightTitle'), text: t('careTip.lightText') },
  { icon: '🌡️', title: t('careTip.tempTitle'), text: t('careTip.tempText') },
  { icon: '🌱', title: t('careTip.feedTitle'), text: t('careTip.feedText') },
])

const currentTip = ref(0)
const visible = ref(true)

onMounted(() => {
  setInterval(() => {
    visible.value = false
    setTimeout(() => {
      currentTip.value = (currentTip.value + 1) % tips.value.length
      visible.value = true
    }, 400)
  }, 5000)
})
</script>

<template>
  <section class="tip-banner">
    <div class="tip-banner__inner">
      <div class="tip-banner__icon">
        <svg viewBox="0 0 100 120" class="tip-banner__plant">
          <path
            d="M50 110 L50 60 C50 40 30 30 20 20 M50 60 C50 40 70 30 80 20 M50 80 C40 70 25 65 15 60 M50 80 C60 70 75 65 85 60"
            fill="none"
            stroke="#74c69d"
            stroke-width="3"
            stroke-linecap="round"
            class="tip-banner__plant-path"
          />
          <circle cx="50" cy="110" r="3" fill="#74c69d" />
        </svg>
      </div>

      <div class="tip-banner__content" :class="{ 'tip-banner__content--hidden': !visible }">
        <span class="tip-banner__emoji">{{ tips[currentTip].icon }}</span>
        <div>
          <h3 class="tip-banner__title">{{ tips[currentTip].title }}</h3>
          <p class="tip-banner__text">{{ tips[currentTip].text }}</p>
        </div>
      </div>

      <div class="tip-banner__dots">
        <span
          v-for="(_, i) in tips"
          :key="i"
          class="tip-banner__dot"
          :class="{ 'tip-banner__dot--active': i === currentTip }"
          @click="currentTip = i"
        />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.tip-banner {
  padding: 3rem 1.5rem;
  background: linear-gradient(135deg, $color-leaf-800, $color-leaf-900);

  &__inner {
    max-width: 800px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    gap: 2rem;
    position: relative;
  }

  &__icon {
    flex-shrink: 0;
  }

  &__plant {
    width: 60px;
    height: 72px;
  }

  &__plant-path {
    stroke-dasharray: 300;
    stroke-dashoffset: 300;
    animation: drawPlant 3s ease forwards;
  }

  &__content {
    flex: 1;
    display: flex;
    align-items: flex-start;
    gap: 1rem;
    opacity: 1;
    transform: translateY(0);
    transition: all 0.4s ease;

    &--hidden {
      opacity: 0;
      transform: translateY(8px);
    }
  }

  &__emoji {
    font-size: 2rem;
    flex-shrink: 0;
  }

  &__title {
    font-family: $font-display;
    font-size: 1.1rem;
    color: $color-leaf-300;
    margin-bottom: 0.25rem;
  }

  &__text {
    font-size: 0.9rem;
    color: rgba(240, 253, 244, 0.8);
    line-height: 1.5;
  }

  &__dots {
    display: flex;
    gap: 0.5rem;
    position: absolute;
    bottom: -1.5rem;
    left: 50%;
    transform: translateX(-50%);
  }

  &__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: rgba(240, 253, 244, 0.3);
    cursor: pointer;
    transition: all 0.3s ease;

    &--active {
      background: $color-leaf-400;
      transform: scale(1.2);
    }

    &:hover {
      background: rgba(240, 253, 244, 0.6);
    }
  }
}

@keyframes drawPlant {
  to {
    stroke-dashoffset: 0;
  }
}
</style>
