package ru.practicum.controller.pub;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.service.category.CategoryService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/categories")
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping()
    List<CategoryDto> getCategories(@RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                    @RequestParam(name = "size", defaultValue = "10") @PositiveOrZero int size) {
        log.info("==> getCategories from = {}, size = {}", from, size);
        List<CategoryDto> categoriesDto = categoryService.getCategories(from, size);
        log.info("<== getCategories result: {}", categoriesDto);
        return categoriesDto;
    }

    @GetMapping("/{cat-id}")
    CategoryDto getCategoryById(@PathVariable("cat-id") Long catId) {
        log.info("==> getCategoryById = {}", catId);
        return categoryService.getCategoryById(catId);
    }

}
