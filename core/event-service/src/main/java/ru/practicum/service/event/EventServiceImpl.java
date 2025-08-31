package ru.practicum.service.event;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.AnalyzerGrpcClient;
import ru.practicum.CollectorGrpcClient;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.enums.EventState;
import ru.practicum.enums.RequestStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.feignClient.RequestServiceFeignClient;
import ru.practicum.feignClient.UserServiceFeignClient;
import ru.practicum.mapper.event.EventMapper;
import ru.practicum.mapper.event.UtilEventClass;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.SearchEventRepository;
import ru.practicum.service.category.CategoryService;
import ru.practicum.state.AdminStateAction;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventServiceImpl implements EventService {

    EventRepository eventRepository;
    UserServiceFeignClient userClient;
    RequestServiceFeignClient requestClient;
    EventMapper eventMapper;
    CategoryService categoryService;
    UtilEventClass utilEventClass;
    LocationRepository locationRepository;
    SearchEventRepository searchEventRepository;
    CategoryRepository categoryRepository;
    AnalyzerGrpcClient analyzerGrpcClient;
    CollectorGrpcClient collectorGrpcClient;


    @Autowired
    public EventServiceImpl(EventRepository eventRepository, UserServiceFeignClient userClient,
                            RequestServiceFeignClient requestClient,
                            EventMapper eventMapper, CategoryService categoryService, UtilEventClass utilEventClass,
                            LocationRepository locationRepository, SearchEventRepository searchEventRepository,
                            CategoryRepository categoryRepository, AnalyzerGrpcClient analyzerGrpcClient, CollectorGrpcClient collectorGrpcClient) {
        this.eventRepository = eventRepository;
        this.userClient = userClient;
        this.requestClient = requestClient;
        this.eventMapper = eventMapper;
        this.categoryService = categoryService;
        this.utilEventClass = utilEventClass;
        this.locationRepository = locationRepository;
        this.searchEventRepository = searchEventRepository;
        this.categoryRepository = categoryRepository;
        this.analyzerGrpcClient = analyzerGrpcClient;
        this.collectorGrpcClient = collectorGrpcClient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsForUser(Long userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequest.of(from, size));

        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event, userClient))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto eventDto) {
        UserDto initializer = checkUser(userId);
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
            long confirmedRequests = requestClient.getRequestsCountByStatusAndEventId(RequestStatus.CONFIRMED, event.getId());
            eventRequestCounts.put(event.getId(), confirmedRequests);
        }

        // Фильтруем мероприятия, если onlyAvailable = true
        List<Event> filteredEvents = new ArrayList<>(events.stream()
                .filter(event -> {
                    Long confirmedRequests = eventRequestCounts.get(event.getId());
                    return !onlyAvailable || event.getParticipantLimit() == null || confirmedRequests < event.getParticipantLimit();
                })
                .toList());

        // Применяем пагинацию
        int start = Math.min(from, filteredEvents.size());
        int end = Math.min(from + size, filteredEvents.size());
        List<Event> paginatedEvents = filteredEvents.subList(start, end);

        return paginatedEvents.stream()
                .map(event -> eventMapper.toEventShortDto(event, userClient))
                .toList();
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        // Проверка существования события
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " not found!", "")
        );

        // Проверка, что событие опубликовано
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Event with id=" + eventId + " is not published yet!", "");
        }

        eventRepository.save(event);

        // Подсчет подтвержденных запросов
        long confirmedRequests = requestClient.getRequestsCountByStatusAndEventId(RequestStatus.CONFIRMED, event.getId());

        // Создание DTO
        EventFullDto eventFullDto = utilEventClass.toEventFullDto(event);
        eventFullDto.setConfirmedRequests((int) confirmedRequests);
        collectorGrpcClient.sendEventView(userId, eventId);
        return eventFullDto;
    }

    @Override
    public EventInitiatorDto getEventWithInitiatorId(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " not found!", "")
        );
        return eventMapper.toEventInitiatorDto(event);
    }

    @Override
    public void setConfirmedRequests(Long eventId, Integer count) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " not found!", "")
        );
        event.setConfirmedRequests(count);
        eventRepository.save(event);
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

    private UserDto checkUser(Long userId) {
        Optional<List<UserDto>> optionalUserDto = Optional.ofNullable(userClient.getUsers(List.of(userId), 0, 1));
        List<UserDto> userDtos = optionalUserDto.filter(userDto -> !userDto.isEmpty()).orElseThrow(
                () -> new NotFoundException("User with id=" + userId + " not found!", ""));
        return userDtos.getFirst();
    }

    @Override
    public List<EventShortDto> getRecommendations(Long userId, Integer maxResults) {
        checkUser(userId);
        return analyzerGrpcClient.getRecommendationsForUser(userId, maxResults).stream()
                .sorted((e1, e2) -> (int) (e1.getScore() - e2.getScore()))
                .map(recommendedEventProto -> {
                    Event event = eventRepository.findById(recommendedEventProto.getEventId()).orElseThrow(
                            () -> new NotFoundException("Event with id=" + recommendedEventProto.getEventId() + " not found!", ""));
                    return eventMapper.toEventShortDto(event, userClient);
                }).toList();
    }

    @Override
    public void setLikeEvent(Long userId, Long eventId) {
        checkUser(userId);
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id=" + eventId + " not found!", ""));
        if (event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Event is not finished yet", "");
        }

        List<ParticipationRequestDto> requestDtos = requestClient.getRequestByUserAndEvent(userId, eventId);

        boolean isPresent = requestDtos.stream().anyMatch(
                dto -> dto.getRequester().equals(userId) && dto.getStatus() == RequestStatus.CONFIRMED);
        if (isPresent) {
            collectorGrpcClient.sendEventLike(userId, eventId);
        } else {
            throw new ValidationException("user " + userId + " is not a participant or status is not confirmed", "");
        }
    }

    @Override
    public void getInteractionsCount(List<Long> eventIds) {
        Map<Long, Double> interactions = analyzerGrpcClient.getInteractionsCount(eventIds);
        System.out.println(interactions);
    }

}