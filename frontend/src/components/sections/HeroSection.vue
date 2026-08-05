<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

const heroRef = ref<HTMLElement>()
const canvasRef = ref<HTMLCanvasElement>()
const fallCanvasRef = ref<HTMLCanvasElement>()

const titleWords = computed(() => {
  if (locale.value === 'zh-CN') {
    return [t('hero.word1'), t('hero.word2'), t('hero.word3'), t('hero.word4')]
  }
  return [t('hero.word1'), t('hero.word2'), t('hero.word3'), t('hero.word4'), t('hero.word5')]
})

const heroBadges = computed(() => {
  if (locale.value === 'zh-CN') {
    return ['光照匹配', '浇水提醒', '养护记录']
  }
  return ['Light match', 'Watering rhythm', 'Care journal']
})

const showcaseTop = [
  { name: 'Monstera', image: '/images/plants/monstera.jpg' },
  { name: 'Pothos', image: '/images/plants/pothos.jpg' },
  { name: 'Peace lily', image: '/images/plants/peace-lily.jpg' },
  { name: 'Calathea', image: '/images/plants/calathea.jpg' },
  { name: 'Bird of paradise', image: '/images/plants/bird-of-paradise.jpg' },
  { name: 'Areca palm', image: '/images/plants/areca-palm.jpg' },
  { name: 'Rubber plant', image: '/images/plants/rubber-plant.jpg' },
  { name: 'Alocasia', image: '/images/plants/alocasia-amazonica.jpg' },
  { name: 'Arrowhead', image: '/images/plants/arrowhead.jpg' },
  { name: 'Asplenium', image: '/images/plants/asplenium-nidus.jpg' },
  { name: 'Basil', image: '/images/plants/basil.jpg' },
  { name: 'Caladium', image: '/images/plants/caladium-bicolor.jpg' },
  { name: 'China doll', image: '/images/plants/china-doll.jpg' },
  { name: 'Chinese evergreen', image: '/images/plants/chinese-evergreen.jpg' },
  { name: 'Cyclamen', image: '/images/plants/cyclamen.jpg' },
  { name: 'Dragon tree', image: '/images/plants/dragon-tree.jpg' },
  { name: 'Dumb cane', image: '/images/plants/dumb-cane.jpg' },
  { name: 'Fuchsia', image: '/images/plants/fuchsia.jpg' },
  { name: 'Jade plant', image: '/images/plants/jade-plant.jpg' },
  { name: 'Lavender', image: '/images/plants/lavender.jpg' },
  { name: 'Mint', image: '/images/plants/mint.jpg' },
  { name: 'Orchid', image: '/images/plants/orchid.jpg' },
  { name: 'Parlor palm', image: '/images/plants/parlor-palm.jpg' },
  { name: 'Pennywort', image: '/images/plants/pennywort.jpg' },
  { name: 'Philodendron', image: '/images/plants/philodendron.jpg' },
]

const showcaseBottom = [
  { name: 'Snake plant', image: '/images/plants/snake-plant.jpg' },
  { name: 'Aloe vera', image: '/images/plants/aloe-vera.jpg' },
  { name: 'Spider plant', image: '/images/plants/spider-plant.jpg' },
  { name: 'ZZ plant', image: '/images/plants/zz-plant.jpg' },
  { name: 'Fiddle leaf fig', image: '/images/plants/fiddle-leaf-fig.jpg' },
  { name: 'Boston fern', image: '/images/plants/boston-fern.jpg' },
  { name: 'Peperomia', image: '/images/plants/peperomia.jpg' },
  { name: 'Alocasia cucullata', image: '/images/plants/alocasia-cucullata.jpg' },
  { name: 'Cactus', image: '/images/plants/cactus.jpg' },
  { name: 'Chinese money plant', image: '/images/plants/chinese-money-plant.jpg' },
  { name: 'Echeveria', image: '/images/plants/echeveria-chihuahuensis.jpg' },
  { name: 'Hedera', image: '/images/plants/hedera-helix.jpg' },
  { name: 'Hydrangea', image: '/images/plants/hydrangea-endless-summer.jpg' },
  { name: 'Murraya', image: '/images/plants/murraya-exotica.jpg' },
  { name: 'Nerve plant', image: '/images/plants/nerve-plant.jpg' },
  { name: 'Oncidium', image: '/images/plants/oncidium.jpg' },
  { name: 'Pieris', image: '/images/plants/pieris-japonica.jpg' },
  { name: 'Pilea', image: '/images/plants/pilea-cadierei.jpg' },
  { name: 'Ponytail palm', image: '/images/plants/ponytail-palm.jpg' },
  { name: 'Satin pothos', image: '/images/plants/satin-pothos.jpg' },
  { name: 'Selloum', image: '/images/plants/selloum.jpg' },
  { name: 'String of pearls', image: '/images/plants/string-of-pearls.jpg' },
  { name: 'Sunflower', image: '/images/plants/sunflower.jpg' },
  { name: 'Tradescantia', image: '/images/plants/tradescantia-quadricolor.jpg' },
  { name: 'Philodendron imperial', image: '/images/plants/philodendron-imperial-green.jpg' },
]

const revealedWords = ref<boolean[]>([])
const subtitleVisible = ref(false)
const ctaVisible = ref(false)

function resetReveal() {
  const count = titleWords.value.length
  revealedWords.value = new Array(count).fill(false)
  subtitleVisible.value = false
  ctaVisible.value = false
  titleWords.value.forEach((_, i) => {
    setTimeout(() => { revealedWords.value[i] = true }, 200 + i * 150)
  })
  setTimeout(() => { subtitleVisible.value = true }, 200 + count * 150 + 300)
  setTimeout(() => { ctaVisible.value = true }, 200 + count * 150 + 600)
}

// Watch locale changes to re-trigger animation
watch(locale, () => {
  resetReveal()
})

// Particle system
interface Particle {
  x: number
  y: number
  size: number
  speedY: number
  speedX: number
  opacity: number
  rotation: number
  rotationSpeed: number
  type: number
}

let particles: Particle[] = []
let animationId: number
let mouseX = 0
let mouseY = 0

