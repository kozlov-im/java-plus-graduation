package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ValueMapping;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.UserAction;
import ru.practicum.model.UserActionType;

@Mapper(componentModel = "spring")
public interface AnalyzerMapper {

    @Named("mapActionTypeAvroToUserActionType")
    @ValueMapping(source = "VIEW", target = "VIEW")
    @ValueMapping(source = "REGISTER", target = "REGISTER")
    @ValueMapping(source = "LIKE", target = "LIKE")
    UserActionType mapActionTypeAvroToUserActionType(ActionTypeAvro source);

    @Mapping(source = "actionType", target = "userActionType", qualifiedByName = "mapActionTypeAvroToUserActionType")
    @Mapping(target = "weight", expression = "java(mapToWeight(avro.getActionType()))")
    UserAction mapToUserAction(UserActionAvro avro);

    default double mapToWeight(ActionTypeAvro typeAvro) {
        return switch (typeAvro) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
        };
    }

    EventSimilarity mapToEventSimilarity(EventSimilarityAvro avro);

}
