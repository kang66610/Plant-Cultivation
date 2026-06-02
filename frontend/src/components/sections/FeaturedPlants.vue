<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
const router = useRouter()
const sectionVisible = ref(false)
const sectionRef = ref<HTMLElement>()

interface Plant {
  id: number
  commonName: string
  scientificName: string
  slug: string
  shortDescription: string
  imageUrl: string
  difficulty: string
  lightLevel: string
  waterFrequency: string
}

const plants = ref<Plant[]>([])
const loading = ref(true)

const difficultyColors: Record<string, string> = {
  easy: '#22c55e',
  medium: '#eab308',
  hard: '#ef4444',
}

const lightIcons: Record<string, string> = {
  low: '🌙',
  medium: '⛅',
  high: '☀️',
  bright_direct: '🔆',
}

const waterIcons: Record<string, string> = {
  rarely: '💧',
  weekly: '💧💧',
  biweekly: '💧',
  frequent: '💧💧💧',
}

onMounted(async () => {
  const observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) {
        sectionVisible.value = true
      }
    },
    { threshold: 0.1 }
  )
  if (sectionRef.value) observer.observe(sectionRef.value)

  try {
    const res: any = await request.get('/plants/featured?limit=6')
    plants.value = res.data || []
  } catch {
    plants.value = [
      { id: 1, commonName: '龟背竹', scientificName: 'Monstera deliciosa', slug: 'monstera-deliciosa', shortDescription: '标志性裂叶热带植物，非常适合营造丛林氛围', imageUrl: '/images/plants/monstera.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
      { id: 2, commonName: '绿萝', scientificName: 'Epipremnum aureum', slug: 'pothos', shortDescription: '超易养护的垂吊藤本，非常适合初学者和吊篮', imageUrl: '/images/plants/pothos.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'biweekly' },
      { id: 3, commonName: '虎皮兰', scientificName: 'Dracaena trifasciata', slug: 'snake-plant', shortDescription: '几乎坚不可摧的直立植物，日夜净化空气', imageUrl: '/images/plants/snake-plant.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'rarely' },
      { id: 4, commonName: '琴叶榕', scientificName: 'Ficus lyrata', slug: 'fiddle-leaf-fig', shortDescription: '大型小提琴叶形叶片的戏剧性焦点植物', imageUrl: '/images/plants/fiddle-leaf-fig.jpg', difficulty: 'hard', lightLevel: 'bright_direct', waterFrequency: 'weekly' },
      { id: 5, commonName: '白掌', scientificName: 'Spathiphyllum wallisii', slug: 'peace-lily', shortDescription: '优雅开花植物，能告诉你它何时需要水', imageUrl: '/images/plants/peace-lily.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'weekly' },
      { id: 6, commonName: '芦荟', scientificName: 'Aloe barbadensis miller', slug: 'aloe-vera', shortDescription: '药用多肉植物，具有修复凝胶，非常适合阳光窗台', imageUrl: '/images/plants/aloe-vera.jpg', difficulty: 'easy', lightLevel: 'bright_direct', waterFrequency: 'rarely' },
      { id: 21, commonName: '散尾葵', scientificName: 'Dypsis lutescens', slug: 'areca-palm', shortDescription: '优雅的棕榈植物，净化空气的能手', imageUrl: '/images/plants/areca-palm.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
      { id: 22, commonName: '春羽', scientificName: 'Thaumatophyllum bipinnatifidum', slug: 'selloum', shortDescription: '大型热带观叶植物，叶片深裂极具装饰性', imageUrl: '/images/plants/selloum.jpg', difficulty: 'medium', lightLevel: 'medium', waterFrequency: 'weekly' },
      { id: 36, commonName: '马醉木', scientificName: 'Pieris japonica', slug: 'pieris-japonica', shortDescription: '飘逸灵动、自带日式高雅禅意的水培网红树', imageUrl: '/images/plants/pieris-japonica.jpg', difficulty: 'hard', lightLevel: 'medium', waterFrequency: 'weekly' },
      { id: 37, commonName: '油画婚礼紫露草', scientificName: 'Tradescantia fluminensis Quadricolor', slug: 'tradescantia-quadricolor', shortDescription: '粉白绿紫四色交织，如梦似幻的油画色彩爆盆小仙女', imageUrl: '/images/plants/tradescantia-quadricolor.jpg', difficulty: 'easy', lightLevel: 'high', waterFrequency: 'weekly' },
      { id: 38, commonName: '琴叶喜林芋', scientificName: 'Philodendron panduriforme', slug: 'philodendron-panduriforme', shortDescription: '大叶提琴造型独特，挺拔油亮的高端热带雨林大木柱', imageUrl: '/images/plants/philodendron-panduriforme.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
      { id: 40, commonName: '鸟巢蕨', scientificName: 'Asplenium nidus', slug: 'asplenium-nidus', shortDescription: '环抱如巢，翠绿光滑且极度耐阴的热带雨林林下附生蕨', imageUrl: '/images/plants/asplenium-nidus.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'frequent' },
      { id: 47, commonName: '向日葵', scientificName: 'Helianthus annuus', slug: 'helianthus-annuus', shortDescription: '金黄夺目、追随阳光的阳光活力代言人', imageUrl: '/images/plants/sunflower.jpg', difficulty: 'easy', lightLevel: 'bright_direct', waterFrequency: 'frequent' },
      { id: 49, commonName: '无尽夏', scientificName: 'Hydrangea macrophylla Endless Summer', slug: 'hydrangea-macrophylla-endless-summer', shortDescription: '新老枝开花不断，能随土壤酸碱度自动魔幻变色的顶级观花网红', imageUrl: '/images/plants/hydrangea-endless-summer.jpg', difficulty: 'medium', lightLevel: 'medium', waterFrequency: 'frequent' },
      { id: 50, commonName: '多肉吉娃娃', scientificName: 'Echeveria chihuahuensis', slug: 'echeveria-chihuahuensis', shortDescription: '叶覆白霜，叶尖一抹惊艳小红尖的经典莲座多肉小硬汉', imageUrl: '/images/plants/echeveria-chihuahuensis.jpg', difficulty: 'easy', lightLevel: 'bright_direct', waterFrequency: 'rarely' },
    ]
  } finally {
    loading.value = false
  }
})

function goToPlant(slug: string) {
  router.push(`/plant/${slug}`)
}
</script>

<template>
  <section ref="sectionRef" class="featured" :class="{ 'featured--visible': sectionVisible }">
    <div class="featured__header">
      <div class="featured__header-deco">✦</div>
      <h2 class="featured__title">{{ $t('featured.title') }}</h2>
      <p class="featured__subtitle">{{ $t('featured.subtitle') }}</p>
    </div>

    <div class="featured__grid">
      <div
        v-for="(plant, i) in plants"
        :key="plant.id"
        class="featured__card"
        :style="{ transitionDelay: `${i * 0.08}s` }"
        @click="goToPlant(plant.slug)"
      >
        <div class="featured__card-shine" />
        <div class="featured__card-image">
          <img
            :src="plant.imageUrl"
            :alt="plant.commonName"
            class="featured__card-img"
            loading="lazy"
          />
        </div>

        <div class="featured__card-body">
          <h3 class="featured__card-name">{{ plant.commonName }}</h3>
          <p class="featured__card-sci">{{ plant.scientificName }}</p>
          <p class="featured__card-desc">{{ plant.shortDescription }}</p>

          <div class="featured__card-meta">
            <span
              class="featured__card-badge"
              :style="{ background: difficultyColors[plant.difficulty] || '#22c55e' }"
            >
              {{ plant.difficulty }}
            </span>
            <span class="featured__card-icon">{{ lightIcons[plant.lightLevel] || '☀️' }}</span>
            <span class="featured__card-icon">{{ waterIcons[plant.waterFrequency] || '💧' }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="featured__action">
      <router-link to="/encyclopedia" class="featured__link">
        {{ $t('featured.viewAll') }}
      </router-link>
    </div>
  </section>
</template>

<style scoped lang="scss">
.featured {
  padding: 5rem 1.5rem;
  max-width: 1200px;
  margin: 0 auto;

  &__header {
    text-align: center;
    margin-bottom: 3rem;
  }

  &__header-deco {
    font-size: 1.5rem;
    color: $color-leaf-400;
    margin-bottom: 0.75rem;
    opacity: 0;
    transform: scale(0.5) rotate(45deg);
    transition: all 0.6s $ease-spring;
  }

  &--visible &__header-deco {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }

  &__title {
    font-family: $font-display;
    font-size: clamp(1.75rem, 3vw, 2.5rem);
    color: $color-leaf-900;
    margin-bottom: 0.5rem;
    opacity: 0;
    transform: translateY(20px);
    transition: all 0.6s $ease-out-expo 0.1s;
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
    transition: all 0.6s $ease-out-expo 0.2s;
  }

  &--visible &__subtitle {
    opacity: 1;
    transform: translateY(0);
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.5rem;
  }

  &__card {
    position: relative;
    background: white;
    border-radius: 16px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.4s $ease-spring;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    opacity: 0;
    transform: translateY(30px);

    &--visible &, .featured--visible & {
      opacity: 1;
      transform: translateY(0);
    }

    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);

      .featured__card-image {
        transform: scale(1.03);
      }

      .featured__card-name {
        color: $color-leaf-600;
      }

      .featured__card-shine {
        opacity: 1;
      }
    }
  }

  &__card-shine {
    position: absolute;
    top: 0;
    left: -100%;
    width: 60%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.15), transparent);
    transform: skewX(-15deg);
    opacity: 0;
    transition: opacity 0.3s ease;
    z-index: 2;
    pointer-events: none;

    .featured__card:hover & {
      animation: cardShine 0.8s ease forwards;
    }
  }

  &__card-image {
    height: 200px;
    overflow: hidden;
    transition: transform 0.6s $ease-out-expo;
    background: linear-gradient(135deg, $color-leaf-50, $color-leaf-100);
  }

  &__card-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;

    .featured__card:hover & {
      transform: scale(1.05);
    }
  }

  &__card-body {
    padding: 1.25rem;
  }

  &__card-name {
    font-family: $font-display;
    font-size: 1.15rem;
    font-weight: 600;
    color: $color-leaf-900;
    margin-bottom: 0.25rem;
    transition: color 0.3s ease;
  }

  &__card-sci {
    font-style: italic;
    font-size: 0.85rem;
    color: $color-text-muted;
    margin-bottom: 0.5rem;
  }

  &__card-desc {
    font-size: 0.9rem;
    color: $color-text;
    line-height: 1.5;
    margin-bottom: 0.75rem;
  }

  &__card-meta {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  &__card-badge {
    display: inline-block;
    padding: 0.2rem 0.6rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
    color: white;
    text-transform: capitalize;
  }

  &__card-icon {
    font-size: 0.85rem;
  }

  &__action {
    text-align: center;
    margin-top: 2.5rem;
  }

  &__link {
    color: $color-leaf-600;
    font-weight: 600;
    font-size: 1.05rem;
    text-decoration: none;
    transition: all 0.3s ease;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      width: 0;
      height: 2px;
      background: $color-leaf-500;
      transition: width 0.3s ease;
    }

    &:hover {
      color: $color-leaf-700;

      &::after {
        width: 100%;
      }
    }
  }
}

@keyframes cardShine {
  0% { left: -100%; }
  100% { left: 150%; }
}
</style>
