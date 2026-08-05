package com.plantcultivation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.dto.CreateDiaryRequest;
import com.plantcultivation.entity.PlantDiary;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.service.PlantDiaryService;
import com.plantcultivation.util.SecurityUtil;
import com.plantcultivation.vo.PageResultVO;
import com.plantcultivation.vo.ResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class PlantDiaryController {

    private final PlantDiaryService diaryService;

    @GetMapping("/my")
    public ResultVO<PageResultVO<PlantDiary>> myDiaries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);
        Page<PlantDiary> result = diaryService.listMyDiaries(requireLogin(), page, size);
        return ResultVO.success(PageResultVO.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()));
    }

    @GetMapping("/{id}")
    public ResultVO<PlantDiary> getDiary(@PathVariable Long id) {
        PlantDiary diary = diaryService.getDiary(id, requireLogin());
        if (diary == null) {
            return ResultVO.error(404, "日记不存在");
        }
        return ResultVO.success(diary);
    }

    @PostMapping
    public ResultVO<PlantDiary> createDiary(@Valid @RequestBody CreateDiaryRequest body) {
        PlantDiary diary = toEntity(body);
        diary.setUserAccount(requireLogin());
        return ResultVO.success(diaryService.createDiary(diary));
    }

    @PutMapping("/{id}")
    public ResultVO<PlantDiary> updateDiary(@PathVariable Long id,
                                            @Valid @RequestBody CreateDiaryRequest body) {
        return ResultVO.success(diaryService.updateDiary(id, requireLogin(), toEntity(body)));
    }

    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteDiary(@PathVariable Long id) {
        diaryService.deleteDiary(id, requireLogin());
        return ResultVO.success();
    }

    private String requireLogin() {
        String account = SecurityUtil.currentAccount();
        if (account == null) {
            throw new BusinessException("请先登录", 401);
        }
        return account;
    }

    private PlantDiary toEntity(CreateDiaryRequest body) {
        PlantDiary diary = new PlantDiary();
        diary.setTitle(body.title());
        diary.setContent(body.content());
        diary.setPlantSlug(blankToNull(body.plantSlug()));
        diary.setPlantName(body.plantName());
        diary.setImages(body.images());
        diary.setWeather(body.weather());
        diary.setMood(body.mood());
        diary.setHeightCm(body.heightCm());
        diary.setLeafCount(body.leafCount());
        diary.setGrowthStage(body.growthStage());
        return diary;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
