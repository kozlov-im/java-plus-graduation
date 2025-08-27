package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@Table(name = "user_actions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long userId;
    Long eventId;

    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    UserActionType userActionType;

    @Column(name = "created")
    Instant timestamp;

    double weight;
}

