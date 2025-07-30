package ru.practicum.service.compilation;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto add(NewCompilationDto newCompilationDto);

    CompilationDto update(long compId, UpdateCompilationRequest updateCompilationRequest);

    CompilationDto get(long compId);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    void delete(long compId);
}
