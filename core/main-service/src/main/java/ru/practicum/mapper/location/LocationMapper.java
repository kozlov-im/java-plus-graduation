package ru.practicum.mapper.location;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toLocationDto(Location location);

    @Mapping(target = "id", ignore = true)
    Location toLocation(LocationDto locationDto);
}
