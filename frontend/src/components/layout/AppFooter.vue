<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const quickLinks = computed(() => [
  { name: t('nav.home'), to: '/' },
  { name: t('nav.encyclopedia'), to: '/encyclopedia' },
  { name: t('nav.community'), to: '/community' },
  { name: t('nav.diary'), to: '/diary' },
  { name: t('nav.about'), to: '/about' },
])

const resourceLinks = computed(() => [
  { name: t('footer.plantCare'), to: '/tools/watering-calculator' },
  { name: t('footer.lightGuide'), to: '/tools/light-quiz' },
])

const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="footer">
    <!-- SVG Vine Divider -->
    <div class="footer__divider">
      <svg
        viewBox="0 0 1440 80"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        preserveAspectRatio="none"
      >
        <path
          d="M0 40C120 60 240 80 360 60C480 40 600 0 720 20C840 40 960 80 1080 60C1200 40 1320 20 1440 40V0H0V40Z"
          fill="var(--color-surface)"
        />
        <path
          d="M0 50C100 65 200 78 300 65C400 52 500 15 620 30C740 45 860 75 980 60C1100 45 1250 25 1440 45"
          stroke="rgba(74, 222, 128, 0.3)"
          stroke-width="2"
          fill="none"
        />
        <!-- Small leaf decorations -->
        <circle cx="300" cy="62" r="3" fill="rgba(74, 222, 128, 0.25)" />
        <circle cx="620" cy="28" r="2.5" fill="rgba(74, 222, 128, 0.2)" />
        <circle cx="980" cy="58" r="3" fill="rgba(74, 222, 128, 0.25)" />
      </svg>
    </div>

    <div class="footer__content">
      <div class="footer__inner">
        <!-- Brand Column -->
        <div class="footer__brand">
          <RouterLink to="/" class="footer__logo">
            <svg
              class="footer__logo-icon"
              viewBox="0 0 32 32"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M16 2C16 2 8 6 8 14C8 18.418 11.582 22 16 22C20.418 22 24 18.418 24 14C24 6 16 2 16 2Z"
                fill="url(#footerLeafGrad)"
                stroke="rgba(255,255,255,0.3)"
                stroke-width="1"
              />
              <path
                d="M16 22V30"
                stroke="rgba(255,255,255,0.4)"
                stroke-width="2"
                stroke-linecap="round"
              />
              <defs>
                <linearGradient id="footerLeafGrad" x1="8" y1="2" x2="24" y2="22">
                  <stop stop-color="#4ade80" />
                  <stop offset="1" stop-color="#16a34a" />
                </linearGradient>
              </defs>
            </svg>
            <span>{{ $t('footer.brand') }}</span>
          </RouterLink>
          <p class="footer__tagline">
            {{ $t('footer.brandDesc') }}
          </p>
        </div>

        <!-- Quick Links -->
        <div class="footer__column">
          <h4 class="footer__column-title">{{ $t('footer.quickLinks') }}</h4>
          <ul class="footer__links">
            <li v-for="link in quickLinks" :key="link.to">
              <RouterLink :to="link.to" class="footer__link">
                {{ link.name }}
              </RouterLink>
            </li>
          </ul>
        </div>

        <!-- Resources -->
        <div class="footer__column">
          <h4 class="footer__column-title">{{ $t('footer.resources') }}</h4>
          <ul class="footer__links">
            <li v-for="link in resourceLinks" :key="link.to">
              <RouterLink :to="link.to" class="footer__link">
                {{ link.name }}
              </RouterLink>
            </li>
          </ul>
        </div>

        <!-- Connect -->
        <div class="footer__column">
          <h4 class="footer__column-title">{{ $t('footer.connect') }}</h4>
          <div class="footer__socials">
            <!-- GitHub -->
            <a href="#" class="footer__social-link" aria-label="GitHub">
              <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.477 2 2 6.477 2 12c0 4.42 2.865 8.166 6.839 9.489.5.092.682-.217.682-.482 0-.237-.008-.866-.013-1.7-2.782.604-3.369-1.341-3.369-1.341-.454-1.155-1.11-1.462-1.11-1.462-.908-.62.069-.608.069-.608 1.003.07 1.531 1.03 1.531 1.03.892 1.529 2.341 1.087 2.91.831.092-.646.35-1.086.636-1.336-2.22-.253-4.555-1.11-4.555-4.943 0-1.091.39-1.984 1.029-2.683-.103-.253-.446-1.27.098-2.647 0 0 .84-.269 2.75 1.025A9.578 9.578 0 0 1 12 6.836c.85.004 1.705.115 2.504.337 1.909-1.294 2.747-1.025 2.747-1.025.546 1.377.203 2.394.1 2.647.64.699 1.028 1.592 1.028 2.683 0 3.842-2.339 4.687-4.566 4.935.359.309.678.919.678 1.852 0 1.336-.012 2.415-.012 2.743 0 .267.18.578.688.48C19.138 20.161 22 16.418 22 12c0-5.523-4.477-10-10-10z"/></svg>
            </a>
          </div>
        </div>
      </div>

      <!-- Copyright -->
      <div class="footer__bottom">
        <p>&copy; {{ currentYear }} {{ $t('footer.copyright') }}</p>
      </div>
    </div>
  </footer>
