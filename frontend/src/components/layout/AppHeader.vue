<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { AuthModal } from '@/components/common'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const isScrolled = ref(false)
const isMobileOpen = ref(false)
const isToolsOpen = ref(false)
const showUserMenu = ref(false)

const authModalRef = ref<InstanceType<typeof AuthModal> | null>(null)

function openAuth(mode: 'login' | 'register') {
  authModalRef.value?.open(mode)
}

function handleScroll() {
  isScrolled.value = window.scrollY > 20
}

function closeMobile() {
  isMobileOpen.value = false
  isToolsOpen.value = false
}

function toggleTools() {
  isToolsOpen.value = !isToolsOpen.value
}

function toggleLang() {
  locale.value = locale.value === 'zh-CN' ? 'en' : 'zh-CN'
  localStorage.setItem('lang', locale.value)
}

const langLabel = computed(() => locale.value === 'zh-CN' ? 'EN' : '中文')

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const navLinks = computed(() => [
  { name: t('nav.home'), to: '/' },
  { name: t('nav.encyclopedia'), to: '/encyclopedia' },
  { name: t('nav.community'), to: '/community' },
  { name: t('nav.diary'), to: '/diary' },
  { name: t('nav.about'), to: '/about' },
])

const toolLinks = computed(() => [
  { name: t('nav.wateringCalc'), to: '/tools/watering-calculator' },
  { name: t('nav.lightQuiz'), to: '/tools/light-quiz' },
])
</script>

