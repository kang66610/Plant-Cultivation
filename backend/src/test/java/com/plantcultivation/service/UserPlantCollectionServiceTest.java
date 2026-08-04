package com.plantcultivation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.plantcultivation.entity.Plant;
import com.plantcultivation.entity.UserPlantCollection;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.mapper.PlantMapper;
import com.plantcultivation.mapper.UserPlantCollectionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserPlantCollectionServiceTest {

    private UserPlantCollectionMapper collectionMapper;
    private PlantMapper plantMapper;
    private UserPlantCollectionService service;

    @BeforeEach
    void setUp() {
        collectionMapper = mock(UserPlantCollectionMapper.class);
        plantMapper = mock(PlantMapper.class);
        service = new UserPlantCollectionService(collectionMapper, plantMapper);
    }

    @Test
    void 未收藏时addCollect返回true并插入默认间隔() {
        when(collectionMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        Plant plant = new Plant();
        plant.setId(10L);
        plant.setWaterIntervalDaysMin(3);
        when(plantMapper.selectById(10L)).thenReturn(plant);

        boolean collected = service.addCollect(1L, 10L);

        assertTrue(collected);
        ArgumentCaptor<UserPlantCollection> captor = ArgumentCaptor.forClass(UserPlantCollection.class);
        verify(collectionMapper).insert(captor.capture());
        UserPlantCollection inserted = captor.getValue();
        assertEquals(1L, inserted.getUserId());
        assertEquals(10L, inserted.getPlantId());
        assertEquals(3, inserted.getWaterIntervalDays());
    }

    @Test
    void 植物无间隔数据时使用默认7天() {
        when(collectionMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);
        when(plantMapper.selectById(10L)).thenReturn(new Plant());

        service.addCollect(1L, 10L);

        ArgumentCaptor<UserPlantCollection> captor = ArgumentCaptor.forClass(UserPlantCollection.class);
        verify(collectionMapper).insert(captor.capture());
        assertEquals(7, captor.getValue().getWaterIntervalDays());
    }

    @Test
    void 已收藏时addCollect返回false且不重复插入() {
        when(collectionMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new UserPlantCollection());

        boolean collected = service.addCollect(1L, 10L);

        assertFalse(collected);
        verify(collectionMapper, never()).insert(any(UserPlantCollection.class));
    }

    @Test
    void removeCollect未收藏时静默成功不新增() {
        when(collectionMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        service.removeCollect(1L, 10L);

        verify(collectionMapper).delete(any(QueryWrapper.class));
        verify(collectionMapper, never()).insert(any(UserPlantCollection.class));
    }

    @Test
    void markWatered未收藏时抛业务异常() {
        when(collectionMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        assertThrows(BusinessException.class, () -> service.markWatered(1L, 10L));
        verify(collectionMapper, never()).updateById(any(UserPlantCollection.class));
    }

    @Test
    void markWatered按间隔推算下次浇水时间() {
        UserPlantCollection collection = new UserPlantCollection();
        collection.setId(5L);
        collection.setWaterIntervalDays(7);
        when(collectionMapper.selectOne(any(QueryWrapper.class))).thenReturn(collection);

        service.markWatered(1L, 10L);

        verify(collectionMapper).updateById(collection);
        assertNotNull(collection.getLastWateredAt());
        assertNotNull(collection.getNextWaterAt());
        assertEquals(collection.getLastWateredAt().plusDays(7), collection.getNextWaterAt());
    }
}
