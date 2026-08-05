package com.plantcultivation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.plantcultivation.entity.PlantDiary;
import com.plantcultivation.entity.User;
import com.plantcultivation.mapper.PlantDiaryMapper;
import com.plantcultivation.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

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
    void getDiaryFiltersByIdAndAccount() {
        PlantDiary diary = new PlantDiary();
        diary.setId(10L);
        diary.setUserAccount("alice");
        when(diaryMapper.selectOne(any(QueryWrapper.class))).thenReturn(diary);

        User user = new User();
        user.setAccount("alice");
        user.setUsername("Alice");
        user.setAvatarUrl("/uploads/a.jpg");
        when(userMapper.selectByAccounts(List.of("alice"))).thenReturn(List.of(user));

        PlantDiary result = service.getDiary(10L, "alice");

        assertNotNull(result);
        assertEquals("Alice", result.getUsername());

        ArgumentCaptor<QueryWrapper<PlantDiary>> captor =
                ArgumentCaptor.forClass((Class<QueryWrapper<PlantDiary>>) (Class<?>) QueryWrapper.class);
        verify(diaryMapper).selectOne(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertTrue(sql.contains("id"));
        assertTrue(sql.contains("user_account"));
    }
}
