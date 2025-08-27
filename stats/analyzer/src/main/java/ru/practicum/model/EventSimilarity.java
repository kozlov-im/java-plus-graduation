package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@Table(name = "event_similarity")
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(SimilarityId.class)
public class EventSimilarity {

    @Id
    @Column(name = "event_a")
    Long eventA;

    @Id
    @Column(name = "event_b")
    Long eventB;

    Double score;

    @Column(name = "created")
    Instant timestamp;
}

