<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '@/api/request'

const { t } = useI18n()

const router = useRouter()
const route = useRoute()

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

interface Category {
  id: number
  name: string
  slug: string
}

const plants = ref<Plant[]>([])
const categories = ref<Category[]>([])
const loading = ref(true)
const searchQuery = ref('')
const activeCategory = ref('')
const activeDifficulty = ref('')
const activeLight = ref('')

const difficultyLabels = computed<Record<string, string>>(() => ({
  easy: t('encyclopedia.easy'),
  medium: t('encyclopedia.medium'),
  hard: t('encyclopedia.hard'),
}))

const difficultyColors: Record<string, string> = {
  easy: '#22c55e',
  medium: '#eab308',
  hard: '#ef4444',
}

const lightLabels = computed<Record<string, string>>(() => ({
  low: t('encyclopedia.lowLight'),
  medium: t('encyclopedia.mediumLight'),
  high: t('encyclopedia.brightLight'),
  bright_direct: t('encyclopedia.directSun'),
}))

const waterLabels = computed<Record<string, string>>(() => ({
  rarely: t('encyclopedia.rarely'),
  weekly: t('encyclopedia.weekly'),
  biweekly: t('encyclopedia.biweekly'),
  frequent: t('encyclopedia.frequent'),
}))

const allMockPlants: Plant[] = [
  { id: 1, commonName: '龟背竹', scientificName: 'Monstera deliciosa', slug: 'monstera-deliciosa', shortDescription: '标志性裂叶热带植物，非常适合营造丛林氛围', imageUrl: '/images/plants/monstera.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 2, commonName: '绿萝', scientificName: 'Epipremnum aureum', slug: 'pothos', shortDescription: '超易养护的垂吊藤本，非常适合初学者和吊篮', imageUrl: '/images/plants/pothos.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'biweekly' },
  { id: 3, commonName: '虎皮兰', scientificName: 'Dracaena trifasciata', slug: 'snake-plant', shortDescription: '几乎坚不可摧的直立植物，日夜净化空气', imageUrl: '/images/plants/snake-plant.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'rarely' },
  { id: 4, commonName: '琴叶榕', scientificName: 'Ficus lyrata', slug: 'fiddle-leaf-fig', shortDescription: '大型小提琴叶形叶片的戏剧性焦点植物', imageUrl: '/images/plants/fiddle-leaf-fig.jpg', difficulty: 'hard', lightLevel: 'bright_direct', waterFrequency: 'weekly' },
  { id: 5, commonName: '白掌', scientificName: 'Spathiphyllum wallisii', slug: 'peace-lily', shortDescription: '优雅开花植物，能告诉你它何时需要水', imageUrl: '/images/plants/peace-lily.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'weekly' },
  { id: 6, commonName: '芦荟', scientificName: 'Aloe barbadensis miller', slug: 'aloe-vera', shortDescription: '药用多肉植物，具有修复凝胶，非常适合阳光窗台', imageUrl: '/images/plants/aloe-vera.jpg', difficulty: 'easy', lightLevel: 'bright_direct', waterFrequency: 'rarely' },
  { id: 7, commonName: '吊兰', scientificName: 'Chlorophytum comosum', slug: 'spider-plant', shortDescription: '令人愉悦的拱形植物，带有小吊兰，超级容易种植', imageUrl: '/images/plants/spider-plant.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 8, commonName: '金钱树', scientificName: 'Zamioculcas zamiifolia', slug: 'zz-plant', shortDescription: '建筑感光滑植物，在疏忽下茁壮成长', imageUrl: '/images/plants/zz-plant.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'rarely' },
  { id: 9, commonName: '橡皮树', scientificName: 'Ficus elastica', slug: 'rubber-plant', shortDescription: '大胆光泽叶片的室内树木，具有戏剧性存在感', imageUrl: '/images/plants/rubber-plant.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 10, commonName: '蝴蝶兰', scientificName: 'Phalaenopsis spp.', slug: 'orchid', shortDescription: '优雅的开花兰花，花朵持续数月', imageUrl: '/images/plants/orchid.jpg', difficulty: 'medium', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 11, commonName: '竹芋', scientificName: 'Calathea spp.', slug: 'calathea', shortDescription: '图案祈祷植物，拥有令人惊叹的叶片设计', imageUrl: '/images/plants/calathea.jpg', difficulty: 'hard', lightLevel: 'low', waterFrequency: 'weekly' },
  { id: 12, commonName: '天堂鸟', scientificName: 'Strelitzia reginae', slug: 'bird-of-paradise', shortDescription: '戏剧性的热带焦点植物，拥有香蕉状叶片', imageUrl: '/images/plants/bird-of-paradise.jpg', difficulty: 'medium', lightLevel: 'bright_direct', waterFrequency: 'weekly' },
  { id: 21, commonName: '散尾葵', scientificName: 'Dypsis lutescens', slug: 'areca-palm', shortDescription: '优雅的棕榈植物，净化空气的能手', imageUrl: '/images/plants/areca-palm.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 22, commonName: '春羽', scientificName: 'Thaumatophyllum bipinnatifidum', slug: 'selloum', shortDescription: '大型热带观叶植物，叶片深裂极具装饰性', imageUrl: '/images/plants/selloum.jpg', difficulty: 'medium', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 26, commonName: '袖珍椰子', scientificName: 'Chamaedorea elegans', slug: 'parlor-palm', shortDescription: '小巧玲珑的棕榈，适合桌面和书架', imageUrl: '/images/plants/parlor-palm.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'weekly' },
  { id: 27, commonName: '豆瓣绿', scientificName: 'Peperomia obtusifolia', slug: 'peperomia', shortDescription: '肉质光滑叶片的小巧植物，超级容易养护', imageUrl: '/images/plants/peperomia.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'biweekly' },
  { id: 29, commonName: '酒瓶兰', scientificName: 'Beaucarnea recurvata', slug: 'ponytail-palm', shortDescription: '独特的瓶状茎干储存水分，几乎杀不死', imageUrl: '/images/plants/ponytail-palm.jpg', difficulty: 'easy', lightLevel: 'bright_direct', waterFrequency: 'rarely' },
  { id: 31, commonName: '绿宝石', scientificName: 'Scindapsus pictus', slug: 'satin-pothos', shortDescription: '银绿色斑点的优雅藤本，适合悬挂种植', imageUrl: '/images/plants/satin-pothos.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'biweekly' },
  { id: 32, commonName: '白雪公主', scientificName: 'Aglaonema commutatum', slug: 'chinese-evergreen', shortDescription: '银白色斑纹的优雅观叶植物，极度耐阴', imageUrl: '/images/plants/chinese-evergreen.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'weekly' },
  { id: 33, commonName: '龙血树', scientificName: 'Dracaena marginata', slug: 'dragon-tree', shortDescription: '细长叶片的建筑感室内树木，现代简约风格', imageUrl: '/images/plants/dragon-tree.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'biweekly' },
  { id: 34, commonName: '仙客来', scientificName: 'Cyclamen persicum', slug: 'cyclamen', shortDescription: '优雅的冬季开花植物，花朵独特迷人', imageUrl: '/images/plants/cyclamen.jpg', difficulty: 'medium', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 35, commonName: '倒挂金钟', scientificName: 'Fuchsia spp.', slug: 'fuchsia', shortDescription: '灯笼状花朵垂吊绽放，色彩绚丽', imageUrl: '/images/plants/fuchsia.jpg', difficulty: 'medium', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 36, commonName: '马醉木', scientificName: 'Pieris japonica', slug: 'pieris-japonica', shortDescription: '飘逸灵动、自带日式高雅禅意的水培网红树', imageUrl: '/images/plants/pieris-japonica.jpg', difficulty: 'hard', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 37, commonName: '油画婚礼紫露草', scientificName: 'Tradescantia fluminensis Quadricolor', slug: 'tradescantia-quadricolor', shortDescription: '粉白绿紫四色交织，如梦似幻的油画色彩爆盆小仙女', imageUrl: '/images/plants/tradescantia-quadricolor.jpg', difficulty: 'easy', lightLevel: 'high', waterFrequency: 'weekly' },
  { id: 38, commonName: '琴叶喜林芋', scientificName: 'Philodendron panduriforme', slug: 'philodendron-panduriforme', shortDescription: '大叶提琴造型独特，挺拔油亮的高端热带雨林大木柱', imageUrl: '/images/plants/philodendron-panduriforme.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 39, commonName: '青叶冷水花', scientificName: 'Pilea cadierei', slug: 'pilea-cadierei', shortDescription: '银绿相间、金属质感浓郁的耐阴小草本', imageUrl: '/images/plants/pilea-cadierei.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'frequent' },
  { id: 40, commonName: '鸟巢蕨', scientificName: 'Asplenium nidus', slug: 'asplenium-nidus', shortDescription: '环抱如巢，翠绿光滑且极度耐阴的热带雨林林下附生蕨', imageUrl: '/images/plants/asplenium-nidus.jpg', difficulty: 'easy', lightLevel: 'low', waterFrequency: 'frequent' },
  { id: 41, commonName: '黑叶观音莲', scientificName: 'Alocasia amazonica', slug: 'alocasia-amazonica', shortDescription: '墨黑箭叶交织银白脉络，极具现代冷艳感的高端魔幻绿植', imageUrl: '/images/plants/alocasia-amazonica.jpg', difficulty: 'medium', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 42, commonName: '金钻蔓绿绒', scientificName: 'Philodendron Imperial Green', slug: 'philodendron-imperial-green', shortDescription: '叶片肥硕如漆，硬朗挺拔且抗逆性极强的大器镇宅绿植', imageUrl: '/images/plants/philodendron-imperial-green.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 43, commonName: '千手观音海芋', scientificName: 'Alocasia cucullata', slug: 'alocasia-cucullata', shortDescription: '叶形如心，叶丛层叠密集如千手舒展的灵动吐水神草', imageUrl: '/images/plants/alocasia-cucullata.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 44, commonName: '彩叶芋', scientificName: 'Caladium bicolor', slug: 'caladium-bicolor', shortDescription: '叶薄如蝉翼、白粉红交织如天使翅膀的华丽彩色艺术品', imageUrl: '/images/plants/caladium-bicolor.jpg', difficulty: 'hard', lightLevel: 'high', waterFrequency: 'frequent' },
  { id: 45, commonName: '常春藤', scientificName: 'Hedera helix', slug: 'hedera-helix', shortDescription: '枫叶状彩叶向四周弯曲悬垂，抗寒且吸毒净化全能的垂吊功勋绿植', imageUrl: '/images/plants/hedera-helix.jpg', difficulty: 'easy', lightLevel: 'medium', waterFrequency: 'weekly' },
  { id: 46, commonName: '九里香', scientificName: 'Murraya exotica', slug: 'murraya-exotica', shortDescription: '白花如玉，香飘九里，自带古典文人风骨的常青小树盆景', imageUrl: '/images/plants/murraya-exotica.jpg', difficulty: 'easy', lightLevel: 'bright_direct', waterFrequency: 'weekly' },
  { id: 47, commonName: '向日葵', scientificName: 'Helianthus annuus', slug: 'helianthus-annuus', shortDescription: '金黄夺目、追随阳光的阳光活力代言人', imageUrl: '/images/plants/sunflower.jpg', difficulty: 'easy', lightLevel: 'bright_direct', waterFrequency: 'frequent' },
  { id: 48, commonName: '薄荷', scientificName: 'Mentha canadensis', slug: 'mentha-canadensis', shortDescription: '清香提神，遇水即活的居家调味芳香草本', imageUrl: '/images/plants/mint.jpg', difficulty: 'easy', lightLevel: 'bright_direct', waterFrequency: 'frequent' },
  { id: 49, commonName: '无尽夏', scientificName: 'Hydrangea macrophylla Endless Summer', slug: 'hydrangea-macrophylla-endless-summer', shortDescription: '新老枝开花不断，能随土壤酸碱度自动魔幻变色的顶级观花网红', imageUrl: '/images/plants/hydrangea-endless-summer.jpg', difficulty: 'medium', lightLevel: 'medium', waterFrequency: 'frequent' },
  { id: 50, commonName: '多肉吉娃娃', scientificName: 'Echeveria chihuahuensis', slug: 'echeveria-chihuahuensis', shortDescription: '叶覆白霜，叶尖一抹惊艳小红尖的经典莲座多肉小硬汉', imageUrl: '/images/plants/echeveria-chihuahuensis.jpg', difficulty: 'easy', lightLevel: 'bright_direct', waterFrequency: 'rarely' },
  { id: 51, commonName: '文心兰', scientificName: 'Oncidium flexuosum', slug: 'oncidium-flexuosum', shortDescription: '花形似翩翩起舞的黄裙女郎，散发甜蜜巧克力奇香的高档附生兰花', imageUrl: '/images/plants/oncidium.jpg', difficulty: 'hard', lightLevel: 'medium', waterFrequency: 'weekly' },
]

async function fetchPlants() {
  loading.value = true
  try {
    const params: Record<string, string> = { page: '1', size: '50' }
    if (searchQuery.value) params.search = searchQuery.value
    if (activeLight.value) params.light = activeLight.value
    if (activeDifficulty.value) params.difficulty = activeDifficulty.value
    if (activeCategory.value) params.category = activeCategory.value

    const res: any = await request.get('/plants', { params })
    plants.value = res.data?.records || []
  } catch {
    let filtered = [...allMockPlants]
    if (activeDifficulty.value) {
      filtered = filtered.filter(p => p.difficulty === activeDifficulty.value)
    }
    if (activeLight.value) {
      filtered = filtered.filter(p => p.lightLevel === activeLight.value)
    }
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase()
      filtered = filtered.filter(p =>
        p.commonName.toLowerCase().includes(q) ||
        p.scientificName.toLowerCase().includes(q)
      )
    }
    plants.value = filtered
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res: any = await request.get('/categories')
    categories.value = res.data || []
  } catch {
    categories.value = [
      { id: 1, name: '多肉植物', slug: 'succulents' },
      { id: 2, name: '热带植物', slug: 'tropical' },
      { id: 3, name: '香草植物', slug: 'herbs' },
      { id: 4, name: '蕨类植物', slug: 'ferns' },
      { id: 5, name: '开花植物', slug: 'flowering' },
      { id: 6, name: '室内树木', slug: 'trees' },
    ]
  }
}

function goToPlant(slug: string) {
  router.push(`/plant/${slug}`)
}

function clearFilters() {
  searchQuery.value = ''
  activeCategory.value = ''
  activeDifficulty.value = ''
  activeLight.value = ''
  router.replace({ query: {} })
}

onMounted(() => {
  const cat = route.query.category as string
  if (cat) {
    activeCategory.value = cat
  }
  fetchPlants()
  fetchCategories()
})

watch([searchQuery, activeDifficulty, activeLight, activeCategory], () => {
  fetchPlants()
})
</script>

<template>
  <div class="encyclopedia">
    <div class="encyclopedia__hero">
      <h1 class="encyclopedia__title">{{ $t('encyclopedia.title') }}</h1>
      <p class="encyclopedia__subtitle">{{ $t('encyclopedia.subtitle') }}</p>

      <div class="encyclopedia__search">
        <svg class="encyclopedia__search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <path d="m21 21-4.3-4.3" />
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          :placeholder="$t('encyclopedia.searchPlaceholder')"
          class="encyclopedia__search-input"
        />
      </div>
    </div>

    <div class="encyclopedia__content">
      <aside class="encyclopedia__filters">
        <div class="encyclopedia__filter-group" v-if="categories.length">
          <h3 class="encyclopedia__filter-title">{{ $t('nav.encyclopedia') }}</h3>
          <div class="encyclopedia__filter-options">
            <button
              class="encyclopedia__filter-btn"
              :class="{ 'encyclopedia__filter-btn--active': activeCategory === '' }"
              @click="activeCategory = ''"
            >
              {{ $t('encyclopedia.all') }}
            </button>
            <button
              v-for="cat in categories"
              :key="cat.slug"
              class="encyclopedia__filter-btn"
              :class="{ 'encyclopedia__filter-btn--active': activeCategory === cat.slug }"
              @click="activeCategory = cat.slug"
            >
              {{ cat.name }}
            </button>
          </div>
        </div>

        <div class="encyclopedia__filter-group">
          <h3 class="encyclopedia__filter-title">{{ $t('encyclopedia.difficulty') }}</h3>
          <div class="encyclopedia__filter-options">
            <button
              v-for="d in ['', 'easy', 'medium', 'hard']"
              :key="d"
              class="encyclopedia__filter-btn"
              :class="{ 'encyclopedia__filter-btn--active': activeDifficulty === d }"
              @click="activeDifficulty = d"
            >
              {{ d === '' ? $t('encyclopedia.all') : d === 'easy' ? $t('encyclopedia.easy') : d === 'medium' ? $t('encyclopedia.medium') : $t('encyclopedia.hard') }}
            </button>
          </div>
        </div>

        <div class="encyclopedia__filter-group">
          <h3 class="encyclopedia__filter-title">{{ $t('encyclopedia.lightLevel') }}</h3>
          <div class="encyclopedia__filter-options">
            <button
              v-for="l in ['', 'low', 'medium', 'high', 'bright_direct']"
              :key="l"
              class="encyclopedia__filter-btn"
              :class="{ 'encyclopedia__filter-btn--active': activeLight === l }"
              @click="activeLight = l"
            >
              {{ lightLabels[l] || $t('encyclopedia.all') }}
            </button>
          </div>
        </div>

        <button class="encyclopedia__clear-btn" @click="clearFilters">
          {{ $t('encyclopedia.clearFilters') }}
        </button>
      </aside>

      <div class="encyclopedia__grid">
        <div v-if="loading" class="encyclopedia__loading">
          <div class="encyclopedia__loading-seed">
            <div class="encyclopedia__loading-sprout" />
          </div>
          <p>{{ $t('encyclopedia.loading') }}</p>
        </div>

        <div
          v-for="plant in plants"
          v-else
          :key="plant.id"
          class="encyclopedia__card"
          @click="goToPlant(plant.slug)"
        >
          <div class="encyclopedia__card-image">
            <img
              :src="plant.imageUrl"
              :alt="plant.commonName"
              class="encyclopedia__card-img"
              loading="lazy"
            />
          </div>
          <div class="encyclopedia__card-body">
            <h3 class="encyclopedia__card-name">{{ plant.commonName }}</h3>
            <p class="encyclopedia__card-sci">{{ plant.scientificName }}</p>
            <p class="encyclopedia__card-desc">{{ plant.shortDescription }}</p>
            <div class="encyclopedia__card-meta">
              <span
                class="encyclopedia__card-badge"
                :style="{ background: difficultyColors[plant.difficulty] || '#22c55e' }"
              >
                {{ difficultyLabels[plant.difficulty] || plant.difficulty }}
              </span>
              <span class="encyclopedia__card-info">{{ lightLabels[plant.lightLevel] }}</span>
              <span class="encyclopedia__card-info">{{ waterLabels[plant.waterFrequency] }}</span>
            </div>
          </div>
        </div>

        <div v-if="!loading && plants.length === 0" class="encyclopedia__empty">
          <p>{{ $t('encyclopedia.noResults') }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.encyclopedia {
  min-height: 100vh;

  &__hero {
    padding: 6rem 1.5rem 3rem;
    text-align: center;
    background: linear-gradient(135deg, $color-leaf-800, $color-leaf-900);
  }

  &__title {
    font-family: $font-display;
    font-size: clamp(2rem, 4vw, 3rem);
    color: #f0fdf4;
    margin-bottom: 0.5rem;
  }

  &__subtitle {
    color: rgba(240, 253, 244, 0.7);
    font-size: 1.1rem;
    margin-bottom: 2rem;
  }

  &__search {
    max-width: 500px;
    margin: 0 auto;
    position: relative;
  }

  &__search-icon {
    position: absolute;
    left: 1rem;
    top: 50%;
    transform: translateY(-50%);
    width: 20px;
    height: 20px;
    color: $color-text-muted;
  }

  &__search-input {
    width: 100%;
    padding: 0.875rem 1rem 0.875rem 3rem;
    border: none;
    border-radius: 50px;
    font-size: 1rem;
    background: white;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    outline: none;
    transition: box-shadow 0.3s ease;

    &:focus {
      box-shadow: 0 4px 20px rgba(22, 163, 74, 0.3);
    }

    &::placeholder {
      color: $color-text-muted;
    }
  }

  &__content {
    display: flex;
    gap: 2rem;
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem 1.5rem;
  }

  &__filters {
    width: 220px;
    flex-shrink: 0;
  }

  &__filter-group {
    margin-bottom: 1.5rem;
  }

  &__filter-title {
    font-size: 0.85rem;
    font-weight: 600;
    color: $color-text;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    margin-bottom: 0.75rem;
  }

  &__filter-options {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
  }

  &__filter-btn {
    padding: 0.4rem 0.8rem;
    border: 1px solid $color-border;
    border-radius: 20px;
    background: white;
    font-size: 0.8rem;
    color: $color-text;
    cursor: pointer;
    transition: all 0.2s ease;
    text-transform: capitalize;

    &:hover {
      border-color: $color-leaf-400;
      color: $color-leaf-600;
    }

    &--active {
      background: $color-leaf-600;
      border-color: $color-leaf-600;
      color: white;
    }
  }

  &__clear-btn {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid $color-border;
    border-radius: 8px;
    background: white;
    font-size: 0.85rem;
    color: $color-text-muted;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      border-color: $color-leaf-400;
      color: $color-leaf-600;
    }
  }

  &__grid {
    flex: 1;
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 1.5rem;
    align-content: start;
  }

  &__card {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.4s $ease-spring;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);

      .encyclopedia__card-image {
        transform: scale(1.03);
      }
    }
  }

  &__card-image {
    height: 180px;
    overflow: hidden;
    background: linear-gradient(135deg, $color-leaf-50, $color-leaf-100);
    transition: transform 0.6s $ease-out-expo;
  }

  &__card-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s ease;

    .encyclopedia__card:hover & {
      transform: scale(1.05);
    }
  }

  &__card-body {
    padding: 1.25rem;
  }

  &__card-name {
    font-family: $font-display;
    font-size: 1.1rem;
    font-weight: 600;
    color: $color-leaf-900;
    margin-bottom: 0.25rem;
  }

  &__card-sci {
    font-style: italic;
    font-size: 0.8rem;
    color: $color-text-muted;
    margin-bottom: 0.5rem;
  }

  &__card-desc {
    font-size: 0.85rem;
    color: $color-text;
    line-height: 1.5;
    margin-bottom: 0.75rem;
  }

  &__card-meta {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex-wrap: wrap;
  }

  &__card-badge {
    display: inline-block;
    padding: 0.15rem 0.5rem;
    border-radius: 20px;
    font-size: 0.7rem;
    font-weight: 600;
    color: white;
    text-transform: capitalize;
  }

  &__card-info {
    font-size: 0.75rem;
    color: $color-text-muted;
  }

  &__loading {
    grid-column: 1 / -1;
    text-align: center;
    padding: 4rem;
    color: $color-text-muted;
  }

  &__loading-seed {
    width: 40px;
    height: 40px;
    margin: 0 auto 1rem;
    background: $color-leaf-200;
    border-radius: 50% 50% 50% 0;
    transform: rotate(-45deg);
    animation: seedGrow 2s ease-in-out infinite;
  }

  &__empty {
    grid-column: 1 / -1;
    text-align: center;
    padding: 4rem;
    color: $color-text-muted;
  }
}

@keyframes seedGrow {
  0%, 100% { transform: rotate(-45deg) scale(1); }
  50% { transform: rotate(-45deg) scale(1.1); }
}

@media (max-width: 768px) {
  .encyclopedia {
    &__content {
      flex-direction: column;
    }

    &__filters {
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      gap: 1rem;
    }

    &__filter-group {
      margin-bottom: 0;
    }
  }
}
</style>
