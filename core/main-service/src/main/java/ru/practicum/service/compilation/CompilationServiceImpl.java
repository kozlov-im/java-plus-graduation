package ru.practicum.service.compilation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.compilation.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompilationServiceImpl implements CompilationService {

    EventRepository eventRepository;
    CompilationRepository compilationRepository;
    CompilationMapper compilationMapper;

    @Override
    public CompilationDto add(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setEvents(eventRepository.findByIdIn(new HashSet<>(newCompilationDto.getEvents())));
        compilation = compilationRepository.save(compilation);

        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);

        return compilationDto;
    }

    @Override
    public CompilationDto update(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = checkCompId(compId);
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(eventRepository.findByIdIn(new HashSet<>(updateCompilationRequest.getEvents())));
        }
        compilationRepository.save(compilation);

        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);

        return compilationDto;
    }

    @Override
    public CompilationDto get(long compId) {
        Compilation compilation = checkCompId(compId);
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilation);

        return compilationDto;
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.getCompilationsWithPin(from, size, pinned);
        } else {
            compilations = compilationRepository.getCompilations(from, size);
        }
        List<CompilationDto> compilationDtos = compilationMapper.toCompilationDtos(compilations);

        return compilationDtos;
    }

    @Override
    public void delete(long compId) {
        checkCompId(compId);
        compilationRepository.deleteById(compId);
    }

    private Compilation checkCompId(long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found",
                        "The required object was not found."));
    }
}
