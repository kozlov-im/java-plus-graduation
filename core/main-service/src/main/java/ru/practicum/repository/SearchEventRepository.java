package ru.practicum.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.event.SearchEventsParamAdmin;
import ru.practicum.model.Event;

import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class SearchEventRepository {
    private final EntityManager entityManager;

    public List<Event> getEventsByParamForAdmin(SearchEventsParamAdmin parameters) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = criteriaBuilder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        if (parameters.getUsers() != null && !parameters.getUsers().isEmpty() && parameters.getUsers().get(0) != 0) {
            predicates.add(root.get("initiator").get("id").in(parameters.getUsers()));
        }

        if (parameters.getStates() != null && !parameters.getStates().isEmpty()) {
            predicates.add(root.get("state").in(parameters.getStates()));
        }

        if (parameters.getCategories() != null && !parameters.getCategories().isEmpty() && parameters.getCategories().get(0) != 0) {
            predicates.add(root.get("category").get("id").in(parameters.getCategories()));
        }

        if (parameters.getRangeStart() != null && parameters.getRangeEnd() != null) {
            predicates.add(criteriaBuilder.between(root.get("eventDate"), parameters.getRangeStart(), parameters.getRangeEnd()));
        }
        query.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(query).setFirstResult(parameters.getFrom())
                .setMaxResults(parameters.getSize())
                .getResultList();

    }
}
