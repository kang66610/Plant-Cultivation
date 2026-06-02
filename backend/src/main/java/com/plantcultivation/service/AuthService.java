package com.plantcultivation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.plantcultivation.entity.User;
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
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("account", account);
        if (userMapper.selectOne(qw) != null) {
            throw new RuntimeException("ACCOUNT_EXISTS");
        }
        User user = new User();
        user.setUsername(username);
        user.setAccount(account);
        user.setPassword(passwordEncoder.encode(password));
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    public Map<String, Object> login(String account, String password) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("account", account);
        User user = userMapper.selectOne(qw);
        if (user == null) {
            throw new RuntimeException("ACCOUNT_NOT_FOUND");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("WRONG_PASSWORD");
        }
        String token = jwtUtil.generateToken(account);
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        user.setPassword(null);
        result.put("user", user);
        return result;
    }

    public User getUserByToken(String token) {
        String account = jwtUtil.parseToken(token);
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("account", account);
        User user = userMapper.selectOne(qw);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    public User updateProfile(String account, String username, String bio, String avatarUrl) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("account", account);
        User user = userMapper.selectOne(qw);
        if (user == null) {
            throw new RuntimeException("USER_NOT_FOUND");
        }
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
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("account", account);
        User user = userMapper.selectOne(qw);
        if (user == null) {
            throw new RuntimeException("USER_NOT_FOUND");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("WRONG_PASSWORD");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
