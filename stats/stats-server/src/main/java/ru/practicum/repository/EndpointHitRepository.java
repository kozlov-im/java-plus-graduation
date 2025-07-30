package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Integer> {

    String SELECT_STAT_WITHOUT_UNIQUE_IP_SQL = "SELECT " +
            "new ru.practicum.dto.ViewStatsDto(e.app, e.uri, " +
            "(SELECT count(ep.ip) FROM EndpointHit AS ep WHERE ep.uri = e.uri) AS hits) " +
            "FROM EndpointHit AS e WHERE e.uri IN ( ?3 ) AND e.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY e.uri, e.app ORDER BY hits DESC ";

    String SELECT_STAT_WITH_UNIQUE_IP_SQL = "SELECT " +
            "new ru.practicum.dto.ViewStatsDto(e.app, e.uri, " +
            "(SELECT count(DISTINCT ep.ip) FROM EndpointHit AS ep WHERE ep.uri = e.uri) AS hits) " +
            "FROM EndpointHit AS e WHERE e.uri IN ( ?3 ) AND e.timestamp BETWEEN ?1 AND ?2 " +
            "GROUP BY e.uri, e.app ORDER BY hits DESC ";

    String SELECT_STAT_ALL_WITHOUT_UNIQUE_IP_SQL = "SELECT " +
            "new ru.practicum.dto.ViewStatsDto(e.app, e.uri, " +
            "(SELECT count(ep.ip) FROM EndpointHit AS ep WHERE ep.uri = e.uri) AS hits) " +
            "FROM EndpointHit AS e WHERE e.timestamp BETWEEN ?1 AND ?2 GROUP BY e.uri, e.app ORDER BY hits DESC ";

    String SELECT_STAT_ALL_WITH_UNIQUE_IP_SQL = "SELECT " +
            "new ru.practicum.dto.ViewStatsDto(e.app, e.uri, " +
            "(SELECT count(DISTINCT ep.ip) FROM EndpointHit AS ep WHERE ep.uri = e.uri) AS hits) " +
            "FROM EndpointHit AS e WHERE e.timestamp BETWEEN ?1 AND ?2 GROUP BY e.uri, e.app ORDER BY hits DESC ";

    @Query(SELECT_STAT_WITHOUT_UNIQUE_IP_SQL)
    List<ViewStatsDto> findStatWithoutUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(SELECT_STAT_WITH_UNIQUE_IP_SQL)
    List<ViewStatsDto> findStatWithUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(SELECT_STAT_ALL_WITHOUT_UNIQUE_IP_SQL)
    List<ViewStatsDto> findStatAllWithoutUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query(SELECT_STAT_ALL_WITH_UNIQUE_IP_SQL)
    List<ViewStatsDto> findStatAllWithUniqueIp(LocalDateTime start, LocalDateTime end);
}