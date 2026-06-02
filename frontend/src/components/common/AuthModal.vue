<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const auth = useAuthStore()

const visible = defineModel<boolean>('visible', { default: false })
const mode = ref<'login' | 'register'>('login')
const form = ref({ username: '', account: '', password: '' })
const error = ref('')
const loading = ref(false)

function open(loginMode: 'login' | 'register' = 'login') {
  mode.value = loginMode
  form.value = { username: '', account: '', password: '' }
  error.value = ''
  visible.value = true
}

async function handleSubmit() {
  loading.value = true
  error.value = ''
  if (mode.value === 'login') {
    const err = await auth.login(form.value.account, form.value.password)
    if (err) { error.value = err; loading.value = false; return }
  } else {
    if (!form.value.username) { error.value = '请输入用户名'; loading.value = false; return }
    const err = await auth.register(form.value.username, form.value.account, form.value.password)
    if (err) { error.value = err; loading.value = false; return }
    const loginErr = await auth.login(form.value.account, form.value.password)
    if (loginErr) { error.value = loginErr; loading.value = false; return }
  }
  visible.value = false
  loading.value = false
}

function switchMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  error.value = ''
}

// 暴露 open 方法供父组件调用
defineExpose({ open })
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="auth-overlay" @click.self="visible = false">
        <div class="auth-modal">
          <button class="auth-modal__close" @click="visible = false">&times;</button>
          <div class="auth-modal__header">
            <div class="auth-modal__leaf-icon">
              <svg viewBox="0 0 32 32" fill="none"><path d="M16 2C16 2 8 6 8 14C8 18.4 11.6 22 16 22C20.4 22 24 18.4 24 14C24 6 16 2 16 2Z" fill="url(#leafAg)" stroke="#16a34a" stroke-width="1.5"/><path d="M16 22V30" stroke="#16a34a" stroke-width="2" stroke-linecap="round"/><defs><linearGradient id="leafAg" x1="8" y1="2" x2="24" y2="22"><stop stop-color="#4ade80"/><stop offset="1" stop-color="#16a34a"/></linearGradient></defs></svg>
            </div>
            <h2>{{ mode === 'login' ? t('community.login') : t('community.register') }}</h2>
          </div>
          <form class="auth-modal__form" @submit.prevent="handleSubmit">
            <div v-if="mode === 'register'" class="auth-modal__field">
              <label>{{ t('community.username') }}</label>
              <input v-model="form.username" type="text" :placeholder="t('community.usernamePlaceholder')" required />
            </div>
            <div class="auth-modal__field">
              <label>{{ t('community.account') }}</label>
              <input v-model="form.account" type="text" :placeholder="t('community.accountPlaceholder')" required />
            </div>
            <div class="auth-modal__field">
              <label>{{ t('community.password') }}</label>
              <input v-model="form.password" type="password" :placeholder="t('community.passwordPlaceholder')" required />
            </div>
            <p v-if="error" class="auth-modal__error">{{ error }}</p>
            <button type="submit" class="auth-modal__submit" :disabled="loading">
              {{ loading ? '...' : (mode === 'login' ? t('community.login') : t('community.register')) }}
            </button>
          </form>
          <p class="auth-modal__switch">
            {{ mode === 'login' ? t('community.noAccount') : t('community.hasAccount') }}
            <a href="#" @click.prevent="switchMode">
              {{ mode === 'login' ? t('community.register') : t('community.login') }}
            </a>
          </p>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped lang="scss">
.auth-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.auth-modal {
  position: relative;
  background: white;
  border-radius: 1.25rem;
  padding: 2.5rem 2rem 2rem;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);

  &__close {
    position: absolute;
    top: 0.75rem;
    right: 1rem;
    background: none;
    border: none;
    font-size: 1.5rem;
    color: $color-text-muted;
    cursor: pointer;
    line-height: 1;

    &:hover { color: $color-text; }
  }

  &__header {
    text-align: center;
    margin-bottom: 1.5rem;
  }

  &__leaf-icon {
    width: 48px;
    height: 48px;
    margin: 0 auto 0.75rem;

    svg { width: 100%; height: 100%; }
  }

  &__header h2 {
    font-family: $font-display;
    font-size: 1.5rem;
    color: $color-leaf-900;
  }

  &__form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  &__field {
    display: flex;
    flex-direction: column;
    gap: 0.3rem;

    label {
      font-size: 0.85rem;
      font-weight: 600;
      color: $color-leaf-800;
    }

    input {
      padding: 0.7rem 1rem;
      border: 1.5px solid var(--color-border);
      border-radius: 0.6rem;
      font-size: 0.95rem;
      outline: none;
      transition: border-color 0.2s;

      &:focus { border-color: $color-leaf-400; }
      &::placeholder { color: #ccc; }
    }
  }

  &__error {
    color: #ef4444;
    font-size: 0.85rem;
    text-align: center;
    margin: 0;
  }

  &__submit {
    padding: 0.75rem;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white;
    border: none;
    border-radius: 0.6rem;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-top: 0.5rem;

    &:hover { box-shadow: 0 4px 12px rgba(22, 163, 74, 0.3); }
    &:disabled { opacity: 0.6; cursor: default; }
  }

  &__switch {
    text-align: center;
    margin-top: 1rem;
    font-size: 0.85rem;
    color: $color-text-muted;

    a {
      color: $color-leaf-600;
      font-weight: 600;
      text-decoration: none;

      &:hover { text-decoration: underline; }
    }
  }
}

.modal-enter-active { transition: opacity 0.3s ease; }
.modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
