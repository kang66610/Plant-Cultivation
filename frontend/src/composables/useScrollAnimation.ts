import { onMounted, onUnmounted, type Ref } from 'vue'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

gsap.registerPlugin(ScrollTrigger)

interface ScrollAnimationOptions {
  y?: number
  opacity?: number
  duration?: number
  delay?: number
  ease?: string
  stagger?: number
  start?: string
}

export function useScrollAnimation(
  elementRef: Ref<HTMLElement | undefined>,
  options: ScrollAnimationOptions = {}
) {
  const {
    y = 60,
    opacity = 0,
    duration = 0.8,
    delay = 0,
    ease = 'power3.out',
    stagger = 0,
    start = 'top 85%',
  } = options

  let trigger: ScrollTrigger | null = null

  onMounted(() => {
    if (!elementRef.value) return

    const targets = stagger
      ? elementRef.value.children
      : elementRef.value

    gsap.set(targets, { y, opacity })

    trigger = ScrollTrigger.create({
      trigger: elementRef.value,
      start,
      onEnter: () => {
        gsap.to(targets, {
          y: 0,
          opacity: 1,
          duration,
          delay,
          ease,
          stagger,
        })
      },
      once: true,
    })
  })

  onUnmounted(() => {
    trigger?.kill()
  })
}

export function useParallax(speed: number = 0.3) {
  const elementRef = ref<HTMLElement>()

  onMounted(() => {
    if (!elementRef.value) return

    gsap.to(elementRef.value, {
      y: () => window.innerHeight * speed,
      ease: 'none',
      scrollTrigger: {
        trigger: elementRef.value,
        start: 'top bottom',
        end: 'bottom top',
        scrub: true,
      },
    })
  })

  return { elementRef }
}

import { ref } from 'vue'
