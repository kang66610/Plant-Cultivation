<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { AuthModal } from '@/components/common'
import request from '@/api/request'

const { t } = useI18n()
const router = useRouter()
const auth = useAuthStore()

interface Post {
  id: number
  userAccount: string
  content: string
  images: string | null
  plantSlug: string | null
  categorySlug: string | null
  plantName: string | null
  likeCount: number
  commentCount: number
  createdAt: string
  username: string
  avatarUrl: string | null
  liked: boolean
}

interface Comment {
  id: number
  postId: number
  userAccount: string
  content: string
  createdAt: string
  username: string
  avatarUrl: string | null
}

const posts = ref<Post[]>([])
const loading = ref(true)
const page = ref(1)
const total = ref(0)
const size = 10

const authModalRef = ref<InstanceType<typeof AuthModal> | null>(null)

const showCreatePost = ref(false)
const newPost = ref({ content: '', images: [] as string[], plantSlug: '', categorySlug: '' })
const uploadLoading = ref(false)

const expandedPost = ref<number | null>(null)
const comments = ref<Record<number, Comment[]>>({})
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

const plants = ref<{ slug: string; commonName: string }[]>([])

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

async function fetchPosts() {
  loading.value = true
  try {
    const params: any = { page: page.value, size }
    if (categoryFilter.value) params.categorySlug = categoryFilter.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res: any = await request.get('/posts', { params })
    if (res.code === 200) {
      posts.value = res.data.records
      total.value = res.data.total
    }
  } catch {} finally {
    loading.value = false
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

async function fetchPlants() {
  try {
    const res: any = await request.get('/plants', { params: { page: 1, size: 200 } })
    if (res.code === 200) {
      plants.value = res.data.records.map((p: any) => ({ slug: p.slug, commonName: p.commonName }))
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
    const res: any = await request.post('/posts', {
      content: newPost.value.content,
      images: newPost.value.images.length ? JSON.stringify(newPost.value.images) : null,
      plantSlug: newPost.value.plantSlug || null,
      categorySlug: newPost.value.categorySlug || null
    })
    if (res.code === 200) {
      showCreatePost.value = false
      newPost.value = { content: '', images: [], plantSlug: '', categorySlug: '' }
      page.value = 1
      fetchPosts()
    }
  } catch {}
}

async function handleUploadImage(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploadLoading.value = true
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res: any = await request.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
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
      const res: any = await request.post(`/posts/${post.id}/like`)
      if (res.code === 200) {
        post.liked = res.data.liked
        post.likeCount += post.liked ? 1 : -1
      }
    } catch {}
  })
}

