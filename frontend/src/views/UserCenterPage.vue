<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import request from '@/api/request'

const { t } = useI18n()
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
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res: any = await request.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      const avatarUrl = res.data
      const err = await auth.updateProfile(auth.user!.username, auth.user!.bio || '', avatarUrl)
      if (!err) {
        editSuccess.value = t('community.avatarUpdated')
      }
    }
  } catch {} finally {
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
