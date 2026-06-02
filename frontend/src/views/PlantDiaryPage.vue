<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'
import { AuthModal } from '@/components/common'
import request from '@/api/request'

const { t } = useI18n()
const auth = useAuthStore()

interface Diary {
  id: number
  userAccount: string
  plantSlug: string
  plantName: string
  title: string
  content: string
  images: string
  weather: string
  mood: string
  heightCm: number | null
  leafCount: number | null
  growthStage: string
  createdAt: string
  username: string
  avatarUrl: string
}

const diaries = ref<Diary[]>([])
const loading = ref(false)
const showModal = ref(false)
const submitting = ref(false)
const filterPlant = ref('')

const newDiary = ref({
  title: '',
  content: '',
  plantName: '',
  weather: '',
  mood: '',
  heightCm: null as number | null,
  leafCount: null as number | null,
  growthStage: '',
  images: [] as string[],
})

const weatherOptions = ['☀️ 晴', '⛅ 多云', '🌧️ 雨', '❄️ 雪', '🌫️ 雾']
const moodOptions = ['😊 开心', '🥰 幸福', '😌 平静', '🤔 思考', '😤 担心', '🤩 惊喜']
const growthStages = ['🌱 幼苗期', '🌿 生长期', '🌸 开花期', '🍂 休眠期', '🌲 成熟期']

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
  const names = new Set(diaries.value.map(d => d.plantName).filter(Boolean))
  return Array.from(names)
})

