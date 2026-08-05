export type PreparedUploadImage = {
  file: File
  compressed: boolean
}

type PrepareOptions = {
  locale: string
  maxBytes?: number
  maxDimension?: number
  targetDimension?: number
  targetBytes?: number
}

const SUPPORTED_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/gif'])

function text(locale: string, zh: string, en: string) {
  return locale === 'zh-CN' ? zh : en
}

function loadImage(file: File) {
  return new Promise<HTMLImageElement>((resolve, reject) => {
    const url = URL.createObjectURL(file)
    const img = new Image()
    img.onload = () => {
      URL.revokeObjectURL(url)
      resolve(img)
    }
    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('invalid image'))
    }
    img.src = url
  })
}

function canvasToBlob(canvas: HTMLCanvasElement, quality: number) {
  return new Promise<Blob>((resolve, reject) => {
    canvas.toBlob(
      (blob) => blob ? resolve(blob) : reject(new Error('compress failed')),
      'image/jpeg',
      quality
    )
  })
}

function jpgName(name: string) {
  return name.replace(/\.[^.]+$/, '') + '.jpg'
}

export async function prepareImageForUpload(file: File, options: PrepareOptions): Promise<PreparedUploadImage> {
  const maxBytes = options.maxBytes ?? 10 * 1024 * 1024
  const maxDimension = options.maxDimension ?? 8000
  const targetDimension = options.targetDimension ?? 1800
  const targetBytes = options.targetBytes ?? 1.6 * 1024 * 1024

  if (!file.type.startsWith('image/') || !SUPPORTED_TYPES.has(file.type)) {
    throw new Error(text(options.locale, '只支持 JPG、PNG、WEBP、GIF 图片', 'Only JPG, PNG, WEBP, and GIF images are supported.'))
  }
  if (file.size > maxBytes) {
    throw new Error(text(options.locale, '图片不能超过 10MB', 'Image size cannot exceed 10MB.'))
  }
  if (file.type === 'image/gif') {
    return { file, compressed: false }
  }

  const image = await loadImage(file)
  if (image.naturalWidth > maxDimension || image.naturalHeight > maxDimension) {
    throw new Error(text(options.locale, `图片单边最大 ${maxDimension}px`, `Image dimensions cannot exceed ${maxDimension}px per side.`))
  }
  if (file.size <= 900 * 1024 && image.naturalWidth <= targetDimension && image.naturalHeight <= targetDimension) {
    return { file, compressed: false }
  }

  const scale = Math.min(targetDimension / image.naturalWidth, targetDimension / image.naturalHeight, 1)
  const width = Math.max(1, Math.round(image.naturalWidth * scale))
  const height = Math.max(1, Math.round(image.naturalHeight * scale))
  const canvas = document.createElement('canvas')
  canvas.width = width
  canvas.height = height

  const ctx = canvas.getContext('2d')
  if (!ctx) {
    throw new Error(text(options.locale, '图片处理失败，请换一张图片', 'Image processing failed. Please choose another image.'))
  }
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, width, height)
  ctx.drawImage(image, 0, 0, width, height)

  let blob = await canvasToBlob(canvas, 0.86)
  if (blob.size > targetBytes) blob = await canvasToBlob(canvas, 0.78)
  if (blob.size > targetBytes) blob = await canvasToBlob(canvas, 0.68)

  return {
    file: new File([blob], jpgName(file.name), { type: 'image/jpeg', lastModified: Date.now() }),
    compressed: true,
  }
}