// Falling leaves
interface FallingLeaf {
  x: number
  y: number
  size: number
  speedY: number
  swayAmplitude: number
  swaySpeed: number
  swayOffset: number
  rotation: number
  rotationSpeed: number
  opacity: number
  type: number
}

let fallingLeaves: FallingLeaf[] = []
let fallAnimationId: number

function initFallingLeaves(canvas: HTMLCanvasElement) {
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  fallingLeaves = Array.from({ length: 15 }, () => ({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height * -1,
    size: Math.random() * 12 + 8,
    speedY: Math.random() * 1.2 + 0.8,
    swayAmplitude: Math.random() * 30 + 15,
    swaySpeed: Math.random() * 0.02 + 0.01,
    swayOffset: Math.random() * Math.PI * 2,
    rotation: Math.random() * 360,
    rotationSpeed: (Math.random() - 0.5) * 1.5,
    opacity: Math.random() * 0.25 + 0.1,
    type: Math.floor(Math.random() * 3),
  }))
}

function drawFallingLeaf(ctx: CanvasRenderingContext2D, leaf: FallingLeaf) {
  ctx.save()
  ctx.translate(leaf.x, leaf.y)
  ctx.rotate((leaf.rotation * Math.PI) / 180)
  ctx.globalAlpha = leaf.opacity

  if (leaf.type === 0) {
    // Simple oval leaf
    ctx.fillStyle = '#4ade80'
    ctx.beginPath()
    ctx.ellipse(0, 0, leaf.size * 0.35, leaf.size * 0.7, 0, 0, Math.PI * 2)
    ctx.fill()
    ctx.strokeStyle = '#22c55e'
    ctx.lineWidth = 0.5
    ctx.beginPath()
    ctx.moveTo(0, -leaf.size * 0.7)
    ctx.lineTo(0, leaf.size * 0.7)
    ctx.stroke()
  } else if (leaf.type === 1) {
    // Heart-shaped leaf
    ctx.fillStyle = '#86efac'
    ctx.beginPath()
    ctx.moveTo(0, leaf.size * 0.3)
    ctx.bezierCurveTo(-leaf.size * 0.5, -leaf.size * 0.1, -leaf.size * 0.3, -leaf.size * 0.6, 0, -leaf.size * 0.4)
    ctx.bezierCurveTo(leaf.size * 0.3, -leaf.size * 0.6, leaf.size * 0.5, -leaf.size * 0.1, 0, leaf.size * 0.3)
    ctx.fill()
  } else {
    // Small round leaf
    ctx.fillStyle = '#6ee7b7'
    ctx.beginPath()
    ctx.ellipse(0, 0, leaf.size * 0.4, leaf.size * 0.3, 0, 0, Math.PI * 2)
    ctx.fill()
    ctx.strokeStyle = '#34d399'
    ctx.lineWidth = 0.4
    ctx.beginPath()
    ctx.moveTo(-leaf.size * 0.4, 0)
    ctx.lineTo(leaf.size * 0.4, 0)
    ctx.stroke()
  }
  ctx.restore()
}

function animateFallingLeaves(canvas: HTMLCanvasElement) {
  const ctx = canvas.getContext('2d')!
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  fallingLeaves.forEach((leaf) => {
    leaf.y += leaf.speedY
    leaf.x += Math.sin(leaf.y * leaf.swaySpeed + leaf.swayOffset) * 0.8
    leaf.rotation += leaf.rotationSpeed

    if (leaf.y > canvas.height + 20) {
      leaf.y = -20
      leaf.x = Math.random() * canvas.width
    }
  })

  fallingLeaves.forEach((leaf) => drawFallingLeaf(ctx, leaf))
  fallAnimationId = requestAnimationFrame(() => animateFallingLeaves(canvas))
}

function initParticles(canvas: HTMLCanvasElement) {
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  particles = Array.from({ length: 40 }, () => ({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height,
    size: Math.random() * 5 + 2,
    speedY: -(Math.random() * 0.4 + 0.15),
    speedX: (Math.random() - 0.5) * 0.25,
    opacity: Math.random() * 0.22 + 0.06,
    rotation: Math.random() * 360,
    rotationSpeed: (Math.random() - 0.5) * 1.5,
    type: Math.floor(Math.random() * 4),
  }))
}

function drawLeaf(ctx: CanvasRenderingContext2D, x: number, y: number, size: number, rotation: number, opacity: number, type: number) {
  ctx.save()
  ctx.translate(x, y)
  ctx.rotate((rotation * Math.PI) / 180)
  ctx.globalAlpha = opacity

  if (type === 0) {
    // Simple oval leaf
    ctx.fillStyle = '#74c69d'
    ctx.beginPath()
    ctx.ellipse(0, 0, size * 0.4, size, 0, 0, Math.PI * 2)
    ctx.fill()
  } else if (type === 1) {
    // Monstera-like leaf
    ctx.fillStyle = '#4ade80'
    ctx.beginPath()
    ctx.ellipse(0, 0, size * 0.6, size * 0.9, 0, 0, Math.PI * 2)
    ctx.fill()
    ctx.globalAlpha = opacity * 0.6
    ctx.fillStyle = '#052e16'
    ctx.beginPath()
    ctx.ellipse(size * 0.15, -size * 0.1, size * 0.12, size * 0.2, 0.3, 0, Math.PI * 2)
    ctx.fill()
  } else if (type === 2) {
    // Round leaf (like Pilea)
    ctx.fillStyle = '#86efac'
    ctx.beginPath()
    ctx.arc(0, 0, size * 0.5, 0, Math.PI * 2)
    ctx.fill()
    ctx.globalAlpha = opacity * 0.4
    ctx.strokeStyle = '#22c55e'
    ctx.lineWidth = 0.5
    ctx.beginPath()
    ctx.moveTo(0, -size * 0.5)
    ctx.lineTo(0, size * 0.5)
    ctx.stroke()
  } else {
    // Thin fern frond
    ctx.strokeStyle = '#6ee7b7'
    ctx.lineWidth = size * 0.08
    ctx.lineCap = 'round'
    ctx.beginPath()
    ctx.moveTo(0, size)
    ctx.quadraticCurveTo(size * 0.3, 0, 0, -size)
    ctx.stroke()
    for (let i = 0; i < 5; i++) {
      const t = (i + 1) / 6
      const fx = size * 0.3 * t * (1 - t) * 4 * 0.25
      const fy = size - t * size * 2
      ctx.beginPath()
      ctx.moveTo(0, fy)
      ctx.quadraticCurveTo(fx + size * 0.15, fy - size * 0.08, fx + size * 0.25, fy + size * 0.05)
      ctx.stroke()
    }
  }
  ctx.restore()
}

