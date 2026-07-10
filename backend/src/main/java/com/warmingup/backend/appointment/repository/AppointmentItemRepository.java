package com.warmingup.backend.appointment.repository;

import com.warmingup.backend.domain.AppointmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentItemRepository extends JpaRepository<AppointmentItem, Long> {

    Optional<AppointmentItem> findByIdAndAppointmentIdAndAppointmentUserId(
            Long itemId,
            Long appointmentId,
            Long userId
    );
}
