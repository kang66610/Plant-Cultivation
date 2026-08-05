package com.plantcultivation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.entity.PlantDiary;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.service.PlantDiaryService;
import com.plantcultivation.util.SecurityUtil;
import com.plantcultivation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class PlantDiaryController {

    private final PlantDiaryService diaryService;

    @GetMapping("/my")
    public ResultVO<Page<PlantDiary>> myDiaries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);
        String account = requireLogin();
        return ResultVO.success(diaryService.listMyDiaries(account, page, size));
    }

    @GetMapping("/{id}")
    public ResultVO<PlantDiary> getDiary(@PathVariable Long id) {
        String account = requireLogin();
        PlantDiary diary = diaryService.getDiary(id, account);
        if (diary == null) {
            return ResultVO.error(404, "日记不存在");
        }
        return ResultVO.success(diary);
    }

    @PostMapping
    public ResultVO<PlantDiary> createDiary(@RequestBody PlantDiary diary) {
        String account = requireLogin();
        diary.setUserAccount(account);
        return ResultVO.success(diaryService.createDiary(diary));
    }

    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteDiary(@PathVariable Long id) {
        String account = requireLogin();
        try {
            diaryService.deleteDiary(id, account);
        } catch (BusinessException e) {
            return ResultVO.error(e.getStatus(), e.getMessage());
        }
        return ResultVO.success();
    }

    private String requireLogin() {
        String account = SecurityUtil.currentAccount();
        if (account == null) {
            throw new BusinessException("请先登录", 401);
        }
        return account;
    }
}
