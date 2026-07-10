package com.warmingup.backend.routine;

import com.warmingup.backend.domain.ItemType;
import com.warmingup.backend.domain.Routine;
import com.warmingup.backend.domain.RoutineItem;
import com.warmingup.backend.domain.User;
import com.warmingup.backend.routine.dto.RoutineCreateRequest;
import com.warmingup.backend.routine.dto.RoutineResponse;
import com.warmingup.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoutineResponse create(Long userId, RoutineCreateRequest request) {
        User user = getUser(userId);
        Routine routine = Routine.builder()
                .user(user)
                .name(request.name())
                .build();

        addItemsToRoutine(routine, request);
        return RoutineResponse.from(routineRepository.save(routine));
    }

    public List<RoutineResponse> findAll(Long userId) {
        User user = getUser(userId);
        return routineRepository.findAllByUser(user).stream()
                .map(RoutineResponse::from)
                .toList();
    }

    public RoutineResponse findById(Long userId, Long routineId) {
        return RoutineResponse.from(getRoutine(userId, routineId));
    }

    @Transactional
    public RoutineResponse update(Long userId, Long routineId, RoutineCreateRequest request) {
        Routine routine = getRoutine(userId, routineId);
        routine.updateName(request.name());
        routine.getItems().clear();
        addItemsToRoutine(routine, request);
        return RoutineResponse.from(routine);
    }

    @Transactional
    public void delete(Long userId, Long routineId) {
        routineRepository.delete(getRoutine(userId, routineId));
    }

    private void addItemsToRoutine(Routine routine, RoutineCreateRequest request) {
        if (request.steps() != null) {
            for (var step : request.steps()) {
                routine.getItems().add(RoutineItem.builder()
                        .routine(routine)
                        .name(step.name())
                        .itemType(ItemType.STEP)
                        .durationMinutes(step.durationMinutes())
                        .itemOrder(step.stepOrder())
                        .build());
            }
        }
        if (request.checklist() != null) {
            for (int i = 0; i < request.checklist().size(); i++) {
                routine.getItems().add(RoutineItem.builder()
                        .routine(routine)
                        .name(request.checklist().get(i))
                        .itemType(ItemType.CHECKLIST)
                        .durationMinutes(0)
                        .itemOrder(i + 1)
                        .build());
            }
        }
    }

    private Routine getRoutine(Long userId, Long routineId) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new NoSuchElementException("루틴을 찾을 수 없습니다."));
        if (!routine.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        return routine;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
    }
}
