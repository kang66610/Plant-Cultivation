package com.plantcultivation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plantcultivation.entity.CareGuide;
import com.plantcultivation.entity.Plant;
import com.plantcultivation.mapper.CareGuideMapper;
import com.plantcultivation.mapper.PlantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantService extends ServiceImpl<PlantMapper, Plant> {

    private final PlantMapper plantMapper;
    private final CareGuideMapper careGuideMapper;

    public Page<Plant> listPlants(String search, String category, String light,
                                  String water, String difficulty, Boolean indoor,
                                  Boolean petSafe, int page, int size) {
        if (category != null && !category.isBlank()) {
            return plantMapper.selectPageByCategorySlug(new Page<>(page, size),
                    category, search, light, water, difficulty, indoor, petSafe);
        }

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
        wrapper.orderByAsc("common_name");
        return plantMapper.selectPage(new Page<>(page, size), wrapper);
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
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return plantMapper.naturalSearch(keyword, limit);
    }

    public List<Plant> getPlantsByCategory(String categorySlug) {
        return plantMapper.selectByCategorySlug(categorySlug);
    }

    public List<CareGuide> getCareGuides(Long plantId, String season) {
        if (season != null && !season.isBlank() && !season.equals("all")) {
            return careGuideMapper.selectByPlantIdAndSeason(plantId, season);
        }
        return careGuideMapper.selectByPlantId(plantId);
    }
}