function animateParticles(canvas: HTMLCanvasElement) {
  const ctx = canvas.getContext('2d')!
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  particles.forEach((p) => {
    p.y += p.speedY
    p.x += p.speedX + Math.sin(p.y * 0.01) * 0.2
    p.rotation += p.rotationSpeed

    if (p.y < -10) {
      p.y = canvas.height + 10
      p.x = Math.random() * canvas.width
    }

    drawLeaf(ctx, p.x, p.y, p.size, p.rotation, p.opacity, p.type)
  })

  animationId = requestAnimationFrame(() => animateParticles(canvas))
}

function handleMouseMove(e: MouseEvent) {
  mouseX = (e.clientX / window.innerWidth - 0.5) * 10
  mouseY = (e.clientY / window.innerHeight - 0.5) * 10
}

// 具名 resize 处理器：onUnmounted 时才能移除（匿名函数会泄漏监听器）
function handleResize() {
  if (canvasRef.value) {
    canvasRef.value.width = window.innerWidth
    canvasRef.value.height = window.innerHeight
  }
  if (fallCanvasRef.value) {
    fallCanvasRef.value.width = window.innerWidth
    fallCanvasRef.value.height = window.innerHeight
  }
}

onMounted(() => {
  // Word reveal animation
  resetReveal()

  // Particles
  if (canvasRef.value) {
    initParticles(canvasRef.value)
    animateParticles(canvasRef.value)
  }

  // Falling leaves
  if (fallCanvasRef.value) {
    initFallingLeaves(fallCanvasRef.value)
    animateFallingLeaves(fallCanvasRef.value)
  }

  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  cancelAnimationFrame(fallAnimationId)
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <section ref="heroRef" class="hero">
    <canvas ref="canvasRef" class="hero__particles" />
    <canvas ref="fallCanvasRef" class="hero__falling-leaves" />

    <!-- Background texture -->
    <div class="hero__texture" />

    <!-- Aurora overlay -->
    <div class="hero__aurora" />

    <!-- Ambient glow orbs -->
    <div class="hero__orb hero__orb--1" />
    <div class="hero__orb hero__orb--2" />
    <div class="hero__orb hero__orb--3" />

    <!-- Floating botanical decorations -->
    <svg class="hero__deco hero__deco--tl" viewBox="0 0 140 140" fill="none">
      <path d="M20 120 Q35 70 70 45 Q45 75 55 120" stroke="rgba(134,239,172,0.18)" stroke-width="2" fill="rgba(134,239,172,0.06)"/>
      <path d="M8 95 Q28 55 58 38 Q35 60 40 95" stroke="rgba(134,239,172,0.12)" stroke-width="1.5" fill="rgba(134,239,172,0.04)"/>
      <circle cx="65" cy="30" r="3" fill="rgba(74,222,128,0.15)"/>
      <circle cx="45" cy="50" r="2" fill="rgba(74,222,128,0.1)"/>
    </svg>
    <svg class="hero__deco hero__deco--tr" viewBox="0 0 130 130" fill="none">
      <path d="M110 115 Q95 65 60 42 Q85 70 75 115" stroke="rgba(134,239,172,0.18)" stroke-width="2" fill="rgba(134,239,172,0.06)"/>
      <circle cx="95" cy="28" r="4" fill="rgba(74,222,128,0.14)"/>
      <circle cx="112" cy="55" r="2.5" fill="rgba(74,222,128,0.12)"/>
      <circle cx="80" cy="45" r="1.8" fill="rgba(74,222,128,0.1)"/>
    </svg>
    <svg class="hero__deco hero__deco--ml" viewBox="0 0 60 100" fill="none">
      <path d="M5 90 Q15 50 35 20 Q18 55 12 90" stroke="rgba(134,239,172,0.1)" stroke-width="1.5" fill="rgba(134,239,172,0.03)"/>
      <circle cx="30" cy="15" r="2" fill="rgba(74,222,128,0.1)"/>
    </svg>
    <svg class="hero__deco hero__deco--mr" viewBox="0 0 60 100" fill="none">
      <path d="M55 90 Q45 50 25 20 Q42 55 48 90" stroke="rgba(134,239,172,0.1)" stroke-width="1.5" fill="rgba(134,239,172,0.03)"/>
      <circle cx="28" cy="18" r="2" fill="rgba(74,222,128,0.1)"/>
    </svg>
    <svg class="hero__deco hero__deco--bl" viewBox="0 0 100 100" fill="none">
      <path d="M12 12 Q25 38 50 55 Q28 40 18 12" stroke="rgba(134,239,172,0.14)" stroke-width="1.5" fill="rgba(134,239,172,0.05)"/>
      <circle cx="42" cy="30" r="2.5" fill="rgba(74,222,128,0.12)"/>
    </svg>
    <svg class="hero__deco hero__deco--br" viewBox="0 0 110 110" fill="none">
      <path d="M98 12 Q80 40 58 58 Q75 42 92 12" stroke="rgba(134,239,172,0.14)" stroke-width="1.5" fill="rgba(134,239,172,0.05)"/>
      <circle cx="75" cy="22" r="3" fill="rgba(74,222,128,0.12)"/>
      <circle cx="60" cy="40" r="1.5" fill="rgba(74,222,128,0.08)"/>
    </svg>

    <!-- Large monstera leaf silhouette - left -->
    <svg class="hero__leaf hero__leaf--monstera-l" viewBox="0 0 200 260" fill="none">
      <path d="M100 250 C100 250 30 200 25 130 C20 60 60 10 100 5 C140 10 180 60 175 130 C170 200 100 250 100 250Z" fill="rgba(34,197,94,0.15)" stroke="rgba(74,222,128,0.3)" stroke-width="1.5"/>
      <path d="M100 250 L100 40" stroke="rgba(74,222,128,0.25)" stroke-width="2"/>
      <path d="M100 80 C80 60 50 55 35 70" stroke="rgba(74,222,128,0.2)" stroke-width="1.2" fill="none"/>
      <path d="M100 80 C120 60 150 55 165 70" stroke="rgba(74,222,128,0.2)" stroke-width="1.2" fill="none"/>
      <path d="M100 130 C75 115 45 115 30 135" stroke="rgba(74,222,128,0.2)" stroke-width="1.2" fill="none"/>
      <path d="M100 130 C125 115 155 115 170 135" stroke="rgba(74,222,128,0.2)" stroke-width="1.2" fill="none"/>
      <path d="M100 180 C80 165 55 170 40 190" stroke="rgba(74,222,128,0.2)" stroke-width="1.2" fill="none"/>
      <path d="M100 180 C120 165 145 170 160 190" stroke="rgba(74,222,128,0.2)" stroke-width="1.2" fill="none"/>
      <!-- Fenestrations (holes) -->
      <ellipse cx="65" cy="100" rx="12" ry="18" fill="rgba(5,46,22,0.7)" stroke="rgba(74,222,128,0.15)" stroke-width="0.8"/>
      <ellipse cx="135" cy="100" rx="12" ry="18" fill="rgba(5,46,22,0.7)" stroke="rgba(74,222,128,0.15)" stroke-width="0.8"/>
      <ellipse cx="60" cy="155" rx="10" ry="15" fill="rgba(5,46,22,0.7)" stroke="rgba(74,222,128,0.15)" stroke-width="0.8"/>
      <ellipse cx="140" cy="155" rx="10" ry="15" fill="rgba(5,46,22,0.7)" stroke="rgba(74,222,128,0.15)" stroke-width="0.8"/>
    </svg>

    <!-- Fern frond - right -->
    <svg class="hero__leaf hero__leaf--fern-r" viewBox="0 0 120 300" fill="none">
      <path d="M60 290 Q55 200 60 100 Q62 50 58 10" stroke="rgba(110,231,183,0.3)" stroke-width="2" fill="none"/>
      <g stroke="rgba(110,231,183,0.22)" stroke-width="1" fill="rgba(110,231,183,0.08)">
        <path d="M60 260 Q35 250 15 260 Q35 245 60 250"/>
        <path d="M60 260 Q85 250 105 260 Q85 245 60 250"/>
        <path d="M60 230 Q30 218 8 230 Q30 212 60 220"/>
        <path d="M60 230 Q90 218 112 230 Q90 212 60 220"/>
        <path d="M60 200 Q28 188 5 200 Q28 182 60 190"/>
        <path d="M60 200 Q92 188 115 200 Q92 182 60 190"/>
        <path d="M60 170 Q32 158 12 170 Q32 155 60 162"/>
        <path d="M60 170 Q88 158 108 170 Q88 155 60 162"/>
        <path d="M60 140 Q38 130 22 140 Q38 126 60 134"/>
        <path d="M60 140 Q82 130 98 140 Q82 126 60 134"/>
        <path d="M60 110 Q42 102 32 110 Q42 98 60 106"/>
        <path d="M60 110 Q78 102 88 110 Q78 98 60 106"/>
        <path d="M60 80 Q48 74 40 80 Q48 72 60 78"/>
        <path d="M60 80 Q72 74 80 80 Q72 72 60 78"/>
      </g>
    </svg>

    <!-- Pilea / Chinese money plant - top right -->
    <svg class="hero__leaf hero__leaf--pilea" viewBox="0 0 160 160" fill="none">
      <path d="M80 150 L80 70" stroke="rgba(134,239,172,0.25)" stroke-width="1.5"/>
      <circle cx="80" cy="55" r="20" fill="rgba(74,222,128,0.12)" stroke="rgba(74,222,128,0.25)" stroke-width="1"/>
      <circle cx="50" cy="85" r="16" fill="rgba(74,222,128,0.1)" stroke="rgba(74,222,128,0.2)" stroke-width="1"/>
      <circle cx="110" cy="85" r="16" fill="rgba(74,222,128,0.1)" stroke="rgba(74,222,128,0.2)" stroke-width="1"/>
      <circle cx="60" cy="115" r="13" fill="rgba(74,222,128,0.1)" stroke="rgba(74,222,128,0.2)" stroke-width="1"/>
      <circle cx="100" cy="115" r="13" fill="rgba(74,222,128,0.1)" stroke="rgba(74,222,128,0.2)" stroke-width="1"/>
      <!-- petioles -->
      <line x1="80" y1="70" x2="80" y2="55" stroke="rgba(134,239,172,0.2)" stroke-width="1"/>
      <line x1="80" y1="90" x2="50" y2="85" stroke="rgba(134,239,172,0.2)" stroke-width="1"/>
      <line x1="80" y1="90" x2="110" y2="85" stroke="rgba(134,239,172,0.2)" stroke-width="1"/>
      <line x1="80" y1="110" x2="60" y2="115" stroke="rgba(134,239,172,0.2)" stroke-width="1"/>
      <line x1="80" y1="110" x2="100" y2="115" stroke="rgba(134,239,172,0.2)" stroke-width="1"/>
    </svg>

    <!-- Succulent rosette - bottom left -->
    <svg class="hero__leaf hero__leaf--succulent" viewBox="0 0 140 140" fill="none">
      <g transform="translate(70,70)">
        <ellipse cx="0" cy="-28" rx="10" ry="18" fill="rgba(134,239,172,0.12)" stroke="rgba(74,222,128,0.22)" stroke-width="0.8" transform="rotate(0)"/>
        <ellipse cx="0" cy="-28" rx="10" ry="18" fill="rgba(134,239,172,0.12)" stroke="rgba(74,222,128,0.22)" stroke-width="0.8" transform="rotate(45)"/>
        <ellipse cx="0" cy="-28" rx="10" ry="18" fill="rgba(134,239,172,0.12)" stroke="rgba(74,222,128,0.22)" stroke-width="0.8" transform="rotate(90)"/>
        <ellipse cx="0" cy="-28" rx="10" ry="18" fill="rgba(134,239,172,0.12)" stroke="rgba(74,222,128,0.22)" stroke-width="0.8" transform="rotate(135)"/>
        <ellipse cx="0" cy="-28" rx="10" ry="18" fill="rgba(134,239,172,0.12)" stroke="rgba(74,222,128,0.22)" stroke-width="0.8" transform="rotate(180)"/>
        <ellipse cx="0" cy="-28" rx="10" ry="18" fill="rgba(134,239,172,0.12)" stroke="rgba(74,222,128,0.22)" stroke-width="0.8" transform="rotate(225)"/>
        <ellipse cx="0" cy="-28" rx="10" ry="18" fill="rgba(134,239,172,0.12)" stroke="rgba(74,222,128,0.22)" stroke-width="0.8" transform="rotate(270)"/>
        <ellipse cx="0" cy="-28" rx="10" ry="18" fill="rgba(134,239,172,0.12)" stroke="rgba(74,222,128,0.22)" stroke-width="0.8" transform="rotate(315)"/>
        <ellipse cx="0" cy="-16" rx="7" ry="12" fill="rgba(134,239,172,0.15)" stroke="rgba(74,222,128,0.25)" stroke-width="0.7" transform="rotate(22)"/>
        <ellipse cx="0" cy="-16" rx="7" ry="12" fill="rgba(134,239,172,0.15)" stroke="rgba(74,222,128,0.25)" stroke-width="0.7" transform="rotate(67)"/>
        <ellipse cx="0" cy="-16" rx="7" ry="12" fill="rgba(134,239,172,0.15)" stroke="rgba(74,222,128,0.25)" stroke-width="0.7" transform="rotate(112)"/>
        <ellipse cx="0" cy="-16" rx="7" ry="12" fill="rgba(134,239,172,0.15)" stroke="rgba(74,222,128,0.25)" stroke-width="0.7" transform="rotate(157)"/>
        <ellipse cx="0" cy="-16" rx="7" ry="12" fill="rgba(134,239,172,0.15)" stroke="rgba(74,222,128,0.25)" stroke-width="0.7" transform="rotate(202)"/>
        <ellipse cx="0" cy="-16" rx="7" ry="12" fill="rgba(134,239,172,0.15)" stroke="rgba(74,222,128,0.25)" stroke-width="0.7" transform="rotate(247)"/>
        <ellipse cx="0" cy="-16" rx="7" ry="12" fill="rgba(134,239,172,0.15)" stroke="rgba(74,222,128,0.25)" stroke-width="0.7" transform="rotate(292)"/>
        <ellipse cx="0" cy="-16" rx="7" ry="12" fill="rgba(134,239,172,0.15)" stroke="rgba(74,222,128,0.25)" stroke-width="0.7" transform="rotate(337)"/>
        <circle cx="0" cy="0" r="5" fill="rgba(74,222,128,0.2)"/>
      </g>
    </svg>

    <!-- Trailing vine - left edge -->
    <svg class="hero__leaf hero__leaf--vine-l" viewBox="0 0 50 300" fill="none">
      <path d="M25 0 Q10 50 20 100 Q35 150 15 200 Q5 250 25 300" stroke="rgba(110,231,183,0.25)" stroke-width="1.5" fill="none"/>
      <g fill="rgba(74,222,128,0.18)">
        <ellipse cx="14" cy="60" rx="8" ry="5" transform="rotate(-30 14 60)"/>
        <ellipse cx="28" cy="110" rx="7" ry="4.5" transform="rotate(25 28 110)"/>
        <ellipse cx="10" cy="165" rx="8" ry="5" transform="rotate(-20 10 165)"/>
        <ellipse cx="22" cy="220" rx="7" ry="4.5" transform="rotate(30 22 220)"/>
        <ellipse cx="12" cy="270" rx="6" ry="4" transform="rotate(-25 12 270)"/>
      </g>
    </svg>

    <!-- Trailing vine - right edge -->
    <svg class="hero__leaf hero__leaf--vine-r" viewBox="0 0 50 300" fill="none">
      <path d="M25 0 Q40 50 30 100 Q15 150 35 200 Q45 250 25 300" stroke="rgba(110,231,183,0.25)" stroke-width="1.5" fill="none"/>
      <g fill="rgba(74,222,128,0.18)">
        <ellipse cx="36" cy="60" rx="8" ry="5" transform="rotate(30 36 60)"/>
        <ellipse cx="22" cy="110" rx="7" ry="4.5" transform="rotate(-25 22 110)"/>
        <ellipse cx="40" cy="165" rx="8" ry="5" transform="rotate(20 40 165)"/>
        <ellipse cx="28" cy="220" rx="7" ry="4.5" transform="rotate(-30 28 220)"/>
        <ellipse cx="38" cy="270" rx="6" ry="4" transform="rotate(25 38 270)"/>
      </g>
    </svg>

    <!-- Floating sparkle dots -->
    <div class="hero__sparkles">
      <span v-for="n in 18" :key="n" class="hero__sparkle" :style="{
        left: `${8 + (n * 7.5) % 85}%`,
        top: `${10 + (n * 13) % 75}%`,
        animationDelay: `${n * 0.7}s`,
        animationDuration: `${3 + (n % 3)}s`
      }" />
    </div>

    <div class="hero__layout">
      <div
        class="hero__content"
        :class="{ 'hero__content--en': locale !== 'zh-CN' }"
        :style="{
          transform: `translate(${mouseX * 0.3}px, ${mouseY * 0.3}px)`,
        }"
      >
        <div class="hero__eyebrow">
          <span class="hero__eyebrow-dot" />
          <span>{{ locale === 'zh-CN' ? '绿色伙伴的养护中枢' : 'A living care hub' }}</span>
        </div>

        <h1 class="hero__title">
          <span
            v-for="(word, i) in titleWords"
            :key="i"
            class="hero__word"
            :class="{ 'hero__word--visible': revealedWords[i] }"
          >
            {{ word }}
          </span>
        </h1>

        <p
          class="hero__subtitle"
          :class="{ 'hero__subtitle--visible': subtitleVisible }"
        >
          {{ $t('hero.subtitle') }}
        </p>

        <div
          class="hero__cta"
          :class="{ 'hero__cta--visible': ctaVisible }"
        >
          <router-link to="/encyclopedia" class="hero__button">
            <span>{{ $t('hero.cta') }}</span>
            <svg class="hero__button-leaf" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c1.5 0 3-.3 4.3-.9C10 18 6 14 6 12c0-4.4 3.6-8 8-8 .7 0 1.4.1 2 .2C15 3.5 13.5 2 12 2z" />
              <path d="M18 4c-2 2-3 5-3 8s1 6 3 8" />
            </svg>
          </router-link>
        </div>

        <div class="hero__badges" :class="{ 'hero__badges--visible': ctaVisible }">
          <span v-for="badge in heroBadges" :key="badge" class="hero__badge">
            {{ badge }}
          </span>
        </div>
      </div>

    </div>

    <div class="hero__showcase" aria-label="Plant gallery">
      <div class="hero__showcase-glow" />
      <div class="hero__showcase-row hero__showcase-row--top">
        <div class="hero__showcase-track">
          <article
            v-for="(plant, index) in [...showcaseTop, ...showcaseTop]"
            :key="`top-${plant.name}-${index}`"
            class="hero__plant-tile"
          >
            <img :src="plant.image" :alt="plant.name" loading="lazy" decoding="async" fetchpriority="low" />
          </article>
        </div>
      </div>
      <div class="hero__showcase-row hero__showcase-row--bottom">
        <div class="hero__showcase-track">
          <article
            v-for="(plant, index) in [...showcaseBottom, ...showcaseBottom]"
            :key="`bottom-${plant.name}-${index}`"
            class="hero__plant-tile"
          >
            <img :src="plant.image" :alt="plant.name" loading="lazy" decoding="async" fetchpriority="low" />
          </article>
        </div>
      </div>
    </div>

    <div class="hero__scroll-hint">
      <div class="hero__scroll-line" />
      <span>{{ $t('hero.scrollHint') }}</span>
    </div>

    <!-- Wave bottom edge -->
    <div class="hero__wave">
      <svg viewBox="0 0 1440 120" preserveAspectRatio="none">
        <path d="M0,60 C360,120 720,0 1080,60 C1260,90 1380,80 1440,60 L1440,120 L0,120 Z" fill="#fafaf8"/>
      </svg>
    </div>
  </section>
