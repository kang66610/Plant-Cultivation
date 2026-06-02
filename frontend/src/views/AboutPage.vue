<template>
  <div class="about-page">
    <!-- Hero -->
    <div class="about__hero">
      <svg class="about__hero-icon" viewBox="0 0 64 64" fill="none">
        <path d="M32 4C32 4 16 12 16 28C16 36.8 23.2 44 32 44C40.8 44 48 36.8 48 28C48 12 32 4 32 4Z" fill="url(#heroLeaf)" stroke="rgba(255,255,255,0.5)" stroke-width="2"/>
        <path d="M32 44V60" stroke="rgba(255,255,255,0.7)" stroke-width="3" stroke-linecap="round"/>
        <path d="M32 28C32 28 24 20 18 22" stroke="rgba(255,255,255,0.4)" stroke-width="2" stroke-linecap="round"/>
        <defs>
          <linearGradient id="heroLeaf" x1="16" y1="4" x2="48" y2="44">
            <stop stop-color="#4ade80"/>
            <stop offset="1" stop-color="#16a34a"/>
          </linearGradient>
        </defs>
      </svg>
      <h1 class="about__title">{{ $t('about.title') }}</h1>
      <p class="about__subtitle">{{ $t('about.subtitle') }}</p>
    </div>

    <!-- Mission -->
    <div class="about__content">
      <section class="about__section about__section--center">
        <h2 class="about__section-title">{{ $t('about.mission') }}</h2>
        <p class="about__section-text about__section-text--lead">{{ $t('about.missionText') }}</p>
      </section>

      <!-- Features -->
      <section class="about__section">
        <h2 class="about__section-title">{{ $t('about.features') }}</h2>
        <div class="about__features">
          <div class="about__feature-card">
            <div class="about__feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </div>
            <h3 class="about__feature-title">{{ $t('about.feature1Title') }}</h3>
            <p class="about__feature-text">{{ $t('about.feature1Text') }}</p>
          </div>

          <div class="about__feature-card">
            <div class="about__feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 2v6M12 16v6"/>
                <path d="M4.93 4.93l4.24 4.24M14.83 14.83l4.24 4.24"/>
                <path d="M2 12h6M16 12h6"/>
                <circle cx="12" cy="12" r="4"/>
              </svg>
            </div>
            <h3 class="about__feature-title">{{ $t('about.feature2Title') }}</h3>
            <p class="about__feature-text">{{ $t('about.feature2Text') }}</p>
          </div>

          <div class="about__feature-card">
            <div class="about__feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <circle cx="12" cy="12" r="10"/>
                <path d="M12 16v-4M12 8h.01"/>
              </svg>
            </div>
            <h3 class="about__feature-title">{{ $t('about.feature3Title') }}</h3>
            <p class="about__feature-text">{{ $t('about.feature3Text') }}</p>
          </div>

          <div class="about__feature-card">
            <div class="about__feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
              </svg>
            </div>
            <h3 class="about__feature-title">{{ $t('about.feature4Title') }}</h3>
            <p class="about__feature-text">{{ $t('about.feature4Text') }}</p>
          </div>
        </div>
      </section>

      <!-- Values -->
      <section class="about__section">
        <h2 class="about__section-title">{{ $t('about.values') }}</h2>
        <div class="about__values">
          <div class="about__value">
            <div class="about__value-badge">01</div>
            <h3 class="about__value-title">{{ $t('about.value1Title') }}</h3>
            <p class="about__value-text">{{ $t('about.value1Text') }}</p>
          </div>
          <div class="about__value">
            <div class="about__value-badge">02</div>
            <h3 class="about__value-title">{{ $t('about.value2Title') }}</h3>
            <p class="about__value-text">{{ $t('about.value2Text') }}</p>
          </div>
          <div class="about__value">
            <div class="about__value-badge">03</div>
            <h3 class="about__value-title">{{ $t('about.value3Title') }}</h3>
            <p class="about__value-text">{{ $t('about.value3Text') }}</p>
          </div>
        </div>
      </section>

      <!-- Team -->
      <section class="about__section about__section--center">
        <h2 class="about__section-title">{{ $t('about.team') }}</h2>
        <p class="about__section-text about__section-text--lead">{{ $t('about.teamText') }}</p>
      </section>

      <!-- Contact -->
      <section class="about__section about__section--center">
        <h2 class="about__section-title">{{ $t('about.contact') }}</h2>
        <p class="about__section-text">{{ $t('about.contactText') }}</p>
        <a href="mailto:233793kang@gmail.com" class="about__contact-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
            <rect x="2" y="4" width="20" height="16" rx="2"/>
            <path d="M22 4L12 13 2 4"/>
          </svg>
          233793kang@gmail.com
        </a>
      </section>
    </div>
  </div>
