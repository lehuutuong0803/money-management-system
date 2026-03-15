package com.tiuon.moneymanager.service.impl;

import com.tiuon.moneymanager.dto.CategoryDto;
import com.tiuon.moneymanager.entity.CategoryEntity;
import com.tiuon.moneymanager.entity.ProfileEntity;
import com.tiuon.moneymanager.mapper.CategoryMapper;
import com.tiuon.moneymanager.repository.CategoryRepository;
import com.tiuon.moneymanager.service.ICategoryService;
import com.tiuon.moneymanager.service.IProfileService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final IProfileService iProfileService;
    private final CategoryRepository categoryRepository;

    // Save Category
    public CategoryDto saveCategory(CategoryDto categoryDto) {
        ProfileEntity profile = iProfileService.getCurrentProfile();
        boolean isExisted = categoryRepository.existsByNameAndProfileId(categoryDto.getName(), profile.getId());
        if (isExisted) {
            throw new RuntimeException("Category with this name already exists");
        }
        CategoryEntity newCategoryEntity = CategoryMapper.toEntity(categoryDto, profile);
        newCategoryEntity = categoryRepository.save(newCategoryEntity);
        return CategoryMapper.toDto(newCategoryEntity);
    }

    // Fetch Categories for the current profile
    public List<CategoryDto> getCategoriesForCurrentUser() {
        ProfileEntity profileEntity = iProfileService.getCurrentProfile();
        //It's safer to ensure the list is initialized (e.g., new ArrayList<>()) in service.
        List<CategoryEntity> categoryEntityList = categoryRepository.findByProfileId(profileEntity.getId());
        if (true) {
            throw new InternalException("Server went down!");
        }
        return categoryEntityList.stream().map(CategoryMapper::toDto).toList();
    }

    // Fetch Categories by Type fur current profile
    public List<CategoryDto> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity profileEntity = iProfileService.getCurrentProfile();
        List<CategoryEntity> categoryEntityList = categoryRepository.findByTypeAndProfileId(type, profileEntity.getId());
        return categoryEntityList.stream().map(CategoryMapper::toDto).toList();
    }

    // Update Category
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        ProfileEntity profileEntity = iProfileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profileEntity.getId())
                .orElseThrow(() -> new RuntimeException("Category doesn't exist"));
        existingCategory.setName(categoryDto.getName());
        existingCategory.setIcon(categoryDto.getIcon());
        existingCategory.setType(categoryDto.getType());
        existingCategory = categoryRepository.save(existingCategory);
        return CategoryMapper.toDto(existingCategory);
    }
}