async function loadComments(postId: number) {
  try {
    const res: any = await request.get(`/posts/${postId}/comments`, { params: { page: 1, size: 50 } })
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
      const res: any = await request.post(`/posts/${postId}/comments`, { content: newComment.value })
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
    await request.delete(`/posts/${postId}`)
    posts.value = posts.value.filter(p => p.id !== postId)
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
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return d.toLocaleDateString('zh-CN')
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
    <div v-if="loading" class="community__loading">
      <div class="community__spinner"></div>
    </div>
    <div v-else-if="posts.length === 0" class="community__empty">
      <p>{{ t('community.noPosts') }}</p>
    </div>
    <div v-else class="community__posts">
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
          <img v-for="(img, i) in parseImages(post.images)" :key="i" :src="img" alt="post image" class="post-card__image" @click="openLightbox(img)" />
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

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="community__pagination">
      <button :disabled="page <= 1" @click="page--; fetchPosts()">&laquo;</button>
      <span>{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="page++; fetchPosts()">&raquo;</button>
    </div>

    <!-- Auth Modal -->
    <AuthModal ref="authModalRef" />

    <!-- Create Post Modal -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="showCreatePost" class="auth-modal__overlay" @click.self="showCreatePost = false">
          <div class="create-modal">
            <button class="auth-modal__close" @click="showCreatePost = false">&times;</button>
            <h2>{{ t('community.createPost') }}</h2>
            <textarea v-model="newPost.content" :placeholder="t('community.contentPlaceholder')" rows="5"></textarea>
            <div class="create-modal__row">
              <select v-model="newPost.categorySlug" class="create-modal__select">
                <option value="">{{ t('community.selectCategory') }}</option>
                <option v-for="cat in categories" :key="cat.slug" :value="cat.slug">{{ cat.emoji }} {{ cat.name }}</option>
              </select>
              <select v-model="newPost.plantSlug" class="create-modal__select">
                <option value="">{{ t('community.selectPlant') }}</option>
                <option v-for="p in plants" :key="p.slug" :value="p.slug">{{ p.commonName }}</option>
              </select>
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
    background: white;
    border: 1.5px solid var(--color-border);
    border-radius: 0.6rem;
    padding: 0 0.5rem;
    transition: border-color 0.2s;
    max-width: 480px;

    &:focus-within { border-color: $color-leaf-400; }
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
    padding: 0.4rem 0.8rem;
    background: $color-leaf-600;
    color: white;
    border: none;
    border-radius: 0.4rem;
    font-size: 0.85rem;
    font-weight: 600;
    cursor: pointer;
    flex-shrink: 0;

    &:hover { background: $color-leaf-700; }
  }

  &__categories {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1.5rem;
  }

  &__cat-btn {
    padding: 0.4rem 0.85rem;
    background: white;
    border: 1.5px solid var(--color-border);
    border-radius: 2rem;
    font-size: 0.82rem;
    font-weight: 500;
    color: $color-text-muted;
    cursor: pointer;
    transition: all 0.2s ease;
    white-space: nowrap;

    &:hover {
      border-color: $color-leaf-400;
      color: $color-leaf-700;
      background: $color-leaf-50;
    }

    &--active {
      background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
      color: white;
      border-color: transparent;

      &:hover {
        color: white;
        background: linear-gradient(135deg, $color-leaf-700, $color-leaf-600);
      }
    }
  }

  &__create-btn {
    display: inline-flex;
    align-items: center;
    gap: 0.4rem;
    padding: 0.6rem 1.2rem;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white;
    border: none;
    border-radius: 0.5rem;
    font-size: 0.9rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;

    svg { width: 18px; height: 18px; }

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(22, 163, 74, 0.3);
    }
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
    padding: 0.2rem 0.6rem;
    background: rgba(34, 197, 94, 0.1);
    color: $color-leaf-600;
    border: none;
    border-radius: 1rem;
    font-size: 0.75rem;
    font-weight: 500;
    cursor: pointer;

    &:hover { background: rgba(34, 197, 94, 0.2); }

    &--cat {
      background: rgba(59, 130, 246, 0.1);
      color: #2563eb;

      &:hover { background: rgba(59, 130, 246, 0.2); }
    }
  }

  &__delete {
    background: none;
    border: none;
    cursor: pointer;
    color: #ef4444;
    padding: 0.2rem;
    opacity: 0.5;
    transition: opacity 0.2s;

    &:hover { opacity: 1; }
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
    transition: transform 0.2s, box-shadow 0.2s;

    &:hover { transform: scale(1.02); box-shadow: 0 4px 16px rgba(0,0,0,0.12); }
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
    background: none;
    border: none;
    color: $color-text-muted;
    font-size: 0.85rem;
    cursor: pointer;
    padding: 0.3rem 0;
    transition: color 0.2s;

    svg { width: 18px; height: 18px; }

    &:hover { color: $color-leaf-600; }
    &--active { color: #ef4444; }
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
    border: 1.5px solid var(--color-border);
    border-radius: 0.6rem;
    font-size: 0.95rem;
    resize: vertical;
    outline: none;
    font-family: inherit;

    &:focus { border-color: $color-leaf-400; }
  }

  &__row {
    margin-top: 0.75rem;
    display: flex;
    gap: 0.5rem;
  }

  &__select {
    flex: 1;
    padding: 0.5rem 1rem;
    border: 1px solid var(--color-border);
    border-radius: 0.5rem;
    font-size: 0.9rem;
    background: white;
    cursor: pointer;
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
    padding: 0.4rem 0.8rem;
    background: $color-leaf-50;
    border: 1px solid rgba(34, 197, 94, 0.2);
    border-radius: 0.5rem;
    font-size: 0.8rem;
    color: $color-leaf-700;
    cursor: pointer;
    transition: background 0.2s;

    svg { width: 16px; height: 16px; }
    &:hover { background: $color-leaf-100; }
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
    padding: 0.75rem;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white;
    border: none;
    border-radius: 0.6rem;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    margin-top: 1rem;

    &:hover { box-shadow: 0 4px 12px rgba(22, 163, 74, 0.3); }
    &:disabled { opacity: 0.5; cursor: default; }
  }
}

.modal-enter-active { transition: opacity 0.3s ease; }
.modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }

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
  }
}
</style>
