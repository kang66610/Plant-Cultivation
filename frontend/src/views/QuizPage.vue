<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const router = useRouter()

const currentStep = ref(0)
const answers = ref({
  window: '',
  hours: 3,
  direction: '',
})

const windows = computed(() => [
  { id: 'north', label: t('quiz.north'), emoji: '🧭', light: 'low', desc: t('quiz.northDesc') },
  { id: 'east', label: t('quiz.east'), emoji: '🌅', light: 'medium', desc: t('quiz.eastDesc') },
  { id: 'west', label: t('quiz.west'), emoji: '🌇', light: 'high', desc: t('quiz.westDesc') },
  { id: 'south', label: t('quiz.south'), emoji: '☀️', light: 'bright_direct', desc: t('quiz.southDesc') },
])

const directions = computed(() => [
  { id: 'indoor', label: t('quiz.indoor'), emoji: '🏠', desc: t('quiz.indoorDesc') },
  { id: 'outdoor', label: t('quiz.outdoor'), emoji: '🌳', desc: t('quiz.outdoorDesc') },
])

const recommendations = computed(() => {
  const lightLevel = windows.value.find(w => w.id === answers.value.window)?.light || 'medium'
  const plants: Record<string, string[]> = {
    low: ['虎皮兰', '绿萝', '金钱树', '白掌', '竹芋'],
    medium: ['龟背竹', '吊兰', '橡皮树', '蝴蝶兰', '无尽夏', '文心兰'],
    high: ['琴叶榕', '天堂鸟', '薰衣草', '罗勒'],
    bright_direct: ['芦荟', '仙人掌（各种）', '玉树', '向日葵', '薄荷', '多肉吉娃娃'],
  }
  return plants[lightLevel] || plants.medium
})

const progress = computed(() => ((currentStep.value + 1) / 3) * 100)

function nextStep() {
  if (currentStep.value < 2) currentStep.value++
}

function prevStep() {
  if (currentStep.value > 0) currentStep.value--
}

function goToEncyclopedia() {
  router.push('/encyclopedia')
}
</script>

