<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { listCollections, markWatered, removeCollection, updateCollection } from '@/api/collectionApi'
import { uploadImage } from '@/api/uploadApi'
import { prepareImageForUpload } from '@/utils/imageUpload'
import type { PlantCollection } from '@/types'

const { t, locale } = useI18n()
const router = useRouter()
const auth = useAuthStore()

const editMode = ref(false)
const editForm = ref({ username: '', bio: '' })
const editSuccess = ref('')
const editError = ref('')

const showPasswordForm = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordError = ref('')
const passwordSuccess = ref('')

const avatarUploading = ref(false)
const avatarUploadError = ref('')

// ---- 我的收藏（浇水提醒） ----
const showCollections = ref(false)
const collectionsLoading = ref(false)
const collections = ref<PlantCollection[]>([])
const editingCollection = ref<number | null>(null)
const intervalDraft = ref<number>(7)

const sortedCollections = computed(() => {
  return [...collections.value].sort((a, b) => dateMs(a.nextWaterAt) - dateMs(b.nextWaterAt))
})

const careStats = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  let overdue = 0
  let dueToday = 0
  let upcoming = 0

  for (const c of collections.value) {
    if (!c.nextWaterAt) continue
    const due = new Date(c.nextWaterAt)
    if (due < today) overdue++
    else if (due < tomorrow) dueToday++
    else upcoming++
  }
  return { overdue, dueToday, upcoming }
})

async function loadCollections() {
  collectionsLoading.value = true
  try {
    const res = await listCollections()
    collections.value = res.data?.records || []
  } catch {
    // 请求失败保持空列表，不中断页面
  } finally {
    collectionsLoading.value = false
  }
}

function toggleCollections() {
  showCollections.value = !showCollections.value
  if (showCollections.value && collections.value.length === 0 && !collectionsLoading.value) {
    loadCollections()
  }
}

async function waterPlant(c: PlantCollection) {
  try {
    await markWatered(c.plantId)
    await loadCollections()
  } catch {
    // 静默失败，避免打断用户
  }
}

async function uncollect(c: PlantCollection) {
  try {
    await removeCollection(c.plantId)
    collections.value = collections.value.filter((x) => x.id !== c.id)
  } catch {
    // 静默失败，避免打断用户
  }
}

function formatDate(iso: string) {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function dateMs(iso?: string) {
  if (!iso) return Number.MAX_SAFE_INTEGER
  const ms = new Date(iso).getTime()
  return Number.isNaN(ms) ? Number.MAX_SAFE_INTEGER : ms
}

function daysUntil(iso?: string) {
  if (!iso) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const due = new Date(iso)
  due.setHours(0, 0, 0, 0)
  if (Number.isNaN(due.getTime())) return null
  return Math.round((due.getTime() - today.getTime()) / 86400000)
}

function waterStatus(c: PlantCollection) {
  const days = daysUntil(c.nextWaterAt)
  if (days === null) return 'not-started'
  if (days < 0) return 'overdue'
  if (days === 0) return 'today'
  return 'upcoming'
}

function waterStatusText(c: PlantCollection) {
  const days = daysUntil(c.nextWaterAt)
  if (days === null) return '未浇水'
  if (days < 0) return `逾期 ${Math.abs(days)} 天`
  if (days === 0) return '今天'
  return `${days} 天后`
}

function startIntervalEdit(c: PlantCollection) {
  editingCollection.value = c.id
  intervalDraft.value = c.waterIntervalDays || 7
}

async function saveInterval(c: PlantCollection) {
  try {
    const res = await updateCollection(c.plantId, { waterIntervalDays: intervalDraft.value })
    if (res.code === 200) {
      collections.value = collections.value.map((item) => item.id === c.id ? res.data : item)
      editingCollection.value = null
    }
  } catch {}
}

onMounted(() => {
  if (!auth.isLoggedIn) {
    router.push('/')
    return
  }
  editForm.value.username = auth.user?.username || ''
  editForm.value.bio = auth.user?.bio || ''
})

function startEdit() {
  editMode.value = true
  editForm.value.username = auth.user?.username || ''
  editForm.value.bio = auth.user?.bio || ''
  editSuccess.value = ''
  editError.value = ''
}

async function saveProfile() {
  editError.value = ''
  editSuccess.value = ''
  const err = await auth.updateProfile(editForm.value.username, editForm.value.bio)
  if (err) {
    editError.value = err
  } else {
    editSuccess.value = t('community.profileUpdated')
    editMode.value = false
  }
}

async function handleAvatarUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  avatarUploading.value = true
  avatarUploadError.value = ''
  try {
    const prepared = await prepareImageForUpload(file, { locale: locale.value, targetDimension: 900, targetBytes: 500 * 1024 })
    // 不手动设置 Content-Type：让浏览器自动生成带 boundary 的 multipart 头
    const res = await uploadImage(prepared.file)
    if (res.code === 200) {
      const avatarUrl = res.data
      const err = await auth.updateProfile(auth.user!.username, auth.user!.bio || '', avatarUrl)
      if (!err) {
        editSuccess.value = t('community.avatarUpdated')
      } else {
        avatarUploadError.value = err
      }
    } else {
      avatarUploadError.value = res.message || t('community.avatarUploadFailed')
    }
  } catch (e: any) {
    avatarUploadError.value = e.response?.data?.message || t('community.avatarUploadFailed')
  } finally {
    avatarUploading.value = false
  }
}

