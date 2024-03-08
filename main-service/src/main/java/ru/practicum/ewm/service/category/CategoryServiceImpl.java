package ru.practicum.ewm.service.category;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.category.CategoryMapper;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.repository.category.CategoryRepository;
import ru.practicum.ewm.repository.event.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        Category category;
        try {
            category = categoryRepository.save(CategoryMapper.toCategory(newCategoryDto));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Нарушение целостности данных");
        }
        log.info("Категория добавлена {}", category);
        return CategoryMapper.toDto(category);
    }

    @Override
    public void deleteCategory(long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена или недоступна"));
        int sizeEvent = eventRepository.findAllByCategory_Id(catId).size();
        if (sizeEvent != 0) {
            throw new ConflictException("Существуют события, связанные с категорией");
        }
        log.info("Категория удалена {}", category);
        categoryRepository.delete(category);
    }

    @Override
    public CategoryDto updateCategory(long catId, NewCategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена или недоступна"));
        category.setName(newCategoryDto.getName());
        try {
            categoryRepository.save(category);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Нарушение целостности данных");
        }
        Category updateCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена или недоступна"));
        log.info("Изменение категории {}", updateCategory);
        return CategoryMapper.toDto(updateCategory);
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        List<Category> categories = categoryRepository.findAll(PageRequest.of(from / size, size)).toList();
        log.info("Категории найдены {}", categories);
        return categories.stream().map(CategoryMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(long catId) {
        checkId(catId);
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория не найдена или недоступна"));
        log.info("Категория найдена {}", category);
        return CategoryMapper.toDto(category);
    }

    private void checkId(long id) {
        if (id < 0) throw new ValidationException("Запрос составлен некорректно");
    }
}
