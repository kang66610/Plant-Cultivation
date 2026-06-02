package com.plantcultivation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.entity.PlantDiary;
import com.plantcultivation.entity.User;
import com.plantcultivation.service.AuthService;
import com.plantcultivation.service.PlantDiaryService;
import com.plantcultivation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class PlantDiaryController {

    private final PlantDiaryService diaryService;
    private final AuthService authService;

    @GetMapping("/my")
    public ResultVO<Page<PlantDiary>> myDiaries(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User user = authService.getUserByToken(auth.substring(7));
            if (user == null) return ResultVO.error(401, "请先登录");
            return ResultVO.success(diaryService.listMyDiaries(user.getAccount(), page, size));
        } catch (Exception e) {
            return ResultVO.error(401, "请先登录");
        }
    }

    @GetMapping("/{id}")
    public ResultVO<PlantDiary> getDiary(@PathVariable Long id) {
        PlantDiary diary = diaryService.getDiary(id);
        if (diary == null) return ResultVO.error(404, "日记不存在");
        return ResultVO.success(diary);
    }

    @PostMapping
    public ResultVO<PlantDiary> createDiary(@RequestHeader("Authorization") String auth,
                                             @RequestBody PlantDiary diary) {
        try {
            User user = authService.getUserByToken(auth.substring(7));
            if (user == null) return ResultVO.error(401, "请先登录");
            diary.setUserAccount(user.getAccount());
            return ResultVO.success(diaryService.createDiary(diary));
        } catch (Exception e) {
            return ResultVO.error(401, "请先登录");
        }
    }

    @DeleteMapping("/{id}")
    public ResultVO<Void> deleteDiary(@RequestHeader("Authorization") String auth,
                                       @PathVariable Long id) {
        try {
            User user = authService.getUserByToken(auth.substring(7));
            diaryService.deleteDiary(id, user.getAccount());
            return ResultVO.success();
        } catch (RuntimeException e) {
            if ("UNAUTHORIZED".equals(e.getMessage())) {
                return ResultVO.error(403, "无权删除");
            }
            return ResultVO.error(500, "删除失败");
        } catch (Exception e) {
            return ResultVO.error(401, "请先登录");
        }
    }
}
