package ru.practicum.mapper.category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category dtoToCategory(NewCategoryDto newCategoryDto);

    List<CategoryDto> toCategoriesDto(List<Category> categories);

    Category toEntity(CategoryDto categoryDto);

    Category toCategoryFromCategoryDto(CategoryDto categoryDto);
}
