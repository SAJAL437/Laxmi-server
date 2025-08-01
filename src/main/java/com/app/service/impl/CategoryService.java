package com.app.service.impl;

import com.app.Repository.CategoryRepo;
import com.app.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    public Category createCategory(Category category) {
        if (category.getId() == null) {
            category.setId(UUID.randomUUID().toString()); // Generate a unique ID
        }
        return categoryRepo.save(category);
    }
}
