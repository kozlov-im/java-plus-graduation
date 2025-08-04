package ru.practicum.service.category;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.category.CategoryMapper;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.model.Category;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    EventRepository eventRepository;
    CategoryMapper categoryMapper;

    static final String CATEGORY_NOT_FOUND = "Категория с id = %d не найдена";

    @Transactional
    @Override
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Категория с таким именем уже существует", "");
        }
        Category category = categoryMapper.dtoToCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(savedCategory);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void delete(Long id) {
        validateId(id);
        // Проверка на связанные события
        boolean hasEvents = eventRepository.existsByCategoryId(id); // Метод проверки в репозитории
        if (hasEvents) {
            log.error("Удаление невозможно: категория с id = {} связана с событиями.", id);
            throw new ConflictException("Удаление невозможно", "C категорией связано одно или несколько событий.");
        }
        categoryRepository.deleteById(id);
        log.info("Категория с id = {} успешно удалена.", id);
    }

    @Override
    public CategoryDto update(Long id, NewCategoryDto newCategoryDto) {
        Category existCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND, "Объект не найден"));
        String newName = newCategoryDto.getName();
        if (!Objects.equals(newName, existCategory.getName()) && categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException("Категория с таким именем уже существует", "Ошибка обновления категории");
        }
        existCategory.setName(newCategoryDto.getName());

        Category updateCategory = categoryRepository.save(existCategory);
        return categoryMapper.toCategoryDto(updateCategory);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        return categoryMapper.toCategoriesDto(categoryRepository.getCategories(from, size));
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category existCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND, "Объект не найден"));
        return categoryMapper.toCategoryDto(existCategory);
    }

    private void validateId(Long id) {
        if (!categoryRepository.existsById(id)) {
            log.error("Указана несуществующая категория с id: {}", id);
            throw new NotFoundException(CATEGORY_NOT_FOUND, "Объект не существует");
        }
    }


}