<template>
  <header
    class="header"
    :class="{ 'header--scrolled': isScrolled }"
  >
    <div class="header__inner">
      <!-- Logo -->
      <RouterLink to="/" class="header__logo" @click="closeMobile">
        <svg
          class="header__logo-icon"
          viewBox="0 0 32 32"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M16 2C16 2 8 6 8 14C8 18.418 11.582 22 16 22C20.418 22 24 18.418 24 14C24 6 16 2 16 2Z"
            fill="url(#leafGradient)"
            stroke="currentColor"
            stroke-width="1.5"
          />
          <path
            d="M16 22V30"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          />
          <path
            d="M16 14C16 14 13 10 10 11"
            stroke="rgba(255,255,255,0.6)"
            stroke-width="1.5"
            stroke-linecap="round"
          />
          <defs>
            <linearGradient id="leafGradient" x1="8" y1="2" x2="24" y2="22">
              <stop stop-color="#4ade80" />
              <stop offset="1" stop-color="#16a34a" />
            </linearGradient>
          </defs>
        </svg>
        <span class="header__logo-text">PlantCultivation</span>
      </RouterLink>

      <!-- Desktop Nav -->
      <nav class="header__nav">
        <RouterLink
          v-for="link in navLinks"
          :key="link.to"
          :to="link.to"
          class="header__nav-link"
          :class="{ 'header__nav-link--active': route.path === link.to }"
        >
          {{ link.name }}
          <span class="header__nav-underline"></span>
        </RouterLink>

        <!-- Tools Dropdown -->
        <div class="header__dropdown" @mouseenter="isToolsOpen = true" @mouseleave="isToolsOpen = false">
          <button
            class="header__nav-link header__dropdown-trigger"
            :class="{ 'header__nav-link--active': route.path.startsWith('/tools') }"
            @click="toggleTools"
          >
            {{ $t('nav.tools') }}
            <svg class="header__dropdown-arrow" :class="{ 'header__dropdown-arrow--open': isToolsOpen }" viewBox="0 0 12 12" fill="none">
              <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="header__nav-underline"></span>
          </button>
          <Transition name="dropdown">
            <div v-if="isToolsOpen" class="header__dropdown-menu">
              <RouterLink
                v-for="tool in toolLinks"
                :key="tool.to"
                :to="tool.to"
                class="header__dropdown-item"
                @click="isToolsOpen = false"
              >
                {{ tool.name }}
              </RouterLink>
            </div>
          </Transition>
        </div>

        <!-- Language Switch -->
        <button class="header__lang-btn" @click="toggleLang" :title="$t('langSwitch')">
          {{ langLabel }}
        </button>

        <!-- Auth Button / User Menu -->
        <div v-if="auth.isLoggedIn" class="header__user-menu" @mouseenter="showUserMenu = true" @mouseleave="showUserMenu = false">
          <button class="header__user-btn">
            <div class="header__user-avatar">
              <img v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" alt="avatar" />
              <span v-else>{{ auth.user?.username?.charAt(0) }}</span>
            </div>
          </button>
          <Transition name="dropdown">
            <div v-if="showUserMenu" class="header__dropdown-menu header__user-dropdown">
              <div class="header__user-info">
                <span class="header__user-name">{{ auth.user?.username }}</span>
                <span class="header__user-account">@{{ auth.user?.account }}</span>
              </div>
              <div class="header__dropdown-divider"></div>
              <RouterLink to="/user-center" class="header__dropdown-item" @click="showUserMenu = false">
                {{ t('nav.userCenter') }}
              </RouterLink>
              <button class="header__dropdown-item header__dropdown-item--danger" @click="auth.logout(); showUserMenu = false">
                {{ t('nav.logout') }}
              </button>
            </div>
          </Transition>
        </div>
        <button v-else class="header__login-btn" @click="openAuth('login')">
          {{ t('nav.login') }}
        </button>
      </nav>

      <!-- Mobile Hamburger -->
      <button
        class="header__hamburger"
        :class="{ 'header__hamburger--open': isMobileOpen }"
        aria-label="Toggle menu"
        @click="isMobileOpen = !isMobileOpen"
      >
        <span></span>
        <span></span>
        <span></span>
      </button>
    </div>

    <!-- Mobile Drawer -->
    <Transition name="drawer">
      <div v-if="isMobileOpen" class="header__drawer-overlay" @click="closeMobile">
        <nav class="header__drawer" @click.stop>
          <RouterLink
            v-for="link in navLinks"
            :key="link.to"
            :to="link.to"
            class="header__drawer-link"
            :class="{ 'header__drawer-link--active': route.path === link.to }"
            @click="closeMobile"
          >
            {{ link.name }}
          </RouterLink>

          <div class="header__drawer-divider"></div>
          <span class="header__drawer-section-title">{{ $t('nav.tools') }}</span>

          <RouterLink
            v-for="tool in toolLinks"
            :key="tool.to"
            :to="tool.to"
            class="header__drawer-link header__drawer-link--sub"
            :class="{ 'header__drawer-link--active': route.path === tool.to }"
            @click="closeMobile"
          >
            {{ tool.name }}
          </RouterLink>

          <div class="header__drawer-divider"></div>
          <button class="header__drawer-lang-btn" @click="toggleLang">
            {{ langLabel }}
          </button>
          <button v-if="auth.isLoggedIn" class="header__drawer-link" @click="closeMobile; $router.push('/user-center')">
            {{ t('nav.userCenter') }}
          </button>
          <button v-if="auth.isLoggedIn" class="header__drawer-link" style="color: #ef4444;" @click="auth.logout(); closeMobile()">
            {{ t('nav.logout') }}
          </button>
          <button v-else class="header__drawer-link" @click="closeMobile; openAuth('login')">
            {{ t('nav.login') }}
          </button>
        </nav>
      </div>
    </Transition>

    <!-- Auth Modal -->
    <AuthModal ref="authModalRef" />
  </header>
</template>

