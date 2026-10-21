package com.plantcultivation.util;

import com.plantcultivation.security.AuthPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具。
 * <p>JwtAuthenticationFilter 校验 JWT 后把 {@link AuthPrincipal} 放入 SecurityContext，
 * 控制器通过此工具统一获取，替代手工解析 Authorization 头。</p>
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /**
     * 当前登录用户（含 user_id 与 account），未登录返回 {@code null}。
     */
    public static AuthPrincipal currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal principal) {
            return principal;
        }
        return null;
    }

    /**
     * 当前登录用户主键，未登录返回 {@code null}。业务表外键统一使用该值。
     */
    public static Long currentUserId() {
        AuthPrincipal principal = currentUser();
        return principal == null ? null : principal.id();
    }

    /**
     * 当前登录账号，未登录返回 {@code null}。
     */
    public static String currentAccount() {
        AuthPrincipal principal = currentUser();
        return principal == null ? null : principal.account();
    }
}
