package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.category.CategoryService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;
    private static final String PATH = "cat-id";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CategoryDto create(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("==> Admin creating category: {}", newCategoryDto);
        CategoryDto categoryDto = categoryService.create(newCategoryDto);
        log.info("<== Creating category: {}", categoryDto);
        return categoryDto;
    }

    @DeleteMapping("/{cat-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable(PATH) Long catId) {
        log.info("==> delete for catId = {}", catId);
        categoryService.delete(catId);
    }

    @PatchMapping("/{cat-id}")
    CategoryDto update(@PathVariable(PATH) Long catId, @Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("==> Admin updating category: {}", newCategoryDto);
        CategoryDto categoryDto = categoryService.update(catId, newCategoryDto);
        log.info("<== Admin updating user: {}", categoryDto);
        return categoryDto;
    }

}