</template>

<style scoped lang="scss">
.hero {
  position: relative;
  min-height: 100dvh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  overflow: hidden;
  padding-top: clamp(6.5rem, 12vh, 8.5rem);
  background:
    radial-gradient(ellipse 52% 42% at 72% 48%, rgba(187, 247, 208, 0.24), transparent 58%),
    radial-gradient(ellipse 40% 28% at 18% 78%, rgba(21, 128, 61, 0.28), transparent 58%),
    linear-gradient(135deg, #042713 0%, #0d3f22 42%, #166534 100%);

  &__texture {
    position: absolute;
    inset: 0;
    background-image: radial-gradient(rgba(134, 239, 172, 0.04) 1px, transparent 1px);
    background-size: 24px 24px;
    pointer-events: none;
    z-index: 0;
  }

  &__aurora {
    position: absolute;
    inset: 0;
    background:
      radial-gradient(ellipse 60% 40% at 20% 30%, rgba(34, 197, 94, 0.06) 0%, transparent 60%),
      radial-gradient(ellipse 50% 50% at 80% 60%, rgba(74, 222, 128, 0.05) 0%, transparent 55%),
      radial-gradient(ellipse 70% 35% at 50% 80%, rgba(16, 185, 129, 0.04) 0%, transparent 50%);
    pointer-events: none;
    z-index: 0;
    animation: auroraShift 15s ease-in-out infinite alternate;
  }

  &__orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(50px);
    pointer-events: none;
    z-index: 0;

    &--1 {
      width: 350px;
      height: 350px;
      background: rgba(74, 222, 128, 0.08);
      top: 10%;
      left: -5%;
      animation: orbFloat 10s ease-in-out infinite;
    }

    &--2 {
      width: 250px;
      height: 250px;
      background: rgba(34, 197, 94, 0.06);
      bottom: 20%;
      right: -3%;
      animation: orbFloat 10s ease-in-out infinite -3s;
    }

    &--3 {
      width: 180px;
      height: 180px;
      background: rgba(134, 239, 172, 0.07);
      top: 60%;
      left: 30%;
      animation: orbFloat 10s ease-in-out infinite -6s;
    }
  }

  &__particles {
    position: absolute;
    inset: 0;
    pointer-events: none;
    z-index: 1;
  }

  &__falling-leaves {
    position: absolute;
    inset: 0;
    pointer-events: none;
    z-index: 2;
  }

  &__deco {
    position: absolute;
    pointer-events: none;
    z-index: 1;
    animation: floatDeco 6s ease-in-out infinite;

    &--tl { top: 4%; left: 2%; width: 130px; height: 130px; animation-delay: 0s; }
    &--tr { top: 6%; right: 3%; width: 120px; height: 120px; animation-delay: -2s; }
    &--ml { top: 35%; left: 1%; width: 55px; height: 90px; animation-delay: -3s; }
    &--mr { top: 30%; right: 1%; width: 55px; height: 90px; animation-delay: -1s; }
    &--bl { bottom: 18%; left: 6%; width: 90px; height: 90px; animation-delay: -4s; }
    &--br { bottom: 15%; right: 8%; width: 100px; height: 100px; animation-delay: -1.5s; }
  }

  &__sparkles {
    position: absolute;
    inset: 0;
    pointer-events: none;
    z-index: 1;
  }

  &__sparkle {
    position: absolute;
    width: 3px;
    height: 3px;
    border-radius: 50%;
    background: rgba(134, 239, 172, 0.4);
    animation: sparkle 3s ease-in-out infinite;

    &::after {
      content: '';
      position: absolute;
      top: -1px;
      left: -1px;
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: rgba(134, 239, 172, 0.15);
    }
  }

  // ── Large leaf decorations ──────────────────────────────
  &__leaf {
    position: absolute;
    pointer-events: none;
    z-index: 1;
    opacity: 1;

    &--monstera-l {
      top: 5%;
      left: -2%;
      width: 220px;
      height: 280px;
      animation: floatDeco 8s ease-in-out infinite;
      transform-origin: bottom center;
    }

    &--fern-r {
      top: 2%;
      right: -1%;
      width: 100px;
      height: 260px;
      animation: floatDeco 9s ease-in-out infinite -2s;
      transform-origin: bottom center;
    }

    &--pilea {
      top: 8%;
      right: 12%;
      width: 120px;
      height: 120px;
      animation: floatDeco 7s ease-in-out infinite -4s;
    }

    &--succulent {
      bottom: 20%;
      left: 5%;
      width: 110px;
      height: 110px;
      animation: floatDeco 10s ease-in-out infinite -1s;
    }

    &--vine-l {
      top: 10%;
      left: 0;
      width: 40px;
      height: 260px;
      animation: floatDeco 12s ease-in-out infinite -3s;
    }

    &--vine-r {
      top: 5%;
      right: 0;
      width: 40px;
      height: 260px;
      animation: floatDeco 11s ease-in-out infinite -5s;
    }
  }

  &__layout {
    position: relative;
    z-index: 2;
    width: min(1180px, calc(100% - 3rem));
    padding: 0;
    display: flex;
    justify-content: center;
    text-align: center;
  }

  &__content {
    max-width: 760px;
    transition: transform 0.1s ease-out;

    &--en {
      position: relative;
      top: clamp(-2.4rem, -4vh, -1.4rem);

      .hero__title {
        flex-wrap: nowrap;
        gap: clamp(0.35rem, 0.85vw, 0.72rem);
        white-space: nowrap;
      }

      .hero__word {
        font-size: clamp(1.9rem, 4.05vw, 3.05rem);
      }

      .hero__badges {
        margin-top: 0.85rem;
      }
    }
  }

  &__eyebrow {
    display: inline-flex;
    align-items: center;
    gap: 0.55rem;
    padding: 0.48rem 0.85rem;
    margin-bottom: 1.15rem;
    border: 1px solid rgba(187, 247, 208, 0.24);
    border-radius: 999px;
    background: rgba(240, 253, 244, 0.08);
    color: rgba(240, 253, 244, 0.82);
    font-size: 0.86rem;
    font-weight: 650;
    backdrop-filter: blur(12px);
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
  }

  &__eyebrow-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: $color-leaf-400;
    box-shadow: 0 0 0 5px rgba(74, 222, 128, 0.12);
    animation: livePulse 2.4s ease-in-out infinite;
  }

  &__title {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 0.4em;
    margin-bottom: 1.5rem;
    text-wrap: balance;
  }

  &__word {
    display: inline-block;
    font-family: $font-display;
    font-size: clamp(2.5rem, 6vw, 4.5rem);
    font-weight: 700;
    color: #f0fdf4;
    text-shadow: 0 0 40px rgba(74, 222, 128, 0.2);
    transform: translateY(40px);
    opacity: 0;
    transition: all 0.6s $ease-out-expo;

    &--visible {
      transform: translateY(0);
      opacity: 1;
    }

    &:nth-child(3) {
      color: $color-leaf-400;
      text-shadow: 0 0 30px rgba(74, 222, 128, 0.3);
    }
  }

  &__subtitle {
    font-size: clamp(1rem, 2vw, 1.25rem);
    color: rgba(240, 253, 244, 0.78);
    max-width: 560px;
    line-height: 1.75;
    margin-bottom: 2rem;
    margin-left: auto;
    margin-right: auto;
    transform: translateY(20px);
    opacity: 0;
    transition: all 0.6s $ease-out-expo;

    &--visible {
      transform: translateY(0);
      opacity: 1;
    }
  }

  &__cta {
    transform: translateY(20px);
    opacity: 0;
    transition: all 0.6s $ease-out-expo;

    &--visible {
      transform: translateY(0);
      opacity: 1;
    }
  }

  &__badges {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 0.65rem;
    margin-top: 1.1rem;
    opacity: 0;
    transform: translateY(12px);
    transition: all 0.5s $ease-out-expo;

    &--visible {
      opacity: 1;
      transform: translateY(0);
    }
  }

  &__badge {
    padding: 0.42rem 0.78rem;
    border-radius: 999px;
    border: 1px solid rgba(187, 247, 208, 0.2);
    background: rgba(236, 253, 245, 0.08);
    color: rgba(240, 253, 244, 0.78);
    font-size: 0.8rem;
    font-weight: 650;
    backdrop-filter: blur(10px);
    transition: transform 0.22s ease, background-color 0.22s ease, color 0.22s ease;

    &:hover {
      transform: translateY(-2px);
      background: rgba(236, 253, 245, 0.14);
      color: #f0fdf4;
    }
  }

  &__button {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.875rem 2rem;
    background: linear-gradient(135deg, $color-leaf-600, $color-leaf-500);
    color: white;
    font-weight: 600;
    font-size: 1.05rem;
    border-radius: 50px;
    text-decoration: none;
    transition: all 0.3s $ease-spring;
    position: relative;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(22, 163, 74, 0.25);

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: rgba(255, 255, 255, 0.15);
      transform: translateX(-100%);
      transition: transform 0.4s ease;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 30px rgba(22, 163, 74, 0.4);

      &::before {
        transform: translateX(0);
      }
    }

    &:active {
      transform: translateY(0) scale(0.98);
    }
  }

  &__button-leaf {
    width: 20px;
    height: 20px;
    transition: transform 0.3s ease;

    .hero__button:hover & {
      transform: rotate(15deg) scale(1.1);
    }
  }

  &__showcase {
    position: absolute;
    left: 0;
    right: 0;
    bottom: clamp(4.8rem, 8vh, 6.2rem);
    z-index: 3;
    display: grid;
    gap: 1rem;
    padding: 0.35rem 0;
    overflow: hidden;
    pointer-events: auto;
    mask-image: linear-gradient(90deg, transparent 0, black 9%, black 91%, transparent 100%);
  }

  &__showcase-glow {
    position: absolute;
    inset: 12% 0 auto;
    height: 120px;
    background:
      radial-gradient(ellipse at 30% 50%, rgba(187, 247, 208, 0.18), transparent 55%),
      radial-gradient(ellipse at 75% 45%, rgba(74, 222, 128, 0.16), transparent 52%);
    filter: blur(24px);
    pointer-events: none;
  }

  &__showcase-row {
    position: relative;
    display: flex;
    width: max-content;
    min-width: 100%;

    &--top .hero__showcase-track {
      animation: plantMarqueeRight 76s linear infinite;
    }

    &--bottom .hero__showcase-track {
      animation: plantMarqueeLeft 82s linear infinite;
    }

    &:hover .hero__showcase-track {
      animation-play-state: paused;
    }
  }

  &__showcase-track {
    display: flex;
    gap: 1rem;
    min-width: max-content;
    will-change: transform;
  }

  &__plant-tile {
    position: relative;
    width: clamp(150px, 13vw, 210px);
    height: clamp(92px, 8vw, 122px);
    flex: 0 0 auto;
    overflow: hidden;
    border: 1px solid rgba(220, 252, 231, 0.16);
    border-radius: 1.35rem;
    background:
      linear-gradient(180deg, rgba(255, 255, 255, 0.16), rgba(240, 253, 244, 0.06));
    box-shadow:
      0 18px 40px rgba(0, 0, 0, 0.18),
      inset 0 1px 0 rgba(255, 255, 255, 0.12);
    backdrop-filter: blur(12px);
    transition: transform 0.28s ease, border-color 0.28s ease, box-shadow 0.28s ease;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
      opacity: 0.58;
      filter: saturate(1.02) contrast(1.02);
      transition: transform 0.4s ease, opacity 0.28s ease, filter 0.28s ease;
    }

    &::after {
      content: '';
      position: absolute;
      inset: 0;
      background:
        linear-gradient(180deg, rgba(4, 39, 19, 0.08), rgba(4, 39, 19, 0.28)),
        radial-gradient(circle at 20% 18%, rgba(187, 247, 208, 0.12), transparent 38%);
      pointer-events: none;
    }

    &:hover {
      transform: translateY(-4px) scale(1.02);
      border-color: rgba(187, 247, 208, 0.42);
      box-shadow:
        0 24px 50px rgba(0, 0, 0, 0.24),
        0 0 0 1px rgba(187, 247, 208, 0.08),
        inset 0 1px 0 rgba(255, 255, 255, 0.18);

      img {
        opacity: 0.78;
        filter: saturate(1.16) contrast(1.04);
        transform: scale(1.08);
      }
    }
  }

  &__scroll-hint {
    position: absolute;
    bottom: 3.5rem;
    left: 50%;
    transform: translateX(-50%);
    z-index: 3;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    color: rgba(240, 253, 244, 0.5);
    font-size: 0.75rem;
    letter-spacing: 0.1em;
    text-transform: uppercase;
  }

  &__scroll-line {
    width: 1px;
    height: 40px;
    background: linear-gradient(to bottom, transparent, rgba(240, 253, 244, 0.5));
    animation: scrollPulse 2s ease-in-out infinite;
  }

  &__wave {
    position: absolute;
    bottom: -1px;
    left: 0;
    width: 100%;
    z-index: 3;
    line-height: 0;

    svg {
      width: 100%;
      height: 80px;
    }
  }
}

