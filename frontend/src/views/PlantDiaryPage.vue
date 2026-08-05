<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { AuthModal } from '@/components/common'
import * as diaryApi from '@/api/diaryApi'
import { uploadImage as uploadImageApi } from '@/api/uploadApi'
import { prepareImageForUpload } from '@/utils/imageUpload'
import type { Diary } from '@/types'

const { t, locale } = useI18n()
const auth = useAuthStore()

const diaries = ref<Diary[]>([])
const loading = ref(false)
const showModal = ref(false)
const submitting = ref(false)
const filterPlant = ref('')
const diarySearch = ref('')
const diaryView = ref<'timeline' | 'cards'>('timeline')
const diaryError = ref('')
const editingId = ref<number | null>(null)
const selectedDiary = ref<Diary | null>(null)
const uploadLoading = ref(false)
const uploadError = ref('')

const newDiary = ref({
  title: '',
  content: '',
  plantName: '',
  weather: '',
  mood: '',
  // v-model.number 在输入清空时实际值为 ''（空字符串），类型需包含它
  heightCm: null as number | '' | null,
  leafCount: null as number | '' | null,
  growthStage: '',
  images: [] as string[],
})

type DiaryOption = {
  id: string
  emoji: string
  zh: string
  en: string
  color?: string
}

const weatherOptions: DiaryOption[] = [
  { id: 'sunny', emoji: '☀️', zh: '晴', en: 'Sunny' },
  { id: 'cloudy', emoji: '⛅', zh: '多云', en: 'Cloudy' },
  { id: 'rainy', emoji: '🌧️', zh: '雨', en: 'Rainy' },
  { id: 'snowy', emoji: '❄️', zh: '雪', en: 'Snowy' },
  { id: 'foggy', emoji: '🌫️', zh: '雾', en: 'Foggy' },
]
const moodOptions: DiaryOption[] = [
  { id: 'happy', emoji: '😊', zh: '开心', en: 'Happy' },
  { id: 'blessed', emoji: '🥰', zh: '幸福', en: 'Content' },
  { id: 'calm', emoji: '😌', zh: '平静', en: 'Calm' },
  { id: 'thinking', emoji: '🤔', zh: '思考', en: 'Thinking' },
  { id: 'worried', emoji: '😤', zh: '担心', en: 'Concerned' },
  { id: 'surprised', emoji: '🤩', zh: '惊喜', en: 'Delighted' },
]
const growthStages: DiaryOption[] = [
  { id: 'seedling', emoji: '🌱', zh: '幼苗期', en: 'Seedling', color: '#22c55e' },
  { id: 'growing', emoji: '🌿', zh: '生长期', en: 'Growing', color: '#16a34a' },
  { id: 'flowering', emoji: '🌸', zh: '开花期', en: 'Flowering', color: '#f59e0b' },
  { id: 'dormant', emoji: '🍂', zh: '休眠期', en: 'Dormant', color: '#94a3b8' },
  { id: 'mature', emoji: '🌲', zh: '成熟期', en: 'Mature', color: '#15803d' },
]

const pageLanguageClass = computed(() => locale.value === 'zh-CN' ? 'diary-page--zh' : 'diary-page--en')

function uiText(zh: string, en: string) {
  return locale.value === 'zh-CN' ? zh : en
}

function optionValue(option: DiaryOption) {
  return `${option.emoji} ${option.zh}`
}

function optionLabel(option: DiaryOption) {
  return `${option.emoji} ${locale.value === 'zh-CN' ? option.zh : option.en}`
}

function optionMatches(value: string | null | undefined, option: DiaryOption) {
  if (!value) return false
  return value === optionValue(option)
    || value === `${option.emoji} ${option.en}`
    || value === option.zh
    || value === option.en
    || value.includes(option.zh)
    || value.includes(option.en)
}

function toggleOption(field: 'weather' | 'mood' | 'growthStage', option: DiaryOption) {
  newDiary.value[field] = optionMatches(newDiary.value[field], option) ? '' : optionValue(option)
}

function displayOption(value: string | null | undefined, options: DiaryOption[]) {
  const option = options.find((item) => optionMatches(value, item))
  return option ? optionLabel(option) : value || ''
}

// Lightbox
const lightboxImages = ref<string[]>([])
const lightboxIndex = ref(0)
const showLightbox = ref(false)

function openLightbox(images: string[], index: number) {
  lightboxImages.value = images
  lightboxIndex.value = index
  showLightbox.value = true
}

function closeLightbox() {
  showLightbox.value = false
}

function prevImage() {
  lightboxIndex.value = (lightboxIndex.value - 1 + lightboxImages.value.length) % lightboxImages.value.length
}

function nextImage() {
  lightboxIndex.value = (lightboxIndex.value + 1) % lightboxImages.value.length
}

// Auth modal
const authModalRef = ref<InstanceType<typeof AuthModal> | null>(null)

function openAuth(mode: 'login' | 'register') {
  authModalRef.value?.open(mode)
}

// Plant filter
const plantNames = computed(() => {
  const names = new Set(diaries.value.map(d => d.plantName).filter((name): name is string => !!name))
  return Array.from(names)
})

const filteredDiaries = computed(() => {
  const query = diarySearch.value.trim().toLowerCase()
  return diaries.value.filter((d) => {
    const matchesPlant = !filterPlant.value || d.plantName === filterPlant.value
    const matchesQuery = !query
      || d.title.toLowerCase().includes(query)
      || (d.content || '').toLowerCase().includes(query)
      || (d.plantName || '').toLowerCase().includes(query)
    return matchesPlant && matchesQuery
  })
})

// Group by date
const groupedDiaries = computed(() => {
  const groups: { date: string; entries: Diary[] }[] = []
  const map = new Map<string, Diary[]>()
  for (const d of filteredDiaries.value) {
    const date = d.createdAt.slice(0, 10)
    if (!map.has(date)) map.set(date, [])
    map.get(date)!.push(d)
  }
  for (const [date, entries] of map) {
    groups.push({ date, entries })
  }
  return groups
})

// Stats
const stats = computed(() => {
  const total = diaries.value.length
  const plants = new Set(diaries.value.map(d => d.plantName).filter(Boolean)).size
  const days = new Set(diaries.value.map(d => d.createdAt.slice(0, 10))).size
  return { total, plants, days }
})

const growthTrend = computed(() => {
  const entries = [...diaries.value]
    .filter(d => d.heightCm || d.leafCount)
    .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
    .slice(-8)

  const maxHeight = Math.max(...entries.map(d => d.heightCm || 0), 1)
  const maxLeaves = Math.max(...entries.map(d => d.leafCount || 0), 1)
  const points = entries.map((entry, index) => {
    const x = entries.length <= 1 ? 50 : (index / (entries.length - 1)) * 100
    const heightY = 100 - ((entry.heightCm || 0) / maxHeight) * 86 - 7
    const leafY = 100 - ((entry.leafCount || 0) / maxLeaves) * 86 - 7
    return { entry, x, heightY, leafY }
  })

  return {
    entries,
    heightLine: points.map(p => `${p.x},${p.heightY}`).join(' '),
    leafLine: points.map(p => `${p.x},${p.leafY}`).join(' '),
    points,
  }
})

