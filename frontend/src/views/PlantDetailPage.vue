<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getPlant } from '@/api/plantApi'

const { t, locale } = useI18n()
const route = useRoute()

interface CareGuide {
  id: number
  careType: string
  title: string
  content: string
  tips: string
  commonMistakes: string
}

interface Plant {
  id: number
  commonName: string
  aliases: string
  scientificName: string
  slug: string
  family: string
  genusName: string
  plantType: string
  growthCycle: string
  ornamentalType: string
  origin: string
  description: string
  shortDescription: string
  leafShape: string
  leafColor: string
  stemFeature: string
  flowerShape: string
  flowerColor: string
  bloomMonth: string
  fruitPeriod: string
  overallShape: string
  lightLevel: string
  lightHoursMin: number
  lightHoursMax: number
  lightDescription: string
  waterFrequency: string
  waterIntervalDaysMin: number
  waterIntervalDaysMax: number
  waterDescription: string
  waterPrinciple: string
  waterSpring: string
  waterSummer: string
  waterFall: string
  waterWinter: string
  waterTaboo: string
  waterQuality: string
  humidityLevel: string
  humidityDescription: string
  tempMinCelsius: number
  tempMaxCelsius: number
  tempColdMin: number
  tempHeatMax: number
  summerDormancy: boolean
  suitablePosition: string
  growthHabit: string
  tempDescription: string
  fertilizerType: string
  fertilizerFrequency: string
  fertilizerDescription: string
  fertilizerBestSeason: string
  fertilizerGrow: string
  fertilizerBloom: string
  fertilizerStopDormancy: boolean
  fertilizerTaboo: string
  deficiencySymptom: string
  soilType: string
  soilRecipe: string
  soilPhMin: number
  soilPhMax: number
  potType: string
  potSizeSuggestion: string
  repotCycle: string
  pruneBestTime: string
  pruneParts: string
  pruneMethod: string
  pruneTaboo: string
  seasonalCare: string
  pestDisease: string
  toxicityLevel: string
  toxicParts: string
  toxicitySymptom: string
  petKidWarning: string
  commonProblems: string
  ornamentalValue: string
  airPurifyDetail: string
  edibleValue: string
  medicinalValue: string
  fengShui: string
  suitableScene: string
  difficulty: string
  growthRate: string
  imageUrl?: string
  maxHeightCm: number
  isIndoor: boolean
  isOutdoor: boolean
  isPetSafe: boolean
  isAirPurifying: boolean
  careGuides: CareGuide[]
}

const plant = ref<Plant | null>(null)
const loading = ref(true)
const error = ref(false)
const activeTab = ref('overview')

const difficultyColors: Record<string, string> = {
  easy: '#22c55e',
  medium: '#eab308',
  hard: '#ef4444',
}

const lightLevelMap = computed<Record<string, { label: string; level: number }>>(() => ({
  low: { label: t('encyclopedia.lowLight'), level: 1 },
  medium: { label: t('encyclopedia.mediumLight'), level: 2 },
  high: { label: t('encyclopedia.brightLight'), level: 3 },
  bright_direct: { label: t('encyclopedia.directSun'), level: 4 },
}))

const waterFreqMap = computed<Record<string, { label: string; level: number }>>(() => ({
  rarely: { label: t('encyclopedia.rarely'), level: 1 },
  biweekly: { label: t('encyclopedia.biweekly'), level: 2 },
  weekly: { label: t('encyclopedia.weekly'), level: 3 },
  frequent: { label: t('encyclopedia.frequent'), level: 4 },
}))

const humidityLabels = computed<Record<string, string>>(() => ({
  low: t('encyclopedia.humidityLow'),
  medium: t('encyclopedia.humidityMedium'),
  high: t('encyclopedia.humidityHigh'),
}))

const guideTypeLabels = computed<Record<string, string>>(() => ({
  watering: t('detail.water'),
  light: t('detail.light'),
  environment: t('detail.environment'),
  fertilizer: t('detail.fertilizer'),
  pruning: t('detail.pruneMethod'),
  general: t('detail.care'),
}))

const allTabs = computed(() => [
  'overview', 'morphology', 'environment', 'care',
  'seasonal', 'pest', 'safety', 'problems', 'value', 'guides'
])

function parseJson(val: string | null | undefined): any {
  if (!val) return null
  try { return JSON.parse(val) } catch { return null }
}

function copy(zh: string, en: string) {
  return locale.value === 'zh-CN' ? zh : en
}

function toJsonList(items: Array<string | number | null | undefined>) {
  return JSON.stringify(items.map((item) => String(item).trim()).filter(Boolean))
}

function joinText(items: Array<string | null | undefined>) {
  return items.map((item) => item?.trim()).filter(Boolean).join('\n')
}

