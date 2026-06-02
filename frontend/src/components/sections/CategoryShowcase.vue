<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()
const sectionVisible = ref(false)
const sectionRef = ref<HTMLElement>()

const categories = computed(() => [
  { name: t('categories.succulents'), slug: 'succulents', emoji: '🌵', color: '#a3e635', desc: t('categories.succulentsDesc') },
  { name: t('categories.tropical'), slug: 'tropical', emoji: '🌿', color: '#22c55e', desc: t('categories.tropicalDesc') },
  { name: t('categories.herbs'), slug: 'herbs', emoji: '🌱', color: '#84cc16', desc: t('categories.herbsDesc') },
  { name: t('categories.ferns'), slug: 'ferns', emoji: '🌾', color: '#10b981', desc: t('categories.fernsDesc') },
  { name: t('categories.flowering'), slug: 'flowering', emoji: '🌸', color: '#f472b6', desc: t('categories.floweringDesc') },
  { name: t('categories.trees'), slug: 'trees', emoji: '🌳', color: '#059669', desc: t('categories.treesDesc') },
])

onMounted(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) sectionVisible.value = true
    },
    { threshold: 0.1 }
  )
  if (sectionRef.value) observer.observe(sectionRef.value)
})

function goToCategory(slug: string) {
  router.push(`/encyclopedia?category=${slug}`)
}
</script>

<template>
  <section ref="sectionRef" class="categories" :class="{ 'categories--visible': sectionVisible }">
    <div class="categories__header">
      <h2 class="categories__title">{{ $t('categories.title') }}</h2>
      <p class="categories__subtitle">{{ $t('categories.subtitle') }}</p>
    </div>

    <div class="categories__grid">
      <div
        v-for="(cat, i) in categories"
        :key="cat.slug"
        class="categories__card"
        :style="{ transitionDelay: `${i * 0.08}s` }"
        @click="goToCategory(cat.slug)"
      >
        <div class="categories__card-bg" :style="{ background: `linear-gradient(135deg, ${cat.color}22, ${cat.color}11)` }" />
        <div class="categories__card-ring" :style="{ borderColor: `${cat.color}33` }" />
        <span class="categories__card-emoji">{{ cat.emoji }}</span>
        <h3 class="categories__card-name">{{ cat.name }}</h3>
        <p class="categories__card-desc">{{ cat.desc }}</p>
        <div class="categories__card-arrow">→</div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.categories {
  padding: 5rem 1.5rem;
  background: linear-gradient(180deg, #fafaf8 0%, #f0fdf4 100%);

  &__header {
    text-align: center;
    max-width: 1200px;
    margin: 0 auto 3rem;
  }

  &__title {
    font-family: $font-display;
    font-size: clamp(1.75rem, 3vw, 2.5rem);
    color: $color-leaf-900;
    margin-bottom: 0.5rem;
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.6s $ease-out-expo;
  }

  &--visible &__title {
    opacity: 1;
    transform: translateY(0);
  }

  &__subtitle {
    color: $color-text-muted;
    font-size: 1.05rem;
    opacity: 0;
    transform: translateY(15px);
    transition: all 0.6s $ease-out-expo 0.1s;
  }

  &--visible &__subtitle {
    opacity: 1;
    transform: translateY(0);
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 1.25rem;
    max-width: 1200px;
    margin: 0 auto;
  }

  &__card {
    position: relative;
    padding: 2rem 1.5rem;
    background: white;
    border-radius: 16px;
    text-align: center;
    cursor: pointer;
    overflow: hidden;
    transition: all 0.4s $ease-spring;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    opacity: 0;
    transform: translateY(25px);

    &--visible &, .categories--visible & {
      opacity: 1;
      transform: translateY(0);
    }

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 12px 30px rgba(0, 0, 0, 0.1);

      .categories__card-bg {
        opacity: 1;
      }

      .categories__card-emoji {
        transform: scale(1.2) rotate(5deg);
      }

      .categories__card-arrow {
        opacity: 1;
        transform: translateX(0);
      }

      .categories__card-ring {
        transform: scale(1.1);
        opacity: 0.6;
      }
    }
  }

  &__card-bg {
    position: absolute;
    inset: 0;
    opacity: 0.5;
    transition: opacity 0.4s ease;
  }

  &__card-ring {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 80px;
    height: 80px;
    border-radius: 50%;
    border: 2px solid;
    transform: translate(-50%, -50%);
    opacity: 0;
    transition: all 0.5s $ease-spring;
    pointer-events: none;
  }

  &__card-emoji {
    display: block;
    font-size: 2.5rem;
    margin-bottom: 0.75rem;
    transition: transform 0.4s $ease-spring;
    position: relative;
    z-index: 1;
  }

  &__card-name {
    font-family: $font-display;
    font-size: 1.1rem;
    font-weight: 600;
    color: $color-leaf-900;
    margin-bottom: 0.25rem;
    position: relative;
    z-index: 1;
  }

  &__card-desc {
    font-size: 0.8rem;
    color: $color-text-muted;
    position: relative;
    z-index: 1;
  }

  &__card-arrow {
    margin-top: 0.75rem;
    font-size: 1.2rem;
    color: $color-leaf-600;
    opacity: 0;
    transform: translateX(-8px);
    transition: all 0.3s ease;
    position: relative;
    z-index: 1;
  }
}
</style>
