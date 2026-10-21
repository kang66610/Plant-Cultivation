package com.plantcultivation.util;

import com.plantcultivation.entity.LoginAttempt;
import com.plantcultivation.mapper.LoginAttemptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 登录失败限流器（MySQL 持久化实现，支持多实例部署）。
 * <p>规则：同一 key（账号或 IP）在 10 分钟滑动窗口内失败达到 {@link #MAX_FAILURES} 次即拒绝；
 * 成功登录后 {@link #clear} 重置。记录落库，因此进程重启或多台服务器共享同一数据库时依然生效。</p>
 */
@Component
@RequiredArgsConstructor
public class LoginRateLimiter {

    public static final int MAX_FAILURES = 5;
    public static final Duration WINDOW = Duration.ofMinutes(10);

    /** 失败记录保留时长（远大于窗口，便于排查，同时控制表大小）。 */
    private static final Duration RETENTION = Duration.ofHours(24);

    private final LoginAttemptMapper attemptMapper;

    /**
     * @param key 限流维度，如 {@code account:xxx} 或 {@code ip:1.2.3.4}
     * @return true 允许继续尝试；false 已超限，应拒绝
     */
    public boolean isAllowed(String key) {
        LocalDateTime since = LocalDateTime.now().minus(WINDOW);
        return attemptMapper.countSince(key, since) < MAX_FAILURES;
    }

    /** 记录一次失败（仅窗口内的失败计数生效）。 */
    public void recordFailure(String key) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setRateKey(key);
        attempt.setAttemptedAt(LocalDateTime.now());
        attemptMapper.insert(attempt);
    }

    /** 成功登录后清零该 key 的失败记录。 */
    public void clear(String key) {
        attemptMapper.deleteByKey(key);
    }

    /** 每小时清理一天前的记录，避免表无限增长。 */
    @Scheduled(fixedDelay = 3_600_000L, initialDelay = 3_600_000L)
    public void purgeExpired() {
        attemptMapper.deleteOlderThan(LocalDateTime.now().minus(RETENTION));
    }
}
