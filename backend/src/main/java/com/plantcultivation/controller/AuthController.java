package com.plantcultivation.controller;

import com.plantcultivation.dto.ChangePasswordRequest;
import com.plantcultivation.dto.LoginRequest;
import com.plantcultivation.dto.ProfileUpdateRequest;
import com.plantcultivation.dto.RegisterRequest;
import com.plantcultivation.entity.User;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.service.AuthService;
import com.plantcultivation.util.LoginRateLimiter;
import com.plantcultivation.util.SecurityUtil;
import com.plantcultivation.vo.ResultVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public ResultVO<User> register(@Valid @RequestBody RegisterRequest body, HttpServletRequest request) {
        String ipKey = "ip:" + clientIp(request);
        if (!rateLimiter.isAllowed(ipKey)) {
            throw new BusinessException("操作过于频繁，请10分钟后再试", 429);
        }
        try {
            return ResultVO.success(authService.register(body.username(), body.account(), body.password()));
        } catch (BusinessException e) {
            rateLimiter.recordFailure(ipKey);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResultVO<Map<String, Object>> login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        String accountKey = "account:" + body.account();
        String ipKey = "ip:" + clientIp(request);
        if (!rateLimiter.isAllowed(accountKey) || !rateLimiter.isAllowed(ipKey)) {
            throw new BusinessException("尝试次数过多，请10分钟后再试", 429);
        }
        try {
            Map<String, Object> result = authService.login(body.account(), body.password());
            rateLimiter.clear(accountKey);
            rateLimiter.clear(ipKey);
            return ResultVO.success(result);
        } catch (BusinessException e) {
            rateLimiter.recordFailure(accountKey);
            rateLimiter.recordFailure(ipKey);
            throw e;
        }
    }

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
    public ResultVO<User> updateProfile(@Valid @RequestBody ProfileUpdateRequest body) {
        String account = SecurityUtil.currentAccount();
        if (account == null) {
            throw new BusinessException("未登录", 401);
        }
        User user = authService.updateProfile(account, body.username(), body.bio(), body.avatarUrl());
        return ResultVO.success(user);
    }

    @PutMapping("/password")
    public ResultVO<Void> changePassword(@Valid @RequestBody ChangePasswordRequest body) {
        String account = SecurityUtil.currentAccount();
        if (account == null) {
            throw new BusinessException("未登录", 401);
        }
        authService.changePassword(account, body.oldPassword(), body.newPassword());
        return ResultVO.success();
    }
}
