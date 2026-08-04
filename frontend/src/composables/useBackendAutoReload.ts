/**
 * 开发模式辅助：后端重启完成后自动刷新页面。
 *
 * 场景：修改后端 Java 代码 → 重启 bootRun → 页面数据还是旧的，需手动刷新。
 * 本逻辑每 3 秒探测后端健康接口：
 *   - 连续多次探测失败（后端重启中）→ 标记"不可用"
 *   - 从"不可用"恢复到"可用" → 自动刷新页面，拿到新代码的数据
 *
 * 仅在 import.meta.env.DEV 下启用；用原生 fetch 避免经过 axios 拦截器。
 */
export function useBackendAutoReload() {
  if (!import.meta.env.DEV) return

  const PROBE_URL = '/api/plants?page=1&size=1'
  const PROBE_INTERVAL = 3000
  const FAIL_THRESHOLD = 3 // 连续失败 3 次（约 9 秒）才认为后端不可用，避免瞬时抖动误判

  let consecutiveFailures = 0
  let backendDown = false

  setInterval(async () => {
    try {
      const res = await fetch(PROBE_URL, {
        headers: { Accept: 'application/json' },
        signal: AbortSignal.timeout(2500),
      })
      if (!res.ok) {
        throw new Error(`probe failed: ${res.status}`)
      }
      // 后端恢复可用：若此前处于"不可用"状态，刷新页面
      if (backendDown) {
        backendDown = false
        consecutiveFailures = 0
        // 延迟 500ms 确保后端完全就绪（连接池预热等）
        setTimeout(() => window.location.reload(), 500)
      } else {
        consecutiveFailures = 0
      }
    } catch {
      consecutiveFailures++
      if (consecutiveFailures >= FAIL_THRESHOLD) {
        backendDown = true
      }
    }
  }, PROBE_INTERVAL)
}
