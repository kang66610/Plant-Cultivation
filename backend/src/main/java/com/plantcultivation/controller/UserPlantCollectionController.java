package com.plantcultivation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.entity.User;
import com.plantcultivation.entity.UserPlantCollection;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.service.AuthService;
import com.plantcultivation.service.UserPlantCollectionService;
import com.plantcultivation.util.SecurityUtil;
import com.plantcultivation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class UserPlantCollectionController {

    private final UserPlantCollectionService collectionService;
    private final AuthService authService;

    /** 我的收藏列表（带植物名称/图片/浇水提醒） */
    @GetMapping
    public ResultVO<Page<UserPlantCollection>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);
        return ResultVO.success(collectionService.listMine(requireUser().getId(), page, size));
    }

    /** 收藏（幂等） */
    @PostMapping("/{plantId}")
    public ResultVO<Map<String, Boolean>> collect(@PathVariable Long plantId) {
        boolean added = collectionService.addCollect(requireUser().getId(), plantId);
        return ResultVO.success(Map.of("collected", added));
    }

    /** 取消收藏（幂等，未收藏时不会反向新增） */
    @DeleteMapping("/{plantId}")
    public ResultVO<Void> uncollect(@PathVariable Long plantId) {
        collectionService.removeCollect(requireUser().getId(), plantId);
        return ResultVO.success();
    }

    /** 标记浇水：重置 last_watered_at 并推算下次浇水时间 */
    @PostMapping("/{plantId}/water")
    public ResultVO<Void> markWatered(@PathVariable Long plantId) {
        collectionService.markWatered(requireUser().getId(), plantId);
        return ResultVO.success();
    }

    private User requireUser() {
        String account = SecurityUtil.currentAccount();
        if (account == null) {
            throw new BusinessException("请先登录", 401);
        }
        return authService.getUserByAccount(account);
    }
}
