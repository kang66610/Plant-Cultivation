package com.plantcultivation.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plantcultivation.entity.Category;
import com.plantcultivation.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {
}