<style scoped lang="scss">
@use 'sass:map';

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 1rem 0;
  transition:
    padding 0.35s var(--ease-smooth),
    box-shadow 0.35s var(--ease-smooth),
    background-color 0.35s var(--ease-smooth);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);

  &--scrolled {
    padding: 0.5rem 0;
    box-shadow:
      0 1px 3px rgba(0, 0, 0, 0.06),
      0 4px 12px rgba(0, 0, 0, 0.04);
    background: rgba(255, 255, 255, 0.85);
  }

  &__inner {
    max-width: 1280px;
    margin: 0 auto;
    padding: 0 1.5rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__logo {
    display: flex;
    align-items: center;
    gap: 0.625rem;
    text-decoration: none;
    color: inherit;

    &:hover {
      color: inherit;
    }
  }

  &__logo-icon {
    width: 32px;
    height: 32px;
    color: var(--color-primary);
    transition: transform 0.4s var(--ease-spring);

    .header__logo:hover & {
      transform: rotate(-8deg) scale(1.08);
    }
  }

  &__logo-text {
    font-family: var(--font-display);
    font-size: 1.25rem;
    font-weight: 700;
    background: linear-gradient(135deg, var(--color-primary-dark), var(--color-primary));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  // ── Desktop Nav ──────────────────────────────────────────
  &__nav {
    display: none;
    align-items: center;
    gap: 0.25rem;

    @media (min-width: 768px) {
      display: flex;
    }
  }

  &__nav-link {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
    padding: 0.5rem 0.875rem;
    font-size: 0.9375rem;
    font-weight: 500;
    color: var(--color-text-muted);
    text-decoration: none;
    background: none;
    border: none;
    cursor: pointer;
    transition: color 0.25s var(--ease-smooth);
    border-radius: 0.5rem;

    &:hover {
      color: var(--color-primary-dark);
    }

    &--active {
      color: var(--color-primary-dark);
    }
  }

  &__nav-underline {
    position: absolute;
    bottom: 0;
    left: 50%;
    width: 0;
    height: 2px;
    background: linear-gradient(90deg, var(--color-primary), var(--color-primary-light));
    border-radius: 1px;
    transform: translateX(-50%);
    transition: width 0.35s var(--ease-spring);

    .header__nav-link:hover &,
    .header__nav-link--active & {
      width: 60%;
    }
  }

  // ── Dropdown ─────────────────────────────────────────────
  &__dropdown {
    position: relative;
  }

  &__dropdown-arrow {
    width: 12px;
    height: 12px;
    transition: transform 0.3s var(--ease-spring);

    &--open {
      transform: rotate(180deg);
    }
  }

  &__dropdown-menu {
    position: absolute;
    top: calc(100% + 0.5rem);
    left: 50%;
    transform: translateX(-50%);
    min-width: 200px;
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(16px);
    border-radius: 0.75rem;
    border: 1px solid var(--color-border);
    box-shadow:
      0 4px 16px rgba(0, 0, 0, 0.06),
      0 8px 32px rgba(0, 0, 0, 0.04);
  }

  &__dropdown-item {
    display: block;
    padding: 0.625rem 1rem;
    font-size: 0.9375rem;
    font-weight: 500;
    color: var(--color-text-muted);
    text-decoration: none;
    border-radius: 0.5rem;
    transition:
      color 0.2s var(--ease-smooth),
      background-color 0.2s var(--ease-smooth);

    &:hover {
      color: var(--color-primary-dark);
      background: rgba(34, 197, 94, 0.06);
    }
  }

  // ── Mobile Hamburger ─────────────────────────────────────
  &__hamburger {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 5px;
    width: 36px;
    height: 36px;
    padding: 6px;
    background: none;
    border: none;
    cursor: pointer;

    @media (min-width: 768px) {
      display: none;
    }

    span {
      display: block;
      width: 100%;
      height: 2px;
      background: var(--color-text);
      border-radius: 1px;
      transition:
        transform 0.35s var(--ease-spring),
        opacity 0.25s var(--ease-smooth);
    }

    &--open {
      span:nth-child(1) {
        transform: translateY(7px) rotate(45deg);
      }
      span:nth-child(2) {
        opacity: 0;
      }
      span:nth-child(3) {
        transform: translateY(-7px) rotate(-45deg);
      }
    }
  }

  // ── Auth / User Menu ──────────────────────────────────────
  &__login-btn {
    display: inline-flex;
    align-items: center;
    padding: 0.375rem 0.875rem;
    font-size: 0.8125rem;
    font-weight: 600;
    color: white;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    border: none;
    border-radius: 0.5rem;
    cursor: pointer;
    transition: all 0.25s ease;

    &:hover {
      box-shadow: 0 2px 8px rgba(22, 163, 74, 0.3);
      transform: translateY(-1px);
    }
  }

  &__user-menu {
    position: relative;
  }

  &__user-btn {
    background: none;
    border: none;
    cursor: pointer;
    padding: 0;
  }

  &__user-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, $color-leaf-300, $color-leaf-600);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    transition: transform 0.2s ease;

    img { width: 100%; height: 100%; object-fit: cover; }
    span { color: white; font-weight: 600; font-size: 0.85rem; }

    &:hover { transform: scale(1.1); }
  }

  &__user-dropdown {
    min-width: 180px;
  }

  &__user-info {
    padding: 0.5rem 1rem;
  }

  &__user-name {
    display: block;
    font-weight: 600;
    font-size: 0.9rem;
    color: $color-leaf-900;
  }

  &__user-account {
    display: block;
    font-size: 0.75rem;
    color: $color-text-muted;
  }

  &__dropdown-divider {
    height: 1px;
    background: var(--color-border);
    margin: 0.25rem 0;
  }

  &__dropdown-item--danger {
    color: #ef4444 !important;

    &:hover { background: #fef2f2 !important; }
  }

  // ── Language Switch ──────────────────────────────────────
  &__lang-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 0.375rem 0.75rem;
    font-size: 0.8125rem;
    font-weight: 600;
    color: var(--color-text-muted);
    background: rgba(34, 197, 94, 0.06);
    border: 1px solid rgba(34, 197, 94, 0.15);
    border-radius: 0.5rem;
    cursor: pointer;
    transition:
      color 0.25s var(--ease-smooth),
      background-color 0.25s var(--ease-smooth);

    &:hover {
      color: var(--color-primary-dark);
      background: rgba(34, 197, 94, 0.12);
    }
  }

  &__drawer-lang-btn {
    width: 100%;
    padding: 0.75rem 1rem;
    font-size: 0.9375rem;
    font-weight: 600;
    color: var(--color-text-muted);
    background: rgba(34, 197, 94, 0.06);
    border: 1px solid rgba(34, 197, 94, 0.12);
    border-radius: 0.5rem;
    cursor: pointer;
    transition:
      color 0.2s var(--ease-smooth),
      background-color 0.2s var(--ease-smooth);

    &:hover {
      color: var(--color-primary-dark);
      background: rgba(34, 197, 94, 0.12);
    }
  }

  // ── Mobile Drawer ────────────────────────────────────────
  &__drawer-overlay {
    position: fixed;
    inset: 0;
    top: 0;
    z-index: 99;
    background: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(4px);
  }

  &__drawer {
    position: fixed;
    top: 0;
    right: 0;
    width: 280px;
    max-width: 80vw;
    height: 100vh;
    padding: 5rem 1.5rem 2rem;
    background: var(--color-surface);
    box-shadow: -4px 0 24px rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
    gap: 0.25rem;
    overflow-y: auto;
  }

  &__drawer-link {
    display: block;
    padding: 0.75rem 1rem;
    font-size: 1rem;
    font-weight: 500;
    color: var(--color-text-muted);
    text-decoration: none;
    border-radius: 0.5rem;
    transition:
      color 0.2s var(--ease-smooth),
      background-color 0.2s var(--ease-smooth);

    &:hover {
      color: var(--color-primary-dark);
      background: rgba(34, 197, 94, 0.06);
    }

    &--active {
      color: var(--color-primary-dark);
      background: rgba(34, 197, 94, 0.08);
    }

    &--sub {
      padding-left: 1.5rem;
      font-size: 0.9375rem;
    }
  }

  &__drawer-divider {
    height: 1px;
    margin: 0.5rem 0;
    background: var(--color-border);
  }

  &__drawer-section-title {
    display: block;
    padding: 0.5rem 1rem 0.25rem;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--color-text-muted);
  }
}

// ── Transitions ─────────────────────────────────────────────
.dropdown-enter-active {
  transition:
    opacity 0.25s var(--ease-smooth),
    transform 0.3s var(--ease-spring);
}
.dropdown-leave-active {
  transition:
    opacity 0.2s var(--ease-smooth),
    transform 0.2s var(--ease-smooth);
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-8px) scale(0.95);
}

.drawer-enter-active {
  transition:
    opacity 0.3s var(--ease-smooth);

  .header__drawer {
    transition: transform 0.35s var(--ease-spring);
  }
}
.drawer-leave-active {
  transition:
    opacity 0.25s var(--ease-smooth);

  .header__drawer {
    transition: transform 0.25s var(--ease-smooth);
  }
}
.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;

  .header__drawer {
    transform: translateX(100%);
  }
}

</style>
