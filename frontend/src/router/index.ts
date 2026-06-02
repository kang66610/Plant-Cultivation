import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomePage.vue'),
      meta: { transition: 'fade' },
    },
    {
      path: '/encyclopedia',
      name: 'encyclopedia',
      component: () => import('@/views/EncyclopediaPage.vue'),
      meta: { transition: 'slide-left' },
    },
    {
      path: '/plant/:slug',
      name: 'plant-detail',
      component: () => import('@/views/PlantDetailPage.vue'),
      meta: { transition: 'slide-up' },
    },
    {
      path: '/tools/watering-calculator',
      name: 'watering-calculator',
      component: () => import('@/views/WateringCalcPage.vue'),
      meta: { transition: 'slide-left' },
    },
    {
      path: '/tools/light-quiz',
      name: 'light-quiz',
      component: () => import('@/views/QuizPage.vue'),
      meta: { transition: 'slide-left' },
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('@/views/CommunityPage.vue'),
      meta: { transition: 'slide-left' },
    },
    {
      path: '/diary',
      name: 'diary',
      component: () => import('@/views/PlantDiaryPage.vue'),
      meta: { transition: 'slide-left' },
    },
    {
      path: '/user-center',
      name: 'user-center',
      component: () => import('@/views/UserCenterPage.vue'),
      meta: { transition: 'fade' },
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/AboutPage.vue'),
      meta: { transition: 'fade' },
    },
  ],
})

export default router
