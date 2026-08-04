package com.plantcultivation.controller;

import com.plantcultivation.entity.User;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.service.AuthService;
import com.plantcultivation.util.LoginRateLimiter;
import com.plantcultivation.util.SecurityUtil;
import com.plantcultivation.vo.ResultVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;

    @PostMapping("/register")
    public ResultVO<User> register(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String account = body.get("account");
        String password = body.get("password");
        if (username == null || account == null || password == null) {
            return ResultVO.error(400, "参数不完整");
        }
        // 注册按 IP 限流，防止批量灌库
        String ipKey = "ip:" + clientIp(request);
        if (!rateLimiter.isAllowed(ipKey)) {
            throw new BusinessException("操作过于频繁，请10分钟后再试", 429);
        }
        try {
            return ResultVO.success(authService.register(username, account, password));
        } catch (BusinessException e) {
            rateLimiter.recordFailure(ipKey);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResultVO<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String account = body.get("account");
        String password = body.get("password");
        if (account == null || password == null) {
            return ResultVO.error(400, "参数不完整");
        }
        // 账号 + IP 双维度限流，防暴力破解
        String accountKey = "account:" + account;
        String ipKey = "ip:" + clientIp(request);
        if (!rateLimiter.isAllowed(accountKey) || !rateLimiter.isAllowed(ipKey)) {
            throw new BusinessException("尝试次数过多，请10分钟后再试", 429);
        }
        try {
            Map<String, Object> result = authService.login(account, password);
            rateLimiter.clear(accountKey);
            return ResultVO.success(result);
        } catch (BusinessException e) {
            rateLimiter.recordFailure(accountKey);
            rateLimiter.recordFailure(ipKey);
            throw e;
        }
    }

    /**
     * 取客户端 IP：仅当请求来自本机/内网代理（Nginx 反代）时才信任 X-Forwarded-For，
     * 防止直连 8080 时伪造该头绕过限流。
     */
    private String clientIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        boolean fromLocalProxy = remote != null
                && (remote.startsWith("127.")
                || remote.equals("0:0:0:0:0:0:0:1")
                || remote.startsWith("::ffff:127.")
                || remote.startsWith("192.168.")
                || remote.startsWith("10."));
        if (fromLocalProxy) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return remote;
    }

    @GetMapping("/me")
    public ResultVO<User> getCurrentUser() {
        String account = SecurityUtil.currentAccount();
        if (account == null) {
            throw new BusinessException("未登录", 401);
        }
        User user = authService.getUserByAccount(account);
        user.setPassword(null);
        return ResultVO.success(user);
    }

    @PutMapping("/profile")
    public ResultVO<User> updateProfile(@RequestBody Map<String, String> body) {
        String account = SecurityUtil.currentAccount();
        if (account == null) {
            throw new BusinessException("未登录", 401);
        }
        User user = authService.updateProfile(account, body.get("username"), body.get("bio"), body.get("avatarUrl"));
        return ResultVO.success(user);
    }

    @PutMapping("/password")
    public ResultVO<Void> changePassword(@RequestBody Map<String, String> body) {
        String account = SecurityUtil.currentAccount();
        if (account == null) {
            throw new BusinessException("未登录", 401);
        }
        authService.changePassword(account, body.get("oldPassword"), body.get("newPassword"));
        return ResultVO.success();
    }
}
