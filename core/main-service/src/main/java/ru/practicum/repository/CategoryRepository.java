package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query(value = "select * " +
                   "from categories " +
                   "order by category_id OFFSET :from LIMIT :size",
            nativeQuery = true)
    List<Category> getCategories(@Param("from") int from, @Param("size") int size);

    public boolean existsByName(String name);
}
