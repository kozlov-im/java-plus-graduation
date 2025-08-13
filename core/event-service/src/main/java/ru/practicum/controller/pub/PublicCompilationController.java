package ru.practicum.controller.pub;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.compilation.CompilationService;

import java.util.List;

@RestController
@RequestMapping(path = "/compilations")
@Slf4j
@RequiredArgsConstructor
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping()
    List<CompilationDto> getCompilations(@RequestParam(required = false, name = "pinned") @Valid Boolean pinned,
                                         @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(name = "size", defaultValue = "10") @PositiveOrZero int size) {
        log.info("==> getCompilations with pinned = {}, from = {}, size = {}", pinned, from, size);
        List<CompilationDto> compilationDtos = compilationService.getCompilations(pinned, from, size);
        log.info("<== getCompilations result: {}", compilationDtos);

        return compilationDtos;
    }

    @GetMapping("/{comp-id}")
    CompilationDto get(@PathVariable("comp-id") @Positive long compId) {
        log.info("==> get by compId = {}", compId);
        CompilationDto compilationDto = compilationService.get(compId);
        log.info("<== get result: {}", compilationDto);

        return compilationDto;
    }
}
