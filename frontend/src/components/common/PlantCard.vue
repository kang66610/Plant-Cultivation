<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

interface Plant {
  id: number | string
  commonName: string
  scientificName: string
  slug: string
  shortDescription: string
  imageUrl: string
  difficulty: 'easy' | 'medium' | 'hard'
  lightLevel: number
  waterFrequency: number
}

const props = defineProps<{
  plant: Plant
}>()

const router = useRouter()
const cardRef = ref<HTMLElement | null>(null)
const rotateX = ref(0)
const rotateY = ref(0)
const isHovering = ref(false)

const difficultyConfig = {
  easy: { label: 'Easy', color: 'bg-emerald-100 text-emerald-700' },
  medium: { label: 'Medium', color: 'bg-amber-100 text-amber-700' },
  hard: { label: 'Hard', color: 'bg-red-100 text-red-700' },
}

function handleMouseMove(e: MouseEvent) {
  if (!cardRef.value) return
  const rect = cardRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2

  rotateX.value = ((y - centerY) / centerY) * -6
  rotateY.value = ((x - centerX) / centerX) * 6
}

function handleMouseEnter() {
  isHovering.value = true
}

function handleMouseLeave() {
  isHovering.value = false
  rotateX.value = 0
  rotateY.value = 0
}

function navigateToPlant() {
  router.push(`/plant/${props.plant.slug}`)
}
</script>

<template>
  <article
    ref="cardRef"
    class="plant-card"
    :class="{ 'plant-card--hovering': isHovering }"
    :style="{
      transform: `perspective(800px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`,
    }"
    tabindex="0"
    role="link"
    :aria-label="`View ${plant.commonName} details`"
    @mousemove="handleMouseMove"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    @click="navigateToPlant"
    @keydown.enter="navigateToPlant"
  >
    <!-- Image -->
    <div class="plant-card__image-wrapper">
      <img
        :src="plant.imageUrl"
        :alt="plant.commonName"
        class="plant-card__image"
        loading="lazy"
      />
      <div class="plant-card__image-overlay"></div>
    </div>

    <!-- Content -->
    <div class="plant-card__body">
      <div class="plant-card__header">
        <h3 class="plant-card__name">{{ plant.commonName }}</h3>
        <span
          class="plant-card__badge"
          :class="difficultyConfig[plant.difficulty].color"
        >
          {{ difficultyConfig[plant.difficulty].label }}
        </span>
      </div>

      <p class="plant-card__scientific">{{ plant.scientificName }}</p>
      <p class="plant-card__desc">{{ plant.shortDescription }}</p>

      <!-- Care Icons -->
      <div class="plant-card__care">
        <!-- Light -->
        <div class="plant-card__care-item" title="Light level">
          <svg viewBox="0 0 20 20" fill="none" class="plant-card__care-icon plant-card__care-icon--sun">
            <circle cx="10" cy="10" r="4" fill="currentColor" />
            <path d="M10 1V3M10 17V19M1 10H3M17 10H19M4.22 4.22L5.64 5.64M14.36 14.36L15.78 15.78M4.22 15.78L5.64 14.36M14.36 5.64L15.78 4.22" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <div class="plant-card__care-bar">
            <div
              class="plant-card__care-fill plant-card__care-fill--sun"
              :style="{ width: `${(plant.lightLevel / 4) * 100}%` }"
            ></div>
          </div>
        </div>

        <!-- Water -->
        <div class="plant-card__care-item" title="Water frequency">
          <svg viewBox="0 0 20 20" fill="none" class="plant-card__care-icon plant-card__care-icon--water">
            <path d="M10 2C10 2 4 8.5 4 13C4 16.314 6.686 19 10 19C13.314 19 16 16.314 16 13C16 8.5 10 2 10 2Z" fill="currentColor"/>
          </svg>
          <div class="plant-card__care-bar">
            <div
              class="plant-card__care-fill plant-card__care-fill--water"
              :style="{ width: `${(plant.waterFrequency / 4) * 100}%` }"
            ></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom border animation -->
    <div class="plant-card__border-accent"></div>
  </article>
</template>

<style scoped lang="scss">
.plant-card {
  position: relative;
  background: var(--color-surface);
  border-radius: 1rem;
  overflow: hidden;
  cursor: pointer;
  transition:
    transform 0.4s var(--ease-spring),
    box-shadow 0.4s var(--ease-smooth);
  will-change: transform;
  border: 1px solid var(--color-border);

  &:hover {
    transform: perspective(800px) rotateX(var(--rx, 0deg)) rotateY(var(--ry, 0deg)) translateY(-8px) !important;
    box-shadow:
      0 8px 24px rgba(0, 0, 0, 0.06),
      0 16px 48px rgba(0, 0, 0, 0.04);
  }

  &:focus-visible {
    outline: 2px solid var(--color-primary);
    outline-offset: 2px;
  }

  &--hovering {
    box-shadow:
      0 8px 24px rgba(0, 0, 0, 0.06),
      0 16px 48px rgba(0, 0, 0, 0.04);
  }

  // ── Image ────────────────────────────────────────────────
  &__image-wrapper {
    position: relative;
    aspect-ratio: 4 / 3;
    overflow: hidden;
    background: var(--color-leaf-50, #f0fdf4);
  }

  &__image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s var(--ease-smooth);

    .plant-card:hover & {
      transform: scale(1.05);
    }
  }

  &__image-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(
      to bottom,
      transparent 60%,
      rgba(0, 0, 0, 0.04) 100%
    );
    pointer-events: none;
  }

  // ── Body ─────────────────────────────────────────────────
  &__body {
    padding: 1rem 1.25rem 1.25rem;
  }

  &__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 0.5rem;
    margin-bottom: 0.125rem;
  }

  &__name {
    font-family: var(--font-display);
    font-size: 1.125rem;
    font-weight: 700;
    color: var(--color-text);
    line-height: 1.3;
  }

  &__badge {
    flex-shrink: 0;
    padding: 0.125rem 0.5rem;
    font-size: 0.6875rem;
    font-weight: 600;
    border-radius: 9999px;
    line-height: 1.6;
    white-space: nowrap;
  }

  &__scientific {
    font-style: italic;
    font-size: 0.8125rem;
    color: var(--color-text-muted);
    margin-bottom: 0.5rem;
  }

  &__desc {
    font-size: 0.875rem;
    color: var(--color-text-muted);
    line-height: 1.5;
    margin-bottom: 1rem;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  // ── Care Indicators ──────────────────────────────────────
  &__care {
    display: flex;
    gap: 1rem;
  }

  &__care-item {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    flex: 1;
  }

  &__care-icon {
    width: 18px;
    height: 18px;
    flex-shrink: 0;

    &--sun {
      color: #eab308;
    }

    &--water {
      color: #3b82f6;
    }
  }

  &__care-bar {
    flex: 1;
    height: 4px;
    background: rgba(0, 0, 0, 0.06);
    border-radius: 2px;
    overflow: hidden;
  }

  &__care-fill {
    height: 100%;
    border-radius: 2px;
    transition: width 0.6s var(--ease-spring);

    &--sun {
      background: linear-gradient(90deg, #fde047, #eab308);
    }

    &--water {
      background: linear-gradient(90deg, #93c5fd, #3b82f6);
    }
  }

  // ── Bottom Accent ────────────────────────────────────────
  &__border-accent {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, var(--color-primary-light), var(--color-primary), var(--color-primary-light));
    transform: scaleX(0);
    transform-origin: center;
    transition: transform 0.4s var(--ease-spring);

    .plant-card:hover & {
      transform: scaleX(1);
    }
  }
}
</style>
