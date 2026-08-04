package com.plantcultivation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.plantcultivation.entity.User;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.mapper.UserMapper;
import com.plantcultivation.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User register(String username, String account, String password) {
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码长度不能少于6位", 400);
        }
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("account", account);
        if (userMapper.selectOne(qw) != null) {
            throw new BusinessException("账号已存在", 409);
        }
        User user = new User();
        user.setUsername(username);
        user.setAccount(account);
        user.setPassword(passwordEncoder.encode(password));
        try {
            userMapper.insert(user);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发注册同一账号：唯一键兜底，返回 409 而非 500
            throw new BusinessException("账号已存在", 409);
        }
        user.setPassword(null);
        return user;
    }

    public Map<String, Object> login(String account, String password) {
        User user = getUserByAccount(account);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误", 401);
        }
        String token = jwtUtil.generateToken(account);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        user.setPassword(null);
        result.put("user", user);
        return result;
    }

    /**
     * 按账号查询用户，不存在抛业务异常。
     */
    public User getUserByAccount(String account) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("account", account);
        User user = userMapper.selectOne(qw);
        if (user == null) {
            throw new BusinessException("账号不存在", 404);
        }
        return user;
    }

    public User updateProfile(String account, String username, String bio, String avatarUrl) {
        User user = getUserByAccount(account);
        if (username != null && !username.isBlank()) {
            user.setUsername(username);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        userMapper.updateById(user);
        user.setPassword(null);
        return user;
    }

    public void changePassword(String account, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位", 400);
        }
        User user = getUserByAccount(account);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误", 401);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
