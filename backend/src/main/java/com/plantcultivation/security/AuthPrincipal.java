package com.plantcultivation.security;

/**
 * 已认证用户在请求上下文中的身份。
 * <p>JWT 只携带 userId + account + 令牌版本；过滤器校验通过后把本对象放入
 * SecurityContext，控制器通过 {@code SecurityUtil} 取用，避免每次请求再用账号反查用户。</p>
 *
 * @param id      用户主键（所有业务表的外键）
 * @param account 登录账号（仅用于展示 / 兼容按账号查询的接口）
 */
public record AuthPrincipal(Long id, String account) {
}