const displayGuides = computed<CareGuide[]>(() => {
  if (!plant.value) return []
  if (plant.value.careGuides?.length) return plant.value.careGuides

  const p = plant.value
  const guides: CareGuide[] = []
  let fallbackId = -1

  function addGuide(
    careType: string,
    title: string,
    content: string | null | undefined,
    tips: Array<string | number | null | undefined> = [],
    commonMistakes: Array<string | number | null | undefined> = []
  ) {
    const cleanContent = content?.trim()
    if (!cleanContent) return
    guides.push({
      id: fallbackId--,
      careType,
      title,
      content: cleanContent,
      tips: toJsonList(tips),
      commonMistakes: toJsonList(commonMistakes),
    })
  }

  addGuide(
    'watering',
    copy('浇水指南', 'Watering guide'),
    p.waterDescription || p.waterPrinciple,
    [p.waterPrinciple, p.waterQuality, p.waterSpring, p.waterSummer, p.waterFall, p.waterWinter],
    [p.waterTaboo]
  )

  addGuide(
    'light',
    copy('光照指南', 'Light guide'),
    p.lightDescription,
    [
      p.lightHoursMin && p.lightHoursMax
        ? `${copy('建议光照', 'Suggested light')}: ${p.lightHoursMin}-${p.lightHoursMax} ${t('detail.hours')}`
        : null,
      lightLevelMap.value[p.lightLevel]?.label,
    ]
  )

  addGuide(
    'environment',
    copy('环境指南', 'Environment guide'),
    joinText([p.growthHabit, p.tempDescription, p.humidityDescription]),
    [p.suitablePosition, p.soilRecipe, p.potSizeSuggestion]
  )

  addGuide(
    'fertilizer',
    copy('施肥指南', 'Fertilizer guide'),
    p.fertilizerDescription || p.fertilizerGrow,
    [p.fertilizerBestSeason, p.fertilizerGrow, p.fertilizerBloom],
    [p.fertilizerTaboo]
  )

  addGuide(
    'pruning',
    copy('修剪指南', 'Pruning guide'),
    p.pruneMethod || p.pruneParts,
    [p.pruneBestTime, p.pruneParts],
    [p.pruneTaboo]
  )

  return guides
})

const problemKeyMap: Record<string, string> = {
  yellowLeaves: '黄叶',
  noBloom: '不开花',
  leggy: '徒长',
  drooping: '萎蔫下垂',
  mainTaboo: '主要禁忌',
  beginnerTip: '新手建议',
}

// i18n 缺失 key 时 t() 返回 key 字符串本身（truthy），不能用 || 做回退，用映射表
const growthLabels = computed<Record<string, string>>(() => ({
  slow: t('detail.slow'),
  moderate: t('detail.moderate'),
  fast: t('detail.fast'),
}))

