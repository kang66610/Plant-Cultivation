package com.plantcultivation.util;

import com.plantcultivation.entity.LoginAttempt;
import com.plantcultivation.mapper.LoginAttemptMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginRateLimiterTest {

    private LoginAttemptMapper mapper;
    private LoginRateLimiter limiter;

    @BeforeEach
    void setUp() {
        mapper = mock(LoginAttemptMapper.class);
        limiter = new LoginRateLimiter(mapper);
    }

    @Test
    void 窗口内失败未达上限时允许() {
        when(mapper.countSince(eq("account:test"), any(LocalDateTime.class)))
                .thenReturn(LoginRateLimiter.MAX_FAILURES - 1);

        assertTrue(limiter.isAllowed("account:test"));
    }

    @Test
    void 达到上限后拒绝() {
        when(mapper.countSince(eq("account:test"), any(LocalDateTime.class)))
                .thenReturn(LoginRateLimiter.MAX_FAILURES);

        assertFalse(limiter.isAllowed("account:test"));
    }

    @Test
    void 记录失败写入当前时间() {
        limiter.recordFailure("ip:1.2.3.4");

        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(mapper).insert(captor.capture());
        assertEquals("ip:1.2.3.4", captor.getValue().getRateKey());
        assertTrue(captor.getValue().getAttemptedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void 成功登录后清空该维度() {
        limiter.clear("account:test");

        verify(mapper).deleteByKey("account:test");
    }

    @Test
    void 清理只删除保留期之前的记录() {
        limiter.purgeExpired();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(mapper).deleteOlderThan(captor.capture());
        assertTrue(captor.getValue().isBefore(LocalDateTime.now().minusHours(23)));
    }
}