@keyframes scrollPulse {
  0%, 100% { opacity: 0.3; transform: scaleY(0.6); }
  50% { opacity: 1; transform: scaleY(1); }
}

@keyframes floatDeco {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  33% { transform: translateY(-10px) rotate(3deg); }
  66% { transform: translateY(5px) rotate(-2deg); }
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.6; transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.1); }
}

@keyframes livePulse {
  0%, 100% { box-shadow: 0 0 0 5px rgba(74, 222, 128, 0.12); }
  50% { box-shadow: 0 0 0 8px rgba(74, 222, 128, 0.04); }
}

@keyframes plantMarqueeRight {
  from { transform: translateX(-50%); }
  to { transform: translateX(0); }
}

@keyframes plantMarqueeLeft {
  from { transform: translateX(0); }
  to { transform: translateX(-50%); }
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0); }
  25% { transform: translate(15px, -10px); }
  50% { transform: translate(-5px, 15px); }
  75% { transform: translate(-15px, -5px); }
}

@keyframes sparkle {
  0%, 100% { opacity: 0; transform: scale(0.5); }
  50% { opacity: 1; transform: scale(1.2); }
}

@keyframes auroraShift {
  0% { opacity: 0.7; transform: scale(1) translate(0, 0); }
  50% { opacity: 1; transform: scale(1.05) translate(2%, -1%); }
  100% { opacity: 0.8; transform: scale(0.98) translate(-1%, 1%); }
}

