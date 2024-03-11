package ru.practicum.ewm.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.user.UserMapper;
import ru.practicum.ewm.model.user.User;
import ru.practicum.ewm.repository.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        List<User> users;
        if (ids == null) {
            users = userRepository.findAll(PageRequest.of(from / size, size)).toList();
        } else {
            users = userRepository.findAllById(ids);
            log.info("Пользователи найдены {}", users);
        }
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());

    }

    @Override
    public UserDto registerUser(NewUserRequest newUserRequest) {
        User user;
        try {
            user = userRepository.save(UserMapper.toUser(newUserRequest));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Нарушение целостности данных");
        }
        log.info("Пользователь зарегистрирован {}", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void delete(long userId) {
        User deleteUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден или недоступен"));
        log.info("Пользователь удален {}", deleteUser);
        userRepository.delete(deleteUser);
    }
}
