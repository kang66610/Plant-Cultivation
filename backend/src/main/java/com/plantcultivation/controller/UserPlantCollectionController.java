package com.plantcultivation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.dto.UpdateCollectionRequest;
import com.plantcultivation.entity.User;
import com.plantcultivation.entity.UserPlantCollection;
import com.plantcultivation.exception.BusinessException;
import com.plantcultivation.service.AuthService;
import com.plantcultivation.service.UserPlantCollectionService;
import com.plantcultivation.util.SecurityUtil;
import com.plantcultivation.vo.PageResultVO;
import com.plantcultivation.vo.ResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class UserPlantCollectionController {

    private final UserPlantCollectionService collectionService;
    private final AuthService authService;

    @GetMapping
    public ResultVO<PageResultVO<UserPlantCollection>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 100);
        Page<UserPlantCollection> result = collectionService.listMine(requireUser().getId(), page, size);
        return ResultVO.success(PageResultVO.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()));
    }

    @PostMapping("/{plantId}")
    public ResultVO<Map<String, Boolean>> collect(@PathVariable Long plantId) {
        boolean added = collectionService.addCollect(requireUser().getId(), plantId);
        return ResultVO.success(Map.of("collected", added));
    }

    @PutMapping("/{plantId}")
    public ResultVO<UserPlantCollection> update(@PathVariable Long plantId,
                                                @Valid @RequestBody UpdateCollectionRequest body) {
        User user = requireUser();
        return ResultVO.success(collectionService.updateCollect(user.getId(), plantId, body));
    }

    @DeleteMapping("/{plantId}")
    public ResultVO<Void> uncollect(@PathVariable Long plantId) {
        collectionService.removeCollect(requireUser().getId(), plantId);
        return ResultVO.success();
    }

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
