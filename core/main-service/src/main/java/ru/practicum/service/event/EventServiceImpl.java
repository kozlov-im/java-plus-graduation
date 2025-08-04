package ru.practicum.service.event;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.SearchEventsParamAdmin;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.event.EventMapper;
import ru.practicum.mapper.event.UtilEventClass;
import ru.practicum.mapper.request.RequestMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.Request;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.RequestRepository;
import ru.practicum.repository.SearchEventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.category.CategoryService;
import ru.practicum.state.AdminStateAction;
import ru.practicum.state.EventState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {

    EventRepository eventRepository;
    UserRepository userRepository;
    RequestRepository requestRepository;
    RequestMapper requestMapper;
    EventMapper eventMapper;
    CategoryService categoryService;
    UtilEventClass utilEventClass;
    LocationRepository locationRepository;
    SearchEventRepository searchEventRepository;
    CategoryRepository categoryRepository;
    StatClient statClient;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository,
                            RequestRepository requestRepository, RequestMapper requestMapper,
                            EventMapper eventMapper, CategoryService categoryService, UtilEventClass utilEventClass,
                            LocationRepository locationRepository, SearchEventRepository searchEventRepository,
                            CategoryRepository categoryRepository, StatClient statClient) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.eventMapper = eventMapper;
        this.categoryService = categoryService;
        this.utilEventClass = utilEventClass;
        this.locationRepository = locationRepository;
        this.searchEventRepository = searchEventRepository;
        this.categoryRepository = categoryRepository;
        this.statClient = statClient;
    }

    @Override
    public List<ParticipationRequestDto> getRequestByUserId(Long userId) {
        userRepository.findById(userId);
        List<Request> requestList = requestRepository.findAllByRequesterId(userId);
        return requestList.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequestDto> getRequestByUserAndEvent(Long userId, Long eventId) {
        List<Request> requestList = requestRepository.findAllByEventId(eventId);
        return requestList.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + " not found!", ""));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " not found!", ""));
        requestToEventVerification(user, event);
        Request request = requestMapper.formUserAndEventToRequest(user, event);
        requestRepository.save(request);
        return requestMapper.toParticipationRequestDto(request);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found!", ""));
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Object with id=" + requestId + " was not found!", "")
        );
        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);

        return requestMapper.toParticipationRequestDto(request);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult requestUpdateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        List<Request> requestList = requestRepository
                .findByIdInAndEventId(eventRequestStatusUpdateRequest.getRequestIds(), eventId);
        return requestUpdateVerification(eventId, requestList, eventRequestStatusUpdateRequest.getStatus());
    }


    private void requestToEventVerification(User user, Event event) {
        long userId = user.getId();

        if (requestRepository.findAllByRequesterId(userId).stream()
                .map(r -> r.getEvent().equals(event))
                .toList().contains(true)) {
            throw new ConflictException("User with id=" + userId +
                                        " has already made a request for participation in the event with id=" + event.getId(), "");
        }
        if (userId == event.getInitiator().getId() && event.getInitiator() != null) {
            throw new ConflictException("Initiator of event with id=" + userId +
                                        " cannot add request for participation in his own event", "");
        }
        if (event.getPublishedOn() == null) {
            throw new ConflictException("", "You cannot participate in an unpublished event id=" + event.getId());
        }
        if (event.getParticipantLimit() != 0) {
            long countRequests = requestRepository.countByStatusAndEventId(RequestStatus.CONFIRMED, event.getId());
            if (countRequests >= event.getParticipantLimit()) {
                throw new ConflictException("The event with id=" + event.getId() + " has reached the limit of participation requests", "");
            }
        }
    }

    private EventRequestStatusUpdateResult requestUpdateVerification(Long eventId, List<Request> requestList, RequestStatus status) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        for (Request request : requestList) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("You can only change the status of pending applications", "");
            }
            int count = requestRepository.countByStatusAndEventId(RequestStatus.CONFIRMED, eventId);

            Event event = request.getEvent();
            if (count >= event.getParticipantLimit()) {
                throw new ConflictException("The event with id=" + event.getId() +
                                            " has reached the limit of participation requests", "");
            }
            if (request.getEvent().getId().equals(eventId)) {
                request.setStatus(status);
                requestRepository.save(request);

                if (status == RequestStatus.CONFIRMED) {
                    confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
                } else if (status == RequestStatus.REJECTED) {
                    rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
                }
            }
        }
        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);

        Integer count = requestRepository.countByStatusAndEventId(RequestStatus.CONFIRMED, eventId);
        Event event = eventRepository.getReferenceById(eventId);
        event.setConfirmedRequests(count);
        eventRepository.save(event);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsForUser(Long userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequest.of(from, size));

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto eventDto) {
        User initializer = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("User with id=" + userId + " not found!", ""));
        CategoryDto category = categoryService.getCategoryById(eventDto.getCategory());
        Location location = eventDto.getLocation();
        locationRepository.save(location);
        Event event = utilEventClass.toEventFromNewEventDto(eventDto, initializer, category, location);

        if (eventDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (eventDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (eventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        event = eventRepository.save(event);
        return utilEventClass.toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByIdForUser(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " not found!", ""));
        return utilEventClass.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto changeEvent(Long userId, Long eventId, UpdateEventAdminRequest eventDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " not found!", ""));
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("event with id=" + eventId + "published and cannot be changed", "");
        }
        if (eventDto.getStateAction() != null) {
            switch (eventDto.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
            }
        }
        utilEventClass.updateEventFromDto(event, eventDto);

        event = eventRepository.save(event);
        return utilEventClass.toEventFullDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Integer from, Integer size) {
        checkDateTime(rangeStart, rangeEnd);
        SearchEventsParamAdmin searchEventsParamAdmin = SearchEventsParamAdmin.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
        List<Event> events = searchEventRepository.getEventsByParamForAdmin(searchEventsParamAdmin);
        return events.stream().map(utilEventClass::toEventFullDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest request, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " not found!", ""));
        Category category;
        if (request.getCategory() != null) {
            category = categoryRepository.findById(request.getCategory()).orElseThrow(() ->
                    new NotFoundException("Category with id=" + request.getCategory() + " not found!", ""));
        } else {
            category = event.getCategory();
        }
        Location location = checkAndSaveLocation(request.getLocation());
        checkTimeBeforeStart(request.getEventDate(), 1);
        checkTimeBeforeStart(event.getEventDate(), 1);

        if (AdminStateAction.PUBLISH_EVENT.equals(request.getStateAction())) {
            if (event.getState().equals(EventState.PENDING)) {
                event = utilEventClass.updateEvent(event, request, category, location);
                event.setPublishedOn(LocalDateTime.now());
                event.setState(EventState.PUBLISHED);
            } else {
                throw new ConflictException("Event is not PENDING!", "");
            }
        } else if (AdminStateAction.REJECT_EVENT.equals(request.getStateAction())) {
            if (!event.getState().equals(EventState.PUBLISHED)) {
                event = utilEventClass.updateEvent(event, request, category, location);
                event.setState(EventState.CANCELED);
            } else {
                throw new ConflictException("PUBLISHED events can't be cancelled!", "event should be PENDING or CANCELED");

            }
        }
        return utilEventClass.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                         Boolean onlyAvailable, String sort, int from, int size, String clientIp) {

        // Если все параметры отсутствуют, то возвращаем пустой список и записываем статистику
        if (Boolean.TRUE.equals(text == null && categories == null && paid == null && rangeStart == null && rangeEnd == null
                                && !onlyAvailable && sort == null && from == 0) && size == 10) {

            log.info("==> Статистика: вызов метода getEvents с пустыми параметрами от клиента {}", clientIp);

            // Записываем статистику
            saveEventsRequestToStats(clientIp);

            // Возвращаем пустой список
            return Collections.emptyList();
        }

        rangeStart = (rangeStart == null) ? LocalDateTime.of(1970, 1, 1, 0, 0) : rangeStart;
        rangeEnd = (rangeEnd == null) ? LocalDateTime.of(2099, 12, 31, 23, 59) : rangeEnd;

        log.info("Параметры для SQL: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}",
                text, categories, paid, rangeStart, rangeEnd);
        List<Event> events = eventRepository.findAllEvents(
                text,
                (categories == null) ? new Long[0] : categories.toArray(new Long[0]), // Передаем пустой массив, если null
                paid,
                rangeStart,
                rangeEnd
        );

        // Получаем количество подтвержденных заявок для каждого мероприятия
        Map<Long, Long> eventRequestCounts = new HashMap<>();
        for (Event event : events) {
            long confirmedRequests = requestRepository.countByStatusAndEventId(RequestStatus.CONFIRMED, event.getId());
            eventRequestCounts.put(event.getId(), confirmedRequests);
        }

        // Фильтруем мероприятия, если onlyAvailable = true
        List<Event> filteredEvents = new ArrayList<>(events.stream()
                .filter(event -> {
                    Long confirmedRequests = eventRequestCounts.get(event.getId());
                    return !onlyAvailable || event.getParticipantLimit() == null || confirmedRequests < event.getParticipantLimit();
                })
                .toList());

        // Получаем статистику просмотров для каждого мероприятия с помощью StatClient
        Map<Long, Long> eventViews = new HashMap<>();
        List<String> uris = filteredEvents.stream()
                .map(event -> "/events/" + event.getId()) // Получаем URI для каждого мероприятия
                .toList();

        // Запрашиваем статистику просмотров с использованием StatClient
        List<ViewStatsDto> viewStats = statClient.getStats(rangeStart.toString(), rangeEnd.toString(), uris, true);

        // Обработка случая, если статистика отсутствует
        if (viewStats == null || viewStats.isEmpty()) {
            log.warn("Сервис статистики вернул пустой результат или null");
            viewStats = Collections.emptyList();
        }

        // Заполняем Map с количеством просмотров
        for (ViewStatsDto stat : viewStats) {
            Long eventId = Long.valueOf(stat.getUri().substring(stat.getUri().lastIndexOf("/") + 1));
            eventViews.put(eventId, stat.getHits());
        }

        // Сортировка
        if ("VIEWS".equalsIgnoreCase(sort)) {
            // Сортировка по количеству просмотров
            filteredEvents.sort((e1, e2) -> {
                long views1 = eventViews.getOrDefault(e1.getId(), 0L);
                long views2 = eventViews.getOrDefault(e2.getId(), 0L);
                return Long.compare(views2, views1); // по убыванию просмотров
            });
        } else if ("EVENT_DATE".equalsIgnoreCase(sort)) {
            // Сортировка по дате события
            filteredEvents.sort(Comparator.comparing(Event::getEventDate));
        }
        log.info("Передаем запрос в статистику");

        // Логируем запрос в статистику
        saveEventsRequestToStats(clientIp);

        // Применяем пагинацию
        int start = Math.min(from, filteredEvents.size());
        int end = Math.min(from + size, filteredEvents.size());
        List<Event> paginatedEvents = filteredEvents.subList(start, end);

        return paginatedEvents.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto getEventById(Long eventId, String clientIp) {
        // Проверка существования события
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " not found!", "")
        );

        // Проверка, что событие опубликовано
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Event with id=" + eventId + " is not published yet!", "");
        }

        // Увеличение количества просмотров
        saveEventRequestToStats(event, clientIp);

        // Получение количества просмотров из статистики
        long views = getViewsFromStats(event);

        event.setViews(views);
        eventRepository.save(event);

        // Подсчет подтвержденных запросов
        long confirmedRequests = requestRepository.countByStatusAndEventId(RequestStatus.CONFIRMED, eventId);

        // Создание DTO
        EventFullDto eventFullDto = utilEventClass.toEventFullDto(event);
        eventFullDto.setViews(views);
        eventFullDto.setConfirmedRequests((int) confirmedRequests);

        return eventFullDto;
    }

    private void saveEventsRequestToStats(String clientIp) {
        try {
            // Создание объекта для статистики
            log.info("Создание объекта для статистики");
            EndpointHitDto hitDto = new EndpointHitDto();
            hitDto.setApp("ewm-main-service");
            hitDto.setUri("/events");
            hitDto.setIp(clientIp);
            hitDto.setTimestamp(LocalDateTime.now());

            // Логируем успешный запрос
            log.info("Логируем запрос в статистику: URI={}, IP={}", hitDto.getUri(), hitDto.getIp());

            // Отправка статистики
            statClient.sendHit(hitDto);
        } catch (Exception e) {
            log.error("Ошибка при сохранении статистики для URI=/events, IP=" + clientIp, e);
        }
    }

    private void saveEventRequestToStats(Event event, String clientIp) {
        try {
            EndpointHitDto hitDto = new EndpointHitDto();
            hitDto.setApp("ewm-main-service");
            hitDto.setUri("/events/" + event.getId());
            hitDto.setIp(clientIp);
            hitDto.setTimestamp(LocalDateTime.now());

            statClient.sendHit(hitDto);
        } catch (Exception e) {
            log.error("Ошибка при сохранении статистики для события id=" + event.getId(), e);
        }
    }

    private long getViewsFromStats(Event event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            String uri = "/events/" + event.getId();
            // Добавляем одну секунду к началу и завершению диапазона
            String start = event.getCreatedOn().minusSeconds(1).format(formatter);
            String end = LocalDateTime.now().plusSeconds(1).format(formatter);

            List<ViewStatsDto> stats = statClient.getStats(
                    start,
                    end,
                    List.of(uri),
                    true
            );

            return stats.stream()
                    .filter(stat -> stat.getUri().equals(uri))
                    .mapToLong(ViewStatsDto::getHits)
                    .sum();
        } catch (Exception e) {
            log.error("Ошибка при получении статистики просмотров для события id=" + event.getId(), e);
            return 0;
        }
    }


    private void checkDateTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("start time can't be after end time", "time range is incorrect");
        }
    }

    private Location checkAndSaveLocation(Location newLocation) {
        if (newLocation == null) {
            return null;
        }
        Location location = locationRepository.findByLatAndLon(newLocation.getLat(), newLocation.getLon())
                .orElse(null);
        if (location == null) {
            return locationRepository.save(newLocation);
        }
        return location;
    }

    private void checkTimeBeforeStart(LocalDateTime checkingTime, Integer plusHour) {
        if (checkingTime != null && checkingTime.isBefore(LocalDateTime.now().plusHours(plusHour))) {
            throw new ValidationException("updated time should be " + plusHour + "ahead then current time!", "not enough time before event");
        }
    }


}
