package com.warmingup.backend.appointment.service;

import com.warmingup.backend.appointment.dto.AppointmentCreateRequest;
import com.warmingup.backend.appointment.dto.AppointmentResponse;
import com.warmingup.backend.appointment.repository.AppointmentRepository;
import com.warmingup.backend.domain.ItemType;
import com.warmingup.backend.domain.Routine;
import com.warmingup.backend.domain.RoutineItem;
import com.warmingup.backend.domain.User;
import com.warmingup.backend.routine.RoutineRepository;
import com.warmingup.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createsAppointmentWithRoutineSnapshotAndTimeline() {
        User user = saveUser("appointment-service-user@example.com");
        Routine routine = saveRoutineWithItems(user);

        AppointmentResponse response = appointmentService.createAppointment(
                user.getId(),
                new AppointmentCreateRequest(
                        routine.getId(),
                        "토스 면접",
                        LocalDateTime.of(2026, 7, 10, 18, 0),
                        40,
                        10
                )
        );

        assertThat(response.departureTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 17, 10));
        assertThat(response.preparationStartTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 16, 20));
        assertThat(response.steps()).hasSize(2);
        assertThat(response.steps().get(0).id()).isNotNull();
        assertThat(response.steps().get(0).name()).isEqualTo("씻기");
        assertThat(response.steps().get(0).startTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 16, 20));
        assertThat(response.steps().get(0).endTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 16, 40));
        assertThat(response.steps().get(0).completed()).isFalse();
        assertThat(response.steps().get(1).startTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 16, 40));
        assertThat(response.steps().get(1).endTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 17, 10));
        assertThat(response.checklist()).hasSize(1);
        assertThat(response.checklist().get(0).id()).isNotNull();
        assertThat(response.checklist().get(0).name()).isEqualTo("신분증");
        assertThat(response.checklist().get(0).completed()).isFalse();

        var appointment = appointmentRepository.findById(response.id()).orElseThrow();
        assertThat(appointment.getItems())
                .filteredOn(item -> item.getItemType() == ItemType.CHECKLIST)
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getStartTime()).isNull();
                    assertThat(item.getEndTime()).isNull();
                    assertThat(item.getAppointment()).isEqualTo(appointment);
                });
    }

    @Test
    void createsAppointmentWithoutRoutine() {
        User user = saveUser("appointment-service-empty@example.com");

        AppointmentResponse response = appointmentService.createAppointment(
                user.getId(),
                new AppointmentCreateRequest(
                        null,
                        "약속",
                        LocalDateTime.of(2026, 7, 10, 18, 0),
                        40,
                        10
                )
        );

        assertThat(response.departureTime()).isEqualTo(LocalDateTime.of(2026, 7, 10, 17, 10));
        assertThat(response.preparationStartTime()).isEqualTo(response.departureTime());
        assertThat(response.steps()).isEmpty();
        assertThat(response.checklist()).isEmpty();
    }

    @Test
    void rejectsRoutineOwnedByOtherUserAsNotFound() {
        User owner = saveUser("appointment-service-owner@example.com");
        User other = saveUser("appointment-service-other@example.com");
        Routine routine = saveRoutineWithItems(owner);

        assertThatThrownBy(() -> appointmentService.createAppointment(
                other.getId(),
                new AppointmentCreateRequest(
                        routine.getId(),
                        "약속",
                        LocalDateTime.of(2026, 7, 10, 18, 0),
                        40,
                        10
                )
        ))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("루틴");
    }

    private User saveUser(String email) {
        return userRepository.save(User.builder()
                .email(email)
                .password("password")
                .build());
    }

    private Routine saveRoutineWithItems(User user) {
        Routine routine = Routine.builder()
                .user(user)
                .name("면접 준비")
                .build();
        routine.getItems().add(RoutineItem.builder()
                .routine(routine)
                .name("씻기")
                .itemType(ItemType.STEP)
                .durationMinutes(20)
                .itemOrder(1)
                .build());
        routine.getItems().add(RoutineItem.builder()
                .routine(routine)
                .name("옷 입기")
                .itemType(ItemType.STEP)
                .durationMinutes(30)
                .itemOrder(2)
                .build());
        routine.getItems().add(RoutineItem.builder()
                .routine(routine)
                .name("신분증")
                .itemType(ItemType.CHECKLIST)
                .durationMinutes(0)
                .itemOrder(1)
                .build());
        return routineRepository.saveAndFlush(routine);
    }
}