async function loadDiaries() {
  if (!auth.isLoggedIn) return
  loading.value = true
  diaryError.value = ''
  try {
    const res = await diaryApi.listMyDiaries({ page: 1, size: 200 })
    if (res.code === 200) {
      diaries.value = res.data.records
    } else {
      diaryError.value = res.message || uiText('日记加载失败', 'Failed to load entries')
    }
  } catch {
    diaryError.value = uiText('日记加载失败，请稍后重试', 'Failed to load entries. Please try again.')
  } finally {
    loading.value = false
  }
}

async function submitDiary() {
  if (!newDiary.value.title.trim()) return
  submitting.value = true
  try {
    const payload: diaryApi.DiaryPayload = {
      title: newDiary.value.title,
      content: newDiary.value.content,
      plantName: newDiary.value.plantName,
      weather: newDiary.value.weather,
      mood: newDiary.value.mood,
      // v-model.number 清空后为 ''（空字符串），后端 Integer 无法解析，转 null
      heightCm: newDiary.value.heightCm === '' ? null : newDiary.value.heightCm,
      leafCount: newDiary.value.leafCount === '' ? null : newDiary.value.leafCount,
      growthStage: newDiary.value.growthStage,
      images: JSON.stringify(newDiary.value.images),
    }
    const res = editingId.value
      ? await diaryApi.updateDiary(editingId.value, payload)
      : await diaryApi.createDiary(payload)
    if (res.code === 200) {
      if (editingId.value) {
        diaries.value = diaries.value.map((d) => d.id === editingId.value ? res.data : d)
      } else {
        diaries.value.unshift(res.data)
      }
      showModal.value = false
      resetForm()
    }
  } finally {
    submitting.value = false
  }
}

async function deleteDiary(id: number) {
  if (!confirm(t('diary.confirmDelete'))) return
  try {
    const res = await diaryApi.deleteDiary(id)
    // 仅在后端确认成功后移除本地条目
    if (res.code === 200) {
      diaries.value = diaries.value.filter(d => d.id !== id)
    }
  } catch {}
}

async function uploadImage(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files?.length) return
  const file = input.files[0]
  if (uploadLoading.value) return
  uploadError.value = ''
  if (newDiary.value.images.length >= 6) {
    uploadError.value = uiText('最多上传 6 张图片', 'You can upload up to 6 images.')
    input.value = ''
    return
  }
  uploadLoading.value = true
  try {
    const prepared = await prepareImageForUpload(file, { locale: locale.value })
    // 不手动设置 Content-Type：让浏览器自动生成带 boundary 的 multipart 头
    const res = await uploadImageApi(prepared.file)
    if (res.code === 200) {
      newDiary.value.images.push(res.data)
    } else {
      uploadError.value = res.message || uiText('上传失败', 'Upload failed.')
    }
  } catch (err) {
    uploadError.value = err instanceof Error ? err.message : uiText('上传失败，请稍后重试', 'Upload failed. Please try again.')
  } finally {
    uploadLoading.value = false
  }
  input.value = ''
}

function removeImage(index: number) {
  newDiary.value.images.splice(index, 1)
}

function resetForm() {
  newDiary.value = { title: '', content: '', plantName: '', weather: '', mood: '', heightCm: null, leafCount: null, growthStage: '', images: [] }
  editingId.value = null
  uploadError.value = ''
}

function editDiary(entry: Diary) {
  editingId.value = entry.id
  newDiary.value = {
    title: entry.title,
    content: entry.content || '',
    plantName: entry.plantName || '',
    weather: entry.weather || '',
    mood: entry.mood || '',
    heightCm: entry.heightCm,
    leafCount: entry.leafCount,
    growthStage: entry.growthStage || '',
    images: parseImages(entry.images || ''),
  }
  showModal.value = true
}

function openDiaryDetail(entry: Diary) {
  selectedDiary.value = entry
}

function openCreateDiary() {
  resetForm()
  showModal.value = true
}

