package com.plantcultivation.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRateLimiterTest {

    @Test
    void 前五次失败允许第六次拒绝() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        String key = "account:test";
        for (int i = 0; i < LoginRateLimiter.MAX_FAILURES; i++) {
            assertTrue(limiter.isAllowed(key), "第 " + (i + 1) + " 次应允许");
            limiter.recordFailure(key);
        }
        assertFalse(limiter.isAllowed(key));
    }

    @Test
    void 成功登录后清零() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        limiter.recordFailure("account:test");
        limiter.recordFailure("account:test");
        limiter.clear("account:test");
        assertTrue(limiter.isAllowed("account:test"));
    }

    @Test
    void 不同key互不影响() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        for (int i = 0; i < LoginRateLimiter.MAX_FAILURES; i++) {
            limiter.recordFailure("account:a");
        }
        assertFalse(limiter.isAllowed("account:a"));
        assertTrue(limiter.isAllowed("account:b"));
        assertTrue(limiter.isAllowed("ip:1.2.3.4"));
    }
}
