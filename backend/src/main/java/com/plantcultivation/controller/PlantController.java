package com.plantcultivation.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plantcultivation.entity.CareGuide;
import com.plantcultivation.entity.Plant;
import com.plantcultivation.service.PlantService;
import com.plantcultivation.vo.PageResultVO;
import com.plantcultivation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;

    @GetMapping
    public ResultVO<PageResultVO<Plant>> listPlants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String light,
            @RequestParam(required = false) String water,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Boolean indoor,
            @RequestParam(required = false) Boolean petSafe,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        page = clampPage(page);
        size = clampSize(size);
        Page<Plant> result = plantService.listPlants(search, category, light, water, difficulty, indoor, petSafe, page, size);
        PageResultVO<Plant> pageResult = PageResultVO.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
        return ResultVO.success(pageResult);
    }

    @GetMapping("/featured")
    public ResultVO<List<Plant>> getFeaturedPlants(
            @RequestParam(defaultValue = "6") int limit) {
        if (limit < 1) limit = 6;
        if (limit > 100) limit = 100;
        return ResultVO.success(plantService.getFeaturedPlants(limit));
    }

    @GetMapping("/search")
    public ResultVO<List<Plant>> searchPlants(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;
        return ResultVO.success(plantService.searchPlants(q, limit));
    }

    private int clampPage(int page) {
        return Math.max(page, 1);
    }

    private int clampSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    @GetMapping("/{slug}")
    public ResultVO<Plant> getPlantBySlug(@PathVariable String slug) {
        Plant plant = plantService.getPlantBySlug(slug);
        if (plant == null) {
            return ResultVO.error(404, "Plant not found");
        }
        return ResultVO.success(plant);
    }

    @GetMapping("/{id}/care-guides")
    public ResultVO<List<CareGuide>> getCareGuides(
            @PathVariable Long id,
            @RequestParam(required = false) String season) {
        return ResultVO.success(plantService.getCareGuides(id, season));
    }
}
