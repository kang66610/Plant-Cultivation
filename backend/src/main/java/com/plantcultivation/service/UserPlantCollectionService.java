package com.plantcultivation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.dto.UpdateCollectionRequest;
import com.plantcultivation.entity.Plant;
import com.plantcultivation.entity.UserPlantCollection;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.mapper.PlantMapper;
import com.plantcultivation.mapper.UserPlantCollectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPlantCollectionService {

    private final UserPlantCollectionMapper collectionMapper;
    private final PlantMapper plantMapper;

    /** 收藏默认浇水间隔（植物数据缺失时兜底） */
    private static final int DEFAULT_WATER_INTERVAL_DAYS = 7;

    public Page<UserPlantCollection> listMine(Long userId, int page, int size) {
        Page<UserPlantCollection> pageObj = new Page<>(page, size);
        QueryWrapper<UserPlantCollection> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("created_at");
        Page<UserPlantCollection> result = collectionMapper.selectPage(pageObj, qw);

        List<Long> plantIds = result.getRecords().stream()
                .map(UserPlantCollection::getPlantId)
                .distinct()
                .collect(Collectors.toList());
        if (!plantIds.isEmpty()) {
            Map<Long, Plant> plantMap = plantMapper.selectByIds(plantIds).stream()
                    .collect(Collectors.toMap(Plant::getId, p -> p));
            for (UserPlantCollection c : result.getRecords()) {
                Plant plant = plantMap.get(c.getPlantId());
                if (plant != null) {
                    c.setPlantName(plant.getCommonName());
                    c.setPlantImage(plant.getImageUrl());
                    c.setPlantSlug(plant.getSlug());
                }
            }
        }
        return result;
    }

    /**
     * 收藏（幂等：已收藏时直接返回 false 表示"已存在"）。
     *
     * @return true 本次新建收藏；false 原本已收藏
     */
    @Transactional
    public boolean addCollect(Long userId, Long plantId) {
        QueryWrapper<UserPlantCollection> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("plant_id", plantId);
        if (collectionMapper.selectOne(qw) != null) {
            return false;
        }
        UserPlantCollection collection = new UserPlantCollection();
        collection.setUserId(userId);
        collection.setPlantId(plantId);
        // 默认浇水间隔取植物的最小浇水间隔，无数据则兜底 7 天
        Plant plant = plantMapper.selectById(plantId);
        if (plant != null && plant.getWaterIntervalDaysMin() != null) {
            collection.setWaterIntervalDays(plant.getWaterIntervalDaysMin());
        } else {
            collection.setWaterIntervalDays(DEFAULT_WATER_INTERVAL_DAYS);
        }
        collectionMapper.insert(collection);
        return true;
    }

    /**
     * 取消收藏（幂等：未收藏时静默成功，不会反向新增）。
     */
    @Transactional
    public void removeCollect(Long userId, Long plantId) {
        QueryWrapper<UserPlantCollection> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("plant_id", plantId);
        collectionMapper.delete(qw);
    }

    @Transactional
    public UserPlantCollection updateCollect(Long userId, Long plantId, UpdateCollectionRequest request) {
        QueryWrapper<UserPlantCollection> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("plant_id", plantId);
        UserPlantCollection collection = collectionMapper.selectOne(qw);
        if (collection == null) {
            throw new BusinessException("尚未收藏该植物", 404);
        }
        if (request.nickname() != null) {
            collection.setNickname(request.nickname());
        }
        if (request.location() != null) {
            collection.setLocation(request.location());
        }
        if (request.notes() != null) {
            collection.setNotes(request.notes());
        }
        if (request.waterIntervalDays() != null) {
            collection.setWaterIntervalDays(request.waterIntervalDays());
            LocalDateTime base = collection.getLastWateredAt() != null
                    ? collection.getLastWateredAt()
                    : LocalDateTime.now();
            collection.setNextWaterAt(base.plusDays(request.waterIntervalDays()));
        }
        collectionMapper.updateById(collection);
        enrichCollection(collection);
        return collection;
    }

    /** 标记浇水：刷新 last_watered_at 并按间隔推算 next_water_at。 */
    @Transactional
    public void markWatered(Long userId, Long plantId) {
        QueryWrapper<UserPlantCollection> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("plant_id", plantId);
        UserPlantCollection collection = collectionMapper.selectOne(qw);
        if (collection == null) {
            throw new BusinessException("尚未收藏该植物", 404);
        }
        LocalDateTime now = LocalDateTime.now();
        collection.setLastWateredAt(now);
        int days = collection.getWaterIntervalDays() != null
                ? collection.getWaterIntervalDays() : DEFAULT_WATER_INTERVAL_DAYS;
        collection.setNextWaterAt(now.plusDays(days));
        collectionMapper.updateById(collection);
    }

    private void enrichCollection(UserPlantCollection collection) {
        Plant plant = plantMapper.selectById(collection.getPlantId());
        if (plant != null) {
            collection.setPlantName(plant.getCommonName());
            collection.setPlantImage(plant.getImageUrl());
            collection.setPlantSlug(plant.getSlug());
        }
    }
}
