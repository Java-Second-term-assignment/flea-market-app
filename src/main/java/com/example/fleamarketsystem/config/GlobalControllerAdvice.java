package com.example.fleamarketsystem.config;

import com.example.fleamarketsystem.entity.Category;
import com.example.fleamarketsystem.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CategoryService categoryService;

    @Autowired
    public GlobalControllerAdvice(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ModelAttribute("categories")
    public List<Category> addCategories() {
        return categoryService.getAllCategories();
    }
}