<template>
  <div class="quiz">
    <div class="quiz__hero">
      <h1 class="quiz__title">{{ $t('quiz.title') }}</h1>
      <p class="quiz__subtitle">{{ $t('quiz.subtitle') }}</p>
    </div>

    <div class="quiz__container">
      <div class="quiz__progress">
        <div class="quiz__progress-bar" :style="{ width: `${progress}%` }" />
      </div>

      <div class="quiz__step-indicator">
        <span v-for="i in 3" :key="i" class="quiz__step-dot" :class="{ 'quiz__step-dot--active': i - 1 <= currentStep }" />
      </div>

      <Transition :name="currentStep > 0 ? 'slide-left' : 'slide-right'" mode="out-in">
        <div v-if="currentStep === 0" key="step1" class="quiz__step">
          <h2 class="quiz__question">{{ $t('quiz.step1Q') }}</h2>
          <p class="quiz__hint">{{ $t('quiz.step1Hint') }}</p>

          <div class="quiz__options">
            <button
              v-for="win in windows"
              :key="win.id"
              class="quiz__option"
              :class="{ 'quiz__option--selected': answers.window === win.id }"
              @click="answers.window = win.id"
            >
              <span class="quiz__option-emoji">{{ win.emoji }}</span>
              <span class="quiz__option-label">{{ win.label }}</span>
              <span class="quiz__option-desc">{{ win.desc }}</span>
            </button>
          </div>

          <button class="quiz__next" :disabled="!answers.window" @click="nextStep">
            {{ $t('quiz.continue') }}
          </button>
        </div>

        <div v-else-if="currentStep === 1" key="step2" class="quiz__step">
          <h2 class="quiz__question">{{ $t('quiz.step2Q') }}</h2>
          <p class="quiz__hint">{{ $t('quiz.step2Hint') }}</p>

          <div class="quiz__slider-container">
            <input
              v-model.number="answers.hours"
              type="range"
              min="0"
              max="12"
              class="quiz__slider"
            />
            <div class="quiz__slider-labels">
              <span>0h</span>
              <span class="quiz__slider-value">{{ answers.hours }} {{ $t('quiz.hours') }}</span>
              <span>12h</span>
            </div>

            <div class="quiz__sun-visual">
              <div class="quiz__sun" :style="{ transform: `scale(${0.5 + answers.hours / 24})`, opacity: 0.3 + answers.hours / 15 }">
                ☀️
              </div>
              <div class="quiz__rays" :style="{ opacity: answers.hours / 12 }">
                <span v-for="i in 8" :key="i" class="quiz__ray" :style="{ transform: `rotate(${i * 45}deg)` }" />
              </div>
            </div>
          </div>

          <div class="quiz__nav">
            <button class="quiz__back" @click="prevStep">{{ $t('quiz.back') }}</button>
            <button class="quiz__next" @click="nextStep">{{ $t('quiz.continue') }}</button>
          </div>
        </div>

        <div v-else key="step3" class="quiz__step">
          <h2 class="quiz__question">{{ $t('quiz.step3Q') }}</h2>
          <p class="quiz__hint">{{ $t('quiz.step3Hint') }}</p>

          <div class="quiz__options quiz__options--horizontal">
            <button
              v-for="dir in directions"
              :key="dir.id"
              class="quiz__option quiz__option--wide"
              :class="{ 'quiz__option--selected': answers.direction === dir.id }"
              @click="answers.direction = dir.id"
            >
              <span class="quiz__option-emoji">{{ dir.emoji }}</span>
              <span class="quiz__option-label">{{ dir.label }}</span>
              <span class="quiz__option-desc">{{ dir.desc }}</span>
            </button>
          </div>

          <div class="quiz__result" v-if="answers.direction">
            <h3 class="quiz__result-title">{{ $t('quiz.recommend') }}</h3>
            <p class="quiz__result-subtitle">{{ $t('quiz.recommendDesc', { window: windows.find(w => w.id === answers.window)?.label?.toLowerCase() || '', hours: answers.hours }) }}</p>

            <div class="quiz__result-list">
              <div v-for="(plant, i) in recommendations" :key="i" class="quiz__result-plant">
                <span class="quiz__result-plant-icon">🌱</span>
                <span>{{ plant }}</span>
              </div>
            </div>

            <button class="quiz__cta" @click="goToEncyclopedia">
              {{ $t('quiz.browseAll') }}
            </button>
          </div>

          <div class="quiz__nav">
            <button class="quiz__back" @click="prevStep">{{ $t('quiz.back') }}</button>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<style scoped lang="scss">
