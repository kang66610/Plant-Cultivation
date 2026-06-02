package com.plantcultivation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plantcultivation.entity.CareGuide;
import com.plantcultivation.entity.Category;
import com.plantcultivation.entity.Plant;
import com.plantcultivation.mapper.CareGuideMapper;
import com.plantcultivation.mapper.CategoryMapper;
import com.plantcultivation.mapper.PlantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService extends ServiceImpl<PlantMapper, Plant> {

    private final PlantMapper plantMapper;
    private final CareGuideMapper careGuideMapper;
    private final CategoryMapper categoryMapper;

    public Page<Plant> listPlants(String search, String category, String light,
                                   String water, String difficulty, Boolean indoor,
                                   Boolean petSafe, int page, int size) {
        QueryWrapper<Plant> wrapper = new QueryWrapper<>();

        if (search != null && !search.isBlank()) {
            wrapper.and(w -> w.like("common_name", search)
                    .or().like("scientific_name", search));
        }
        if (light != null && !light.isBlank()) {
            wrapper.eq("light_level", light);
        }
        if (water != null && !water.isBlank()) {
            wrapper.eq("water_frequency", water);
        }
        if (difficulty != null && !difficulty.isBlank()) {
            wrapper.eq("difficulty", difficulty);
        }
        if (indoor != null) {
            wrapper.eq("is_indoor", indoor);
        }
        if (petSafe != null) {
            wrapper.eq("is_pet_safe", petSafe);
        }

        if (category != null && !category.isBlank()) {
            QueryWrapper<Category> catWrapper = new QueryWrapper<>();
            catWrapper.eq("slug", category);
            Category cat = categoryMapper.selectOne(catWrapper);
            if (cat != null) {
                wrapper.inSql("id",
                    "SELECT plant_id FROM plant_category WHERE category_id = " + cat.getId());
            }
        }

        wrapper.orderByAsc("common_name");

        Page<Plant> result = plantMapper.selectPage(new Page<>(page, size), wrapper);
        return result;
    }

    public Plant getPlantBySlug(String slug) {
        QueryWrapper<Plant> wrapper = new QueryWrapper<>();
        wrapper.eq("slug", slug);
        Plant plant = plantMapper.selectOne(wrapper);
        if (plant != null) {
            plant.setCareGuides(careGuideMapper.selectByPlantId(plant.getId()));
        }
        return plant;
    }

    public Plant getPlantById(Long id) {
        Plant plant = plantMapper.selectById(id);
        if (plant != null) {
            plant.setCareGuides(careGuideMapper.selectByPlantId(id));
        }
        return plant;
    }

    public List<Plant> getFeaturedPlants(int limit) {
        return plantMapper.selectFeatured(limit);
    }

    public List<Plant> searchPlants(String keyword, int limit) {
        return plantMapper.fullTextSearch(keyword, limit);
    }

    public List<Plant> getPlantsByCategory(String categorySlug) {
        QueryWrapper<Category> catWrapper = new QueryWrapper<>();
        catWrapper.eq("slug", categorySlug);
        Category category = categoryMapper.selectOne(catWrapper);
        if (category == null) {
            return List.of();
        }
        return plantMapper.selectByCategoryId(category.getId());
    }

    public List<CareGuide> getCareGuides(Long plantId, String season) {
        if (season != null && !season.isBlank() && !season.equals("all")) {
            return careGuideMapper.selectByPlantIdAndSeason(plantId, season);
        }
        return careGuideMapper.selectByPlantId(plantId);
    }
}
