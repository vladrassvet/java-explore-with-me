package ru.practicum.ewm.service.compilations;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.compilations.CompilationDto;
import ru.practicum.ewm.dto.compilations.NewCompilationDto;
import ru.practicum.ewm.dto.compilations.UpdateCompilationRequest;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.category.CategoryMapper;
import ru.practicum.ewm.mapper.compilations.CompilationMapper;
import ru.practicum.ewm.mapper.event.EventMapper;
import ru.practicum.ewm.mapper.user.UserMapper;
import ru.practicum.ewm.model.compilations.Compilation;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.repository.compilations.CompilationsRepository;
import ru.practicum.ewm.repository.event.EventRepository;
import ru.practicum.ewm.repository.request.RequestRepository;
import ru.practicum.ewm.service.view.ViewService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final EventRepository eventRepository;
    private final CompilationsRepository compilationsRepository;
    private final ViewService viewService;
    private final RequestRepository requestRepository;

    @Override
    public CompilationDto saveCompilation(NewCompilationDto newCompilationDto) {
        if (newCompilationDto.getPinned() == null) {
            newCompilationDto.setPinned(false);
        }
        List<Event> eventList;
        if (newCompilationDto.getEvents() == null || newCompilationDto.getEvents().isEmpty()) {
            eventList = new ArrayList<>();
        } else {
            eventList = eventRepository.findByIdIn(newCompilationDto.getEvents());
        }
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto, eventList);
        try {
            compilationsRepository.save(compilation);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Нарушение целостности данных");
        }
        log.info("Подборка добавлена {}", compilation);
        return returnToCompilationDto(compilation);
    }

    @Override
    public void deleteCompilation(long compId) {
        Compilation compilation = compilationsRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена или недоступна"));
        compilationsRepository.delete(compilation);
        log.info("Подборка удалена {}", compilation);
    }

    @Override
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationsRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена или недоступна"));
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(eventRepository.findByIdIn(updateCompilationRequest.getEvents()));
        }
        Compilation updateCompilation = compilationsRepository.save(compilation);
        log.info("Подборка обновлена {}", updateCompilation);
        return returnToCompilationDto(updateCompilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        List<Compilation> compilation = new ArrayList<>();
        if (pinned != null) {
            compilation = compilationsRepository.findAllByPinned(pinned, PageRequest.of(from / size, size));
        } else {
            compilation = compilationsRepository.findAll(PageRequest.of(from / size, size)).getContent();
        }
        log.info("Найдены подборки событий {}", compilation);
        return compilation.stream().map(this::returnToCompilationDto).collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationsById(long compId) {
        Compilation compilation = compilationsRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка не найдена или недоступна"));
        log.info("Подборка событий найдена {}", compilation);
        return returnToCompilationDto(compilation);
    }

    private CompilationDto returnToCompilationDto(Compilation compilation) {
        List<EventShortDto> eventShortDtoList = new ArrayList<>();
        Map<Long, Integer> viewsMap = viewService.getViews(compilation.getEvents());

        for (Event event : compilation.getEvents()) {
            Integer confirmedRequest = requestRepository.findAllByEvent(event).size();
            CategoryDto categoryDto = CategoryMapper.toDto(event.getCategory());
            UserShortDto userShortDto = UserMapper.toUserShortDto(event.getInitiator());
            EventShortDto eventShortDto = EventMapper.toEventShortDto(event, confirmedRequest, categoryDto,
                    userShortDto, viewsMap);
            eventShortDtoList.add(eventShortDto);
        }
        return CompilationMapper.toCompilationDto(compilation, eventShortDtoList);
    }
}
