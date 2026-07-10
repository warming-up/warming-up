package com.warmingup.backend.appointment.service;

import com.warmingup.backend.appointment.dto.AppointmentCreateRequest;
import com.warmingup.backend.appointment.dto.AppointmentResponse;
import com.warmingup.backend.appointment.repository.AppointmentRepository;
import com.warmingup.backend.appointment.support.AppointmentTimeCalculation;
import com.warmingup.backend.appointment.support.AppointmentTimeCalculator;
import com.warmingup.backend.appointment.support.StepTimeline;
import com.warmingup.backend.domain.Appointment;
import com.warmingup.backend.domain.AppointmentItem;
import com.warmingup.backend.domain.ItemType;
import com.warmingup.backend.domain.Routine;
import com.warmingup.backend.domain.RoutineItem;
import com.warmingup.backend.domain.User;
import com.warmingup.backend.routine.RoutineRepository;
import com.warmingup.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final RoutineRepository routineRepository;
    private final UserRepository userRepository;
    private final AppointmentTimeCalculator appointmentTimeCalculator;

    @Transactional
    public AppointmentResponse createAppointment(Long currentUserId, AppointmentCreateRequest request) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
        Routine routine = findRoutine(currentUserId, request.routineId());
        List<RoutineItem> routineItems = routine == null ? List.of() : routine.getItems();
        List<RoutineItem> steps = routineItems.stream()
                .filter(item -> item.getItemType() == ItemType.STEP)
                .toList();

        AppointmentTimeCalculation calculation = appointmentTimeCalculator.calculate(
                request.arrivalTime(),
                request.travelMinutes(),
                request.bufferMinutes(),
                steps.stream()
                        .map(RoutineItem::getDurationMinutes)
                        .toList()
        );

        Appointment appointment = Appointment.builder()
                .user(user)
                .routine(routine)
                .name(request.name())
                .arrivalTime(request.arrivalTime())
                .travelMinutes(request.travelMinutes())
                .bufferMinutes(request.bufferMinutes())
                .preparationStartTime(calculation.preparationStartTime())
                .departureTime(calculation.departureTime())
                .build();

        copyRoutineItems(appointment, routineItems, calculation.stepTimelines());
        return AppointmentResponse.from(appointmentRepository.saveAndFlush(appointment));
    }

    private Routine findRoutine(Long currentUserId, Long routineId) {
        if (routineId == null) {
            return null;
        }
        return routineRepository.findByIdAndUserIdWithItems(routineId, currentUserId)
                .orElseThrow(() -> new NoSuchElementException("루틴을 찾을 수 없습니다."));
    }

    private void copyRoutineItems(Appointment appointment, List<RoutineItem> routineItems, List<StepTimeline> stepTimelines) {
        int stepIndex = 0;
        for (RoutineItem routineItem : routineItems) {
            if (routineItem.getItemType() == ItemType.STEP) {
                StepTimeline timeline = stepTimelines.get(stepIndex++);
                appointment.addItem(AppointmentItem.builder()
                        .name(routineItem.getName())
                        .itemType(ItemType.STEP)
                        .durationMinutes(routineItem.getDurationMinutes())
                        .itemOrder(routineItem.getItemOrder())
                        .startTime(timeline.startTime())
                        .endTime(timeline.endTime())
                        .completed(false)
                        .build());
                continue;
            }

            appointment.addItem(AppointmentItem.builder()
                    .name(routineItem.getName())
                    .itemType(ItemType.CHECKLIST)
                    .durationMinutes(0)
                    .itemOrder(routineItem.getItemOrder())
                    .completed(false)
                    .build());
        }
    }
}
