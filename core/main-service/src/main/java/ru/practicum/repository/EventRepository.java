package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {

    Set<Event> findByIdIn(Set<Long> ids);

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    boolean existsByCategoryId(Long categoryId);

    @Query(value = "SELECT e.* FROM events e " +
                   "WHERE e.state = 'PUBLISHED' " +
                   "AND (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
                   "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
                   "AND (:categories IS NULL OR e.category_id = ANY(COALESCE(:categories, '{}'))) " +
                   "AND (:paid IS NULL OR e.paid = :paid) " +
                   "AND (e.event_date >= :rangeStart) " +
                   "AND (e.event_date <= :rangeEnd)", nativeQuery = true)
    List<Event> findAllEvents(@Param("text") String text,
                              @Param("categories") Long[] categories,
                              @Param("paid") Boolean paid,
                              @Param("rangeStart") LocalDateTime rangeStart,
                              @Param("rangeEnd") LocalDateTime rangeEnd);
}
