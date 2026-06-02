<script setup lang="ts">
import HeroSection from '@/components/sections/HeroSection.vue'
import StatsCounter from '@/components/sections/StatsCounter.vue'
import FeaturedPlants from '@/components/sections/FeaturedPlants.vue'
import CategoryShowcase from '@/components/sections/CategoryShowcase.vue'
import TestimonialsSection from '@/components/sections/TestimonialsSection.vue'
import CareTipBanner from '@/components/sections/CareTipBanner.vue'
import CTASection from '@/components/sections/CTASection.vue'
</script>

<template>
  <div class="home-page">
    <HeroSection />
    <StatsCounter />
    <FeaturedPlants />
    <CategoryShowcase />
    <TestimonialsSection />
    <CareTipBanner />
    <CTASection />
  </div>
</template>

<style scoped lang="scss">
.home-page {
  min-height: 100vh;
}
</style>
