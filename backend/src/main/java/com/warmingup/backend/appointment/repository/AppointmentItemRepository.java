package com.warmingup.backend.appointment.repository;

import com.warmingup.backend.domain.AppointmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentItemRepository extends JpaRepository<AppointmentItem, Long> {
}
