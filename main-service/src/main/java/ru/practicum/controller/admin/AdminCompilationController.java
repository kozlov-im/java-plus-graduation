package ru.practicum.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.compilation.CompilationService;

@RestController
@RequestMapping(path = "/admin/compilations")
@Slf4j
@RequiredArgsConstructor
public class AdminCompilationController {

    private static final String PATH = "comp-id";
    private final CompilationService compilationService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    CompilationDto add(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("==> Admin add by newCompilationDto = {}", newCompilationDto);
        CompilationDto compilationDto = compilationService.add(newCompilationDto);
        log.info("<== Admin add result: {}", compilationDto);

        return compilationDto;
    }

    @DeleteMapping("/{comp-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable(PATH) @Positive long compId) {
        log.info("==> Admin delete for compId = {}", compId);
        compilationService.delete(compId);
    }

    @PatchMapping("/{comp-id}")
    CompilationDto update(@PathVariable(PATH) @Positive long compId,
                          @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        log.info("==> Admin updating category: {}", updateCompilationRequest);
        CompilationDto compilationDto = compilationService.update(compId, updateCompilationRequest);
        log.info("<== Admin updating user: {}", compilationDto);

        return compilationDto;
    }
}
