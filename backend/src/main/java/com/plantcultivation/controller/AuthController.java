package com.plantcultivation.controller;

import com.plantcultivation.entity.User;
import com.plantcultivation.service.AuthService;
import com.plantcultivation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResultVO<User> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String account = body.get("account");
        String password = body.get("password");
        if (username == null || account == null || password == null) {
            return ResultVO.error(400, "参数不完整");
        }
        try {
            User user = authService.register(username, account, password);
            return ResultVO.success(user);
        } catch (RuntimeException e) {
            if ("ACCOUNT_EXISTS".equals(e.getMessage())) {
                return ResultVO.error(409, "账号已存在");
            }
            return ResultVO.error(500, "注册失败");
        }
    }

    @PostMapping("/login")
    public ResultVO<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String account = body.get("account");
        String password = body.get("password");
        if (account == null || password == null) {
            return ResultVO.error(400, "参数不完整");
        }
        try {
            Map<String, Object> result = authService.login(account, password);
            return ResultVO.success(result);
        } catch (RuntimeException e) {
            switch (e.getMessage()) {
                case "ACCOUNT_NOT_FOUND":
                    return ResultVO.error(404, "账号不存在");
                case "WRONG_PASSWORD":
                    return ResultVO.error(401, "密码错误");
                default:
                    return ResultVO.error(500, "登录失败");
            }
        }
    }

    @GetMapping("/me")
    public ResultVO<User> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResultVO.error(401, "未登录");
        }
        try {
            User user = authService.getUserByToken(auth.substring(7));
            if (user == null) {
                return ResultVO.error(404, "用户不存在");
            }
            return ResultVO.success(user);
        } catch (Exception e) {
            return ResultVO.error(401, "登录已过期");
        }
    }

    @PutMapping("/profile")
    public ResultVO<User> updateProfile(@RequestHeader("Authorization") String auth,
                                         @RequestBody Map<String, String> body) {
        try {
            String account = authService.getUserByToken(auth.substring(7)).getAccount();
            User user = authService.updateProfile(account, body.get("username"), body.get("bio"), body.get("avatarUrl"));
            return ResultVO.success(user);
        } catch (Exception e) {
            return ResultVO.error(401, "操作失败");
        }
    }

    @PutMapping("/password")
    public ResultVO<Void> changePassword(@RequestHeader("Authorization") String auth,
                                          @RequestBody Map<String, String> body) {
        try {
            String account = authService.getUserByToken(auth.substring(7)).getAccount();
            authService.changePassword(account, body.get("oldPassword"), body.get("newPassword"));
            return ResultVO.success();
        } catch (RuntimeException e) {
            if ("WRONG_PASSWORD".equals(e.getMessage())) {
                return ResultVO.error(401, "原密码错误");
            }
            return ResultVO.error(500, "修改失败");
        }
    }
}