async function handleChangePassword() {
  passwordError.value = ''
  passwordSuccess.value = ''
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = t('community.passwordMismatch')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    passwordError.value = t('community.passwordTooShort')
    return
  }
  const err = await auth.changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
  if (err) {
    passwordError.value = err
  } else {
    passwordSuccess.value = t('community.passwordChanged')
    showPasswordForm.value = false
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  }
}

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<template>
  <div class="user-center">
    <div class="user-center__card">
      <div class="user-center__header">
        <label class="user-center__avatar-wrap">
          <div class="user-center__avatar">
            <img v-if="auth.user?.avatarUrl" :src="auth.user.avatarUrl" alt="avatar" />
            <span v-else class="user-center__avatar-text">{{ auth.user?.username?.charAt(0) }}</span>
            <div class="user-center__avatar-overlay">
              <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/><circle cx="12" cy="13" r="4"/></svg>
              <span v-if="avatarUploading">{{ t('community.uploading') }}</span>
            </div>
          </div>
          <input type="file" accept="image/*" @change="handleAvatarUpload" hidden />
        </label>
        <h1 class="user-center__name">{{ auth.user?.username }}</h1>
        <p class="user-center__account">@{{ auth.user?.account }}</p>
        <p v-if="avatarUploadError" class="user-center__error">{{ avatarUploadError }}</p>
      </div>

      <div v-if="!editMode" class="user-center__info">
        <div class="user-center__info-row">
          <span class="user-center__label">{{ t('community.username') }}</span>
          <span class="user-center__value">{{ auth.user?.username }}</span>
        </div>
        <div class="user-center__info-row">
          <span class="user-center__label">{{ t('community.bio') }}</span>
          <span class="user-center__value">{{ auth.user?.bio || '-' }}</span>
        </div>
        <button class="user-center__edit-btn" @click="startEdit">
          {{ t('community.editProfile') }}
        </button>
      </div>

      <div v-else class="user-center__edit">
        <div class="user-center__field">
          <label>{{ t('community.username') }}</label>
          <input v-model="editForm.username" type="text" />
        </div>
        <div class="user-center__field">
          <label>{{ t('community.bio') }}</label>
          <textarea v-model="editForm.bio" rows="3"></textarea>
        </div>
        <p v-if="editError" class="user-center__error">{{ editError }}</p>
        <p v-if="editSuccess" class="user-center__success">{{ editSuccess }}</p>
        <div class="user-center__edit-actions">
          <button class="user-center__save-btn" @click="saveProfile">{{ t('community.save') }}</button>
          <button class="user-center__cancel-btn" @click="editMode = false">{{ t('community.cancel') }}</button>
        </div>
      </div>

      <div class="user-center__section">
        <button class="user-center__section-toggle" @click="showPasswordForm = !showPasswordForm">
          <span>{{ t('community.changePassword') }}</span>
          <svg :class="{ 'user-center__arrow--open': showPasswordForm }" viewBox="0 0 12 12" fill="none">
            <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
        <Transition name="expand">
          <div v-if="showPasswordForm" class="user-center__password-form">
            <div class="user-center__field">
              <label>{{ t('community.oldPassword') }}</label>
              <input v-model="passwordForm.oldPassword" type="password" />
            </div>
            <div class="user-center__field">
              <label>{{ t('community.newPassword') }}</label>
              <input v-model="passwordForm.newPassword" type="password" />
            </div>
            <div class="user-center__field">
              <label>{{ t('community.confirmPassword') }}</label>
              <input v-model="passwordForm.confirmPassword" type="password" />
            </div>
            <p v-if="passwordError" class="user-center__error">{{ passwordError }}</p>
            <p v-if="passwordSuccess" class="user-center__success">{{ passwordSuccess }}</p>
            <button class="user-center__save-btn" @click="handleChangePassword">{{ t('community.save') }}</button>
          </div>
        </Transition>
      </div>

      <div class="user-center__section">
        <button class="user-center__section-toggle" @click="toggleCollections">
          <span>{{ t('community.collections') }}</span>
          <svg :class="{ 'user-center__arrow--open': showCollections }" viewBox="0 0 12 12" fill="none">
            <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
        <Transition name="expand">
          <div v-if="showCollections" class="user-center__collections">
            <p v-if="collectionsLoading" class="user-center__empty">{{ t('community.collectionsLoading') }}</p>
            <p v-else-if="collections.length === 0" class="user-center__empty">{{ t('community.emptyCollections') }}</p>
            <div v-else class="user-center__care-stats">
              <div class="user-center__care-stat user-center__care-stat--overdue">
                <strong>{{ careStats.overdue }}</strong><span>逾期</span>
              </div>
              <div class="user-center__care-stat user-center__care-stat--today">
                <strong>{{ careStats.dueToday }}</strong><span>今日</span>
              </div>
              <div class="user-center__care-stat">
                <strong>{{ careStats.upcoming }}</strong><span>未来</span>
              </div>
            </div>
            <div v-if="!collectionsLoading && collections.length > 0" class="user-center__collection-item" v-for="c in sortedCollections" :key="c.id">
              <img v-if="c.plantImage" :src="c.plantImage" class="user-center__collection-img" alt="" loading="lazy" />
              <div v-else class="user-center__collection-img user-center__collection-img--placeholder">🌿</div>
              <div class="user-center__collection-info">
                <p class="user-center__collection-name">{{ c.plantName || c.plantSlug }}</p>
                <p class="user-center__collection-meta">{{ t('community.waterInterval') }}：{{ c.waterIntervalDays }} {{ t('community.days') }}</p>
                <p v-if="c.nextWaterAt" class="user-center__collection-meta">
                  {{ t('community.nextWater') }}：{{ formatDate(c.nextWaterAt) }}
                </p>
                <p v-else class="user-center__collection-meta">{{ t('community.notWateredYet') }}</p>
                <p class="user-center__collection-meta">
                  <span class="user-center__water-status" :class="`user-center__water-status--${waterStatus(c)}`">
                    {{ waterStatusText(c) }}
                  </span>
                </p>
                <div v-if="editingCollection === c.id" class="user-center__interval-edit">
                  <input v-model.number="intervalDraft" type="number" min="1" max="365" />
                  <button @click="saveInterval(c)">保存</button>
                  <button @click="editingCollection = null">取消</button>
                </div>
              </div>
              <div class="user-center__collection-actions">
                <button class="user-center__collection-btn" @click="waterPlant(c)">{{ t('community.markWatered') }}</button>
                <button class="user-center__collection-btn" @click="startIntervalEdit(c)">周期</button>
                <button class="user-center__collection-btn user-center__collection-btn--danger" @click="uncollect(c)">{{ t('community.uncollect') }}</button>
              </div>
            </div>
          </div>
        </Transition>
      </div>

      <button class="user-center__logout" @click="handleLogout">
        {{ t('community.logout') }}
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.user-center {
  max-width: 500px;
  margin: 2rem auto;
  padding: 0 1.5rem;
  min-height: 70vh;

  &__card {
    background: white;
    border-radius: 1.25rem;
    padding: 2rem;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }

  &__header {
    text-align: center;
    margin-bottom: 1.5rem;
  }

  &__avatar-wrap {
    display: block;
    width: 80px;
    height: 80px;
    margin: 0 auto 0.75rem;
    cursor: pointer;
  }

  &__avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, $color-leaf-300, $color-leaf-600);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;

    img { width: 100%; height: 100%; object-fit: cover; }
  }

  &__avatar-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    border-radius: 50%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s;

    svg { width: 22px; height: 22px; }
    span { font-size: 0.6rem; color: white; margin-top: 2px; }

    .user-center__avatar-wrap:hover & { opacity: 1; }
  }

  &__avatar-text {
    font-size: 2rem;
    color: white;
    font-weight: 700;
  }

  &__name {
    font-family: $font-display;
    font-size: 1.5rem;
    color: $color-leaf-900;
    margin-bottom: 0.2rem;
  }

  &__account {
    color: $color-text-muted;
    font-size: 0.9rem;
  }

  &__info {
    margin-bottom: 1.5rem;
  }

  &__info-row {
    display: flex;
    justify-content: space-between;
    padding: 0.6rem 0;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  }

  &__label {
    font-size: 0.9rem;
    color: $color-text-muted;
  }

  &__value {
    font-size: 0.9rem;
    color: $color-text;
    font-weight: 500;
  }

  &__edit-btn {
    width: 100%;
    padding: 0.7rem;
    background: $color-leaf-50;
    color: $color-leaf-700;
    border: 1px solid rgba(34, 197, 94, 0.2);
    border-radius: 0.6rem;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;
    margin-top: 1rem;

    &:hover { background: $color-leaf-100; }
  }

  &__edit {
    margin-bottom: 1.5rem;
  }

  &__field {
    margin-bottom: 0.75rem;

    label {
      display: block;
      font-size: 0.85rem;
      font-weight: 600;
      color: $color-leaf-800;
      margin-bottom: 0.25rem;
    }

    input, textarea {
      width: 100%;
      padding: 0.6rem 0.8rem;
      border: 1.5px solid var(--color-border);
      border-radius: 0.5rem;
      font-size: 0.95rem;
      outline: none;
      font-family: inherit;

      &:focus { border-color: $color-leaf-400; }
    }

    textarea { resize: vertical; }
  }

  &__edit-actions {
    display: flex;
    gap: 0.75rem;
    margin-top: 0.5rem;
  }

  &__save-btn {
    flex: 1;
    padding: 0.6rem;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white;
    border: none;
    border-radius: 0.5rem;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;

    &:hover { box-shadow: 0 2px 8px rgba(22, 163, 74, 0.3); }
  }

  &__cancel-btn {
    flex: 1;
    padding: 0.6rem;
    background: white;
    color: $color-text-muted;
    border: 1px solid var(--color-border);
    border-radius: 0.5rem;
    font-size: 0.9rem;
    cursor: pointer;

    &:hover { background: #f9f9f9; }
  }

  &__section {
    margin-bottom: 1.5rem;
  }

  &__section-toggle {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem 0;
    background: none;
    border: none;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
    font-size: 0.95rem;
    font-weight: 600;
    color: $color-leaf-800;
    cursor: pointer;

    svg {
      width: 14px;
      height: 14px;
      transition: transform 0.3s ease;
    }
  }

  &__arrow--open { transform: rotate(180deg); }

  &__password-form {
    padding-top: 0.5rem;
  }

  &__collections {
    padding-top: 0.5rem;
  }

  &__care-stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 0.5rem;
    margin-bottom: 0.75rem;
  }

  &__care-stat {
    padding: 0.65rem;
    border: 1px solid rgba(22, 163, 74, 0.16);
    border-radius: 0.5rem;
    background: $color-leaf-50;
    text-align: center;

    strong {
      display: block;
      color: $color-leaf-700;
      font-size: 1.15rem;
    }

    span {
      color: $color-text-muted;
      font-size: 0.75rem;
    }

    &--overdue {
      border-color: rgba(239, 68, 68, 0.22);
      background: #fef2f2;

      strong { color: #dc2626; }
    }

    &--today {
      border-color: rgba(245, 158, 11, 0.28);
      background: #fffbeb;

      strong { color: #b45309; }
    }
  }

  &__empty {
    text-align: center;
    color: $color-text-muted;
    font-size: 0.85rem;
    padding: 1rem 0;
  }

  &__collection-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 0;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);

    &:last-child { border-bottom: none; }
  }

  &__collection-img {
    width: 48px;
    height: 48px;
    border-radius: 0.5rem;
    object-fit: cover;
    flex-shrink: 0;
    background: $color-leaf-50;

    &--placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
    }
  }

  &__collection-info {
    flex: 1;
    min-width: 0;
  }

  &__collection-name {
    font-size: 0.9rem;
    font-weight: 600;
    color: $color-leaf-900;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__collection-meta {
    font-size: 0.75rem;
    color: $color-text-muted;
    margin-top: 0.15rem;
  }

  &__water-status {
    display: inline-flex;
    margin-left: 0.35rem;
    padding: 0.05rem 0.4rem;
    border-radius: 999px;
    background: $color-leaf-50;
    color: $color-leaf-700;
    font-weight: 700;

    &--overdue {
      background: #fef2f2;
      color: #dc2626;
    }

    &--today {
      background: #fffbeb;
      color: #b45309;
    }
  }

  &__interval-edit {
    display: flex;
    gap: 0.35rem;
    margin-top: 0.45rem;

    input {
      width: 70px;
      padding: 0.3rem 0.45rem;
      border: 1px solid var(--color-border);
      border-radius: 0.35rem;
    }

    button {
      padding: 0.3rem 0.5rem;
      border: 1px solid rgba(34, 197, 94, 0.25);
      border-radius: 0.35rem;
      background: white;
      color: $color-leaf-700;
      cursor: pointer;
    }
  }

  &__collection-actions {
    display: flex;
    flex-direction: column;
    gap: 0.35rem;
    flex-shrink: 0;
  }

  &__collection-btn {
    padding: 0.35rem 0.6rem;
    font-size: 0.75rem;
    border-radius: 0.4rem;
    border: 1px solid rgba(34, 197, 94, 0.3);
    background: $color-leaf-50;
    color: $color-leaf-700;
    font-weight: 600;
    cursor: pointer;
    white-space: nowrap;

    &:hover { background: $color-leaf-100; }

    &--danger {
      border-color: rgba(239, 68, 68, 0.3);
      background: white;
      color: #ef4444;

      &:hover { background: #fef2f2; }
    }
  }

  &__error {
    color: #ef4444;
    font-size: 0.85rem;
    margin-bottom: 0.5rem;
  }

  &__success {
    color: $color-leaf-600;
    font-size: 0.85rem;
    margin-bottom: 0.5rem;
  }

  &__logout {
    width: 100%;
    padding: 0.7rem;
    background: white;
    color: #ef4444;
    border: 1px solid rgba(239, 68, 68, 0.2);
    border-radius: 0.6rem;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;

    &:hover { background: #fef2f2; }
  }
}

.expand-enter-active { transition: all 0.3s ease; overflow: hidden; }
.expand-leave-active { transition: all 0.2s ease; overflow: hidden; }
.expand-enter-from, .expand-leave-to { opacity: 0; max-height: 0; }
.expand-enter-to, .expand-leave-from { max-height: 400px; }
</style>