function formatDate(dateStr: string) {
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatTime(dateStr: string) {
  const d = new Date(dateStr)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatWeekday(dateStr: string) {
  const days = locale.value === 'zh-CN'
    ? ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    : ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
  return days[new Date(dateStr).getDay()]
}

function parseImages(images?: string): string[] {
  if (!images) return []
  try { return JSON.parse(images) } catch { return [] }
}

function getGrowthColor(stage: string) {
  const option = growthStages.find((item) => optionMatches(stage, item))
  if (option?.color) return option.color
  return '#6b7280'
}

onMounted(() => {
  if (auth.isLoggedIn) loadDiaries()
})

watch(() => auth.isLoggedIn, (loggedIn) => {
  if (loggedIn) loadDiaries()
})
</script>

<template>
  <div class="diary-page" :class="pageLanguageClass">
    <!-- Hero -->
    <div class="diary__hero">
      <svg class="diary__hero-icon" viewBox="0 0 64 64" fill="none">
        <path d="M16 8h32a4 4 0 014 4v40a4 4 0 01-4 4H16a4 4 0 01-4-4V12a4 4 0 014-4z" stroke="rgba(255,255,255,0.5)" stroke-width="2" fill="rgba(255,255,255,0.06)"/>
        <line x1="20" y1="16" x2="44" y2="16" stroke="rgba(255,255,255,0.3)" stroke-width="1.5"/>
        <line x1="20" y1="24" x2="40" y2="24" stroke="rgba(255,255,255,0.2)" stroke-width="1.5"/>
        <line x1="20" y1="32" x2="36" y2="32" stroke="rgba(255,255,255,0.2)" stroke-width="1.5"/>
        <path d="M38 40 Q44 34 50 42 Q44 36 38 40Z" fill="rgba(74,222,128,0.5)"/>
        <path d="M38 40 V52" stroke="rgba(74,222,128,0.4)" stroke-width="1.5" stroke-linecap="round"/>
        <path d="M38 46 Q34 42 30 46" stroke="rgba(74,222,128,0.3)" stroke-width="1" fill="none"/>
      </svg>
      <h1 class="diary__title">{{ t('diary.title') }}</h1>
      <p class="diary__subtitle">{{ t('diary.subtitle') }}</p>
    </div>

    <div class="diary__content">
      <!-- Not logged in -->
      <div v-if="!auth.isLoggedIn" class="diary__login-hint">
        <p>{{ t('diary.loginHint') }}</p>
        <button class="diary__login-btn" @click="openAuth('login')">{{ t('nav.login') }}</button>
      </div>

      <template v-else>
        <!-- Stats bar -->
        <div class="diary__stats">
          <div class="diary__stat">
            <span class="diary__stat-num">{{ stats.total }}</span>
            <span class="diary__stat-label">{{ t('diary.statEntries') }}</span>
          </div>
          <div class="diary__stat">
            <span class="diary__stat-num">{{ stats.plants }}</span>
            <span class="diary__stat-label">{{ t('diary.statPlants') }}</span>
          </div>
          <div class="diary__stat">
            <span class="diary__stat-num">{{ stats.days }}</span>
            <span class="diary__stat-label">{{ t('diary.statDays') }}</span>
          </div>
        </div>

        <div v-if="growthTrend.entries.length >= 2" class="diary__chart">
          <div class="diary__chart-head">
            <div>
              <h3>{{ uiText('生长趋势', 'Growth trend') }}</h3>
              <p>{{ uiText('最近 8 条有数据的记录', 'Latest 8 measured entries') }}</p>
            </div>
            <div class="diary__chart-legend">
              <span><i class="diary__chart-dot diary__chart-dot--height"></i>{{ t('diary.height') }}</span>
              <span><i class="diary__chart-dot diary__chart-dot--leaf"></i>{{ t('diary.leafCount') }}</span>
            </div>
          </div>
          <svg class="diary__chart-svg" viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
            <path d="M0 20 H100 M0 50 H100 M0 80 H100" class="diary__chart-grid" />
            <polyline v-if="growthTrend.heightLine" :points="growthTrend.heightLine" class="diary__chart-line diary__chart-line--height" />
            <polyline v-if="growthTrend.leafLine" :points="growthTrend.leafLine" class="diary__chart-line diary__chart-line--leaf" />
            <g v-for="point in growthTrend.points" :key="point.entry.id">
              <circle v-if="point.entry.heightCm" :cx="point.x" :cy="point.heightY" r="1.8" class="diary__chart-point diary__chart-point--height" />
              <circle v-if="point.entry.leafCount" :cx="point.x" :cy="point.leafY" r="1.8" class="diary__chart-point diary__chart-point--leaf" />
            </g>
          </svg>
          <div class="diary__chart-labels">
            <span v-for="point in growthTrend.points" :key="point.entry.id">{{ formatDate(point.entry.createdAt).slice(5) }}</span>
          </div>
        </div>

        <!-- Actions -->
        <div class="diary__actions">
          <div class="diary__toolbar">
            <div class="diary__search">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <circle cx="11" cy="11" r="7" />
                <path d="m20 20-3.8-3.8" />
              </svg>
              <input
                v-model="diarySearch"
                :placeholder="uiText('搜索标题、内容或植物...', 'Search title, content, or plant...')"
              />
            </div>
            <div class="diary__view-toggle" aria-label="Diary view">
              <button
                :class="{ 'diary__view-btn--active': diaryView === 'timeline' }"
                class="diary__view-btn"
                @click="diaryView = 'timeline'"
              >{{ uiText('时间线', 'Timeline') }}</button>
              <button
                :class="{ 'diary__view-btn--active': diaryView === 'cards' }"
                class="diary__view-btn"
                @click="diaryView = 'cards'"
              >{{ uiText('卡片', 'Cards') }}</button>
            </div>
          </div>

          <div class="diary__filters" v-if="plantNames.length > 0">
            <button
              :class="['diary__filter-btn', { 'diary__filter-btn--active': !filterPlant }]"
              @click="filterPlant = ''"
            >{{ t('diary.allPlants') }}</button>
            <button
              v-for="name in plantNames"
              :key="name"
              :class="['diary__filter-btn', { 'diary__filter-btn--active': filterPlant === name }]"
              @click="filterPlant = filterPlant === name ? '' : name"
            >🌱 {{ name }}</button>
          </div>
          <button class="diary__new-btn" @click="openCreateDiary">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            {{ t('diary.newEntry') }}
          </button>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="diary__loading">{{ t('encyclopedia.loading') }}</div>

        <div v-else-if="diaryError" class="diary__empty diary__empty--error">
          <p>{{ diaryError }}</p>
          <button class="diary__retry-btn" @click="loadDiaries">{{ uiText('重试', 'Retry') }}</button>
        </div>

        <!-- Empty -->
        <div v-else-if="diaries.length === 0" class="diary__empty">
          <svg viewBox="0 0 80 80" fill="none" width="80" height="80">
            <path d="M40 10 Q30 20 25 35 Q20 50 30 60 Q35 65 40 68 Q45 65 50 60 Q60 50 55 35 Q50 20 40 10Z" stroke="rgba(0,0,0,0.12)" stroke-width="2" fill="none"/>
            <path d="M40 68 V78" stroke="rgba(0,0,0,0.1)" stroke-width="2" stroke-linecap="round"/>
            <path d="M40 35 Q45 30 52 32" stroke="rgba(0,0,0,0.08)" stroke-width="1.5" fill="none"/>
          </svg>
          <p>{{ t('diary.empty') }}</p>
        </div>

        <div v-else-if="filteredDiaries.length === 0" class="diary__empty">
          <p>{{ uiText('没有找到匹配的日记', 'No matching entries found') }}</p>
          <button class="diary__retry-btn" @click="filterPlant = ''; diarySearch = ''">
            {{ uiText('清除筛选', 'Clear filters') }}
          </button>
        </div>

        <!-- Timeline -->
        <div v-else class="diary__timeline" :class="{ 'diary__timeline--cards': diaryView === 'cards' }">
          <div v-for="group in groupedDiaries" :key="group.date" class="diary__day">
            <div class="diary__day-header">
              <div class="diary__day-dot"></div>
              <div class="diary__day-info">
                <span class="diary__day-date">{{ group.date }}</span>
                <span class="diary__day-weekday">{{ formatWeekday(group.date) }}</span>
              </div>
              <div class="diary__day-count">{{ group.entries.length }} {{ t('diary.entries') }}</div>
            </div>

            <div class="diary__day-entries">
              <div v-for="entry in group.entries" :key="entry.id" class="diary__card">
                <div class="diary__card-time">{{ formatTime(entry.createdAt) }}</div>

                <div class="diary__card-body">
                  <div class="diary__card-top">
                    <h3 class="diary__card-title">{{ entry.title }}</h3>
                    <div class="diary__card-actions">
                      <button
                        v-if="entry.userAccount === auth.user?.account"
                        class="diary__icon-btn diary__icon-btn--edit"
                        @click="editDiary(entry)"
                        :title="uiText('编辑', 'Edit')"
                        :aria-label="uiText('编辑日记', 'Edit entry')"
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M12 20h9" />
                          <path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z" />
                        </svg>
                      </button>
                      <button
                        class="diary__icon-btn diary__icon-btn--view"
                        @click="openDiaryDetail(entry)"
                        :title="uiText('查看', 'View')"
                        :aria-label="uiText('查看日记', 'View entry')"
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z" />
                          <circle cx="12" cy="12" r="2.8" />
                        </svg>
                      </button>
                      <button
                        v-if="entry.userAccount === auth.user?.account"
                        class="diary__icon-btn diary__icon-btn--delete"
                        @click="deleteDiary(entry.id)"
                        :title="t('diary.delete')"
                        :aria-label="t('diary.delete')"
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M18 6 6 18" />
                          <path d="m6 6 12 12" />
                        </svg>
                      </button>
                    </div>
                  </div>

                  <!-- Plant & Stage tags -->
                  <div class="diary__card-tags">
                    <span v-if="entry.plantName" class="diary__tag diary__tag--plant">🌱 {{ entry.plantName }}</span>
                    <span v-if="entry.growthStage" class="diary__tag diary__tag--stage" :style="{ borderColor: getGrowthColor(entry.growthStage) }">
                      {{ displayOption(entry.growthStage, growthStages) }}
                    </span>
                    <span v-if="entry.weather" class="diary__tag diary__tag--weather">{{ displayOption(entry.weather, weatherOptions) }}</span>
                    <span v-if="entry.mood" class="diary__tag diary__tag--mood">{{ displayOption(entry.mood, moodOptions) }}</span>
                  </div>

                  <!-- Growth data -->
                  <div v-if="entry.heightCm || entry.leafCount" class="diary__card-growth">
                    <div v-if="entry.heightCm" class="diary__growth-item">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="14" height="14">
                        <path d="M12 2v20M8 6l4-4 4 4M8 18l4 4 4-4"/>
                      </svg>
                      {{ entry.heightCm }} cm
                    </div>
                    <div v-if="entry.leafCount" class="diary__growth-item">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="14" height="14">
                        <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c1.5 0 3-.3 4.3-.9C10 18 6 14 6 12c0-4.4 3.6-8 8-8 .7 0 1.4.1 2 .2C15 3.5 13.5 2 12 2z"/>
                      </svg>
                      {{ entry.leafCount }} {{ t('diary.leaves') }}
                    </div>
                  </div>

                  <p v-if="entry.content" class="diary__card-content">{{ entry.content }}</p>

                  <!-- Images -->
                  <div v-if="parseImages(entry.images).length" class="diary__card-images">
                    <img
                      v-for="(img, idx) in parseImages(entry.images)"
                      :key="idx"
                      :src="img"
                      class="diary__card-image"
                      loading="lazy"
                      @click="openLightbox(parseImages(entry.images), idx)"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Image Lightbox -->
    <Teleport to="body">
      <div v-if="showLightbox" class="diary__lightbox" :class="pageLanguageClass" @click.self="closeLightbox">
        <button class="diary__lightbox-close" @click="closeLightbox">×</button>
        <button class="diary__lightbox-prev" @click="prevImage">‹</button>
        <img :src="lightboxImages[lightboxIndex]" class="diary__lightbox-img" />
        <button class="diary__lightbox-next" @click="nextImage">›</button>
        <div class="diary__lightbox-counter">{{ lightboxIndex + 1 }} / {{ lightboxImages.length }}</div>
      </div>
    </Teleport>

    <!-- Create modal -->
    <Teleport to="body">
      <div v-if="selectedDiary" class="diary__modal-overlay" :class="pageLanguageClass" @click.self="selectedDiary = null">
        <div class="diary__modal">
          <div class="diary__modal-header">
            <h2>{{ selectedDiary.title }}</h2>
            <button class="diary__modal-close" @click="selectedDiary = null" :aria-label="uiText('关闭', 'Close')">×</button>
          </div>
          <div class="diary__modal-body">
            <div class="diary__card-tags">
              <span v-if="selectedDiary.plantName" class="diary__tag diary__tag--plant">🌱 {{ selectedDiary.plantName }}</span>
              <span v-if="selectedDiary.growthStage" class="diary__tag diary__tag--stage" :style="{ borderColor: getGrowthColor(selectedDiary.growthStage) }">
                {{ displayOption(selectedDiary.growthStage, growthStages) }}
              </span>
              <span v-if="selectedDiary.weather" class="diary__tag diary__tag--weather">{{ displayOption(selectedDiary.weather, weatherOptions) }}</span>
              <span v-if="selectedDiary.mood" class="diary__tag diary__tag--mood">{{ displayOption(selectedDiary.mood, moodOptions) }}</span>
            </div>
            <p class="diary__card-content">{{ selectedDiary.content }}</p>
            <div v-if="parseImages(selectedDiary.images || '').length" class="diary__card-images">
              <img
                v-for="(img, idx) in parseImages(selectedDiary.images || '')"
                :key="idx"
                :src="img"
                loading="lazy"
                class="diary__card-image"
                @click="openLightbox(parseImages(selectedDiary!.images || ''), idx)"
              />
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="showModal" class="diary__modal-overlay" :class="pageLanguageClass" @click.self="showModal = false">
        <div class="diary__modal">
          <div class="diary__modal-header">
            <h2>{{ editingId ? uiText('编辑日记', 'Edit entry') : t('diary.newEntry') }}</h2>
            <button class="diary__modal-close" @click="showModal = false" :aria-label="uiText('关闭', 'Close')">×</button>
          </div>

          <div class="diary__modal-body">
            <input
              v-model="newDiary.title"
              :placeholder="t('diary.titlePlaceholder')"
              class="diary__input"
              maxlength="150"
            />

            <input
              v-model="newDiary.plantName"
              :placeholder="t('diary.plantNamePlaceholder')"
              class="diary__input"
            />

            <textarea
              v-model="newDiary.content"
              :placeholder="t('diary.contentPlaceholder')"
              class="diary__textarea"
              rows="4"
            ></textarea>

            <!-- Growth tracking row -->
            <div class="diary__row diary__row--3">
              <div class="diary__field">
                <label>{{ t('diary.height') }}</label>
                <div class="diary__input-with-unit">
                  <input
                    v-model.number="newDiary.heightCm"
                    type="number"
                    min="0"
                    max="999"
                    placeholder="—"
                    class="diary__input diary__input--sm"
                  />
                  <span class="diary__unit">cm</span>
                </div>
              </div>
              <div class="diary__field">
                <label>{{ t('diary.leafCount') }}</label>
                <div class="diary__input-with-unit">
                  <input
                    v-model.number="newDiary.leafCount"
                    type="number"
                    min="0"
                    max="9999"
                    placeholder="—"
                    class="diary__input diary__input--sm"
                  />
                  <span class="diary__unit">{{ t('diary.leaves') }}</span>
                </div>
              </div>
              <div class="diary__field">
                <label>{{ t('diary.growthStage') }}</label>
                <div class="diary__options diary__options--wrap">
                  <button
                    v-for="s in growthStages"
                    :key="s.id"
                    :class="['diary__option', { 'diary__option--active': optionMatches(newDiary.growthStage, s) }]"
                    @click="toggleOption('growthStage', s)"
                  >{{ optionLabel(s) }}</button>
                </div>
              </div>
            </div>

            <div class="diary__row">
              <div class="diary__field">
                <label>{{ t('diary.weather') }}</label>
                <div class="diary__options">
                  <button
                    v-for="w in weatherOptions"
                    :key="w.id"
                    :class="['diary__option', { 'diary__option--active': optionMatches(newDiary.weather, w) }]"
                    @click="toggleOption('weather', w)"
                  >{{ optionLabel(w) }}</button>
                </div>
              </div>
              <div class="diary__field">
                <label>{{ t('diary.mood') }}</label>
                <div class="diary__options">
                  <button
                    v-for="m in moodOptions"
                    :key="m.id"
                    :class="['diary__option', { 'diary__option--active': optionMatches(newDiary.mood, m) }]"
                    @click="toggleOption('mood', m)"
                  >{{ optionLabel(m) }}</button>
                </div>
              </div>
            </div>

            <div class="diary__upload">
              <label class="diary__upload-btn" :class="{ 'diary__upload-btn--disabled': uploadLoading || newDiary.images.length >= 6 }">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                  <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/>
                </svg>
                {{ uploadLoading ? uiText('处理中...', 'Processing...') : newDiary.images.length >= 6 ? uiText('已达上限', 'Limit reached') : t('diary.uploadImage') }}
                <input type="file" accept="image/*" :disabled="uploadLoading || newDiary.images.length >= 6" @change="uploadImage" hidden />
              </label>
              <span class="diary__upload-count">{{ newDiary.images.length }}/6</span>
            </div>
            <p v-if="uploadError" class="diary__error">{{ uploadError }}</p>

            <div v-if="newDiary.images.length" class="diary__preview">
              <div v-for="(img, idx) in newDiary.images" :key="idx" class="diary__preview-item">
                <img :src="img" loading="lazy" />
                <button class="diary__preview-remove" @click="removeImage(idx)">×</button>
              </div>
            </div>
          </div>

          <div class="diary__modal-footer">
            <button class="diary__cancel-btn" @click="showModal = false">{{ t('community.cancel') }}</button>
            <button
              class="diary__submit-btn"
              :disabled="!newDiary.title.trim() || submitting"
              @click="submitDiary"
            >{{ submitting ? '...' : editingId ? uiText('保存', 'Save') : t('diary.publish') }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Auth Modal -->
    <AuthModal ref="authModalRef" />
  </div>
</template>

<style scoped lang="scss">
.diary-page,
.diary__modal-overlay,
.diary__lightbox {
  min-height: 100vh;
  --diary-font-sans: 'Noto Sans SC', 'Microsoft YaHei', system-ui, -apple-system, sans-serif;
  --diary-font-display: 'Noto Serif SC', 'Songti SC', Georgia, serif;
  font-family: var(--diary-font-sans);
}

.diary-page--en {
  --diary-font-sans: 'Inter', 'Avenir Next', system-ui, -apple-system, sans-serif;
  --diary-font-display: 'Playfair Display', Georgia, serif;
}

.diary {
  &__hero {
    padding: 5rem 1.5rem 3rem;
    text-align: center;
    background: linear-gradient(135deg, $color-leaf-800, $color-leaf-900);
    position: relative;
    overflow: hidden;
    &::before {
      content: '';
      position: absolute;
      inset: -50%;
      width: 200%;
      height: 200%;
      background: radial-gradient(circle, rgba(74,222,128,0.1) 0%, transparent 50%);
      animation: pulse 8s ease-in-out infinite;
    }
  }
  &__hero-icon { width: 60px; height: 60px; margin-bottom: 1.25rem; position: relative; z-index: 1; animation: float 3s ease-in-out infinite; }
  &__title { font-family: var(--diary-font-display); font-size: clamp(1.8rem, 4vw, 2.8rem); color: #f0fdf4; margin-bottom: 0.5rem; position: relative; z-index: 1; }
  &__subtitle { color: rgba(240,253,244,0.7); font-size: 1.05rem; position: relative; z-index: 1; }

  &__content { max-width: 860px; margin: 0 auto; padding: 2rem 1.5rem 4rem; }

  &__login-hint {
    text-align: center; padding: 3rem 1rem; color: $color-text-muted;
    p { margin-bottom: 1.25rem; font-size: 1.05rem; }
  }
  &__login-btn {
    padding: 0.75rem 2rem; background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white; border: none; border-radius: 999px; font-weight: 700; font-size: 1rem; cursor: pointer;
    box-shadow: 0 10px 24px rgba(22, 163, 74, 0.22);
    transition: transform 0.24s $ease-spring, box-shadow 0.24s ease, filter 0.24s ease;
    &:hover { transform: translateY(-2px); box-shadow: 0 14px 30px rgba(22,163,74,0.3); filter: saturate(1.05); }
    &:active { transform: translateY(0) scale(0.98); }
    &:focus-visible { outline: 3px solid rgba(34, 197, 94, 0.28); outline-offset: 3px; }
  }

  // Stats
  &__stats {
    display: flex; gap: 1.5rem; justify-content: center; margin-bottom: 2rem;
    padding: 1.25rem; background: white; border-radius: 1rem;
    box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  }
  &__stat { text-align: center; flex: 1; }
  &__stat-num { display: block; font-family: var(--diary-font-display); font-size: 1.8rem; font-weight: 700; color: $color-leaf-600; font-variant-numeric: tabular-nums; }
  &__stat-label { font-size: 0.8rem; color: $color-text-muted; }

  &__chart {
    margin: -0.75rem 0 2rem;
    padding: 1.1rem 1.15rem 0.9rem;
    border: 1px solid rgba(22, 163, 74, 0.14);
    border-radius: 1.05rem;
    background:
      radial-gradient(circle at 14% 0%, rgba(187, 247, 208, 0.44), transparent 32%),
      linear-gradient(180deg, rgba(240, 253, 244, 0.88), rgba(255, 255, 255, 0.96));
    box-shadow: 0 16px 36px rgba(20, 83, 45, 0.08);
    animation: fadeInUp 0.45s ease both;
  }
  &__chart-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 1rem;
    margin-bottom: 0.7rem;

    h3 {
      font-family: var(--diary-font-display);
      font-size: 1.05rem;
      color: $color-leaf-800;
      margin: 0 0 0.1rem;
    }

    p {
      margin: 0;
      color: $color-text-muted;
      font-size: 0.8rem;
    }
  }
  &__chart-legend {
    display: flex;
    gap: 0.75rem;
    flex-wrap: wrap;
    justify-content: flex-end;
    color: $color-text-muted;
    font-size: 0.78rem;
    font-weight: 650;

    span {
      display: inline-flex;
      align-items: center;
      gap: 0.3rem;
    }
  }
  &__chart-dot {
    width: 0.5rem;
    height: 0.5rem;
    border-radius: 50%;
    display: inline-block;

    &--height { background: #16a34a; }
    &--leaf { background: #0ea5e9; }
  }
  &__chart-svg {
    width: 100%;
    height: 150px;
    overflow: visible;
  }
  &__chart-grid {
    stroke: rgba(22, 163, 74, 0.1);
    stroke-width: 0.5;
  }
  &__chart-line {
    fill: none;
    stroke-linecap: round;
    stroke-linejoin: round;
    stroke-width: 2.8;
    vector-effect: non-scaling-stroke;

    &--height { stroke: #16a34a; }
    &--leaf { stroke: #0ea5e9; }
  }
  &__chart-point {
    vector-effect: non-scaling-stroke;
    stroke: white;
    stroke-width: 1.2;

    &--height { fill: #16a34a; }
    &--leaf { fill: #0ea5e9; }
  }
  &__chart-labels {
    display: flex;
    justify-content: space-between;
    gap: 0.35rem;
    margin-top: 0.25rem;
    color: $color-text-muted;
    font-size: 0.72rem;
  }

  // Actions
  &__actions { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; margin-bottom: 2rem; flex-wrap: wrap; }
  &__toolbar {
    width: 100%;
    display: flex;
    gap: 0.75rem;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
  }
  &__search {
    flex: 1;
    min-width: min(100%, 260px);
    display: flex;
    align-items: center;
    gap: 0.55rem;
    padding: 0.65rem 0.85rem;
    border: 1px solid rgba(22, 163, 74, 0.14);
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.86);
    box-shadow: 0 10px 24px rgba(20, 83, 45, 0.05);

    svg {
      width: 1rem;
      height: 1rem;
      color: $color-leaf-600;
      flex-shrink: 0;
    }

    input {
      width: 100%;
      border: 0;
      outline: 0;
      background: transparent;
      color: $color-text;
      font: inherit;
      font-size: 0.9rem;
    }
  }
  &__view-toggle {
    display: inline-flex;
    gap: 0.25rem;
    padding: 0.25rem;
    border: 1px solid rgba(22, 163, 74, 0.14);
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.86);
  }
  &__view-btn {
    min-height: 2rem;
    padding: 0.4rem 0.8rem;
    border: 0;
    border-radius: 999px;
    background: transparent;
    color: $color-text-muted;
    font-size: 0.82rem;
    font-weight: 700;
    cursor: pointer;
    transition: color 0.22s ease, background-color 0.22s ease, transform 0.22s ease;

    &:hover {
      color: $color-leaf-700;
      transform: translateY(-1px);
    }

    &--active {
      color: white;
      background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
      box-shadow: 0 8px 18px rgba(22, 163, 74, 0.2);
    }
  }
  &__filters { display: flex; gap: 0.4rem; flex-wrap: wrap; flex: 1; }
  &__filter-btn {
    padding: 0.45rem 0.95rem; border: 1px solid rgba(22, 163, 74, 0.18); border-radius: 999px;
    background: rgba(255, 255, 255, 0.82); color: $color-leaf-800; font-size: 0.82rem; font-weight: 650;
    cursor: pointer; transition: transform 0.22s $ease-spring, border-color 0.22s ease, background 0.22s ease, box-shadow 0.22s ease; white-space: nowrap;
    box-shadow: 0 4px 14px rgba(20, 83, 45, 0.04);
    &:hover { border-color: rgba(34, 197, 94, 0.55); background: $color-leaf-50; transform: translateY(-2px); }
    &:active { transform: translateY(0) scale(0.98); }
    &:focus-visible { outline: 3px solid rgba(34, 197, 94, 0.22); outline-offset: 2px; }
    &--active { background: linear-gradient(135deg, $color-leaf-500, $color-leaf-600); border-color: transparent; color: white; box-shadow: 0 10px 22px rgba(22,163,74,0.2); }
  }
  &__new-btn {
    display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.65rem 1.4rem;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500); color: white;
    border: none; border-radius: 0.85rem; font-weight: 700; font-size: 0.95rem; cursor: pointer;
    box-shadow: 0 12px 26px rgba(22,163,74,0.24);
    transition: transform 0.24s $ease-spring, box-shadow 0.24s ease, filter 0.24s ease;
    &:hover { transform: translateY(-2px); box-shadow: 0 16px 32px rgba(22,163,74,0.32); filter: saturate(1.06); }
    &:active { transform: translateY(0) scale(0.98); }
    &:focus-visible { outline: 3px solid rgba(34, 197, 94, 0.26); outline-offset: 3px; }
  }

  &__loading, &__empty { text-align: center; padding: 3rem 1rem; color: $color-text-muted; }
  &__empty { svg { margin-bottom: 1rem; opacity: 0.4; } p { font-size: 1.05rem; } }
  &__empty--error {
    color: #b91c1c;
  }
  &__retry-btn {
    margin-top: 1rem;
    padding: 0.55rem 1rem;
    border: 0;
    border-radius: 999px;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white;
    font-weight: 700;
    cursor: pointer;
    box-shadow: 0 10px 22px rgba(22, 163, 74, 0.2);
    transition: transform 0.22s $ease-spring, box-shadow 0.22s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 14px 28px rgba(22, 163, 74, 0.28);
    }
  }

  // Timeline
  &__timeline { position: relative; padding-left: 24px; }
  &__timeline::before {
    content: ''; position: absolute; left: 7px; top: 0; bottom: 0;
    width: 2px; background: linear-gradient(to bottom, $color-leaf-300, $color-leaf-100);
  }
  &__timeline--cards {
    padding-left: 0;

    &::before {
      display: none;
    }

    .diary__day-header {
      padding-left: 0;
    }

    .diary__day-dot {
      display: none;
    }

    .diary__day-info {
      margin-left: 0;
    }

    .diary__day-entries {
      margin-left: 0;
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1rem;
    }

    .diary__card {
      height: 100%;
      flex-direction: column;
    }

    .diary__card-time {
      min-width: 0;
      padding-top: 0;
    }
  }

  &__day { margin-bottom: 2rem; position: relative; }
  &__day-header {
    display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem;
    position: relative; z-index: 1;
  }
  &__day-dot {
    width: 16px; height: 16px; border-radius: 50%;
    background: $color-leaf-500; border: 3px solid white;
    box-shadow: 0 0 0 2px $color-leaf-300;
    flex-shrink: 0; position: absolute; left: -24px;
  }
  &__day-info { display: flex; align-items: baseline; gap: 0.5rem; margin-left: 8px; }
  &__day-date { font-family: var(--diary-font-display); font-weight: 700; font-size: 1rem; color: $color-leaf-800; }
  &__day-weekday { font-size: 0.82rem; color: $color-text-muted; }
  &__day-count { font-size: 0.78rem; color: $color-text-muted; margin-left: auto; }

  &__day-entries { display: flex; flex-direction: column; gap: 0.75rem; margin-left: 8px; }

  // Cards
  &__card {
    display: flex; gap: 1rem; background: white; border-radius: 0.875rem;
    padding: 1.25rem; border: 1px solid rgba(0,0,0,0.04);
    box-shadow: 0 2px 8px rgba(0,0,0,0.03); transition: all 0.3s;
    &:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.06); transform: translateY(-1px); }
  }
  &__card-time {
    font-size: 0.78rem; color: $color-text-muted; font-weight: 500;
    min-width: 40px; padding-top: 2px; flex-shrink: 0;
  }
  &__card-body { flex: 1; min-width: 0; }
  &__card-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 0.75rem; }
  &__card-title { font-family: var(--diary-font-display); font-size: 1.05rem; color: $color-leaf-800; margin-bottom: 0.5rem; }
  &__card-actions { display: inline-flex; align-items: center; gap: 0.35rem; flex-shrink: 0; }
  &__icon-btn {
    width: 2rem; height: 2rem; border: 1px solid rgba(22, 163, 74, 0.12); border-radius: 0.75rem;
    display: inline-flex; align-items: center; justify-content: center; color: $color-leaf-700;
    background: rgba(240, 253, 244, 0.72); cursor: pointer;
    transition: transform 0.2s $ease-spring, box-shadow 0.2s ease, background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
    svg { width: 1rem; height: 1rem; }
    &:hover { transform: translateY(-2px); border-color: rgba(34, 197, 94, 0.4); background: white; box-shadow: 0 8px 18px rgba(20, 83, 45, 0.1); }
    &:active { transform: translateY(0) scale(0.96); }
    &:focus-visible { outline: 3px solid rgba(34, 197, 94, 0.22); outline-offset: 2px; }
    &--view { color: #0f766e; background: rgba(204, 251, 241, 0.52); border-color: rgba(15, 118, 110, 0.12); }
    &--delete { color: #dc2626; background: rgba(254, 242, 242, 0.72); border-color: rgba(220, 38, 38, 0.12); }
    &--delete:hover { color: #b91c1c; border-color: rgba(220, 38, 38, 0.32); }
  }

  &__card-tags { display: flex; flex-wrap: wrap; gap: 0.35rem; margin-bottom: 0.5rem; }
  &__tag {
    display: inline-flex; align-items: center; gap: 0.2rem;
    padding: 0.18rem 0.6rem; border-radius: 999px; font-size: 0.75rem; font-weight: 650;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    &:hover { transform: translateY(-1px); }
    &--plant { background: $color-leaf-50; color: $color-leaf-700; }
    &--stage { background: white; border: 1.5px solid; color: $color-text; }
    &--weather { background: #f0f9ff; color: #0369a1; }
    &--mood { background: #fef3c7; color: #92400e; }
  }

  &__card-growth {
    display: flex; gap: 1rem; margin-bottom: 0.5rem;
  }
  &__growth-item {
    display: inline-flex; align-items: center; gap: 0.3rem;
    padding: 0.2rem 0.6rem; background: rgba(34,197,94,0.08);
    border-radius: 0.5rem; font-size: 0.8rem; font-weight: 600; color: $color-leaf-700;
  }

  &__card-content { font-size: 0.9rem; line-height: 1.7; color: $color-text; margin-bottom: 0.5rem; white-space: pre-line; }

  &__card-images { display: flex; gap: 0.4rem; flex-wrap: wrap; }
  &__card-image {
    width: 100px; height: 100px; object-fit: cover; border-radius: 0.5rem;
    border: 1px solid rgba(0,0,0,0.06); cursor: pointer; transition: all 0.2s;
    &:hover { transform: scale(1.05); box-shadow: 0 4px 12px rgba(0,0,0,0.12); }
  }

  // Lightbox
  &__lightbox {
    position: fixed; inset: 0; background: rgba(0,0,0,0.92);
    display: flex; align-items: center; justify-content: center; z-index: 2000;
  }
  &__lightbox-img { max-width: 90vw; max-height: 85vh; object-fit: contain; border-radius: 0.5rem; }
  &__lightbox-close {
    position: absolute; top: 1.5rem; right: 1.5rem; background: rgba(255,255,255,0.15);
    border: none; color: white; font-size: 2rem; width: 44px; height: 44px;
    border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center;
    transition: background 0.2s;
    &:hover { background: rgba(255,255,255,0.3); }
  }
  &__lightbox-prev, &__lightbox-next {
    position: absolute; top: 50%; transform: translateY(-50%);
    background: rgba(255,255,255,0.15); border: none; color: white;
    font-size: 2.5rem; width: 50px; height: 50px; border-radius: 50%;
    cursor: pointer; display: flex; align-items: center; justify-content: center;
    transition: background 0.2s;
    &:hover { background: rgba(255,255,255,0.3); }
  }
  &__lightbox-prev { left: 1.5rem; }
  &__lightbox-next { right: 1.5rem; }
  &__lightbox-counter {
    position: absolute; bottom: 1.5rem; left: 50%; transform: translateX(-50%);
    color: rgba(255,255,255,0.7); font-size: 0.9rem;
  }

  // Modal
  &__modal-overlay {
    position: fixed; inset: 0; background: rgba(12, 31, 22, 0.52);
    backdrop-filter: blur(10px);
    display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 1rem;
  }
  &__modal {
    background: rgba(255, 255, 255, 0.96); border-radius: 1.15rem; width: 100%; max-width: 640px; max-height: 90vh; overflow-y: auto;
    border: 1px solid rgba(187, 247, 208, 0.65);
    box-shadow: 0 28px 70px rgba(20, 83, 45, 0.22), inset 0 1px 0 rgba(255, 255, 255, 0.75);
  }
  &__modal-header {
    display: flex; justify-content: space-between; align-items: center;
    padding: 1.25rem 1.5rem; border-bottom: 1px solid rgba(22, 163, 74, 0.1);
    h2 { font-family: var(--diary-font-display); font-size: 1.25rem; color: $color-leaf-800; }
  }
  &__modal-close {
    width: 2.25rem; height: 2.25rem; border-radius: 50%; background: $color-leaf-50; border: 1px solid rgba(22, 163, 74, 0.12);
    font-size: 1.35rem; color: $color-leaf-700; cursor: pointer; padding: 0; line-height: 1;
    display: inline-flex; align-items: center; justify-content: center;
    transition: transform 0.2s $ease-spring, background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
    &:hover { color: $color-leaf-900; background: white; transform: rotate(90deg) scale(1.04); box-shadow: 0 8px 18px rgba(20, 83, 45, 0.1); }
    &:active { transform: rotate(90deg) scale(0.96); }
    &:focus-visible { outline: 3px solid rgba(34, 197, 94, 0.22); outline-offset: 2px; }
  }
  &__modal-body { padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; }
  &__modal-footer { display: flex; justify-content: flex-end; align-items: center; gap: 0.75rem; padding: 1rem 1.5rem; border-top: 1px solid rgba(0,0,0,0.06); }

  &__input {
    width: 100%; padding: 0.78rem 1rem; border: 1px solid rgba(22, 163, 74, 0.14); border-radius: 0.8rem;
    background: rgba(240, 253, 244, 0.34);
    font-size: 0.95rem; outline: none; transition: border-color 0.22s ease, box-shadow 0.22s ease, background-color 0.22s ease, transform 0.22s ease; box-sizing: border-box;
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
    &::placeholder { color: #9ca3af; }
    &:hover { border-color: rgba(34, 197, 94, 0.34); background: rgba(240, 253, 244, 0.5); }
    &:focus { border-color: $color-leaf-500; background: white; box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.12); transform: translateY(-1px); }
    &--sm { padding: 0.5rem 0.7rem; font-size: 0.9rem; }
  }
  &__textarea {
    width: 100%; padding: 0.85rem 1rem; border: 1px solid rgba(22, 163, 74, 0.14); border-radius: 0.8rem;
    background: rgba(240, 253, 244, 0.34);
    font-size: 0.95rem; outline: none; resize: vertical; font-family: inherit; line-height: 1.65;
    min-height: 118px;
    transition: border-color 0.22s ease, box-shadow 0.22s ease, background-color 0.22s ease, transform 0.22s ease; box-sizing: border-box;
    &:hover { border-color: rgba(34, 197, 94, 0.34); background: rgba(240, 253, 244, 0.5); }
    &:focus { border-color: $color-leaf-500; background: white; box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.12); transform: translateY(-1px); }
  }
  &__input-with-unit { display: flex; align-items: center; gap: 0.4rem; }
  &__unit { font-size: 0.82rem; color: $color-text-muted; white-space: nowrap; }

  &__row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;
    &--3 { grid-template-columns: auto auto 1fr; }
    @media (max-width: 640px) { grid-template-columns: 1fr; &--3 { grid-template-columns: 1fr; } }
  }
  &__field {
    label { display: block; font-size: 0.82rem; font-weight: 600; color: $color-text; margin-bottom: 0.4rem; }
  }
  &__options { display: flex; flex-wrap: wrap; gap: 0.35rem; &--wrap { flex-wrap: wrap; } }
  &__option {
    padding: 0.38rem 0.72rem; border: 1px solid rgba(22, 163, 74, 0.16); border-radius: 999px;
    background: rgba(255, 255, 255, 0.86); color: $color-leaf-800; font-size: 0.78rem; font-weight: 650;
    cursor: pointer; transition: transform 0.2s $ease-spring, border-color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease; white-space: nowrap;
    &:hover { border-color: rgba(34, 197, 94, 0.5); background: $color-leaf-50; transform: translateY(-2px); }
    &:active { transform: translateY(0) scale(0.98); }
    &:focus-visible { outline: 3px solid rgba(34, 197, 94, 0.2); outline-offset: 2px; }
    &--active { background: linear-gradient(135deg, $color-leaf-500, $color-leaf-600); border-color: transparent; color: white; box-shadow: 0 8px 18px rgba(22, 163, 74, 0.2); }
  }

  &__upload { display: flex; gap: 0.5rem; }
  &__upload-btn {
    display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.6rem 1rem;
    border: 1px dashed rgba(22, 163, 74, 0.3); border-radius: 0.8rem; color: $color-leaf-700;
    background: rgba(240, 253, 244, 0.44);
    font-size: 0.9rem; font-weight: 650; cursor: pointer; transition: transform 0.2s $ease-spring, border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
    &:hover { border-color: $color-leaf-500; color: $color-leaf-800; background: white; transform: translateY(-2px); box-shadow: 0 8px 18px rgba(20, 83, 45, 0.08); }
    &:active { transform: translateY(0) scale(0.98); }
    &--disabled { opacity: 0.58; pointer-events: none; }
  }
  &__upload-count {
    display: inline-flex;
    align-items: center;
    color: $color-text-muted;
    font-size: 0.82rem;
  }
  &__preview { display: flex; gap: 0.5rem; flex-wrap: wrap; }
  &__preview-item {
    position: relative;
    img { width: 80px; height: 80px; object-fit: cover; border-radius: 0.5rem; border: 1px solid rgba(0,0,0,0.06); }
  }
  &__preview-remove {
    position: absolute; top: -6px; right: -6px; width: 20px; height: 20px;
    border-radius: 50%; background: #ef4444; color: white; border: none;
    font-size: 0.75rem; cursor: pointer; display: flex; align-items: center; justify-content: center; line-height: 1;
  }

  &__cancel-btn {
    padding: 0.62rem 1.25rem; border: 1px solid rgba(22, 163, 74, 0.16); border-radius: 0.75rem;
    background: white; color: $color-leaf-700; font-weight: 700; font-size: 0.9rem; cursor: pointer;
    transition: transform 0.2s $ease-spring, border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
    &:hover { border-color: rgba(34, 197, 94, 0.48); background: $color-leaf-50; transform: translateY(-1px); }
    &:active { transform: translateY(0) scale(0.98); }
    &:focus-visible { outline: 3px solid rgba(34, 197, 94, 0.22); outline-offset: 2px; }
  }
  &__submit-btn {
    padding: 0.6rem 1.5rem; background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white; border: none; border-radius: 0.75rem; font-weight: 800; font-size: 0.9rem;
    cursor: pointer; transition: transform 0.22s $ease-spring, box-shadow 0.22s ease, filter 0.22s ease;
    box-shadow: 0 10px 22px rgba(22, 163, 74, 0.24);
    &:disabled { opacity: 0.5; cursor: not-allowed; }
    &:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 14px 28px rgba(22,163,74,0.32); filter: saturate(1.06); }
    &:active:not(:disabled) { transform: translateY(0) scale(0.98); }
    &:focus-visible { outline: 3px solid rgba(34, 197, 94, 0.26); outline-offset: 3px; }
  }
  &__auth-toggle { background: none; border: none; color: $color-leaf-600; font-size: 0.85rem; cursor: pointer; &:hover { text-decoration: underline; } }
  &__error { color: #ef4444; font-size: 0.85rem; margin: 0; }
}

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }
@keyframes pulse { 0%, 100% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.1); opacity: 0.8; } }
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 640px) {
  .diary {
    &__hero { padding: 4rem 1.5rem 2rem; }
    &__hero-icon { width: 48px; height: 48px; }
    &__content { padding: 1.5rem 1rem 3rem; }
    &__stats { gap: 0.75rem; padding: 1rem; }
    &__stat-num { font-size: 1.4rem; }
    &__card { flex-direction: column; gap: 0.5rem; padding: 1rem; }
    &__card-time { min-width: auto; }
    &__card-image { width: 72px; height: 72px; }
    &__actions { flex-direction: column; }
  }
}
</style>
