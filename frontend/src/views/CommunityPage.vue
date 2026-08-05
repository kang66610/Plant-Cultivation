<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ApiState, AuthModal } from '@/components/common'
import { listPlants } from '@/api/plantApi'
import * as postApi from '@/api/postApi'
import { uploadImage } from '@/api/uploadApi'
import type { Plant, Post, PostComment } from '@/types'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()

const posts = ref<Post[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const size = 10
const loadingMore = ref(false)
const hasMore = ref(false)
const nextCursorId = ref<number | null>(null)
const nextCursorCreatedAt = ref<string | null>(null)

const authModalRef = ref<InstanceType<typeof AuthModal> | null>(null)

const showCreatePost = ref(false)
const newPost = ref({ content: '', images: [] as string[], plantSlug: '', categorySlug: '' })
const activePostPicker = ref<'category' | 'plant' | null>(null)
const uploadLoading = ref(false)

const expandedPost = ref<number | null>(null)
const comments = ref<Record<number, PostComment[]>>({})
const newComment = ref('')
const categoryFilter = ref('')
const searchKeyword = ref('')
const searchInput = ref('')

const categories = [
  { slug: 'succulents', name: '多肉植物', emoji: '🌵' },
  { slug: 'tropical', name: '热带植物', emoji: '🌿' },
  { slug: 'herbs', name: '香草植物', emoji: '🌱' },
  { slug: 'ferns', name: '蕨类植物', emoji: '🌾' },
  { slug: 'flowering', name: '开花植物', emoji: '🌸' },
  { slug: 'trees', name: '室内树木', emoji: '🌳' },
]

const plants = ref<Pick<Plant, 'slug' | 'commonName'>[]>([])

const totalPages = computed(() => Math.ceil(total.value / size))

// Lightbox (image only)
const lightboxVisible = ref(false)
const lightboxSrc = ref('')

function openLightbox(src: string) {
  lightboxSrc.value = src
  lightboxVisible.value = true
  document.body.style.overflow = 'hidden'
}

function closeLightbox() {
  lightboxVisible.value = false
  lightboxSrc.value = ''
  document.body.style.overflow = ''
}

async function fetchPosts(reset = true) {
  if (reset) {
    loading.value = true
    nextCursorId.value = null
    nextCursorCreatedAt.value = null
  } else {
    loadingMore.value = true
  }
  try {
    const params: postApi.PostListParams = { page: 1, size }
    if (categoryFilter.value) params.categorySlug = categoryFilter.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (!reset && nextCursorId.value && nextCursorCreatedAt.value) {
      params.cursorId = nextCursorId.value
      params.cursorCreatedAt = nextCursorCreatedAt.value
    }
    const res = await postApi.listPosts(params)
    if (res.code === 200) {
      posts.value = reset ? res.data.records : [...posts.value, ...res.data.records]
      total.value = res.data.total
      hasMore.value = !!res.data.hasMore
      nextCursorId.value = res.data.nextCursorId ?? null
      nextCursorCreatedAt.value = res.data.nextCursorCreatedAt ?? null
    }
  } catch {} finally {
    loading.value = false
    loadingMore.value = false
  }
}

function handleSearch() {
  searchKeyword.value = searchInput.value.trim()
  page.value = 1
  fetchPosts()
}

function clearSearch() {
  searchInput.value = ''
  searchKeyword.value = ''
  page.value = 1
  fetchPosts()
}

function selectCategory(slug: string) {
  categoryFilter.value = categoryFilter.value === slug ? '' : slug
  page.value = 1
  fetchPosts()
}

function setPostCategory(slug: string) {
  newPost.value.categorySlug = slug
  activePostPicker.value = null
}

function setPostPlant(slug: string) {
  newPost.value.plantSlug = slug
  activePostPicker.value = null
}

function togglePostPicker(picker: 'category' | 'plant') {
  activePostPicker.value = activePostPicker.value === picker ? null : picker
}

const selectedPostCategory = computed(() => {
  return categories.find(cat => cat.slug === newPost.value.categorySlug)
})

const selectedPostPlantName = computed(() => {
  return plants.value.find(p => p.slug === newPost.value.plantSlug)?.commonName || ''
})

async function fetchPlants() {
  try {
    const res = await listPlants({ page: 1, size: 200 })
    if (res.code === 200) {
      plants.value = res.data.records.map((p) => ({ slug: p.slug, commonName: p.commonName }))
    }
  } catch {}
}

function openAuth(mode: 'login' | 'register') {
  authModalRef.value?.open(mode)
}

function requireAuth(callback: () => void) {
  if (!auth.isLoggedIn) {
    openAuth('login')
    return
  }
  callback()
}

async function handleCreatePost() {
  if (!newPost.value.content.trim()) return
  try {
    const res = await postApi.createPost({
      content: newPost.value.content,
      images: newPost.value.images.length ? JSON.stringify(newPost.value.images) : null,
      plantSlug: newPost.value.plantSlug || null,
      categorySlug: newPost.value.categorySlug || null
    })
    if (res.code === 200) {
      showCreatePost.value = false
      newPost.value = { content: '', images: [], plantSlug: '', categorySlug: '' }
      page.value = 1
      fetchPosts(true)
    }
  } catch {}
}

async function handleUploadImage(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploadLoading.value = true
  try {
    // 不手动设置 Content-Type：让浏览器自动生成带 boundary 的 multipart 头
    const res = await uploadImage(file)
    if (res.code === 200) {
      newPost.value.images.push(res.data)
    }
  } catch {} finally {
    uploadLoading.value = false
  }
}

async function toggleLike(post: Post) {
  requireAuth(async () => {
    try {
      const res = await postApi.togglePostLike(post.id)
      if (res.code === 200) {
        post.liked = res.data.liked
        post.likeCount += post.liked ? 1 : -1
      }
    } catch {}
  })
}

async function loadComments(postId: number) {
  try {
    const res = await postApi.listComments(postId, { page: 1, size: 50 })
    if (res.code === 200) {
      comments.value[postId] = res.data.records
    }
  } catch {}
}

function toggleComments(postId: number) {
  if (expandedPost.value === postId) {
    expandedPost.value = null
  } else {
    expandedPost.value = postId
    if (!comments.value[postId]) loadComments(postId)
  }
}

async function addComment(postId: number) {
  if (!newComment.value.trim()) return
  requireAuth(async () => {
    try {
      const res = await postApi.createComment(postId, newComment.value)
      if (res.code === 200) {
        if (!comments.value[postId]) comments.value[postId] = []
        comments.value[postId].push(res.data)
        const post = posts.value.find(p => p.id === postId)
        if (post) post.commentCount++
        newComment.value = ''
      }
    } catch {}
  })
}

async function deletePost(postId: number) {
  try {
    const res = await postApi.deletePost(postId)
    // 仅在后端确认成功后移除本地条目（无权限等场景后端返回 code≠200）
    if (res.code === 200) {
      posts.value = posts.value.filter(p => p.id !== postId)
    }
  } catch {}
}

function parseImages(images: string | null): string[] {
  if (!images) return []
  try { return JSON.parse(images) } catch { return [] }
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return t('community.justNow')
  if (mins < 60) return t('community.minutesAgo', { n: mins })
  const hours = Math.floor(mins / 60)
  if (hours < 24) return t('community.hoursAgo', { n: hours })
  const days = Math.floor(hours / 24)
  if (days < 7) return t('community.daysAgo', { n: days })
  return d.toLocaleDateString()
}

function goToPlant(slug: string) {
  router.push(`/plant/${slug}`)
}

onMounted(() => {
  fetchPosts()
  fetchPlants()
})
</script>

<template>
  <div class="community">
    <div class="community__header">
      <h1 class="community__title">{{ t('community.title') }}</h1>
      <p class="community__subtitle">{{ t('community.subtitle') }}</p>
    </div>

    <div class="community__toolbar">
      <div class="community__search">
        <svg class="community__search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35"/></svg>
        <input v-model="searchInput" :placeholder="t('community.searchPlaceholder')" @keyup.enter="handleSearch" class="community__search-input" />
        <button v-if="searchInput" class="community__search-clear" @click="clearSearch">&times;</button>
        <button class="community__search-btn" @click="handleSearch">{{ t('community.search') }}</button>
      </div>
      <button class="community__create-btn" @click="requireAuth(() => showCreatePost = true)">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
        {{ t('community.createPost') }}
      </button>
    </div>

    <!-- Category Filters -->
    <div class="community__categories">
      <button
        class="community__cat-btn"
        :class="{ 'community__cat-btn--active': !categoryFilter }"
        @click="selectCategory('')"
      >
        {{ t('community.allCategories') }}
      </button>
      <button
        v-for="cat in categories"
        :key="cat.slug"
        class="community__cat-btn"
        :class="{ 'community__cat-btn--active': categoryFilter === cat.slug }"
        @click="selectCategory(cat.slug)"
      >
        {{ cat.emoji }} {{ cat.name }}
      </button>
    </div>

    <!-- Posts List -->
    <ApiState :loading="loading" :empty="!loading && posts.length === 0" :empty-text="t('community.noPosts')">
    <div class="community__posts">
      <div v-for="post in posts" :key="post.id" class="post-card">
        <div class="post-card__header">
          <div class="post-card__user">
            <div class="post-card__avatar">
              <img v-if="post.avatarUrl" :src="post.avatarUrl" :alt="post.username" />
              <span v-else class="post-card__avatar-text">{{ post.username?.charAt(0) }}</span>
            </div>
            <div>
              <span class="post-card__username">{{ post.username }}</span>
              <span class="post-card__time">{{ formatDate(post.createdAt) }}</span>
            </div>
          </div>
          <div class="post-card__actions">
            <button v-if="post.categorySlug" class="post-card__tag post-card__tag--cat" @click="selectCategory(post.categorySlug)">
              {{ categories.find(c => c.slug === post.categorySlug)?.emoji }} {{ categories.find(c => c.slug === post.categorySlug)?.name }}
            </button>
            <button v-if="post.plantSlug" class="post-card__tag" @click="goToPlant(post.plantSlug)">
              {{ post.plantName || post.plantSlug }}
            </button>
            <button v-if="auth.isLoggedIn && auth.user?.account === post.userAccount"
              class="post-card__delete" @click="deletePost(post.id)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18M8 6V4h8v2M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6"/></svg>
            </button>
          </div>
        </div>
        <div class="post-card__content">{{ post.content }}</div>
        <div v-if="parseImages(post.images).length" class="post-card__images">
          <img v-for="(img, i) in parseImages(post.images)" :key="i" :src="img" alt="post image" loading="lazy" class="post-card__image" @click="openLightbox(img)" />
        </div>
        <div class="post-card__footer">
          <button class="post-card__action-btn" :class="{ 'post-card__action-btn--active': post.liked }" @click="toggleLike(post)">
            <svg viewBox="0 0 24 24" :fill="post.liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/></svg>
            {{ post.likeCount || '' }}
          </button>
          <button class="post-card__action-btn" @click="toggleComments(post.id)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
            {{ post.commentCount || '' }}
          </button>
        </div>
        <!-- Comments Section -->
        <div v-if="expandedPost === post.id" class="post-card__comments">
          <div v-for="c in comments[post.id] || []" :key="c.id" class="comment">
            <div class="comment__avatar">
              <img v-if="c.avatarUrl" :src="c.avatarUrl" :alt="c.username" />
              <span v-else>{{ c.username?.charAt(0) }}</span>
            </div>
            <div class="comment__body">
              <span class="comment__username">{{ c.username }}</span>
              <span class="comment__content">{{ c.content }}</span>
              <span class="comment__time">{{ formatDate(c.createdAt) }}</span>
            </div>
          </div>
          <div class="comment-input">
            <input v-model="newComment" :placeholder="t('community.commentPlaceholder')" @keyup.enter="addComment(post.id)" />
            <button @click="addComment(post.id)">{{ t('community.send') }}</button>
          </div>
        </div>
      </div>
    </div>
    </ApiState>

    <!-- Cursor load more -->
    <div v-if="hasMore" class="community__pagination">
      <button :disabled="loadingMore" @click="fetchPosts(false)">
        {{ loadingMore ? 'Loading...' : 'Load more' }}
      </button>
    </div>

    <!-- Auth Modal -->
    <AuthModal ref="authModalRef" />

    <!-- Create Post Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showCreatePost" class="auth-modal__overlay" @click.self="showCreatePost = false; activePostPicker = null">
          <div class="create-modal">
            <button class="auth-modal__close" @click="showCreatePost = false; activePostPicker = null">&times;</button>
            <h2>{{ t('community.createPost') }}</h2>
            <textarea v-model="newPost.content" :placeholder="t('community.contentPlaceholder')" rows="5"></textarea>
            <div class="create-modal__row">
              <div class="create-modal__picker">
                <button
                  class="create-modal__select"
                  :class="{ 'create-modal__select--open': activePostPicker === 'category', 'create-modal__select--filled': newPost.categorySlug }"
                  @click="togglePostPicker('category')"
                >
                  <span>{{ selectedPostCategory ? `${selectedPostCategory.emoji} ${selectedPostCategory.name}` : t('community.selectCategory') }}</span>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9l6 6 6-6"/></svg>
                </button>
                <Transition name="picker">
                  <div v-if="activePostPicker === 'category'" class="create-modal__menu">
                    <button class="create-modal__option" @click="setPostCategory('')">{{ t('community.selectCategory') }}</button>
                    <button
                      v-for="cat in categories"
                      :key="cat.slug"
                      class="create-modal__option"
                      :class="{ 'create-modal__option--active': newPost.categorySlug === cat.slug }"
                      @click="setPostCategory(cat.slug)"
                    >
                      {{ cat.emoji }} {{ cat.name }}
                    </button>
                  </div>
                </Transition>
              </div>

              <div class="create-modal__picker">
                <button
                  class="create-modal__select"
                  :class="{ 'create-modal__select--open': activePostPicker === 'plant', 'create-modal__select--filled': newPost.plantSlug }"
                  @click="togglePostPicker('plant')"
                >
                  <span>{{ selectedPostPlantName || t('community.selectPlant') }}</span>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 9l6 6 6-6"/></svg>
                </button>
                <Transition name="picker">
                  <div v-if="activePostPicker === 'plant'" class="create-modal__menu">
                    <button class="create-modal__option" @click="setPostPlant('')">{{ t('community.selectPlant') }}</button>
                    <button
                      v-for="p in plants"
                      :key="p.slug"
                      class="create-modal__option"
                      :class="{ 'create-modal__option--active': newPost.plantSlug === p.slug }"
                      @click="setPostPlant(p.slug)"
                    >
                      {{ p.commonName }}
                    </button>
                  </div>
                </Transition>
              </div>
            </div>
            <div class="create-modal__media">
              <label class="create-modal__upload-btn">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
                {{ t('community.uploadImage') }}
                <input type="file" accept="image/*" @change="handleUploadImage" hidden />
              </label>
              <span v-if="uploadLoading" class="create-modal__uploading">{{ t('community.uploading') }}</span>
            </div>
            <div v-if="newPost.images.length" class="create-modal__preview">
              <div v-for="(img, i) in newPost.images" :key="i" class="create-modal__preview-item">
                <img :src="img" alt="preview" />
                <button @click="newPost.images.splice(i, 1)">&times;</button>
              </div>
            </div>
            <button class="create-modal__submit" :disabled="!newPost.content.trim()" @click="handleCreatePost">
              {{ t('community.publish') }}
            </button>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Lightbox (image only) -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="lightboxVisible" class="lightbox" @click.self="closeLightbox">
          <button class="lightbox__close" @click="closeLightbox">&times;</button>
          <img :src="lightboxSrc" class="lightbox__media" alt="preview" />
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped lang="scss">
.community {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
  min-height: 80vh;

  &__header {
    text-align: center;
    margin-bottom: 2rem;
  }

  &__title {
    font-family: $font-display;
    font-size: clamp(1.75rem, 3vw, 2.5rem);
    color: $color-leaf-900;
    margin-bottom: 0.5rem;
  }

  &__subtitle {
    color: $color-text-muted;
    font-size: 1rem;
  }

  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    gap: 1rem;
    flex-wrap: wrap;
  }

  &__search {
    flex: 1;
    display: flex;
    align-items: center;
    background: linear-gradient(180deg, white, rgba(240, 253, 244, 0.76));
    border: 1px solid rgba(22, 163, 74, 0.16);
    border-radius: 999px;
    padding: 0 0.5rem;
    transition: border-color 0.22s ease, box-shadow 0.22s ease, transform 0.22s ease;
    max-width: 480px;
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.82);

    &:focus-within {
      border-color: rgba(34, 197, 94, 0.42);
      box-shadow: 0 10px 22px rgba(21, 128, 61, 0.1);
      transform: translateY(-1px);
    }
  }

  &__search-icon {
    width: 18px;
    height: 18px;
    color: $color-text-muted;
    flex-shrink: 0;
  }

  &__search-input {
    flex: 1;
    border: none;
    outline: none;
    padding: 0.55rem 0.5rem;
    font-size: 0.9rem;
    background: transparent;

    &::placeholder { color: #bbb; }
  }

  &__search-clear {
    background: none;
    border: none;
    font-size: 1.2rem;
    color: $color-text-muted;
    cursor: pointer;
    padding: 0 0.3rem;
    line-height: 1;

    &:hover { color: $color-text; }
  }

  &__search-btn {
    padding: 0.45rem 0.9rem;
    background: linear-gradient(135deg, $color-leaf-700, $color-leaf-500);
    color: white;
    border: none;
    border-radius: 999px;
    font-size: 0.85rem;
    font-weight: 600;
    cursor: pointer;
    flex-shrink: 0;
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 8px 16px rgba(22, 163, 74, 0.22);
    }

    &:active { transform: translateY(0) scale(0.98); }
  }

  &__categories {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1.5rem;
  }

  &__cat-btn {
    position: relative;
    overflow: hidden;
    padding: 0.52rem 1rem;
    background: linear-gradient(180deg, #ffffff, rgba(240, 253, 244, 0.76));
    border: 1px solid rgba(22, 163, 74, 0.16);
    border-radius: 999px;
    font-size: 0.82rem;
    font-weight: 650;
    color: $color-leaf-900;
    cursor: pointer;
    transition:
      transform 0.22s ease,
      color 0.22s ease,
      border-color 0.22s ease,
      box-shadow 0.22s ease,
      background-color 0.22s ease;
    white-space: nowrap;
    box-shadow:
      inset 0 1px 0 rgba(255, 255, 255, 0.82),
      0 4px 12px rgba(21, 128, 61, 0.05);

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: radial-gradient(circle at 22% 18%, rgba(187, 247, 208, 0.8), transparent 34%);
      opacity: 0;
      transition: opacity 0.25s ease;
      pointer-events: none;
    }

    &:hover {
      color: $color-leaf-700;
      border-color: rgba(34, 197, 94, 0.36);
      transform: translateY(-2px);
      box-shadow:
        inset 0 1px 0 rgba(255, 255, 255, 0.9),
        0 10px 22px rgba(21, 128, 61, 0.12);

      &::before {
        opacity: 1;
      }
    }

    &:active {
      transform: translateY(0) scale(0.98);
    }

    &--active {
      background: linear-gradient(135deg, $color-leaf-700, $color-leaf-500 58%, #22c55e);
      color: white;
      border-color: transparent;
      box-shadow:
        0 12px 24px rgba(22, 163, 74, 0.28),
        inset 0 1px 0 rgba(255, 255, 255, 0.22);

      &:hover {
        color: white;
        background: linear-gradient(135deg, $color-leaf-800, $color-leaf-600 58%, $color-leaf-500);
        transform: translateY(-2px) scale(1.02);
      }
    }
  }

  &__create-btn {
    display: inline-flex;
    align-items: center;
    gap: 0.4rem;
    position: relative;
    overflow: hidden;
    padding: 0.68rem 1.25rem;
    background: linear-gradient(135deg, $color-leaf-700, $color-leaf-500 62%, #22c55e);
    color: white;
    border: none;
    border-radius: 999px;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.22s ease, box-shadow 0.22s ease, filter 0.22s ease;
    box-shadow: 0 12px 24px rgba(22, 163, 74, 0.24);

    svg { width: 18px; height: 18px; }

    &:hover {
      transform: translateY(-2px);
      filter: saturate(1.08);
      box-shadow: 0 16px 30px rgba(22, 163, 74, 0.32);
    }

    &:active { transform: translateY(0) scale(0.98); }
  }

  &__loading {
    display: flex;
    justify-content: center;
    padding: 4rem 0;
  }

  &__spinner {
    width: 40px;
    height: 40px;
    border: 3px solid rgba(22, 163, 74, 0.2);
    border-top-color: $color-leaf-600;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  &__empty {
    text-align: center;
    padding: 4rem 0;
    color: $color-text-muted;
  }

  &__pagination {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 1rem;
    margin-top: 2rem;

    button {
      padding: 0.4rem 0.8rem;
      border: 1px solid var(--color-border);
      border-radius: 0.4rem;
      background: white;
      cursor: pointer;
      font-size: 0.9rem;

      &:disabled { opacity: 0.4; cursor: default; }
      &:not(:disabled):hover { background: $color-leaf-50; }
    }

    span { font-size: 0.9rem; color: $color-text-muted; }
  }
}

.post-card {
  background: white;
  border-radius: 1rem;
  padding: 1.25rem;
  margin-bottom: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(22, 163, 74, 0.12);
    box-shadow: 0 14px 32px rgba(20, 83, 45, 0.1);
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 0.75rem;
  }

  &__user {
    display: flex;
    align-items: center;
    gap: 0.6rem;
  }

  &__avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, $color-leaf-200, $color-leaf-400);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    flex-shrink: 0;

    img { width: 100%; height: 100%; object-fit: cover; }
    span { color: white; font-weight: 600; font-size: 1rem; }
  }

  &__username {
    display: block;
    font-weight: 600;
    font-size: 0.95rem;
    color: $color-leaf-900;
  }

  &__time {
    display: block;
    font-size: 0.8rem;
    color: $color-text-muted;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  &__tag {
    padding: 0.32rem 0.72rem;
    background: linear-gradient(180deg, rgba(240, 253, 244, 0.95), rgba(220, 252, 231, 0.75));
    color: $color-leaf-700;
    border: 1px solid rgba(34, 197, 94, 0.18);
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 650;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;

    &:hover {
      border-color: rgba(34, 197, 94, 0.36);
      transform: translateY(-1px);
      box-shadow: 0 8px 16px rgba(21, 128, 61, 0.1);
    }

    &:active { transform: translateY(0) scale(0.98); }

    &--cat {
      background: linear-gradient(180deg, rgba(236, 253, 245, 0.98), rgba(187, 247, 208, 0.62));
      color: $color-leaf-800;

      &:hover {
        background: linear-gradient(180deg, rgba(220, 252, 231, 0.98), rgba(187, 247, 208, 0.72));
      }
    }
  }

  &__delete {
    background: rgba(254, 242, 242, 0.7);
    border: 1px solid rgba(239, 68, 68, 0.12);
    border-radius: 999px;
    cursor: pointer;
    color: #ef4444;
    padding: 0.28rem;
    opacity: 0.5;
    transition: opacity 0.2s ease, transform 0.2s ease, background-color 0.2s ease;

    &:hover {
      opacity: 1;
      transform: translateY(-1px);
      background: rgba(254, 226, 226, 0.9);
    }
    svg { width: 16px; height: 16px; }
  }

  &__content {
    font-size: 0.95rem;
    line-height: 1.7;
    color: $color-text;
    margin-bottom: 0.75rem;
    white-space: pre-wrap;
    word-break: break-word;
  }

  &__images {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 0.5rem;
    margin-bottom: 0.75rem;
  }

  &__image {
    width: 100%;
    aspect-ratio: 1;
    object-fit: cover;
    border-radius: 0.5rem;
    cursor: pointer;
    transition: transform 0.24s ease, box-shadow 0.24s ease, filter 0.24s ease;

    &:hover {
      transform: translateY(-2px) scale(1.025);
      filter: saturate(1.05);
      box-shadow: 0 12px 24px rgba(20, 83, 45, 0.14);
    }
  }

  &__footer {
    display: flex;
    gap: 1.5rem;
    padding-top: 0.5rem;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
  }

  &__action-btn {
    display: inline-flex;
    align-items: center;
    gap: 0.3rem;
    background: transparent;
    border: 1px solid transparent;
    border-radius: 999px;
    color: $color-text-muted;
    font-size: 0.85rem;
    cursor: pointer;
    padding: 0.36rem 0.6rem;
    transition: color 0.2s ease, background-color 0.2s ease, transform 0.2s ease, border-color 0.2s ease;

    svg { width: 18px; height: 18px; }

    &:hover {
      color: $color-leaf-700;
      background: rgba(240, 253, 244, 0.9);
      border-color: rgba(34, 197, 94, 0.14);
      transform: translateY(-1px);
    }

    &:active { transform: translateY(0) scale(0.98); }
    &--active { color: #ef4444; background: rgba(254, 242, 242, 0.78); }
  }

  &__comments {
    margin-top: 0.75rem;
    padding-top: 0.75rem;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
  }
}

.comment {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.6rem;

  &__avatar {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: $color-leaf-200;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    font-size: 0.7rem;
    color: white;
    font-weight: 600;
    overflow: hidden;

    img { width: 100%; height: 100%; object-fit: cover; }
  }

  &__body {
    flex: 1;
    background: #f8faf8;
    padding: 0.5rem 0.75rem;
    border-radius: 0.75rem;
  }

  &__username {
    font-weight: 600;
    font-size: 0.8rem;
    color: $color-leaf-800;
    margin-right: 0.4rem;
  }

  &__content {
    font-size: 0.85rem;
    color: $color-text;
  }

  &__time {
    display: block;
    font-size: 0.7rem;
    color: $color-text-muted;
    margin-top: 0.2rem;
  }
}

.comment-input {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;

  input {
    flex: 1;
    padding: 0.5rem 0.75rem;
    border: 1px solid var(--color-border);
    border-radius: 1rem;
    font-size: 0.85rem;
    outline: none;

    &:focus { border-color: $color-leaf-400; }
  }

  button {
    padding: 0.5rem 1rem;
    background: $color-leaf-600;
    color: white;
    border: none;
    border-radius: 1rem;
    font-size: 0.85rem;
    font-weight: 600;
    cursor: pointer;

    &:hover { background: $color-leaf-700; }
  }
}

// Auth Modal
.auth-modal__overlay {
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

.create-modal {
  position: relative;
  background: white;
  border-radius: 1.25rem;
  padding: 2rem;
  width: 100%;
  max-width: 560px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);

  h2 {
    font-family: $font-display;
    font-size: 1.3rem;
    color: $color-leaf-900;
    margin-bottom: 1rem;
  }

  textarea {
    width: 100%;
    padding: 0.75rem 1rem;
    border: 1px solid rgba(22, 163, 74, 0.16);
    border-radius: 1rem;
    font-size: 0.95rem;
    resize: vertical;
    outline: none;
    font-family: inherit;
    background: linear-gradient(180deg, white, rgba(240, 253, 244, 0.62));
    transition: border-color 0.22s ease, box-shadow 0.22s ease, transform 0.22s ease;

    &:focus {
      border-color: rgba(34, 197, 94, 0.42);
      box-shadow: 0 10px 24px rgba(21, 128, 61, 0.1);
      transform: translateY(-1px);
    }
  }

  &__row {
    margin-top: 0.75rem;
    display: flex;
    gap: 0.65rem;
    align-items: flex-start;
  }

  &__picker {
    position: relative;
    flex: 1;
    min-width: 0;
  }

  &__select {
    width: 100%;
    min-height: 42px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.75rem;
    padding: 0.58rem 0.9rem 0.58rem 1rem;
    border: 1px solid rgba(22, 163, 74, 0.16);
    border-radius: 999px;
    font-size: 0.9rem;
    font-weight: 600;
    color: $color-text-muted;
    background: linear-gradient(180deg, white, rgba(240, 253, 244, 0.78));
    cursor: pointer;
    transition: border-color 0.22s ease, box-shadow 0.22s ease, transform 0.22s ease, color 0.22s ease;
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.82);

    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    svg {
      width: 16px;
      height: 16px;
      flex-shrink: 0;
      color: $color-leaf-600;
      transition: transform 0.22s ease;
    }

    &:hover,
    &--open {
      color: $color-leaf-800;
      border-color: rgba(34, 197, 94, 0.38);
      box-shadow: 0 10px 22px rgba(21, 128, 61, 0.1);
      transform: translateY(-1px);
    }

    &--open svg {
      transform: rotate(180deg);
    }

    &--filled {
      color: $color-leaf-900;
    }
  }

  &__menu {
    position: absolute;
    z-index: 12;
    left: 0;
    right: 0;
    top: calc(100% + 0.45rem);
    max-height: 240px;
    overflow-y: auto;
    padding: 0.38rem;
    border: 1px solid rgba(22, 163, 74, 0.18);
    border-radius: 1rem;
    background:
      linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(240, 253, 244, 0.96));
    box-shadow:
      0 18px 44px rgba(20, 83, 45, 0.16),
      inset 0 1px 0 rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(12px);
  }

  &__option {
    width: 100%;
    display: flex;
    align-items: center;
    padding: 0.58rem 0.72rem;
    border: 0;
    border-radius: 0.75rem;
    background: transparent;
    color: $color-leaf-900;
    font-size: 0.88rem;
    line-height: 1.35;
    text-align: left;
    cursor: pointer;
    transition: background-color 0.18s ease, color 0.18s ease, transform 0.18s ease;

    &:hover {
      background: rgba(187, 247, 208, 0.55);
      color: $color-leaf-800;
      transform: translateX(2px);
    }

    &--active {
      background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
      color: white;
      box-shadow: 0 8px 18px rgba(22, 163, 74, 0.22);

      &:hover {
        color: white;
        transform: translateX(0);
      }
    }
  }

  &__media {
    display: flex;
    gap: 0.75rem;
    margin-top: 0.75rem;
    align-items: center;
    flex-wrap: wrap;
  }

  &__upload-btn {
    display: inline-flex;
    align-items: center;
    gap: 0.3rem;
    padding: 0.5rem 0.92rem;
    background: linear-gradient(180deg, white, rgba(240, 253, 244, 0.86));
    border: 1px solid rgba(34, 197, 94, 0.22);
    border-radius: 999px;
    font-size: 0.8rem;
    font-weight: 600;
    color: $color-leaf-700;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;

    svg { width: 16px; height: 16px; }
    &:hover {
      border-color: rgba(34, 197, 94, 0.42);
      transform: translateY(-2px);
      box-shadow: 0 8px 18px rgba(21, 128, 61, 0.1);
    }

    &:active { transform: translateY(0) scale(0.98); }
  }

  &__uploading {
    font-size: 0.8rem;
    color: $color-text-muted;
  }

  &__preview {
    display: flex;
    gap: 0.5rem;
    margin-top: 0.75rem;
    flex-wrap: wrap;
  }

  &__preview-item {
    position: relative;
    width: 80px;
    height: 80px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 0.4rem;
    }

    button {
      position: absolute;
      top: -4px;
      right: -4px;
      width: 20px;
      height: 20px;
      background: #ef4444;
      color: white;
      border: 2px solid white;
      border-radius: 50%;
      font-size: 0.7rem;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      line-height: 1;
    }
  }

  &__submit {
    width: 100%;
    padding: 0.82rem;
    background: linear-gradient(135deg, $color-leaf-700, $color-leaf-500 58%, #22c55e);
    color: white;
    border: none;
    border-radius: 999px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    margin-top: 1rem;
    transition: transform 0.22s ease, box-shadow 0.22s ease, filter 0.22s ease;
    box-shadow: 0 14px 28px rgba(22, 163, 74, 0.24);

    &:hover {
      transform: translateY(-2px);
      filter: saturate(1.08);
      box-shadow: 0 18px 34px rgba(22, 163, 74, 0.32);
    }

    &:active { transform: translateY(0) scale(0.98); }
    &:disabled { opacity: 0.5; cursor: default; }
  }
}