</template>

<style scoped lang="scss">
.footer {
  position: relative;
  background: #052e16;
  color: rgba(255, 255, 255, 0.7);

  // Subtle leaf pattern background
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background-image:
      radial-gradient(ellipse 40px 50px at 15% 30%, rgba(74, 222, 128, 0.04) 0%, transparent 70%),
      radial-gradient(ellipse 35px 45px at 75% 20%, rgba(74, 222, 128, 0.03) 0%, transparent 70%),
      radial-gradient(ellipse 30px 40px at 50% 60%, rgba(74, 222, 128, 0.03) 0%, transparent 70%),
      radial-gradient(ellipse 45px 55px at 85% 70%, rgba(74, 222, 128, 0.04) 0%, transparent 70%),
      radial-gradient(ellipse 38px 48px at 25% 80%, rgba(74, 222, 128, 0.03) 0%, transparent 70%);
    pointer-events: none;
  }

  &__divider {
    position: relative;
    margin-top: -1px;
    background: var(--color-surface);

    svg {
      display: block;
      width: 100%;
      height: 60px;
    }
  }

  &__content {
    position: relative;
    z-index: 1;
  }

  &__inner {
    max-width: 1280px;
    margin: 0 auto;
    padding: 3.5rem 1.5rem 2rem;
    display: grid;
    grid-template-columns: 1fr;
    gap: 2.5rem;

    @media (min-width: 640px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (min-width: 1024px) {
      grid-template-columns: 2fr 1fr 1fr 1fr;
      gap: 3rem;
    }
  }

  // ── Brand ────────────────────────────────────────────────
  &__logo {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    text-decoration: none;
    color: #ffffff;
    font-family: var(--font-display);
    font-size: 1.25rem;
    font-weight: 700;
    margin-bottom: 0.75rem;

    &:hover {
      color: #ffffff;
    }
  }

  &__logo-icon {
    width: 28px;
    height: 28px;
  }

  &__tagline {
    font-size: 0.9375rem;
    line-height: 1.6;
    max-width: 320px;
    color: rgba(255, 255, 255, 0.5);
  }

  // ── Columns ──────────────────────────────────────────────
  &__column-title {
    font-family: var(--font-sans);
    font-size: 0.8125rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: rgba(255, 255, 255, 0.4);
    margin-bottom: 1rem;
  }

  &__links {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  &__link {
    font-size: 0.9375rem;
    color: rgba(255, 255, 255, 0.7);
    text-decoration: none;
    transition: color 0.25s var(--ease-smooth);
    position: relative;
    display: inline-block;

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 0;
      height: 1px;
      background: rgba(74, 222, 128, 0.6);
      transition: width 0.3s var(--ease-spring);
    }

    &:hover {
      color: #4ade80;

      &::after {
        width: 100%;
      }
    }
  }

  // ── Socials ──────────────────────────────────────────────
  &__socials {
    display: flex;
    gap: 0.75rem;
  }

  &__social-link {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 0.5rem;
    background: rgba(255, 255, 255, 0.06);
    color: rgba(255, 255, 255, 0.5);
    text-decoration: none;
    transition:
      color 0.25s var(--ease-smooth),
      background-color 0.25s var(--ease-smooth),
      transform 0.3s var(--ease-spring);

    svg {
      width: 18px;
      height: 18px;
    }

    &:hover {
      color: #4ade80;
      background: rgba(74, 222, 128, 0.1);
      transform: translateY(-2px);
    }
  }

  // ── Bottom ───────────────────────────────────────────────
  &__bottom {
    max-width: 1280px;
    margin: 0 auto;
    padding: 1.5rem;
    border-top: 1px solid rgba(255, 255, 255, 0.06);
    text-align: center;

    p {
      font-size: 0.8125rem;
      color: rgba(255, 255, 255, 0.3);
    }
  }
}
</style>