</template>

<style scoped lang="scss">
.about-page {
  min-height: 100vh;
}

.about {
  &__hero {
    padding: 6rem 1.5rem 4rem;
    text-align: center;
    background: linear-gradient(135deg, $color-leaf-800, $color-leaf-900);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: -50%;
      left: -50%;
      width: 200%;
      height: 200%;
      background: radial-gradient(circle, rgba(74, 222, 128, 0.1) 0%, transparent 50%);
      animation: pulse 8s ease-in-out infinite;
    }
  }

  &__hero-icon {
    width: 72px;
    height: 72px;
    margin-bottom: 1.5rem;
    position: relative;
    z-index: 1;
    animation: float 3s ease-in-out infinite;
  }

  &__title {
    font-family: $font-display;
    font-size: clamp(2rem, 4vw, 3rem);
    color: #f0fdf4;
    margin-bottom: 0.75rem;
    position: relative;
    z-index: 1;
  }

  &__subtitle {
    color: rgba(240, 253, 244, 0.7);
    font-size: 1.15rem;
    position: relative;
    z-index: 1;
  }

  &__content {
    max-width: 960px;
    margin: 0 auto;
    padding: 3rem 1.5rem 4rem;
  }

  &__section {
    margin-bottom: 4rem;

    &--center {
      text-align: center;
    }

    &-title {
      font-family: $font-display;
      font-size: 1.6rem;
      color: $color-leaf-800;
      margin-bottom: 1.25rem;
      position: relative;
      display: inline-block;

      &::after {
        content: '';
        display: block;
        width: 40px;
        height: 3px;
        background: linear-gradient(90deg, $color-leaf-400, $color-leaf-600);
        border-radius: 2px;
        margin-top: 0.5rem;
      }
    }

    &--center &-title::after {
      margin-left: auto;
      margin-right: auto;
    }

    &-text {
      font-size: 1.05rem;
      line-height: 1.8;
      color: $color-text;

      &--lead {
        font-size: 1.1rem;
        max-width: 700px;
        margin-left: auto;
        margin-right: auto;
      }
    }
  }

  // Features Grid
  &__features {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 1.25rem;
    margin-top: 1.5rem;
  }

  &__feature-card {
    background: white;
    border-radius: 1rem;
    padding: 1.75rem;
    border: 1px solid rgba(0, 0, 0, 0.04);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    transition: transform 0.3s ease, box-shadow 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }
  }

  &__feature-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: linear-gradient(135deg, $color-leaf-50, $color-leaf-100);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 1rem;

    svg {
      width: 24px;
      height: 24px;
      color: $color-leaf-600;
    }
  }

  &__feature-title {
    font-family: $font-display;
    font-size: 1.1rem;
    color: $color-leaf-900;
    margin-bottom: 0.5rem;
  }

  &__feature-text {
    font-size: 0.9rem;
    line-height: 1.65;
    color: $color-text-muted;
  }

  // Values
  &__values {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 1.5rem;
    margin-top: 1.5rem;
  }

  &__value {
    position: relative;
    padding: 1.5rem;
    padding-left: 4rem;
    background: $color-leaf-50;
    border-radius: 1rem;
  }

  &__value-badge {
    position: absolute;
    top: 1.5rem;
    left: 1.25rem;
    font-family: $font-display;
    font-size: 1.25rem;
    font-weight: 700;
    color: $color-leaf-300;
  }

  &__value-title {
    font-family: $font-display;
    font-size: 1.05rem;
    color: $color-leaf-800;
    margin-bottom: 0.4rem;
  }

  &__value-text {
    font-size: 0.9rem;
    line-height: 1.65;
    color: $color-text-muted;
  }

  // Contact
  &__contact-btn {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    margin-top: 1.25rem;
    padding: 0.75rem 1.5rem;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white;
    text-decoration: none;
    border-radius: 0.75rem;
    font-weight: 600;
    font-size: 0.95rem;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(22, 163, 74, 0.3);
    }
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

@media (max-width: 640px) {
  .about {
    &__hero {
      padding: 4rem 1.5rem 2.5rem;
    }

    &__hero-icon {
      width: 56px;
      height: 56px;
    }

    &__content {
      padding: 2rem 1rem 3rem;
    }

    &__features {
      grid-template-columns: 1fr;
    }

    &__values {
      grid-template-columns: 1fr;
    }

    &__contact-btn {
      width: 100%;
      justify-content: center;
    }
  }
}
</style>
