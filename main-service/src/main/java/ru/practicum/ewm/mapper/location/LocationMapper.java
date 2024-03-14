package ru.practicum.ewm.mapper.location;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.model.location.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationMapper {

    public static Location toLocation(LocationDto locationDto) {
        return Location.builder()
                .lon(locationDto.getLon())
                .lat(locationDto.getLat())
                .build();
    }

    public static LocationDto toLocationDto(Location location) {
        return LocationDto.builder()
                .lon(location.getLon())
                .lat(location.getLat())
                .build();
    }
}
