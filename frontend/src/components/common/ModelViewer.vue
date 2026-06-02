<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  modelUrl?: string
  autoRotate?: boolean
  height?: string
}>()

const canvasRef = ref<HTMLCanvasElement>()
let animationId: number
let rotation = 0

onMounted(() => {
  if (!canvasRef.value) return

  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')!
  canvas.width = 300
  canvas.height = 300

  function drawPlant() {
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    const cx = canvas.width / 2
    const cy = canvas.height / 2

    ctx.save()
    ctx.translate(cx, cy)
    ctx.rotate(rotation * Math.PI / 180)

    // Draw pot
    ctx.fillStyle = '#a0703c'
    ctx.beginPath()
    ctx.moveTo(-40, 60)
    ctx.lineTo(-30, 100)
    ctx.lineTo(30, 100)
    ctx.lineTo(40, 60)
    ctx.closePath()
    ctx.fill()

    // Draw soil
    ctx.fillStyle = '#553a20'
    ctx.beginPath()
    ctx.ellipse(0, 60, 40, 8, 0, 0, Math.PI * 2)
    ctx.fill()

    // Draw stem
    ctx.strokeStyle = '#166534'
    ctx.lineWidth = 4
    ctx.beginPath()
    ctx.moveTo(0, 55)
    ctx.quadraticCurveTo(0, 0, 0, -20)
    ctx.stroke()

    // Draw leaves
    const leaves = [
      { angle: -30, length: 60, curve: 20 },
      { angle: 30, length: 55, curve: -18 },
      { angle: -60, length: 45, curve: 15 },
      { angle: 60, length: 50, curve: -12 },
      { angle: 0, length: 65, curve: 0 },
    ]

    leaves.forEach((leaf) => {
      ctx.save()
      ctx.translate(0, -10)
      ctx.rotate((leaf.angle * Math.PI) / 180)

      ctx.fillStyle = '#22c55e'
      ctx.beginPath()
      ctx.moveTo(0, 0)
      ctx.quadraticCurveTo(leaf.curve, -leaf.length / 2, 0, -leaf.length)
      ctx.quadraticCurveTo(-leaf.curve, -leaf.length / 2, 0, 0)
      ctx.fill()

      // Leaf vein
      ctx.strokeStyle = '#15803d'
      ctx.lineWidth = 1
      ctx.beginPath()
      ctx.moveTo(0, 0)
      ctx.lineTo(0, -leaf.length)
      ctx.stroke()

      ctx.restore()
    })

    ctx.restore()

    if (props.autoRotate !== false) {
      rotation += 0.3
    }

    animationId = requestAnimationFrame(drawPlant)
  }

  drawPlant()
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
})
</script>

<template>
  <div class="model-viewer" :style="{ height: height || '300px' }">
    <canvas ref="canvasRef" class="model-viewer__canvas" />
  </div>
</template>

<style scoped lang="scss">
.model-viewer {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(ellipse at center, $color-leaf-50 0%, transparent 70%);
  border-radius: 16px;
  overflow: hidden;

  &__canvas {
    max-width: 100%;
    max-height: 100%;
  }
}
</style>
