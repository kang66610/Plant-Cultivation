package com.plantcultivation.controller;

import com.plantcultivation.entity.Category;
import com.plantcultivation.entity.Plant;
import com.plantcultivation.service.CategoryService;
import com.plantcultivation.service.PlantService;
import com.plantcultivation.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final PlantService plantService;

    @GetMapping
    public ResultVO<List<Category>> listCategories() {
        return ResultVO.success(categoryService.list());
    }

    @GetMapping("/{slug}/plants")
    public ResultVO<List<Plant>> getPlantsByCategory(@PathVariable String slug) {
        return ResultVO.success(plantService.getPlantsByCategory(slug));
    }
}
