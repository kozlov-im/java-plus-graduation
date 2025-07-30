package ru.practicum.mapper.compilation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.model.Compilation;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CompilationsMapperUtil.class})
public interface CompilationMapper {

    @Mapping(target = "events", qualifiedByName = {"CompilationsMapperUtil", "getEventShortDtos"}, source = "compilation")
    CompilationDto toCompilationDto(Compilation compilation);

    List<CompilationDto> toCompilationDtos(List<Compilation> compilations);
}