async function fetchPlant() {
  const slug = route.params.slug as string
  loading.value = true
  error.value = false
  plant.value = null
  try {
    const res = await getPlant(slug)
    // 后端对不存在的 slug 返回 HTTP 200 + code 404，必须检查 code
    if (res.code === 200 && res.data) {
      plant.value = res.data as unknown as Plant
    } else {
      error.value = true
    }
  } catch {
    // 加载失败时显示错误提示，不伪造假数据
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(fetchPlant)

// 路由参数变化（前进/后退切换植物）时重新加载
watch(() => route.params.slug, fetchPlant)
</script>

<template>
  <div class="detail" v-if="plant">
    <div class="detail__hero">
      <div class="detail__hero-image">
        <img v-if="plant.imageUrl" :src="plant.imageUrl" :alt="plant.commonName" class="detail__hero-img" />
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c1.5 0 3-.3 4.3-.9C10 18 6 14 6 12c0-4.4 3.6-8 8-8 .7 0 1.4.1 2 .2C15 3.5 13.5 2 12 2z" />
        </svg>
      </div>
      <div class="detail__hero-info">
        <span class="detail__badge" :style="{ background: difficultyColors[plant.difficulty] }">
          {{ plant.difficulty === 'easy' ? $t('encyclopedia.easy') : plant.difficulty === 'medium' ? $t('encyclopedia.medium') : $t('encyclopedia.hard') }}
        </span>
        <h1 class="detail__name">{{ plant.commonName }}</h1>
        <p class="detail__sci">{{ plant.scientificName }}</p>
        <p class="detail__family" v-if="plant.family">{{ plant.family }}{{ plant.genusName ? ' · ' + plant.genusName : '' }} · {{ plant.origin }}</p>
        <p class="detail__aliases" v-if="plant.aliases">{{ $t('detail.aliases') }}：{{ plant.aliases }}</p>
        <div class="detail__tags">
          <span v-if="plant.isIndoor" class="detail__tag">{{ $t('detail.indoor') }}</span>
          <span v-if="plant.isOutdoor" class="detail__tag">{{ $t('detail.outdoor') }}</span>
          <span v-if="plant.isPetSafe" class="detail__tag detail__tag--green">{{ $t('detail.petSafe') }}</span>
          <span v-if="plant.isAirPurifying" class="detail__tag detail__tag--blue">{{ $t('detail.airPurifying') }}</span>
          <span v-if="plant.plantType" class="detail__tag">{{ plant.plantType }}</span>
          <span v-if="plant.growthCycle" class="detail__tag">{{ plant.growthCycle }}</span>
          <span v-if="plant.ornamentalType" class="detail__tag">{{ plant.ornamentalType }}</span>
          <span v-if="plant.toxicityLevel && plant.toxicityLevel !== '无毒'" class="detail__tag detail__tag--red">{{ plant.toxicityLevel }}</span>
        </div>
      </div>
    </div>

    <div class="detail__tabs">
      <button v-for="tab in allTabs" :key="tab" class="detail__tab" :class="{ 'detail__tab--active': activeTab === tab }" @click="activeTab = tab">
        {{ $t('detail.' + tab) }}
      </button>
    </div>

    <div class="detail__content">
      <!-- 概述 -->
      <div v-if="activeTab === 'overview'" class="detail__overview">
        <p class="detail__desc">{{ plant.description }}</p>
        <div class="detail__specs">
          <div class="detail__spec">
            <span class="detail__spec-label">{{ $t('detail.growthRate') }}</span>
            <span class="detail__spec-value">{{ growthLabels[plant.growthRate] || plant.growthRate }}</span>
          </div>
          <div class="detail__spec">
            <span class="detail__spec-label">{{ $t('detail.maxHeight') }}</span>
            <span class="detail__spec-value">{{ plant.maxHeightCm }} {{ $t('detail.cm') }}</span>
          </div>
          <div class="detail__spec">
            <span class="detail__spec-label">{{ $t('detail.fertilizer') }}</span>
            <span class="detail__spec-value">{{ plant.fertilizerType }}</span>
          </div>
          <div class="detail__spec" v-if="plant.suitablePosition">
            <span class="detail__spec-label">{{ $t('detail.suitablePosition') }}</span>
            <span class="detail__spec-value">{{ plant.suitablePosition }}</span>
          </div>
          <div class="detail__spec" v-if="plant.growthHabit">
            <span class="detail__spec-label">{{ $t('detail.growthHabit') }}</span>
            <span class="detail__spec-value">{{ plant.growthHabit }}</span>
          </div>
        </div>
      </div>

      <!-- 形态特征 -->
      <div v-if="activeTab === 'morphology'" class="detail__section">
        <div class="detail__info-grid">
          <div class="detail__info-item" v-if="plant.leafShape">
            <span class="detail__info-label">{{ $t('detail.leafShape') }}</span>
            <span class="detail__info-value">{{ plant.leafShape }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.leafColor">
            <span class="detail__info-label">{{ $t('detail.leafColor') }}</span>
            <span class="detail__info-value">{{ plant.leafColor }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.stemFeature">
            <span class="detail__info-label">{{ $t('detail.stemFeature') }}</span>
            <span class="detail__info-value">{{ plant.stemFeature }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.flowerShape">
            <span class="detail__info-label">{{ $t('detail.flowerShape') }}</span>
            <span class="detail__info-value">{{ plant.flowerShape }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.flowerColor">
            <span class="detail__info-label">{{ $t('detail.flowerColor') }}</span>
            <span class="detail__info-value">{{ plant.flowerColor }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.bloomMonth">
            <span class="detail__info-label">{{ $t('detail.bloomMonth') }}</span>
            <span class="detail__info-value">{{ plant.bloomMonth }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.fruitPeriod">
            <span class="detail__info-label">{{ $t('detail.fruitPeriod') }}</span>
            <span class="detail__info-value">{{ plant.fruitPeriod }}</span>
          </div>
        </div>
        <div class="detail__text-block" v-if="plant.overallShape">
          <h3>{{ $t('detail.overallShape') }}</h3>
          <p>{{ plant.overallShape }}</p>
        </div>
      </div>

      <!-- 生长环境 -->
      <div v-if="activeTab === 'environment'" class="detail__section">
        <div class="detail__care-grid">
          <div class="detail__care-card">
            <div class="detail__care-icon">☀️</div>
            <h3>{{ $t('detail.light') }}</h3>
            <div class="detail__care-meter">
              <div class="detail__care-fill detail__care-fill--sun" :style="{ width: `${(lightLevelMap[plant.lightLevel]?.level || 1) * 25}%` }" />
            </div>
            <p class="detail__care-label">{{ lightLevelMap[plant.lightLevel]?.label }}</p>
            <p class="detail__care-desc" v-if="plant.lightHoursMin">{{ $t('detail.lightHours') }}：{{ plant.lightHoursMin }}-{{ plant.lightHoursMax }} {{ $t('detail.hours') }}</p>
            <p class="detail__care-desc">{{ plant.lightDescription }}</p>
          </div>
          <div class="detail__care-card">
            <div class="detail__care-icon">💧</div>
            <h3>{{ $t('detail.water') }}</h3>
            <div class="detail__care-meter">
              <div class="detail__care-fill detail__care-fill--water" :style="{ width: `${(waterFreqMap[plant.waterFrequency]?.level || 1) * 25}%` }" />
            </div>
            <p class="detail__care-label">{{ waterFreqMap[plant.waterFrequency]?.label }}</p>
            <p class="detail__care-desc">{{ plant.waterDescription }}</p>
          </div>
          <div class="detail__care-card">
            <div class="detail__care-icon">🌡️</div>
            <h3>{{ $t('detail.temperature') }}</h3>
            <div class="detail__care-meter">
              <div class="detail__care-fill detail__care-fill--temp" style="width: 70%" />
            </div>
            <p class="detail__care-label">{{ plant.tempMinCelsius }}-{{ plant.tempMaxCelsius }}°C</p>
            <p class="detail__care-desc" v-if="plant.tempColdMin">{{ $t('detail.coldHardy') }}：{{ plant.tempColdMin }}°C · {{ $t('detail.heatTolerance') }}：{{ plant.tempHeatMax }}°C</p>
            <p class="detail__care-desc" v-if="plant.summerDormancy">{{ $t('detail.summerDormancy') }}：{{ $t('detail.yes') }}</p>
            <p class="detail__care-desc">{{ plant.tempDescription }}</p>
          </div>
          <div class="detail__care-card">
            <div class="detail__care-icon">💨</div>
            <h3>{{ $t('detail.humidity') }}</h3>
            <div class="detail__care-meter">
              <div class="detail__care-fill detail__care-fill--humidity" :style="{ width: plant.humidityLevel === 'high' ? '100%' : plant.humidityLevel === 'medium' ? '66%' : '33%' }" />
            </div>
            <p class="detail__care-label">{{ humidityLabels[plant.humidityLevel] || plant.humidityLevel }}</p>
            <p class="detail__care-desc">{{ plant.humidityDescription }}</p>
          </div>
        </div>
        <div class="detail__info-grid" style="margin-top:1.5rem">
          <div class="detail__info-item" v-if="plant.soilType">
            <span class="detail__info-label">{{ $t('detail.soilType') }}</span>
            <span class="detail__info-value">{{ plant.soilType }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.soilRecipe">
            <span class="detail__info-label">{{ $t('detail.soilRecipe') }}</span>
            <span class="detail__info-value">{{ plant.soilRecipe }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.soilPhMin">
            <span class="detail__info-label">{{ $t('detail.soilPh') }}</span>
            <span class="detail__info-value">{{ plant.soilPhMin }}-{{ plant.soilPhMax }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.potType">
            <span class="detail__info-label">{{ $t('detail.potType') }}</span>
            <span class="detail__info-value">{{ plant.potType }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.potSizeSuggestion">
            <span class="detail__info-label">{{ $t('detail.potSize') }}</span>
            <span class="detail__info-value">{{ plant.potSizeSuggestion }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.repotCycle">
            <span class="detail__info-label">{{ $t('detail.repotCycle') }}</span>
            <span class="detail__info-value">{{ plant.repotCycle }}</span>
          </div>
        </div>
      </div>

      <!-- 养护管理 -->
      <div v-if="activeTab === 'care'" class="detail__section">
        <div class="detail__info-grid">
          <div class="detail__info-item" v-if="plant.waterPrinciple">
            <span class="detail__info-label">{{ $t('detail.waterPrinciple') }}</span>
            <span class="detail__info-value">{{ plant.waterPrinciple }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.waterQuality">
            <span class="detail__info-label">{{ $t('detail.waterQuality') }}</span>
            <span class="detail__info-value">{{ plant.waterQuality }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.waterTaboo">
            <span class="detail__info-label">{{ $t('detail.waterTaboo') }}</span>
            <span class="detail__info-value">{{ plant.waterTaboo }}</span>
          </div>
        </div>
        <div class="detail__season-water" v-if="plant.waterSpring">
          <h3>{{ $t('detail.waterSeason') }}</h3>
          <div class="detail__season-grid">
            <div class="detail__season-item"><span class="detail__season-tag spring">🌱 {{ $t('detail.spring') }}</span>{{ plant.waterSpring }}</div>
            <div class="detail__season-item"><span class="detail__season-tag summer">☀️ {{ $t('detail.summer') }}</span>{{ plant.waterSummer }}</div>
            <div class="detail__season-item"><span class="detail__season-tag fall">🍂 {{ $t('detail.fall') }}</span>{{ plant.waterFall }}</div>
            <div class="detail__season-item"><span class="detail__season-tag winter">❄️ {{ $t('detail.winter') }}</span>{{ plant.waterWinter }}</div>
          </div>
        </div>
        <div class="detail__info-grid" style="margin-top:1.5rem">
          <div class="detail__info-item" v-if="plant.fertilizerBestSeason">
            <span class="detail__info-label">{{ $t('detail.fertilizerSeason') }}</span>
            <span class="detail__info-value">{{ plant.fertilizerBestSeason }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.fertilizerGrow">
            <span class="detail__info-label">{{ $t('detail.fertilizerGrow') }}</span>
            <span class="detail__info-value">{{ plant.fertilizerGrow }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.fertilizerBloom">
            <span class="detail__info-label">{{ $t('detail.fertilizerBloom') }}</span>
            <span class="detail__info-value">{{ plant.fertilizerBloom }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.fertilizerTaboo">
            <span class="detail__info-label">{{ $t('detail.fertilizerTaboo') }}</span>
            <span class="detail__info-value">{{ plant.fertilizerTaboo }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.deficiencySymptom">
            <span class="detail__info-label">{{ $t('detail.deficiency') }}</span>
            <span class="detail__info-value">{{ plant.deficiencySymptom }}</span>
          </div>
        </div>
        <div class="detail__info-grid" style="margin-top:1.5rem" v-if="plant.pruneBestTime">
          <h3 style="grid-column:1/-1;margin-bottom:0">{{ $t('detail.pruneTime') }}</h3>
          <div class="detail__info-item">
            <span class="detail__info-label">{{ $t('detail.pruneTime') }}</span>
            <span class="detail__info-value">{{ plant.pruneBestTime }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.pruneParts">
            <span class="detail__info-label">{{ $t('detail.pruneParts') }}</span>
            <span class="detail__info-value">{{ plant.pruneParts }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.pruneMethod">
            <span class="detail__info-label">{{ $t('detail.pruneMethod') }}</span>
            <span class="detail__info-value">{{ plant.pruneMethod }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.pruneTaboo">
            <span class="detail__info-label">{{ $t('detail.pruneTaboo') }}</span>
            <span class="detail__info-value">{{ plant.pruneTaboo }}</span>
          </div>
        </div>
      </div>

      <!-- 四季养护 -->
      <div v-if="activeTab === 'seasonal'" class="detail__section">
        <template v-if="parseJson(plant.seasonalCare)">
          <div class="detail__seasonal-cards">
            <div class="detail__seasonal-card" v-for="(val, key) in parseJson(plant.seasonalCare)" :key="key">
              <span class="detail__seasonal-icon">{{ String(key) === 'spring' ? '🌱' : String(key) === 'summer' ? '☀️' : String(key) === 'fall' ? '🍂' : '❄️' }}</span>
              <h4>{{ $t('detail.' + String(key)) }}</h4>
              <p>{{ val }}</p>
            </div>
          </div>
        </template>
        <p v-else class="detail__empty">{{ $t('detail.loading') }}</p>
      </div>

      <!-- 病虫害防治 -->
      <div v-if="activeTab === 'pest'" class="detail__section">
        <template v-if="parseJson(plant.pestDisease)">
          <div v-if="parseJson(plant.pestDisease).diseases?.length">
            <h3 class="detail__sub-title">{{ $t('detail.commonDiseases') }}</h3>
            <div class="detail__pest-list">
              <div class="detail__pest-item" v-for="(d, i) in parseJson(plant.pestDisease).diseases" :key="i">
                <h4>{{ d.name }}</h4>
                <p><strong>{{ $t('detail.symptom') }}：</strong>{{ d.symptom }}</p>
                <p><strong>{{ $t('detail.cause') }}：</strong>{{ d.cause }}</p>
                <p><strong>{{ $t('detail.treatment') }}：</strong>{{ d.treatment }}</p>
              </div>
            </div>
          </div>
          <div v-if="parseJson(plant.pestDisease).pests?.length">
            <h3 class="detail__sub-title">{{ $t('detail.commonPests') }}</h3>
            <div class="detail__pest-list">
              <div class="detail__pest-item" v-for="(p, i) in parseJson(plant.pestDisease).pests" :key="i">
                <h4>{{ p.name }}</h4>
                <p><strong>{{ $t('detail.symptom') }}：</strong>{{ p.symptom }}</p>
                <p><strong>{{ $t('detail.control') }}：</strong>{{ p.treatment }}</p>
              </div>
            </div>
          </div>
          <div class="detail__text-block" v-if="parseJson(plant.pestDisease).prevention">
            <h3>{{ $t('detail.dailyPrevention') }}</h3>
            <p>{{ parseJson(plant.pestDisease).prevention }}</p>
          </div>
        </template>
        <p v-else class="detail__empty">{{ $t('detail.noPestData') }}</p>
      </div>

      <!-- 毒性安全 -->
      <div v-if="activeTab === 'safety'" class="detail__section">
        <div class="detail__info-grid">
          <div class="detail__info-item">
            <span class="detail__info-label">{{ $t('detail.toxicity') }}</span>
            <span class="detail__info-value" :class="{ 'detail__toxic': plant.toxicityLevel && plant.toxicityLevel !== '无毒' }">{{ plant.toxicityLevel || '无毒' }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.toxicParts">
            <span class="detail__info-label">{{ $t('detail.toxicParts') }}</span>
            <span class="detail__info-value">{{ plant.toxicParts }}</span>
          </div>
        </div>
        <div class="detail__text-block" v-if="plant.toxicitySymptom">
          <h3>{{ $t('detail.toxicityDetail') }}</h3>
          <p>{{ plant.toxicitySymptom }}</p>
        </div>
        <div class="detail__text-block" v-if="plant.petKidWarning">
          <h3>{{ $t('detail.kidPetWarning') }}</h3>
          <p>{{ plant.petKidWarning }}</p>
        </div>
      </div>

      <!-- 常见问题 -->
      <div v-if="activeTab === 'problems'" class="detail__section">
        <template v-if="parseJson(plant.commonProblems)">
          <div class="detail__problems-list">
            <div class="detail__problem-item" v-for="(val, key) in parseJson(plant.commonProblems)" :key="key">
              <h4>{{ problemKeyMap[String(key)] || key }}</h4>
              <p>{{ val }}</p>
            </div>
          </div>
        </template>
        <p v-else class="detail__empty">暂无常见问题数据</p>
      </div>

      <!-- 价值用途 -->
      <div v-if="activeTab === 'value'" class="detail__section">
        <div class="detail__info-grid">
          <div class="detail__info-item" v-if="plant.ornamentalValue">
            <span class="detail__info-label">{{ $t('detail.ornamentalValue') }}</span>
            <span class="detail__info-value">{{ plant.ornamentalValue }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.airPurifyDetail">
            <span class="detail__info-label">{{ $t('detail.airPurifyDetail') }}</span>
            <span class="detail__info-value">{{ plant.airPurifyDetail }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.edibleValue">
            <span class="detail__info-label">{{ $t('detail.edibleValue') }}</span>
            <span class="detail__info-value">{{ plant.edibleValue }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.medicinalValue">
            <span class="detail__info-label">{{ $t('detail.medicinalValue') }}</span>
            <span class="detail__info-value">{{ plant.medicinalValue }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.fengShui">
            <span class="detail__info-label">{{ $t('detail.fengShui') }}</span>
            <span class="detail__info-value">{{ plant.fengShui }}</span>
          </div>
          <div class="detail__info-item" v-if="plant.suitableScene">
            <span class="detail__info-label">{{ $t('detail.suitableScene') }}</span>
            <span class="detail__info-value">{{ plant.suitableScene }}</span>
          </div>
        </div>
      </div>

      <!-- 养护指南 -->
      <div v-if="activeTab === 'guides'" class="detail__guides">
        <template v-if="displayGuides.length">
          <div v-for="guide in displayGuides" :key="guide.id" class="detail__guide">
            <span class="detail__guide-type">
              {{ guideTypeLabels[guide.careType] || guide.careType }}
            </span>
            <h3 class="detail__guide-title">{{ guide.title }}</h3>
            <p class="detail__guide-content">{{ guide.content }}</p>
            <div v-if="parseJson(guide.tips)?.length" class="detail__guide-tips">
              <h4>{{ $t('detail.tips') }}</h4>
              <ul><li v-for="(tip, i) in parseJson(guide.tips)" :key="i">{{ tip }}</li></ul>
            </div>
            <div v-if="parseJson(guide.commonMistakes)?.length" class="detail__guide-mistakes">
              <h4>{{ $t('detail.commonMistakes') }}</h4>
              <ul><li v-for="(m, i) in parseJson(guide.commonMistakes)" :key="i">{{ m }}</li></ul>
            </div>
          </div>
        </template>
        <p v-else class="detail__empty detail__empty--guides">
          {{ copy('这个植物暂时还没有养护指南数据。', 'Care guide data is not available for this plant yet.') }}
        </p>
      </div>
    </div>
  </div>

  <div v-else-if="loading" class="detail__loading">
    <div class="detail__loading-spinner" />
    <p>{{ $t('detail.loading') }}</p>
  </div>

  <div v-else-if="error" class="detail__loading">
    <p>{{ $t('detail.loadError') }}</p>
  </div>
</template>

<style scoped lang="scss">
.detail {
  max-width: 1000px;
  margin: 0 auto;
  padding: 6rem 1.5rem 3rem;

  &__hero { display: flex; gap: 2rem; margin-bottom: 2rem; align-items: center; }
  &__hero-image {
    width: 200px; height: 200px; flex-shrink: 0;
    background: linear-gradient(135deg, $color-leaf-50, $color-leaf-100);
    border-radius: 20px; display: flex; align-items: center; justify-content: center; overflow: hidden;
    svg { width: 80px; height: 80px; color: $color-leaf-300; }
  }
  &__hero-img { width: 100%; height: 100%; object-fit: cover; }
  &__badge { display: inline-block; padding: 0.25rem 0.75rem; border-radius: 20px; font-size: 0.75rem; font-weight: 600; color: white; text-transform: capitalize; margin-bottom: 0.5rem; }
  &__name { font-family: $font-display; font-size: clamp(1.5rem, 3vw, 2.25rem); color: $color-leaf-900; margin-bottom: 0.25rem; }
  &__sci { font-style: italic; color: $color-text-muted; font-size: 1rem; margin-bottom: 0.5rem; }
  &__family { font-size: 0.9rem; color: $color-text-muted; margin-bottom: 0.25rem; }
  &__aliases { font-size: 0.85rem; color: $color-text-muted; margin-bottom: 0.75rem; }
  &__tags { display: flex; gap: 0.5rem; flex-wrap: wrap; }
  &__tag { padding: 0.2rem 0.6rem; border-radius: 12px; font-size: 0.75rem; background: $color-leaf-100; color: $color-leaf-700;
    &--green { background: #dcfce7; color: #166534; }
    &--blue { background: #dbeafe; color: #1e40af; }
    &--red { background: #fee2e2; color: #991b1b; }
  }
  &__tabs { display: flex; gap: 0; border-bottom: 2px solid $color-border; margin-bottom: 2rem; overflow-x: auto; flex-wrap: nowrap; }
  &__tab { padding: 0.75rem 1rem; border: none; background: none; font-size: 0.9rem; font-weight: 500; color: $color-text-muted; cursor: pointer; position: relative; transition: color 0.3s ease; white-space: nowrap; flex-shrink: 0;
    &--active { color: $color-leaf-600; &::after { content: ''; position: absolute; bottom: -2px; left: 0; right: 0; height: 2px; background: $color-leaf-500; } }
    &:hover { color: $color-leaf-600; }
  }
  &__desc { font-size: 1.05rem; line-height: 1.7; color: $color-text; margin-bottom: 2rem; }
  &__specs { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 1rem; }
  &__spec { padding: 1rem; background: $color-leaf-50; border-radius: 12px; }
  &__spec-label { display: block; font-size: 0.8rem; color: $color-text-muted; margin-bottom: 0.25rem; }
  &__spec-value { font-weight: 600; color: $color-leaf-800; }

  &__section { }
  &__info-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1rem; }
  &__info-item { padding: 1rem; background: $color-leaf-50; border-radius: 12px; }
  &__info-label { display: block; font-size: 0.8rem; color: $color-text-muted; margin-bottom: 0.25rem; }
  &__info-value { font-size: 0.95rem; color: $color-leaf-800; line-height: 1.5; }
  &__toxic { color: #dc2626; font-weight: 600; }

  &__text-block { margin-top: 1.5rem; padding: 1.25rem; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    h3 { font-family: $font-display; font-size: 1rem; color: $color-leaf-900; margin-bottom: 0.5rem; }
    p { font-size: 0.95rem; line-height: 1.6; color: $color-text; }
  }

  &__care-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 1.5rem; }
  &__care-card { padding: 1.5rem; background: white; border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); h3 { font-family: $font-display; font-size: 1rem; color: $color-leaf-900; margin-bottom: 0.75rem; } }
  &__care-icon { font-size: 2rem; margin-bottom: 0.75rem; }
  &__care-meter { height: 8px; background: #e5e7eb; border-radius: 4px; overflow: hidden; margin-bottom: 0.5rem; }
  &__care-fill { height: 100%; border-radius: 4px; transition: width 1s $ease-out-expo; &--sun { background: linear-gradient(90deg, #fde047, #f59e0b); } &--water { background: linear-gradient(90deg, #93c5fd, #3b82f6); } &--temp { background: linear-gradient(90deg, #fca5a5, #ef4444); } &--humidity { background: linear-gradient(90deg, #a5f3fc, #06b6d4); } }
  &__care-label { font-weight: 600; font-size: 0.9rem; color: $color-text; margin-bottom: 0.25rem; }
  &__care-desc { font-size: 0.85rem; color: $color-text-muted; line-height: 1.5; }

  &__season-water { margin-top: 1.5rem; padding: 1.25rem; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    h3 { font-family: $font-display; font-size: 1rem; color: $color-leaf-900; margin-bottom: 1rem; }
  }
  &__season-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0.75rem; }
  &__season-item { font-size: 0.9rem; color: $color-text; }
  &__season-tag { display: inline-block; font-weight: 600; margin-right: 0.5rem; }

  &__sub-title { font-family: $font-display; font-size: 1.1rem; color: $color-leaf-900; margin: 1.5rem 0 1rem; }
  &__pest-list { display: grid; gap: 1rem; }
  &__pest-item { padding: 1.25rem; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    h4 { font-family: $font-display; font-size: 1rem; color: $color-leaf-900; margin-bottom: 0.5rem; }
    p { font-size: 0.9rem; color: $color-text; line-height: 1.5; margin-bottom: 0.25rem; }
  }

  &__seasonal-cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem; }
  &__seasonal-card { padding: 1.5rem; background: white; border-radius: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); text-align: center;
    h4 { font-family: $font-display; font-size: 1rem; color: $color-leaf-900; margin: 0.5rem 0; }
    p { font-size: 0.9rem; color: $color-text; line-height: 1.5; text-align: left; }
  }
  &__seasonal-icon { font-size: 2rem; }

  &__problems-list { display: grid; gap: 0.75rem; }
  &__problem-item { padding: 1rem; background: white; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
    h4 { font-size: 0.9rem; font-weight: 600; color: $color-leaf-800; margin-bottom: 0.25rem; }
    p { font-size: 0.9rem; color: $color-text; line-height: 1.5; }
  }

  &__empty { text-align: center; padding: 3rem; color: $color-text-muted; }
  &__empty--guides {
    border: 1px dashed rgba(34, 197, 94, 0.25);
    border-radius: 16px;
    background: linear-gradient(135deg, rgba(240, 253, 244, 0.85), rgba(255, 255, 255, 0.96));
  }

  &__guides { }
  &__guide {
    padding: 1.5rem;
    background: white;
    border: 1px solid rgba(34, 197, 94, 0.1);
    border-radius: 16px;
    box-shadow: 0 12px 32px rgba(15, 118, 62, 0.07);
    margin-bottom: 1.5rem;
  }
  &__guide-type {
    display: inline-flex;
    align-items: center;
    margin-bottom: 0.75rem;
    padding: 0.25rem 0.65rem;
    border-radius: 999px;
    color: $color-leaf-700;
    background: rgba(34, 197, 94, 0.12);
    font-size: 0.75rem;
    font-weight: 700;
  }
  &__guide-title { font-family: $font-display; font-size: 1.15rem; color: $color-leaf-900; margin-bottom: 0.75rem; }
  &__guide-content { font-size: 0.95rem; line-height: 1.6; color: $color-text; margin-bottom: 1rem; }
  &__guide-tips, &__guide-mistakes { margin-top: 1rem; h4 { font-size: 0.9rem; margin-bottom: 0.5rem; } ul { list-style: none; padding: 0; } li { padding: 0.3rem 0; font-size: 0.85rem; color: $color-text; padding-left: 1.25rem; position: relative; &::before { content: '•'; position: absolute; left: 0; color: $color-leaf-500; } } }
  &__guide-mistakes li::before { color: #ef4444; }
  &__loading { min-height: 60vh; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 1rem; color: $color-text-muted; }
  &__loading-spinner { width: 40px; height: 40px; border: 3px solid $color-leaf-200; border-top-color: $color-leaf-600; border-radius: 50%; animation: spin 0.8s linear infinite; }
}

@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .detail {
    &__hero { flex-direction: column; text-align: center; }
    &__hero-image { width: 150px; height: 150px; }
    &__tags { justify-content: center; }
    &__tabs { padding-bottom: 0.5rem; }
    &__season-grid { grid-template-columns: 1fr; }
    &__seasonal-cards { grid-template-columns: 1fr; }
    &__info-grid { grid-template-columns: 1fr; }
  }
}
</style>
