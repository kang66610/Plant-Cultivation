<script setup lang="ts">
import { ref, onMounted } from 'vue'

const sectionVisible = ref(false)
const sectionRef = ref<HTMLElement>()

onMounted(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) sectionVisible.value = true
    },
    { threshold: 0.2 }
  )
  if (sectionRef.value) observer.observe(sectionRef.value)
})
</script>

<template>
  <section ref="sectionRef" class="cta" :class="{ 'cta--visible': sectionVisible }">
    <div class="cta__bg">
      <div class="cta__leaf cta__leaf--1">
        <svg viewBox="0 0 60 60" fill="none"><path d="M30 5 Q45 20 40 45 Q30 30 15 40 Q25 20 30 5Z" fill="rgba(134,239,172,0.15)"/></svg>
      </div>
      <div class="cta__leaf cta__leaf--2">
        <svg viewBox="0 0 50 50" fill="none"><path d="M25 5 Q40 15 35 40 Q25 25 10 35 Q20 15 25 5Z" fill="rgba(74,222,128,0.1)"/></svg>
      </div>
      <div class="cta__leaf cta__leaf--3">
        <svg viewBox="0 0 40 40" fill="none"><circle cx="20" cy="20" r="15" fill="rgba(34,197,94,0.06)"/></svg>
      </div>
    </div>

    <div class="cta__content">
      <div class="cta__icon">🌿</div>
      <h2 class="cta__title">开始你的绿色之旅</h2>
      <p class="cta__text">探索 50+ 种植物的详细养护指南，找到最适合你的绿色伙伴</p>
      <div class="cta__actions">
        <router-link to="/encyclopedia" class="cta__button cta__button--primary">
          浏览植物百科
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="cta__button-icon"><path d="M5 12h14m-7-7l7 7-7 7"/></svg>
        </router-link>
        <router-link to="/quiz" class="cta__button cta__button--secondary">
          光照问答
        </router-link>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.cta {
  position: relative;
  padding: 5rem 1.5rem;
  background: linear-gradient(135deg, $color-leaf-800, $color-leaf-900);
  overflow: hidden;
  text-align: center;

  &__bg {
    position: absolute;
    inset: 0;
    pointer-events: none;
  }

  &__leaf {
    position: absolute;
    opacity: 0.8;

    &--1 {
      top: 10%;
      left: 5%;
      width: 80px;
      animation: leafFloat 7s ease-in-out infinite;
    }

    &--2 {
      bottom: 15%;
      right: 8%;
      width: 60px;
      animation: leafFloat 7s ease-in-out infinite -3s;
    }

    &--3 {
      top: 50%;
      right: 15%;
      width: 50px;
      animation: leafFloat 7s ease-in-out infinite -5s;
    }
  }

  &__content {
    position: relative;
    max-width: 600px;
    margin: 0 auto;
  }

  &__icon {
    font-size: 3rem;
    margin-bottom: 1.5rem;
    opacity: 0;
    transform: scale(0.5) rotate(-20deg);
    transition: all 0.6s $ease-spring 0.1s;
  }

  &--visible &__icon {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }

  &__title {
    font-family: $font-display;
    font-size: clamp(1.75rem, 3.5vw, 2.5rem);
    color: white;
    margin-bottom: 1rem;
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.6s $ease-out-expo 0.2s;
  }

  &--visible &__title {
    opacity: 1;
    transform: translateY(0);
  }

  &__text {
    font-size: 1.05rem;
    color: rgba(240, 253, 244, 0.7);
    line-height: 1.6;
    margin-bottom: 2rem;
    opacity: 0;
    transform: translateY(15px);
    transition: all 0.6s $ease-out-expo 0.3s;
  }

  &--visible &__text {
    opacity: 1;
    transform: translateY(0);
  }

  &__actions {
    display: flex;
    gap: 1rem;
    justify-content: center;
    flex-wrap: wrap;
    opacity: 0;
    transform: translateY(15px);
    transition: all 0.6s $ease-out-expo 0.4s;
  }

  &--visible &__actions {
    opacity: 1;
    transform: translateY(0);
  }

  &__button {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.875rem 2rem;
    border-radius: 50px;
    font-weight: 600;
    font-size: 1rem;
    text-decoration: none;
    transition: all 0.3s $ease-spring;

    &--primary {
      background: linear-gradient(135deg, $color-leaf-500, $color-leaf-400);
      color: white;
      box-shadow: 0 4px 15px rgba(34, 197, 94, 0.3);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(34, 197, 94, 0.4);
      }
    }

    &--secondary {
      background: rgba(255, 255, 255, 0.1);
      color: white;
      border: 1px solid rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(4px);

      &:hover {
        background: rgba(255, 255, 255, 0.15);
        transform: translateY(-2px);
      }
    }
  }

  &__button-icon {
    width: 18px;
    height: 18px;
    transition: transform 0.3s ease;

    .cta__button:hover & {
      transform: translateX(3px);
    }
  }
}

@keyframes leafFloat {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  33% { transform: translateY(-10px) rotate(5deg); }
  66% { transform: translateY(5px) rotate(-3deg); }
}
</style>