.quiz {
  min-height: 100vh;

  &__hero {
    padding: 6rem 1.5rem 3rem;
    text-align: center;
    background: linear-gradient(135deg, $color-sun-700, $color-sun-500);
  }

  &__title {
    font-family: $font-display;
    font-size: clamp(2rem, 4vw, 3rem);
    color: white;
    margin-bottom: 0.5rem;
  }

  &__subtitle {
    color: rgba(255, 255, 255, 0.85);
    font-size: 1.1rem;
  }

  &__container {
    max-width: 600px;
    margin: -2rem auto 3rem;
    padding: 0 1.5rem;
  }

  &__progress {
    height: 4px;
    background: #e5e7eb;
    border-radius: 2px;
    overflow: hidden;
    margin-bottom: 1.5rem;
  }

  &__progress-bar {
    height: 100%;
    background: linear-gradient(90deg, $color-sun-400, $color-sun-600);
    border-radius: 2px;
    transition: width 0.5s $ease-out-expo;
  }

  &__step-indicator {
    display: flex;
    justify-content: center;
    gap: 0.5rem;
    margin-bottom: 2rem;
  }

  &__step-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #e5e7eb;
    transition: all 0.3s ease;

    &--active {
      background: $color-sun-500;
      transform: scale(1.2);
    }
  }

  &__step {
    background: white;
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  &__question {
    font-family: $font-display;
    font-size: 1.4rem;
    color: $color-leaf-900;
    margin-bottom: 0.5rem;
    text-align: center;
  }

  &__hint {
    text-align: center;
    color: $color-text-muted;
    font-size: 0.9rem;
    margin-bottom: 2rem;
  }

  &__options {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
    margin-bottom: 2rem;

    &--horizontal {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  &__option {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    padding: 1.5rem 1rem;
    border: 2px solid $color-border;
    border-radius: 16px;
    background: white;
    cursor: pointer;
    transition: all 0.3s $ease-spring;

    &:hover {
      border-color: $color-sun-300;
      transform: translateY(-2px);
    }

    &--selected {
      border-color: $color-sun-500;
      background: $color-sun-50;
      box-shadow: 0 4px 12px rgba(234, 179, 8, 0.2);
    }

    &--wide {
      padding: 2rem;
    }
  }

  &__option-emoji {
    font-size: 2rem;
  }

  &__option-label {
    font-weight: 600;
    color: $color-leaf-900;
  }

  &__option-desc {
    font-size: 0.8rem;
    color: $color-text-muted;
    text-align: center;
  }

  &__slider-container {
    margin-bottom: 2rem;
    text-align: center;
  }

  &__slider {
    width: 100%;
    height: 8px;
    -webkit-appearance: none;
    appearance: none;
    background: linear-gradient(90deg, #1e3a5f, $color-sun-400, $color-sun-300);
    border-radius: 4px;
    outline: none;
    margin-bottom: 0.75rem;

    &::-webkit-slider-thumb {
      -webkit-appearance: none;
      width: 24px;
      height: 24px;
      border-radius: 50%;
      background: $color-sun-500;
      cursor: pointer;
      box-shadow: 0 2px 8px rgba(234, 179, 8, 0.4);
    }
  }

  &__slider-labels {
    display: flex;
    justify-content: space-between;
    font-size: 0.8rem;
    color: $color-text-muted;
  }

  &__slider-value {
    font-weight: 600;
    color: $color-sun-700;
    font-size: 1rem;
  }

  &__sun-visual {
    position: relative;
    width: 100px;
    height: 100px;
    margin: 1.5rem auto;
  }

  &__sun {
    font-size: 3rem;
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.5s ease;
  }

  &__rays {
    position: absolute;
    inset: 0;
    transition: opacity 0.5s ease;
  }

  &__ray {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 2px;
    height: 40px;
    background: linear-gradient(to bottom, $color-sun-400, transparent);
    transform-origin: bottom center;
    margin-left: -1px;
    margin-top: -40px;
  }

  &__result {
    margin-top: 2rem;
    padding-top: 2rem;
    border-top: 1px solid $color-border;
  }

  &__result-title {
    font-family: $font-display;
    font-size: 1.2rem;
    color: $color-leaf-900;
    text-align: center;
    margin-bottom: 0.25rem;
  }

  &__result-subtitle {
    text-align: center;
    font-size: 0.85rem;
    color: $color-text-muted;
    margin-bottom: 1.5rem;
  }

  &__result-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 0.75rem;
    margin-bottom: 1.5rem;
  }

  &__result-plant {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem;
    background: $color-leaf-50;
    border-radius: 10px;
    font-size: 0.9rem;
    color: $color-leaf-800;
  }

  &__result-plant-icon {
    font-size: 1.2rem;
  }

  &__nav {
    display: flex;
    justify-content: space-between;
    margin-top: 1.5rem;
  }

  &__back {
    padding: 0.75rem 1.5rem;
    border: 1px solid $color-border;
    border-radius: 10px;
    background: white;
    color: $color-text-muted;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      border-color: $color-leaf-400;
      color: $color-leaf-600;
    }
  }

  &__next, &__cta {
    padding: 0.75rem 2rem;
    border: none;
    border-radius: 10px;
    background: linear-gradient(135deg, $color-sun-500, $color-sun-600);
    color: white;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s $ease-spring;

    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 4px 15px rgba(234, 179, 8, 0.4);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  &__cta {
    display: block;
    width: 100%;
    text-align: center;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-700);

    &:hover {
      box-shadow: 0 4px 15px rgba(22, 163, 74, 0.4);
    }
  }
}

.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s ease;
}

.slide-left-enter-from { opacity: 0; transform: translateX(30px); }
.slide-left-leave-to { opacity: 0; transform: translateX(-30px); }
.slide-right-enter-from { opacity: 0; transform: translateX(-30px); }
.slide-right-leave-to { opacity: 0; transform: translateX(30px); }

@media (max-width: 640px) {
  .quiz__options {
    grid-template-columns: 1fr;
  }
}
</style>