const filteredDiaries = computed(() => {
  if (!filterPlant.value) return diaries.value
  return diaries.value.filter(d => d.plantName === filterPlant.value)
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

async function loadDiaries() {
  if (!auth.isLoggedIn) return
  loading.value = true
  try {
    const res: any = await request.get('/diaries/my', {
      params: { page: 1, size: 200 },
    })
    if (res.code === 200) {
      diaries.value = res.data.records
    }
  } finally {
    loading.value = false
  }
}

async function submitDiary() {
  if (!newDiary.value.title.trim()) return
  submitting.value = true
  try {
    const payload: any = {
      title: newDiary.value.title,
      content: newDiary.value.content,
      plantName: newDiary.value.plantName,
      weather: newDiary.value.weather,
      mood: newDiary.value.mood,
      heightCm: newDiary.value.heightCm,
      leafCount: newDiary.value.leafCount,
      growthStage: newDiary.value.growthStage,
      images: JSON.stringify(newDiary.value.images),
    }
    const res: any = await request.post('/diaries', payload)
    if (res.code === 200) {
      diaries.value.unshift(res.data)
      showModal.value = false
      resetForm()
    }
  } finally {
    submitting.value = false
  }
}

async function deleteDiary(id: number) {
  if (!confirm('确定删除这篇日记吗？')) return
  try {
    await request.delete(`/diaries/${id}`)
    diaries.value = diaries.value.filter(d => d.id !== id)
  } catch {}
}

async function uploadImage(event: Event) {
  const input = event.target as HTMLInputElement
  if (!input.files?.length) return
  const file = input.files[0]
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res: any = await request.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    if (res.code === 200) {
      newDiary.value.images.push(res.data)
    }
  } catch {}
  input.value = ''
}

function removeImage(index: number) {
  newDiary.value.images.splice(index, 1)
}

function resetForm() {
  newDiary.value = { title: '', content: '', plantName: '', weather: '', mood: '', heightCm: null, leafCount: null, growthStage: '', images: [] }
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
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[new Date(dateStr).getDay()]
}

function parseImages(images: string): string[] {
  try { return JSON.parse(images) } catch { return [] }
}

function getGrowthColor(stage: string) {
  if (stage.includes('幼苗')) return '#22c55e'
  if (stage.includes('生长')) return '#16a34a'
  if (stage.includes('开花')) return '#f59e0b'
  if (stage.includes('休眠')) return '#94a3b8'
  if (stage.includes('成熟')) return '#15803d'
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
  <div class="diary-page">
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

        <!-- Actions -->
        <div class="diary__actions">
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
          <button class="diary__new-btn" @click="showModal = true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            {{ t('diary.newEntry') }}
          </button>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="diary__loading">{{ t('encyclopedia.loading') }}</div>

        <!-- Empty -->
        <div v-else-if="diaries.length === 0" class="diary__empty">
          <svg viewBox="0 0 80 80" fill="none" width="80" height="80">
            <path d="M40 10 Q30 20 25 35 Q20 50 30 60 Q35 65 40 68 Q45 65 50 60 Q60 50 55 35 Q50 20 40 10Z" stroke="rgba(0,0,0,0.12)" stroke-width="2" fill="none"/>
            <path d="M40 68 V78" stroke="rgba(0,0,0,0.1)" stroke-width="2" stroke-linecap="round"/>
            <path d="M40 35 Q45 30 52 32" stroke="rgba(0,0,0,0.08)" stroke-width="1.5" fill="none"/>
          </svg>
          <p>{{ t('diary.empty') }}</p>
        </div>

        <!-- Timeline -->
        <div v-else class="diary__timeline">
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
                    <button
                      v-if="entry.userAccount === auth.user?.account"
                      class="diary__card-delete"
                      @click="deleteDiary(entry.id)"
                      :title="t('diary.delete')"
                    >×</button>
                  </div>

                  <!-- Plant & Stage tags -->
                  <div class="diary__card-tags">
                    <span v-if="entry.plantName" class="diary__tag diary__tag--plant">🌱 {{ entry.plantName }}</span>
                    <span v-if="entry.growthStage" class="diary__tag diary__tag--stage" :style="{ borderColor: getGrowthColor(entry.growthStage) }">
                      {{ entry.growthStage }}
                    </span>
                    <span v-if="entry.weather" class="diary__tag diary__tag--weather">{{ entry.weather }}</span>
                    <span v-if="entry.mood" class="diary__tag diary__tag--mood">{{ entry.mood }}</span>
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
      <div v-if="showLightbox" class="diary__lightbox" @click.self="closeLightbox">
        <button class="diary__lightbox-close" @click="closeLightbox">×</button>
        <button class="diary__lightbox-prev" @click="prevImage">‹</button>
        <img :src="lightboxImages[lightboxIndex]" class="diary__lightbox-img" />
        <button class="diary__lightbox-next" @click="nextImage">›</button>
        <div class="diary__lightbox-counter">{{ lightboxIndex + 1 }} / {{ lightboxImages.length }}</div>
      </div>
    </Teleport>

    <!-- Create modal -->
    <Teleport to="body">
      <div v-if="showModal" class="diary__modal-overlay" @click.self="showModal = false">
        <div class="diary__modal">
          <div class="diary__modal-header">
            <h2>{{ t('diary.newEntry') }}</h2>
            <button class="diary__modal-close" @click="showModal = false">×</button>
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
                    :key="s"
                    :class="['diary__option', { 'diary__option--active': newDiary.growthStage === s }]"
                    @click="newDiary.growthStage = newDiary.growthStage === s ? '' : s"
                  >{{ s }}</button>
                </div>
              </div>
            </div>

            <div class="diary__row">
              <div class="diary__field">
                <label>{{ t('diary.weather') }}</label>
                <div class="diary__options">
                  <button
                    v-for="w in weatherOptions"
                    :key="w"
                    :class="['diary__option', { 'diary__option--active': newDiary.weather === w }]"
                    @click="newDiary.weather = newDiary.weather === w ? '' : w"
                  >{{ w }}</button>
                </div>
              </div>
              <div class="diary__field">
                <label>{{ t('diary.mood') }}</label>
                <div class="diary__options">
                  <button
                    v-for="m in moodOptions"
                    :key="m"
                    :class="['diary__option', { 'diary__option--active': newDiary.mood === m }]"
                    @click="newDiary.mood = newDiary.mood === m ? '' : m"
                  >{{ m }}</button>
                </div>
              </div>
            </div>

            <div class="diary__upload">
              <label class="diary__upload-btn">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                  <rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/>
                </svg>
                {{ t('diary.uploadImage') }}
                <input type="file" accept="image/*" @change="uploadImage" hidden />
              </label>
            </div>

            <div v-if="newDiary.images.length" class="diary__preview">
              <div v-for="(img, idx) in newDiary.images" :key="idx" class="diary__preview-item">
                <img :src="img" />
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
            >{{ submitting ? '...' : t('diary.publish') }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Auth Modal -->
    <AuthModal ref="authModalRef" />
  </div>
</template>

<style scoped lang="scss">
.diary-page { min-height: 100vh; }

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
  &__title { font-family: $font-display; font-size: clamp(1.8rem, 4vw, 2.8rem); color: #f0fdf4; margin-bottom: 0.5rem; position: relative; z-index: 1; }
  &__subtitle { color: rgba(240,253,244,0.7); font-size: 1.05rem; position: relative; z-index: 1; }

  &__content { max-width: 860px; margin: 0 auto; padding: 2rem 1.5rem 4rem; }

  &__login-hint {
    text-align: center; padding: 3rem 1rem; color: $color-text-muted;
    p { margin-bottom: 1.25rem; font-size: 1.05rem; }
  }
  &__login-btn {
    padding: 0.75rem 2rem; background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white; border: none; border-radius: 0.75rem; font-weight: 600; font-size: 1rem; cursor: pointer; transition: all 0.3s;
    &:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(22,163,74,0.3); }
  }

  // Stats
  &__stats {
    display: flex; gap: 1.5rem; justify-content: center; margin-bottom: 2rem;
    padding: 1.25rem; background: white; border-radius: 1rem;
    box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  }
  &__stat { text-align: center; flex: 1; }
  &__stat-num { display: block; font-family: $font-display; font-size: 1.8rem; font-weight: 700; color: $color-leaf-600; }
  &__stat-label { font-size: 0.8rem; color: $color-text-muted; }

  // Actions
  &__actions { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; margin-bottom: 2rem; flex-wrap: wrap; }
  &__filters { display: flex; gap: 0.4rem; flex-wrap: wrap; flex: 1; }
  &__filter-btn {
    padding: 0.4rem 0.8rem; border: 1.5px solid rgba(0,0,0,0.1); border-radius: 1rem;
    background: white; font-size: 0.82rem; cursor: pointer; transition: all 0.2s; white-space: nowrap;
    &:hover { border-color: $color-leaf-400; }
    &--active { background: $color-leaf-50; border-color: $color-leaf-500; color: $color-leaf-700; font-weight: 600; }
  }
  &__new-btn {
    display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.65rem 1.4rem;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500); color: white;
    border: none; border-radius: 0.75rem; font-weight: 600; font-size: 0.95rem; cursor: pointer; transition: all 0.3s;
    &:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(22,163,74,0.3); }
  }

  &__loading, &__empty { text-align: center; padding: 3rem 1rem; color: $color-text-muted; }
  &__empty { svg { margin-bottom: 1rem; opacity: 0.4; } p { font-size: 1.05rem; } }

  // Timeline
  &__timeline { position: relative; padding-left: 24px; }
  &__timeline::before {
    content: ''; position: absolute; left: 7px; top: 0; bottom: 0;
    width: 2px; background: linear-gradient(to bottom, $color-leaf-300, $color-leaf-100);
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
  &__day-date { font-family: $font-display; font-weight: 700; font-size: 1rem; color: $color-leaf-800; }
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
  &__card-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 0.5rem; }
  &__card-title { font-family: $font-display; font-size: 1.05rem; color: $color-leaf-800; margin-bottom: 0.5rem; }
  &__card-delete {
    background: none; border: none; color: #ccc; font-size: 1.2rem; cursor: pointer;
    padding: 0.2rem; line-height: 1; flex-shrink: 0; transition: color 0.2s;
    &:hover { color: #ef4444; }
  }

  &__card-tags { display: flex; flex-wrap: wrap; gap: 0.35rem; margin-bottom: 0.5rem; }
  &__tag {
    display: inline-flex; align-items: center; gap: 0.2rem;
    padding: 0.15rem 0.55rem; border-radius: 1rem; font-size: 0.75rem; font-weight: 500;
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
    position: fixed; inset: 0; background: rgba(0,0,0,0.5);
    display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 1rem;
  }
  &__modal { background: white; border-radius: 1rem; width: 100%; max-width: 600px; max-height: 90vh; overflow-y: auto; }
  &__modal-header {
    display: flex; justify-content: space-between; align-items: center;
    padding: 1.25rem 1.5rem; border-bottom: 1px solid rgba(0,0,0,0.06);
    h2 { font-family: $font-display; font-size: 1.2rem; color: $color-leaf-800; }
  }
  &__modal-close { background: none; border: none; font-size: 1.5rem; color: #999; cursor: pointer; padding: 0.25rem; line-height: 1; &:hover { color: #333; } }
  &__modal-body { padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; }
  &__modal-footer { display: flex; justify-content: flex-end; align-items: center; gap: 0.75rem; padding: 1rem 1.5rem; border-top: 1px solid rgba(0,0,0,0.06); }

  &__input {
    width: 100%; padding: 0.7rem 1rem; border: 1.5px solid rgba(0,0,0,0.1); border-radius: 0.6rem;
    font-size: 0.95rem; outline: none; transition: border-color 0.2s; box-sizing: border-box;
    &:focus { border-color: $color-leaf-500; }
    &--sm { padding: 0.5rem 0.7rem; font-size: 0.9rem; }
  }
  &__textarea {
    width: 100%; padding: 0.7rem 1rem; border: 1.5px solid rgba(0,0,0,0.1); border-radius: 0.6rem;
    font-size: 0.95rem; outline: none; resize: vertical; font-family: inherit; line-height: 1.6;
    transition: border-color 0.2s; box-sizing: border-box;
    &:focus { border-color: $color-leaf-500; }
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
    padding: 0.3rem 0.6rem; border: 1.5px solid rgba(0,0,0,0.1); border-radius: 1rem;
    background: white; font-size: 0.78rem; cursor: pointer; transition: all 0.2s; white-space: nowrap;
    &:hover { border-color: $color-leaf-400; }
    &--active { background: $color-leaf-50; border-color: $color-leaf-500; color: $color-leaf-700; }
  }

  &__upload { display: flex; gap: 0.5rem; }
  &__upload-btn {
    display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.6rem 1rem;
    border: 1.5px dashed rgba(0,0,0,0.15); border-radius: 0.6rem; color: $color-text-muted;
    font-size: 0.9rem; cursor: pointer; transition: all 0.2s;
    &:hover { border-color: $color-leaf-500; color: $color-leaf-600; }
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
    padding: 0.6rem 1.2rem; border: 1.5px solid rgba(0,0,0,0.12); border-radius: 0.6rem;
    background: white; color: $color-text-muted; font-size: 0.9rem; cursor: pointer; transition: all 0.2s;
    &:hover { border-color: #999; color: #333; }
  }
  &__submit-btn {
    padding: 0.6rem 1.5rem; background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white; border: none; border-radius: 0.6rem; font-weight: 600; font-size: 0.9rem;
    cursor: pointer; transition: all 0.3s;
    &:disabled { opacity: 0.5; cursor: not-allowed; }
    &:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(22,163,74,0.3); }
  }
  &__auth-toggle { background: none; border: none; color: $color-leaf-600; font-size: 0.85rem; cursor: pointer; &:hover { text-decoration: underline; } }
  &__error { color: #ef4444; font-size: 0.85rem; margin: 0; }
}

@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }
@keyframes pulse { 0%, 100% { transform: scale(1); opacity: 0.5; } 50% { transform: scale(1.1); opacity: 0.8; } }

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
