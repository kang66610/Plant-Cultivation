package com.plantcultivation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.plantcultivation.entity.PlantDiary;
import com.plantcultivation.entity.User;
import com.plantcultivation.mapper.PlantDiaryMapper;
import com.plantcultivation.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlantDiaryServiceTest {

    private PlantDiaryMapper diaryMapper;
    private UserMapper userMapper;
    private PlantDiaryService service;

    @BeforeEach
    void setUp() {
        diaryMapper = mock(PlantDiaryMapper.class);
        userMapper = mock(UserMapper.class);
        service = new PlantDiaryService(diaryMapper, userMapper);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void getDiaryFiltersByIdAndUserId() {
        PlantDiary diary = new PlantDiary();
        diary.setId(10L);
        diary.setUserId(7L);
        when(diaryMapper.selectOne(any(QueryWrapper.class))).thenReturn(diary);

        User user = new User();
        user.setId(7L);
        user.setAccount("alice");
        user.setUsername("Alice");
        user.setAvatarUrl("/uploads/a.jpg");
        when(userMapper.selectById(7L)).thenReturn(user);

        PlantDiary result = service.getDiary(10L, 7L);

        assertNotNull(result);
        assertEquals("Alice", result.getUsername());

        ArgumentCaptor<QueryWrapper<PlantDiary>> captor =
                ArgumentCaptor.forClass((Class<QueryWrapper<PlantDiary>>) (Class<?>) QueryWrapper.class);
        verify(diaryMapper).selectOne(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertTrue(sql.contains("id"));
        assertTrue(sql.contains("user_id"));
    }
}
