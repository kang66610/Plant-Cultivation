<script setup lang="ts">
import { ref, onMounted } from 'vue'

const sectionVisible = ref(false)
const sectionRef = ref<HTMLElement>()
const activeIndex = ref(0)

const testimonials = [
  {
    name: '小绿',
    avatar: '🧑‍🌾',
    role: '园艺新手',
    text: '以前总是养死植物，自从用了这个网站的浇水计算器，我的绿萝已经长了三倍大！',
    plant: '绿萝',
  },
  {
    name: '植物达人',
    avatar: '👩‍🔬',
    role: '室内设计师',
    text: '植物百科的数据非常全面，形态特征、四季养护都有详细说明，做方案时经常参考。',
    plant: '琴叶榕',
  },
  {
    name: '阿花',
    avatar: '👨‍🍳',
    role: '厨房园丁',
    text: '光照问答帮我找到了厨房窗台最适合的香草，现在罗勒和薄荷长得特别好！',
    plant: '罗勒',
  },
]

onMounted(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) sectionVisible.value = true
    },
    { threshold: 0.15 }
  )
  if (sectionRef.value) observer.observe(sectionRef.value)

  setInterval(() => {
    activeIndex.value = (activeIndex.value + 1) % testimonials.length
  }, 6000)
})
</script>

<template>
  <section ref="sectionRef" class="testimonials" :class="{ 'testimonials--visible': sectionVisible }">
    <div class="testimonials__bg">
      <div class="testimonials__bg-circle testimonials__bg-circle--1" />
      <div class="testimonials__bg-circle testimonials__bg-circle--2" />
    </div>

    <div class="testimonials__inner">
      <h2 class="testimonials__title">他们都在用</h2>
      <p class="testimonials__subtitle">来自真实用户的反馈</p>

      <div class="testimonials__carousel">
        <div
          v-for="(item, i) in testimonials"
          :key="i"
          class="testimonials__card"
          :class="{ 'testimonials__card--active': i === activeIndex }"
        >
          <div class="testimonials__card-quote">"</div>
          <p class="testimonials__card-text">{{ item.text }}</p>
          <div class="testimonials__card-footer">
            <span class="testimonials__card-avatar">{{ item.avatar }}</span>
            <div>
              <span class="testimonials__card-name">{{ item.name }}</span>
              <span class="testimonials__card-role">{{ item.role }}</span>
            </div>
            <span class="testimonials__card-plant">🌱 {{ item.plant }}</span>
          </div>
        </div>
      </div>

      <div class="testimonials__dots">
        <button
          v-for="(_, i) in testimonials"
          :key="i"
          class="testimonials__dot"
          :class="{ 'testimonials__dot--active': i === activeIndex }"
          @click="activeIndex = i"
        />
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.testimonials {
  position: relative;
  padding: 5rem 1.5rem;
  background: $color-leaf-50;
  overflow: hidden;

  &__bg {
    position: absolute;
    inset: 0;
    pointer-events: none;
  }

  &__bg-circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);

    &--1 {
      width: 300px;
      height: 300px;
      background: rgba(74, 222, 128, 0.1);
      top: -80px;
      right: -60px;
      animation: floatBg 8s ease-in-out infinite;
    }

    &--2 {
      width: 200px;
      height: 200px;
      background: rgba(34, 197, 94, 0.08);
      bottom: -40px;
      left: -40px;
      animation: floatBg 8s ease-in-out infinite -4s;
    }
  }

  &__inner {
    position: relative;
    max-width: 700px;
    margin: 0 auto;
    text-align: center;
  }

  &__title {
    font-family: $font-display;
    font-size: clamp(1.75rem, 3vw, 2.25rem);
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
    font-size: 1rem;
    margin-bottom: 2.5rem;
    opacity: 0;
    transform: translateY(15px);
    transition: all 0.6s $ease-out-expo 0.1s;
  }

  &--visible &__subtitle {
    opacity: 1;
    transform: translateY(0);
  }

  &__carousel {
    position: relative;
    min-height: 220px;
  }

  &__card {
    position: absolute;
    inset: 0;
    background: white;
    border-radius: 20px;
    padding: 2rem 2.5rem;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    opacity: 0;
    transform: translateX(40px) scale(0.95);
    transition: all 0.5s $ease-out-expo;
    pointer-events: none;

    &--active {
      opacity: 1;
      transform: translateX(0) scale(1);
      pointer-events: auto;
    }
  }

  &__card-quote {
    font-family: $font-display;
    font-size: 4rem;
    color: $color-leaf-200;
    line-height: 1;
    margin-bottom: -0.5rem;
  }

  &__card-text {
    font-size: 1.05rem;
    line-height: 1.7;
    color: $color-text;
    margin-bottom: 1.5rem;
    text-align: left;
  }

  &__card-footer {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    text-align: left;
  }

  &__card-avatar {
    font-size: 2rem;
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: $color-leaf-50;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__card-name {
    display: block;
    font-weight: 600;
    color: $color-leaf-900;
    font-size: 0.95rem;
  }

  &__card-role {
    display: block;
    font-size: 0.8rem;
    color: $color-text-muted;
  }

  &__card-plant {
    margin-left: auto;
    font-size: 0.8rem;
    color: $color-leaf-600;
    background: $color-leaf-50;
    padding: 0.3rem 0.75rem;
    border-radius: 20px;
    white-space: nowrap;
  }

  &__dots {
    display: flex;
    justify-content: center;
    gap: 0.5rem;
    margin-top: 2rem;
  }

  &__dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    border: none;
    background: $color-leaf-200;
    cursor: pointer;
    transition: all 0.3s ease;
    padding: 0;

    &--active {
      background: $color-leaf-600;
      width: 24px;
      border-radius: 4px;
    }
  }
}

@keyframes floatBg {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

@media (max-width: 768px) {
  .testimonials__card {
    padding: 1.5rem;
  }
}
</style>
