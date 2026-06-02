<script setup lang="ts">
import { ref, onMounted } from 'vue'
import gsap from 'gsap'

interface Props {
  type: 'light' | 'water' | 'temp' | 'fertilizer'
  level: number
  label: string
  description?: string
}

const props = withDefaults(defineProps<Props>(), {
  description: undefined,
})

const fillProgress = ref(0)
const indicatorRef = ref<HTMLElement | null>(null)

const typeConfig = {
  light: { color: '#eab308', gradientFrom: '#fde047', gradientTo: '#eab308', icon: 'sun' },
  water: { color: '#3b82f6', gradientFrom: '#93c5fd', gradientTo: '#3b82f6', icon: 'water' },
  temp: { color: '#ef4444', gradientFrom: '#fca5a5', gradientTo: '#ef4444', icon: 'temp' },
  fertilizer: { color: '#22c55e', gradientFrom: '#86efac', gradientTo: '#16a34a', icon: 'fertilizer' },
}

const normalizedLevel = Math.min(4, Math.max(1, props.level))
const percentage = (normalizedLevel / 4) * 100

onMounted(() => {
  gsap.to(fillProgress, {
    value: percentage,
    duration: 1.2,
    ease: 'power3.out',
    delay: 0.2,
  })
})
</script>

<template>
  <div ref="indicatorRef" class="care-indicator">
    <!-- SVG Icon -->
    <div class="care-indicator__icon">
      <!-- Light / Sun -->
      <svg
        v-if="type === 'light'"
        viewBox="0 0 64 64"
        class="care-indicator__svg"
      >
        <defs>
          <linearGradient :id="`grad-${type}`" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" :stop-color="typeConfig.light.gradientFrom" />
            <stop offset="100%" :stop-color="typeConfig.light.gradientTo" />
          </linearGradient>
          <clipPath :id="`clip-${type}`">
            <rect x="0" :y="64 - (fillProgress / 100) * 64" width="64" height="64" />
          </clipPath>
        </defs>
        <!-- Background ring -->
        <circle
          cx="32" cy="32" r="28"
          fill="none"
          stroke="#fef9c3"
          stroke-width="3"
        />
        <!-- Fill circle (clipped) -->
        <circle
          cx="32" cy="32" r="28"
          :fill="`url(#grad-${type})`"
          :clip-path="`url(#clip-${type})`"
          opacity="0.9"
        />
        <!-- Sun rays -->
        <g
          :clip-path="`url(#clip-${type})`"
          :fill="typeConfig.light.color"
          opacity="0.6"
        >
          <rect x="30" y="4" width="4" height="8" rx="2" />
          <rect x="30" y="52" width="4" height="8" rx="2" />
          <rect x="4" y="30" width="8" height="4" rx="2" />
          <rect x="52" y="30" width="8" height="4" rx="2" />
          <rect x="11" y="11" width="4" height="8" rx="2" transform="rotate(45 13 15)" />
          <rect x="49" y="11" width="4" height="8" rx="2" transform="rotate(-45 51 15)" />
          <rect x="11" y="45" width="4" height="8" rx="2" transform="rotate(-45 13 49)" />
          <rect x="49" y="45" width="4" height="8" rx="2" transform="rotate(45 51 49)" />
        </g>
        <!-- Center icon -->
        <circle cx="32" cy="32" r="10" fill="white" opacity="0.3" />
      </svg>

      <!-- Water Drop -->
      <svg
        v-else-if="type === 'water'"
        viewBox="0 0 64 64"
        class="care-indicator__svg"
      >
        <defs>
          <linearGradient :id="`grad-${type}`" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" :stop-color="typeConfig.water.gradientFrom" />
            <stop offset="100%" :stop-color="typeConfig.water.gradientTo" />
          </linearGradient>
          <clipPath :id="`clip-${type}`">
            <rect x="0" :y="64 - (fillProgress / 100) * 64" width="64" height="64" />
          </clipPath>
          <path
            :id="`drop-${type}`"
            d="M32 8C32 8 16 26 16 38C16 46.837 23.163 54 32 54C40.837 54 48 46.837 48 38C48 26 32 8 32 8Z"
          />
        </defs>
        <!-- Background -->
        <use
          :href="`#drop-${type}`"
          fill="none"
          stroke="#dbeafe"
          stroke-width="3"
        />
        <!-- Fill (clipped) -->
        <use
          :href="`#drop-${type}`"
          :fill="`url(#grad-${type})`"
          :clip-path="`url(#clip-${type})`"
          opacity="0.85"
        />
        <!-- Highlight -->
        <ellipse cx="26" cy="32" rx="4" ry="6" fill="white" opacity="0.25" />
      </svg>

      <!-- Temp / Thermometer -->
      <svg
        v-else-if="type === 'temp'"
        viewBox="0 0 64 64"
        class="care-indicator__svg"
      >
        <defs>
          <linearGradient :id="`grad-${type}`" x1="0" y1="1" x2="0" y2="0">
            <stop offset="0%" :stop-color="typeConfig.temp.gradientFrom" />
            <stop offset="100%" :stop-color="typeConfig.temp.gradientTo" />
          </linearGradient>
          <clipPath :id="`clip-${type}`">
            <rect x="0" :y="64 - (fillProgress / 100) * 64" width="64" height="64" />
          </clipPath>
        </defs>
        <!-- Tube outline -->
        <rect x="27" y="8" width="10" height="36" rx="5" fill="none" stroke="#fecaca" stroke-width="2.5" />
        <!-- Bulb -->
        <circle cx="32" cy="50" r="8" fill="none" stroke="#fecaca" stroke-width="2.5" />
        <!-- Mercury fill (tube) -->
        <rect
          x="29"
          :y="44 - (fillProgress / 100) * 34"
          width="6"
          :height="(fillProgress / 100) * 34"
          rx="3"
          :fill="typeConfig.temp.color"
          opacity="0.9"
        />
        <!-- Mercury fill (bulb) - always filled -->
        <circle
          cx="32" cy="50" r="5"
          :fill="typeConfig.temp.color"
          opacity="0.9"
        />
        <!-- Tick marks -->
        <g stroke="#e5e7eb" stroke-width="1" opacity="0.5">
          <line x1="38" y1="14" x2="41" y2="14" />
          <line x1="38" y1="22" x2="41" y2="22" />
          <line x1="38" y1="30" x2="41" y2="30" />
          <line x1="38" y1="38" x2="41" y2="38" />
        </g>
      </svg>

      <!-- Fertilizer / Bottle -->
      <svg
        v-else-if="type === 'fertilizer'"
        viewBox="0 0 64 64"
        class="care-indicator__svg"
      >
        <defs>
          <linearGradient :id="`grad-${type}`" x1="0" y1="1" x2="0" y2="0">
            <stop offset="0%" :stop-color="typeConfig.fertilizer.gradientFrom" />
            <stop offset="100%" :stop-color="typeConfig.fertilizer.gradientTo" />
          </linearGradient>
          <clipPath :id="`clip-${type}`">
            <rect x="0" :y="64 - (fillProgress / 100) * 64" width="64" height="64" />
          </clipPath>
        </defs>
        <!-- Bottle body -->
        <path
          d="M20 20H44V50C44 52.209 42.209 54 40 54H24C21.791 54 20 52.209 20 50V20Z"
          fill="none"
          stroke="#bbf7d0"
          stroke-width="2.5"
          stroke-linejoin="round"
        />
        <!-- Bottle neck -->
        <path
          d="M26 8H38V20H26V8Z"
          fill="none"
          stroke="#bbf7d0"
          stroke-width="2.5"
          stroke-linejoin="round"
        />
        <!-- Cap -->
        <rect x="24" y="4" width="16" height="4" rx="1" fill="#bbf7d0" />
        <!-- Fill level -->
        <path
          d="M21 22H43V50C43 51.657 41.657 53 40 53H24C22.343 53 21 51.657 21 50V22Z"
          :fill="`url(#grad-${type})`"
          :clip-path="`url(#clip-${type})`"
          opacity="0.7"
        />
        <!-- Label -->
        <rect x="24" y="32" width="16" height="8" rx="1" fill="white" opacity="0.2" />
        <text x="32" y="38" text-anchor="middle" font-size="5" fill="white" opacity="0.4" font-weight="bold">NPK</text>
      </svg>
    </div>

    <!-- Info -->
    <div class="care-indicator__info">
      <div class="care-indicator__label-row">
        <span class="care-indicator__label">{{ label }}</span>
        <span class="care-indicator__level">{{ level }}/4</span>
      </div>
      <p v-if="description" class="care-indicator__desc">{{ description }}</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.care-indicator {
  display: flex;
  align-items: center;
  gap: 0.875rem;

  &__icon {
    flex-shrink: 0;
    width: 48px;
    height: 48px;
  }

  &__svg {
    width: 100%;
    height: 100%;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__label-row {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 0.5rem;
    margin-bottom: 0.125rem;
  }

  &__label {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--color-text);
  }

  &__level {
    font-size: 0.75rem;
    font-weight: 500;
    color: var(--color-text-muted);
    font-variant-numeric: tabular-nums;
  }

  &__desc {
    font-size: 0.8125rem;
    color: var(--color-text-muted);
    line-height: 1.4;
    margin: 0;
  }
}
</style>
