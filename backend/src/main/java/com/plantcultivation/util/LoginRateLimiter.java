package com.plantcultivation.util;

import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 登录失败限流器（内存实现，单实例部署适用）。
 * <p>规则：同一 key（账号或 IP）在 10 分钟窗口内失败超过 {@link #MAX_FAILURES} 次则拒绝；
 * 窗口随时间滑动，过期记录自动清理，成功登录后 {@link #clear} 重置。</p>
 */
@Component
public class LoginRateLimiter {

    static final int MAX_FAILURES = 5;
    static final long WINDOW_MS = 10 * 60 * 1000L;

    private final ConcurrentHashMap<String, Deque<Long>> failures = new ConcurrentHashMap<>();

    /**
     * @param key 限流维度，如 {@code account:xxx} 或 {@code ip:1.2.3.4}
     * @return true 允许继续尝试；false 已超限，应拒绝
     */
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        Deque<Long> deque = failures.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        deque.removeIf(t -> now - t > WINDOW_MS);
        return deque.size() < MAX_FAILURES;
    }

    /** 记录一次失败（仅窗口内的失败计数生效）。 */
    public void recordFailure(String key) {
        long now = System.currentTimeMillis();
        failures.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>()).addLast(now);
    }

    /** 成功登录后清零该 key 的失败记录。 */
    public void clear(String key) {
        failures.remove(key);
    }
}
