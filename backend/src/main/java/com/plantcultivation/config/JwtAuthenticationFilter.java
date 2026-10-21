package com.plantcultivation.config;

import com.plantcultivation.entity.User;
import com.plantcultivation.mapper.UserMapper;
import com.plantcultivation.security.AuthPrincipal;
import com.plantcultivation.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                authenticate(jwtUtil.parseToken(header.substring(7)));
            } catch (Exception ignored) {
                // 签名错误 / 已过期 / 数据库暂不可用：不建立认证，受保护接口由 Security 返回 401
            }
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(JwtUtil.TokenClaims claims) {
        if (claims == null || claims.userId() == null || claims.account() == null) {
            return;
        }
        User user = userMapper.selectById(claims.userId());
        if (user == null || !user.getAccount().equals(claims.account())) {
            return;
        }
        // 服务端失效核心：登出/改密后 token_version 自增，旧令牌版本不匹配即视为未登录
        int currentVersion = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
        if (currentVersion != claims.tokenVersion()) {
            return;
        }
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        new AuthPrincipal(user.getId(), user.getAccount()), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
