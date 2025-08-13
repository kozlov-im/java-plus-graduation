package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(
            value = """
                    SELECT *
                    FROM comments c
                    WHERE c.event_id = :eventId
                    ORDER BY c.created DESC
                    LIMIT :size OFFSET :from
                    """,
            nativeQuery = true
    )
    List<Comment> findAllByEventId(@Param("eventId") Long eventId,
                                   @Param("from") int from,
                                   @Param("size") int size);

    @Query(
            value = """
                    SELECT *
                    FROM comments c
                    WHERE c.author_id = :userId
                    ORDER BY c.created DESC
                    LIMIT :size OFFSET :from
                    """,
            nativeQuery = true
    )
    List<Comment> findAllByUserId(@Param("userId") Long userId,
                                  @Param("from") int from,
                                  @Param("size") int size);

    Optional<List<Comment>> findAllByAuthorId(Long userId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE LOWER(c.text) LIKE CONCAT('%', LOWER(:text), '%')")
    Optional<List<Comment>> findAllByText(String text, Pageable pageable);
}