.modal-enter-active { transition: opacity 0.3s ease; }
.modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }

.picker-enter-active,
.picker-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.picker-enter-from,
.picker-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}

@keyframes spin { to { transform: rotate(360deg); } }

// Lightbox (image only)
.lightbox {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(0, 0, 0, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;

  &__close {
    position: absolute;
    top: 1rem;
    right: 1.5rem;
    background: rgba(255, 255, 255, 0.15);
    border: none;
    color: white;
    font-size: 2rem;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;
    z-index: 2001;
    line-height: 1;

    &:hover { background: rgba(255, 255, 255, 0.3); }
  }

  &__media {
    max-width: 90vw;
    max-height: 90vh;
    object-fit: contain;
    border-radius: 0.5rem;
  }
}

@media (max-width: 640px) {
  .community {
    padding: 1rem;
  }
  .post-card {
    padding: 1rem;
  }
  .auth-modal {
    padding: 2rem 1.5rem 1.5rem;
  }
  .create-modal {
    padding: 1.5rem;

    &__row {
      flex-direction: column;
    }

    &__picker {
      width: 100%;
    }
  }

  .community {
    &__toolbar {
      align-items: stretch;
    }

    &__search,
    &__create-btn {
      width: 100%;
      max-width: none;
    }

    &__create-btn {
      justify-content: center;
    }
  }
}
</style>
