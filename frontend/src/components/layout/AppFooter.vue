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
            <a href="mailto:233793kang@gmail.com" class="footer__social-link footer__social-link--email" aria-label="Email 233793kang@gmail.com">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="5" width="18" height="14" rx="2" />
                <path d="m3 7 9 7 9-7" />
              </svg>
              <span>233793kang@gmail.com</span>
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

  &__social-link--email {
    width: auto;
    min-height: 40px;
    justify-content: flex-start;
    gap: 0.55rem;
    padding: 0 0.85rem;
    color: rgba(255, 255, 255, 0.68);
    font-size: 0.9rem;
    word-break: break-all;

    span {
      line-height: 1.35;
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
