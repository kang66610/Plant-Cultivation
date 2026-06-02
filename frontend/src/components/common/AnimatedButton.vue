<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'

interface Props {
  text: string
  variant?: 'primary' | 'secondary'
  to?: string
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  to: undefined,
})

const ripples = ref<Array<{ id: number; x: number; y: number }>>([])
let rippleCounter = 0

function createRipple(e: MouseEvent) {
  const target = e.currentTarget as HTMLElement
  const rect = target.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top

  const id = ++rippleCounter
  ripples.value.push({ id, x, y })

  setTimeout(() => {
    ripples.value = ripples.value.filter((r) => r.id !== id)
  }, 700)
}
</script>

<template>
  <component
    :is="to ? RouterLink : 'button'"
    :to="to"
    class="animated-btn"
    :class="[
      `animated-btn--${variant}`,
    ]"
    @click="createRipple"
  >
    <span class="animated-btn__content">
      <span class="animated-btn__text">{{ text }}</span>
      <!-- Leaf SVG that draws in on hover -->
      <svg
        class="animated-btn__leaf"
        viewBox="0 0 20 20"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          class="animated-btn__leaf-path"
          d="M3 17C3 17 4 10 10 6C16 2 17 3 17 3C17 3 16 10 10 14C4 18 3 17 3 17Z"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          class="animated-btn__leaf-vein"
          d="M10 14C10 14 8 10 6 9"
          stroke="currentColor"
          stroke-width="1"
          stroke-linecap="round"
          opacity="0.5"
        />
      </svg>
    </span>

    <!-- Ripple effects -->
    <span
      v-for="ripple in ripples"
      :key="ripple.id"
      class="animated-btn__ripple"
      :style="{ left: `${ripple.x}px`, top: `${ripple.y}px` }"
    ></span>
  </component>
</template>

<style scoped lang="scss">
.animated-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 1.75rem;
  font-size: 0.9375rem;
  font-weight: 600;
  text-decoration: none;
  border: none;
  border-radius: 0.75rem;
  cursor: pointer;
  overflow: hidden;
  transition:
    transform 0.3s var(--ease-spring),
    box-shadow 0.3s var(--ease-smooth);
  outline: none;

  &:focus-visible {
    outline: 2px solid var(--color-primary);
    outline-offset: 2px;
  }

  &:hover {
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(0);
  }

  // ── Primary Variant ──────────────────────────────────────
  &--primary {
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
    color: #ffffff;
    box-shadow:
      0 2px 8px rgba(34, 197, 94, 0.25),
      0 1px 2px rgba(0, 0, 0, 0.05);

    &:hover {
      box-shadow:
        0 4px 16px rgba(34, 197, 94, 0.35),
        0 2px 4px rgba(0, 0, 0, 0.05);
    }

    .animated-btn__leaf {
      color: rgba(255, 255, 255, 0.8);
    }

    .animated-btn__ripple {
      background: rgba(255, 255, 255, 0.3);
    }
  }

  // ── Secondary Variant ────────────────────────────────────
  &--secondary {
    background: transparent;
    color: var(--color-primary-dark);
    border: 2px solid var(--color-primary);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);

    &:hover {
      background: rgba(34, 197, 94, 0.06);
      box-shadow:
        0 4px 12px rgba(34, 197, 94, 0.12),
        0 2px 4px rgba(0, 0, 0, 0.04);
    }

    .animated-btn__leaf {
      color: var(--color-primary);
    }

    .animated-btn__ripple {
      background: rgba(34, 197, 94, 0.15);
    }
  }

  // ── Content ──────────────────────────────────────────────
  &__content {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    position: relative;
    z-index: 1;
  }

  &__text {
    white-space: nowrap;
  }

  // ── Leaf SVG ─────────────────────────────────────────────
  &__leaf {
    width: 18px;
    height: 18px;
    opacity: 0;
    transform: translateX(-8px) rotate(-20deg);
    transition:
      opacity 0.35s var(--ease-smooth),
      transform 0.4s var(--ease-spring);
  }

  &__leaf-path {
    stroke-dasharray: 60;
    stroke-dashoffset: 60;
    transition: stroke-dashoffset 0.6s var(--ease-smooth) 0.1s;
  }

  &__leaf-vein {
    stroke-dasharray: 20;
    stroke-dashoffset: 20;
    transition: stroke-dashoffset 0.5s var(--ease-smooth) 0.3s;
  }

  &:hover &__leaf {
    opacity: 1;
    transform: translateX(0) rotate(0deg);
  }

  &:hover &__leaf-path {
    stroke-dashoffset: 0;
  }

  &:hover &__leaf-vein {
    stroke-dashoffset: 0;
  }

  // ── Ripple ───────────────────────────────────────────────
  &__ripple {
    position: absolute;
    width: 10px;
    height: 10px;
    margin-left: -5px;
    margin-top: -5px;
    border-radius: 50%;
    pointer-events: none;
    animation: ripple-expand 0.7s var(--ease-smooth) forwards;
    z-index: 0;
  }
}

@keyframes ripple-expand {
  0% {
    transform: scale(0);
    opacity: 1;
  }
  100% {
    transform: scale(25);
    opacity: 0;
  }
}
</style>
