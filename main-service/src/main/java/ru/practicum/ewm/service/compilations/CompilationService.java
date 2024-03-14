package ru.practicum.ewm.service.compilations;

import ru.practicum.ewm.dto.compilations.CompilationDto;
import ru.practicum.ewm.dto.compilations.NewCompilationDto;
import ru.practicum.ewm.dto.compilations.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto saveCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(long compId);

    CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilationRequest);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationsById(long compId);
}
