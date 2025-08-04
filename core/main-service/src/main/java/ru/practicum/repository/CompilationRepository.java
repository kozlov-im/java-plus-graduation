package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query(value = "select * " +
            "from compilations " +
            "where " +
            "pinned = :pinned " +
            "order by compilation_id ASC " +
            "OFFSET :from LIMIT :size", nativeQuery = true)
    List<Compilation> getCompilationsWithPin(@Param("from") int from, @Param("size") int size, @Param("pinned") Boolean pinned);

    @Query(value = "select * " +
            "from compilations " +
            "order by compilation_id ASC " +
            "OFFSET :from LIMIT :size", nativeQuery = true)
    List<Compilation> getCompilations(@Param("from") int from, @Param("size") int size);
}
