package ru.practicum.service.event;

import org.junit.jupiter.api.Test;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;
import ru.practicum.state.EventState;

import java.time.LocalDateTime;


class EventServiceImplTest {

    @Test
    void getEventsForAdmin() {
        Event event = new Event(
                1L,
                "Эксклюзивность нашего шоу гарантирует привлечение максимальной зрительской аудитории",
                new Category(
                        1,
                        "Концерты"),
                5,
                LocalDateTime.now().plusHours(1),
                "Что получится, если соединить кукурузу и полёт? Создатели \"Шоу летающей кукурузы\" испытали эту идею на практике и воплотили в жизнь инновационный проект, предлагающий свежий взгляд на развлечения...",
                LocalDateTime.now().plusHours(2),
                new User(1L,
                        "Фёдоров Матвей",
                        "mail@mail.ru"),
                new Location(1L, 55.25f, 37.62f),
                true,
                10,
                LocalDateTime.now().minusHours(2),
                true,
                EventState.PUBLISHED,
                "title",
                999L
        );
    }
}