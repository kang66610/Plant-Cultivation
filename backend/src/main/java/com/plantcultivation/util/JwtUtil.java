package com.plantcultivation.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    /** 用户主键声明：所有业务表统一用 user_id 做外键。 */
    private static final String CLAIM_USER_ID = "uid";

    /** 令牌版本声明：与 user.token_version 比对，用于服务端失效。 */
    private static final String CLAIM_TOKEN_VERSION = "ver";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(Long userId, String account, int tokenVersion) {
        Date now = new Date();
        return Jwts.builder()
                .subject(account)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_TOKEN_VERSION, tokenVersion)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验签并解析令牌。
     *
     * @return 身份声明；升级前签发、缺少 uid/ver 的旧令牌返回 {@code null}，调用方按未登录处理
     * @throws io.jsonwebtoken.JwtException 签名错误或已过期
     */
    public TokenClaims parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object rawId = claims.get(CLAIM_USER_ID);
        Object rawVersion = claims.get(CLAIM_TOKEN_VERSION);
        // Jackson 反序列化数字可能是 Integer 或 Long，统一按 Number 处理
        if (!(rawId instanceof Number id) || !(rawVersion instanceof Number version)) {
            return null;
        }
        return new TokenClaims(id.longValue(), claims.getSubject(), version.intValue());
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** JWT 中的身份声明。 */
    public record TokenClaims(Long userId, String account, int tokenVersion) {
    }
}
