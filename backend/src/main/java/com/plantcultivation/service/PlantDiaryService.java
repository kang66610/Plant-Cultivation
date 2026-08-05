package com.plantcultivation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.entity.PlantDiary;
import com.plantcultivation.entity.User;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.mapper.PlantDiaryMapper;
import com.plantcultivation.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlantDiaryService {

    private final PlantDiaryMapper diaryMapper;
    private final UserMapper userMapper;

    public Page<PlantDiary> listMyDiaries(String account, int page, int size) {
        Page<PlantDiary> pageObj = new Page<>(page, size);
        QueryWrapper<PlantDiary> qw = new QueryWrapper<>();
        qw.eq("user_account", account).orderByDesc("created_at");
        Page<PlantDiary> result = diaryMapper.selectPage(pageObj, qw);
        enrichDiaries(result.getRecords());
        return result;
    }

    public PlantDiary getDiary(Long id, String account) {
        QueryWrapper<PlantDiary> qw = new QueryWrapper<>();
        qw.eq("id", id).eq("user_account", account);
        PlantDiary diary = diaryMapper.selectOne(qw);
        if (diary != null) {
            enrichDiary(diary);
        }
        return diary;
    }

    @Transactional
    public PlantDiary createDiary(PlantDiary diary) {
        diaryMapper.insert(diary);
        enrichDiary(diary);
        return diary;
    }

    @Transactional
    public PlantDiary updateDiary(Long id, String account, PlantDiary changes) {
        PlantDiary diary = getDiary(id, account);
        if (diary == null) {
            throw new BusinessException("日记不存在", 404);
        }
        diary.setTitle(changes.getTitle());
        diary.setContent(changes.getContent());
        diary.setPlantSlug(changes.getPlantSlug());
        diary.setPlantName(changes.getPlantName());
        diary.setImages(changes.getImages());
        diary.setWeather(changes.getWeather());
        diary.setMood(changes.getMood());
        diary.setHeightCm(changes.getHeightCm());
        diary.setLeafCount(changes.getLeafCount());
        diary.setGrowthStage(changes.getGrowthStage());
        diaryMapper.updateById(diary);
        enrichDiary(diary);
        return diary;
    }

    @Transactional
    public void deleteDiary(Long id, String account) {
        PlantDiary diary = diaryMapper.selectById(id);
        if (diary == null || !diary.getUserAccount().equals(account)) {
            throw new BusinessException("无权删除", 403);
        }
        diaryMapper.deleteById(id);
    }

    private void enrichDiaries(List<PlantDiary> diaries) {
        if (diaries.isEmpty()) return;

        // 批量查询所有日记作者的用户信息（N+1 → 1次查询）
        List<String> accounts = diaries.stream()
                .map(PlantDiary::getUserAccount)
                .distinct()
                .collect(Collectors.toList());
        Map<String, User> userMap = userMapper.selectByAccounts(accounts).stream()
                .collect(Collectors.toMap(User::getAccount, u -> u));

        for (PlantDiary d : diaries) {
            User user = userMap.get(d.getUserAccount());
            if (user != null) {
                d.setUsername(user.getUsername());
                d.setAvatarUrl(user.getAvatarUrl());
            }
        }
    }

    private void enrichDiary(PlantDiary diary) {
        List<User> users = userMapper.selectByAccounts(List.of(diary.getUserAccount()));
        if (!users.isEmpty()) {
            diary.setUsername(users.get(0).getUsername());
            diary.setAvatarUrl(users.get(0).getAvatarUrl());
        }
    }
}