@media (max-width: 768px) {
  .hero {
    &__layout {
      text-align: center;
      width: min(100% - 2rem, 560px);
    }

    &__content {
      max-width: none;
    }

    &__title,
    &__badges {
      justify-content: center;
    }

    &__subtitle {
      max-width: none;
    }

    &__showcase {
      bottom: 4.8rem;
      gap: 0.75rem;
      mask-image: linear-gradient(90deg, transparent 0, black 5%, black 95%, transparent 100%);
    }

    &__showcase-track {
      gap: 0.75rem;
    }

    &__plant-tile {
      width: 142px;
      height: 88px;
      border-radius: 1rem;
    }

    &__deco {
      opacity: 0.4;
      &--tl, &--tr { width: 80px; height: 80px; }
      &--ml, &--mr { display: none; }
      &--bl, &--br { width: 60px; height: 60px; }
    }

    &__leaf {
      opacity: 0.5;
      &--monstera-l { width: 140px; height: 180px; left: -5%; }
      &--fern-r { width: 60px; height: 160px; }
      &--pilea { width: 80px; height: 80px; right: 5%; }
      &--succulent { width: 70px; height: 70px; }
      &--vine-l, &--vine-r { display: none; }
    }

    &__orb { filter: blur(40px); }
    &__sparkles { display: none; }
    &__aurora { display: none; }
  }
}

</style>
