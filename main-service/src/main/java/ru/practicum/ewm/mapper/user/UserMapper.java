package ru.practicum.ewm.mapper.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.model.user.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static UserShortDto toUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static User toUser(NewUserRequest userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }
}
