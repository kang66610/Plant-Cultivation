package com.plantcultivation.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具。
 * <p>JwtAuthenticationFilter 解析 token 后会把账号（String principal）放入
 * SecurityContext，控制器通过此工具统一获取，替代手工解析 Authorization 头。</p>
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * 当前登录账号，未登录返回 {@code null}。
     */
    public static String currentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String account && !account.isBlank()) {
            return account;
        }
        return null;
    }
}
